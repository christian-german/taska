package com.taska.android.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MoveToInbox
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavDestination {
    INBOX, TODAY, WEEK, TRACKER
}

private val NavBarBg = Color(0xFF1A1A1A)
private val NavItemActive = Color(0xFFFFFFFF)
private val NavItemInactive = Color(0xFF6B6B6B)
private val FabBg = Color(0xFF2C2C2E)

@Composable
fun BottomNavBar(
    current: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavBarBg)
            .navigationBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavItem(
            icon = Icons.Outlined.MoveToInbox,
            label = "Inbox",
            active = current == NavDestination.INBOX,
            onClick = { onNavigate(NavDestination.INBOX) }
        )
        NavItem(
            icon = Icons.Outlined.AccessTime,
            label = "Aujourd'hui",
            active = current == NavDestination.TODAY,
            onClick = { onNavigate(NavDestination.TODAY) }
        )

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FabBg)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 28.sp
            )
        }

        NavItem(
            icon = Icons.Outlined.CalendarMonth,
            label = "Semaine",
            active = current == NavDestination.WEEK,
            onClick = { onNavigate(NavDestination.WEEK) }
        )
        NavItem(
            icon = Icons.Outlined.Timer,
            label = "Tracker",
            active = current == NavDestination.TRACKER,
            onClick = { onNavigate(NavDestination.TRACKER) }
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val tint = if (active) NavItemActive else NavItemInactive
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label.uppercase(),
            color = tint,
            fontSize = 9.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 0.5.sp
        )
    }
}
