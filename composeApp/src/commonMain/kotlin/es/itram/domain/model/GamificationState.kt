package es.itram.domain.model

data class GamificationState(
    val xp: Int,
    val level: Int,
    val coins: Int,
    val dailyStreak: Int,
    /** UTC calendar day (epoch milliseconds / DAY_MILLIS) of the last active session. */
    val lastActiveEpochDay: Long,
    /**
     * Epoch milliseconds of the last feed that earned XP/coins.
     * A value of -1 means the pet has never been fed.
     */
    val lastFeedEpochMillis: Long,
    val totalFeedCount: Int,
    val unlockedAchievements: Set<Achievement>,
) {
    init {
        require(xp >= 0) { "xp must be >= 0" }
        require(level >= 1) { "level must be >= 1" }
        require(coins >= 0) { "coins must be >= 0" }
        require(dailyStreak >= 0) { "dailyStreak must be >= 0" }
        require(totalFeedCount >= 0) { "totalFeedCount must be >= 0" }
    }

    companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L

        fun epochDay(epochMillis: Long): Long = epochMillis / DAY_MILLIS

        fun initial(nowEpochMillis: Long): GamificationState = GamificationState(
            xp = 0,
            level = 1,
            coins = 0,
            dailyStreak = 1,
            lastActiveEpochDay = epochDay(nowEpochMillis),
            lastFeedEpochMillis = -1L,
            totalFeedCount = 0,
            unlockedAchievements = emptySet(),
        )
    }
}
