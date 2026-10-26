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
import us.timinc.mc.cobblemon.unchained.config.IvBoosterConfig

object IvBooster : AbstractBooster() {
    class Runner(
        player: ServerPlayer,
        pokemon: PokemonRepresentation<*>,
        override val config: IvBoosterConfig,
        lockToPlayer: () -> Unit = {},
    ) : AbstractBoosterRunner<IvBoosterConfig>(
        player,
        pokemon,
        lockToPlayer,
    ) {

        override fun roll(unlockedBoost: Float): Boolean = true

        override fun test(): Boolean = true

        override fun boost(unlockedBoost: Float): Boolean {
            val boostedIvs = unlockedBoost.toInt()
            val perfectedIvs = pokemon.makePerfectIvs(boostedIvs)
            debug("Made $perfectedIvs perfect.")
            return perfectedIvs.isNotEmpty()
        }
    }

    class IvBoosterInfluence(
        private val config: () -> IvBoosterConfig,
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

    class IvSpawnHandler(
        private val context: String,
        private val config: () -> IvBoosterConfig,
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

    object IvEggHandler : AbstractHandler<HatchEggEvent.Post>() {
        override fun handle(evt: HatchEggEvent.Post) {
            val player = evt.player
            Runner(
                player,
                PokemonRepresentation.FromPokemon(evt.pokemon),
                Unchained.ivEggBooster
            ).runThrough()
        }
    }

    object IvFossilHandler : AbstractHandler<FossilRevivedEvent>() {
        override fun handle(evt: FossilRevivedEvent) {
            evt.player?.let {
                Runner(
                    it,
                    PokemonRepresentation.FromPokemon(evt.pokemon),
                    Unchained.ivRezBooster
                ).runThrough()
            }
        }
    }

    object IvCaptureHandler : AbstractHandler<PokemonCapturedEvent>() {
        override fun handle(evt: PokemonCapturedEvent) {
            Runner(evt.player, PokemonRepresentation.FromPokemon(evt.pokemon), Unchained.ivCaptureBooster).runThrough()
        }
    }

    override fun initialize() {
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            IvSpawnHandler(TimCore.DataKeys.SpawnerTypes.PLAYER, { Unchained.ivSpawnBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            IvSpawnHandler(TimCore.DataKeys.SpawnerTypes.FISHING, { Unchained.ivFishBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            IvSpawnHandler(TimCore.DataKeys.SpawnerTypes.SNACK, { Unchained.ivSnackBooster })::handle,
        )
        TimCoreEvents.SPAWNING_SPAWN.subscribe(
            Priority.LOW,
            IvSpawnHandler(
                TimCore.DataKeys.SpawnerTypes.HABITAT,
                { Unchained.ivSpawnBooster },
                enabled = { Unchained.config.boostActivatedHabitatSpawns },
            )::handle,
        )
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.LOW, IvEggHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOW, IvFossilHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOW, IvCaptureHandler::handle)
    }
}
