package us.timinc.mc.cobblemon.unchained.event

import com.cobblemon.mod.common.api.events.Cancelable
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.timcore.PokemonRepresentation

interface BoostApplication {
    val boostKey: String
    val pokemonRep: PokemonRepresentation<*>
    val player: ServerPlayer

    class Pre(
        override val boostKey: String,
        override val pokemonRep: PokemonRepresentation<*>,
        override val player: ServerPlayer,
        val passedRoll: Boolean,
        val passedTest: Boolean,
        var ignoreRoll: Boolean = false,
        var ignoreTest: Boolean = false,
    ) : BoostApplication, Cancelable()

    class Post(
        override val boostKey: String,
        override val pokemonRep: PokemonRepresentation<*>,
        override val player: ServerPlayer,
    ) : BoostApplication
}