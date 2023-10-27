/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */


package net.greemdev.meteor.gui.theme.round

import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.WidgetScreen
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture
import meteordevelopment.meteorclient.gui.utils.AlignmentX
import meteordevelopment.meteorclient.gui.utils.CharFilter
import meteordevelopment.meteorclient.gui.widgets.*
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox
import meteordevelopment.meteorclient.renderer.text.TextRenderer
import meteordevelopment.meteorclient.settings.SettingGroup
import meteordevelopment.meteorclient.systems.accounts.Account
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.utils.render.color.Color
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.gui.theme.round.widget.*
import net.greemdev.meteor.gui.theme.round.widget.pressable.*
import net.greemdev.meteor.gui.theme.round.widget.input.*
import net.greemdev.meteor.type.ColorSettingScreenMode
import net.greemdev.meteor.type.setting.TriStateColorSetting
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.invoke
import net.greemdev.meteor.type.setting.triColorSetting


object RoundedTheme : GuiTheme("Rounded") {
    private val sg = settings.group()
    private val sgC = settings group "Colors"
    private val sgTC = settings group "Text"
    private val sgBC = settings group "Background"
    private val sgO = settings group "Outline"
    private val sgSep = settings group "Separator"
    private val sgSB = settings group "Scrollbar"
    private val sgSl = settings group "Slider"
    private val sgSS = settings group "Starscript"

    val scale by sg double {
        name("scale")
        description("Scale of the GUI.")
        defaultValue(1.0)
        range(0.75, 2.0)
        onSliderRelease()
        onChanged {
            (minecraft.currentScreen as? WidgetScreen)?.invalidate()
        }
    }

    val moduleAlignment by sg.enum<AlignmentX> {
        name("module-alignment")
        description("How module titles are aligned in the Modules screen rectangles.")
        defaultValue(AlignmentX.Center)
    }

    val showCategoryIcons by sg bool {
        name("category-icons")
        description("Show item icons on module categories.")
        defaultValue(true)
    }

    val hideHud by sg bool {
        name("hide-HUD")
        description("Hide HUD when in GUI.")
        defaultValue(false)
        onChanged {
            if (minecraft.currentScreen is WidgetScreen)
                minecraft.options.hudHidden = it
        }
    }

    val round by sg double {
        name("roundness")
        description("How much the GUI and HUD should be rounded.")
        defaultValue(4.0)
        range(0.0, 33.0)
    }

    // Colors

    val accentColor by sgC.colorSetting("accent", "Main color of the GUI.", SettingColor.rainbow())
    val checkboxColor by sgC.colorSetting("checkbox", "Color of checkbox.", SettingColor.rainbow())
    val plusColor by sgC.colorSetting("plus", "Color of plus button.", SettingColor(0, 255, 0))
    val minusColor by sgC.colorSetting("minus", "Color of minus button.", SettingColor(255, 0, 0))
    val favoriteColor by sgC.colorSetting("favorite", "Color of checked favorite button.", SettingColor(255, 255, 0))

    // Text
    val textColor by sgTC.colorSetting("text", "Color of text.", SettingColor(255, 255, 255))
    val textSecondaryColor by sgTC.colorSetting("text-secondary-text", "Color of secondary text.", SettingColor(150, 150, 150))
    val textHighlightColor by sgTC.colorSetting("text-highlight", "Color of text highlighting.", SettingColor(45, 125, 245, 100))
    val titleTextColor by sgTC.colorSetting("title-text", "Color of title text.", SettingColor(255, 255, 255))
    val loggedInColor by sgTC.colorSetting("logged-in-text", "Color of logged in account name.", SettingColor(45, 225, 45))
    val placeholderColor by sgTC.colorSetting("placeholder", "Color of placeholder text.", SettingColor(255, 255, 255, 20))

    // Background
    val backgroundColor = sgBC.triColorSetting(
        "background",
        SettingColor(20, 20, 20, 200),
        SettingColor(30, 30, 30, 200),
        SettingColor(40, 40, 40, 200)
    )

    val moduleBackground by sgBC.colorSetting("module-background", "Color of module background when active.", SettingColor(50, 50, 50))

    // Outline
    val outlineColor = sgO.triColorSetting(
        "outline",
        SettingColor(0, 0, 0),
        SettingColor(10, 10, 10),
        SettingColor(20, 20, 20)
    )

    // Separator
    val separatorText by sgSep.colorSetting("separator-text", "Color of separator text", SettingColor(255, 255, 255))
    val separatorCenter by sgSep.colorSetting("separator-center", "Center color of separators.", SettingColor(255, 255, 255))
    val separatorEdges by sgSep.colorSetting("separator-edges", "Color of separator edges.", SettingColor(225, 225, 225, 150))

    //Scrollbar
    val scrollbarColor = sgSB.triColorSetting(
        "scrollbar",
        SettingColor(30, 30, 30, 200),
        SettingColor(40, 40, 40, 200),
        SettingColor(50, 50, 50, 200)
    )

    //Slider
    val sliderHandle = sgSl.triColorSetting(
        "slider-handle",
        SettingColor(0, 255, 180),
        SettingColor(0, 240, 165),
        SettingColor(0, 225, 150)
    )

    val sliderLeft by sgSl.colorSetting("slider-left", "Color of slider left part.", SettingColor(0, 150, 80))
    val sliderRight by sgSl.colorSetting("slider-right", "Color of slider right part.", SettingColor(50, 50, 50))

