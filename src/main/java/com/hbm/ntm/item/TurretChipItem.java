package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class TurretChipItem extends Item {
    private static final String PLAYERS = "players";

    public TurretChipItem() { super(new Properties().stacksTo(1)); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        String name = player.getGameProfile().getName();
        if (!contains(stack, name)) {
            add(stack, name);
            player.swing(hand, true);
            level.playSound(player, player.blockPosition(), ModSounds.TECH_BLEEP.get(),
                    SoundSource.PLAYERS, 1F, 1F);
            if (!level.isClientSide) player.displayClientMessage(Component.literal("Added player data!"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                                          List<Component> tooltip, TooltipFlag flag) {
        for (String name : names(stack)) tooltip.add(Component.literal(name).withStyle(ChatFormatting.GRAY));
    }

    public static boolean contains(ItemStack stack, String name) {
        return names(stack).stream().anyMatch(existing -> existing.equalsIgnoreCase(name));
    }

    public static void add(ItemStack stack, String name) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag players = tag.getList(PLAYERS, CompoundTag.TAG_STRING);
        players.add(StringTag.valueOf(name));
        tag.put(PLAYERS, players);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void remove(ItemStack stack, int index) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag players = tag.getList(PLAYERS, CompoundTag.TAG_STRING);
        if (index < 0 || index >= players.size()) return;
        players.remove(index);
        tag.put(PLAYERS, players);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static List<String> names(ItemStack stack) {
        if (!(stack.getItem() instanceof TurretChipItem)) return List.of();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag players = tag.getList(PLAYERS, CompoundTag.TAG_STRING);
        List<String> result = new ArrayList<>(players.size());
        for (int index = 0; index < players.size(); index++) result.add(players.getString(index));
        return result;
    }
}
