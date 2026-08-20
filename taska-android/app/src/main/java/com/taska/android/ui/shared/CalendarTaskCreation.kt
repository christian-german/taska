package com.taska.android.ui.shared

internal fun handleCalendarTaskCreated(
    dismissTaskCreation: () -> Unit,
    refreshCalendar: () -> Unit,
) {
    dismissTaskCreation()
    refreshCalendar()
}
