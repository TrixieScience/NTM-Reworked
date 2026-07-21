package com.hbm.ntm.foundry;

import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.FoundryIngotItem;
import com.hbm.ntm.item.OreChunkItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.ShellItem;
import com.hbm.ntm.item.StoneResourceBlockItem;
import com.hbm.ntm.item.WireFineItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** Materials accepted by the Crucible and Foundry. */
public enum FoundryMaterial {
    WOOD("wood", 3, 0x896727, false, false),
    IVORY("ivory", 4, 0xEDEBCA, false, false),
    STONE("stone", 0, 0x4D2F23, false),
    IRON("iron", 2600, 0xFFA259, false),
    GOLD("gold", 7900, 0xE8D754, false),
    OBSIDIAN("obsidian", 2, 0x3D234D, false),
    TITANIUM("titanium", 2200, 0xA99E79, false),
    COPPER("copper", 2900, 0xC18336, false),
    ALUMINIUM("aluminium", 1300, 0xD0B8EB, false),
    LEAD("lead", 8200, 0x646470, false),
    TUNGSTEN("tungsten", 7400, 0x977474, false),
    COBALT("cobalt", 2700, 0x8F72AE, false),
    URANIUM("uranium", 9200, 0x9AA196, false),
    URANIUM_238("uranium_238", 9238, 0x9AA196, false),
    REACTOR_GRADE_PLUTONIUM("plutonium_rg", 9401, 0x9AA3A0, false),
    SCHRABIDIUM("schrabidium", 12626, 0x32FFFF, false),
    TECHNETIUM("technetium", 4399, 0xCADFDF, false),
    CADMIUM("cadmium", 4800, 0xA85600, false),
    BISMUTH("bismuth", 8300, 0xB200FF, false),
    ARSENIC("arsenic", 3300, 0x558080, false),
    STRONTIUM("strontium", 3800, 0xCAC193, false),
    CALCIUM("calcium", 2000, 0xB7B784, false),
    MUD("mud", 44, 0x96783B, false),
    CARBON("carbon", 699, 0x404040, true),
    REDSTONE("redstone", 331, 0xFF1000, true),
    FLUX("flux", 40, 0xDECCAD, true),
    HEMATITE("hematite", 2601, 0x6E463D, true),
    MALACHITE("malachite", 2901, 0x61AF87, true),
    RED_COPPER("red_copper", 31, 0xE44C0F, false),
    STEEL("steel", 30, 0x4A4A4A, false),
    DURA_STEEL("dura_steel", 33, 0x42665C, false),
    DESH("desh", 42, 0xF22929, false),
    FERROURANIUM("ferrouranium", 37, 0x6B6B8B, false),
    TECHNETIUM_STEEL("technetium_steel", 36, 0x9CA6A6, false),
    CADMIUM_STEEL("cadmium_steel", 43, 0xFBD368, false),
    BISMUTH_BRONZE("bismuth_bronze", 46, 0x987D65, false),
    ARSENIC_BRONZE("arsenic_bronze", 47, 0x77644D, false),
    BSCCO("bscco", 48, 0x5E62C0, false),
    MAGNETIZED_TUNGSTEN("magnetized_tungsten", 38, 0x22A2A2, false),
    COMBINE_STEEL("combine_steel", 39, 0x6F6FB4, false),
    GUNMETAL("gunmetal", 49, 0xF9C62C, false),
    WEAPON_STEEL("weapon_steel", 50, 0x808080, false),
    POLYMER("polymer", 20_001, 0x272727, false, false),
    RUBBER("rubber", 20_003, 0x4B4A3F, false, false),
    SLAG("slag", 41, 0x6C6562, false);

    public static final int NUGGET = 8;
    public static final int WIRE = 9;
    public static final int BILLET = 48;
    public static final int INGOT = 72;
    public static final int CAST_PLATE = 216;
    public static final int WELDED_PLATE = INGOT * 6;
    public static final int SHELL = 288;
    public static final int BLOCK = 648;

    private final String id;
    private final int legacyId;
    private final int moltenColor;
    private final boolean additive;
    private final boolean smeltable;

    FoundryMaterial(String id, int legacyId, int moltenColor, boolean additive) {
        this(id, legacyId, moltenColor, additive, true);
    }

    FoundryMaterial(String id, int legacyId, int moltenColor, boolean additive, boolean smeltable) {
        this.id = id;
        this.legacyId = legacyId;
        this.moltenColor = moltenColor;
        this.additive = additive;
        this.smeltable = smeltable;
    }

    public String id() { return id; }
    public int legacyId() { return legacyId; }
    public int moltenColor() { return moltenColor; }
    public boolean additive() { return additive; }
    public boolean smeltable() { return smeltable; }

    public static FoundryMaterial byId(String id) {
        for (FoundryMaterial material : values()) if (material.id.equals(id)) return material;
        return null;
    }

