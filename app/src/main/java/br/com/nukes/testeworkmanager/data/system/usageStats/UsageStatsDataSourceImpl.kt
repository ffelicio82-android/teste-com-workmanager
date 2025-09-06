package br.com.nukes.testeworkmanager.data.system.usageStats

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import br.com.nukes.testeworkmanager.data.system.entities.AppUsageData
import br.com.nukes.testeworkmanager.data.system.entities.HourlyUsageData
import br.com.nukes.testeworkmanager.data.system.entities.SystemUsageData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class UsageStatsDataSourceImpl(private val context: Context): UsageStatsDataSource {
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private val packageManager = context.packageManager

    private fun getTime(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        val startTime = calendar.apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endTime = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 599)
        }.timeInMillis

        Log.d("UsageStatsDataSourceImpl", "getTime: ${convertMillisToDate(startTime)} - ${convertMillisToDate(endTime)}")

        return Pair(startTime, endTime)
    }

    fun convertMillisToDate(milliseconds: Long): String {
        // 1. Create an Instant from the milliseconds
        val instant = Instant.ofEpochMilli(milliseconds)

        // 2. Define the desired time zone (e.g., system default or a specific one)
        val zoneId = ZoneId.of("America/Sao_Paulo") // Or ZoneId.systemDefault()

        // 3. Convert the Instant to a ZonedDateTime in the specified time zone
        val zonedDateTime = instant.atZone(zoneId)

        // 4. Define the desired date and time format
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        // 5. Format the ZonedDateTime into a string
        return zonedDateTime.format(formatter)
    }

    override fun getUsageReport(installedApps: List<String>): SystemUsageData {
        return getUsageDataForPeriod(installedApps)
    }

    private fun getUsageDataForPeriod(
        installedApps: List<String>
    ): SystemUsageData {
        val (startTime, endTime) = getTime()

        // Estrutura para armazenar dados limpos
        val appUsageMap = mutableMapOf<String, AppUsageBuilder>()

        // 1. Coleta eventos para launches e foreground time
        collectUsageEvents(startTime, endTime, appUsageMap, installedApps)

        // 2. Complementa com background time estimado
        collectBackgroundTime(startTime, endTime, appUsageMap, installedApps)

        // 3. Converte para AppUsageInfo
        val appUsageList = appUsageMap.values
            .map { it.build() }
            .filter { it.totalForegroundTime > 0 }
            .sortedByDescending { it.totalForegroundTime }

        // 4. Cria dados globais do sistema
        return createSystemUsageData(appUsageList)
    }

    private fun collectUsageEvents(
        startTime: Long,
        endTime: Long,
        appUsageMap: MutableMap<String, AppUsageBuilder>,
        installedApps: List<String>
    ) {
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        val activeApps = mutableMapOf<String, Long>() // app -> quando entrou em foreground

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            val packageName = event.packageName ?: continue
            if (!installedApps.contains(packageName)) continue

            val hour = getHourFromTimestamp(event.timeStamp)

            // Inicializa builder se não existir
            if (!appUsageMap.containsKey(packageName)) {
                appUsageMap[packageName] = AppUsageBuilder(packageName, getAppName(packageName))
            }

            val builder = appUsageMap[packageName] ?: continue

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED,
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // App entrou em foreground
                    activeApps[packageName] = event.timeStamp
                }

                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    // App saiu de foreground
                    activeApps[packageName]?.let { startForeground ->
                        val duration = event.timeStamp - startForeground
                        builder.addForegroundTime(hour, duration)
                    }
                    activeApps.remove(packageName)
                }
            }
        }

        // Processa apps que ainda estavam ativos no final
        activeApps.forEach { (packageName, startTime) ->
            val duration = endTime - startTime
            val hour = getHourFromTimestamp(startTime)
            appUsageMap[packageName]?.addForegroundTime(hour, duration)
        }
    }

    private fun collectBackgroundTime(
        startTime: Long,
        endTime: Long,
        appUsageMap: Map<String, AppUsageBuilder>,
        installedApps: List<String>
    ) {
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )

        usageStatsList?.forEach { usageStats ->
            val packageName = usageStats.packageName

            if (!installedApps.contains(packageName) || usageStats.totalTimeInForeground <= 0) {
                return@forEach
            }

            // Só adiciona background time para apps que já têm foreground time
            val builder = appUsageMap[packageName]
            if (builder != null && builder.getTotalForegroundTime() > 0) {
                val hour = getHourFromTimestamp(usageStats.lastTimeUsed)

                // Background time conservativo: 20% do foreground time
                val backgroundTime = (usageStats.totalTimeInForeground * 0.2).toLong()
                builder.addBackgroundTime(hour, backgroundTime)
            }
        }
    }

    private fun createSystemUsageData(appUsageList: List<AppUsageData>): SystemUsageData {
        // Cria array de 24 horas com todos os atributos zerados explicitamente
        val systemHours = (0..23).map { hour ->
            HourlyUsageData(
                hour = hour,
                foregroundTime = 0L,
                backgroundTime = 0L
            )
        }.toMutableList()

        // Soma dados de todos os apps por hora
        appUsageList.forEach { app ->
            app.hourlyData.forEachIndexed { hour, hourData ->
                val currentHour = systemHours[hour]
                systemHours[hour] = HourlyUsageData(
                    hour = hour,
                    foregroundTime = currentHour.foregroundTime + hourData.foregroundTime,
                    backgroundTime = currentHour.backgroundTime + hourData.backgroundTime
                )
            }
        }

        return SystemUsageData(
            hourlyUsageData = systemHours,
            totalForegroundTime = systemHours.sumOf { it.foregroundTime },
            totalBackgroundTime = systemHours.sumOf { it.backgroundTime },
            apps = appUsageList.toMutableList()
        )
    }

    // Classe auxiliar para construir dados limpos
    private class AppUsageBuilder(
        val packageName: String,
        val appName: String
    ) {
        private val hourlyData = Array(24) { HourlyUsageData(it) }

        fun addForegroundTime(hour: Int, time: Long) {
            if (hour in 0..23 && time > 0) {
                val current = hourlyData[hour]
                hourlyData[hour] = current.copy(foregroundTime = current.foregroundTime + time)
            }
        }

        fun addBackgroundTime(hour: Int, time: Long) {
            if (hour in 0..23 && time > 0) {
                val current = hourlyData[hour]
                hourlyData[hour] = current.copy(backgroundTime = current.backgroundTime + time)
            }
        }

        fun getTotalForegroundTime(): Long {
            return hourlyData.sumOf { it.foregroundTime }
        }

        fun build(): AppUsageData {
            // Filtra apenas horas com dados reais para o resultado final
            val cleanHourlyData = hourlyData.toList()

            return AppUsageData(
                packageName = packageName,
                appName = appName,
                hourlyData = cleanHourlyData,
                totalForegroundTime = cleanHourlyData.sumOf { it.foregroundTime },
                totalBackgroundTime = cleanHourlyData.sumOf { it.backgroundTime }
            )
        }
    }

    private fun getHourFromTimestamp(timestamp: Long): Int {
        val instant = Instant.ofEpochMilli(timestamp)
        val zoneId = ZoneId.of("America/Sao_Paulo")
        val zonedDateTime = instant.atZone(zoneId)
        return zonedDateTime.hour
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("UsageStatsDataSourceImpl", "App não encontrado: $packageName", e)
            packageName
        }
    }
}