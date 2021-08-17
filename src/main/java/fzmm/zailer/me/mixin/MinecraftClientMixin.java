package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.keys.FzmmGuiKey;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Final
	@Shadow
	private static final Logger LOGGER = LogManager.getLogger();
	@Shadow
	@Nullable
	public ClientWorld world;
	@Shadow
	public ClientPlayerEntity player;
	@Shadow
	public HitResult crosshairTarget;
	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	public void handleInputEvents(CallbackInfo ci) {
		FzmmGuiKey.handleInputEvents();
	}

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
	private void doItemPick(MinecraftClient minecraftClient) {
		if (crosshairTarget == null) {
			return;
		}
		assert this.world != null;
		boolean isCreative = this.player.getAbilities().creativeMode;
		HitResult.Type type = this.crosshairTarget.getType();
		ItemStack stack = Items.STONE.getDefaultStack();

		switch (type) {
			case BLOCK -> {
				BlockPos blockPos = ((BlockHitResult) this.crosshairTarget).getBlockPos();
				BlockState blockState = this.world.getBlockState(blockPos);
				if (blockState.isAir()) {
					return;
				}

				Block block = blockState.getBlock();
				stack = block.getPickStack(this.world, blockPos, blockState);
				PlayerInventory playerInventory = this.player.getInventory();

				int i = playerInventory.getSlotWithStack(stack);
				if ((!isCreative || !Screen.hasControlDown()) && i != -1) {
					if (PlayerInventory.isValidHotbarIndex(i)) {
						playerInventory.selectedSlot = i;
					} else {
						this.interactionManager.pickFromInventory(i);
					}
					return;
				}
				if (Screen.hasControlDown()) {
					if (blockState.hasBlockEntity() && !(stack.getItem() instanceof BannerItem)) {
						this.addBlockEntityNbt(stack, blockPos);
					} else {
						this.normalBlockSetState(stack, blockState);
					}
				}
			}
			case ENTITY -> {
				Entity entity = ((EntityHitResult) this.crosshairTarget).getEntity();
				stack = entity.getPickBlockStack();
			}
			case MISS -> {
				return;
			}
		}
		if (!isCreative) {
			return;
		}

		if (stack == null || stack.isEmpty()) {
			String string;

			string = switch (type) {
				case BLOCK -> Registry.BLOCK.getId(this.world.getBlockState(((BlockHitResult) this.crosshairTarget).getBlockPos()).getBlock()).toString();
				case ENTITY -> Registry.ENTITY_TYPE.getId(((EntityHitResult) this.crosshairTarget).getEntity().getType()).toString();
				default -> throw new IllegalStateException("Unexpected value: " + type);
			};

			LOGGER.warn("Picking on: [{}] {} gave null item", type, string);
		} else {
			FzmmUtils.giveItem(stack);
		}
	}

	private void addBlockEntityNbt(ItemStack stack, BlockPos blockPos) {
		assert this.world != null;
		BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
		assert blockEntity != null;
		NbtCompound blockEntityTags = blockEntity.writeNbt(new NbtCompound());
		NbtCompound display = null;

		if (stack.getItem() instanceof SkullItem && blockEntityTags.contains(SkullItem.SKULL_OWNER_KEY)) {
			stack.getOrCreateTag().put(SkullItem.SKULL_OWNER_KEY, blockEntityTags.getCompound(SkullItem.SKULL_OWNER_KEY));
		} else {
			stack.putSubTag(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTags);
			display = FzmmUtils.generateLoreMessage("(BlockEntityTag)");
			if (!blockEntityTags.getString("CustomName").matches("")) {
				display.putString(ItemStack.NAME_KEY, blockEntityTags.getString("CustomName"));
			}
			stack.putSubTag(ItemStack.DISPLAY_KEY, display);
		}

		if (Screen.hasAltDown()) {
			stack.putSubTag(ItemStack.DISPLAY_KEY, FzmmUtils.generateLoreMessage("(" + (display == null ? "" : "BlockEntityTag + ") + "BlockStateTag)"));
			stack.putSubTag("BlockStateTag", this.getBlockStateTag(blockEntity.getCachedState()));
		}
	}

	public void normalBlockSetState(ItemStack stack, BlockState blockState) {
		if (blockState.getProperties().size() > 0) {

			stack.putSubTag(ItemStack.DISPLAY_KEY, FzmmUtils.generateLoreMessage("(BlockStateTag)"));
			stack.putSubTag("BlockStateTag", this.getBlockStateTag(blockState));
		}
	}

	public NbtCompound getBlockStateTag(BlockState state) {
		NbtCompound blockStateTag = new NbtCompound();

		for (Property property : state.getProperties()) {
			blockStateTag.putString(property.getName(), property.name(state.get(property)));
		}

		return blockStateTag;
	}
}
