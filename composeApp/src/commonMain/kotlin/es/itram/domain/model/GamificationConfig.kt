package es.itram.domain.model

object GamificationConfig {
    const val XP_PER_FEED = 10
    const val XP_PER_TICK = 2
    const val COINS_PER_FEED = 5
    const val COINS_PER_TICK = 1

    /** Minimum milliseconds between feed actions to earn XP/coins (anti-spam cooldown). */
    const val FEED_COOLDOWN_MILLIS = 60_000L

    /**
     * Total cumulative XP required to reach [level].
     * Level 1 starts at 0. Each level requires [level] × 100 XP to advance.
     * Formula: xpNeededForLevel(n) = 100 × (n-1) × n / 2
     */
    fun xpNeededForLevel(level: Int): Int {
        if (level <= 1) return 0
        return 100 * (level - 1) * level / 2
    }

    /** Compute the level corresponding to [totalXp] accumulated XP. */
    fun levelForXp(totalXp: Int): Int {
        var level = 1
        while (totalXp >= xpNeededForLevel(level + 1)) level++
        return level
    }

    /**
     * XP span of [level]: how many XP are needed to advance from [level] to [level]+1.
     * This equals [level] × 100.
     */
    fun xpSpanForLevel(level: Int): Int = level * 100
}
