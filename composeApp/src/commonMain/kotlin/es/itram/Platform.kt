package es.itram

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform