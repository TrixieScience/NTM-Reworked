package com.hbm.ntm.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

/** Component-backed carrier for the material-metadata {@code hbm:plate_welded}. */
public final class WeldedPlateItem extends Item {
    private static final String MATERIAL = "material";

    public WeldedPlateItem() {
        super(new Properties());
    }

    public static ItemStack create(Item item, WeldedPlateMaterial material, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(MATERIAL, material.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(material.legacyMetadata()));
        return stack;
    }

    public static ItemStack steel(Item item, int count) {
        return create(item, WeldedPlateMaterial.STEEL, count);
    }

    public static WeldedPlateMaterial material(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(MATERIAL)) {
            String id = tag.getString(MATERIAL);
            for (WeldedPlateMaterial material : WeldedPlateMaterial.values()) {
                if (material.id().equals(id)) return material;
            }
        }
        CustomModelData model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (model != null) {
            for (WeldedPlateMaterial material : WeldedPlateMaterial.values()) {
                if (material.legacyMetadata() == model.value()) return material;
            }
        }
        return WeldedPlateMaterial.STEEL;
    }

    public static boolean isSteel(ItemStack stack) {
        return material(stack) == WeldedPlateMaterial.STEEL;
    }

    @Override public String getDescriptionId(ItemStack stack) {
        return "item.hbm.plate_welded." + material(stack).id();
    }

    public enum WeldedPlateMaterial {
        STEEL("steel", 30),
        TECHNETIUM_STEEL("technetium_steel", 36),
        CADMIUM_STEEL("cadmium_steel", 43);

        private final String id;
        private final int legacyMetadata;

        WeldedPlateMaterial(String id, int legacyMetadata) {
            this.id = id;
            this.legacyMetadata = legacyMetadata;
        }

        public String id() { return id; }
        public int legacyMetadata() { return legacyMetadata; }
    }
}
