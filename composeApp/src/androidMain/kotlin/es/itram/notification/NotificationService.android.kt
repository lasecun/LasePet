package es.itram.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

actual class NotificationService(private val context: Context) {

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alertas de mascota",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Notificaciones sobre el estado de tu mascota virtual"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    actual fun sendHungerAlert(petName: String) {
        sendNotification(
            id = 1,
            title = "¡$petName tiene mucha hambre! 🍖",
            body = "Tu mascota lleva tiempo sin comer. ¡Aliméntala!",
        )
    }

    actual fun sendEnergyAlert(petName: String) {
        sendNotification(
            id = 2,
            title = "¡$petName está agotada! 😴",
            body = "Tu mascota necesita descansar. ¡Ponla a dormir!",
        )
    }

    actual fun sendHygieneAlert(petName: String) {
        sendNotification(
            id = 3,
            title = "¡$petName necesita un baño! 🛁",
            body = "Tu mascota está muy sucia. ¡Límpiala!",
        )
    }

    private fun sendNotification(id: Int, title: String, body: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "pet_alerts"
    }
}

