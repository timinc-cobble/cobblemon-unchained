package us.timinc.mc.cobblemon.unchained.config

import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.HATCH
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.KO
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.RESURRECTION
import us.timinc.mc.cobblemon.counter.CounterMod.ScoreTypes.COUNT
import us.timinc.mc.cobblemon.counter.CounterMod.ScoreTypes.STREAK

abstract class HiddenBoosterConfig(key: String) : AbstractBoosterConfig(key) {
    class Spawn : HiddenBoosterConfig("hidden.spawn") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            KO.type to mapOf(
                STREAK.type to 99F,
                COUNT.type to 1F,
            ),
        )
        override val thresholds = mapOf(
            99 to 1F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Fish : HiddenBoosterConfig("hidden.fish") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            KO.type to mapOf(
                STREAK.type to 99F,
                COUNT.type to 1F,
            ),
        )
        override val thresholds = mapOf(
            99 to 1F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Egg : HiddenBoosterConfig("hidden.egg") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            HATCH.type to mapOf(
                COUNT.type to 1F,
            ),
        )
        override val thresholds: Map<Int, Float> = mapOf(
            100 to 1F,
            200 to 2F,
            300 to 3F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Resurrection : HiddenBoosterConfig("hidden.resurrection") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            RESURRECTION.type to mapOf(
                COUNT.type to 1F,
            ),
        )
        override val thresholds: Map<Int, Float> = mapOf(
            100 to 1F,
            200 to 2F,
            300 to 3F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Capture : HiddenBoosterConfig("hidden.capture") {
        override val points: Map<String, Map<String, Float>> = mapOf()
        override val thresholds: Map<Int, Float> = mapOf()
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Snack : HiddenBoosterConfig("hidden.snack") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            KO.type to mapOf(
                STREAK.type to 99F,
                COUNT.type to 1F,
            ),
        )
        override val thresholds = mapOf(
            99 to 1F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    override fun additionalValidationErrors(): List<String> =
        if (marbles <= 0) listOf("marbles: must be greater than zero (got $marbles)") else emptyList()

    val marbles = 5
}