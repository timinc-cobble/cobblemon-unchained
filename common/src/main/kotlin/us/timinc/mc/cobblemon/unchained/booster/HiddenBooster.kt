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
import us.timinc.mc.cobblemon.timcore.TimCore
import us.timinc.mc.cobblemon.timcore.TimCoreEvents
import us.timinc.mc.cobblemon.timcore.event.SpawningInfluenceEvents
import us.timinc.mc.cobblemon.timcore.getType
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
        override fun roll(unlockedBoost: Float): Boolean {
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

        override fun boost(unlockedBoost: Float): Boolean {
            pokemon.giveHiddenAbility()
            debug("Gave hidden ability ${pokemon.abilityName}")
            return true
        }
    }

    class HiddenBoosterInfluence(
        private val config: () -> HiddenBoosterConfig,
        private val player: ServerPlayer? = null,
        private val enabled: () -> Boolean = { true },
    ) : SpawningInfluence {
        override fun affectSpawn(action: SpawnAction<*>, entity: Entity) {
            if (!enabled()) return
            if (action !is PokemonSpawnAction || entity !is PokemonEntity) return
            val player = player ?: action.spawnablePosition.cause.entity as? ServerPlayer ?: return
            val pokemonRep = PokemonRepresentation.FromEntity(entity)

            Runner(player, pokemonRep, config()) { entity.pokemon.reserveFor(player) }.runThrough()
        }
    }

    class HiddenSpawnHandler(
        private val context: String,
        private val config: () -> HiddenBoosterConfig,
        private val enabled: () -> Boolean = { true },
    ) : AbstractHandler<SpawningInfluenceEvents.Spawn>() {
        override fun handle(evt: SpawningInfluenceEvents.Spawn) {
            if (!enabled()) return
            if (evt.position.spawner.getType() != context) return
            val action = evt.action as? PokemonSpawnAction ?: return
            val entity = evt.entity as? PokemonEntity ?: return
            val player = action.spawnablePosition.cause.entity as? ServerPlayer ?: return
            Runner(player, PokemonRepresentation.FromEntity(entity), config()) {
                entity.pokemon.reserveFor(player)
            }.runThrough()
        }
    }

    object HiddenEggHandler : AbstractHandler<HatchEggEvent.Post>() {
        override fun handle(evt: HatchEggEvent.Post) {
            val player = evt.player
            val pokemonRep = PokemonRepresentation.FromPokemon(evt.pokemon)
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
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            HiddenSpawnHandler(TimCore.DataKeys.SpawnerTypes.PLAYER, { Unchained.hiddenSpawnBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            HiddenSpawnHandler(TimCore.DataKeys.SpawnerTypes.FISHING, { Unchained.hiddenFishBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            HiddenSpawnHandler(TimCore.DataKeys.SpawnerTypes.SNACK, { Unchained.hiddenSnackBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            HiddenSpawnHandler(
                TimCore.DataKeys.SpawnerTypes.HABITAT,
                { Unchained.hiddenSpawnBooster },
                enabled = { Unchained.config.boostActivatedHabitatSpawns },
            )::handle,
        )
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.LOW, HiddenEggHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOW, HiddenFossilHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOW, HiddenCaptureHandler::handle)
    }
}
