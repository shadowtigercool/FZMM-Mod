package fzmm.zailer.me.config.hotkeys;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;

public class Hotkeys {
    private static final KeybindSettings ANY_CONTEXT = KeybindSettings.create(KeybindSettings.Context.ANY, KeyAction.PRESS, false, true, false, true);

    public static final ConfigHotkey FZMM_MAIN_GUI = new ConfigHotkey("fzmmMainGui", "Z", KeybindSettings.RELEASE_EXCLUSIVE,  "");
    public static final ConfigHotkey CONFIG_GUI = new ConfigHotkey("configGui", "Z,C",  "");
    public static final ConfigHotkey CONVERTERS_GUI = new ConfigHotkey("convertersGui", "", "");
    public static final ConfigHotkey ENCODEBOOK_GUI = new ConfigHotkey("encodebookGui", "", "");
    public static final ConfigHotkey GRADIENT_GUI = new ConfigHotkey("gradientGui", "Z,G", "");
    public static final ConfigHotkey IMAGETEXT_GUI = new ConfigHotkey("imagetextGui", "Z,I", "");
    public static final ConfigHotkey IMAGETEXT_BOOK_PAGE_GUI = new ConfigHotkey("imagetextBookPageGui", "", "");
    public static final ConfigHotkey IMAGETEXT_BOOK_TOOLTIP_GUI = new ConfigHotkey("imagetextBookTooltipGui", "", "");
    public static final ConfigHotkey IMAGETEXT_HOLOGRAM_GUI = new ConfigHotkey("imagetextHologramGui", "", "");
    public static final ConfigHotkey IMAGETEXT_LORE_GUI = new ConfigHotkey("imagetextLoreGui", "", "");
    public static final ConfigHotkey IMAGETEXT_TELLRAW_GUI = new ConfigHotkey("imagetextTellrawGui", "", "");
    public static final ConfigHotkey PLAYER_STATUE_GUI = new ConfigHotkey("playerStatueGui", "Z,P", "");

    public static final ConfigHotkey INCREMENT_GUI_SCALE = new ConfigHotkey("incrementGuiScale", "LEFT_CONTROL,KP_ADD", ANY_CONTEXT,"");
    public static final ConfigHotkey DECREMENT_GUI_SCALE = new ConfigHotkey("decrementGuiScale", "LEFT_CONTROL,KP_SUBTRACT", ANY_CONTEXT, "");
    public static final ConfigHotkey COPY_ITEM_NAME = new ConfigHotkey("copyItemName", "LEFT_CONTROL,C", KeybindSettings.MODIFIER_GUI, "");
    public static final ConfigHotkey COPY_ITEM_NAME_JSON = new ConfigHotkey("copyItemNameJson", "LEFT_ALT,C", KeybindSettings.MODIFIER_GUI, "");
    public static final ConfigHotkey BACK_IN_FZMM_GUI = new ConfigHotkey("backInFzmmGui", "BUTTON_4", KeybindSettings.MODIFIER_GUI, "");

    public static final ImmutableList<IHotkey> HOTKEY_LIST = ImmutableList.of(
            FZMM_MAIN_GUI,
            CONFIG_GUI,
            CONVERTERS_GUI,
            ENCODEBOOK_GUI,
            GRADIENT_GUI,
            IMAGETEXT_GUI,
            IMAGETEXT_BOOK_PAGE_GUI,
            IMAGETEXT_BOOK_TOOLTIP_GUI,
            IMAGETEXT_HOLOGRAM_GUI,
            IMAGETEXT_LORE_GUI,
            IMAGETEXT_TELLRAW_GUI,
            PLAYER_STATUE_GUI,

            INCREMENT_GUI_SCALE,
            DECREMENT_GUI_SCALE,
            COPY_ITEM_NAME,
            COPY_ITEM_NAME_JSON,
            BACK_IN_FZMM_GUI
    );

}