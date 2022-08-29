/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.presence

import meteordevelopment.discordipc.DiscordIPC
import meteordevelopment.discordipc.RichPresence
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.game.OpenScreenEvent
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.WidgetScreen
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence
import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.meteorclient.utils.player.PlayerUtils
import meteordevelopment.meteorclient.utils.world.Dimension
import meteordevelopment.orbit.EventHandler
import meteordevelopment.starscript.Script
import net.greemdev.meteor.GModule
import net.greemdev.meteor.type.ItemSelectMode
import net.greemdev.meteor.util.*
import net.minecraft.SharedConstants
import net.minecraft.client.gui.screen.*
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen
import net.minecraft.client.gui.screen.option.*
import net.minecraft.client.gui.screen.pack.PackScreen
import net.minecraft.client.gui.screen.world.*
import net.minecraft.client.realms.gui.screen.RealmsScreen
import net.minecraft.util.Util

val safeAppId: Long = 1013634358927691846

class MinecraftPresence : GModule("minecraft-presence", "Displays Minecraft as your presence on Discord.") {

    private val sgL1 = settings.group("Line 1")
    private val sgL2 = settings.group("Line 2")
    private val sgO = settings.group("Other", false)

    var lastInMainMenu = false

    init {
        runInMainMenu = true
    }

    companion object {
        @JvmStatic
        var gameStart: Long = 0
        private val rpc = RichPresence()
        val states: CustomStates = hashMapOf()
        init {
            states.register("com.terraformersmc.modmenu.gui", "Browsing Mods")
            states.register("me.jellysquid.mods.sodium.client", "Changing options")
        }
    }

    val appId by sgO string {
        name("RPC-app-ID")
        description("The used Rich Presence app. Default is recommended and the only one supported.")
        defaultValue(safeAppId.toString())
        filter { _, c -> c.isDigit() }
    }

    fun isUsingDefaultApp() = appId().toLong() == safeAppId

    val l1Messages by sgL1 stringList {
        name("line-1-messages")
        description("Messages used on the first line of the Discord presence.")
        defaultValue("{player}", "{server}")
        onChanged { recompileLines(1, it) }
        renderStarscript()
    }

    val l1Delay by sgL1 int {
        name("line-1-update-delay")
        description("How often to update the first line, in game ticks.")
        defaultValue(200)
        min(10)
        max(500)
        saneSlider()
    }

    val l1SelMode by sgL1.enum<ItemSelectMode> {
        name("line-1-select-mode")
        description("How to select messages on the first line.")
        defaultValue(ItemSelectMode.Sequential)
    }

    val l2Messages by sgL2 stringList {
        name("line-2-messages")
        description("Messages used on the second line of the Discord presence.")
        defaultValue("Minecraft is good", "{round(server.tps, 1)} TPS", "Playing on {server.difficulty}", "Playing with {server.player_count} others!")
        onChanged { recompileLines(2, it) }
        renderStarscript()
    }

    val l2Delay by sgL2 int {
        name("line-2-update-delay")
        description("How often to update the second line, in game ticks.")
        defaultValue(60)
        min(10)
        max(500)
        saneSlider()
    }

    val l2SelMode by sgL2.enum<ItemSelectMode> {
        name("line-2-select-mode")
        description("How to select messages on the first line.")
        defaultValue(ItemSelectMode.Sequential)
    }

    val smallMeteor by sgO bool {
        name("small-meteor-logo")
        description("Display a small Meteor logo to the lower right of the main Presence image.")
        defaultValue(isUsingDefaultApp())
    }

    val dimensionAware by sgO bool {
        name("dimension-aware")
        description("Whether or not to change the main Presence image to a dimension-specific image when in that dimension.")
        defaultValue(isUsingDefaultApp())
    }

    private var forceUpdate = false

    private var line1Index = 0
    private var line2Index = 0
    private var line1Ticks = 0
    private var line2Ticks = 0
    private var line1Scripts = mutableListOf<Script>()
    private var line2Scripts = mutableListOf<Script>()

    private fun recompileLines(line: Int, strings: List<String>) {
        if (line == 1) {
            line1Scripts.clear()
            line1Scripts.addAll(strings.map {
                MeteorStarscript.compile(it)
            })
            forceUpdate = true
        } else if (line == 2) {
            line2Scripts.clear()
            line2Scripts.addAll(strings.map {
                MeteorStarscript.compile(it)
            })
            forceUpdate = true
        }
    }

    private var currentLargeImage: Pair<String, String> = "" to ""
    private var hasSmallImage = false

    private fun largeImage(key: String, text: String) {
        currentLargeImage = key to text
        rpc.setLargeImage(key, text)
    }

