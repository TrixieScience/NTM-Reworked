package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HeReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ConverterHEtoFEBlockEntity extends BlockEntity implements HeReceiver {
    public static long HE_INPUT = 5;
    public static int RF_OUTPUT = 1;
    private static final long MAX_POWER = 1_000_000L * HE_INPUT;
    private final EnergyStorage ENERGY_STORAGE = new EnergyStorage(1_000_000 * RF_OUTPUT);

    public ConverterHEtoFEBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_CONVERTER_HE_FE.get(), position, state);
    }

    public static void tick(Level level, BlockPos position, BlockState state, ConverterHEtoFEBlockEntity converter) {
        if (!level.isClientSide) {
            converter.serverTick((ServerLevel) level, position);
        }
    }

    private void serverTick(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            trySubscribe(level, position.relative(direction), direction);
        }

        if (power >= HE_INPUT) {
            int converted = (int) Math.floor(Math.min((double) power / HE_INPUT, (double) (ENERGY_STORAGE.getMaxEnergyStored() - ENERGY_STORAGE.getEnergyStored()) / RF_OUTPUT));
            if (converted > 0) {
                ENERGY_STORAGE.receiveEnergy((int) (converted * RF_OUTPUT), false);
                power = power - converted * HE_INPUT;
            }
        }

        if (ENERGY_STORAGE.getEnergyStored() > 0) {
            pushEnergy(level, position);
        }

        int currentEnergy = ENERGY_STORAGE.getEnergyStored();
        if (power != lastSyncedPower || currentEnergy != lastSyncedEnergy || level.getGameTime() % 20L == 0L) {
            lastSyncedPower = power;
            lastSyncedEnergy = currentEnergy;
            level.sendBlockUpdated(position, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    private long power;
    private long lastSyncedPower = Long.MIN_VALUE;
    private int lastSyncedEnergy = Integer.MIN_VALUE;

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return MAX_POWER;
    }

    @Override
    public boolean isHeLoaded() {
        return hasLevel() && !isRemoved();
    }

    public IEnergyStorage getEnergy() {
        return ENERGY_STORAGE;
    }

    private void pushEnergy(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            if (ENERGY_STORAGE.getEnergyStored() <= 0) break;

            BlockPos targetPos = position.relative(direction);
            IEnergyStorage targetStorage = level.getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    targetPos,
                    direction.getOpposite()
            );

            if (targetStorage != null && targetStorage.canReceive()) {
                int maxExtract = Math.min(ENERGY_STORAGE.getEnergyStored(), 1_000_000);

                int simulatedExtract = ENERGY_STORAGE.extractEnergy(maxExtract, true);
                int accepted = targetStorage.receiveEnergy(simulatedExtract, true);

                if (accepted > 0) {
                    int realExtract = ENERGY_STORAGE.extractEnergy(accepted, false);
                    targetStorage.receiveEnergy(realExtract, false);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("power", power);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        power = tag.getLong("power");
        if (tag.contains("energy")) {
            ENERGY_STORAGE.deserializeNBT(registries, tag.get("energy"));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("power", power);
        tag.putInt("energy", ENERGY_STORAGE.getEnergyStored());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        power = tag.getLong("power");
        if (tag.contains("energy")) {
            ENERGY_STORAGE.deserializeNBT(registries, tag.get("energy"));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
