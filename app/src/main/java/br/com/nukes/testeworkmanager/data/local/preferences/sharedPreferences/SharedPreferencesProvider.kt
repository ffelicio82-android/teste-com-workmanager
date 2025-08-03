package br.com.nukes.testeworkmanager.data.local.preferences.sharedPreferences

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

fun provideSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("sync_prefs", MODE_PRIVATE)
}