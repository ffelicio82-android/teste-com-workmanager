package br.com.nukes.testeworkmanager.domain.models

sealed class DownloadEvent {
    data object Started : DownloadEvent()
    data class Progress(val bytes: Long, val total: Long, val percent: Int) : DownloadEvent()
    data class Completed(val filePath: String) : DownloadEvent()
}