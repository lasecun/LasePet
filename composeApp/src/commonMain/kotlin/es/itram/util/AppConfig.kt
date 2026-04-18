package es.itram.util

expect val isDebugBuild: Boolean

object AppConfig {
    /** Intervalo de tick en producción: 30 minutos */
    const val TICK_INTERVAL_PROD_MS = 30 * 60 * 1000L
    /** Intervalo de tick en debug: 60 segundos */
    const val TICK_INTERVAL_DEBUG_MS = 60 * 1000L
    /** Máximo de ticks de recuperación al abrir la app */
    const val MAX_CATCHUP_TICKS = 48 // máx 24h / 30min
}

