package us.timinc.mc.cobblemon.unchained.config

import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.CAPTURE
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.HATCH
import us.timinc.mc.cobblemon.counter.CounterMod.CounterTypes.RESURRECTION
import us.timinc.mc.cobblemon.counter.CounterMod.ScoreTypes.STREAK

abstract class IvBoosterConfig(key: String) : AbstractBoosterConfig(key) {
    override fun boostValidationError(boost: Float): String? {
        super.boostValidationError(boost)?.let { return it }
        return if (boost % 1F != 0F) {
            "IV boost must be a nonnegative whole number (got $boost)"
        } else null
    }

    class Spawn : IvBoosterConfig("iv.spawn") {
        override val points = mapOf(
            CAPTURE.type to mapOf(
                STREAK.type to 1F,
            ),
        )
        override val thresholds = mutableMapOf(
            5 to 1F,
            10 to 2F,
            20 to 3F,
            30 to 4F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Fish : IvBoosterConfig("iv.fish") {
        override val points = mapOf(
            CAPTURE.type to mapOf(
                STREAK.type to 1F,
            ),
        )
        override val thresholds = mutableMapOf(
            5 to 1F,
            10 to 2F,
            20 to 3F,
            30 to 4F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Egg : IvBoosterConfig("iv.egg") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            HATCH.type to mapOf(
                STREAK.type to 1F,
            ),
        )
        override val thresholds: Map<Int, Float> = mapOf(
            20 to 1F,
            40 to 2F,
            80 to 3F,
            120 to 4F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Resurrection : IvBoosterConfig("iv.resurrection") {
        override val points: Map<String, Map<String, Float>> = mapOf(
            RESURRECTION.type to mapOf(
                STREAK.type to 1F,
            ),
        )
        override val thresholds: Map<Int, Float> = mapOf(
            10 to 1F,
            20 to 2F,
            40 to 3F,
            60 to 4F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Capture : IvBoosterConfig("iv.capture") {
        override val points: Map<String, Map<String, Float>> = mapOf()
        override val thresholds: Map<Int, Float> = mapOf()
        override val breakStreakOnSuccess: Set<String> = setOf()
    }

    class Snack : IvBoosterConfig("iv.snack") {
        override val points = mapOf(
            CAPTURE.type to mapOf(
                STREAK.type to 1F,
            ),
        )
        override val thresholds = mutableMapOf(
            5 to 1F,
            10 to 2F,
            20 to 3F,
            30 to 4F,
        )
        override val breakStreakOnSuccess: Set<String> = setOf()
    }
}