    public static FoundryMaterial byLegacyId(int legacyId) {
        for (FoundryMaterial material : values()) if (material.legacyId == legacyId) return material;
        return null;
    }

    public static MaterialAmount fromItem(ItemStack stack) {
        if (stack.isEmpty()) return null;
        MaterialAmount scraps = FoundryScrapsItem.contents(stack);
        if (scraps != null) return scraps;

        if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)) return new MaterialAmount(STONE, BLOCK);
        if (stack.is(Items.OBSIDIAN)) return new MaterialAmount(OBSIDIAN, BLOCK);

        if (stack.is(Items.GOLD_INGOT) || stack.is(Items.GOLD_NUGGET)
                || stack.is(ModItems.get("powder_gold").get()) || stack.is(ModItems.get("plate_gold").get())) {
            return new MaterialAmount(GOLD, stack.is(Items.GOLD_NUGGET) ? NUGGET : INGOT);
        }
        if (stack.is(Items.GOLD_BLOCK)) return new MaterialAmount(GOLD, BLOCK);

        if (stack.is(ModItems.get("ingot_titanium").get()) || stack.is(ModItems.get("powder_titanium").get())
                || stack.is(ModItems.get("plate_titanium").get())) return new MaterialAmount(TITANIUM, INGOT);
        if (stack.is(ModItems.getBlockItem("block_titanium").get())) return new MaterialAmount(TITANIUM, BLOCK);
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.TITANIUM) {
            return new MaterialAmount(TITANIUM, CAST_PLATE);
        }
        if (stack.is(ModItems.SHELL.get()) && ShellItem.isTitanium(stack)) {
            return new MaterialAmount(TITANIUM, SHELL);
        }

        if (stack.is(ModItems.get("ingot_aluminium").get()) || stack.is(ModItems.get("powder_aluminium").get())
                || stack.is(ModItems.get("plate_aluminium").get())) return new MaterialAmount(ALUMINIUM, INGOT);
        if (stack.is(ModItems.getBlockItem("block_aluminium").get())) return new MaterialAmount(ALUMINIUM, BLOCK);

        if (stack.is(Items.IRON_INGOT) || stack.is(ModItems.get("plate_iron").get())
                || stack.is(ModItems.get("powder_iron").get())) return new MaterialAmount(IRON, INGOT);
        if (stack.is(Items.IRON_NUGGET)) return new MaterialAmount(IRON, NUGGET);
        if (stack.is(Items.IRON_BLOCK)) return new MaterialAmount(IRON, BLOCK);
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.IRON) {
            return new MaterialAmount(IRON, CAST_PLATE);
        }

        if (stack.is(Items.COPPER_INGOT) || stack.is(ModItems.get("ingot_copper").get())
                || stack.is(ModItems.get("plate_copper").get())
                || stack.is(ModItems.get("powder_copper").get())) return new MaterialAmount(COPPER, INGOT);
        if (stack.is(Items.COPPER_BLOCK) || stack.is(ModItems.getBlockItem("block_copper").get())) {
            return new MaterialAmount(COPPER, BLOCK);
        }
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.COPPER) {
            return new MaterialAmount(COPPER, CAST_PLATE);
        }
        if (stack.is(ModItems.PIPE.get()) && PipeItem.isCopper(stack)) {
            return new MaterialAmount(COPPER, CAST_PLATE);
        }

        if (stack.is(ModItems.get("ingot_lead").get()) || stack.is(ModItems.get("plate_lead").get())) {
            return new MaterialAmount(LEAD, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_lead").get())) return new MaterialAmount(LEAD, BLOCK);
        if (stack.is(ModItems.PIPE.get()) && PipeItem.isLead(stack)) {
            return new MaterialAmount(LEAD, CAST_PLATE);
        }

        if (stack.is(ModItems.get("ingot_tungsten").get())
                || stack.is(ModItems.get("powder_tungsten").get())) {
            return new MaterialAmount(TUNGSTEN, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_tungsten").get())) {
            return new MaterialAmount(TUNGSTEN, BLOCK);
        }
        if (stack.is(ModItems.BOLT.get())
                && BoltItem.material(stack) == BoltItem.BoltMaterial.TUNGSTEN) {
            return new MaterialAmount(TUNGSTEN, 9);
        }
        if (stack.is(ModItems.WIRE_FINE.get())
                && WireFineItem.material(stack) == WireFineItem.WireMaterial.TUNGSTEN) {
            return new MaterialAmount(TUNGSTEN, 9);
        }

        if (stack.is(ModItems.get("fragment_cobalt").get())
                || stack.is(ModItems.get("powder_cobalt_tiny").get())) {
            return new MaterialAmount(COBALT, NUGGET);
        }
        if (stack.is(ModItems.get("ingot_cobalt").get())
                || stack.is(ModItems.get("powder_cobalt").get())) {
            return new MaterialAmount(COBALT, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_cobalt").get())) {
            return new MaterialAmount(COBALT, BLOCK);
        }

        if (stack.is(ModItems.get("ingot_uranium").get()) || stack.is(ModItems.get("powder_uranium").get())) {
            return new MaterialAmount(URANIUM, INGOT);
        }
        if (stack.is(ModItems.get("billet_uranium").get())) return new MaterialAmount(URANIUM, BILLET);
        if (stack.is(ModItems.get("nugget_uranium").get())) return new MaterialAmount(URANIUM, NUGGET);
        if (stack.is(ModItems.getBlockItem("block_uranium").get())) return new MaterialAmount(URANIUM, BLOCK);

        if (stack.is(ModItems.get("ingot_u238").get())) return new MaterialAmount(URANIUM_238, INGOT);
        if (stack.is(ModItems.get("billet_u238").get())) return new MaterialAmount(URANIUM_238, NUGGET * 6);
        if (stack.is(ModItems.get("nugget_u238").get())) return new MaterialAmount(URANIUM_238, NUGGET);
        if (stack.is(ModItems.getBlockItem("block_u238").get())) return new MaterialAmount(URANIUM_238, BLOCK);

        if (stack.is(ModItems.get("ingot_pu_mix").get())) {
            return new MaterialAmount(REACTOR_GRADE_PLUTONIUM, INGOT);
        }
        if (stack.is(ModItems.get("billet_pu_mix").get())) {
            return new MaterialAmount(REACTOR_GRADE_PLUTONIUM, BILLET);
        }
        if (stack.is(ModItems.get("nugget_pu_mix").get())) {
            return new MaterialAmount(REACTOR_GRADE_PLUTONIUM, NUGGET);
        }
        if (stack.is(ModItems.getBlockItem("block_pu_mix").get())) {
            return new MaterialAmount(REACTOR_GRADE_PLUTONIUM, BLOCK);
        }

        if (stack.is(ModItems.get("ingot_schrabidium").get())
                || stack.is(ModItems.get("powder_schrabidium").get())) {
            return new MaterialAmount(SCHRABIDIUM, INGOT);
        }
        if (stack.is(ModItems.get("billet_schrabidium").get())) return new MaterialAmount(SCHRABIDIUM, NUGGET * 6);
        if (stack.is(ModItems.get("nugget_schrabidium").get())) return new MaterialAmount(SCHRABIDIUM, NUGGET);
        if (stack.is(ModItems.getBlockItem("block_schrabidium").get())) return new MaterialAmount(SCHRABIDIUM, BLOCK);

        if (stack.is(ModItems.get("ingot_technetium").get())) return new MaterialAmount(TECHNETIUM, INGOT);
        if (stack.is(ModItems.get("billet_technetium").get())) return new MaterialAmount(TECHNETIUM, NUGGET * 6);
        if (stack.is(ModItems.get("nugget_technetium").get())) return new MaterialAmount(TECHNETIUM, NUGGET);

        if (stack.is(ModItems.get("ingot_cadmium").get())
                || stack.is(ModItems.get("powder_cadmium").get())) return new MaterialAmount(CADMIUM, INGOT);
        if (stack.is(ModItems.getBlockItem("block_cadmium").get())) return new MaterialAmount(CADMIUM, BLOCK);

        if (stack.is(ModItems.get("ingot_bismuth").get())
                || stack.is(ModItems.get("powder_bismuth").get())) return new MaterialAmount(BISMUTH, INGOT);
        if (stack.is(ModItems.getBlockItem("block_bismuth").get())) return new MaterialAmount(BISMUTH, BLOCK);

        if (stack.is(ModItems.get("ingot_arsenic").get())) return new MaterialAmount(ARSENIC, INGOT);
        if (stack.is(ModItems.get("powder_strontium").get())) return new MaterialAmount(STRONTIUM, INGOT);
        if (stack.is(ModItems.get("ingot_calcium").get())
                || stack.is(ModItems.get("powder_calcium").get())) return new MaterialAmount(CALCIUM, INGOT);
        if (stack.is(ModItems.get("ingot_mud").get())) return new MaterialAmount(MUD, INGOT);

        if (stack.is(Items.CHARCOAL)) return new MaterialAmount(CARBON, NUGGET * 3);
        if (stack.is(Items.COAL) || stack.is(ModItems.get("powder_coal").get())) {
            return new MaterialAmount(CARBON, INGOT);
        }
        if (stack.is(ModItems.get("ingot_graphite").get())) return new MaterialAmount(CARBON, INGOT);
        if (stack.is(Items.REDSTONE)) return new MaterialAmount(REDSTONE, NUGGET);
        if (stack.is(Items.REDSTONE_BLOCK)) return new MaterialAmount(REDSTONE, INGOT);
        if (stack.is(ModItems.POWDER_FLUX.get())) return new MaterialAmount(FLUX, INGOT);
        if (stack.is(ModItems.STONE_RESOURCE_ITEM.get())) {
            return switch (StoneResourceBlockItem.type(stack)) {
                case HEMATITE -> new MaterialAmount(HEMATITE, INGOT);
                case MALACHITE -> new MaterialAmount(MALACHITE, INGOT * 6);
                default -> null;
            };
        }
        if (stack.is(ModItems.CHUNK_ORE.get())
                && OreChunkItem.type(stack) == OreChunkItem.ChunkType.MALACHITE) {
            return new MaterialAmount(MALACHITE, INGOT);
        }

        if (stack.is(ModItems.get("ingot_red_copper").get())
                || stack.is(ModItems.get("powder_red_copper").get())) {
            return new MaterialAmount(RED_COPPER, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_red_copper").get())) {
            return new MaterialAmount(RED_COPPER, BLOCK);
        }

        if (stack.is(ModItems.get("ingot_steel").get()) || stack.is(ModItems.get("plate_steel").get())
                || stack.is(ModItems.get("powder_steel").get())) return new MaterialAmount(STEEL, INGOT);
        if (stack.is(ModItems.getBlockItem("block_steel").get())) return new MaterialAmount(STEEL, BLOCK);
        if (stack.is(ModItems.BOLT.get()) && BoltItem.material(stack) == BoltItem.BoltMaterial.STEEL) {
            return new MaterialAmount(STEEL, 9);
        }
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.STEEL) {
            return new MaterialAmount(STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.PLATE_WELDED.get()) && WeldedPlateItem.isSteel(stack)) {
            return new MaterialAmount(STEEL, WELDED_PLATE);
        }
        if (stack.is(ModItems.PIPE.get()) && PipeItem.isSteel(stack)) {
            return new MaterialAmount(STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.SHELL.get()) && ShellItem.isSteel(stack)) {
            return new MaterialAmount(STEEL, SHELL);
        }

        if (stack.is(ModItems.get("ingot_dura_steel").get())
                || stack.is(ModItems.get("plate_dura_steel").get())
                || stack.is(ModItems.get("powder_dura_steel").get())) {
            return new MaterialAmount(DURA_STEEL, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_dura_steel").get())) {
            return new MaterialAmount(DURA_STEEL, BLOCK);
        }
        if (stack.is(ModItems.PIPE.get()) && PipeItem.isDuraSteel(stack)) {
            return new MaterialAmount(DURA_STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.DURA_STEEL) {
            return new MaterialAmount(DURA_STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.get("ingot_desh").get())
                || stack.is(ModItems.get("powder_desh").get())) return new MaterialAmount(DESH, INGOT);
        if (stack.is(ModItems.getBlockItem("block_desh").get())) return new MaterialAmount(DESH, BLOCK);
        if (stack.is(ModItems.get("ingot_ferrouranium").get())) return new MaterialAmount(FERROURANIUM, INGOT);
        if (stack.is(ModItems.get("ingot_tcalloy").get())
                || stack.is(ModItems.get("powder_tcalloy").get())) {
            return new MaterialAmount(TECHNETIUM_STEEL, INGOT);
        }
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.TECHNETIUM_STEEL) {
            return new MaterialAmount(TECHNETIUM_STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.PLATE_WELDED.get())
                && WeldedPlateItem.material(stack) == WeldedPlateItem.WeldedPlateMaterial.TECHNETIUM_STEEL) {
            return new MaterialAmount(TECHNETIUM_STEEL, WELDED_PLATE);
        }
        if (stack.is(ModItems.getBlockItem("block_tcalloy").get())) {
            return new MaterialAmount(TECHNETIUM_STEEL, BLOCK);
        }
        if (stack.is(ModItems.get("ingot_cdalloy").get())) return new MaterialAmount(CADMIUM_STEEL, INGOT);
        if (stack.is(ModItems.PLATE_CAST.get())
                && CastPlateItem.material(stack) == CastPlateItem.CastPlateMaterial.CADMIUM_STEEL) {
            return new MaterialAmount(CADMIUM_STEEL, CAST_PLATE);
        }
        if (stack.is(ModItems.PLATE_WELDED.get())
                && WeldedPlateItem.material(stack) == WeldedPlateItem.WeldedPlateMaterial.CADMIUM_STEEL) {
            return new MaterialAmount(CADMIUM_STEEL, WELDED_PLATE);
        }
        if (stack.is(ModItems.getBlockItem("block_cdalloy").get())) {
            return new MaterialAmount(CADMIUM_STEEL, BLOCK);
        }
        if (stack.is(ModItems.get("ingot_bismuth_bronze").get())) {
            return new MaterialAmount(BISMUTH_BRONZE, INGOT);
        }
        if (stack.is(ModItems.get("ingot_arsenic_bronze").get())) {
            return new MaterialAmount(ARSENIC_BRONZE, INGOT);
        }
        if (stack.is(ModItems.get("ingot_bscco").get())) return new MaterialAmount(BSCCO, INGOT);
        if (stack.is(ModItems.get("ingot_magnetized_tungsten").get())
                || stack.is(ModItems.get("powder_magnetized_tungsten").get())) {
            return new MaterialAmount(MAGNETIZED_TUNGSTEN, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_magnetized_tungsten").get())) {
            return new MaterialAmount(MAGNETIZED_TUNGSTEN, BLOCK);
        }
        if (stack.is(ModItems.get("ingot_combine_steel").get())
                || stack.is(ModItems.get("powder_combine_steel").get())) {
            return new MaterialAmount(COMBINE_STEEL, INGOT);
        }
        if (stack.is(ModItems.getBlockItem("block_combine_steel").get())) {
            return new MaterialAmount(COMBINE_STEEL, BLOCK);
        }
        if (stack.is(ModItems.get("ingot_gunmetal").get()) || stack.is(ModItems.get("plate_gunmetal").get())) {
            return new MaterialAmount(GUNMETAL, INGOT);
        }
        if (stack.is(ModItems.get("ingot_weaponsteel").get()) || stack.is(ModItems.get("plate_weaponsteel").get())) {
            return new MaterialAmount(WEAPON_STEEL, INGOT);
        }

        FoundryMaterial denseWire = DenseWireItem.material(stack);
        if (denseWire != null) return new MaterialAmount(denseWire, INGOT);
        CasingItem.CasingType casing = CasingItem.type(stack);
        if (casing != null) return new MaterialAmount(casing.material(), casing.cost());
        FoundryMaterial partMaterial = FoundryPartItem.material(stack);
        if (partMaterial != null && partMaterial.smeltable() && stack.getItem() instanceof FoundryPartItem part) {
            return new MaterialAmount(partMaterial, part.type().cost());
        }

        if (stack.is(ModItems.BLADE_TITANIUM.get())) return new MaterialAmount(TITANIUM, INGOT * 3);
        if (stack.is(ModItems.BLADE_TUNGSTEN.get())) return new MaterialAmount(TUNGSTEN, INGOT * 3);
        if (stack.is(ModItems.BLADES_STEEL.get())) return new MaterialAmount(STEEL, INGOT * 4);
        if (stack.is(ModItems.BLADES_TITANIUM.get())) return new MaterialAmount(TITANIUM, INGOT * 4);
        // The old distribution table deliberately recovers three ingots from a four-ingot flat stamp.
        if (stack.is(ModItems.STAMPS.get("stamp_stone_flat").get())) return new MaterialAmount(STONE, INGOT * 3);
        if (stack.is(ModItems.STAMPS.get("stamp_iron_flat").get())) return new MaterialAmount(IRON, INGOT * 3);
        if (stack.is(ModItems.STAMPS.get("stamp_steel_flat").get())) return new MaterialAmount(STEEL, INGOT * 3);
        if (stack.is(ModItems.STAMPS.get("stamp_titanium_flat").get())) return new MaterialAmount(TITANIUM, INGOT * 3);
        if (stack.is(ModItems.STAMPS.get("stamp_obsidian_flat").get())) return new MaterialAmount(OBSIDIAN, INGOT * 3);
        FoundryMaterial rawIngot = FoundryIngotItem.material(stack);
        if (rawIngot != null) return new MaterialAmount(rawIngot, INGOT);
        return null;
    }

    public ItemStack output(FoundryMoldItem.Mold mold) {
        return switch (mold) {
            case NUGGET -> nugget();
            case BILLET -> billet();
            case INGOT -> ingot();
            case PLATE -> plate();
            case WIRES -> fineWire(8);
            case CAST_PLATE -> castPlate();
            case DENSE_WIRE -> denseWire(1);
            case BLADE -> blade();
            case BLADES -> blades();
            case STAMP -> stamp();
            case SHELL -> shell();
            case PIPE -> pipe();
            case INGOTS -> withCount(ingot(), 9);
            case PLATES -> withCount(plate(), 9);
            case CAST_PLATES -> withCount(castPlate(), 3);
            case DENSE_WIRES -> denseWire(9);
            case BLOCK -> block();
            case SMALL_CASING -> casing(false);
            case LARGE_CASING -> casing(true);
            case LIGHT_BARREL -> part(FoundryPartItem.PartType.LIGHT_BARREL);
            case HEAVY_BARREL -> part(FoundryPartItem.PartType.HEAVY_BARREL);
            case LIGHT_RECEIVER -> part(FoundryPartItem.PartType.LIGHT_RECEIVER);
            case HEAVY_RECEIVER -> part(FoundryPartItem.PartType.HEAVY_RECEIVER);
            case MECHANISM -> part(FoundryPartItem.PartType.MECHANISM);
            case STOCK -> part(FoundryPartItem.PartType.STOCK);
            case GRIP -> part(FoundryPartItem.PartType.GRIP);
        };
    }

    public ItemStack nugget() {
        return switch (this) {
            case IRON -> new ItemStack(Items.IRON_NUGGET);
            case GOLD -> new ItemStack(Items.GOLD_NUGGET);
            case URANIUM -> new ItemStack(ModItems.get("nugget_uranium").get());
            case URANIUM_238 -> new ItemStack(ModItems.get("nugget_u238").get());
            case REACTOR_GRADE_PLUTONIUM -> new ItemStack(ModItems.get("nugget_pu_mix").get());
            case SCHRABIDIUM -> new ItemStack(ModItems.get("nugget_schrabidium").get());
            case TECHNETIUM -> new ItemStack(ModItems.get("nugget_technetium").get());
            default -> ItemStack.EMPTY;
        };
    }

    public ItemStack billet() {
        return switch (this) {
            case URANIUM -> new ItemStack(ModItems.get("billet_uranium").get());
            case URANIUM_238 -> new ItemStack(ModItems.get("billet_u238").get());
            case REACTOR_GRADE_PLUTONIUM -> new ItemStack(ModItems.get("billet_pu_mix").get());
            case SCHRABIDIUM -> new ItemStack(ModItems.get("billet_schrabidium").get());
            case TECHNETIUM -> new ItemStack(ModItems.get("billet_technetium").get());
            default -> ItemStack.EMPTY;
        };
    }

    public ItemStack plate() {
        return switch (this) {
            case IRON -> new ItemStack(ModItems.get("plate_iron").get());
            case GOLD -> new ItemStack(ModItems.get("plate_gold").get());
            case TITANIUM -> new ItemStack(ModItems.get("plate_titanium").get());
            case COPPER -> new ItemStack(ModItems.get("plate_copper").get());
            case ALUMINIUM -> new ItemStack(ModItems.get("plate_aluminium").get());
            case LEAD -> new ItemStack(ModItems.get("plate_lead").get());
            case STEEL -> new ItemStack(ModItems.get("plate_steel").get());
            case DURA_STEEL -> new ItemStack(ModItems.get("plate_dura_steel").get());
            case GUNMETAL -> new ItemStack(ModItems.get("plate_gunmetal").get());
            case WEAPON_STEEL -> new ItemStack(ModItems.get("plate_weaponsteel").get());
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack fineWire(int count) {
        WireFineItem.WireMaterial wire = switch (this) {
            case CARBON -> WireFineItem.WireMaterial.CARBON;
            case GOLD -> WireFineItem.WireMaterial.GOLD;
            case COPPER -> WireFineItem.WireMaterial.COPPER;
            case TUNGSTEN -> WireFineItem.WireMaterial.TUNGSTEN;
            case ALUMINIUM -> WireFineItem.WireMaterial.ALUMINIUM;
            case LEAD -> WireFineItem.WireMaterial.LEAD;
            case STEEL -> WireFineItem.WireMaterial.STEEL;
            case RED_COPPER -> WireFineItem.WireMaterial.RED_COPPER;
            default -> null;
        };
        return wire == null ? ItemStack.EMPTY : WireFineItem.create(ModItems.WIRE_FINE.get(), wire, count);
    }

    private ItemStack denseWire(int count) {
        boolean supported = switch (this) {
            case GOLD, TITANIUM, COPPER, TUNGSTEN, SCHRABIDIUM, RED_COPPER,
                    BSCCO, MAGNETIZED_TUNGSTEN -> true;
            default -> false;
        };
        return supported ? DenseWireItem.create(ModItems.WIRE_DENSE.get(), this, count) : ItemStack.EMPTY;
    }

    private ItemStack blade() {
        return switch (this) {
            case TITANIUM -> new ItemStack(ModItems.BLADE_TITANIUM.get());
            case TUNGSTEN -> new ItemStack(ModItems.BLADE_TUNGSTEN.get());
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack blades() {
        return switch (this) {
            case STEEL -> new ItemStack(ModItems.BLADES_STEEL.get());
            case TITANIUM -> new ItemStack(ModItems.BLADES_TITANIUM.get());
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack stamp() {
        return switch (this) {
            case STONE -> new ItemStack(ModItems.STAMPS.get("stamp_stone_flat").get());
            case IRON -> new ItemStack(ModItems.STAMPS.get("stamp_iron_flat").get());
            case STEEL -> new ItemStack(ModItems.STAMPS.get("stamp_steel_flat").get());
            case TITANIUM -> new ItemStack(ModItems.STAMPS.get("stamp_titanium_flat").get());
            case OBSIDIAN -> new ItemStack(ModItems.STAMPS.get("stamp_obsidian_flat").get());
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack shell() {
        return switch (this) {
            case STEEL -> ShellItem.steel(ModItems.SHELL.get(), 1);
            case TITANIUM -> ShellItem.titanium(ModItems.SHELL.get(), 1);
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack pipe() {
        return switch (this) {
            case COPPER -> PipeItem.copper(ModItems.PIPE.get(), 1);
            case STEEL -> PipeItem.steel(ModItems.PIPE.get(), 1);
            case DURA_STEEL -> PipeItem.duraSteel(ModItems.PIPE.get(), 1);
            case LEAD -> PipeItem.lead(ModItems.PIPE.get(), 1);
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack block() {
        return switch (this) {
            case STONE -> new ItemStack(Items.STONE);
            case OBSIDIAN -> new ItemStack(Items.OBSIDIAN);
            case IRON -> new ItemStack(Items.IRON_BLOCK);
            case GOLD -> new ItemStack(Items.GOLD_BLOCK);
            case COPPER -> new ItemStack(ModItems.getBlockItem("block_copper").get());
            case TITANIUM -> new ItemStack(ModItems.getBlockItem("block_titanium").get());
            case ALUMINIUM -> new ItemStack(ModItems.getBlockItem("block_aluminium").get());
            case LEAD -> new ItemStack(ModItems.getBlockItem("block_lead").get());
            case TUNGSTEN -> new ItemStack(ModItems.getBlockItem("block_tungsten").get());
            case COBALT -> new ItemStack(ModItems.getBlockItem("block_cobalt").get());
            case URANIUM -> new ItemStack(ModItems.getBlockItem("block_uranium").get());
            case URANIUM_238 -> new ItemStack(ModItems.getBlockItem("block_u238").get());
            case REACTOR_GRADE_PLUTONIUM -> new ItemStack(ModItems.getBlockItem("block_pu_mix").get());
            case SCHRABIDIUM -> new ItemStack(ModItems.getBlockItem("block_schrabidium").get());
            case CADMIUM -> new ItemStack(ModItems.getBlockItem("block_cadmium").get());
            case BISMUTH -> new ItemStack(ModItems.getBlockItem("block_bismuth").get());
            case RED_COPPER -> new ItemStack(ModItems.getBlockItem("block_red_copper").get());
            case STEEL -> new ItemStack(ModItems.getBlockItem("block_steel").get());
            case DESH -> new ItemStack(ModItems.getBlockItem("block_desh").get());
            case DURA_STEEL -> new ItemStack(ModItems.getBlockItem("block_dura_steel").get());
            case TECHNETIUM_STEEL -> new ItemStack(ModItems.getBlockItem("block_tcalloy").get());
            case CADMIUM_STEEL -> new ItemStack(ModItems.getBlockItem("block_cdalloy").get());
            case MAGNETIZED_TUNGSTEN -> new ItemStack(ModItems.getBlockItem("block_magnetized_tungsten").get());
            case COMBINE_STEEL -> new ItemStack(ModItems.getBlockItem("block_combine_steel").get());
            default -> ItemStack.EMPTY;
        };
    }

    private ItemStack casing(boolean large) {
        CasingItem.CasingType type = switch (this) {
            case GUNMETAL -> large ? CasingItem.CasingType.LARGE : CasingItem.CasingType.SMALL;
            case WEAPON_STEEL -> large ? CasingItem.CasingType.LARGE_STEEL : CasingItem.CasingType.SMALL_STEEL;
            default -> null;
        };
        return type == null ? ItemStack.EMPTY : CasingItem.create(ModItems.CASING.get(), type, 1);
    }

    private ItemStack part(FoundryPartItem.PartType type) {
        if (!supports(type)) return ItemStack.EMPTY;
        return FoundryPartItem.create(ModItems.FOUNDRY_PARTS.get(type).get(), this, 1);
    }

    private boolean supports(FoundryPartItem.PartType type) {
        return switch (type) {
            case LIGHT_BARREL -> switch (this) {
                case STEEL, DURA_STEEL, DESH, TECHNETIUM_STEEL, CADMIUM_STEEL,
                        BISMUTH_BRONZE, ARSENIC_BRONZE, GUNMETAL, WEAPON_STEEL -> true;
                default -> false;
            };
            case HEAVY_BARREL -> switch (this) {
                case STEEL, DURA_STEEL, DESH, FERROURANIUM, TECHNETIUM_STEEL, CADMIUM_STEEL,
                        GUNMETAL, WEAPON_STEEL -> true;
                default -> false;
            };
            case LIGHT_RECEIVER -> switch (this) {
                case STEEL, DURA_STEEL, DESH, TECHNETIUM_STEEL, CADMIUM_STEEL, BISMUTH_BRONZE,
                        ARSENIC_BRONZE, GUNMETAL, WEAPON_STEEL -> true;
                default -> false;
            };
            case HEAVY_RECEIVER -> switch (this) {
                case DURA_STEEL, FERROURANIUM, TECHNETIUM_STEEL, CADMIUM_STEEL,
                        BISMUTH_BRONZE, ARSENIC_BRONZE, GUNMETAL, WEAPON_STEEL -> true;
                default -> false;
            };
            case MECHANISM -> this == GUNMETAL || this == WEAPON_STEEL;
            case STOCK -> this == WOOD || this == DESH || this == GUNMETAL
                    || this == WEAPON_STEEL || this == POLYMER;
            case GRIP -> this == STEEL || this == DURA_STEEL || this == DESH
                    || this == GUNMETAL || this == WEAPON_STEEL || this == WOOD
                    || this == IVORY || this == POLYMER || this == RUBBER;
        };
    }

    private static ItemStack withCount(ItemStack stack, int count) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(count);
    }

    public ItemStack castPlate() {
        return switch (this) {
            case IRON -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.IRON, 1);
            case TITANIUM -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.TITANIUM, 1);
            case COPPER -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.COPPER, 1);
            case LEAD -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.LEAD, 1);
            case STEEL -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.STEEL, 1);
            case DURA_STEEL -> CastPlateItem.create(ModItems.PLATE_CAST.get(), CastPlateItem.CastPlateMaterial.DURA_STEEL, 1);
            case TECHNETIUM_STEEL -> CastPlateItem.create(ModItems.PLATE_CAST.get(),
                    CastPlateItem.CastPlateMaterial.TECHNETIUM_STEEL, 1);
            case CADMIUM_STEEL -> CastPlateItem.create(ModItems.PLATE_CAST.get(),
                    CastPlateItem.CastPlateMaterial.CADMIUM_STEEL, 1);
            default -> ItemStack.EMPTY;
        };
    }

    public ItemStack ingot() {
        return switch (this) {
            case IRON -> new ItemStack(Items.IRON_INGOT);
            case GOLD -> new ItemStack(Items.GOLD_INGOT);
            case TITANIUM -> new ItemStack(ModItems.get("ingot_titanium").get());
            case COPPER -> new ItemStack(ModItems.get("ingot_copper").get());
            case ALUMINIUM -> new ItemStack(ModItems.get("ingot_aluminium").get());
            case LEAD -> new ItemStack(ModItems.get("ingot_lead").get());
            case TUNGSTEN -> new ItemStack(ModItems.get("ingot_tungsten").get());
            case COBALT -> new ItemStack(ModItems.get("ingot_cobalt").get());
            case URANIUM -> new ItemStack(ModItems.get("ingot_uranium").get());
            case URANIUM_238 -> new ItemStack(ModItems.get("ingot_u238").get());
            case REACTOR_GRADE_PLUTONIUM -> new ItemStack(ModItems.get("ingot_pu_mix").get());
            case SCHRABIDIUM -> new ItemStack(ModItems.get("ingot_schrabidium").get());
            case TECHNETIUM -> new ItemStack(ModItems.get("ingot_technetium").get());
            case CADMIUM -> new ItemStack(ModItems.get("ingot_cadmium").get());
            case BISMUTH -> new ItemStack(ModItems.get("ingot_bismuth").get());
            case ARSENIC -> new ItemStack(ModItems.get("ingot_arsenic").get());
            case CALCIUM -> new ItemStack(ModItems.get("ingot_calcium").get());
            case MUD -> new ItemStack(ModItems.get("ingot_mud").get());
            case CARBON -> new ItemStack(ModItems.get("ingot_graphite").get());
            case RED_COPPER -> new ItemStack(ModItems.get("ingot_red_copper").get());
            case STEEL -> new ItemStack(ModItems.get("ingot_steel").get());
            case DURA_STEEL -> new ItemStack(ModItems.get("ingot_dura_steel").get());
            case DESH -> new ItemStack(ModItems.get("ingot_desh").get());
            case FERROURANIUM -> new ItemStack(ModItems.get("ingot_ferrouranium").get());
            case TECHNETIUM_STEEL -> new ItemStack(ModItems.get("ingot_tcalloy").get());
            case CADMIUM_STEEL -> new ItemStack(ModItems.get("ingot_cdalloy").get());
            case BISMUTH_BRONZE -> new ItemStack(ModItems.get("ingot_bismuth_bronze").get());
            case ARSENIC_BRONZE -> new ItemStack(ModItems.get("ingot_arsenic_bronze").get());
            case BSCCO -> new ItemStack(ModItems.get("ingot_bscco").get());
            case MAGNETIZED_TUNGSTEN -> new ItemStack(ModItems.get("ingot_magnetized_tungsten").get());
            case COMBINE_STEEL -> new ItemStack(ModItems.get("ingot_combine_steel").get());
            case GUNMETAL -> new ItemStack(ModItems.get("ingot_gunmetal").get());
            case WEAPON_STEEL -> new ItemStack(ModItems.get("ingot_weaponsteel").get());
            case SLAG -> FoundryIngotItem.create(ModItems.INGOT_RAW.get(), SLAG, 1);
            default -> ItemStack.EMPTY;
        };
    }

    public static List<FoundryMaterial> castPlateMaterials() {
        return List.of(IRON, TITANIUM, COPPER, STEEL, DURA_STEEL, TECHNETIUM_STEEL, CADMIUM_STEEL);
    }

    public record MaterialAmount(FoundryMaterial material, int amount) { }
}
