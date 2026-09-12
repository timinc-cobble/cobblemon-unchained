package us.timinc.mc.cobblemon.unchained.booster

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.FossilRevivedEvent
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
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
import us.timinc.mc.cobblemon.unchained.config.HiddenBoosterConfig
import kotlin.random.Random.Default.nextFloat

object HiddenBooster : AbstractBooster() {
    class Runner(
        player: ServerPlayer,
        pokemon: PokemonRepresentation<*>,
        override val config: HiddenBoosterConfig,
        lockToPlayer: () -> Unit = {},
    ) : AbstractBoosterRunner<HiddenBoosterConfig>(player, pokemon, lockToPlayer) {
        override fun roll(): Boolean {
            val totalMarbles = config.marbles

            val roll = nextFloat() * totalMarbles
            val successfulRoll = roll < unlockedBoost

            debug(
                "${player.name.string} has a boost of $unlockedBoost, has a $unlockedBoost out of ${totalMarbles}, rolls a $roll, ${if (successfulRoll) "wins" else "loses"}"
            )

            return successfulRoll
        }

        override fun test(): Boolean {
            if (!pokemon.hasHiddenAbility) {
                debug("${species.resourceIdentifier}|${form.name} doesn't have hidden ability.")
                return false
            }
            return true
        }

        override fun boost() {
            pokemon.giveHiddenAbility()
            debug("Gave hidden ability ${pokemon.abilityName}")
        }
    }

    class HiddenBoosterInfluence(
        private val config: HiddenBoosterConfig,
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

    object HiddenEggHandler : AbstractHandler<HatchEggEvent.Pre>() {
        override fun handle(evt: HatchEggEvent.Pre) {
            val player = evt.player
            val pokemonRep = PokemonRepresentation.FromProperties(evt.egg)
            Runner(player, pokemonRep, Unchained.hiddenEggBooster).runThrough()
        }
    }

    object HiddenFossilHandler : AbstractHandler<FossilRevivedEvent>() {
        override fun handle(evt: FossilRevivedEvent) {
            evt.player?.let {
                Runner(
                    it, PokemonRepresentation.FromPokemon(evt.pokemon), Unchained.hiddenRezBooster
                ).runThrough()
            }
        }
    }

    object HiddenCaptureHandler : AbstractHandler<PokemonCapturedEvent>() {
        override fun handle(evt: PokemonCapturedEvent) {
            Runner(
                evt.player, PokemonRepresentation.FromPokemon(evt.pokemon), Unchained.hiddenCaptureBooster
            ).runThrough()
        }

    }

    override fun initialize() {
        Unchained.registerPlayerSpawnerInfluence(HiddenBoosterInfluence(Unchained.hiddenSpawnBooster))
        Unchained.registerFishingSpawnerInfluence(HiddenBoosterInfluence(Unchained.hiddenFishBooster))
        Unchained.registerSnackSpawnerInfluence(HiddenBoosterInfluence(Unchained.hiddenSnackBooster))
        Unchained.registerHabitatSpawnerInfluence(
            HiddenBoosterInfluence(
                Unchained.hiddenSpawnBooster,
                enabled = { Unchained.config.boostActivatedHabitatSpawns },
            )
        )
        CobblemonEvents.HATCH_EGG_PRE.subscribe(Priority.LOWEST, HiddenEggHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOWEST, HiddenFossilHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, HiddenCaptureHandler::handle)
    }
}
