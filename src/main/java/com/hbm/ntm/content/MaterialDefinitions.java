package com.hbm.ntm.content;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import java.util.List;

/** The periodic table, filtered by what currently exists. */
public final class MaterialDefinitions {
    public enum ItemForm {
        INGOT("ingots"),
        BILLET("billets"),
        NUGGET("nuggets"),
        DUST("dusts"),
        PLATE("plates"),
        MISC(null);

        private final String commonTagFolder;

        ItemForm(String commonTagFolder) {
            this.commonTagFolder = commonTagFolder;
        }

        public String commonTagFolder() {
            return commonTagFolder;
        }
    }

    public record ItemDefinition(
            String id,
            String englishName,
            String texture,
            ItemForm form,
            String commonMaterial
    ) {
        public ItemDefinition(String id, String englishName, ItemForm form, String commonMaterial) {
            this(id, englishName, id, form, commonMaterial);
        }
    }

    public record BlockDefinition(
            String id,
            String englishName,
            MapColor mapColor,
            SoundType sound,
            float hardness,
            float resistance,
            String compressedItem,
            String commonMaterial
    ) {
        /** Old resistance took the scenic route through x3/5. */
        public float modernExplosionResistance() {
            return resistance * 0.6F;
        }
    }

    public static final List<ItemDefinition> ITEMS = List.of(
            // Ordinary ingots, before the radiation department arrives.
            new ItemDefinition("ingot_titanium", "Titanium Ingot", ItemForm.INGOT, "titanium"),
            new ItemDefinition("ingot_copper", "Industrial Grade Copper Ingot", ItemForm.INGOT, "copper"),
            new ItemDefinition("ingot_red_copper", "Minecraft Grade Copper Ingot", ItemForm.INGOT, "red_copper"),
            new ItemDefinition("ingot_tungsten", "Tungsten Ingot", ItemForm.INGOT, "tungsten"),
            new ItemDefinition("ingot_tungsten_carbide", "Tungsten Carbide Ingot", ItemForm.INGOT, "tungsten_carbide"),
            new ItemDefinition("ingot_magnetized_tungsten", "Magnetized Tungsten Ingot", ItemForm.INGOT, "magnetized_tungsten"),
            new ItemDefinition("ingot_combine_steel", "CMB Steel Ingot", ItemForm.INGOT, "combine_steel"),
            new ItemDefinition("ingot_aluminium", "Aluminium Ingot", ItemForm.INGOT, "aluminum"),
            new ItemDefinition("ingot_steel", "Steel Ingot", ItemForm.INGOT, "steel"),
            new ItemDefinition("ingot_tcalloy", "Technetium Steel Ingot", ItemForm.INGOT, "technetium_steel"),
            new ItemDefinition("ingot_cdalloy", "Cadmium Steel Ingot", ItemForm.INGOT, "cadmium_steel"),
            new ItemDefinition("ingot_ferrouranium", "Ferrouranium Ingot", ItemForm.INGOT, "ferrouranium"),
            new ItemDefinition("ingot_bismuth_bronze", "Bismuth Bronze Ingot", ItemForm.INGOT, "bismuth_bronze"),
            new ItemDefinition("ingot_arsenic_bronze", "Arsenic Bronze Ingot", ItemForm.INGOT, "arsenic_bronze"),
            new ItemDefinition("ingot_bscco", "BSCCO Ingot", ItemForm.INGOT, "bscco"),
            new ItemDefinition("ingot_lead", "Lead Ingot", ItemForm.INGOT, "lead"),
            new ItemDefinition("ingot_bismuth", "Bismuth Ingot", ItemForm.INGOT, "bismuth"),
            new ItemDefinition("ingot_arsenic", "Arsenic Ingot", ItemForm.INGOT, "arsenic"),
            new ItemDefinition("ingot_calcium", "Calcium Ingot", ItemForm.INGOT, "calcium"),
            new ItemDefinition("ingot_cadmium", "Cadmium Ingot", ItemForm.INGOT, "cadmium"),
            new ItemDefinition("ingot_silicon", "Silicon Boule", ItemForm.INGOT, "silicon"),
            new ItemDefinition("billet_silicon", "Silicon Wafer", ItemForm.BILLET, "silicon"),
            new ItemDefinition("nugget_silicon", "Silicon Nugget", ItemForm.NUGGET, "silicon"),
            new ItemDefinition("nugget_zirconium", "Zirconium Splinter", ItemForm.NUGGET, "zirconium"),
            new ItemDefinition("ingot_tantalium", "Tantalium Ingot", ItemForm.INGOT, "tantalum"),
            new ItemDefinition("nugget_tantalium", "Tantalium Nugget", ItemForm.NUGGET, "tantalum"),
            new ItemDefinition("ingot_niobium", "Niobium Ingot", ItemForm.INGOT, "niobium"),
            new ItemDefinition("ingot_beryllium", "Beryllium Ingot", ItemForm.INGOT, "beryllium"),
            new ItemDefinition("ingot_cobalt", "Cobalt Ingot", ItemForm.INGOT, "cobalt"),
            new ItemDefinition("ingot_boron", "Boron Ingot", ItemForm.INGOT, "boron"),
            new ItemDefinition("ingot_graphite", "Graphite Ingot", ItemForm.INGOT, "graphite"),
            new ItemDefinition("ingot_firebrick", "Firebrick", ItemForm.INGOT, "firebrick"),
            new ItemDefinition("ingot_gunmetal", "Gunmetal Ingot", ItemForm.INGOT, "gunmetal"),
            new ItemDefinition("ingot_weaponsteel", "Weapon Steel Ingot", "ingot_gunsteel", ItemForm.INGOT, "weapon_steel"),
            new ItemDefinition("ingot_zirconium", "Zirconium Cube", ItemForm.INGOT, "zirconium"),
            new ItemDefinition("ingot_dura_steel", "High-Speed Steel Ingot", ItemForm.INGOT, "dura_steel"),
            new ItemDefinition("ingot_desh", "Desh Ingot", ItemForm.INGOT, "desh"),
            new ItemDefinition("ingot_polymer", "Polymer Bar", ItemForm.INGOT, "polymer"),

            // Loose machine snacks.
            new ItemDefinition("sulfur", "Sulfur", ItemForm.MISC, null),
            new ItemDefinition("niter", "Niter", "salpeter", ItemForm.MISC, null),
            new ItemDefinition("fluorite", "Fluorite", ItemForm.MISC, null),
            new ItemDefinition("ball_resin", "Latex", ItemForm.MISC, null),
            new ItemDefinition("ingot_biorubber", "Latex Bar", ItemForm.INGOT, "latex"),
            new ItemDefinition("ingot_rubber", "Rubber Bar", ItemForm.INGOT, "rubber"),
            new ItemDefinition("fragment_coltan", "Coltan", ItemForm.INGOT, "coltan"),
            new ItemDefinition("powder_coltan", "Purified Tantalite", ItemForm.MISC, null),
            new ItemDefinition("gem_tantalium", "Tantalium Polycrystal", ItemForm.MISC, null),
            new ItemDefinition("fragment_cobalt", "Cobalt Fragment", ItemForm.MISC, null),
            new ItemDefinition("scrap", "Scrap", ItemForm.MISC, null),
            new ItemDefinition("dust", "Dust", ItemForm.MISC, null),
            new ItemDefinition("dust_tiny", "Tiny Pile of Dust", ItemForm.MISC, null),
            // Called a nugget, behaves like 125 mB of liquid metal. Fine.
            new ItemDefinition("nugget_mercury", "Drop of Mercury", ItemForm.MISC, null),

            // Powders safe enough to list without a lawyer.
            new ItemDefinition("powder_iron", "Iron Powder", ItemForm.DUST, "iron"),
            new ItemDefinition("powder_gold", "Gold Powder", ItemForm.DUST, "gold"),
            new ItemDefinition("powder_lapis", "Lapis Lazuli Powder", ItemForm.DUST, "lapis"),
            new ItemDefinition("powder_quartz", "Quartz Powder", ItemForm.DUST, "quartz"),
            new ItemDefinition("powder_diamond", "Diamond Powder", ItemForm.DUST, "diamond"),
            new ItemDefinition("powder_emerald", "Emerald Powder", ItemForm.DUST, "emerald"),
            new ItemDefinition("powder_titanium", "Titanium Powder", ItemForm.DUST, "titanium"),
            new ItemDefinition("powder_copper", "Copper Powder", ItemForm.DUST, "copper"),
            new ItemDefinition("powder_red_copper", "Red Copper Powder", ItemForm.DUST, "red_copper"),
            new ItemDefinition("powder_tungsten", "Tungsten Powder", ItemForm.DUST, "tungsten"),
            new ItemDefinition("powder_aluminium", "Aluminium Powder", ItemForm.DUST, "aluminum"),
            new ItemDefinition("powder_steel", "Steel Powder", ItemForm.DUST, "steel"),
            new ItemDefinition("powder_steel_tiny", "Tiny Pile of Steel Powder", ItemForm.MISC, null),
            new ItemDefinition("powder_tcalloy", "Technetium Steel Powder", ItemForm.DUST, "technetium_steel"),
            new ItemDefinition("powder_bismuth", "Bismuth Powder", ItemForm.DUST, "bismuth"),
            new ItemDefinition("powder_calcium", "Calcium Powder", ItemForm.DUST, "calcium"),
            new ItemDefinition("powder_cadmium", "Cadmium Powder", ItemForm.DUST, "cadmium"),
            new ItemDefinition("powder_tantalium", "Tantalium Powder", ItemForm.DUST, "tantalum"),
            new ItemDefinition("powder_beryllium", "Beryllium Powder", ItemForm.DUST, "beryllium"),
            new ItemDefinition("powder_cobalt", "Cobalt Powder", ItemForm.DUST, "cobalt"),
            new ItemDefinition("powder_cobalt_tiny", "Tiny Pile of Cobalt Powder", ItemForm.MISC, null),
            new ItemDefinition("powder_niobium", "Niobium Powder", ItemForm.DUST, "niobium"),
            new ItemDefinition("powder_desh_mix", "Desh Blend", ItemForm.MISC, null),
            new ItemDefinition("powder_polymer", "Polymer Powder", ItemForm.DUST, "polymer"),
            new ItemDefinition("powder_magnetized_tungsten", "Magnetized Tungsten Powder", ItemForm.DUST, "magnetized_tungsten"),
            new ItemDefinition("powder_combine_steel", "CMB Steel Powder", ItemForm.DUST, "combine_steel"),

            // Things the press can flatten today.
            new ItemDefinition("plate_iron", "Iron Plate", ItemForm.PLATE, "iron"),
            new ItemDefinition("plate_gold", "Gold Plate", ItemForm.PLATE, "gold"),
            new ItemDefinition("plate_titanium", "Titanium Plate", ItemForm.PLATE, "titanium"),
            new ItemDefinition("plate_aluminium", "Aluminium Plate", ItemForm.PLATE, "aluminum"),
            new ItemDefinition("plate_steel", "Steel Plate", ItemForm.PLATE, "steel"),
            new ItemDefinition("plate_lead", "Lead Plate", ItemForm.PLATE, "lead"),
            new ItemDefinition("plate_copper", "Copper Plate", ItemForm.PLATE, "copper"),
            new ItemDefinition("plate_gunmetal", "Gunmetal Plate", ItemForm.PLATE, "gunmetal"),
            new ItemDefinition("plate_weaponsteel", "Weapon Steel Plate", "plate_gunsteel", ItemForm.PLATE, "weapon_steel"),
            new ItemDefinition("plate_dura_steel", "High-Speed Steel Plate", ItemForm.PLATE, "dura_steel"),
            new ItemDefinition("plate_desh", "Desh Compound Plate", ItemForm.MISC, null)
    );

