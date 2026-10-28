package us.timinc.mc.cobblemon.unchained.config

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.api.CounterTypeRegistry
import us.timinc.mc.cobblemon.counter.api.ScoreTypeRegistry
import us.timinc.mc.cobblemon.counter.extension.getCounterManager
import us.timinc.mc.cobblemon.timcore.PokemonMatcher
import us.timinc.mc.cobblemon.unchained.Unchained

abstract class AbstractBoosterConfig(
    @Transient
    val key: String,
    val defaultValue: Float = 0F,
) {
    val debug: Boolean = false
    val enabled: Boolean = true
    val lockToPlayer: Boolean = false
    val blacklist = listOf<String>()
    val whitelist = listOf<String>()
    val notifyPlayer: Boolean = true

    @Transient
    var _blacklist: Set<PokemonMatcher>? = null
    val blacklistMatchers: Set<PokemonMatcher>
        get() {
            if (_blacklist == null) {
                _blacklist = blacklist.map(PokemonMatcher::parse).toSet()
            }
            return _blacklist!!
        }

    @Transient
    var _whitelist: Set<PokemonMatcher>? = null
    val whitelistMatchers: Set<PokemonMatcher>
        get() {
            if (_whitelist == null) {
                _whitelist = whitelist.map(PokemonMatcher::parse).toSet()
            }
            return _whitelist!!
        }

    abstract val points: Map<String, Map<String, Float>>
    abstract val thresholds: Map<Int, Float>
    abstract val breakStreakOnSuccess: Set<String>

    @Transient
    var configValid: Boolean = false
        private set

    fun validateConfig(): Boolean {
        val counterTypes = CounterTypeRegistry.types().toSet()
        val scoreTypes = ScoreTypeRegistry.types().toSet()
        val errors = buildList {
            points.forEach { (counterType, scores) ->
                if (counterType !in counterTypes) add("points: unknown counter '$counterType'")
                scores.keys.forEach { scoreType ->
                    if (scoreType !in scoreTypes) add("points.$counterType: unknown score '$scoreType'")
                }
            }
            boostValidationError(defaultValue)?.let { add("defaultValue: $it") }
            thresholds.forEach { (threshold, boost) ->
                boostValidationError(boost)?.let { add("thresholds.$threshold: $it") }
            }
            addAll(additionalValidationErrors())
            breakStreakOnSuccess.forEach { counterType ->
                if (counterType !in counterTypes) add("breakStreakOnSuccess: unknown counter '$counterType'")
            }
        }
        errors.forEach { error ->
            Unchained.debugger.debug("Skipping booster '$key': $error. Correct its config and reload.", true)
        }
        configValid = errors.isEmpty()
        return configValid
    }

    open fun boostValidationError(boost: Float): String? =
        if (boost < 0F) "boost must be nonnegative (got $boost)" else null

    open fun additionalValidationErrors(): List<String> = emptyList()

    fun getPointsFromThreshold(
        player: ServerPlayer,
        species: ResourceLocation,
        form: String,
    ): Float {
        if (!configValid) return 0F
        val counterManager = player.getCounterManager()

        val totalPoints = points.entries.fold(0F) { pointAcc, (counterTypeName, scoreTypeMap) ->
            pointAcc + scoreTypeMap.entries.fold(0F) { counterTypeAcc, (scoreTypeName, multiplier) ->
                val scoreType = ScoreTypeRegistry.findByType(scoreTypeName)
                val counterType = CounterTypeRegistry.findByType(counterTypeName)
                counterTypeAcc + scoreType.getScore(counterManager, counterType, species, form) * multiplier
            }
        }

        return thresholds.maxOfOrNull { if (it.key <= totalPoints) it.value else defaultValue } ?: defaultValue
    }
}
