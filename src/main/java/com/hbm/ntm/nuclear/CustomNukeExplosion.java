package com.hbm.ntm.nuclear;

import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/** This entire class still sucks ass, now with seven flavors of float. */
public final class CustomNukeExplosion {
    // The fun police.
    public static final int MAX_TNT = 150;
    public static final int MAX_NUKE = 200;
    public static final int MAX_HYDRO = 350;
    public static final int MAX_AMAT = 350;
    public static final int MAX_SCHRAB = 250;
    public static final int MAX_DIRTY = 100;

    private CustomNukeExplosion() { }

    public enum BombType { TNT, NUKE, HYDRO, AMAT, DIRTY, SCHRAB, EUPH }

    public enum EntryType { ADD, MULT }

    public record Entry(BombType type, EntryType entry, float value) { }

    private static volatile Map<Item, Entry> entries;

    /** Build it late so missing ingredients do not explode class loading too. */
    public static Map<Item, Entry> entries() {
        Map<Item, Entry> map = entries;
        if (map == null) {
            synchronized (CustomNukeExplosion.class) {
                map = entries;
                if (map == null) {
                    map = buildEntries();
                    entries = map;
                }
            }
        }
        return map;
    }

    private static Map<Item, Entry> buildEntries() {
        Map<Item, Entry> map = new HashMap<>();

        // === TNT (ADD) ===
        add(map, Items.GUNPOWDER, BombType.TNT, 0.8F);
        add(map, Items.TNT, BombType.TNT, 4F);
        add(map, ModItems.CUSTOM_TNT.get(), BombType.TNT, 10F);
        // Not registered yet: det_cord, ingot_semtex, det_charge, red_barrel and pink_barrel.

        // === NUCLEAR (ADD) ===
        // Pu-241 is still at the store.
        add(map, item("ingot_u233"), BombType.NUKE, 15F);
        add(map, item("ingot_u235"), BombType.NUKE, 15F);
        add(map, item("ingot_pu239"), BombType.NUKE, 25F);
        add(map, item("ingot_neptunium"), BombType.NUKE, 30F);
        add(map, item("nugget_u233"), BombType.NUKE, 1.5F);
        add(map, item("nugget_u235"), BombType.NUKE, 1.5F);
        add(map, item("nugget_pu239"), BombType.NUKE, 2.5F);
        add(map, item("nugget_neptunium"), BombType.NUKE, 3F);
        add(map, item("powder_neptunium"), BombType.NUKE, 30F);
        add(map, ModItems.CUSTOM_NUKE.get(), BombType.NUKE, 30F);

        // === HYDROGEN (ADD) ===
        // cell_deuterium and custom_hydro are not registered yet.
        add(map, ModItems.CELL_TRITIUM.get(), BombType.HYDRO, 30F);
        add(map, item("lithium"), BombType.HYDRO, 20F);

        // === ANTIMATTER (ADD) === waiting on cell_antimatter, custom_amat and Balefire eggs.

        // === SALTED / DIRTY (ADD) ===
        add(map, item("ingot_tungsten"), BombType.DIRTY, 1F);
        // custom_dirty is not registered yet.

        // === SCHRABIDIUM (ADD) ===
        add(map, item("ingot_schrabidium"), BombType.SCHRAB, 5F);
        add(map, item("nugget_schrabidium"), BombType.SCHRAB, 0.5F);
        add(map, item("powder_schrabidium"), BombType.SCHRAB, 5F);
        add(map, ModItems.CELL_SAS3.get(), BombType.SCHRAB, 7.5F);
        add(map, ModItems.CUSTOM_SCHRAB.get(), BombType.SCHRAB, 15F);
        // block_schrabidium and cell_anti_schrabidium are not registered yet.

        // === EUPHEMIUM (ADD) === waiting on Euphemium ingots and nuggets.

        // === MULTIPLIERS (MULT) ===
        // Two redstone means x2.1. No, that is not how multiplication works. Yes, it stays.
        mult(map, Items.REDSTONE, BombType.TNT, 1.05F);
        mult(map, Items.REDSTONE_BLOCK, BombType.TNT, 1.5F);

        mult(map, item("ingot_uranium"), BombType.NUKE, 1.05F);
        mult(map, item("ingot_plutonium"), BombType.NUKE, 1.15F);
        mult(map, item("ingot_u238"), BombType.NUKE, 1.1F);
        mult(map, item("ingot_pu238"), BombType.NUKE, 1.15F);
        mult(map, item("nugget_uranium"), BombType.NUKE, 1.005F);
        mult(map, item("nugget_plutonium"), BombType.NUKE, 1.15F);
        mult(map, item("nugget_u238"), BombType.NUKE, 1.01F);
        mult(map, item("nugget_pu238"), BombType.NUKE, 1.015F);
        mult(map, item("powder_uranium"), BombType.NUKE, 1.05F);
        mult(map, item("powder_plutonium"), BombType.NUKE, 1.15F);
        mult(map, item("nuclear_waste"), BombType.DIRTY, 1.025F);
        mult(map, ModItems.BLOCK_WASTE_ITEM.get(), BombType.DIRTY, 1.25F);
        // ingot_pu240 and yellow_barrel are not registered yet.

        return map;
    }

