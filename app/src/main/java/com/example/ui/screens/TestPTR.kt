package com.example.ui.screens
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPullToRefresh() {
    PullToRefreshBox(
        isRefreshing = true,
        onRefresh = {}
    ) {
        Box {}
    }
}
