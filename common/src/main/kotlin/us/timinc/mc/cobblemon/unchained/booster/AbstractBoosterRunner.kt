package us.timinc.mc.cobblemon.unchained.booster

import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.counter.api.CounterTypeRegistry
import us.timinc.mc.cobblemon.counter.extension.getCounterManager
import us.timinc.mc.cobblemon.timcore.LimitedList
import us.timinc.mc.cobblemon.timcore.PokemonRepresentation
import us.timinc.mc.cobblemon.unchained.Unchained
import us.timinc.mc.cobblemon.unchained.config.AbstractBoosterConfig
import us.timinc.mc.cobblemon.unchained.event.BoostApplication
import us.timinc.mc.cobblemon.unchained.event.BoostCalculation

abstract class AbstractBoosterRunner<T : AbstractBoosterConfig>(
    val player: ServerPlayer,
    val pokemon: PokemonRepresentation<*>,
    val lockToPlayer: () -> Unit = {},
) {
    private val debugger = Unchained.debugger.getCaseDebugger()
    fun debug(msg: String, bypass: Boolean = false) = debugger.debug(msg, bypass || config.debug)

    abstract val config: T

    val species = pokemon.species!!
    val form = pokemon.form ?: species.standardForm
    val unlockedBoost: Float
        get() = config.getPointsFromThreshold(player, species.resourceIdentifier, form.name)

    fun runThrough(): Boolean {
        if (!config.enabled) return false
        debug("Running ${config.key} booster for ${species}|${form.name}")

        if (!LimitedList.PokemonMatcherList.matchesList(
                pokemon.getPokemon(),
                config.whitelistMatchers,
                config.blacklistMatchers
            )
        ) {
            debug("${species.name}|${form.name} is prohibited by the whitelist/blacklist.")
            return false
        }

        var usedUnlockedBoost: Float = unlockedBoost
        Unchained.Events.BOOST_CALCULATION.post(
            BoostCalculation(config.key, pokemon, player, unlockedBoost)
        ) { evt ->
            if (evt.unlockedBoost != usedUnlockedBoost) {
                usedUnlockedBoost = evt.unlockedBoost
                debug("Unlocked boost set to $usedUnlockedBoost by an event listener.")
            } else {
                debug("Using boost of $usedUnlockedBoost")
            }
        }

        if (usedUnlockedBoost == 0F) {
            debug("No boost unlocked")
            return false
        }

        val passedRoll = roll()
        val passedTest = test()

        var passedEvent = false
        val preEvent = BoostApplication.Pre(config.key, pokemon, player, passedRoll, passedTest)
        Unchained.Events.BOOST_APPLICATION_PRE.postThen(preEvent, {
            passedEvent = false
        }, {
            passedEvent = true
        })

        if ((!passedTest && !preEvent.ignoreTest) || (!passedRoll && !preEvent.ignoreRoll) || !passedEvent) return false

        boost()
        config.breakStreakOnSuccess.forEach {
            player.getCounterManager().breakStreak(CounterTypeRegistry.findByType(it))
        }
        if (config.lockToPlayer) {
            lockToPlayer()
        }
        if (config.notifyPlayer) {
            notifyPlayer()
        }
        Unchained.Events.BOOST_APPLICATION_POST.post(
            BoostApplication.Post(
                config.key,
                pokemon,
                player,
            )
        )
        return true
    }

    abstract fun roll(): Boolean

    abstract fun test(): Boolean

    abstract fun boost()

    open fun notifyPlayer() {
        player.sendSystemMessage(Unchained.TranslationComponents.notify(config.key, species, form))
    }
}
