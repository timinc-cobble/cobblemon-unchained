package us.timinc.mc.cobblemon.unchained.event

import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.timcore.PokemonRepresentation

class BoostCalculation(
    val boostKey: String,
    val pokemonRep: PokemonRepresentation<*>,
    val player: ServerPlayer,
    var unlockedBoost: Float,
)