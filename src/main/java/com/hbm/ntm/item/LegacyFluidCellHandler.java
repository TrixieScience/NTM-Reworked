package com.hbm.ntm.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;
import java.util.function.Supplier;

/** One-bucket cells which swap the whole item when filled or drained. */
public final class LegacyFluidCellHandler implements IFluidHandlerItem {
    private final List<Filling> fillings;
    private final Supplier<Item> emptyItem;
    private final int capacity;
    private ItemStack container;
    private Filling contents;

    private LegacyFluidCellHandler(ItemStack container, List<Filling> fillings,
                                   Supplier<Item> emptyItem, int capacity, Filling contents) {
        this.container = container;
        this.fillings = fillings;
        this.emptyItem = emptyItem;
        this.capacity = capacity;
        this.contents = contents;
    }

    public static LegacyFluidCellHandler empty(ItemStack container, List<Filling> fillings,
                                               Supplier<Item> emptyItem, int capacity) {
        return new LegacyFluidCellHandler(container, fillings, emptyItem, capacity, null);
    }

    public static LegacyFluidCellHandler full(ItemStack container, Filling contents,
                                              Supplier<Item> emptyItem, int capacity) {
        return new LegacyFluidCellHandler(container, List.of(contents), emptyItem, capacity, contents);
    }

    @Override public ItemStack getContainer() { return container; }
    @Override public int getTanks() { return 1; }
    @Override public int getTankCapacity(int tank) { return capacity; }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return contents == null ? FluidStack.EMPTY : new FluidStack(contents.fluid().get(), capacity);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return contents == null && find(stack.getFluid()) != null;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        Filling filling = find(resource.getFluid());
        if (contents != null || container.getCount() != 1 || resource.getAmount() < capacity || filling == null) {
            return 0;
        }
        if (action.execute()) {
            container = container.transmuteCopy(filling.item().get(), 1);
            contents = filling;
        }
        return capacity;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (contents == null || resource.getAmount() < capacity
                || !resource.getFluid().isSame(contents.fluid().get())) return FluidStack.EMPTY;
        return drain(capacity, action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (contents == null || container.getCount() != 1 || maxDrain < capacity) return FluidStack.EMPTY;
        FluidStack drained = new FluidStack(contents.fluid().get(), capacity);
        if (action.execute()) {
            container = container.transmuteCopy(emptyItem.get(), 1);
            contents = null;
        }
        return drained;
    }

    private Filling find(Fluid fluid) {
        for (Filling filling : fillings) if (fluid.isSame(filling.fluid().get())) return filling;
        return null;
    }

    public record Filling(Supplier<? extends Fluid> fluid, Supplier<? extends Item> item) { }
}
