package fzmm.zailer.me.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@Config(name = "fzmm")
public class FzmmConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("replaceTexts")
    @ConfigEntry.Gui.TransitiveObject
    public ReplaceTexts replaceTexts = new ReplaceTexts();

    @ConfigEntry.Category("encodebook")
    @ConfigEntry.Gui.TransitiveObject
    public Encodebook encodebook = new Encodebook();

    @ConfigEntry.Category("imagetext")
    @ConfigEntry.Gui.TransitiveObject
    public Imagetext imagetext = new Imagetext();

    public static class General {
        public boolean toggleFont = false;
        public boolean forceInvisibleItemFrame = false;
        public boolean disableNightVisionIfBlindness = false;
        public boolean textObfuscated = false;
        public List<String> hideMessagesRegex = Arrays.asList(
                "^Hey! Sorry, but you can't (use|open|change|ride|break|place|harm) that (|block )here.$",
                "^\\[P2\\] You are lacking the permission node: .*$"
        );
    }

    public static class ReplaceTexts {
        public boolean enableReplaceText = true;
        public List<Pair> texts = Arrays.asList(
            new Pair("::shrug::", "¯\\\\_(ツ)_/¯"),
            new Pair("::tableflip::", "(╯°□°）╯︵ ┻━┻"),
            new Pair("::tableflipx2::", "┻━┻ ︵ ＼( °□° )／ ︵ ┻━┻"),
            new Pair("::unflip::", "┬─┬ ノ( ゜-゜ノ)"),
            new Pair("::magic::", "(ﾉ◕ヮ◕)ﾉ*:･ﾟ✧"),
            new Pair("::party::", "(⌐■_■)ノ♪"),
            new Pair("::hi::", "(^Ｕ^)ノ ~Hi"),
            new Pair("::bye::", "(^▽^)┛"),
            new Pair("::zzz::", "(￣o￣) . z Z"),
            new Pair("::nuke::", "(Ｂ Ｏ Ｏ Ｏ Ｍ ！ ＼（〇_ｏ）／"),
            new Pair("::pico::", "⛏"),
            new Pair("::pvp::", "⚔"),
            new Pair("::<::", "«"),
            new Pair("::>::", "»"),
            new Pair("::arriba::", "↑"),
            new Pair("::derecha::", "→"),
            new Pair("::abajo::", "↓"),
            new Pair("::izquierda::", "←"),
            new Pair("::box0::", "☐"),
            new Pair("::box1::", "☑"),
            new Pair("::box2::", "☒"),
            new Pair("::!::", "⚠"),
            new Pair("::boom::", "✸"),
            new Pair("::heart::", "❤"),
            new Pair("::star::", "★"),
            new Pair("::happy::", "☻"),
            new Pair("::xz::", "::fzmm_x_round:: ::fzmm_z_round::"),
            new Pair("::xyz::", "::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round::"),
            new Pair("::xyzyp::", "::fzmm_x_round:: ::fzmm_y_round:: ::fzmm_z_round:: ::fzmm_yaw_round:: ::fzmm_pitch_round::"),
            new Pair("::uuid::", "::fzmm_uuid::"),
            new Pair("::item_name::", "::fzmm_item_name::")
        );
    }

    public static class Encodebook {
        public int messageLength = 255;
        public String myRandom = "1234567890abcdefqwrtyuiopsghjklzxvnmQWERTYUIOPASDFGHJKLZXCVBNM_,.";
        public String translationKey = "secret_mc_";
        public String defaultBookMessage = "Hello world";
        public String bookTitle = "Encode book (%s)";
        public String separatorMessage = "-----";
        @ConfigEntry.Gui.Tooltip()
        public long endToEndEncodeKey = 0;
    }

    public static class Imagetext {
        public ImagetextScale imagetextScale = ImagetextScale.DEFAULT;
        public String defaultBookMessage = "Pon el cursor encima de este mensaje para ver una imagen";
    }

    public static void init() {
        AutoConfig.register(FzmmConfig.class, GsonConfigSerializer::new);
    }

    public static FzmmConfig get() {
        return AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
    }

    public static class Pair {
        String original = "Original";
        String replace = "Replace";

        public Pair(String original, String replace) {
            this.original = original;
            this.replace = replace;
        }

        public Pair() {
        }

        public String getOriginal() {
            return original;
        }


        public String getReplace() {
            return replace;
        }
    }

    public enum ImagetextScale {
        DEFAULT(Image.SCALE_DEFAULT),
        FAST(Image.SCALE_FAST),
        SMOOTH(Image.SCALE_SMOOTH),
        REPLICATE(Image.SCALE_REPLICATE),
        AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

        public int value;
        private ImagetextScale(int value){
            this.value=value;
        }
    }
}
