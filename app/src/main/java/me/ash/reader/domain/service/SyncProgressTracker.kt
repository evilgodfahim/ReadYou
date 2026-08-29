package me.ash.reader.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SyncProgressState(
    val isSyncing: Boolean = false,
    val syncedFeeds: Int = 0,
    val totalFeeds: Int = 0,
    val currentFeedTitle: String = "",
)

object SyncProgressTracker {
    private val _syncProgress = MutableStateFlow(SyncProgressState())
    val syncProgress: StateFlow<SyncProgressState> = _syncProgress.asStateFlow()

    fun startSync(total: Int) {
        _syncProgress.value = SyncProgressState(
            isSyncing = true,
            syncedFeeds = 0,
            totalFeeds = total,
            currentFeedTitle = "Starting sync...",
        )
    }

    fun updateProgress(synced: Int, total: Int, feedTitle: String) {
        _syncProgress.value = SyncProgressState(
            isSyncing = true,
            syncedFeeds = synced,
            totalFeeds = total,
            currentFeedTitle = feedTitle,
        )
    }

    fun finishSync() {
        _syncProgress.value = SyncProgressState(
            isSyncing = false,
            syncedFeeds = 0,
            totalFeeds = 0,
            currentFeedTitle = "",
        )
    }
}
