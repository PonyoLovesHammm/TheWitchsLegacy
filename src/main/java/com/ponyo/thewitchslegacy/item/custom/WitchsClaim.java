package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

public class WitchsClaim extends Item {
    private static final double BACK_STEAL_DOT_THRESHOLD = -0.35D;
    private static final int CLAIM_COOLDOWN_TICKS = 100;

    public WitchsClaim(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!stack.is(ModItems.WITCHS_CLAIM.get()) || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        applyClaimCooldown(player, stack);
        fillClaim(player, usedHand, serverPlayer.getUUID().toString(), serverPlayer.getName().getString());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();

        if (!stack.is(ModItems.WITCHS_CLAIM.get()) || player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        var clickedPos = context.getClickedPos();
        if (!WitchsClaimSavedData.isBed(level, clickedPos)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var normalizedBedPos = WitchsClaimSavedData.normalizeBedPos(level, clickedPos);
        WitchsClaimSavedData claimData = WitchsClaimSavedData.get((ServerLevel) level);
        if (!claimData.canClaimBed(level, normalizedBedPos)) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.witchs_claim_bed_used_today"), true);
            return InteractionResult.FAIL;
        }

        var owner = claimData.getBedOwner(level, normalizedBedPos);
        if (owner.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.witchs_claim_no_bed_owner"), true);
            return InteractionResult.FAIL;
        }

        claimData.markBedClaimed(level, normalizedBedPos);
        applyClaimCooldown(player, stack);
        fillClaim(player, context.getHand(), owner.get().ownerId(), owner.get().ownerName());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand usedHand) {
        if (!stack.is(ModItems.WITCHS_CLAIM.get()) || !(target instanceof ServerPlayer targetPlayer) || player == target) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!isBehindTarget(player, targetPlayer)) {
            applyClaimCooldown(player, stack);
            player.displayClientMessage(Component.translatable("message.thewitchslegacy.witchs_claim_caught"), true);
            targetPlayer.displayClientMessage(Component.translatable("message.thewitchslegacy.witchs_claim_attempted"), true);
            targetPlayer.playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 0.8F, 1.6F);
            return InteractionResult.FAIL;
        }

        applyClaimCooldown(player, stack);
        fillClaim(player, usedHand, targetPlayer.getUUID().toString(), targetPlayer.getName().getString());
        return InteractionResult.SUCCESS;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.is(ModItems.WITCHS_CLAIM_FILLED.get())) {
            return Waystone.getBloodTarget(stack)
                    .<Component>map(target -> Component.translatable("item.thewitchslegacy.witchs_claim_filled.filled", target.entityName()))
                    .orElseGet(() -> super.getName(stack));
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        Waystone.getBloodTarget(stack).ifPresent(target -> tooltipAdder.accept(
                Component.translatable("tooltip.thewitchslegacy.witchs_claim.target", target.entityName())
                        .withStyle(ChatFormatting.DARK_RED)
        ));
    }

    private static void fillClaim(Player player, InteractionHand hand, String ownerId, String ownerName) {
        ItemStack heldStack = player.getItemInHand(hand);
        ItemStack filledClaim = new ItemStack(ModItems.WITCHS_CLAIM_FILLED.get());
        filledClaim = bindClaim(filledClaim, ownerId, ownerName);

        heldStack.shrink(1);

        if (!player.getInventory().add(filledClaim)) {
            player.drop(filledClaim, false);
        }
    }

    private static ItemStack bindClaim(ItemStack stack, String ownerId, String ownerName) {
        Waystone.bindToPlayer(stack, java.util.UUID.fromString(ownerId), ownerName);
        return stack;
    }

    private static void applyClaimCooldown(Player player, ItemStack stack) {
        player.getCooldowns().addCooldown(stack, CLAIM_COOLDOWN_TICKS);
    }

    private static boolean isBehindTarget(Player thief, Player target) {
        Vec3 look = target.getLookAngle();
        Vec3 flatLook = new Vec3(look.x, 0.0D, look.z);
        Vec3 toThief = thief.position().subtract(target.position());
        Vec3 flatToThief = new Vec3(toThief.x, 0.0D, toThief.z);

        if (flatLook.lengthSqr() < 1.0E-6D || flatToThief.lengthSqr() < 1.0E-6D) {
            return false;
        }

        return flatLook.normalize().dot(flatToThief.normalize()) <= BACK_STEAL_DOT_THRESHOLD;
    }

    public static ItemStack createFilledClaim(ServerPlayer player) {
        ItemStack stack = new ItemStack(ModItems.WITCHS_CLAIM_FILLED.get());
        Waystone.bindToPlayer(stack, player);
        return stack;
    }
}
