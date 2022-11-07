from net.greemdev.meteor.util import Util, Strings
from net.greemdev.meteor.util.text import FormattedText, ChatColor, actions, ChatEvents
from net.greemdev.meteor.util.misc import Nbt, NbtUtil, KMC
from net.greemdev.meteor.util.meteor import HiddenModules, Meteor, Prompts

from meteordevelopment.meteorclient.utils import Utils
from meteordevelopment.meteorclient.utils.entity import EntityUtils, TargetUtils, SortPriority, Target, ProjectileEntitySimulator
from meteordevelopment.meteorclient.utils.files import StreamUtils
from meteordevelopment.meteorclient.utils.misc import BaritoneUtils, Keybind, MeteorIdentifier, MeteorStarscript, NbtUtils, Pool, Vec2, Vec3, Vec4
from meteordevelopment.meteorclient.utils.misc.input import Input, KeyAction, KeyBinds
from meteordevelopment.meteorclient.utils.misc.text import TextUtils
from meteordevelopment.meteorclient.utils.network import PacketUtils, Http, Capes
from meteordevelopment.meteorclient.utils.notebot import NotebotUtils
from meteordevelopment.meteorclient.utils.notebot.nbs import Layer, Note, Song
from meteordevelopment.meteorclient.utils.player import ChatUtils, DamageUtils, EChestMemory, InvUtils, PlayerUtils, Rotations, SlotUtils
from meteordevelopment.meteorclient.utils.render import FontUtils, NametagUtils, PlayerHeadUtils, RenderUtils
from meteordevelopment.meteorclient.utils.render.color import Color, RainbowColor, RainbowColors, SettingColor
from meteordevelopment.meteorclient.utils.world import BlockUtils
from meteordevelopment.meteorclient import MeteorClient

from net.minecraft.item import ItemStack
from net.minecraft.text import Text, MutableText, ClickEvent, HoverEvent

from com.mojang.text2speech import Narrator

mc = MeteorClient.mc
true = True
false = False

def info(content):
    ChatUtils.info(str(content))

def speak(content):
    Narrator.getNarrator().say(str(content))

{{{SCRIPT}}}
