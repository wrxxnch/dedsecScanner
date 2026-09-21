package com.example.data.update

data class GitHubReleaseInfo(
    val tagName: String,
    val name: String,
    val body: String,
    val publishedAt: String,
    val downloadUrl: String,
    val apkFileName: String
)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val release: GitHubReleaseInfo, val mandatory: Boolean = true) : UpdateState()
    object UpToDate : UpdateState()
    data class Downloading(val progressPercent: Int) : UpdateState()
    data class DownloadComplete(val apkUriString: String) : UpdateState()
    data class Error(val message: String) : UpdateState()
}
