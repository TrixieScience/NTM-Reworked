package com.hbm.ntm.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

/** Component-backed carrier for {@code hbm:plate_cast}. */
public final class CastPlateItem extends Item {
    private static final String MATERIAL = "material";

    public CastPlateItem() {
        super(new Properties());
    }

    public static ItemStack create(Item item, CastPlateMaterial material, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(MATERIAL, material.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(material.legacyMetadata()));
        return stack;
    }

    public static CastPlateMaterial material(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(MATERIAL)) {
            String id = tag.getString(MATERIAL);
            for (CastPlateMaterial material : CastPlateMaterial.values()) {
                if (material.id().equals(id)) return material;
            }
        }
        CustomModelData model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (model != null) {
            for (CastPlateMaterial material : CastPlateMaterial.values()) {
                if (material.legacyMetadata() == model.value()) return material;
            }
        }
        return CastPlateMaterial.IRON;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return "item.hbm.plate_cast." + material(stack).id();
    }

    public enum CastPlateMaterial {
        IRON("iron", 2600),
        TITANIUM("titanium", 2200),
        STEEL("steel", 30),
        COPPER("copper", 2900),
        DURA_STEEL("dura_steel", 33),
        TECHNETIUM_STEEL("technetium_steel", 36),
        CADMIUM_STEEL("cadmium_steel", 43);

        private final String id;
        private final int legacyMetadata;

        CastPlateMaterial(String id, int legacyMetadata) {
            this.id = id;
            this.legacyMetadata = legacyMetadata;
        }

        public String id() { return id; }
        public int legacyMetadata() { return legacyMetadata; }
    }
}
