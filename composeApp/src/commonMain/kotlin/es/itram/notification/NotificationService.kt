package es.itram.notification

expect class NotificationService {
    fun sendHungerAlert(petName: String)
    fun sendEnergyAlert(petName: String)
    fun sendHygieneAlert(petName: String)
}

