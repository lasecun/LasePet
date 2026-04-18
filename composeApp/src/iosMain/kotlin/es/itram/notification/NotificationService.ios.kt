package es.itram.notification

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

actual class NotificationService {

    init {
        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(
                UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
            ) { _, _ -> }
    }

    actual fun sendHungerAlert(petName: String) {
        sendNotification(
            id = "hunger",
            title = "¡$petName tiene mucha hambre! 🍖",
            body = "Tu mascota lleva tiempo sin comer. ¡Aliméntala!",
        )
    }

    actual fun sendEnergyAlert(petName: String) {
        sendNotification(
            id = "energy",
            title = "¡$petName está agotada! 😴",
            body = "Tu mascota necesita descansar. ¡Ponla a dormir!",
        )
    }

    actual fun sendHygieneAlert(petName: String) {
        sendNotification(
            id = "hygiene",
            title = "¡$petName necesita un baño! 🛁",
            body = "Tu mascota está muy sucia. ¡Límpiala!",
        )
    }

    private fun sendNotification(id: String, title: String, body: String) {
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(platform.UserNotifications.UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = null,
        )
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request) { _ -> }
    }
}

