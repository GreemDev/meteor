/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("HasPlatformType")

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
import net.greemdev.meteor.type.setting.TriStateColorSetting
import net.greemdev.meteor.util.*


class RoundedTheme : GuiTheme("Rounded") {
    private val sg = settings.group()
    private val sgC = settings.group("Colors")
    private val sgTC = settings.group("Text")
    private val sgBC = settings.group("Background")
    private val sgO = settings.group("Outline")
    private val sgSep = settings.group("Separator")
    private val sgSB = settings.group("Scrollbar")
    private val sgSl = settings.group("Slider")
    private val sgSS = settings.group("Starscript")

    val scale by sg double {
        name("scale")
        description("Scale of the GUI.")
        defaultValue(1.0)
        min(0.75)
        max(4.0)
        saneSlider()
        onSliderRelease()
        onChanged {
            (minecraft.currentScreen as? WidgetScreen)?.invalidate()
        }
    }

    val moduleAlignment by sg.enum<AlignmentX> {
        name("module-alignment")
        description("How module titles are aligned")
        defaultValue(AlignmentX.Center)
    }

    val showCategoryIcons by sg bool {
        name("category-icons")
        description("Adds item icons to module categories.")
        defaultValue(false)
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
        min(0.0)
        max(25.0)
        saneSlider()
    }

    // Colors

    val accentColor by colorSetting("accent", "Main color of the GUI.", SettingColor.rainbow())
    val checkboxColor by colorSetting("checkbox", "Color of checkbox.", SettingColor.rainbow())
    val plusColor by colorSetting("plus", "Color of plus button.", SettingColor(0, 255, 0))
    val minusColor by colorSetting("minus", "Color of minus button.", SettingColor(255, 0, 0))
    val favoriteColor by colorSetting("favorite", "Color of checked favorite button.", SettingColor(255, 255, 0))

    // Text
    val textColor by colorSetting(sgTC, "text", "Color of text.", SettingColor(255, 255, 255))
    val textSecondaryColor by colorSetting(sgTC, "text-secondary-text", "Color of secondary text.", SettingColor(150, 150, 150))
    val textHighlightColor by colorSetting(sgTC, "text-highlight", "Color of text highlighting.", SettingColor(45, 125, 245, 100))
    val titleTextColor by colorSetting(sgTC, "title-text", "Color of title text.", SettingColor(255, 255, 255))
    val loggedInColor by colorSetting(sgTC, "logged-in-text", "Color of logged in account name.", SettingColor(45, 225, 45))
    val placeholderColor by colorSetting(sgTC, "placeholder", "Color of placeholder text.", SettingColor(255, 255, 255, 20))

    // Background
    val backgroundColor = TriStateColorSetting(sgBC,
        "background",
        SettingColor(20, 20, 20, 200),
        SettingColor(30, 30, 30, 200),
        SettingColor(40, 40, 40, 200)
    )

    val moduleBackground by colorSetting(sgBC, "module-background", "Color of module background when active.", SettingColor(50, 50, 50))

    // Outline
    val outlineColor = TriStateColorSetting(sgO,
        "outline",
        SettingColor(0, 0, 0),
        SettingColor(10, 10, 10),
        SettingColor(20, 20, 20)
    )

    // Separator
    val separatorText by colorSetting(sgSep, "separator-text", "Color of separator text", SettingColor(255, 255, 255))
    val separatorCenter by colorSetting(sgSep, "separator-center", "Center color of separators.", SettingColor(255, 255, 255))
    val separatorEdges by colorSetting(sgSep, "separator-edges", "Color of separator edges.", SettingColor(225, 225, 225, 150))

    //Scrollbar
    val scrollbarColor = TriStateColorSetting(sgSB,
        "scrollbar",
        SettingColor(30, 30, 30, 200),
        SettingColor(40, 40, 40, 200),
        SettingColor(50, 50, 50, 200)
    )

    //Slider
    val sliderHandle = TriStateColorSetting(sgSl,
        "slider-handle",
        SettingColor(0, 255, 180),
        SettingColor(0, 240, 165),
        SettingColor(0, 225, 150)
    )

    val sliderLeft by colorSetting(sgSl, "slider-left", "Color of slider left part.", SettingColor(0, 150, 80))
    val sliderRight by colorSetting(sgSl, "slider-right", "Color of slider right part.", SettingColor(50, 50, 50))

