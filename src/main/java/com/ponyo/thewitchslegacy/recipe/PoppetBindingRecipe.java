package com.ponyo.thewitchslegacy.recipe;

import com.ponyo.thewitchslegacy.item.ModItems;
import com.ponyo.thewitchslegacy.item.custom.Poppet;
import com.ponyo.thewitchslegacy.item.custom.Waystone;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class PoppetBindingRecipe extends CustomRecipe {
    public PoppetBindingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        Binding binding = findBinding(input);
        return binding != null && binding.canBind();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Binding binding = findBinding(input);
        if (binding == null || !binding.canBind()) {
            return ItemStack.EMPTY;
        }

        return binding.assemble();
    }

    @Override
    public RecipeSerializer<PoppetBindingRecipe> getSerializer() {
        return ModRecipeSerializers.POPPET_BINDING.get();
    }

    private static Binding findBinding(CraftingInput input) {
        Binding vampiricBinding = findVampiricBinding(input);
        if (vampiricBinding != null) {
            return vampiricBinding;
        }
        return findSingleClaimBinding(input);
    }

    private static Binding findSingleClaimBinding(CraftingInput input) {
        if (input.ingredientCount() != 2) {
            return null;
        }

        ItemStack poppet = ItemStack.EMPTY;
        ItemStack claim = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (isBindablePoppet(stack)) {
                if (!poppet.isEmpty()) {
                    return null;
                }
                poppet = stack;
            } else if (stack.is(ModItems.WITCHS_CLAIM_FILLED.get())) {
                if (!claim.isEmpty()) {
                    return null;
                }
                claim = stack;
            } else {
                return null;
            }
        }

        return poppet.isEmpty() || claim.isEmpty() ? null : new SingleClaimBinding(poppet, claim);
    }

    private static Binding findVampiricBinding(CraftingInput input) {
        if (input.width() != 3 || input.ingredientCount() != 3) {
            return null;
        }

        Binding binding = null;
        for (int y = 0; y < input.height(); y++) {
            ItemStack first = input.getItem(0, y);
            ItemStack second = input.getItem(1, y);
            ItemStack third = input.getItem(2, y);
            boolean rowIsEmpty = first.isEmpty() && second.isEmpty() && third.isEmpty();
            if (rowIsEmpty) {
                continue;
            }
            if (binding != null
                    || !first.is(ModItems.WITCHS_CLAIM_FILLED.get())
                    || !second.is(ModItems.VAMPIRIC_POPPET.get())
                    || !third.is(ModItems.WITCHS_CLAIM_FILLED.get())) {
                return null;
            }
            binding = new VampiricBinding(first, second, third);
        }

        return binding;
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

    private interface Binding {
        boolean canBind();

        ItemStack assemble();
    }

    private record SingleClaimBinding(ItemStack poppet, ItemStack claim) implements Binding {
        @Override
        public boolean canBind() {
            return Waystone.getBloodTarget(poppet).isEmpty() && Waystone.getBloodTarget(claim).isPresent();
        }

        @Override
        public ItemStack assemble() {
            ItemStack result = poppet.copyWithCount(1);
            Waystone.getBloodTarget(claim)
                    .ifPresent(target -> Waystone.bindToPlayer(result, target.entityUuid(), target.entityName()));
            return result;
        }
    }

    private record VampiricBinding(ItemStack sourceClaim, ItemStack poppet, ItemStack targetClaim) implements Binding {
        @Override
        public boolean canBind() {
            Optional<Waystone.BloodTarget> source = Waystone.getBloodTarget(sourceClaim);
            Optional<Waystone.BloodTarget> target = Waystone.getBloodTarget(targetClaim);
            return source.isPresent()
                    && target.isPresent()
                    && !source.get().entityUuid().equals(target.get().entityUuid())
                    && Waystone.getBloodTarget(poppet).isEmpty()
                    && Poppet.getVampiricTarget(poppet).isEmpty();
        }

        @Override
        public ItemStack assemble() {
            Optional<Waystone.BloodTarget> source = Waystone.getBloodTarget(sourceClaim);
            Optional<Waystone.BloodTarget> target = Waystone.getBloodTarget(targetClaim);
            if (source.isEmpty() || target.isEmpty()) {
                return ItemStack.EMPTY;
            }

            ItemStack result = poppet.copyWithCount(1);
            Poppet.bindVampiricPoppet(result, source.get(), target.get());
            return result;
        }
    }
}
