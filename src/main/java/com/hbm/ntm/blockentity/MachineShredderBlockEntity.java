package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HeReceiver;
import com.hbm.ntm.inventory.MachineShredderMenu;
import com.hbm.ntm.item.HeBatteryItem;
import com.hbm.ntm.item.ShredderBladeItem;
import com.hbm.ntm.recipe.ShredderRecipes;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class MachineShredderBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, HeReceiver,
        MufflableMachine {
    public static final int INPUT_START = 0;
    public static final int INPUT_END = 9;
    public static final int OUTPUT_START = 9;
    public static final int OUTPUT_END = 27;
    public static final int BLADE_LEFT = 27;
    public static final int BLADE_RIGHT = 28;
    public static final int BATTERY = 29;
    public static final int SLOT_COUNT = 30;
    public static final long MAX_POWER = 10_000L;
    public static final int PROCESSING_SPEED = 60;
    public static final long POWER_PER_TICK = 5L;
    private static final int[] AUTOMATION_SLOTS = createAutomationSlots();

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> (int) power;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> power = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private Component customName;
    private long power;
    private int progress;
    private int soundCycle;
    private boolean muffled;
    private long lastSyncedPower = Long.MIN_VALUE;

    public MachineShredderBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.MACHINE_SHREDDER.get(), position, state);
    }

    public static void tick(Level level, BlockPos position, BlockState state, MachineShredderBlockEntity shredder) {
        if (!level.isClientSide) {
            shredder.serverTick((ServerLevel) level, position);
        }
    }

    private void serverTick(ServerLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            trySubscribe(level, position.relative(direction), direction);
        }

        if (progress == 0) {
            soundCycle = 0;
        }

        if (hasPower() && canProcess()) {
            progress++;
            power -= POWER_PER_TICK;

            if (progress == PROCESSING_SPEED) {
                damageBlade(BLADE_LEFT);
                damageBlade(BLADE_RIGHT);
                progress = 0;
                processItems();
            }

            if (soundCycle == 0) {
                level.playSound(null, position, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS,
                        muffled ? 0.1F : 1.0F, 0.75F);
            }
            soundCycle++;
            if (soundCycle >= 50) {
                soundCycle = 0;
            }
        } else {
            progress = 0;
        }

        dischargeBattery();
        if (power != lastSyncedPower || level.getGameTime() % 20L == 0L) {
            lastSyncedPower = power;
            level.sendBlockUpdated(position, getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        setChanged();
    }

    private void damageBlade(int slot) {
        ItemStack blade = items.get(slot);
        if (blade.getMaxDamage() > 0) {
            blade.setDamageValue(blade.getDamageValue() + 1);
        }
    }

    private void dischargeBattery() {
        ItemStack batteryStack = items.get(BATTERY);
        if (!(batteryStack.getItem() instanceof HeBatteryItem battery)) {
            return;
        }
        long toDischarge = Math.min(Math.min(MAX_POWER - power, battery.getDischargeRate(batteryStack)),
                battery.getCharge(batteryStack));
        if (toDischarge > 0) {
            battery.discharge(batteryStack, toDischarge);
            power += toDischarge;
        }
    }

    private void processItems() {
        for (int inputSlot = INPUT_START; inputSlot < INPUT_END; inputSlot++) {
            ItemStack input = items.get(inputSlot);
            if (input.isEmpty() || !hasSpace(input)) {
                continue;
            }

            ItemStack output = ShredderRecipes.getResult(input);
            boolean merged = false;
            for (int outputSlot = OUTPUT_START; outputSlot < OUTPUT_END; outputSlot++) {
                ItemStack existing = items.get(outputSlot);
                if (!existing.isEmpty()
                        && ItemStack.isSameItem(existing, output)
                        && existing.getDamageValue() == output.getDamageValue()
                        && existing.getCount() + output.getCount() <= output.getMaxStackSize()) {
                    existing.grow(output.getCount());
                    input.shrink(1);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                for (int outputSlot = OUTPUT_START; outputSlot < OUTPUT_END; outputSlot++) {
                    if (items.get(outputSlot).isEmpty()) {
                        items.set(outputSlot, output.copy());
                        input.shrink(1);
                        break;
                    }
                }
            }

            if (input.isEmpty()) {
                items.set(inputSlot, ItemStack.EMPTY);
            }
        }
    }

    public boolean canProcess() {
        int left = getBladeState(BLADE_LEFT);
        int right = getBladeState(BLADE_RIGHT);
        if (left <= 0 || left >= 3 || right <= 0 || right >= 3) {
            return false;
        }
        for (int slot = INPUT_START; slot < INPUT_END; slot++) {
            ItemStack input = items.get(slot);
            if (!input.isEmpty() && hasSpace(input)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpace(ItemStack input) {
        ItemStack result = ShredderRecipes.getResult(input);
        for (int slot = OUTPUT_START; slot < OUTPUT_END; slot++) {
            ItemStack existing = items.get(slot);
            if (existing.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItem(existing, result)
                    && existing.getCount() + result.getCount() <= result.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public int getBladeState(int slot) {
        ItemStack blade = items.get(slot);
        if (!(blade.getItem() instanceof ShredderBladeItem)) {
            return 0;
        }
        if (blade.getMaxDamage() == 0) {
            return 1;
        }
        if (blade.getDamageValue() < blade.getMaxDamage() / 2) {
            return 1;
        }
        return blade.getDamageValue() != blade.getMaxDamage() ? 2 : 3;
    }

    public boolean hasPower() {
        return power > 0;
    }

    public int getProgressScaled(int width) {
        return progress * width / PROCESSING_SPEED;
    }

    public long getPowerScaled(long height) {
        return power * height / MAX_POWER;
    }

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

    @Override
    public boolean installMuffler() {
        if (muffled) return false;
        muffled = true;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Override
    public boolean isMuffled() {
        return muffled;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putLong("powerTime", power);
        tag.putBoolean("muffled", muffled);
        if (customName != null) {
            tag.putString("name", Component.Serializer.toJson(customName, registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        power = tag.getLong("powerTime");
        muffled = tag.getBoolean("muffled");
        customName = tag.contains("name") ? Component.Serializer.fromJson(tag.getString("name"), registries) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("powerTime", power);
        tag.putBoolean("muffled", muffled);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        power = tag.getLong("powerTime");
        muffled = tag.getBoolean("muffled");
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.machineShredder");
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
        setChanged();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MachineShredderMenu(containerId, playerInventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot < INPUT_END) {
            return !(stack.getItem() instanceof ShredderBladeItem);
        }
        if (slot == BATTERY) {
            return stack.getItem() instanceof HeBatteryItem;
        }
        if (slot == BLADE_LEFT || slot == BLADE_RIGHT) {
            return stack.getItem() instanceof ShredderBladeItem;
        }
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return AUTOMATION_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if ((slot >= OUTPUT_START && slot != BLADE_LEFT && slot != BLADE_RIGHT) || !canPlaceItem(slot, stack)) {
            return false;
        }
        if (items.get(slot).isEmpty()) {
            return true;
        }

        int size = items.get(slot).getCount();
        for (int inputSlot = INPUT_START; inputSlot < INPUT_END; inputSlot++) {
            ItemStack existing = items.get(inputSlot);
            if (existing.isEmpty()) {
                return false;
            }
            if (ItemStack.isSameItem(existing, stack)
                    && existing.getDamageValue() == stack.getDamageValue()
                    && existing.getCount() < size) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        if (slot >= OUTPUT_START && slot < OUTPUT_END) {
            return true;
        }
        return (slot == BLADE_LEFT || slot == BLADE_RIGHT)
                && stack.getMaxDamage() > 0
                && stack.getDamageValue() == stack.getMaxDamage();
    }

    public int progress() {
        return progress;
    }

    private static int[] createAutomationSlots() {
        int[] slots = new int[SLOT_COUNT];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }
}
