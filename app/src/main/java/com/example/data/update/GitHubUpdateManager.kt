package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class GitHubUpdateManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("dedsec_update_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CUSTOM_REPO = "github_custom_repo"
        const val DEFAULT_REPO = "dedsec/watchdog-scanner"
    }

    var repositoryTarget: String
        get() = prefs.getString(KEY_CUSTOM_REPO, DEFAULT_REPO) ?: DEFAULT_REPO
        set(value) {
            prefs.edit().putString(KEY_CUSTOM_REPO, value.trim()).apply()
        }

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val currentAppVersionName: String = BuildConfig.VERSION_NAME
    val currentVersionName: String get() = currentAppVersionName

    /**
     * Checks GitHub repository releases for mandatory auto-updates
     */
    suspend fun checkForUpdates(mandatory: Boolean = true): UpdateState = withContext(Dispatchers.IO) {
        _updateState.value = UpdateState.Checking
        try {
            val repo = repositoryTarget.removePrefix("https://github.com/").trim('/')
            val apiUrl = "https://api.github.com/repos/$repo/releases/latest"

            val request = Request.Builder()
                .url(apiUrl)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "DedSec-Watchdog-Android")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonBody = response.body?.string() ?: ""
                val json = JSONObject(jsonBody)
                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val releaseName = json.optString("name", "DedSec Update $tagName")
                val releaseBody = json.optString("body", "Nova versão de segurança obrigatória disponível.")
                val publishedAt = json.optString("published_at", "")

                // Find APK asset or fallback to tarball/zipball
                var apkDownloadUrl = ""
                var apkName = "dedsec-update.apk"
                val assets = json.optJSONArray("assets") ?: JSONArray()
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        apkName = name
                        break
                    }
                }

                if (apkDownloadUrl.isBlank()) {
                    apkDownloadUrl = json.optString("html_url", "https://github.com/$repo/releases/latest")
                }

                val isNewer = isVersionNewer(tagName, currentAppVersionName)
                val state = if (isNewer) {
                    val info = GitHubReleaseInfo(
                        tagName = tagName,
                        name = releaseName,
                        body = releaseBody,
                        publishedAt = publishedAt,
                        downloadUrl = apkDownloadUrl,
                        apkFileName = apkName
                    )
                    UpdateState.UpdateAvailable(info, mandatory = mandatory)
                } else {
                    UpdateState.UpToDate
                }
                _updateState.value = state
                return@withContext state
            } else {
                // If repository is 404 or private, or rate limited
                val state = UpdateState.Error("Repositório GitHub ($repo) indisponível (${response.code}). Verifique a URL do repositório.")
                _updateState.value = state
                return@withContext state
            }
        } catch (e: Exception) {
            val state = UpdateState.Error("Falha na verificação de atualização: ${e.localizedMessage ?: "Erro de rede"}")
            _updateState.value = state
            return@withContext state
        }
    }

    /**
     * Compare semantic versions e.g. "1.1" vs "1.0"
     */
    private fun isVersionNewer(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion.isBlank()) return false
        try {
            val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            return false
        } catch (e: Exception) {
            return remoteVersion != currentVersion
        }
    }

    /**
     * Download APK file and initiate installer
     */
    suspend fun downloadAndInstallApk(release: GitHubReleaseInfo) = withContext(Dispatchers.IO) {
        if (!release.downloadUrl.endsWith(".apk", ignoreCase = true)) {
            // Open in browser if not direct APK
            openUrlInBrowser(release.downloadUrl)
            return@withContext
        }

        try {
            _updateState.value = UpdateState.Downloading(progressPercent = 0)
            val request = Request.Builder()
                .url(release.downloadUrl)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                _updateState.value = UpdateState.Error("Falha ao baixar arquivo da release: HTTP ${response.code}")
                return@withContext
            }

            val body = response.body ?: run {
                _updateState.value = UpdateState.Error("Arquivo vazio recebido")
                return@withContext
            }

            val contentLength = body.contentLength()
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val destFile = File(downloadsDir, release.apkFileName.ifBlank { "dedsec_update.apk" })

            body.byteStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastPercent = 0

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            val percent = ((totalRead * 100) / contentLength).toInt()
                            if (percent != lastPercent) {
                                lastPercent = percent
                                _updateState.value = UpdateState.Downloading(progressPercent = percent)
                            }
                        }
                    }
                    output.flush()
                }
            }

            _updateState.value = UpdateState.DownloadComplete(destFile.absolutePath)

            // Trigger Android Package Installer Intent
            installApkFile(destFile)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("Erro no download da atualização: ${e.localizedMessage}")
        }
    }

    private fun installApkFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                val uri = try {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                } catch (e: Exception) {
                    Uri.fromFile(file)
                }
                setDataAndType(uri, "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openUrlInBrowser("https://github.com/$repositoryTarget/releases/latest")
        }
    }

    fun openUrlInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun dismissNonMandatory() {
        _updateState.value = UpdateState.Idle
    }
}
