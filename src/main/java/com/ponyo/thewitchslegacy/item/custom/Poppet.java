package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class Poppet extends Item {
    private static final String THROW_TRIGGERED_TAG = "VoodooThrowTriggered";
    private static final int CHARGE_TICKS = 80;
    private static final float CHARGE_DAMAGE = 2.0F;
    private static final double THROW_PUSH_DISTANCE = 3.5D;
    private static final String TARGET_UNAVAILABLE_KEY = "message.thewitchslegacy.voodoo_poppet_target_unavailable";
    private static final String TARGET_PROTECTED_KEY = "message.thewitchslegacy.voodoo_poppet_target_protected";
    private static final Set<UUID> DROWN_MIRROR_GUARD = new HashSet<>();

    public Poppet(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Waystone.getBloodTarget(stack)
                .<Component>map(target -> {
                    if (stack.is(ModItems.VOODOO_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.voodoo_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.voodoo_protection_poppet.filled", target.entityName());
                    }
                    return super.getName(stack);
                })
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        Waystone.getBloodTarget(stack).ifPresent(target -> tooltipAdder.accept(
                Component.translatable(stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())
                                ? "tooltip.thewitchslegacy.voodoo_protection_poppet.target"
                                : "tooltip.thewitchslegacy.voodoo_poppet.target",
                        target.entityName())
                        .withStyle(ChatFormatting.DARK_RED)
        ));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (resolveTarget(stack, serverPlayer).isEmpty()) {
                return InteractionResult.FAIL;
            }
        }

        player.startUsingItem(usedHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) {
            return;
        }

        int elapsedTicks = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (elapsedTicks < CHARGE_TICKS) {
            return;
        }

        Optional<ServerPlayer> target = resolveAttackTarget(stack, player);
        if (target.isPresent()) {
            target.get().hurt(level.damageSources().indirectMagic(player, null), CHARGE_DAMAGE);
            player.playSound(SoundEvents.CROSSBOW_SHOOT, 0.7F, 0.7F);
        }

        player.stopUsingItem();
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().isClientSide()) {
            return false;
        }

        if (entity.isInLava()) {
            if (entity.getOwner() instanceof ServerPlayer controller) {
                Optional<ServerPlayer> target = resolveAttackTarget(stack, controller);
                if (target.isPresent()) {
                    target.get().lavaHurt();
                }
            }

            entity.discard();
            return true;
        }

        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(THROW_TRIGGERED_TAG).orElse(false) && entity.onGround() && entity.getOwner() instanceof ServerPlayer controller) {
            Vec3 movement = entity.getDeltaMovement();
            if (movement.lengthSqr() > 0.01D) {
                Optional<ServerPlayer> target = resolveAttackTarget(stack, controller);
                if (target.isPresent()) {
                    pushTarget(target.get(), movement);
                }
            }
            data.putBoolean(THROW_TRIGGERED_TAG, true);
        }

        return false;
    }

    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (!crafted.is(ModItems.VOODOO_POPPET.get()) && !crafted.is(ModItems.VOODOO_PROTECTION_POPPET.get())) {
            return;
        }

        ItemStack filledClaim = ItemStack.EMPTY;
        ItemStack plainPoppet = ItemStack.EMPTY;
        for (int slot = 0; slot < event.getInventory().getContainerSize(); slot++) {
            ItemStack ingredient = event.getInventory().getItem(slot);
            if (ingredient.is(ModItems.WITCHS_CLAIM_FILLED.get())) {
                filledClaim = ingredient;
            } else if (ingredient.is(crafted.getItem())) {
                plainPoppet = ingredient;
            }
        }

        if (filledClaim.isEmpty() || plainPoppet.isEmpty() || Waystone.getBloodTarget(plainPoppet).isPresent()) {
            return;
        }

        Waystone.getBloodTarget(filledClaim).ifPresent(target -> {
            Waystone.bindToPlayer(crafted, target.entityUuid(), target.entityName());
            CustomData.set(DataComponents.CUSTOM_DATA, crafted, crafted.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        });
    }

    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer controller)) {
            return;
        }

        if (!event.getSource().is(DamageTypes.DROWN) || DROWN_MIRROR_GUARD.contains(controller.getUUID())) {
            return;
        }

        Optional<ItemStack> maybePoppet = getHeldBoundPoppet(controller);
        if (maybePoppet.isEmpty()) {
            return;
        }

        Optional<ServerPlayer> target = resolveAttackTarget(maybePoppet.get(), controller);
        if (target.isEmpty()) {
            return;
        }

        ServerPlayer victim = target.get();
        DROWN_MIRROR_GUARD.add(victim.getUUID());
        try {
            victim.hurt(victim.damageSources().drown(), event.getNewDamage());
        } finally {
            DROWN_MIRROR_GUARD.remove(victim.getUUID());
        }
    }

    private static Optional<ItemStack> getHeldBoundPoppet(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(ModItems.VOODOO_POPPET.get()) && Waystone.getBloodTarget(stack).isPresent()) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    private static Optional<ServerPlayer> resolveTarget(ItemStack stack, ServerPlayer controller) {
        return Waystone.getBloodTarget(stack)
                .map(target -> controller.level().getServer().getPlayerList().getPlayer(target.entityUuid()))
                .filter(target -> target != null && isValidTarget(target))
                .or(() -> {
                    controller.displayClientMessage(Component.translatable(TARGET_UNAVAILABLE_KEY), true);
                    return Optional.empty();
                });
    }

    private static Optional<ServerPlayer> resolveAttackTarget(ItemStack stack, ServerPlayer controller) {
        Optional<ServerPlayer> target = resolveTarget(stack, controller);
        if (target.isPresent() && hasBoundProtectionPoppet(target.get())) {
            controller.displayClientMessage(Component.translatable(TARGET_PROTECTED_KEY), true);
            return Optional.empty();
        }
        return target;
    }

    private static boolean isValidTarget(ServerPlayer player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean hasBoundProtectionPoppet(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isBoundProtectionFor(stack, player)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isBoundProtectionFor(ItemStack stack, ServerPlayer player) {
        return stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())
                && Waystone.getBloodTarget(stack)
                .map(target -> target.entityUuid().equals(player.getUUID()))
                .orElse(false);
    }

    private static void pushTarget(ServerPlayer target, Vec3 movement) {
        Vec3 horizontal = new Vec3(movement.x, 0.0D, movement.z);
        Vec3 direction = horizontal.lengthSqr() > 1.0E-4D ? horizontal.normalize() : movement.normalize();
        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }

        Vec3 push = direction.scale(THROW_PUSH_DISTANCE);
        target.push(push.x, 0.35D, push.z);
        target.hurtMarked = true;
    }
}