    public static final List<BlockDefinition> BLOCKS = List.of(
            // Storage blocks without bespoke personality disorders.
            block("block_titanium", "Block of Titanium", SoundType.METAL, 5.0F, 50.0F, "ingot_titanium", "titanium"),
            block("block_sulfur", "Block of Sulfur", SoundType.STONE, 5.0F, 10.0F, "sulfur", "sulfur"),
            block("block_niter", "Block of Niter", SoundType.STONE, 5.0F, 10.0F, "niter", "niter"),
            block("block_copper", "Block of Copper", SoundType.METAL, 5.0F, 20.0F, "ingot_copper", "copper"),
            block("block_red_copper", "Block of Red Copper", SoundType.METAL, 5.0F, 25.0F, "ingot_red_copper", "red_copper"),
            block("block_tungsten", "Block of Tungsten", SoundType.METAL, 5.0F, 20.0F, "ingot_tungsten", "tungsten"),
            block("block_aluminium", "Block of Aluminium", SoundType.METAL, 5.0F, 20.0F, "ingot_aluminium", "aluminum"),
            block("block_fluorite", "Block of Fluorite", SoundType.STONE, 5.0F, 10.0F, "fluorite", "fluorite"),
            block("block_beryllium", "Block of Beryllium", SoundType.METAL, 5.0F, 20.0F, "ingot_beryllium", "beryllium"),
            block("block_cobalt", "Block of Cobalt", SoundType.STONE, 5.0F, 50.0F, "ingot_cobalt", "cobalt"),
            block("block_steel", "Block of Steel", SoundType.METAL, 5.0F, 50.0F, "ingot_steel", "steel"),
            block("block_tcalloy", "Block of Technetium Steel", SoundType.METAL, 5.0F, 70.0F, "ingot_tcalloy", "technetium_steel"),
            block("block_cdalloy", "Block of Cadmium Steel", SoundType.METAL, 5.0F, 70.0F, "ingot_cdalloy", "cadmium_steel"),
            block("block_magnetized_tungsten", "Block of Magnetized Tungsten", SoundType.METAL, 5.0F, 75.0F,
                    "ingot_magnetized_tungsten", "magnetized_tungsten"),
            block("block_combine_steel", "Block of CMB Steel", SoundType.METAL, 5.0F, 600.0F,
                    "ingot_combine_steel", "combine_steel"),
            block("block_lead", "Block of Lead", SoundType.METAL, 5.0F, 50.0F, "ingot_lead", "lead"),
            block("block_bismuth", "Block of Bismuth", SoundType.METAL, 5.0F, 90.0F, null, "bismuth"),
            block("block_cadmium", "Block of Cadmium", SoundType.METAL, 5.0F, 90.0F, "ingot_cadmium", "cadmium"),
            block("block_coltan", "Block of Coltan", SoundType.METAL, 5.0F, 50.0F, "fragment_coltan", "coltan"),
            block("block_tantalium", "Block of Tantalium", SoundType.METAL, 5.0F, 50.0F,
                    "ingot_tantalium", "tantalum"),
            block("block_niobium", "Block of Niobium", SoundType.METAL, 5.0F, 50.0F, "ingot_niobium", "niobium"),
            block("block_zirconium", "Block of Zirconium", SoundType.METAL, 5.0F, 30.0F, "ingot_zirconium", "zirconium"),
            block("block_boron", "Block of Boron", SoundType.METAL, 5.0F, 10.0F, "ingot_boron", "boron"),
            rockBlock("block_polymer", "Block of Polymer", 3.0F, 10.0F, "ingot_polymer", "polymer"),
            rockBlock("block_bakelite", "Block of Bakelite", 3.0F, 5.0F, null, "bakelite"),
            rockBlock("block_rubber", "Block of Rubber", 3.0F, 15.0F, "ingot_rubber", "rubber"),
            block("block_lanthanium", "Block of Lanthanium", SoundType.METAL, 5.0F, 10.0F, null, "lanthanium")
    );

    private MaterialDefinitions() {
    }

    private static BlockDefinition block(
            String id,
            String name,
            SoundType sound,
            float hardness,
            float resistance,
            String compressedItem,
            String commonMaterial
    ) {
        return new BlockDefinition(id, name, MapColor.METAL, sound, hardness, resistance, compressedItem, commonMaterial);
    }

    private static BlockDefinition rockBlock(
            String id,
            String name,
            float hardness,
            float resistance,
            String compressedItem,
            String commonMaterial
    ) {
        return new BlockDefinition(id, name, MapColor.STONE, SoundType.STONE, hardness, resistance, compressedItem, commonMaterial);
    }
}
