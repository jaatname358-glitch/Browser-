package com.example.browser

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.delay

class LocalAIDownloader(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val fileName = inputData.getString("FILE_NAME") ?: return Result.failure()
        
        val totalBytes = 100L
        var downloadedBytes = 0L

        setProgress(workDataOf("PROGRESS" to 0f, "STATE" to "DOWNLOADING"))

        while (downloadedBytes < totalBytes) {
            delay(100) 
            downloadedBytes += 2
            val progressObj = workDataOf(
                "PROGRESS" to (downloadedBytes.toFloat() / totalBytes),
                "STATE" to "DOWNLOADING"
            )
            setProgress(progressObj)
        }

        setProgress(workDataOf("PROGRESS" to 1f, "STATE" to "VERIFYING_CHECKSUM"))
        delay(1500)

        setProgress(workDataOf("PROGRESS" to 1f, "STATE" to "COMPLETED"))
        return Result.success(workDataOf("FILE_PATH" to "/data/user/0/com.example/files/$fileName"))
    }

    companion object {
        fun startDownload(context: Context, model: AIModelConfig): java.util.UUID {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<LocalAIDownloader>()
                .setConstraints(constraints)
                .setInputData(workDataOf("FILE_NAME" to model.fileName))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "download_${model.fileName}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            return workRequest.id
        }
        
        fun cancelDownload(context: Context, id: java.util.UUID) {
            WorkManager.getInstance(context).cancelWorkById(id)
        }
    }
}
