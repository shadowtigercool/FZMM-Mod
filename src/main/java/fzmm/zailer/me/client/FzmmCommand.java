package fzmm.zailer.me.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import fzmm.zailer.me.utils.LoreUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.*;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class FzmmCommand {

    static final CommandException ERROR_WITHOUT_NBT = new CommandException(new TranslatableText("commands.fzmm.item.withoutNbt"));

    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static void registerCommands() {
        LiteralArgumentBuilder<FabricClientCommandSource> fzmmCommand = ClientCommandManager.literal("fzmm");

        fzmmCommand.then(ClientCommandManager.literal("name")
                .then(ClientCommandManager.argument("name", TextArgumentType.text()).executes(ctx -> {

                    Text name = ctx.getArgument("name", Text.class);

                    renameItem(name);
                    return 1;
                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("lore")
                .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("message", TextArgumentType.text()).executes(ctx -> {

                            Text message = ctx.getArgument("message", Text.class);

                            addLore(message);
                            return 1;
                        }))
                ).then(ClientCommandManager.literal("remove")
                        .executes(
                                ctx -> {

                                    removeLore();
                                    return 1;
                                }
                        ).then(ClientCommandManager.argument("line", IntegerArgumentType.integer(0, 32767)).executes(ctx -> {

                            removeLore(ctx.getArgument("line", int.class));
                            return 1;
                        }))
                )
        );


        fzmmCommand.then(ClientCommandManager.literal("give")
                .then(ClientCommandManager.argument("item", ItemStackArgumentType.itemStack()).executes((ctx) -> {

                    giveItem(ItemStackArgumentType.getItemStackArgument(ctx, "item"), 1);
                    return 1;

                }).then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 127)).executes((ctx) -> {

                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                    ItemStackArgument item = ItemStackArgumentType.getItemStackArgument(ctx, "item");

                    giveItem(item, amount);
                    return 1;
                })))

        );

        fzmmCommand.then(ClientCommandManager.literal("enchant")
                .then(ClientCommandManager.argument("enchantment", EnchantmentArgumentType.enchantment()).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);

                    addEnchant(enchant, (short) 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);
                    int level = ctx.getArgument("level", int.class);

                    addEnchant(enchant, (short) level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("fakeenchant")
                .then(ClientCommandManager.argument("enchantment", EnchantmentArgumentType.enchantment()).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);

                    addFakeEnchant(enchant, 1);
                    return 1;

                }).then(ClientCommandManager.argument("level", IntegerArgumentType.integer()).executes(ctx -> {

                    Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);
                    int level = ctx.getArgument("level", int.class);

                    addFakeEnchant(enchant, level);
                    return 1;
                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("nbt")
                .executes(ctx -> {
                    showNbt();
                    return 1;
                })
        );

        fzmmCommand.then(ClientCommandManager.literal("amount")
                .then(ClientCommandManager.argument("amount", IntegerArgumentType.integer(1, 64)).executes(ctx -> {

                    int amount = ctx.getArgument("amount", int.class);
                    amount(amount);
                    return 1;

                }))
        );

        fzmmCommand.then(ClientCommandManager.literal("skull")
                .then(ClientCommandManager.argument("skull owner", StringArgumentType.greedyString()).suggests(FzmmUtils.SUGGESTION_PLAYER)
                        .executes(ctx -> {

                            String skullOwner = ctx.getArgument("skull owner", String.class);
                            getHead(skullOwner);
                            return 1;

                        }))
        );

        fzmmCommand.then(ClientCommandManager.literal("fullcontainer")
                .then(ClientCommandManager.argument("slots to fill", IntegerArgumentType.integer(1, 27)).executes(ctx -> {

                    fullContainer(ctx.getArgument("slots to fill", int.class), 0);
                    return 1;

                }).then(ClientCommandManager.argument("first slot", IntegerArgumentType.integer(0, 27)).executes(ctx -> {

                    int slotsToFill = ctx.getArgument("slots to fill", int.class);
                    int firstSlot = ctx.getArgument("first slot", int.class);

                    fullContainer(slotsToFill, firstSlot);
                    return 1;

                })))
        );

        fzmmCommand.then(ClientCommandManager.literal("lock")
                .then(ClientCommandManager.argument("key", StringArgumentType.greedyString()).executes(ctx -> {

                    String key = ctx.getArgument("key", String.class);
                    lockContainer(key);
                    return 1;

                }))
        );

        ClientCommandManager.DISPATCHER.register(
                fzmmCommand
        );
    }

    private static void renameItem(Text name) {
        assert MC.player != null;

        ItemStack stack = MC.player.getInventory().getMainHandStack();
        stack.setCustomName(FzmmUtils.disableItalicConfig(name));
        FzmmUtils.giveItem(stack);
    }

    private static void giveItem(ItemStackArgument item, int amount) throws CommandSyntaxException {
        assert MC.player != null;

        ItemStack itemStack = item.createStack(amount, false);
        FzmmUtils.giveItem(itemStack);
    }

    private static void addEnchant(Enchantment enchant, short level) {
        assert MC.player != null;

        //{Enchantments:[{id:"minecraft:aqua_affinity",lvl:1s}]}

        ItemStack stack = MC.player.getInventory().getMainHandStack();
        NbtCompound tag = stack.getOrCreateNbt();
        NbtList enchantments = new NbtList();

        if (tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            enchantments = tag.getList(ItemStack.ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        }
        enchantments.add(EnchantmentHelper.createNbt(EnchantmentHelper.getEnchantmentId(enchant), level));

        tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        stack.setNbt(tag);
        FzmmUtils.giveItem(stack);
    }

    private static void addFakeEnchant(Enchantment enchant, int level) {
        assert MC.player != null;
        ItemStack stack = MC.player.getInventory().getMainHandStack();
        Text enchantMessage = enchant.getName(level);

        System.out.println(enchantMessage);
        Style style = enchantMessage.getStyle();
        ((MutableText) enchantMessage).setStyle(style.withItalic(false));
        System.out.println(enchantMessage);
        LoreUtils.addLore(stack, enchantMessage);

        NbtCompound tag = stack.getOrCreateNbt();
        if (!tag.contains(ItemStack.ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) {
            NbtList enchantments = new NbtList();
            enchantments.add(new NbtCompound());
            tag.put(ItemStack.ENCHANTMENTS_KEY, enchantments);
        }

        FzmmUtils.giveItem(stack);
    }

    private static void showNbt() {
        assert MC.player != null;
        ItemStack stack = MC.player.getInventory().getMainHandStack();

        if (!stack.hasNbt()) {
            throw ERROR_WITHOUT_NBT;
        }
        assert stack.getNbt() != null;
        String nbt = stack.getNbt().toString().replaceAll("§", "\u00a7");

        MutableText message = new LiteralText(stack + ": " + nbt)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, nbt))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MutableText length = new LiteralText(Formatting.BLUE + "Length: " + Formatting.DARK_AQUA + nbt.length())
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, String.valueOf(nbt.length())))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to copy")))
                );

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, message, MC.player.getUuid());

        MC.inGameHud.addChatMessage(MessageType.SYSTEM, length, MC.player.getUuid());
    }

    private static void amount(int amount) {
        assert MC.player != null;

        ItemStack stack = MC.player.getInventory().getMainHandStack();

        stack.setCount(amount);

        FzmmUtils.giveItem(stack);
    }

    private static void getHead(String skullOwner) {
        FzmmUtils.giveItem(FzmmUtils.getPlayerHead(skullOwner));
    }

    private static void fullContainer(int slotsToFill, int firstSlots) {
        assert MC.player != null;

        //{BlockEntityTag:{Items:[{Slot:0b,id:"minecraft:stone",Count:1b}],id:"minecraft:dispenser"}}

        ItemStack containerItemStack = MC.player.getInventory().getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList items = fillSlots(new NbtList(), itemStack, slotsToFill, firstSlots);

        blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
        blockEntityTag.putString("id", containerItemStack.getItem().toString());

        if (!(containerItemStack.getNbt() == null)) {
            tag = containerItemStack.getNbt();

            if (!(containerItemStack.getNbt().getCompound(TagsConstant.BLOCK_ENTITY) == null)) {
                items = fillSlots(tag.getCompound(TagsConstant.BLOCK_ENTITY).getList(ShulkerBoxBlockEntity.ITEMS_KEY, 10), itemStack, slotsToFill, firstSlots);
                blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
            }
        }

        tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        containerItemStack.setNbt(tag);
        FzmmUtils.giveItem(containerItemStack);
    }

    private static NbtList fillSlots(NbtList slotsList, ItemStack stack, int slotsToFill, int firstSlot) {
        for (int i = 0; i != slotsToFill; i++) {
            InventoryUtils.addSlot(slotsList, stack, i + firstSlot);
        }
        return slotsList;
    }

    private static void lockContainer(String key) {
        assert MC.player != null;

        //{BlockEntityTag:{Lock:"abc"}}

        ItemStack containerItemStack = MC.player.getInventory().getMainHandStack();
        ItemStack itemStack = MC.player.getOffHandStack();

        NbtCompound tag = new NbtCompound();
        NbtCompound blockEntityTag = new NbtCompound();

        if (containerItemStack.hasNbt() || tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
            tag = containerItemStack.getNbt();
            assert tag != null;

            if (tag.contains(TagsConstant.BLOCK_ENTITY, NbtElement.COMPOUND_TYPE)) {
                tag.getCompound(TagsConstant.BLOCK_ENTITY).putString("Lock", key);
            }

        } else {
            blockEntityTag.putString("Lock", key);
            tag.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        }

        containerItemStack.setNbt(tag);
        itemStack.setCustomName(new LiteralText(key));

        FzmmUtils.giveItem(containerItemStack);
        MC.player.equipStack(EquipmentSlot.OFFHAND, itemStack);
    }

    private static void addLore(Text text) {
        assert MC.player != null;

        ItemStack stack = MC.player.getMainHandStack();
        LoreUtils.addLore(stack, text);
        FzmmUtils.giveItem(stack);
    }

    private static void removeLore() {
        assert MC.player != null;
        ItemStack stack = MC.player.getMainHandStack();

        NbtCompound display = stack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);
        if (display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE)) {
            removeLore(display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE).size() - 1);
        }
    }

    private static void removeLore(int lineToRemove) {
        assert MC.player != null;

        //{display:{Lore:['{"text":"1"}','{"text":"2"}','[{"text":"3"},{"text":"4"}]']}}

        ItemStack itemStack = MC.player.getMainHandStack();

        NbtCompound display = itemStack.getOrCreateSubNbt(ItemStack.DISPLAY_KEY);

        if (!display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE))
            return;

        NbtList lore = display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE);
        if (lore.size() < lineToRemove)
            return;

        lore.remove(lineToRemove);
        display.put(ItemStack.LORE_KEY, lore);

        itemStack.setSubNbt(ItemStack.DISPLAY_KEY, display);
        FzmmUtils.giveItem(itemStack);
    }
}