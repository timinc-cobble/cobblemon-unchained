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

        override fun roll(): Boolean = true

        override fun test(): Boolean = true

        override fun boost() {
            val boostedIvs = unlockedBoost.toInt()
            val perfectedIvs = pokemon.makePerfectIvs(boostedIvs)
            debug("Made $perfectedIvs perfect.")
        }
    }

    class IvBoosterInfluence(
        private val config: IvBoosterConfig,
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

    object IvEggHandler : AbstractHandler<HatchEggEvent.Pre>() {
        override fun handle(evt: HatchEggEvent.Pre) {
            Runner(
                evt.player,
                PokemonRepresentation.FromProperties(evt.egg),
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
        Unchained.registerPlayerSpawnerInfluence(IvBoosterInfluence(Unchained.ivSpawnBooster))
        Unchained.registerFishingSpawnerInfluence(IvBoosterInfluence(Unchained.ivFishBooster))
        Unchained.registerSnackSpawnerInfluence(IvBoosterInfluence(Unchained.ivSnackBooster))
        Unchained.registerHabitatSpawnerInfluence(
            IvBoosterInfluence(
                Unchained.ivSpawnBooster,
                enabled = { Unchained.config.boostActivatedHabitatSpawns },
            )
        )
        CobblemonEvents.HATCH_EGG_PRE.subscribe(Priority.LOWEST, IvEggHandler::handle)
        CobblemonEvents.FOSSIL_REVIVED.subscribe(Priority.LOWEST, IvFossilHandler::handle)
        CobblemonEvents.POKEMON_CAPTURED.subscribe(Priority.LOWEST, IvCaptureHandler::handle)
    }
}
