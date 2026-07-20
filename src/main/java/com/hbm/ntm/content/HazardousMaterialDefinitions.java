package com.hbm.ntm.content;

import com.hbm.ntm.hazard.HazardProfile;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Materials that deserve a warning label and frequently ignore it. */
public final class HazardousMaterialDefinitions {
    public enum Form {
        INGOT("ingots"),
        BILLET("billets"),
        NUGGET("nuggets"),
        DUST("dusts"),
        TINY_DUST(null),
        MISC(null);

        private final String tagFolder;

        Form(String tagFolder) {
            this.tagFolder = tagFolder;
        }

        public String tagFolder() {
            return tagFolder;
        }
    }

    public enum CreativeGroup {
        PARTS,
        WEAPONS
    }

    public record ItemDefinition(
            String id,
            String englishName,
            String texture,
            Form form,
            String commonMaterial,
            HazardProfile hazards,
            boolean mineralSet,
            CreativeGroup creativeGroup
    ) {
    }

    public record BlockDefinition(
            String id,
            String englishName,
            String commonMaterial,
            String compressedItemId,
            HazardProfile hazards,
            boolean radiationFog,
            float hardness,
            float legacyResistance,
            MapColor mapColor,
            SoundType sound,
            float adjacentWaterExplosion,
            int lightLevel
    ) {
        public float modernExplosionResistance() {
            return legacyResistance * 0.6F;
        }

        public float placedEmission() {
            return hazards.radiation() * 0.1F;
        }
    }

    /** Full catalog, including elements still waiting backstage. */
    public static final List<ItemDefinition> CATALOG_ITEMS;
    public static final List<BlockDefinition> CATALOG_BLOCKS;

    /** Hazards currently available to the public. */
    public static final List<ItemDefinition> ITEMS;
    public static final List<BlockDefinition> BLOCKS;

