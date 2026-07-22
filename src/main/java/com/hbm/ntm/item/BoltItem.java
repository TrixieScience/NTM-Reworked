package com.hbm.ntm.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

/** Component-backed carrier for {@code hbm:bolt}. */
public final class BoltItem extends Item {
    private static final String MATERIAL = "material";

    public BoltItem() {
        super(new Properties());
    }

    public static ItemStack create(Item item, BoltMaterial material, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(MATERIAL, material.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(material.legacyMetadata()));
        return stack;
    }

    public static BoltMaterial material(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(MATERIAL)) {
            String id = tag.getString(MATERIAL);
            for (BoltMaterial material : BoltMaterial.values()) {
                if (material.id().equals(id)) return material;
            }
        }
        CustomModelData model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (model != null) {
            for (BoltMaterial material : BoltMaterial.values()) {
                if (material.legacyMetadata() == model.value()) return material;
            }
        }
        return BoltMaterial.STEEL;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.hbm.bolt." + material(stack).id();
    }

    public enum BoltMaterial {
        STEEL("steel", 30),
        LEAD("lead", 8200),
        TUNGSTEN("tungsten", 7400);

        private final String id;
        private final int legacyMetadata;

        BoltMaterial(String id, int legacyMetadata) {
            this.id = id;
            this.legacyMetadata = legacyMetadata;
        }

        public String id() { return id; }
        public int legacyMetadata() { return legacyMetadata; }
    }
}
