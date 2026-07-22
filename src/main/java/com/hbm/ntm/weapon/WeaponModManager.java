package com.hbm.ntm.weapon;

import com.hbm.ntm.item.TwentyTwoGunItem;
import com.hbm.ntm.item.NineMillimeterGunItem;
import com.hbm.ntm.item.DualUziItem;
import com.hbm.ntm.item.DualStarFItem;
import com.hbm.ntm.item.G3Item;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class WeaponModManager {
    public static final int TABLE_SLOTS = 7;
    public static final int SILENCER = 201;
    private static final String MOD_LIST = "KEY_MOD_LIST_";

    private WeaponModManager() {}

    public static boolean isMod(ItemStack stack) {
        return stack.is(ModItems.WEAPON_MOD_SILENCER.get());
    }

    public static boolean isApplicable(ItemStack gun, ItemStack mod, int config) {
        if (gun.isEmpty() || mod.isEmpty() || config < 0 || config >= configCount(gun)) return false;
        if (!mod.is(ModItems.WEAPON_MOD_SILENCER.get())) return false;
        if (gun.getItem() instanceof TwentyTwoGunItem item) {
            return item.variant() == TwentyTwoGunItem.Variant.AM180
                    || item.variant() == TwentyTwoGunItem.Variant.STAR_F;
        }
        if (gun.getItem() instanceof DualUziItem || gun.getItem() instanceof DualStarFItem) return true;
        if (gun.getItem() instanceof NineMillimeterGunItem item) {
            return item.variant() == NineMillimeterGunItem.Variant.UZI;
        }
        if (gun.getItem() instanceof G3Item item) return item.variant() == G3Item.Variant.STANDARD;
        return gun.is(ModItems.GUN_AMAT.get());
    }

    public static int configCount(ItemStack gun) {
        if (gun.getItem() instanceof DualUziItem) return DualUziItem.RECEIVER_COUNT;
        if (gun.getItem() instanceof DualStarFItem) return DualStarFItem.RECEIVER_COUNT;
        if (gun.getItem() instanceof TwentyTwoGunItem item
                && (item.variant() == TwentyTwoGunItem.Variant.AM180
                || item.variant() == TwentyTwoGunItem.Variant.STAR_F)) return 1;
        if (gun.getItem() instanceof NineMillimeterGunItem item
                && item.variant() == NineMillimeterGunItem.Variant.UZI) return 1;
        if (gun.getItem() instanceof G3Item item && item.variant() == G3Item.Variant.STANDARD) return 1;
        if (gun.is(ModItems.GUN_AMAT.get())) return 1;
        return 0;
    }

    public static boolean hasMod(ItemStack gun, int config, int id) {
        for (int installed : installedIds(gun, config)) if (installed == id) return true;
        return false;
    }

    public static int[] installedIds(ItemStack gun, int config) {
        CompoundTag tag = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getIntArray(MOD_LIST + config);
    }

    public static List<ItemStack> installedMods(ItemStack gun, int config) {
        List<ItemStack> result = new ArrayList<>();
        for (int id : installedIds(gun, config)) {
            if (id == SILENCER) result.add(new ItemStack(ModItems.WEAPON_MOD_SILENCER.get()));
        }
        return result;
    }

    public static void install(ItemStack gun, int config, List<ItemStack> candidates) {
        if (gun.isEmpty()) return;
        int[] ids = candidates.stream()
                .filter(stack -> isApplicable(gun, stack, config))
                .sorted(Comparator.comparingInt(WeaponModManager::priority).reversed())
                .mapToInt(WeaponModManager::id)
                .distinct()
                .toArray();
        CompoundTag tag = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (ids.length == 0) tag.remove(MOD_LIST + config);
        else tag.put(MOD_LIST + config, new IntArrayTag(ids));
        gun.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void uninstall(ItemStack gun, int config) {
        if (gun.isEmpty()) return;
        CompoundTag tag = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(MOD_LIST + config);
        gun.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static int id(ItemStack stack) {
        return stack.is(ModItems.WEAPON_MOD_SILENCER.get()) ? SILENCER : -1;
    }

    private static int priority(ItemStack stack) {
        return 500;
    }
}
