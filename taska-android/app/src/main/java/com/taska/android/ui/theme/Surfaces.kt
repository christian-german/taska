package com.taska.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Native, performance-safe equivalent of a frosted surface. */
@Composable
fun Modifier.frostedChrome(contentPadding: PaddingValues = PaddingValues(0.dp)): Modifier =
    this
        .clip(RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
        .padding(contentPadding)

@Composable
fun Modifier.opaqueWorkSurface(): Modifier =
    this.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
