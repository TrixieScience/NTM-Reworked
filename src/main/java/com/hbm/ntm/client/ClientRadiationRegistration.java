package com.hbm.ntm.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.hbm.ntm.client.model.AshGlassesModel;
import com.hbm.ntm.client.model.EnvsuitArmorModel;
import com.hbm.ntm.client.model.DntArmorModel;
import com.hbm.ntm.client.model.ProtectiveMaskModel;
import com.hbm.ntm.client.render.EnvsuitItemRenderer;
import com.hbm.ntm.client.render.DntItemRenderer;
import com.hbm.ntm.client.render.GeigerCounterItemRenderer;
import com.hbm.ntm.client.render.GeigerCounterRenderer;
import com.hbm.ntm.item.MaskFilterStorage;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class ClientRadiationRegistration {
    private static final ResourceLocation HAZMAT_OVERLAY = ResourceLocation.fromNamespaceAndPath(
            com.hbm.ntm.HbmNtm.MOD_ID, "textures/misc/overlay_hazmat.png");
    private static final ResourceLocation[] GOGGLES_OVERLAYS = overlays("overlay_goggles");
    private static final ResourceLocation[] GAS_MASK_OVERLAYS = overlays("overlay_gasmask");

    private ClientRadiationRegistration() {
    }

    public static void register(IEventBus modEventBus) {
        ClientRadiationEvents.register();
        ClientAshEvents.register();
        ClientEnvsuitEvents.register();
        ClientDntArmorEvents.register();
        modEventBus.addListener(ClientRadiationRegistration::registerRenderers);
        modEventBus.addListener(ClientRadiationRegistration::registerModels);
        modEventBus.addListener(ClientRadiationRegistration::registerLayers);
        modEventBus.addListener(ClientRadiationRegistration::registerExtensions);
        modEventBus.addListener(ClientRadiationRegistration::registerKeys);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientDntArmorEvents.TOGGLE_JETPACK);
        event.register(ClientDntArmorEvents.TOGGLE_HUD);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GEIGER.get(), GeigerCounterRenderer::new);
    }

    private static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(GeigerCounterRenderer.MODEL);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AshGlassesModel.LAYER, AshGlassesModel::createLayer);
        event.registerLayerDefinition(EnvsuitArmorModel.LAYER, EnvsuitArmorModel::createLayer);
        event.registerLayerDefinition(DntArmorModel.LAYER, DntArmorModel::createLayer);
        event.registerLayerDefinition(ProtectiveMaskModel.GOGGLES_LAYER, ProtectiveMaskModel::createGogglesLayer);
        event.registerLayerDefinition(ProtectiveMaskModel.GAS_MASK_LAYER, ProtectiveMaskModel::createGasMaskLayer);
        event.registerLayerDefinition(ProtectiveMaskModel.M65_LAYER, ProtectiveMaskModel::createM65Layer);
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private final GeigerCounterItemRenderer renderer = new GeigerCounterItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ModItems.GEIGER_BLOCK_ITEM.get());
        event.registerItem(new IClientItemExtensions() {
            @Override
            public void renderHelmetOverlay(
                    ItemStack stack,
                    Player player,
                    GuiGraphics graphics,
                    DeltaTracker deltaTracker
            ) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.blit(HAZMAT_OVERLAY, 0, 0, -90, 0.0F, 0.0F,
                        graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
                RenderSystem.disableBlend();
            }
        }, ModItems.HAZMAT_HELMET.get());
        event.registerItem(maskExtension(ProtectiveMaskModel.GOGGLES_LAYER,
                ProtectiveMaskModel.Kind.GOGGLES, GOGGLES_OVERLAYS), ModItems.GOGGLES.get());
        event.registerItem(ashGlassesExtension(), ModItems.ASHGLASSES.get());
        event.registerItem(maskExtension(ProtectiveMaskModel.GAS_MASK_LAYER,
                ProtectiveMaskModel.Kind.GAS_MASK, GAS_MASK_OVERLAYS), ModItems.GAS_MASK.get());
        event.registerItem(maskExtension(ProtectiveMaskModel.M65_LAYER,
                ProtectiveMaskModel.Kind.M65, GOGGLES_OVERLAYS), ModItems.GAS_MASK_M65.get());
        event.registerItem(maskExtension(ProtectiveMaskModel.M65_LAYER,
                ProtectiveMaskModel.Kind.M65, null), ModItems.GAS_MASK_MONO.get());
        event.registerItem(maskExtension(ProtectiveMaskModel.M65_LAYER,
                ProtectiveMaskModel.Kind.M65, null), ModItems.GAS_MASK_OLDE.get());
        event.registerItem(envsuitExtension(ArmorItem.Type.HELMET), ModItems.ENVSUIT_HELMET.get());
        event.registerItem(envsuitExtension(ArmorItem.Type.CHESTPLATE), ModItems.ENVSUIT_PLATE.get());
        event.registerItem(envsuitExtension(ArmorItem.Type.LEGGINGS), ModItems.ENVSUIT_LEGS.get());
        event.registerItem(envsuitExtension(ArmorItem.Type.BOOTS), ModItems.ENVSUIT_BOOTS.get());
        event.registerItem(dntExtension(ArmorItem.Type.HELMET), ModItems.DNS_HELMET.get());
        event.registerItem(dntExtension(ArmorItem.Type.CHESTPLATE), ModItems.DNS_PLATE.get());
        event.registerItem(dntExtension(ArmorItem.Type.LEGGINGS), ModItems.DNS_LEGS.get());
        event.registerItem(dntExtension(ArmorItem.Type.BOOTS), ModItems.DNS_BOOTS.get());
    }

    private static IClientItemExtensions dntExtension(ArmorItem.Type piece) {
        return new IClientItemExtensions() {
            private final DntItemRenderer itemRenderer = new DntItemRenderer(piece);
            private DntArmorModel armorModel;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot,
                                                           HumanoidModel<?> original) {
                if (equipmentSlot != slotFor(piece)) return original;
                if (armorModel == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    armorModel = new DntArmorModel(
                            minecraft.getEntityModels().bakeLayer(DntArmorModel.LAYER),
                            minecraft.getResourceManager(), piece);
                }
                armorModel.beginRender();
                return armorModel;
            }

            @Override
            public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity,
                                              ArmorMaterial.Layer layer, int layerIndex,
                                              int fallbackColor) {
                if (armorModel == null || !armorModel.usesPass(layerIndex)) return 0;
                armorModel.setPass(layerIndex);
                return 0xFFFFFFFF;
            }
        };
    }

    private static IClientItemExtensions envsuitExtension(ArmorItem.Type piece) {
        return new IClientItemExtensions() {
            private final EnvsuitItemRenderer itemRenderer = new EnvsuitItemRenderer(piece);
            private EnvsuitArmorModel armorModel;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return itemRenderer;
            }

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot,
                                                           HumanoidModel<?> original) {
                if (equipmentSlot != slotFor(piece)) return original;
                if (armorModel == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    armorModel = new EnvsuitArmorModel(
                            minecraft.getEntityModels().bakeLayer(EnvsuitArmorModel.LAYER),
                            minecraft.getResourceManager(), piece);
                }
                armorModel.beginRender();
                return armorModel;
            }

            @Override
            public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity,
                                              ArmorMaterial.Layer layer, int layerIndex,
                                              int fallbackColor) {
                if (armorModel == null || !armorModel.usesPass(layerIndex)) return 0;
                armorModel.setPass(layerIndex);
                return layerIndex == EnvsuitArmorModel.LAMP_PASS ? 0xFFFFFFCC : 0xFFFFFFFF;
            }
        };
    }

    private static EquipmentSlot slotFor(ArmorItem.Type piece) {
        return switch (piece) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
            case BODY -> EquipmentSlot.BODY;
        };
    }

    private static IClientItemExtensions ashGlassesExtension() {
        return new IClientItemExtensions() {
            private AshGlassesModel model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot,
                                                           HumanoidModel<?> original) {
                if (equipmentSlot != EquipmentSlot.HEAD) return original;
                if (model == null) {
                    Minecraft minecraft = Minecraft.getInstance();
                    model = new AshGlassesModel(minecraft.getEntityModels().bakeLayer(AshGlassesModel.LAYER),
                            minecraft.getResourceManager());
                }
                return model;
            }
        };
    }

    private static IClientItemExtensions maskExtension(ModelLayerLocation layer,
                                                        ProtectiveMaskModel.Kind kind,
                                                        ResourceLocation[] overlay) {
        return new IClientItemExtensions() {
            private ProtectiveMaskModel model;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot,
                                                           HumanoidModel<?> original) {
                if (equipmentSlot != EquipmentSlot.HEAD) return original;
                if (model == null) {
                    model = new ProtectiveMaskModel(
                            Minecraft.getInstance().getEntityModels().bakeLayer(layer), kind);
                }
                model.setFilterVisible(!MaskFilterStorage.installed(itemStack).isEmpty());
                return model;
            }

            @Override
            public void renderHelmetOverlay(ItemStack stack, Player player, GuiGraphics graphics,
                                            DeltaTracker deltaTracker) {
                if (overlay == null) return;
                int maxDamage = Math.max(stack.getMaxDamage(), 1);
                int index = Math.min(stack.getDamageValue() * 6 / maxDamage, 5);
                ResourceLocation texture = overlay[index];
                graphics.blit(texture, 0, 0, -90, 0.0F, 0.0F,
                        graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight());
            }
        };
    }

    private static ResourceLocation[] overlays(String baseName) {
        ResourceLocation[] textures = new ResourceLocation[6];
        for (int index = 0; index < textures.length; index++) {
            textures[index] = ResourceLocation.fromNamespaceAndPath(
                    com.hbm.ntm.HbmNtm.MOD_ID, "textures/misc/" + baseName + "_" + index + ".png");
        }
        return textures;
    }
}
