# Unchained

v1.8.0-1.7

[Modrinth](https://modrinth.com/mod/cobblemon-unchained)

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/cobblemon-unchained)

[GitHub](https://github.com/timinc-cobble/cobblemon-unchained)

## What if…

…you could boost your lucky chances in Cobblemon based on your stats?

## Features

- Unlock bonuses to:
    - Hidden abilities
    - Perfect IVs
    - Shininess
- Unlock these bonuses when a Pokémon:
    - Spawns in the wild
    - Is fished up by a player
    - Hatches from an egg
    - Is revived from a fossil
    - Is captured by a player
    - Spawns from snacking on a snack
    - Spawns from an activated Habitat Block
- All bonuses are optional and highly configurable.
- Activated Habitat Block bonuses can be disabled globally to preserve their exact configured output.
- Use *every* stat from the [Counter](https://www.notion.so/Counter-21d57e0d4afd80d0815fc97b89368998?pvs=21) mod
- Smart spawn reservation system prevents steal attempts in multiplayer
- Immersive in-game notifications when bonuses activate
- Whitelist/blacklist support for species/form control
- Optional form-aware tracking for regional variants
- Streak-breaking mechanics to maintain balance

## Dependencies

- [Cobblemon](https://www.notion.so/Cobblemon-22157e0d4afd80a49896c70a775a3c7f?pvs=21)
- [Cobblemon Tim Core](https://www.notion.so/Tim-Core-22057e0d4afd809b9c02e78f26805376?pvs=21)
- [Cobblemon Counter](https://www.notion.so/Counter-21d57e0d4afd80d0815fc97b89368998?pvs=21)

## Testing

For the boosters, head over to [the config](https://www.notion.so/Config-Options-2fc57e0d4afd81f1a374e784afc74705?pvs=21) for the context you would like to test; for this example, we’re going to use the IV booster in a spawning context. Change the `debug` property to `true`, run the game, join a world. Run `/cobbled_counter set <player> capture streak <species> <form> 30`. Replace `<player>`  with your player name, `<species>` and `<form>` with something that spawns a lot nearby (for example, something like `/cobbled_counter set timinc capture streak wooloo normal 30`). Wait for the given Pokémon to spawn, and you should see Unchained's IV booster give feedback about the IVs that were perfected. While waiting, you should have also seen other debug lines go off describing how they failed.

## Player Help

[The Boosters](https://www.notion.so/The-Boosters-2fc57e0d4afd8174bdf1dc3960f535e5?pvs=21)

[Config Options](https://www.notion.so/Config-Options-2fc57e0d4afd81f1a374e784afc74705?pvs=21)

## Addon Dev Help

### Resource Pack Help

[Translations](https://www.notion.so/Translations-2fc57e0d4afd814b9551c74baff9f419?pvs=21)

## Mod Dev Help

[Events](https://www.notion.so/Events-2fc57e0d4afd81769b04c5b26432a07f?pvs=21)

## Roadmap

If you’d like to keep up with the work being done on the mod, please join [the Discord](https://discord.com/invite/WKAR27SdSv) and subscribe to notifications on the channel for this content. You can also keep track of the to do list available on the mod’s main page ([Unchained](https://www.notion.so/Unchained-21e57e0d4afd809bba63d7e435605008?pvs=21) ).

## Feedback

If you have any questions or requests concerning the mod, or just want to drop by and say hi, visit us over at [the Discord](https://discord.com/invite/WKAR27SdSv)!

## Support

If I've made something you enjoyed or helped you make something, please consider [dropping a tip in the cup](https://ko-fi.com/timsminecraftmods) and mention how I helped if you'd like!
