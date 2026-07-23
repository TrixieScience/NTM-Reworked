package com.hbm.ntm.item;

import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import java.util.List;

public final class FluidBarrelBlockItem extends BlockItem {
    private final FluidBarrelBlock.Type type;

    public FluidBarrelBlockItem(FluidBarrelBlock block, Properties properties) {
        super(block, properties);
        type = block.type();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (data.contains(FluidBarrelBlockEntity.ITEM_FLUID)) {
            FluidIdentifierItem.Selection selection = FluidIdentifierItem.Selection.byId(
                    data.getString(FluidBarrelBlockEntity.ITEM_FLUID));
            int amount = Math.clamp(data.getInt(FluidBarrelBlockEntity.ITEM_AMOUNT), 0, type.capacity());
            tooltip.add(Component.literal(amount + "/" + type.capacity() + "mB ")
                    .append(Component.translatable(selection.translationKey()))
                    .withStyle(ChatFormatting.YELLOW));
        }

        tooltip.add(Component.literal("Capacity: " + String.format("%,d", type.capacity()) + "mB")
                .withStyle(ChatFormatting.AQUA));
        switch (type) {
            case PLASTIC -> {
                warning(tooltip, "Cannot store hot fluids");
                warning(tooltip, "Cannot store corrosive fluids");
                warning(tooltip, "Cannot store antimatter");
            }
            case CORRODED -> {
                allowed(tooltip, "Can store hot fluids");
                allowed(tooltip, "Can store highly corrosive fluids");
                warning(tooltip, "Cannot store antimatter");
                tooltip.add(Component.literal("Leaky").withStyle(ChatFormatting.RED));
            }
            case STEEL -> {
                allowed(tooltip, "Can store hot fluids");
                allowed(tooltip, "Can store corrosive fluids");
                warning(tooltip, "Cannot store highly corrosive fluids properly");
                warning(tooltip, "Cannot store antimatter");
            }
            case TCALLOY -> {
                allowed(tooltip, "Can store hot fluids");
                allowed(tooltip, "Can store highly corrosive fluids");
                warning(tooltip, "Cannot store antimatter");
            }
            case ANTIMATTER -> {
                allowed(tooltip, "Can store hot fluids");
                allowed(tooltip, "Can store highly corrosive fluids");
                allowed(tooltip, "Can store antimatter");
            }
        }
    }

    private static void warning(List<Component> tooltip, String text) {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.YELLOW));
    }

    private static void allowed(List<Component> tooltip, String text) {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.GREEN));
    }
}
