package es.itram.presentation

import androidx.compose.runtime.Composable
import es.itram.notification.NotificationService

@Composable
expect fun rememberPlatformNotificationService(): NotificationService

