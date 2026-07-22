package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.block.StoneResourceBlock;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.BoltItem;
import com.hbm.ntm.item.CasingItem;
import com.hbm.ntm.item.CastPlateItem;
import com.hbm.ntm.item.DenseWireItem;
import com.hbm.ntm.item.FoundryIngotItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryPartItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.OreChunkItem;
import com.hbm.ntm.item.PipeItem;
import com.hbm.ntm.item.ShellItem;
import com.hbm.ntm.item.StoneResourceBlockItem;
import com.hbm.ntm.item.WeldedPlateItem;
import com.hbm.ntm.item.WireFineItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

final class FoundryJeiRecipes {
    private FoundryJeiRecipes() {
    }

    static List<SmeltingRecipe> smelting() {
        List<ItemStack> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) add(candidates, item.getDefaultInstance());
        for (FoundryMaterial material : FoundryMaterial.values()) {
            add(candidates, FoundryIngotItem.create(ModItems.INGOT_RAW.get(), material, 1));
            for (FoundryMoldItem.Mold mold : FoundryMoldItem.Mold.values()) {
                add(candidates, material.output(mold));
            }
        }
        for (StoneResourceBlock.Type type : StoneResourceBlock.Type.values()) {
            add(candidates, StoneResourceBlockItem.create(ModItems.STONE_RESOURCE_ITEM.get(), type, 1));
        }
        for (OreChunkItem.ChunkType type : OreChunkItem.ChunkType.values()) {
            add(candidates, OreChunkItem.create(ModItems.CHUNK_ORE.get(), type, 1));
        }
        for (BoltItem.BoltMaterial material : BoltItem.BoltMaterial.values()) {
            add(candidates, BoltItem.create(ModItems.BOLT.get(), material, 1));
        }
        for (WireFineItem.WireMaterial material : WireFineItem.WireMaterial.values()) {
            add(candidates, WireFineItem.create(ModItems.WIRE_FINE.get(), material, 1));
        }
        for (CastPlateItem.CastPlateMaterial material : CastPlateItem.CastPlateMaterial.values()) {
            add(candidates, CastPlateItem.create(ModItems.PLATE_CAST.get(), material, 1));
        }
        for (FoundryMaterial material : FoundryMaterial.values()) {
            add(candidates, DenseWireItem.create(ModItems.WIRE_DENSE.get(), material, 1));
            for (FoundryPartItem.PartType type : FoundryPartItem.PartType.values()) {
                add(candidates, FoundryPartItem.create(ModItems.get(type.id()).get(), material, 1));
            }
        }
        for (PipeItem.PipeMaterial material : PipeItem.PipeMaterial.values()) {
            add(candidates, PipeItem.create(ModItems.PIPE.get(), material, 1));
        }
        for (ShellItem.ShellMaterial material : ShellItem.ShellMaterial.values()) {
            add(candidates, ShellItem.create(ModItems.SHELL.get(), material, 1));
        }
        for (CasingItem.CasingType type : CasingItem.CasingType.values()) {
            add(candidates, CasingItem.create(ModItems.CASING.get(), type, 1));
        }
        for (WeldedPlateItem.WeldedPlateMaterial material : WeldedPlateItem.WeldedPlateMaterial.values()) {
            add(candidates, WeldedPlateItem.create(ModItems.PLATE_WELDED.get(), material, 1));
        }

        LinkedHashMap<FoundryMaterial.MaterialAmount, List<ItemStack>> grouped = new LinkedHashMap<>();
        for (ItemStack candidate : candidates) {
            FoundryMaterial.MaterialAmount amount = FoundryMaterial.fromItem(candidate);
            if (amount == null || !amount.material().smeltable()) continue;
            grouped.computeIfAbsent(amount, ignored -> new ArrayList<>()).add(candidate.copyWithCount(1));
        }

        List<SmeltingRecipe> recipes = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            FoundryMaterial.MaterialAmount amount = entry.getKey();
            recipes.add(new SmeltingRecipe(
                    ResourceLocation.fromNamespaceAndPath("hbm",
                            "crucible_smelting/" + amount.material().id() + "/" + amount.amount()),
                    List.copyOf(entry.getValue()),
                    FoundryScrapsItem.create(ModItems.SCRAPS.get(), amount.material(), amount.amount())));
        }
        return List.copyOf(recipes);
    }

    static List<CastingRecipe> casting() {
        List<CastingRecipe> recipes = new ArrayList<>();
        for (FoundryMaterial material : FoundryMaterial.values()) {
            if (!material.smeltable() || material.additive()) continue;
            for (FoundryMoldItem.Mold mold : FoundryMoldItem.Mold.values()) {
                ItemStack output = material.output(mold);
                if (output.isEmpty()) continue;
                ItemStack machine = new ItemStack(mold.size() == FoundryMoldItem.MoldSize.SMALL
                        ? ModItems.FOUNDRY_MOLD_ITEM.get() : ModItems.FOUNDRY_BASIN_ITEM.get());
                recipes.add(new CastingRecipe(
                        ResourceLocation.fromNamespaceAndPath("hbm",
                                "foundry_casting/" + material.id() + "/" + mold.name().toLowerCase(java.util.Locale.ROOT)),
                        FoundryScrapsItem.create(ModItems.SCRAPS.get(), material, mold.cost()),
                        FoundryMoldItem.create(ModItems.MOLD.get(), mold), machine, output));
            }
        }
        return List.copyOf(recipes);
    }

    private static void add(List<ItemStack> stacks, ItemStack candidate) {
        if (candidate.isEmpty()) return;
        ItemStack single = candidate.copyWithCount(1);
        for (ItemStack stack : stacks) {
            if (ItemStack.isSameItemSameComponents(stack, single)) return;
        }
        stacks.add(single);
    }

    record SmeltingRecipe(ResourceLocation id, List<ItemStack> inputs, ItemStack output) {
    }

    record CastingRecipe(ResourceLocation id, ItemStack input, ItemStack mold,
                         ItemStack machine, ItemStack output) {
    }
}
