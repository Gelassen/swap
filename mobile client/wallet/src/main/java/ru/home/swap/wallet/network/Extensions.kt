package ru.home.swap.wallet.network

import androidx.work.*

inline fun <reified T : BaseChainWorker> WorkManager.getWorkRequest(inputData: Data): OneTimeWorkRequest {
    return OneTimeWorkRequestBuilder<T>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .setInputData(inputData) /* input data for worker */
        .build()
}