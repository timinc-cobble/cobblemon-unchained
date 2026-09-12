package us.timinc.mc.cobblemon.unchained.booster

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.FossilRevivedEvent
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent
import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnAction
import com.cobblemon.mod.common.api.spawning.detail.SpawnAction
import com.cobblemon.mod.common.api.spawning.influence.SpawningInfluence
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import us.timinc.mc.cobblemon.timcore.AbstractHandler
import us.timinc.mc.cobblemon.timcore.PokemonRepresentation
import us.timinc.mc.cobblemon.timcore.reserveFor
import us.timinc.mc.cobblemon.unchained.Unchained
import us.timinc.mc.cobblemon.unchained.config.ShinyBoosterConfig
import kotlin.random.Random.Default.nextFloat

object ShinyBooster : AbstractBooster() {
    class Runner(
        player: ServerPlayer,
        pokemon: PokemonRepresentation<*>,
        override val config: ShinyBoosterConfig,
        lockToPlayer: () -> Unit = {},
    ) : AbstractBoosterRunner<ShinyBoosterConfig>(
        player,
        pokemon,
        lockToPlayer,
    ) {

        override fun roll(): Boolean {
            var shinyRate = Cobblemon.config.shinyRate
            CobblemonEvents.SHINY_CHANCE_CALCULATION.post(
                ShinyChanceCalculationEvent(
                    shinyRate,
                    pokemon.getPokemon()
                )
            ) { event ->
                shinyRate = event.calculate(player)
            }
            if (shinyRate < 1) shinyRate = 1 / shinyRate
            val roll = nextFloat()
            val passedRoll = roll < ((1 + unlockedBoost) / shinyRate)
            debug("Calculated shiny rate is $shinyRate. Roll is $roll. Roll ${if (passedRoll) "passed" else "failed"}.")
            return passedRoll
        }

        override fun test(): Boolean {
            if (pokemon.shiny) {
                debug("Pokemon's already shiny from something else.")
                return false
            }

            return true
        }

        override fun boost() {
            pokemon.makeShiny()
            debug("Pokemon made shiny.")
        }
    }

    class ShinyBoosterInfluence(
        private val config: ShinyBoosterConfig,
        private val player: ServerPlayer? = null,
        private val enabled: () -> Boolean = { true },
    ) : SpawningInfluence {
        override fun affectSpawn(action: SpawnAction<*>, entity: Entity) {
            if (!enabled()) return
            if (action !is PokemonSpawnAction || entity !is PokemonEntity) return
            val player = player ?: action.spawnablePosition.cause.entity as? ServerPlayer ?: return
            val pokemonRep = PokemonRepresentation.FromEntity(entity)

            Runner(player, pokemonRep, config) { entity.pokemon.reserveFor(player) }.runThrough()
        }
    }

    object ShinyEggHandler : AbstractHandler<HatchEggEvent.Pre>() {
        override fun handle(evt: HatchEggEvent.Pre) {
            Runner(
                evt.player,
                PokemonRepresentation.FromProperties(evt.egg),
                Unchained.shinyEggBooster
            ).runThrough()
        }
    }

    object ShinyFossilHandler : AbstractHandler<FossilRevivedEvent>() {
        override fun handle(evt: FossilRevivedEvent) {
            evt.player?.let {
                Runner(
                    it,
                    PokemonRepresentation.FromPokemon(evt.pokemon),
                    Unchained.shinyRezBooster
                ).runThrough()
            }
        }
    }

    object ShinyCaptureHandler : AbstractHandler<PokemonCapturedEvent>() {
        override fun handle(evt: PokemonCapturedEvent) {
            Runner(
                evt.player,
                PokemonRepresentation.FromPokemon(evt.pokemon),
                Unchained.shinyCaptureBooster
            ).runThrough()
        }
    }

    override fun initialize() {
        Unchained.registerPlayerSpawnerInfluence(ShinyBoosterInfluence(Unchained.shinySpawnBooster))
        Unchained.registerFishingSpawnerInfluence(ShinyBoosterInfluence(Unchained.shinyFishBooster))
        Unchained.registerSnackSpawnerInfluence(ShinyBoosterInfluence(Unchained.shinySnackBooster))
        Unchained.registerHabitatSpawnerInfluence(
            ShinyBoosterInfluence(
                Unchained.shinySpawnBooster,
                enabled = { Unchained.config.boostActivatedHabitatSpawns },
            )
        )
        CobblemonEvents.HATCH_EGG_PRE.subscribe(Priority.LOWEST, ShinyEggHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOWEST, ShinyFossilHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, ShinyCaptureHandler::handle)
    }
}
