package es.itram.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import es.itram.notification.NotificationService

@Composable
actual fun rememberPlatformNotificationService(): NotificationService {
    val context = LocalContext.current.applicationContext
    return remember { NotificationService(context) }
}

