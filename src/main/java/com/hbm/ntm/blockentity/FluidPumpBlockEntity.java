package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidPumpBlock;
import com.hbm.ntm.fluid.PrioritizedFluidHandler;
import com.hbm.ntm.inventory.FluidPumpMenu;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public final class FluidPumpBlockEntity extends BlockEntity implements MenuProvider {
    public static final int MAX_THROUGHPUT = 10_000;
    public static final int PRIORITY_COUNT = 5;

    private FluidIdentifierItem.Selection selection = FluidIdentifierItem.Selection.NONE;
    private final FluidTank tank = new FluidTank(MAX_THROUGHPUT,
            stack -> selection.accepts(stack.getFluid())) {
        @Override protected void onContentsChanged() { FluidPumpBlockEntity.this.setChanged(); }
    };
    private int throughput = 100;
    private int pressure;
    private int priority = 2;
    private boolean redstone;
    private boolean pushing;
    private int lastAmount = Integer.MIN_VALUE;
    private int lastThroughput = Integer.MIN_VALUE;
    private int lastPressure = Integer.MIN_VALUE;
    private int lastPriority = Integer.MIN_VALUE;
    private int lastSelection = Integer.MIN_VALUE;
    private boolean lastRedstone;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> tank.getFluidAmount();
                case 1 -> selection.ordinal();
                case 2 -> throughput;
                case 3 -> pressure;
                case 4 -> priority;
                case 5 -> redstone ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case 1 -> selection = selection(value);
                case 2 -> throughput = Mth.clamp(value, 0, MAX_THROUGHPUT);
                case 3 -> pressure = Mth.clamp(value, 0, 5);
                case 4 -> priority = Mth.clamp(value, 0, PRIORITY_COUNT - 1);
                case 5 -> redstone = value != 0;
                default -> { }
            }
        }
        @Override public int getCount() { return 6; }
    };

    public FluidPumpBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.FLUID_PUMP.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidPumpBlockEntity pump) {
        if (!(level instanceof ServerLevel server)) return;
        pump.redstone = level.hasNeighborSignal(pos);
        if (!pump.redstone) pump.push(server, pos);
        pump.syncIfChanged(server, state);
    }

    private void push(ServerLevel level, BlockPos pos) {
        if (tank.isEmpty() || throughput <= 0) return;
        Direction output = outputSide();
        IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                pos.relative(output), output.getOpposite());
        if (target == null) return;
        FluidStack offered = tank.getFluid().copyWithAmount(Math.min(throughput, tank.getFluidAmount()));
        pushing = true;
        int accepted;
        try {
            accepted = target.fill(offered, IFluidHandler.FluidAction.EXECUTE);
        } finally {
            pushing = false;
        }
        if (accepted > 0) tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
    }

    public IFluidHandler fluidHandler(@Nullable Direction side) {
        if (side == null || side == inputSide() || side == outputSide()) return new PumpHandler(side);
        return null;
    }

    private final class PumpHandler implements PrioritizedFluidHandler {
        private final Direction side;

        private PumpHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int index) {
            return index == 0 ? tank.getFluid().copy() : FluidStack.EMPTY;
        }
        @Override public int getTankCapacity(int index) {
            return index == 0 ? effectiveCapacity() : 0;
        }
        @Override public boolean isFluidValid(int index, FluidStack stack) {
            return index == 0 && canFill() && !pushing && selection.accepts(stack.getFluid());
        }
        @Override public int fill(FluidStack resource, FluidAction action) {
            if (!isFluidValid(0, resource)) return 0;
            int room = Math.max(effectiveCapacity() - tank.getFluidAmount(), 0);
            return tank.fill(resource.copyWithAmount(Math.min(resource.getAmount(), room)), action);
        }
        @Override public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canDrain() || resource.isEmpty() || tank.isEmpty()
                    || !resource.getFluid().isSame(tank.getFluid().getFluid())) return FluidStack.EMPTY;
            return tank.drain(resource.copyWithAmount(Math.min(resource.getAmount(), throughput)), action);
        }
        @Override public FluidStack drain(int maxDrain, FluidAction action) {
            return canDrain() ? tank.drain(Math.min(maxDrain, throughput), action) : FluidStack.EMPTY;
        }
        @Override public int fluidPriority() { return priority; }
        private boolean canFill() { return side == null || side == inputSide(); }
        private boolean canDrain() { return !redstone && (side == null || side == outputSide()); }
    }

    private int effectiveCapacity() {
        return Math.max(throughput, tank.getFluidAmount());
    }

    public Direction inputSide() {
        return getBlockState().getValue(FluidPumpBlock.FACING).getClockWise();
    }

    public Direction outputSide() {
        return inputSide().getOpposite();
    }

    public void selectFluid(FluidIdentifierItem.Selection next) {
        if (next == FluidIdentifierItem.Selection.NONE || next == selection) return;
        selection = next;
        tank.setFluid(FluidStack.EMPTY);
        changed();
    }

    public void configure(int nextThroughput, int nextPressure, int nextPriority) {
        nextThroughput = Mth.clamp(nextThroughput, 0, MAX_THROUGHPUT);
        nextPressure = Mth.clamp(nextPressure, 0, 5);
        nextPriority = Mth.clamp(nextPriority, 0, PRIORITY_COUNT - 1);
        if (pressure != nextPressure) tank.setFluid(FluidStack.EMPTY);
        throughput = nextThroughput;
        pressure = nextPressure;
        priority = nextPriority;
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void syncIfChanged(ServerLevel level, BlockState state) {
        int amount = tank.getFluidAmount();
        int selected = selection.ordinal();
        if (amount == lastAmount && throughput == lastThroughput && pressure == lastPressure
                && priority == lastPriority && selected == lastSelection && redstone == lastRedstone) return;
        lastAmount = amount;
        lastThroughput = throughput;
        lastPressure = pressure;
        lastPriority = priority;
        lastSelection = selected;
        lastRedstone = redstone;
        setChanged();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    public FluidIdentifierItem.Selection selection() { return selection; }
    public int amount() { return tank.getFluidAmount(); }
    public int throughput() { return throughput; }
    public int pressure() { return pressure; }
    public int priority() { return priority; }
    public boolean redstone() { return redstone; }
    public String priorityName() {
        return switch (priority) {
            case 0 -> "LOWEST";
            case 1 -> "LOW";
            case 3 -> "HIGH";
            case 4 -> "HIGHEST";
            default -> "NORMAL";
        };
    }

    @Override public Component getDisplayName() {
        return Component.translatable("block.hbm.fluid_pump");
    }

    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FluidPumpMenu(id, inventory, this, data);
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("selectedFluid", selection.id());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("throughput", throughput);
        tag.putInt("pressure", pressure);
        tag.putInt("priority", priority);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        throughput = Mth.clamp(tag.getInt("throughput"), 0, MAX_THROUGHPUT);
        if (!tag.contains("throughput")) throughput = 100;
        pressure = Mth.clamp(tag.getInt("pressure"), 0, 5);
        priority = tag.contains("priority") ? Mth.clamp(tag.getInt("priority"), 0, PRIORITY_COUNT - 1) : 2;
        if (!tank.isEmpty() && !selection.accepts(tank.getFluid().getFluid())) tank.setFluid(FluidStack.EMPTY);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("selectedFluid", selection.id());
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("throughput", throughput);
        tag.putInt("pressure", pressure);
        tag.putInt("priority", priority);
        tag.putBoolean("redstone", redstone);
        return tag;
    }

    @Override public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        selection = FluidIdentifierItem.Selection.byId(tag.getString("selectedFluid"));
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        throughput = Mth.clamp(tag.getInt("throughput"), 0, MAX_THROUGHPUT);
        pressure = Mth.clamp(tag.getInt("pressure"), 0, 5);
        priority = Mth.clamp(tag.getInt("priority"), 0, PRIORITY_COUNT - 1);
        redstone = tag.getBoolean("redstone");
    }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static FluidIdentifierItem.Selection selection(int ordinal) {
        FluidIdentifierItem.Selection[] values = FluidIdentifierItem.Selection.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FluidIdentifierItem.Selection.NONE;
    }
}
