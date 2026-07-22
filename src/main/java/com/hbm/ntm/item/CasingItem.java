package com.hbm.ntm.item;

import com.hbm.ntm.foundry.FoundryMaterial;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

/** Registered casting subtypes of the shared {@code hbm:casing} item. */
public final class CasingItem extends Item {
    private static final String CASING = "casing";

    public CasingItem() { super(new Properties()); }

    public static ItemStack create(Item item, CasingType type, int count) {
        ItemStack stack = new ItemStack(item, count);
        CompoundTag tag = new CompoundTag();
        tag.putString(CASING, type.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(type.legacyMetadata()));
        return stack;
    }

    public static CasingType type(ItemStack stack) {
        if (!(stack.getItem() instanceof CasingItem)) return null;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains(CASING)) return CasingType.byId(tag.getString(CASING));
        CustomModelData model = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return model == null ? CasingType.SMALL : CasingType.byMetadata(model.value());
    }

    @Override public String getDescriptionId(ItemStack stack) {
        CasingType type = type(stack);
        return type == null ? super.getDescriptionId(stack) : "item.hbm.casing." + type.id();
    }

    public enum CasingType {
        SMALL("small", 0, FoundryMaterial.GUNMETAL, 18),
        LARGE("large", 1, FoundryMaterial.GUNMETAL, 36),
        SMALL_STEEL("small_steel", 2, FoundryMaterial.WEAPON_STEEL, 18),
        LARGE_STEEL("large_steel", 3, FoundryMaterial.WEAPON_STEEL, 36),
        SHOTSHELL("shotshell", 4, FoundryMaterial.GUNMETAL, 18),
        BUCKSHOT("buckshot", 5, FoundryMaterial.GUNMETAL, 18),
        BUCKSHOT_ADVANCED("buckshot_advanced", 6, FoundryMaterial.WEAPON_STEEL, 18);

        private final String id;
        private final int legacyMetadata;
        private final FoundryMaterial material;
        private final int cost;

        CasingType(String id, int legacyMetadata, FoundryMaterial material, int cost) {
            this.id = id;
            this.legacyMetadata = legacyMetadata;
            this.material = material;
            this.cost = cost;
        }

        public String id() { return id; }
        public int legacyMetadata() { return legacyMetadata; }
        public FoundryMaterial material() { return material; }
        public int cost() { return cost; }

        public static CasingType byId(String id) {
            for (CasingType type : values()) if (type.id.equals(id)) return type;
            return null;
        }
        public static CasingType byMetadata(int metadata) {
            for (CasingType type : values()) if (type.legacyMetadata == metadata) return type;
            return null;
        }
    }
}