    static {
        List<ItemDefinition> items = new ArrayList<>();

        // Radioactive ingots. U-238M2 remains in witness protection.
        addMineralSet(items, "uranium", "Uranium", "ingot_uranium", "billet_uranium", "nugget_uranium",
                HazardProfile.NONE.withRadiation(0.35F));
        addMineralSet(items, "uranium_233", "Uranium-233", "ingot_u233", "billet_u233", "nugget_u233",
                HazardProfile.NONE.withRadiation(5.0F));
        addMineralSet(items, "uranium_235", "Uranium-235", "ingot_u235", "billet_u235", "nugget_u235",
                HazardProfile.NONE.withRadiation(1.0F));
        addMineralSet(items, "uranium_238", "Uranium-238", "ingot_u238", "billet_u238", "nugget_u238",
                HazardProfile.NONE.withRadiation(0.25F));
        addMineralSet(items, "thorium_232", "Thorium-232", "ingot_th232", "billet_th232", "nugget_th232",
                HazardProfile.NONE.withRadiation(0.1F));
        addMineralSet(items, "plutonium", "Plutonium", "ingot_plutonium", "billet_plutonium", "nugget_plutonium",
                HazardProfile.NONE.withRadiation(7.5F));
        addMineralSet(items, "plutonium_238", "Plutonium-238", "ingot_pu238", "billet_pu238", "nugget_pu238",
                HazardProfile.NONE.withRadiation(10.0F).withHeat(3.0F));
        addMineralSet(items, "plutonium_239", "Plutonium-239", "ingot_pu239", "billet_pu239", "nugget_pu239",
                HazardProfile.NONE.withRadiation(5.0F));
        addMineralSet(items, "plutonium_240", "Plutonium-240", "ingot_pu240", "billet_pu240", "nugget_pu240",
                HazardProfile.NONE.withRadiation(7.5F));
        addMineralSet(items, "plutonium_241", "Plutonium-241", "ingot_pu241", "billet_pu241", "nugget_pu241",
                HazardProfile.NONE.withRadiation(25.0F));
        addMineralSet(items, "plutonium_rg", "Reactor Grade Plutonium", "ingot_pu_mix", "billet_pu_mix", "nugget_pu_mix",
                HazardProfile.NONE.withRadiation(6.25F));
        items.add(new ItemDefinition("nuclear_waste", "Nuclear Waste", "nuclear_waste", Form.INGOT,
                "nuclear_waste", HazardProfile.NONE.withRadiation(15.0F), true, CreativeGroup.PARTS));
        items.add(new ItemDefinition("billet_nuclear_waste", "Nuclear Waste Billet", "billet_nuclear_waste",
                Form.BILLET, "nuclear_waste", HazardProfile.NONE.withRadiation(7.5F), true,
                CreativeGroup.PARTS));
        items.add(new ItemDefinition("nuclear_waste_tiny", "Tiny Pile of Nuclear Waste", "nuclear_waste_tiny",
                Form.NUGGET, "nuclear_waste", HazardProfile.NONE.withRadiation(1.5F), true,
                CreativeGroup.PARTS));
        addMineralSet(items, "americium_241", "Americium-241", "ingot_am241", "billet_am241", "nugget_am241",
                HazardProfile.NONE.withRadiation(8.5F));
        addMineralSet(items, "americium_242", "Americium-242", "ingot_am242", "billet_am242", "nugget_am242",
                HazardProfile.NONE.withRadiation(9.5F));
        addMineralSet(items, "americium_rg", "Reactor Grade Americium", "ingot_am_mix", "billet_am_mix", "nugget_am_mix",
                HazardProfile.NONE.withRadiation(9.0F));
        addMineralSet(items, "neptunium_237", "Neptunium", "ingot_neptunium", "billet_neptunium", "nugget_neptunium",
                HazardProfile.NONE.withRadiation(2.5F));
        addMineralSet(items, "polonium_210", "Polonium-210", "ingot_polonium", "billet_polonium", "nugget_polonium",
                HazardProfile.NONE.withRadiation(75.0F).withHeat(3.0F));
        addMineralSet(items, "technetium_99", "Technetium-99", "ingot_technetium", "billet_technetium", "nugget_technetium",
                HazardProfile.NONE.withRadiation(2.75F));
        addMineralSet(items, "schrabidium", "Schrabidium", "ingot_schrabidium", "billet_schrabidium", "nugget_schrabidium",
                HazardProfile.NONE.withRadiation(15.0F).withBlinding(50.0F));
        addMineralSet(items, "cobalt_60", "Cobalt-60", "ingot_co60", "billet_co60", "nugget_co60",
                HazardProfile.NONE.withRadiation(30.0F).withHeat(1.0F));
        addMineralSet(items, "strontium_90", "Strontium-90", "ingot_sr90", "billet_sr90", "nugget_sr90",
                HazardProfile.NONE.withRadiation(15.0F).withHeat(1.0F).withHydroactive(1.0F));
        addMineralSet(items, "gold_198", "Gold-198", "ingot_au198", "billet_au198", "nugget_au198",
                HazardProfile.NONE.withRadiation(500.0F).withHeat(5.0F));
        addMineralSet(items, "lead_209", "Lead-209", "ingot_pb209", "billet_pb209", "nugget_pb209",
                HazardProfile.NONE.withRadiation(10_000.0F).withHeat(7.0F).withBlinding(50.0F));
        addMineralSet(items, "radium_226", "Radium-226", "ingot_ra226", "billet_ra226", "nugget_ra226",
                HazardProfile.NONE.withRadiation(7.5F));
        addMineralSet(items, "actinium_227", "Actinium-227", "ingot_actinium", "billet_actinium", "nugget_actinium",
                HazardProfile.NONE.withRadiation(30.0F));

        // Fuel forms without the matching family photo.
        add(items, "ingot_mox_fuel", "Ingot of MOX Fuel", Form.INGOT, "mox_fuel",
                HazardProfile.NONE.withRadiation(2.5F));
        add(items, "billet_ra226be", "Ra226Be Billet", Form.BILLET, null,
                HazardProfile.NONE.withRadiation(11.25F));
        add(items, "billet_pu238be", "Pu238Be Billet", Form.BILLET, null,
                HazardProfile.NONE.withRadiation(15.0F));

        // Powder radiation gets multiplied by three, inhalation being efficient.
        add(items, "powder_uranium", "Uranium Powder", Form.DUST, "uranium",
                HazardProfile.NONE.withRadiation(0.35F * 3));
        add(items, "powder_thorium", "Thorium Powder", Form.DUST, "thorium_232",
                HazardProfile.NONE.withRadiation(0.1F * 3));
        add(items, "powder_plutonium", "Plutonium Powder", Form.DUST, "plutonium",
                HazardProfile.NONE.withRadiation(7.5F * 3));
        add(items, "powder_neptunium", "Neptunium Powder", Form.DUST, "neptunium_237",
                HazardProfile.NONE.withRadiation(2.5F * 3));
        add(items, "powder_polonium", "Polonium-210 Powder", Form.DUST, "polonium_210",
                HazardProfile.NONE.withRadiation(75.0F * 3).withHeat(3.0F * 3));
        add(items, "powder_schrabidium", "Schrabidium Powder", Form.DUST, "schrabidium",
                HazardProfile.NONE.withRadiation(15.0F * 3).withBlinding(50.0F * 3));
        add(items, "powder_co60", "Cobalt-60 Powder", Form.DUST, "cobalt_60",
                HazardProfile.NONE.withRadiation(30.0F * 3).withHeat(1.0F * 3));
        add(items, "powder_au198", "Gold-198 Powder", Form.DUST, "gold_198",
                HazardProfile.NONE.withRadiation(500.0F * 3).withHeat(5.0F * 3));
        add(items, "powder_ra226", "Radium-226 Powder", Form.DUST, "radium_226",
                HazardProfile.NONE.withRadiation(7.5F * 3));
        add(items, "powder_actinium", "Actinium Powder", Form.DUST, "actinium_227",
                HazardProfile.NONE.withRadiation(30.0F * 3));
        add(items, "powder_actinium_tiny", "Tiny Pile of Actinium Powder", Form.TINY_DUST, "actinium_227",
                HazardProfile.NONE.withRadiation(30.0F * 0.3F));

        // Things the lungs, skin and fire brigade all dislike.
        add(items, "powder_coal", "Coal Powder", Form.DUST, "coal", HazardProfile.NONE.withCoalDust(3.0F));
        add(items, "powder_coal_tiny", "Tiny Pile of Coal Powder", Form.TINY_DUST, "coal", HazardProfile.NONE.withCoalDust(0.3F));
        add(items, "powder_lignite", "Lignite Powder", Form.DUST, "lignite", HazardProfile.NONE.withCoalDust(3.0F));
        add(items, "ingot_asbestos", "Asbestos Sheet", Form.INGOT, "asbestos", HazardProfile.NONE.withAsbestos(1.0F));
        add(items, "powder_asbestos", "Asbestos Powder", Form.DUST, "asbestos", HazardProfile.NONE.withAsbestos(3.0F));
        add(items, "powder_coltan_ore", "Crushed Coltan", Form.DUST, "coltan",
                HazardProfile.NONE.withAsbestos(3.0F));
        add(items, "lithium", "Lithium Cube", Form.INGOT, "lithium", HazardProfile.NONE.withHydroactive(1.0F));
        add(items, "powder_lithium", "Lithium Powder", Form.DUST, "lithium", HazardProfile.NONE.withHydroactive(3.0F));
        add(items, "powder_lithium_tiny", "Tiny Pile of Lithium Powder", Form.TINY_DUST, "lithium", HazardProfile.NONE.withHydroactive(0.3F));
        add(items, "powder_sodium", "Sodium", Form.DUST, "sodium", HazardProfile.NONE.withHydroactive(3.0F));
        add(items, "powder_strontium", "Strontium Powder", Form.DUST, "strontium",
                HazardProfile.NONE.withHeat(3.0F).withHydroactive(3.0F));
        add(items, "powder_caesium", "Caesium Powder", Form.DUST, "caesium",
                HazardProfile.NONE.withHeat(3.0F).withHydroactive(1.0F));
        add(items, "ingot_phosphorus", "Bar of White Phosphorus", Form.INGOT, "white_phosphorus",
                HazardProfile.NONE.withHeat(5.0F));
        add(items, "ingot_mud", "Solid Mud Brick", Form.INGOT, "mud",
                HazardProfile.NONE.withRadiation(1.0F));

        // Explosives without additional personality traits.
        add(items, "ball_dynamite", "Dynamite", Form.MISC, null, HazardProfile.NONE.withExplosive(2.0F));
        add(items, "cordite", "Cordite", Form.MISC, null, HazardProfile.NONE.withExplosive(2.0F));
        add(items, "ballistite", "Ballistite", Form.MISC, null, HazardProfile.NONE.withExplosive(1.0F));
        addWeapon(items, "stick_tnt", "Stick of TNT", HazardProfile.NONE.withExplosive(1.5F));
        addWeapon(items, "stick_semtex", "Stick of Semtex", HazardProfile.NONE.withExplosive(2.5F));
        addWeapon(items, "stick_c4", "Stick of C-4", HazardProfile.NONE.withExplosive(2.5F));

        CATALOG_ITEMS = Collections.unmodifiableList(items);
        Set<String> activeItems = Set.of(
                "ingot_uranium", "billet_uranium", "nugget_uranium", "powder_uranium",
                "ingot_u233", "billet_u233", "nugget_u233",
                "ingot_u235", "billet_u235", "nugget_u235",
                "ingot_u238", "billet_u238", "nugget_u238",
                "ingot_th232", "billet_th232", "nugget_th232", "powder_thorium",
                "ingot_plutonium", "billet_plutonium", "nugget_plutonium", "powder_plutonium",
                "ingot_pu238", "billet_pu238", "nugget_pu238",
                "ingot_pu239", "billet_pu239", "nugget_pu239",
                "ingot_pu_mix", "billet_pu_mix", "nugget_pu_mix",
                "nuclear_waste", "billet_nuclear_waste", "nuclear_waste_tiny",
                "ingot_neptunium", "billet_neptunium", "nugget_neptunium", "powder_neptunium",
                "ingot_technetium", "billet_technetium", "nugget_technetium",
                "ingot_schrabidium", "billet_schrabidium", "nugget_schrabidium", "powder_schrabidium",
                "ingot_co60", "billet_co60", "nugget_co60", "powder_co60",
                "ingot_ra226", "billet_ra226", "nugget_ra226", "powder_ra226",
                "ingot_actinium", "billet_actinium", "nugget_actinium",
                "ingot_mox_fuel", "billet_ra226be", "billet_pu238be",
                "ingot_mud",
                "powder_coal",
                "powder_coal_tiny",
                "powder_lignite", "powder_strontium",
                "ingot_asbestos",
                "powder_asbestos",
                "powder_coltan_ore",
                "lithium", "powder_lithium", "powder_lithium_tiny",
                "ingot_phosphorus",
                "cordite"
        );
        ITEMS = CATALOG_ITEMS.stream()
                .filter(definition -> activeItems.contains(definition.id()))
                .toList();

        List<BlockDefinition> blocks = new ArrayList<>();
        blocks.add(radioactiveBlock("block_uranium", "Block of Uranium", "uranium", "ingot_uranium", HazardProfile.NONE.withRadiation(3.5F), 50, false, 0));
        blocks.add(radioactiveBlock("block_u233", "Block of Uranium-233", "uranium_233", "ingot_u233", HazardProfile.NONE.withRadiation(50.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_u235", "Block of Uranium-235", "uranium_235", "ingot_u235", HazardProfile.NONE.withRadiation(10.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_u238", "Block of Uranium-238", "uranium_238", "ingot_u238", HazardProfile.NONE.withRadiation(2.5F), 50, false, 0));
        blocks.add(radioactiveBlock("block_neptunium", "Block of Neptunium", "neptunium_237", "ingot_neptunium", HazardProfile.NONE.withRadiation(25.0F), 60, true, 0));
        blocks.add(radioactiveBlock("block_polonium", "Block of Polonium-210", "polonium_210", "ingot_polonium", HazardProfile.NONE.withRadiation(750.0F).withHeat(30.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_plutonium", "Block of Plutonium", "plutonium", "ingot_plutonium", HazardProfile.NONE.withRadiation(75.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_pu238", "Block of Plutonium-238", "plutonium_238", "ingot_pu238", HazardProfile.NONE.withRadiation(100.0F).withHeat(30.0F), 50, true, 5));
        blocks.add(radioactiveBlock("block_pu239", "Block of Plutonium-239", "plutonium_239", "ingot_pu239", HazardProfile.NONE.withRadiation(50.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_pu240", "Block of Plutonium-240", "plutonium_240", "ingot_pu240", HazardProfile.NONE.withRadiation(75.0F), 50, true, 0));
        blocks.add(radioactiveBlock("block_pu_mix", "Block of Reactor Grade Plutonium", "plutonium_rg", "ingot_pu_mix", HazardProfile.NONE.withRadiation(62.5F), 50, true, 0));
        blocks.add(radioactiveBlock("block_thorium", "Block of Thorium-232", "thorium_232", "ingot_th232", HazardProfile.NONE.withRadiation(1.0F), 50, false, 0));
        blocks.add(hazardBlock("block_lithium", "Block of Lithium", "lithium", "lithium",
                HazardProfile.NONE.withHydroactive(10.0F), 10, MapColor.METAL, SoundType.METAL, 15, 0));
        blocks.add(hazardBlock("block_white_phosphorus", "Block of White Phosphorus", "white_phosphorus", "ingot_phosphorus",
                HazardProfile.NONE.withHeat(50.0F), 10, MapColor.STONE, SoundType.STONE, 0, 0));
        blocks.add(radioactiveBlock("block_ra226", "Block of Radium-226", "radium_226", "ingot_ra226", HazardProfile.NONE.withRadiation(75.0F), 10, false, 0));
        blocks.add(radioactiveBlock("block_actinium", "Block of Actinium", "actinium_227", "ingot_actinium", HazardProfile.NONE.withRadiation(300.0F), 10, false, 0));
        CATALOG_BLOCKS = Collections.unmodifiableList(blocks);
        Set<String> activeBlocks = Set.of(
                "block_uranium", "block_u233", "block_u235", "block_u238", "block_thorium",
                "block_plutonium", "block_neptunium", "block_pu238", "block_pu239", "block_pu_mix",
                "block_lithium", "block_white_phosphorus", "block_ra226", "block_actinium"
        );
        BLOCKS = CATALOG_BLOCKS.stream()
                .filter(definition -> activeBlocks.contains(definition.id()))
                .toList();
    }

    private HazardousMaterialDefinitions() {
    }

    private static void addMineralSet(
            List<ItemDefinition> items,
            String material,
            String name,
            String ingot,
            String billet,
            String nugget,
            HazardProfile ingotHazards
    ) {
        items.add(new ItemDefinition(ingot, name + " Ingot", ingot, Form.INGOT, material, ingotHazards, true, CreativeGroup.PARTS));
        items.add(new ItemDefinition(billet, name + " Billet", billet, Form.BILLET, material, ingotHazards.scale(0.5F), true, CreativeGroup.PARTS));
        items.add(new ItemDefinition(nugget, name + " Nugget", nugget, Form.NUGGET, material, ingotHazards.scale(0.1F), true, CreativeGroup.PARTS));
    }

    private static void add(List<ItemDefinition> items, String id, String name, Form form, String material, HazardProfile hazards) {
        items.add(new ItemDefinition(id, name, id, form, material, hazards, false, CreativeGroup.PARTS));
    }

    private static void addWeapon(List<ItemDefinition> items, String id, String name, HazardProfile hazards) {
        items.add(new ItemDefinition(id, name, id, Form.MISC, null, hazards, false, CreativeGroup.WEAPONS));
    }

    private static BlockDefinition radioactiveBlock(
            String id,
            String name,
            String material,
            String compressedItem,
            HazardProfile hazards,
            float resistance,
            boolean radiationFog,
            int lightLevel
    ) {
        return new BlockDefinition(id, name, material, compressedItem, hazards, radiationFog,
                5.0F, resistance, MapColor.METAL, SoundType.METAL, 0, lightLevel);
    }

    private static BlockDefinition hazardBlock(
            String id,
            String name,
            String material,
            String compressedItem,
            HazardProfile hazards,
            float resistance,
            MapColor mapColor,
            SoundType sound,
            float adjacentWaterExplosion,
            int lightLevel
    ) {
        return new BlockDefinition(id, name, material, compressedItem, hazards, false,
                5.0F, resistance, mapColor, sound, adjacentWaterExplosion, lightLevel);
    }
}
