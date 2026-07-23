package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidDuctBlock;
import com.hbm.ntm.fluid.PrioritizedFluidHandler;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Typed fluid graph node. Ducts route fluid but never store it. */
public class FluidDuctBlockEntity extends BlockEntity {
    private final IFluidHandler handler = new DuctHandler();

    public FluidDuctBlockEntity(BlockPos position, BlockState state) {
        this(ModBlockEntities.FLUID_DUCT.get(), position, state);
    }

    protected FluidDuctBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
    }

    public FluidIdentifierItem.Selection selection() {
        return getBlockState().getValue(FluidDuctBlock.TYPE);
    }

    public IFluidHandler fluidHandler(@Nullable Direction side) {
        return handler;
    }

    public boolean routesFluid() {
        return true;
    }

    protected void recordTransfer(int amount) {
    }

    private final class DuctHandler implements IFluidHandler {
        @Override public int getTanks() { return 1; }
        @Override public FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return 1_000_000_000; }
        @Override public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && selection().accepts(stack.getFluid());
        }

        @Override public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(0, resource) || !(level instanceof ServerLevel server)) return 0;
            NetworkSearch search = findReceivers(server);
            List<IFluidHandler> receivers = search.receivers();
            if (receivers.isEmpty()) return 0;

            List<Integer> capacities = new ArrayList<>(receivers.size());
            long totalCapacity = 0L;
            for (IFluidHandler receiver : receivers) {
                int accepted = Math.max(receiver.fill(resource.copy(), FluidAction.SIMULATE), 0);
                capacities.add(accepted);
                totalCapacity += accepted;
            }
            int transferable = (int) Math.min(resource.getAmount(), Math.min(totalCapacity, Integer.MAX_VALUE));
            if (action.simulate() || transferable <= 0) return transferable;

            int moved = 0;
            for (int priority = 4; priority >= 0 && moved < transferable; priority--) {
                long priorityCapacity = 0L;
                for (int index = 0; index < receivers.size(); index++) {
                    if (priority(receivers.get(index)) == priority) priorityCapacity += capacities.get(index);
                }
                int priorityTransfer = (int) Math.min(transferable - moved, priorityCapacity);
                int priorityMoved = 0;
                long remainingCapacity = priorityCapacity;
                for (int index = 0; index < receivers.size() && priorityMoved < priorityTransfer; index++) {
                    IFluidHandler receiver = receivers.get(index);
                    int capacity = capacities.get(index);
                    if (capacity <= 0 || priority(receiver) != priority) continue;
                    int remaining = priorityTransfer - priorityMoved;
                    int share = remainingCapacity <= 0L ? remaining
                            : (int) Math.min(capacity,
                            Math.max(1L, (long) remaining * capacity / remainingCapacity));
                    FluidStack offered = resource.copyWithAmount(share);
                    priorityMoved += receiver.fill(offered, FluidAction.EXECUTE);
                    remainingCapacity -= capacity;
                }
                moved += priorityMoved;
            }
            if (moved > 0) {
                for (FluidDuctBlockEntity duct : search.ducts()) duct.recordTransfer(moved);
            }
            return moved;
        }

        @Override public FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    private NetworkSearch findReceivers(ServerLevel server) {
        FluidIdentifierItem.Selection type = selection();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, IFluidHandler> endpoints = new LinkedHashMap<>();
        List<FluidDuctBlockEntity> ducts = new ArrayList<>();
        queue.add(worldPosition);
        visited.add(worldPosition);

        while (!queue.isEmpty()) {
            BlockPos ductPosition = queue.removeFirst();
            if (!(server.getBlockEntity(ductPosition) instanceof FluidDuctBlockEntity current)
                    || !current.routesFluid()) continue;
            ducts.add(current);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = ductPosition.relative(direction);
                if (server.getBlockEntity(neighbor) instanceof FluidDuctBlockEntity duct) {
                    if (duct.selection() == type && duct.routesFluid() && visited.add(neighbor)) {
                        queue.addLast(neighbor);
                    }
                    continue;
                }
                if (endpoints.containsKey(neighbor)) continue;
                IFluidHandler candidate = server.getCapability(Capabilities.FluidHandler.BLOCK,
                        neighbor, direction.getOpposite());
                if (candidate != null) endpoints.put(neighbor.immutable(), candidate);
            }
        }
        return new NetworkSearch(List.copyOf(endpoints.values()), List.copyOf(ducts));
    }

    private record NetworkSearch(List<IFluidHandler> receivers, List<FluidDuctBlockEntity> ducts) {
    }

    private static int priority(IFluidHandler handler) {
        return handler instanceof PrioritizedFluidHandler prioritized
                ? Math.clamp(prioritized.fluidPriority(), 0, 4) : 2;
    }
}