    override fun onActivate() {
        with(Meteor.module<DiscordPresence>()) {
            if (isActive) {
                this@MinecraftPresence.info("Disabling Meteor Discord Presence.")
                toggle()
            }
        }

        DiscordIPC.start(appId().toLong(), null)

        largeImage("c418", "Minecraft ${SharedConstants.getGameVersion().name}")

        rpc.setStart(gameStart / 1000)

        recompileLines(1, l1Messages.get())
        recompileLines(2, l2Messages.get())
    }

    override fun onDeactivate() {
        DiscordIPC.stop()
    }

    @EventHandler
    private fun screenOpened(event: OpenScreenEvent) {
        if (!minecraft.isInGame())
            lastInMainMenu = false
    }

    override fun getWidget(theme: GuiTheme): WWidget {
        return theme.button("Starscript Info") {
            Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/starscript/wiki")
        }
    }

    @EventHandler
    private fun afterTick(unused: TickEvent.Post) {
        var update = false

        if (mc.isInGame()) {
            // Line 1
            if (line1Ticks >= l1Delay() || forceUpdate) {
                if (line1Scripts.isNotEmpty()) {
                    var i = Utils.random(0, line1Scripts.size)
                    if (l1SelMode() == ItemSelectMode.Sequential) {
                        if (line1Index >= line1Scripts.size)
                            line1Index = 0

                        i = line1Index++
                    }
                    MeteorStarscript.run(line1Scripts[i])?.also {
                        rpc.setDetails(it)
                    }
                }
                update = true

                line1Ticks = 0
            } else line1Ticks++

            // Line 2

            if (line2Ticks >= l2Delay() || forceUpdate) {
                if (line2Scripts.isNotEmpty()) {
                    var i = Utils.random(0, line2Scripts.size)
                    if (l2SelMode() == ItemSelectMode.Sequential) {
                        if (line2Index >= line2Scripts.size)
                            line2Index = 0

                        i = line2Index++
                    }
                    MeteorStarscript.run(line2Scripts[i])?.also {
                        rpc.setState(it)
                    }
                }
                update = true

                line2Ticks = 0
            } else line2Ticks++

            if (dimensionAware()) {
                val keyPair = when(PlayerUtils.getDimension()) {
                    Dimension.Overworld -> "overworld" to "Currently in the Overworld"
                    Dimension.Nether -> "nether" to "Currently in the Nether"
                    Dimension.End -> "the_end" to "Currently in The End"
                    else -> "c418" to "Minecraft ${SharedConstants.getGameVersion().name}"
                }
                if (currentLargeImage.first != keyPair.first) {
                    update = true
                    largeImage(keyPair.first, keyPair.second)
                }
            } else if (currentLargeImage.first != "c418") {
                update = true
                largeImage("c418", "Minecraft ${SharedConstants.getGameVersion().name}")
            }

            if (smallMeteor() and !hasSmallImage) {
                update = true
                rpc.setSmallImage("meteor_logo", "Meteor Client ${MeteorClient.fullVersion()}")
                hasSmallImage = true
            } else if (!smallMeteor() and hasSmallImage) {
                update = true
                rpc.setSmallImage(null, null)
                hasSmallImage = false
            }
        } else {
            if (!lastInMainMenu) {
                rpc.setDetails("In the menus")

                when(mc.currentScreen) {
                    is TitleScreen -> rpc.setState("Looking at the title screen")
                    is SelectWorldScreen -> rpc.setState("Selecting a world")
                    is EditWorldScreen -> rpc.setState("Editing a world")
                    is CreateWorldScreen, is EditGameRulesScreen -> rpc.setState("Creating a world")
                    is LevelLoadingScreen -> rpc.setState("Loading world")
                    is MultiplayerScreen -> rpc.setState("Selecting a server")
                    is AddServerScreen -> rpc.setState("Adding a server")
                    is ConnectScreen, is DirectConnectScreen -> rpc.setState("Connecting to server")
                    is WidgetScreen -> rpc.setState(if (smallMeteor()) "Browsing Meteor's GUI" else "Experimenting with mods")
                    is OptionsScreen, is SkinOptionsScreen, is SoundOptionsScreen, is VideoOptionsScreen, is ControlsOptionsScreen,
                    is LanguageOptionsScreen, is ChatOptionsScreen, is PackScreen, is AccessibilityOptionsScreen -> rpc.setState("Changing Minecraft options")
                    is CreditsScreen -> rpc.setState("Reading credits")
                    is RealmsScreen -> rpc.setState("Finding a Realm")
                    else -> {
                        val className = mc.currentScreen?.javaClass?.name ?: ""
                        var stateChanged = false
                        states.forEach { (prefix, state) ->
                            if (className.startsWith(prefix)) {
                                rpc.setState(state)
                                stateChanged = true
                                return@forEach
                            }
                        }
                        if (!stateChanged) rpc.setState("In main menu")

                    }
                }
                update = true
            }
        }

        if (update) DiscordIPC.setActivity(rpc)
        forceUpdate = false
        lastInMainMenu = !mc.isInGame()
    }
}
