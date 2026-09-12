package us.timinc.mc.cobblemon.unchained

import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable
import com.cobblemon.mod.common.pokemon.FormData
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import us.timinc.mc.cobblemon.timcore.AbstractConfig
import us.timinc.mc.cobblemon.timcore.AbstractMod
import us.timinc.mc.cobblemon.timcore.ConfigBuilder
import us.timinc.mc.cobblemon.unchained.booster.HiddenBooster
import us.timinc.mc.cobblemon.unchained.booster.IvBooster
import us.timinc.mc.cobblemon.unchained.booster.ShinyBooster
import us.timinc.mc.cobblemon.unchained.config.HiddenBoosterConfig
import us.timinc.mc.cobblemon.unchained.config.IvBoosterConfig
import us.timinc.mc.cobblemon.unchained.config.ShinyBoosterConfig
import us.timinc.mc.cobblemon.unchained.event.BoostApplication
import us.timinc.mc.cobblemon.unchained.event.BoostCalculation

const val MOD_ID: String = "unchained"

object Unchained : AbstractMod<Unchained.UnchainedConfig>(MOD_ID, UnchainedConfig::class.java) {

    class UnchainedConfig : AbstractConfig() {
        val boostActivatedHabitatSpawns: Boolean = true
    }

    var hiddenSpawnBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Spawn::class.java, "unchained/spawn/hiddenSpawnBooster")
    var hiddenEggBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Egg::class.java, "unchained/egg/hiddenEggBooster")
    var hiddenRezBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Resurrection::class.java, "unchained/resurrection/hiddenRezBooster")
    var hiddenFishBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Fish::class.java, "unchained/fish/hiddenFishBooster")
    var hiddenCaptureBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Capture::class.java, "unchained/capture/hiddenCaptureBooster")
    var hiddenSnackBooster: HiddenBoosterConfig =
        ConfigBuilder.load(HiddenBoosterConfig.Snack::class.java, "unchained/snack/hiddenSnackBooster")
    var ivSpawnBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Spawn::class.java, "unchained/spawn/ivSpawnBooster")
    var ivEggBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Egg::class.java, "unchained/egg/ivEggBooster")
    var ivRezBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Resurrection::class.java, "unchained/resurrection/ivRezBooster")
    var ivFishBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Fish::class.java, "unchained/fish/ivFishBooster")
    var ivCaptureBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Capture::class.java, "unchained/capture/ivCaptureBooster")
    var ivSnackBooster: IvBoosterConfig =
        ConfigBuilder.load(IvBoosterConfig.Snack::class.java, "unchained/snack/ivSnackBooster")
    var shinySpawnBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Spawn::class.java, "unchained/spawn/shinySpawnBooster")
    var shinyEggBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Egg::class.java, "unchained/egg/shinyEggBooster")
    var shinyRezBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Resurrection::class.java, "unchained/resurrection/shinyRezBooster")
    var shinyFishBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Fish::class.java, "unchained/fish/shinyFishBooster")
    var shinyCaptureBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Capture::class.java, "unchained/capture/shinyCaptureBooster")
    var shinySnackBooster: ShinyBoosterConfig =
        ConfigBuilder.load(ShinyBoosterConfig.Snack::class.java, "unchained/snack/shinySnackBooster")

    object TranslationComponents {
        fun notify(type: String, species: Species, form: FormData): MutableComponent = Component.translatable(
            "unchained.notification.$type",
            form.name,
            species.translatedName,
        )
    }

    object Events {
        @JvmField
        val BOOST_APPLICATION_PRE = CancelableObservable<BoostApplication.Pre>()

        @JvmField
        val BOOST_APPLICATION_POST = EventObservable<BoostApplication.Post>()

        @JvmField
        val BOOST_CALCULATION = EventObservable<BoostCalculation>()
    }

    init {
        HiddenBooster.initialize()
        IvBooster.initialize()
        ShinyBooster.initialize()

        RELOAD_CONFIG.subscribe {
            hiddenSpawnBooster =
                ConfigBuilder.load(HiddenBoosterConfig.Spawn::class.java, "unchained/spawn/hiddenSpawnBooster")
            hiddenEggBooster = ConfigBuilder.load(HiddenBoosterConfig.Egg::class.java, "unchained/egg/hiddenEggBooster")
            hiddenRezBooster = ConfigBuilder.load(
                HiddenBoosterConfig.Resurrection::class.java, "unchained/resurrection/hiddenRezBooster"
            )
            hiddenFishBooster =
                ConfigBuilder.load(HiddenBoosterConfig.Fish::class.java, "unchained/fish/hiddenFishBooster")
            hiddenCaptureBooster =
                ConfigBuilder.load(HiddenBoosterConfig.Capture::class.java, "unchained/capture/hiddenCaptureBooster")
            hiddenSnackBooster =
                ConfigBuilder.load(HiddenBoosterConfig.Snack::class.java, "unchained/snack/hiddenSnackBooster")
            ivSpawnBooster = ConfigBuilder.load(IvBoosterConfig.Spawn::class.java, "unchained/spawn/ivSpawnBooster")
            ivEggBooster = ConfigBuilder.load(IvBoosterConfig.Egg::class.java, "unchained/egg/ivEggBooster")
            ivRezBooster =
                ConfigBuilder.load(IvBoosterConfig.Resurrection::class.java, "unchained/resurrection/ivRezBooster")
            ivFishBooster = ConfigBuilder.load(IvBoosterConfig.Fish::class.java, "unchained/fish/ivFishBooster")
            ivCaptureBooster =
                ConfigBuilder.load(IvBoosterConfig.Capture::class.java, "unchained/capture/ivCaptureBooster")
            ivSnackBooster =
                ConfigBuilder.load(IvBoosterConfig.Snack::class.java, "unchained/snack/ivSnackBooster")
            shinySpawnBooster =
                ConfigBuilder.load(ShinyBoosterConfig.Spawn::class.java, "unchained/spawn/shinySpawnBooster")
            shinyEggBooster = ConfigBuilder.load(ShinyBoosterConfig.Egg::class.java, "unchained/egg/shinyEggBooster")
            shinyRezBooster = ConfigBuilder.load(
                ShinyBoosterConfig.Resurrection::class.java, "unchained/resurrection/shinyRezBooster"
            )
            shinyFishBooster =
                ConfigBuilder.load(ShinyBoosterConfig.Fish::class.java, "unchained/fish/shinyFishBooster")
            shinyCaptureBooster =
                ConfigBuilder.load(ShinyBoosterConfig.Capture::class.java, "unchained/capture/shinyCaptureBooster")
            shinySnackBooster =
                ConfigBuilder.load(ShinyBoosterConfig.Snack::class.java, "unchained/snack/shinySnackBooster")
        }
    }
}