    private static Item item(String id) {
        return ModItems.get(id).get();
    }

    private static void add(Map<Item, Entry> map, Item item, BombType type, float value) {
        map.put(item, new Entry(type, EntryType.ADD, value));
    }

    private static void mult(Map<Item, Entry> map, Item item, BombType type, float value) {
        map.put(item, new Entry(type, EntryType.MULT, value));
    }

    /** Seven floats enter, one crater leaves. */
    public record Yields(float tnt, float nuke, float hydro, float amat, float dirty, float schrab, float euph) {
        public float nukeAdj() {
            return nuke == 0 ? 0 : Math.min(nuke + tnt / 2, MAX_NUKE);
        }

        public float hydroAdj() {
            return hydro == 0 ? 0 : Math.min(hydro + nuke / 2 + tnt / 4, MAX_HYDRO);
        }

        public float amatAdj() {
            return amat == 0 ? 0 : Math.min(amat + hydro / 2 + nuke / 4 + tnt / 8, MAX_AMAT);
        }

        public float schrabAdj() {
            return schrab == 0 ? 0 : Math.min(schrab + amat / 2 + hydro / 4 + nuke / 8 + tnt / 16, MAX_SCHRAB);
        }
    }

    /** Recount every slot because caching a nuclear yield would be far too sensible. */
    public static Yields computeYields(Iterable<ItemStack> slots) {
        Map<Item, Entry> table = entries();

        float tnt = 0F, tntMod = 1F;
        float nuke = 0F, nukeMod = 1F;
        float hydro = 0F, hydroMod = 1F;
        float amat = 0F, amatMod = 1F;
        float dirty = 0F, dirtyMod = 1F;
        float schrab = 0F, schrabMod = 1F;
        float euph = 0F;

        for (ItemStack stack : slots) {
            if (stack == null || stack.isEmpty()) continue;
            Entry ent = table.get(stack.getItem());
            if (ent == null) continue;
            int count = stack.getCount();

            if (ent.entry() == EntryType.ADD) {
                switch (ent.type()) {
                    case TNT -> tnt += ent.value() * count;
                    case NUKE -> nuke += ent.value() * count;
                    case HYDRO -> hydro += ent.value() * count;
                    case AMAT -> amat += ent.value() * count;
                    case DIRTY -> dirty += ent.value() * count;
                    case SCHRAB -> schrab += ent.value() * count;
                    case EUPH -> euph += ent.value() * count;
                }
            } else if (ent.entry() == EntryType.MULT) {
                switch (ent.type()) {
                    case TNT -> tntMod *= ent.value() * count;
                    case NUKE -> nukeMod *= ent.value() * count;
                    case HYDRO -> hydroMod *= ent.value() * count;
                    case AMAT -> amatMod *= ent.value() * count;
                    case DIRTY -> dirtyMod *= ent.value() * count;
                    case SCHRAB -> schrabMod *= ent.value() * count;
                    case EUPH -> { /* Source MULT switch has no EUPH case. */ }
                }
            }
        }

        tnt *= tntMod;
        nuke *= nukeMod;
        hydro *= hydroMod;
        amat *= amatMod;
        dirty *= dirtyMod;
        schrab *= schrabMod;

        // Order matters. Feed the baby bomb before asking for the grown-up bomb.
        if (tnt < 16) nuke = 0;
        if (nuke < 100) hydro = 0;
        if (nuke < 50) amat = 0;
        if (nuke < 50) schrab = 0;
        if (schrab == 0) euph = 0;

        return new Yields(tnt, nuke, hydro, amat, dirty, schrab, euph);
    }

