package com.antigravity.eframe.modules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

enum class ModulePosition {
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END,
    CENTER_BOTTOM
}

interface FrameModule {
    val id: String
    val name: String
    val description: String
    val icon: ImageVector
    val defaultPosition: ModulePosition
    var isEnabled: Boolean

    @Composable
    fun Content(modifier: Modifier, onClick: (() -> Unit)? = null)
}
