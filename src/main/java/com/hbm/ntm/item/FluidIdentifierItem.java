package com.hbm.ntm.item;

import com.hbm.ntm.inventory.FluidIdentifierMenu;
import com.hbm.ntm.registry.ModFluids;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.function.Supplier;

/** Component-backed {@code fluid_identifier_multi}. */
public final class FluidIdentifierItem extends Item {
    private static final String PRIMARY = "fluid1";
    private static final String SECONDARY = "fluid2";

    public FluidIdentifierItem() {
        super(new Properties().stacksTo(1));
    }

    public static Selection primary(ItemStack stack) {
        return get(stack, PRIMARY);
    }

    public static Selection secondary(ItemStack stack) {
        return get(stack, SECONDARY);
    }

    public static void set(ItemStack stack, Selection selection, boolean primary) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(primary ? PRIMARY : SECONDARY, selection.id());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void swap(ItemStack stack) {
        Selection primary = primary(stack);
        Selection secondary = secondary(stack);
        set(stack, secondary, true);
        set(stack, primary, false);
    }

    private static Selection get(ItemStack stack, String key) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(key) ? Selection.byId(tag.getString(key)) : Selection.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, ignored) -> new FluidIdentifierMenu(id, inventory, hand),
                        Component.translatable("container.fluidIdentifier")),
                        buffer -> buffer.writeEnum(hand));
            }
        } else if (!level.isClientSide) {
            swap(stack);
            Selection selected = primary(stack);
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.PLAYERS, 0.25F, 1.25F);
            player.displayClientMessage(Component.translatable(selected.translationKey()), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return stack.copy(); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.hbm.fluid_identifier_multi.info"));
        tooltip.add(Component.literal("   ").append(Component.translatable(primary(stack).translationKey())));
        tooltip.add(Component.translatable("item.hbm.fluid_identifier_multi.info2"));
        tooltip.add(Component.literal("   ").append(Component.translatable(secondary(stack).translationKey())));
    }

    public enum Selection implements StringRepresentable {
        NONE("none", "hbmfluid.none", 0xFFFFFF, () -> Fluids.EMPTY),
        AIR("air", "hbmfluid.air", 0xE7EAEB, () -> ModFluids.AIR.get()),
        AIRBLAST("airblast", "hbmfluid.airblast", 0xFFDADA, () -> ModFluids.AIRBLAST.get()),
        WATER("water", "block.minecraft.water", 0x3333FF, () -> Fluids.WATER),
        STEAM("steam", "hbmfluid.steam", 0xE5E5E5, () -> ModFluids.STEAM.get()),
        HOTSTEAM("hotsteam", "hbmfluid.hotsteam", 0xE7D6D6, () -> ModFluids.HOTSTEAM.get()),
        SUPERHOTSTEAM("superhotsteam", "hbmfluid.superhotsteam", 0xE7B7B7,
                () -> ModFluids.SUPERHOTSTEAM.get()),
        ULTRAHOTSTEAM("ultrahotsteam", "hbmfluid.ultrahotsteam", 0xE39393,
                () -> ModFluids.ULTRAHOTSTEAM.get()),
        COOLANT("coolant", "hbmfluid.coolant", 0xD8FCFF, () -> ModFluids.COOLANT.get()),
        COOLANT_HOT("coolant_hot", "hbmfluid.coolant_hot", 0x99525E, () -> ModFluids.COOLANT_HOT.get()),
        LAVA("lava", "block.minecraft.lava", 0xFF3300, () -> Fluids.LAVA),
        PEROXIDE("peroxide", "hbmfluid.peroxide", 0xFFF7AA, () -> ModFluids.PEROXIDE.get()),
        SULFURIC_ACID("sulfuric_acid", "hbmfluid.sulfuric_acid", 0xB0AA64,
                () -> ModFluids.SULFURIC_ACID.get()),
        OIL("oil", "hbmfluid.oil", 0x020202, () -> ModFluids.OIL.get()),
        HOTOIL("hotoil", "hbmfluid.hotoil", 0x300900, () -> ModFluids.HOTOIL.get()),
        HEAVYOIL("heavyoil", "hbmfluid.heavyoil", 0x141312, () -> ModFluids.HEAVYOIL.get()),
        NAPHTHA("naphtha", "hbmfluid.naphtha", 0x595744, () -> ModFluids.NAPHTHA.get()),
        LIGHTOIL("lightoil", "hbmfluid.lightoil", 0x8C7451, () -> ModFluids.LIGHTOIL.get()),
        BITUMEN("bitumen", "hbmfluid.bitumen", 0x1F2426, () -> ModFluids.BITUMEN.get()),
        SMEAR("smear", "hbmfluid.smear", 0x190F01, () -> ModFluids.SMEAR.get()),
        HEATINGOIL("heatingoil", "hbmfluid.heatingoil", 0x211806, () -> ModFluids.HEATINGOIL.get()),
        WOODOIL("woodoil", "hbmfluid.woodoil", 0x847D54, () -> ModFluids.WOODOIL.get()),
        COALCREOSOTE("coalcreosote", "hbmfluid.coalcreosote", 0x51694F,
                () -> ModFluids.COALCREOSOTE.get()),
        LUBRICANT("lubricant", "hbmfluid.lubricant", 0x606060, () -> ModFluids.LUBRICANT.get()),
        DIESEL("diesel", "hbmfluid.diesel", 0xF2EED5, () -> ModFluids.DIESEL.get()),
        KEROSENE("kerosene", "hbmfluid.kerosene", 0xFFA5D2, () -> ModFluids.KEROSENE.get()),
        PETROLEUM("petroleum", "hbmfluid.petroleum", 0x7CB7C9, () -> ModFluids.PETROLEUM.get()),
        GAS("gas", "hbmfluid.gas", 0xFFFEED, () -> ModFluids.GAS.get()),
        CARBONDIOXIDE("carbondioxide", "hbmfluid.carbondioxide", 0xB0B0B0,
                () -> ModFluids.CARBONDIOXIDE.get()),
        HYDROGEN("hydrogen", "hbmfluid.hydrogen", 0x4286F4, () -> ModFluids.HYDROGEN.get()),
        DEUTERIUM("deuterium", "hbmfluid.deuterium", 0x3A6EA5, () -> ModFluids.DEUTERIUM.get()),
        TRITIUM("tritium", "hbmfluid.tritium", 0x2FB24C, () -> ModFluids.TRITIUM.get()),
        CRYOGEL("cryogel", "hbmfluid.cryogel", 0x7DE7FF, () -> ModFluids.CRYOGEL.get()),
        UNSATURATEDS("unsaturateds", "hbmfluid.unsaturateds", 0x628FAE,
                () -> ModFluids.UNSATURATEDS.get()),
        SPENTSTEAM("spentsteam", "hbmfluid.spentsteam", 0x445772,
                () -> ModFluids.SPENTSTEAM.get()),
        FLUE("flue", "hbmfluid.flue", 0x131313, () -> ModFluids.FLUE.get()),
        MERCURY("mercury", "hbmfluid.mercury", 0x808080, () -> ModFluids.MERCURY.get()),
        BLOOD("blood", "hbmfluid.blood", 0xB22424, () -> ModFluids.BLOOD.get()),
        OXYGEN("oxygen", "hbmfluid.oxygen", 0x98BDF9, () -> ModFluids.OXYGEN.get()),
        PAIN("pain", "hbmfluid.pain", 0x938541, () -> ModFluids.PAIN.get()),
        SAS3("sas3", "hbmfluid.sas3", 0x4FFFFC, () -> ModFluids.SAS3.get());

        private final String id;
        private final String translationKey;
        private final int color;
        private final Supplier<Fluid> fluid;

        Selection(String id, String translationKey, int color, Supplier<Fluid> fluid) {
            this.id = id;
            this.translationKey = translationKey;
            this.color = color;
            this.fluid = fluid;
        }

        public String id() { return id; }
        @Override public String getSerializedName() { return id; }
        public String translationKey() { return translationKey; }
        public int color() { return color; }
        public Fluid fluid() { return fluid.get(); }
        public boolean accepts(Fluid candidate) { return this != NONE && candidate.isSame(fluid()); }

        public static Selection byId(String id) {
            for (Selection selection : values()) if (selection.id.equals(id)) return selection;
            return NONE;
        }

        public static Selection fromFluid(Fluid fluid) {
            for (Selection selection : values()) if (selection != NONE && fluid.isSame(selection.fluid())) return selection;
            return NONE;
        }
    }
}