    //Starscript
    private val starscriptText by colorSetting(sgSS, "starscript-text", "Color of text in Starscript code.", SettingColor(169, 183, 198))
    private val starscriptBraces by colorSetting(sgSS, "starscript-braces", "Color of braces in Starscript code.", SettingColor(150, 150, 150))
    private val starscriptParenthesis by colorSetting(sgSS,
        "starscript-parenthesis",
        "Color of parenthesis in Starscript code.",
        SettingColor(169, 183, 198)
    )
    private val starscriptDots by colorSetting(sgSS, "starscript-dots", "Color of dots in starscript code.", SettingColor(169, 183, 198))
    private val starscriptCommas by colorSetting(sgSS, "starscript-commas", "Color of commas in starscript code.", SettingColor(169, 183, 198))
    private val starscriptOperators by colorSetting(sgSS,
        "starscript-operators",
        "Color of operators in Starscript code.",
        SettingColor(169, 183, 198)
    )
    private val starscriptStrings by colorSetting(sgSS, "starscript-strings", "Color of strings in Starscript code.", SettingColor(106, 135, 89))
    private val starscriptNumbers by colorSetting(sgSS, "starscript-numbers", "Color of numbers in Starscript code.", SettingColor(104, 141, 187))
    private val starscriptKeywords by colorSetting(sgSS, "starscript-keywords", "Color of keywords in Starscript code.", SettingColor(204, 120, 50))
    private val starscriptAccessedObjects by colorSetting(sgSS,
        "starscript-accessed-objects",
        "Color of accessed objects (before a dot) in Starscript code.",
        SettingColor(152, 118, 170)
    )

    init {
        settingsFactory = DefaultSettingsWidgetFactory(this)
    }


    override fun window(icon: WWidget?, title: String?) = w(WRoundedWindow(icon, title))
    override fun label(text: String?, title: Boolean, maxWidth: Double): WLabel =
        if (maxWidth == 0.0)
            w(WRoundedLabel(text, title))
        else
            w(WRoundedMultiLabel(text, title, maxWidth))

    override fun horizontalSeparator(text: String?) = w(WRoundedHorizontalSeparator(text))
    override fun verticalSeparator() = w(WRoundedVerticalSeparator())
    override fun button(text: String?, texture: GuiTexture?) = w(WRoundedButton(text, texture))
    override fun minus() = w(WRoundedMinus())

    override fun plus() = w(WRoundedPlus())

    override fun checkbox(checked: Boolean) = w(WRoundedCheckbox(checked))

    override fun slider(value: Double, min: Double, max: Double) = w(WRoundedSlider(value, min, max))

    override fun textBox(
        text: String,
        placeholder: String?,
        filter: CharFilter,
        renderer: Class<out WTextBox.Renderer>?
    ) = w(WRoundedTextBox(text, placeholder, filter, renderer))

    override fun <T : Any> dropdown(values: Array<out T>, value: T) = w(WRoundedDropdown(values, value))

    override fun triangle() = w(WRoundedTriangle())
    override fun tooltip(text: String) = w(WRoundedTooltip(text))
    override fun view() = w(WRoundedView())
    override fun section(title: String, expanded: Boolean, headerWidget: WWidget?) = w(WRoundedSection(title, expanded, headerWidget))
    override fun account(screen: WidgetScreen, account: Account<*>) = w(WRoundedAccount(screen, account))
    override fun module(module: Module) = w(WRoundedModule(module))
    override fun quad(color: Color) = w(WRoundedQuad(color))
    override fun topBar() = w(WRoundedTopBar())
    override fun favorite(checked: Boolean) = w(WRoundedFavorite(checked))

    override fun textColor() = textColor.get()
    override fun textSecondaryColor() = textSecondaryColor.get()

    override fun starscriptTextColor() = starscriptText()
    override fun starscriptBraceColor() = starscriptBraces()
    override fun starscriptParenthesisColor() = starscriptParenthesis()
    override fun starscriptDotColor() = starscriptDots()
    override fun starscriptCommaColor() = starscriptCommas()
    override fun starscriptOperatorColor() = starscriptOperators()
    override fun starscriptStringColor() = starscriptStrings()
    override fun starscriptNumberColor() = starscriptNumbers()
    override fun starscriptKeywordColor() = starscriptKeywords()
    override fun starscriptAccessedObjectColor() = starscriptAccessedObjects()

    override fun textRenderer() = TextRenderer.get()
    override fun scale(value: Double) = value * scale()
    override fun categoryIcons() = showCategoryIcons()
    override fun hideHUD() = hideHud()

    private fun colorSetting(name: String, description: String, defaultValue: SettingColor) = colorSetting(sgC, name, description, defaultValue)

    private fun colorSetting(group: SettingGroup, name: String, description: String, defaultValue: SettingColor) = group color {
        name("$name-color")
        description(description)
        defaultValue(defaultValue)
    }
}