    //Starscript
    private val starscriptText by sgSS.colorSetting("starscript-text", "Color of text in Starscript code.", SettingColor(169, 183, 198))
    private val starscriptBraces by sgSS.colorSetting("starscript-braces", "Color of braces in Starscript code.", SettingColor(150, 150, 150))
    private val starscriptParenthesis by sgSS.colorSetting(
        "starscript-parenthesis",
        "Color of parenthesis in Starscript code.",
        SettingColor(169, 183, 198)
    )
    private val starscriptDots by sgSS.colorSetting("starscript-dots", "Color of dots in starscript code.", SettingColor(169, 183, 198))
    private val starscriptCommas by sgSS.colorSetting("starscript-commas", "Color of commas in starscript code.", SettingColor(169, 183, 198))
    private val starscriptOperators by sgSS.colorSetting(
        "starscript-operators",
        "Color of operators in Starscript code.",
        SettingColor(169, 183, 198)
    )
    private val starscriptStrings by sgSS.colorSetting("starscript-strings", "Color of strings in Starscript code.", SettingColor(106, 135, 89))
    private val starscriptNumbers by sgSS.colorSetting("starscript-numbers", "Color of numbers in Starscript code.", SettingColor(104, 141, 187))
    private val starscriptKeywords by sgSS.colorSetting("starscript-keywords", "Color of keywords in Starscript code.", SettingColor(204, 120, 50))
    private val starscriptAccessedObjects by sgSS.colorSetting(
        "starscript-accessed-objects",
        "Color of accessed objects (before a dot) in Starscript code.",
        SettingColor(152, 118, 170)
    )

    init {
        settingsFactory = DefaultSettingsWidgetFactory(this)
        colorScreenMode = sgC.enum<ColorSettingScreenMode> {
            name("color-editing-mode")
            description("Which fields to display in the color editing screen.")
            defaultValue(ColorSettingScreenMode.All)
        }.setting
    }


    override fun window(icon: WWidget?, title: String?): WRoundedWindow = w(WRoundedWindow(icon, title))
    override fun label(text: String?, title: Boolean, maxWidth: Double): WLabel =
        if (maxWidth == 0.0)
            w(WRoundedLabel(text, title))
        else
            w(WRoundedMultiLabel(text, title, maxWidth))

    override fun horizontalSeparator(text: String?): WRoundedHorizontalSeparator =
        w(WRoundedHorizontalSeparator(text))

    override fun verticalSeparator(unicolor: Boolean): WVerticalSeparator = w(WRoundedVerticalSeparator(unicolor))

    override fun button(text: String?, texture: GuiTexture?): WRoundedButton =
        w(WRoundedButton(text, texture))
    override fun minus(): WRoundedMinus =
        w(WRoundedMinus())
    override fun plus(): WRoundedPlus =
        w(WRoundedPlus())
    override fun checkbox(checked: Boolean): WRoundedCheckbox =
        w(WRoundedCheckbox(checked))
    override fun slider(value: Double, min: Double, max: Double): WRoundedSlider =
        w(WRoundedSlider(value, min, max))

    override fun textBox(
        text: String,
        placeholder: String?,
        filter: CharFilter?,
        renderer: Class<out WTextBox.Renderer>?
    ): WRoundedTextBox =
        w(WRoundedTextBox(text, placeholder, CharFilter.orNone(filter), renderer))

    override fun <T : Any> dropdown(values: Array<out T>, value: T): WRoundedDropdown<T> =
        w(WRoundedDropdown(values, value))
    override fun triangle(): WRoundedTriangle =
        w(WRoundedTriangle())
    override fun tooltip(text: String): WRoundedTooltip =
        w(WRoundedTooltip(text))
    override fun view(): WRoundedView =
        w(WRoundedView())

    override fun section(
        title: String,
        expanded: Boolean,
        headerWidget: WWidget?
    ): WRoundedSection =
        w(WRoundedSection(title, expanded, headerWidget))

    override fun account(screen: WidgetScreen, account: Account<*>): WRoundedAccount =
        w(WRoundedAccount(screen, account))
    override fun module(module: Module): WRoundedModule =
        w(WRoundedModule(module))
    override fun quad(color: Color): WRoundedQuad =
        w(WRoundedQuad(color))
    override fun topBar(): WRoundedTopBar =
        w(WRoundedTopBar())
    override fun favorite(checked: Boolean): WRoundedFavorite =
        w(WRoundedFavorite(checked))

    override fun textColor(): SettingColor = textColor.get()
    override fun titleTextColor(): SettingColor = titleTextColor.get()
    override fun textSecondaryColor(): SettingColor = textSecondaryColor.get()

    override fun starscriptTextColor(): SettingColor = starscriptText()
    override fun starscriptBraceColor(): SettingColor = starscriptBraces()
    override fun starscriptParenthesisColor(): SettingColor = starscriptParenthesis()
    override fun starscriptDotColor(): SettingColor = starscriptDots()
    override fun starscriptCommaColor(): SettingColor = starscriptCommas()
    override fun starscriptOperatorColor(): SettingColor = starscriptOperators()
    override fun starscriptStringColor(): SettingColor = starscriptStrings()
    override fun starscriptNumberColor(): SettingColor = starscriptNumbers()
    override fun starscriptKeywordColor(): SettingColor = starscriptKeywords()
    override fun starscriptAccessedObjectColor(): SettingColor = starscriptAccessedObjects()

    override fun scalar(): Double = scale()
    override fun categoryIcons(): Boolean = showCategoryIcons()
    override fun hideHUD(): Boolean = hideHud()

    private fun SettingGroup.colorSetting(name: String, description: String, defaultValue: SettingColor) = color {
        name("$name-color")
        description(description)
        defaultValue(defaultValue)
    }
}
