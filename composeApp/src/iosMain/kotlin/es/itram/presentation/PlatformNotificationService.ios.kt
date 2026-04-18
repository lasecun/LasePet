package es.itram.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import es.itram.notification.NotificationService

@Composable
actual fun rememberPlatformNotificationService(): NotificationService {
    return remember { NotificationService() }
}

