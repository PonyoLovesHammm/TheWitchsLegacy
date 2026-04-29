package com.ponyo.thewitchslegacy.item.custom;

import com.ponyo.thewitchslegacy.block.entity.PoppetShelfSavedData;
import com.ponyo.thewitchslegacy.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class Poppet extends Item {
    private static final String THROW_TRIGGERED_TAG = "VoodooThrowTriggered";
    private static final int CHARGE_TICKS = 20;
    private static final int PIN_PRICK_COOLDOWN_TICKS = 60;
    private static final float CHARGE_DAMAGE = 2.0F;
    private static final double THROW_PUSH_DISTANCE = 3.5D;
    private static final float LAVA_IGNITE_SECONDS = 15.0F;
    private static final float LAVA_DAMAGE = 4.0F;
    private static final float DEATH_PROTECTION_HEALTH = 10.0F;
    private static final String TARGET_UNAVAILABLE_KEY = "message.thewitchslegacy.voodoo_poppet_target_unavailable";
    private static final String TARGET_PROTECTED_KEY = "message.thewitchslegacy.voodoo_poppet_target_protected";
    private static final String VAMPIRIC_TARGET_TAG = "VampiricTarget";
    private static final String ENTITY_UUID_TAG = "EntityUuid";
    private static final String ENTITY_NAME_TAG = "EntityName";
    private static final int BOUND_POPPET_STACK_SIZE = 1;
    private static final int VOODOO_POPPET_DURABILITY = 30;
    private static final int VOODOO_PROTECTION_DURABILITY = 60;
    private static final int ARMOR_PROTECTION_DURABILITY = 500;
    private static final int EARTH_PROTECTION_DURABILITY = 60;
    private static final int FIRE_PROTECTION_DURABILITY = 40;
    private static final int HUNGER_PROTECTION_DURABILITY = 20;
    private static final int TOOL_PROTECTION_DURABILITY = 1000;
    private static final int WATER_PROTECTION_DURABILITY = 40;
    private static final int VAMPIRIC_POPPET_DURABILITY = 40;
    public static final int HUNGER_PROTECTION_MAX_DURABILITY = 100;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };
    private static final Set<UUID> DROWN_MIRROR_GUARD = new HashSet<>();
    private static final Set<UUID> VAMPIRIC_TRANSFER_GUARD = new HashSet<>();
    private static final Map<UUID, Integer> LAST_FOOD_LEVELS = new HashMap<>();
    private static final Map<UUID, ToolDamageSnapshot> TOOL_DAMAGE_SNAPSHOTS = new HashMap<>();

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
                    if (stack.is(ModItems.ARMOR_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.armor_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.DEATH_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.death_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.EARTH_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.earth_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.FIRE_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.fire_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.HUNGER_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.hunger_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.TOOL_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.tool_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.WATER_PROTECTION_POPPET.get())) {
                        return Component.translatable("item.thewitchslegacy.water_protection_poppet.filled", target.entityName());
                    }
                    if (stack.is(ModItems.VAMPIRIC_POPPET.get())) {
                        return getVampiricTarget(stack)
                                .<Component>map(vampiricTarget -> Component.translatable(
                                        "item.thewitchslegacy.vampiric_poppet.filled",
                                        target.entityName(),
                                        vampiricTarget.entityName()
                                ))
                                .orElseGet(() -> super.getName(stack));
                    }
                    return super.getName(stack);
                })
                .orElseGet(() -> super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        if (stack.is(ModItems.VAMPIRIC_POPPET.get())) {
            Optional<Waystone.BloodTarget> source = Waystone.getBloodTarget(stack);
            Optional<Waystone.BloodTarget> target = getVampiricTarget(stack);
            if (source.isPresent() && target.isPresent()) {
                tooltipAdder.accept(Component.translatable(
                                "tooltip.thewitchslegacy.vampiric_poppet.target",
                                source.get().entityName(),
                                target.get().entityName()
                        )
                        .withStyle(ChatFormatting.DARK_RED));
            }
            return;
        }
        Waystone.getBloodTarget(stack).ifPresent(target -> tooltipAdder.accept(
                Component.translatable(tooltipTargetKey(stack),
                        target.entityName())
                        .withStyle(ChatFormatting.DARK_RED)
        ));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!stack.is(ModItems.VOODOO_POPPET.get())) {
            return InteractionResult.PASS;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

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
        if (!stack.is(ModItems.VOODOO_POPPET.get()) || level.isClientSide() || !(livingEntity instanceof ServerPlayer player)) {
            return;
        }

        int elapsedTicks = getUseDuration(stack, livingEntity) - remainingUseDuration;
        if (elapsedTicks < CHARGE_TICKS) {
            return;
        }

        Optional<ServerPlayer> target = resolveTarget(stack, player);
        if (target.isPresent()) {
            float damage = applyVoodooProtection(target.get(), player, CHARGE_DAMAGE);
            if (damage > 0.0F) {
                target.get().hurt(level.damageSources().indirectMagic(player, null), damage);
                damageVoodooPoppet(stack, player, damage);
            }
            player.playSound(SoundEvents.CROSSBOW_SHOOT, 0.7F, 0.7F);
        }

        player.getCooldowns().addCooldown(stack, PIN_PRICK_COOLDOWN_TICKS);
        player.stopUsingItem();
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!stack.is(ModItems.VOODOO_POPPET.get()) || entity.level().isClientSide()) {
            return false;
        }

        if (entity.isInLava()) {
            if (entity.getOwner() instanceof ServerPlayer controller) {
                Optional<ServerPlayer> target = resolveTarget(stack, controller);
                if (target.isPresent()) {
                    float damage = applyVoodooProtection(target.get(), controller, LAVA_DAMAGE);
                    if (damage > 0.0F) {
                        applyLavaEffect(target.get(), damage);
                        damageVoodooPoppet(stack, controller, damage);
                    }
                }
            }

            entity.discard();
            return true;
        }

        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(THROW_TRIGGERED_TAG).orElse(false) && entity.onGround() && entity.getOwner() instanceof ServerPlayer controller) {
            Vec3 movement = entity.getDeltaMovement();
            if (movement.lengthSqr() > 0.01D) {
                Optional<ServerPlayer> target = resolveTarget(stack, controller);
                if (target.isPresent() && !applyVoodooShoveProtection(target.get(), controller)) {
                    pushTarget(target.get(), movement);
                    damageVoodooPoppet(stack, controller, 1.0F);
                }
            }
            data.putBoolean(THROW_TRIGGERED_TAG, true);
        }

        return false;
    }

    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafted = event.getCrafting();
        if (!isBindablePoppet(crafted)) {
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
            applyBoundComponents(crafted);
        });
    }

    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getSource().is(DamageTypes.FALL)) {
            absorbFallDamageWithEarthPoppet(event, player);
        }
        if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
            absorbFireDamageWithFirePoppet(event, player);
        }
        if (event.getSource().is(DamageTypes.DROWN)) {
            absorbDrowningDamageWithWaterPoppet(event, player);
        }

        if (!VAMPIRIC_TRANSFER_GUARD.contains(player.getUUID())) {
            transferDamageWithVampiricPoppet(event, player);
        }

        if (player.getHealth() - event.getNewDamage() > 0.0F) {
            return;
        }

        Optional<PoppetHandle> maybePoppet = getCarriedBoundDeathProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        event.setNewDamage(0.0F);
        player.setHealth(Math.min(player.getMaxHealth(), DEATH_PROTECTION_HEALTH));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        maybePoppet.get().shrink(1, player);
    }

    private static void absorbFallDamageWithEarthPoppet(LivingDamageEvent.Pre event, ServerPlayer player) {
        Optional<PoppetHandle> maybePoppet = getCarriedBoundEarthProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        absorbDamageWithPoppet(event, maybePoppet.get(), player);
    }

    private static void transferDamageWithVampiricPoppet(LivingDamageEvent.Pre event, ServerPlayer player) {
        float incomingDamage = event.getNewDamage();
        if (incomingDamage <= 0.0F) {
            return;
        }

        Optional<VampiricTransfer> maybeTransfer = getCarriedBoundVampiricPoppet(player);
        if (maybeTransfer.isEmpty()) {
            return;
        }

        VampiricTransfer transfer = maybeTransfer.get();
        PoppetHandle poppet = transfer.poppet();
        int remainingDurability = poppet.getMaxDamage() - poppet.getDamageValue();
        int poppetDamage = Math.min((int) Math.ceil(incomingDamage), remainingDurability);
        if (poppetDamage <= 0) {
            return;
        }

        float transferredDamage = Math.min(incomingDamage, poppetDamage);
        poppet.hurtAndBreak(poppetDamage, player);
        float targetDamage = applyVoodooProtection(transfer.target(), player, transferredDamage);

        if (targetDamage > 0.0F) {
            VAMPIRIC_TRANSFER_GUARD.add(transfer.target().getUUID());
            try {
                transfer.target().hurt(event.getSource(), targetDamage);
            } finally {
                VAMPIRIC_TRANSFER_GUARD.remove(transfer.target().getUUID());
            }
        }

        event.setNewDamage(Math.max(0.0F, incomingDamage - targetDamage));
    }

    private static void absorbFireDamageWithFirePoppet(LivingDamageEvent.Pre event, ServerPlayer player) {
        Optional<PoppetHandle> maybePoppet = getCarriedBoundFireProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        absorbDamageWithPoppet(event, maybePoppet.get(), player);
    }

    private static void absorbDrowningDamageWithWaterPoppet(LivingDamageEvent.Pre event, ServerPlayer player) {
        Optional<PoppetHandle> maybePoppet = getCarriedBoundWaterProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        absorbDamageWithPoppet(event, maybePoppet.get(), player);
    }

    private static void absorbDamageWithPoppet(LivingDamageEvent.Pre event, PoppetHandle poppet, ServerPlayer player) {
        float incomingDamage = event.getNewDamage();
        if (incomingDamage <= 0.0F) {
            return;
        }

        int remainingDurability = poppet.getMaxDamage() - poppet.getDamageValue();
        int poppetDamage = Math.min((int) Math.ceil(incomingDamage), remainingDurability);
        if (poppetDamage <= 0) {
            return;
        }

        poppet.hurtAndBreak(poppetDamage, player);
        event.setNewDamage(Math.max(0.0F, incomingDamage - poppetDamage));
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

        Optional<ServerPlayer> target = resolveTarget(maybePoppet.get(), controller);
        if (target.isEmpty()) {
            return;
        }

        ServerPlayer victim = target.get();
        float damage = applyVoodooProtection(victim, controller, event.getNewDamage());
        if (damage <= 0.0F) {
            return;
        }

        DROWN_MIRROR_GUARD.add(victim.getUUID());
        try {
            victim.hurt(victim.damageSources().drown(), damage);
            damageVoodooPoppet(maybePoppet.get(), controller, damage);
        } finally {
            DROWN_MIRROR_GUARD.remove(victim.getUUID());
        }
    }

    public static void onArmorHurt(ArmorHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel)) {
            return;
        }

        Optional<PoppetHandle> maybePoppet = getBoundArmorProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        int redirectedDamage = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            float damage = event.getNewDamage(slot);
            if (damage > 0.0F && !event.getArmorItemStack(slot).isEmpty()) {
                redirectedDamage += (int) Math.ceil(damage);
            }
        }

        if (redirectedDamage <= 0) {
            return;
        }

        PoppetHandle poppet = maybePoppet.get();
        poppet.hurtAndBreak(redirectedDamage, player);

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            event.setNewDamage(slot, 0.0F);
        }
    }

    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() == null) {
            return;
        }

        ItemStack original = event.getOriginal();
        if (!isProtectableTool(original)) {
            return;
        }

        Optional<PoppetHandle> maybePoppet = getCarriedBoundToolProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        PoppetHandle poppet = maybePoppet.get();
        int remainingDurability = poppet.getMaxDamage() - poppet.getDamageValue();
        int damageToRedirect = Math.max(1, original.getMaxDamage() - original.getDamageValue());
        int redirected = Math.min(damageToRedirect, remainingDurability);
        if (redirected <= 0) {
            return;
        }

        poppet.hurtAndBreak(redirected, player);
        ItemStack restored = original.copyWithCount(1);
        restored.setDamageValue(Math.min(restored.getMaxDamage() - 1, original.getDamageValue() + damageToRedirect - redirected));
        player.setItemInHand(event.getHand(), restored);
        rememberToolDamageSnapshot(player);
    }

    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        redirectToolDamageSinceLastSnapshot(player);
        rememberToolDamageSnapshot(player);
    }

    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        redirectToolDamageSinceLastSnapshot(player);

        int currentFood = player.getFoodData().getFoodLevel();
        UUID playerId = player.getUUID();
        Integer previousFood = LAST_FOOD_LEVELS.get(playerId);
        if (previousFood == null) {
            LAST_FOOD_LEVELS.put(playerId, currentFood);
            return;
        }

        if (currentFood < previousFood) {
            int hungerLoss = previousFood - currentFood;
            Optional<PoppetHandle> maybePoppet = getCarriedBoundHungerProtectionPoppet(player);
            if (maybePoppet.isPresent()) {
                PoppetHandle poppet = maybePoppet.get();
                int remainingDurability = poppet.getMaxDamage() - poppet.getDamageValue();
                int absorbed = Math.min(hungerLoss, remainingDurability);
                if (absorbed > 0) {
                    poppet.setDamageValue(Math.min(poppet.getMaxDamage(), poppet.getDamageValue() + absorbed));
                    poppet.setChanged(player);
                    currentFood += absorbed;
                    player.getFoodData().setFoodLevel(Math.min(20, currentFood));
                }
            }
        }

        LAST_FOOD_LEVELS.put(playerId, player.getFoodData().getFoodLevel());
        rememberToolDamageSnapshot(player);
    }

    private static void redirectToolDamageSinceLastSnapshot(ServerPlayer player) {
        ToolDamageSnapshot previous = TOOL_DAMAGE_SNAPSHOTS.get(player.getUUID());
        if (previous == null) {
            return;
        }

        redirectToolDamage(player, InteractionHand.MAIN_HAND, player.getMainHandItem(), previous.mainHand());
        redirectToolDamage(player, InteractionHand.OFF_HAND, player.getOffhandItem(), previous.offHand());
    }

    private static void redirectToolDamage(ServerPlayer player, InteractionHand hand, ItemStack tool, ToolStackSnapshot previous) {
        if (previous == null || !isProtectableTool(tool) || !tool.is(previous.item()) || tool.getDamageValue() <= previous.damage()) {
            return;
        }

        int toolDamage = tool.getDamageValue() - previous.damage();
        Optional<PoppetHandle> maybePoppet = getCarriedBoundToolProtectionPoppet(player);
        if (maybePoppet.isEmpty()) {
            return;
        }

        PoppetHandle poppet = maybePoppet.get();
        int remainingDurability = poppet.getMaxDamage() - poppet.getDamageValue();
        int redirected = Math.min(toolDamage, remainingDurability);
        if (redirected <= 0) {
            return;
        }

        poppet.hurtAndBreak(redirected, player);
        tool.setDamageValue(Math.max(0, tool.getDamageValue() - redirected));
        if (hand == InteractionHand.MAIN_HAND) {
            player.getInventory().setChanged();
        }
    }

    private static boolean isProtectableTool(ItemStack stack) {
        return !stack.isEmpty()
                && stack.isDamageableItem()
                && stack.has(DataComponents.TOOL)
                && !stack.is(ModItems.TOOL_PROTECTION_POPPET.get());
    }

    private static void rememberToolDamageSnapshot(ServerPlayer player) {
        TOOL_DAMAGE_SNAPSHOTS.put(player.getUUID(), new ToolDamageSnapshot(
                createToolStackSnapshot(player.getMainHandItem()),
                createToolStackSnapshot(player.getOffhandItem())
        ));
    }

    private static ToolStackSnapshot createToolStackSnapshot(ItemStack stack) {
        return isProtectableTool(stack) ? new ToolStackSnapshot(stack.getItem(), stack.getDamageValue()) : null;
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

    private static Optional<PoppetHandle> getCarriedBoundDeathProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.DEATH_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getCarriedBoundEarthProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.EARTH_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getCarriedBoundFireProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.FIRE_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getCarriedBoundHungerProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.HUNGER_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getCarriedBoundToolProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.TOOL_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getCarriedBoundWaterProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.WATER_PROTECTION_POPPET.get());
    }

    private static Optional<PoppetHandle> getBoundArmorProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.ARMOR_PROTECTION_POPPET.get());
    }

    private static Optional<VampiricTransfer> getCarriedBoundVampiricPoppet(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(ModItems.VAMPIRIC_POPPET.get())) {
                continue;
            }

            Optional<Waystone.BloodTarget> source = Waystone.getBloodTarget(stack);
            Optional<Waystone.BloodTarget> target = getVampiricTarget(stack);
            if (source.isEmpty() || target.isEmpty() || !source.get().entityUuid().equals(player.getUUID())) {
                continue;
            }

            ServerPlayer targetPlayer = player.level().getServer().getPlayerList().getPlayer(target.get().entityUuid());
            if (targetPlayer != null && targetPlayer.isAlive()) {
                return Optional.of(new VampiricTransfer(new InventoryPoppetHandle(stack), targetPlayer));
            }
        }

        return PoppetShelfSavedData.get((ServerLevel) player.level())
                .findFirst(player, stack -> stack.is(ModItems.VAMPIRIC_POPPET.get()) && isUsableVampiricPoppet(stack, player))
                .map(shelfPoppet -> {
                    Waystone.BloodTarget target = getVampiricTarget(shelfPoppet.stack()).orElseThrow();
                    ServerPlayer targetPlayer = player.level().getServer().getPlayerList().getPlayer(target.entityUuid());
                    return new VampiricTransfer(new ShelfPoppetHandle(shelfPoppet), targetPlayer);
                });
    }

    private static Optional<PoppetHandle> getBoundProtectionPoppet(ServerPlayer player, Item poppetItem) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isBoundFor(stack, poppetItem, player)) {
                return Optional.of(new InventoryPoppetHandle(stack));
            }
        }

        return PoppetShelfSavedData.get((ServerLevel) player.level())
                .findFirst(player, stack -> isBoundFor(stack, poppetItem, player))
                .map(ShelfPoppetHandle::new);
    }

    private static boolean isBindablePoppet(ItemStack stack) {
        return stack.is(ModItems.VOODOO_POPPET.get())
                || stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())
                || stack.is(ModItems.ARMOR_PROTECTION_POPPET.get())
                || stack.is(ModItems.DEATH_PROTECTION_POPPET.get())
                || stack.is(ModItems.EARTH_PROTECTION_POPPET.get())
                || stack.is(ModItems.FIRE_PROTECTION_POPPET.get())
                || stack.is(ModItems.HUNGER_PROTECTION_POPPET.get())
                || stack.is(ModItems.TOOL_PROTECTION_POPPET.get())
                || stack.is(ModItems.WATER_PROTECTION_POPPET.get());
    }

    public static boolean isAnyPoppet(ItemStack stack) {
        return stack.is(ModItems.POPPET.get())
                || stack.getItem() instanceof Poppet;
    }

    private static String tooltipTargetKey(ItemStack stack) {
        if (stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.voodoo_protection_poppet.target";
        }
        if (stack.is(ModItems.ARMOR_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.armor_protection_poppet.target";
        }
        if (stack.is(ModItems.DEATH_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.death_protection_poppet.target";
        }
        if (stack.is(ModItems.EARTH_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.earth_protection_poppet.target";
        }
        if (stack.is(ModItems.FIRE_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.fire_protection_poppet.target";
        }
        if (stack.is(ModItems.HUNGER_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.hunger_protection_poppet.target";
        }
        if (stack.is(ModItems.TOOL_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.tool_protection_poppet.target";
        }
        if (stack.is(ModItems.WATER_PROTECTION_POPPET.get())) {
            return "tooltip.thewitchslegacy.water_protection_poppet.target";
        }
        return "tooltip.thewitchslegacy.voodoo_poppet.target";
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

    private static boolean isValidTarget(ServerPlayer player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean hasBoundProtectionPoppet(ServerPlayer player) {
        return getBoundProtectionPoppet(player, ModItems.VOODOO_PROTECTION_POPPET.get()).isPresent();
    }

    private static float applyVoodooProtection(ServerPlayer target, ServerPlayer controller, float incomingDamage) {
        Optional<PoppetHandle> maybeProtection = getBoundProtectionPoppet(target, ModItems.VOODOO_PROTECTION_POPPET.get());
        if (maybeProtection.isEmpty()) {
            return incomingDamage;
        }

        PoppetHandle protection = maybeProtection.get();
        int remainingDurability = protection.getMaxDamage() - protection.getDamageValue();
        int stoppedDamage = Math.min((int) Math.ceil(incomingDamage), remainingDurability);
        if (stoppedDamage <= 0) {
            return incomingDamage;
        }

        protection.hurtAndBreak(stoppedDamage, target);
        controller.displayClientMessage(Component.translatable(TARGET_PROTECTED_KEY), true);
        return Math.max(0.0F, incomingDamage - stoppedDamage);
    }

    private static boolean applyVoodooShoveProtection(ServerPlayer target, ServerPlayer controller) {
        Optional<PoppetHandle> maybeProtection = getBoundProtectionPoppet(target, ModItems.VOODOO_PROTECTION_POPPET.get());
        if (maybeProtection.isEmpty()) {
            return false;
        }

        PoppetHandle protection = maybeProtection.get();
        if (protection.getMaxDamage() - protection.getDamageValue() <= 0) {
            return false;
        }

        protection.hurtAndBreak(1, target);
        controller.displayClientMessage(Component.translatable(TARGET_PROTECTED_KEY), true);
        return true;
    }

    private static void damageVoodooPoppet(ItemStack stack, ServerPlayer controller, float dealtDamage) {
        if (!stack.isDamageableItem()) {
            return;
        }

        int poppetDamage = (int) Math.ceil(dealtDamage);
        if (poppetDamage > 0) {
            stack.hurtAndBreak(poppetDamage, (ServerLevel) controller.level(), controller, item -> {});
            controller.getInventory().setChanged();
        }
    }

    private static boolean isBoundFor(ItemStack stack, Item item, ServerPlayer player) {
        return stack.is(item)
                && Waystone.getBloodTarget(stack)
                .map(target -> target.entityUuid().equals(player.getUUID()))
                .orElse(false);
    }

    private static boolean isUsableVampiricPoppet(ItemStack stack, ServerPlayer player) {
        Optional<Waystone.BloodTarget> source = Waystone.getBloodTarget(stack);
        Optional<Waystone.BloodTarget> target = getVampiricTarget(stack);
        if (source.isEmpty() || target.isEmpty() || !source.get().entityUuid().equals(player.getUUID())) {
            return false;
        }

        ServerPlayer targetPlayer = player.level().getServer().getPlayerList().getPlayer(target.get().entityUuid());
        return targetPlayer != null && targetPlayer.isAlive();
    }

    public static void bindVampiricPoppet(ItemStack stack, Waystone.BloodTarget source, Waystone.BloodTarget target) {
        Waystone.bindToPlayer(stack, source.entityUuid(), source.entityName());
        CompoundTag root = getRootTag(stack);
        CompoundTag targetTag = new CompoundTag();
        targetTag.putString(ENTITY_UUID_TAG, target.entityUuid().toString());
        targetTag.putString(ENTITY_NAME_TAG, target.entityName());
        root.put(VAMPIRIC_TARGET_TAG, targetTag);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, root);
        applyBoundComponents(stack);
    }

    public static void applyBoundComponents(ItemStack stack) {
        stack.set(DataComponents.MAX_STACK_SIZE, BOUND_POPPET_STACK_SIZE);
        boundDurability(stack).ifPresent(maxDamage -> {
            stack.set(DataComponents.MAX_DAMAGE, maxDamage);
            if (!stack.has(DataComponents.DAMAGE)) {
                stack.set(DataComponents.DAMAGE, 0);
            }
        });
    }

    private static Optional<Integer> boundDurability(ItemStack stack) {
        if (stack.is(ModItems.VOODOO_POPPET.get())) {
            return Optional.of(VOODOO_POPPET_DURABILITY);
        }
        if (stack.is(ModItems.VOODOO_PROTECTION_POPPET.get())) {
            return Optional.of(VOODOO_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.ARMOR_PROTECTION_POPPET.get())) {
            return Optional.of(ARMOR_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.EARTH_PROTECTION_POPPET.get())) {
            return Optional.of(EARTH_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.FIRE_PROTECTION_POPPET.get())) {
            return Optional.of(FIRE_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.HUNGER_PROTECTION_POPPET.get())) {
            return Optional.of(HUNGER_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.TOOL_PROTECTION_POPPET.get())) {
            return Optional.of(TOOL_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.WATER_PROTECTION_POPPET.get())) {
            return Optional.of(WATER_PROTECTION_DURABILITY);
        }
        if (stack.is(ModItems.VAMPIRIC_POPPET.get())) {
            return Optional.of(VAMPIRIC_POPPET_DURABILITY);
        }
        return Optional.empty();
    }

    public static Optional<Waystone.BloodTarget> getVampiricTarget(ItemStack stack) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(VAMPIRIC_TARGET_TAG)) {
            return Optional.empty();
        }

        CompoundTag targetTag = root.getCompoundOrEmpty(VAMPIRIC_TARGET_TAG);
        Optional<String> entityUuid = targetTag.getString(ENTITY_UUID_TAG);
        if (entityUuid.isEmpty()) {
            return Optional.empty();
        }

        UUID parsedUuid;
        try {
            parsedUuid = UUID.fromString(entityUuid.get());
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }

        return Optional.of(new Waystone.BloodTarget(
                parsedUuid,
                targetTag.getString(ENTITY_NAME_TAG).orElse("")
        ));
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
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

    private static void applyLavaEffect(ServerPlayer target, float damage) {
        target.igniteForSeconds(LAVA_IGNITE_SECONDS);
        target.hurt(target.damageSources().lava(), damage);
    }

    private record ToolDamageSnapshot(ToolStackSnapshot mainHand, ToolStackSnapshot offHand) {
    }

    private record ToolStackSnapshot(Item item, int damage) {
    }

    private interface PoppetHandle {
        ItemStack stack();

        default int getMaxDamage() {
            return stack().getMaxDamage();
        }

        default int getDamageValue() {
            return stack().getDamageValue();
        }

        default void setDamageValue(int damageValue) {
            stack().setDamageValue(damageValue);
        }

        default void hurtAndBreak(int amount, ServerPlayer player) {
            stack().hurtAndBreak(amount, (ServerLevel) player.level(), player, item -> {});
            setChanged(player);
        }

        default void shrink(int amount, ServerPlayer player) {
            stack().shrink(amount);
            setChanged(player);
        }

        void setChanged(ServerPlayer player);
    }

    private record InventoryPoppetHandle(ItemStack stack) implements PoppetHandle {
        @Override
        public void setChanged(ServerPlayer player) {
            player.getInventory().setChanged();
        }
    }

    private record ShelfPoppetHandle(PoppetShelfSavedData.ShelfPoppet shelfPoppet) implements PoppetHandle {
        @Override
        public ItemStack stack() {
            return this.shelfPoppet.stack();
        }

        @Override
        public void setChanged(ServerPlayer player) {
            this.shelfPoppet.setChanged(player);
        }
    }

    private record VampiricTransfer(PoppetHandle poppet, ServerPlayer target) {
    }
}