    /** Biggest number wins. Most branches add 0.5 twice because this class sucks ass. */
    public static void explodeCustom(ServerLevel level, double x, double y, double z,
                                     float tnt, float nuke, float hydro, float amat,
                                     float dirty, float schrab, float euph) {
        dirty = Math.min(dirty, MAX_DIRTY);

        /// EUPHEMIUM /// currently extinct
        if (euph > 0) {
            FleijaExplosionEntity ex = FleijaExplosionEntity.create(level, x, y, z, 150);
            level.addFreshEntity(ex);
            level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS,
                    100000.0F, 1.0F);
            FleijaRainbowCloudEntity cloud = FleijaRainbowCloudEntity.create(level, x, y, z, 50);
            level.addFreshEntity(cloud);

            /// SCHRABIDIUM ///
        } else if (schrab > 0) {
            schrab += amat / 2 + hydro / 4 + nuke / 8 + tnt / 16;
            schrab = Math.min(schrab, MAX_SCHRAB);

            FleijaExplosionEntity ex = FleijaExplosionEntity.create(level, x + 0.5, y + 0.5, z + 0.5, (int) schrab);
            level.addFreshEntity(ex);
            FleijaCloudEntity cloud = FleijaCloudEntity.create(level, x + 0.5, y + 0.5, z + 0.5, (int) schrab);
            level.addFreshEntity(cloud);

            /// ANTIMATTER /// please imagine the explosion
        } else if (amat > 0) {
            // TODO: Add the Balefire blast and cloud.

            /// HYDROGEN ///
        } else if (hydro > 0) {
            hydro += nuke / 2 + tnt / 4;
            hydro = Math.min(hydro, MAX_HYDRO);
            dirty *= 0.25F;

            NuclearExplosionEntity ex = NuclearExplosionEntity
                    .create(level, (int) hydro, x + 0.5, y + 0.5, z + 0.5).moreFallout((int) dirty);
            level.addFreshEntity(ex);
            spawnTorex(level, x + 0.5, y + 5, z + 0.5, hydro);

            /// NUCLEAR ///
        } else if (nuke > 0) {
            nuke += tnt / 2;
            nuke = Math.min(nuke, MAX_NUKE);

            NuclearExplosionEntity ex = NuclearExplosionEntity
                    .create(level, (int) nuke, x + 0.5, y + 5, z + 0.5).moreFallout((int) dirty);
            level.addFreshEntity(ex);
            spawnTorex(level, x + 0.5, y + 5, z + 0.5, nuke);

            /// NON-NUCLEAR (large) ///
        } else if (tnt >= 75) {
            tnt = Math.min(tnt, MAX_TNT);

            NuclearExplosionEntity ex = NuclearExplosionEntity
                    .createNoRad(level, (int) tnt, x + 0.5, y + 0.5, z + 0.5);
            level.addFreshEntity(ex);
            spawnTorex(level, x + 0.5, y + 5, z + 0.5, tnt);

            /// NON-NUCLEAR (small) ///
        } else if (tnt > 0) {
            // TODO smoke and debris, for now vanilla gets to pretend
            level.explode(null, x + 0.5, y + 0.5, z + 0.5, tnt, true, Level.ExplosionInteraction.TNT);
        }
    }

    /** Apply mushroom cloud directly to forehead. */
    private static void spawnTorex(ServerLevel level, double x, double y, double z, float scale) {
        MushroomCloudEntity cloud = new MushroomCloudEntity(ModEntities.MUSHROOM_CLOUD.get(), level);
        cloud.setPos(x, y, z);
        cloud.configure((int) scale);
        level.addFreshEntity(cloud);
    }
}
