package com.hbm.ntm.client.compat.jei;

import com.hbm.ntm.block.StoneResourceBlock;
import com.hbm.ntm.foundry.FoundryMaterial;
import com.hbm.ntm.item.StoneResourceBlockItem;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Maps a Crucible {@link FoundryMaterial.MaterialAmount} to the item a player actually feeds, so the
 * JEI page shows clickable icons. Most materials use {@link FoundryMaterial#ingot()}/{@code nugget()};
 * the five that have no metal form (flux, redstone, strontium, hematite/malachite ore) are the exact
 * items {@code FoundryMaterial.fromItem} recognizes for them. Counts are the whole feed-item count for
 * the amount (the crucible meters sub-item units internally, so tiny amounts show as one feed item).
 */
final class JeiMaterials {
    private JeiMaterials() {
    }

    static ItemStack crucibleStack(FoundryMaterial.MaterialAmount amount) {
        return crucibleStack(amount.material(), amount.amount());
    }

    static ItemStack crucibleStack(FoundryMaterial material, int amount) {
        return switch (material) {
            case FLUX -> countStack(new ItemStack(ModItems.POWDER_FLUX.get()), amount, FoundryMaterial.INGOT);
            case STRONTIUM -> countStack(new ItemStack(ModItems.get("powder_strontium").get()),
                    amount, FoundryMaterial.INGOT);
            case REDSTONE -> countStack(new ItemStack(Items.REDSTONE), amount, FoundryMaterial.NUGGET);
            case HEMATITE -> ore(StoneResourceBlock.Type.HEMATITE, amount);
            case MALACHITE -> ore(StoneResourceBlock.Type.MALACHITE, amount);
            default -> genericStack(material, amount);
        };
    }

    private static ItemStack genericStack(FoundryMaterial material, int amount) {
        ItemStack nugget = material.nugget();
        if (!nugget.isEmpty()) {
            return nugget.copyWithCount(Math.max(1, amount / FoundryMaterial.NUGGET));
        }
        ItemStack ingot = material.ingot();
        if (!ingot.isEmpty()) {
            return ingot.copyWithCount(Math.max(1, Math.round(amount / (float) FoundryMaterial.INGOT)));
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack countStack(ItemStack stack, int amount, int unit) {
        stack.setCount(Math.max(1, amount / unit));
        return stack;
    }

    private static ItemStack ore(StoneResourceBlock.Type type, int amount) {
        return StoneResourceBlockItem.create(ModItems.STONE_RESOURCE_ITEM.get(), type,
                Math.max(1, amount / FoundryMaterial.INGOT));
    }

    /** The exact crucible amount in the machine's own units, e.g. "2I", "5N", "1I 3N". */
    static String compactAmount(int amount) {
        int ingots = amount / FoundryMaterial.INGOT;
        amount -= ingots * FoundryMaterial.INGOT;
        int nuggets = amount / FoundryMaterial.NUGGET;
        amount -= nuggets * FoundryMaterial.NUGGET;
        StringBuilder builder = new StringBuilder();
        if (ingots > 0) builder.append(ingots).append('I');
        if (nuggets > 0) builder.append(builder.isEmpty() ? "" : " ").append(nuggets).append('N');
        if (amount > 0) builder.append(builder.isEmpty() ? "" : " ").append(amount).append('Q');
        return builder.isEmpty() ? "0" : builder.toString();
    }
}

