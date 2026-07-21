package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HeProvider;
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

import javax.annotation.Nullable;

public class ConverterFEtoHEBlockEntity extends BlockEntity implements HeProvider {
    public static int RF_INPUT = 2;
    public static long HE_OUTPUT = 5;
    private static final long MAX_POWER = 1_000_000L * HE_OUTPUT;
    private final EnergyStorage ENERGY_STORAGE = new EnergyStorage(1_000_000 * RF_INPUT);

    public ConverterFEtoHEBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_CONVERTER_FE_HE.get(), position, state);
    }

    public static void tick(Level level, BlockPos position, BlockState state, ConverterFEtoHEBlockEntity converter) {
        if (!level.isClientSide) {
            converter.serverTick((ServerLevel) level, position);
        }
    }

    private void serverTick(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            tryProvide(level, position.relative(direction), direction);
        }

        int currentEnergy = ENERGY_STORAGE.getEnergyStored();
        if (currentEnergy >= RF_INPUT) {
            int converted = (int) Math.floor(Math.min((double) currentEnergy / RF_INPUT, (double) (MAX_POWER-power) / HE_OUTPUT));
            if (converted > 0) {
                ENERGY_STORAGE.extractEnergy((int) (converted * RF_INPUT), false);
                power = power + converted * HE_OUTPUT;
            }
        }

        if (ENERGY_STORAGE.getEnergyStored() < ENERGY_STORAGE.getMaxEnergyStored()) {
            pullEnergy(level, position);
        }

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
    public void setPower(long power) { this.power = power; }

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

    private void pullEnergy(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            if (ENERGY_STORAGE.getEnergyStored() >= ENERGY_STORAGE.getMaxEnergyStored()) break;

            BlockPos targetPos = position.relative(direction);
            IEnergyStorage targetStorage = level.getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    targetPos,
                    direction.getOpposite()
            );

            if (targetStorage != null && targetStorage.canExtract()) {
                int maxExtract = ENERGY_STORAGE.getMaxEnergyStored() - ENERGY_STORAGE.getEnergyStored();

                int simulatedExtract = targetStorage.extractEnergy(maxExtract, true);
                int accepted = ENERGY_STORAGE.receiveEnergy(simulatedExtract, true);

                if (accepted > 0) {
                    int realExtract = ENERGY_STORAGE.receiveEnergy(accepted, false);
                    targetStorage.extractEnergy(realExtract, false);
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
