package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.function.Supplier;

/** Fixed-size full/empty swap for dedicated canisters and gas tanks. */
public final class SourceFluidContainerHandler implements IFluidHandlerItem {
    private final SourceFluidContainerItem.ContainedFluid[] acceptedFluids;
    private final Supplier<Item> emptyItem;
    private final Supplier<Item> fullItem;
    private final int capacity;
    private final boolean consumable;
    private final Supplier<Fluid> fixedFluid;
    private ItemStack container;
    private boolean full;

    private SourceFluidContainerHandler(ItemStack container,
                                        SourceFluidContainerItem.ContainedFluid[] acceptedFluids,
                                        Supplier<Item> emptyItem, Supplier<Item> fullItem,
                                        int capacity, boolean full, boolean consumable,
                                        Supplier<Fluid> fixedFluid) {
        this.container = container;
        this.acceptedFluids = acceptedFluids;
        this.emptyItem = emptyItem;
        this.fullItem = fullItem;
        this.capacity = capacity;
        this.full = full;
        this.consumable = consumable;
        this.fixedFluid = fixedFluid;
    }

    public static SourceFluidContainerHandler empty(ItemStack container,
                                                    SourceFluidContainerItem.ContainedFluid fluid,
                                                    Supplier<Item> emptyItem, Supplier<Item> fullItem, int capacity) {
        return empty(container, new SourceFluidContainerItem.ContainedFluid[]{fluid},
                emptyItem, fullItem, capacity);
    }

    public static SourceFluidContainerHandler empty(ItemStack container,
                                                    SourceFluidContainerItem.ContainedFluid[] fluids,
                                                    Supplier<Item> emptyItem, Supplier<Item> fullItem, int capacity) {
        return new SourceFluidContainerHandler(container, fluids, emptyItem, fullItem,
                capacity, false, false, null);
    }

    public static SourceFluidContainerHandler full(ItemStack container,
                                                   Supplier<Item> emptyItem, Supplier<Item> fullItem, int capacity) {
        return new SourceFluidContainerHandler(container,
                new SourceFluidContainerItem.ContainedFluid[]{SourceFluidContainerItem.fluid(container)},
                emptyItem, fullItem, capacity, true, false, null);
    }

    public static SourceFluidContainerHandler fixedEmpty(ItemStack container,
                                                         SourceFluidContainerItem.ContainedFluid fluid,
                                                         Supplier<Item> emptyItem, Supplier<Item> fullItem,
                                                         int capacity) {
        return new SourceFluidContainerHandler(container,
                new SourceFluidContainerItem.ContainedFluid[]{fluid}, emptyItem, fullItem,
                capacity, false, false, fluid::fluid);
    }

    public static SourceFluidContainerHandler fixedFull(ItemStack container,
                                                        SourceFluidContainerItem.ContainedFluid fluid,
                                                        Supplier<Item> emptyItem, Supplier<Item> fullItem,
                                                        int capacity) {
        return new SourceFluidContainerHandler(container,
                new SourceFluidContainerItem.ContainedFluid[]{fluid}, emptyItem, fullItem,
                capacity, true, false, fluid::fluid);
    }

    public static SourceFluidContainerHandler consumable(ItemStack container, Supplier<Fluid> fluid, int capacity) {
        return new SourceFluidContainerHandler(container,
                new SourceFluidContainerItem.ContainedFluid[0],
                () -> net.minecraft.world.item.Items.AIR, () -> container.getItem(),
                capacity, true, true, fluid);
    }

    private SourceFluidContainerItem.ContainedFluid typeFor(Fluid resource) {
        for (SourceFluidContainerItem.ContainedFluid candidate : acceptedFluids) {
            if (resource.isSame(candidate.fluid())) return candidate;
        }
        return SourceFluidContainerItem.ContainedFluid.NONE;
    }

    private SourceFluidContainerItem.ContainedFluid storedType() {
        return acceptedFluids.length == 0
                ? SourceFluidContainerItem.ContainedFluid.NONE : acceptedFluids[0];
    }

    private Fluid storedFluid() {
        return fixedFluid != null ? fixedFluid.get() : storedType().fluid();
    }

    @Override public ItemStack getContainer() { return container; }
    @Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int tank) {
        return full ? new FluidStack(storedFluid(), capacity) : FluidStack.EMPTY;
    }
    @Override public int getTankCapacity(int tank) { return capacity; }
    @Override public boolean isFluidValid(int tank, FluidStack stack) {
        return !full && typeFor(stack.getFluid()) != SourceFluidContainerItem.ContainedFluid.NONE;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        SourceFluidContainerItem.ContainedFluid type = typeFor(resource.getFluid());
        if (full || container.getCount() != 1 || resource.getAmount() < capacity
                || type == SourceFluidContainerItem.ContainedFluid.NONE) return 0;
        if (action.execute()) {
            container = fixedFluid == null
                    ? SourceFluidContainerItem.create(fullItem.get(), type, 1)
                    : new ItemStack(fullItem.get());
            full = true;
        }
        return capacity;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!full || resource.getAmount() < capacity
                || !resource.getFluid().isSame(storedFluid())) return FluidStack.EMPTY;
        return drain(capacity, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (!full || container.getCount() != 1 || maxDrain < capacity) return FluidStack.EMPTY;
        FluidStack drained = new FluidStack(storedFluid(), capacity);
        if (action.execute()) {
            if (consumable) container.shrink(1);
            else container = container.transmuteCopy(emptyItem.get(), 1);
            full = false;
        }
        return drained;
    }
}
