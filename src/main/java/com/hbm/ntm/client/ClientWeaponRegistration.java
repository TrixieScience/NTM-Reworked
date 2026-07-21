package com.hbm.ntm.client;

import com.hbm.ntm.client.model.B92Model;
import com.hbm.ntm.client.model.B93Model;
import com.hbm.ntm.client.model.PowerFistModel;
import com.hbm.ntm.client.model.PowerFistRubbleModel;
import com.hbm.ntm.client.render.AutoShotgunItemRenderer;
import com.hbm.ntm.client.render.B92BeamRenderer;
import com.hbm.ntm.client.render.B92ItemRenderer;
import com.hbm.ntm.client.render.B93BeamRenderer;
import com.hbm.ntm.client.render.B93ItemRenderer;
import com.hbm.ntm.client.render.BlackHoleRenderer;
import com.hbm.ntm.client.render.BreakActionRevolverItemRenderer;
import com.hbm.ntm.client.render.BrokenMaresLegItemRenderer;
import com.hbm.ntm.client.render.BulletRenderer;
import com.hbm.ntm.client.render.BuildingRenderer;
import com.hbm.ntm.client.render.DualUziAkimboLayer;
import com.hbm.ntm.client.render.DualMaresLegItemRenderer;
import com.hbm.ntm.client.render.DualUziItemRenderer;
import com.hbm.ntm.client.render.DualStarFItemRenderer;
import com.hbm.ntm.client.render.FleijaRainbowCloudRenderer;
import com.hbm.ntm.client.render.FleijaCloudRenderer;
import com.hbm.ntm.client.render.FortyMillimeterGunItemRenderer;
import com.hbm.ntm.client.render.FortyMillimeterProjectileRenderer;
import com.hbm.ntm.client.render.G3ItemRenderer;
import com.hbm.ntm.client.render.HenryItemRenderer;
import com.hbm.ntm.client.render.HangmanItemRenderer;
import com.hbm.ntm.client.render.HeavyRevolverItemRenderer;
import com.hbm.ntm.client.render.LaserDetonatorItemRenderer;
import com.hbm.ntm.client.render.LaserPistolBeamRenderer;
import com.hbm.ntm.client.render.LaserPistolItemRenderer;
import com.hbm.ntm.client.render.LasrifleItemRenderer;
import com.hbm.ntm.client.render.FlattenedMobRenderer;
import com.hbm.ntm.client.render.LagPistolItemRenderer;
import com.hbm.ntm.client.render.LiberatorItemRenderer;
import com.hbm.ntm.client.render.MaresLegItemRenderer;
import com.hbm.ntm.client.render.MissileLauncherItemRenderer;
import com.hbm.ntm.client.render.NineMillimeterGunItemRenderer;
import com.hbm.ntm.client.render.PepperboxItemRenderer;
import com.hbm.ntm.client.render.PanzerschreckItemRenderer;
import com.hbm.ntm.client.render.QuadRocketLauncherItemRenderer;
import com.hbm.ntm.client.render.PowerFistBeamRenderer;
import com.hbm.ntm.client.render.PowerFistItemRenderer;
import com.hbm.ntm.client.render.PowerFistRubbleRenderer;
import com.hbm.ntm.client.render.ShredderBeamRenderer;
import com.hbm.ntm.client.render.ShredderItemRenderer;
import com.hbm.ntm.client.render.ShredderSubmunitionRenderer;
import com.hbm.ntm.client.render.SexyItemRenderer;
import com.hbm.ntm.client.render.SevenSixTwoGunItemRenderer;
import com.hbm.ntm.client.render.TwentyTwoGunItemRenderer;
import com.hbm.ntm.client.render.FlamerGunItemRenderer;
import com.hbm.ntm.client.render.RocketProjectileRenderer;
import com.hbm.ntm.client.render.SpasItemRenderer;
import com.hbm.ntm.client.render.StingerItemRenderer;
import com.hbm.ntm.client.render.Stg77ItemRenderer;
import com.hbm.ntm.client.render.M2ItemRenderer;
import com.hbm.ntm.client.render.AmatItemRenderer;
import com.hbm.ntm.client.render.TeslaBeamRenderer;
import com.hbm.ntm.client.render.TeslaImpactRenderer;
import com.hbm.ntm.client.render.TeslaCannonItemRenderer;
import com.hbm.ntm.client.render.TauBeamRenderer;
import com.hbm.ntm.client.render.TauGunItemRenderer;
import com.hbm.ntm.client.render.CoilgunItemRenderer;
import com.hbm.ntm.network.DetonatorInfoPayload;
import com.hbm.ntm.item.PenanceItem;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class ClientWeaponRegistration {
    private ClientWeaponRegistration() { }

    private static IClientItemExtensions weaponExtension(BlockEntityWithoutLevelRenderer renderer) {
        return new IClientItemExtensions() {
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
            @Override public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand,
                                                               ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            @Override public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                               HumanoidArm arm, ItemStack stack,
                                                               float partialTick, float equipProgress,
                                                               float swingProgress) {
                return true;
            }
        };
    }

    public static void register(IEventBus modEventBus) {
        DetonatorInfoPayload.installClientHandler(ClientWeaponEvents::showDetonatorInfo);
        ClientWeaponEvents.register();
        ClientPenanceEvents.register();
        modEventBus.addListener(ClientWeaponRegistration::registerRenderers);
        modEventBus.addListener(ClientWeaponRegistration::registerModels);
        modEventBus.addListener(ClientWeaponRegistration::registerLayers);
        modEventBus.addListener(ClientWeaponRegistration::addPlayerLayers);
        modEventBus.addListener(ClientWeaponRegistration::registerKeys);
        modEventBus.addListener(ClientWeaponRegistration::registerReloadListeners);
        modEventBus.addListener(ClientWeaponRegistration::registerExtensions);
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener)
                FortyMillimeterGunItemRenderer::validateAnimationResources);
        event.registerReloadListener((ResourceManagerReloadListener)
                FlamerGunItemRenderer::validateAnimationResources);
        event.registerReloadListener((ResourceManagerReloadListener)
                Stg77ItemRenderer::validateAnimationResources);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BULLET.get(), BulletRenderer::new);
        event.registerEntityRenderer(ModEntities.BUILDING.get(), BuildingRenderer::new);
        event.registerEntityRenderer(ModEntities.FORTY_MILLIMETER_PROJECTILE.get(),
                FortyMillimeterProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.FLAME_PROJECTILE.get(),
                net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.ROCKET_PROJECTILE.get(), RocketProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.LINGERING_FIRE.get(),
                net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.B92_BEAM.get(), B92BeamRenderer::new);
        event.registerEntityRenderer(ModEntities.B93_BEAM.get(), B93BeamRenderer::new);
        event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(ModEntities.VORTEX.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(ModEntities.RAGING_VORTEX.get(), BlackHoleRenderer::new);
        event.registerEntityRenderer(ModEntities.FLEIJA_EXPLOSION.get(), net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FLEIJA_RAINBOW_CLOUD.get(), FleijaRainbowCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.FLEIJA_CLOUD.get(), FleijaCloudRenderer::new);
        event.registerEntityRenderer(ModEntities.POWER_FIST_LASER_BEAM.get(),
                context -> new PowerFistBeamRenderer(context, PowerFistBeamRenderer.Kind.LASER));
        event.registerEntityRenderer(ModEntities.POWER_FIST_MINER_BEAM.get(),
                context -> new PowerFistBeamRenderer(context, PowerFistBeamRenderer.Kind.MINER));
        event.registerEntityRenderer(ModEntities.POWER_FIST_RUBBLE.get(), PowerFistRubbleRenderer::new);
        event.registerEntityRenderer(ModEntities.FLATTENED_MOB.get(), FlattenedMobRenderer::new);
        event.registerEntityRenderer(ModEntities.SHREDDER_BEAM.get(), ShredderBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.SHREDDER_SUBMUNITION.get(), ShredderSubmunitionRenderer::new);
        event.registerEntityRenderer(ModEntities.TESLA_BEAM.get(), TeslaBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.TESLA_IMPACT.get(), TeslaImpactRenderer::new);
        event.registerEntityRenderer(ModEntities.LASER_PISTOL_BEAM.get(), LaserPistolBeamRenderer::new);
        event.registerEntityRenderer(ModEntities.TAU_BEAM.get(), TauBeamRenderer::new);
    }

    private static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(PepperboxItemRenderer.GRIP);
        event.register(PepperboxItemRenderer.CYLINDER);
        event.register(PepperboxItemRenderer.HAMMER);
        event.register(PepperboxItemRenderer.TRIGGER);
        event.register(PepperboxItemRenderer.SPEEDLOADER);
        event.register(PepperboxItemRenderer.SHOT);
        event.register(BreakActionRevolverItemRenderer.BARREL);
        event.register(BreakActionRevolverItemRenderer.LATCH);
        event.register(BreakActionRevolverItemRenderer.HAMMER);
        event.register(BreakActionRevolverItemRenderer.DRUM);
        event.register(BreakActionRevolverItemRenderer.GRIP);
        event.register(BreakActionRevolverItemRenderer.BARREL_ATLAS);
        event.register(BreakActionRevolverItemRenderer.LATCH_ATLAS);
        event.register(BreakActionRevolverItemRenderer.HAMMER_ATLAS);
        event.register(BreakActionRevolverItemRenderer.DRUM_ATLAS);
        event.register(BreakActionRevolverItemRenderer.GRIP_ATLAS);
        event.register(HenryItemRenderer.GUN);
        event.register(HenryItemRenderer.SIGHT);
        event.register(HenryItemRenderer.HAMMER);
        event.register(HenryItemRenderer.LEVER);
        event.register(HenryItemRenderer.FRONT);
        event.register(HenryItemRenderer.BULLET);
        event.register(HenryItemRenderer.GUN_LINCOLN);
        event.register(HenryItemRenderer.SIGHT_LINCOLN);
        event.register(HenryItemRenderer.HAMMER_LINCOLN);
        event.register(HenryItemRenderer.LEVER_LINCOLN);
        event.register(HenryItemRenderer.FRONT_LINCOLN);
        event.register(HenryItemRenderer.BULLET_LINCOLN);
        event.register(NineMillimeterGunItemRenderer.GREASE_GUN);
        event.register(NineMillimeterGunItemRenderer.GREASE_STOCK);
        event.register(NineMillimeterGunItemRenderer.GREASE_MAGAZINE);
        event.register(NineMillimeterGunItemRenderer.GREASE_BULLET);
        event.register(NineMillimeterGunItemRenderer.GREASE_HANDLE);
        event.register(NineMillimeterGunItemRenderer.GREASE_FLAP);
        event.register(NineMillimeterGunItemRenderer.UZI_GUN);
        event.register(NineMillimeterGunItemRenderer.UZI_STOCK_FRONT);
        event.register(NineMillimeterGunItemRenderer.UZI_STOCK_BACK);
        event.register(NineMillimeterGunItemRenderer.UZI_SLIDE);
        event.register(NineMillimeterGunItemRenderer.UZI_MAGAZINE);
        event.register(NineMillimeterGunItemRenderer.UZI_BULLET);
        event.register(DualUziItemRenderer.UZI_GUN_MIRROR);
        event.register(MaresLegItemRenderer.GUN);
        event.register(MaresLegItemRenderer.STOCK);
        event.register(MaresLegItemRenderer.BARREL);
        event.register(MaresLegItemRenderer.LEVER);
        event.register(MaresLegItemRenderer.SHELL);
        event.register(BrokenMaresLegItemRenderer.GUN_BROKEN);
        event.register(BrokenMaresLegItemRenderer.LEVER_BROKEN);
        event.register(BrokenMaresLegItemRenderer.SHELL_BROKEN);
        event.register(LiberatorItemRenderer.GUN);
        event.register(LiberatorItemRenderer.BARREL);
        event.register(LiberatorItemRenderer.SHELL1);
        event.register(LiberatorItemRenderer.SHELL2);
        event.register(LiberatorItemRenderer.SHELL3);
        event.register(LiberatorItemRenderer.SHELL4);
        event.register(LiberatorItemRenderer.LATCH);
        event.register(AutoShotgunItemRenderer.GUN);
        event.register(AutoShotgunItemRenderer.MAGAZINE);
        event.register(AutoShotgunItemRenderer.SHELLS);
        event.register(ShredderItemRenderer.GUN);
        event.register(ShredderItemRenderer.MAGAZINE);
        event.register(ShredderItemRenderer.SHELLS);
        event.register(SexyItemRenderer.GUN);
        event.register(SexyItemRenderer.BARREL);
        event.register(SexyItemRenderer.RECOIL_SPRING);
        event.register(SexyItemRenderer.HOOD);
        event.register(SexyItemRenderer.LEVER);
        event.register(SexyItemRenderer.LOCK_SPRING);
        event.register(SexyItemRenderer.MAGAZINE);
        event.register(SexyItemRenderer.SHELL);
        event.register(SexyItemRenderer.BELT);
        event.register(SexyItemRenderer.WHISKEY);
        event.register(B92ItemRenderer.ICON);
        event.register(B93ItemRenderer.ICON);
        event.register(LaserDetonatorItemRenderer.MAIN);
        event.register(LaserDetonatorItemRenderer.LIGHTS);
        event.register(PowerFistItemRenderer.FIST_ICON);
        event.register(PowerFistItemRenderer.CLAW_ICON);
        event.register(PowerFistItemRenderer.OPEN_ICON);
        event.register(PowerFistItemRenderer.POINTER_ICON);
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(B92Model.LAYER, B92Model::createBodyLayer);
        event.registerLayerDefinition(B93Model.LAYER, B93Model::createBodyLayer);
        event.registerLayerDefinition(PowerFistModel.FIST_LAYER, PowerFistModel::createFistLayer);
        event.registerLayerDefinition(PowerFistModel.CLAW_LAYER, PowerFistModel::createClawLayer);
        event.registerLayerDefinition(PowerFistModel.OPEN_LAYER, PowerFistModel::createOpenLayer);
        event.registerLayerDefinition(PowerFistModel.POINTER_LAYER, PowerFistModel::createPointerLayer);
        event.registerLayerDefinition(PowerFistRubbleModel.LAYER, PowerFistRubbleModel::createLayer);
        event.registerLayerDefinition(com.hbm.ntm.client.model.ShrapnelModel.LAYER,
                com.hbm.ntm.client.model.ShrapnelModel::createLayer);
    }

    private static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new DualUziAkimboLayer(
                        renderer, event.getContext().getItemInHandRenderer()));
            }
        }
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ClientWeaponEvents.RELOAD);
        event.register(ClientWeaponEvents.AIM);
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private PowerFistItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new PowerFistItemRenderer();
                return renderer;
            }

            @Override
            public boolean shouldBobAsEntity(ItemStack stack) {
                // The Power Fist does not bob for anyone.
                return false;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                B92ItemRenderer.applyLegacyFirstPersonTransform(
                        poses, player, arm, stack, partialTick, equipProgress, swingProgress);
                return true;
            }
        }, ModItems.MULTITOOL_DIG.get(), ModItems.MULTITOOL_SILK.get(),
                ModItems.MULTITOOL_EXT.get(), ModItems.MULTITOOL_MINER.get(),
                ModItems.MULTITOOL_HIT.get(), ModItems.MULTITOOL_BEAM.get(),
                ModItems.MULTITOOL_SKY.get(), ModItems.MULTITOOL_MEGA.get(),
                ModItems.MULTITOOL_JOULE.get(), ModItems.MULTITOOL_DECON.get(),
                ModItems.MULTITOOL_PANE.get());

        event.registerItem(new IClientItemExtensions() {
            private LaserDetonatorItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new LaserDetonatorItemRenderer();
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                // Both arms aim the detonator because one arm is not threatening enough.
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                B92ItemRenderer.applyLegacyFirstPersonTransform(
                        poses, player, arm, stack, partialTick, equipProgress, swingProgress);
                return true;
            }
        }, ModItems.DETONATOR_LASER.get());

        event.registerItem(new IClientItemExtensions() {
            private final PepperboxItemRenderer renderer = new PepperboxItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                // Guns borrow the bow pose to keep both arms attached.
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                // Sedna owns camera space. Vanilla swinging can wait outside.
                return true;
            }
        }, ModItems.GUN_PEPPERBOX.get());

        event.registerItem(new IClientItemExtensions() {
            private final BreakActionRevolverItemRenderer renderer = new BreakActionRevolverItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_LIGHT_REVOLVER.get(), ModItems.GUN_LIGHT_REVOLVER_ATLAS.get());

        event.registerItem(new IClientItemExtensions() {
            private final HeavyRevolverItemRenderer renderer = new HeavyRevolverItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_HEAVY_REVOLVER.get());

        event.registerItem(new IClientItemExtensions() {
            private final HangmanItemRenderer renderer = new HangmanItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_HANGMAN.get());

        event.registerItem(new IClientItemExtensions() {
            private final LagPistolItemRenderer renderer = new LagPistolItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_LAG.get());

        event.registerItem(new IClientItemExtensions() {
            private final HenryItemRenderer renderer = new HenryItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_HENRY.get(), ModItems.GUN_HENRY_LINCOLN.get());

        event.registerItem(new IClientItemExtensions() {
            private final NineMillimeterGunItemRenderer renderer = new NineMillimeterGunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_GREASEGUN.get(), ModItems.GUN_UZI.get());

        event.registerItem(new IClientItemExtensions() {
            private final FortyMillimeterGunItemRenderer renderer = new FortyMillimeterGunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_FLAREGUN.get(), ModItems.GUN_CONGOLAKE.get(), ModItems.GUN_MK108.get());

        event.registerItem(new IClientItemExtensions() {
            private final SevenSixTwoGunItemRenderer renderer = new SevenSixTwoGunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_CARBINE.get(), ModItems.GUN_MINIGUN.get(), ModItems.GUN_MAS36.get());

        event.registerItem(new IClientItemExtensions() {
            private final G3ItemRenderer renderer = new G3ItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_G3.get(), ModItems.GUN_G3_ZEBRA.get());

        event.registerItem(new IClientItemExtensions() {
            private final Stg77ItemRenderer renderer = new Stg77ItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                return true;
            }
        }, ModItems.GUN_STG77.get());

        event.registerItem(new IClientItemExtensions() {
            private final M2ItemRenderer renderer = new M2ItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }
            @Override public HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand,
                                                               ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            @Override public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                               HumanoidArm arm, ItemStack stack,
                                                               float partialTick, float equipProgress,
                                                               float swingProgress) {
                return true;
            }
        }, ModItems.GUN_M2.get());

        event.registerItem(weaponExtension(new AmatItemRenderer()), ModItems.GUN_AMAT.get());
        event.registerItem(weaponExtension(new TeslaCannonItemRenderer()), ModItems.GUN_TESLA_CANNON.get());
        event.registerItem(weaponExtension(new LaserPistolItemRenderer()), ModItems.GUN_LASER_PISTOL.get());
        event.registerItem(weaponExtension(new LaserPistolItemRenderer()),
                ModItems.GUN_LASER_PISTOL_PEW_PEW.get());
        event.registerItem(weaponExtension(new LaserPistolItemRenderer()),
                ModItems.GUN_LASER_PISTOL_MORNING_GLORY.get());
        event.registerItem(weaponExtension(new LasrifleItemRenderer()), ModItems.GUN_LASRIFLE.get());
        event.registerItem(weaponExtension(new TauGunItemRenderer()), ModItems.GUN_TAU.get());
        event.registerItem(weaponExtension(new CoilgunItemRenderer()), ModItems.GUN_COILGUN.get());
        event.registerItem(weaponExtension(new AmatItemRenderer(ResourceLocation.fromNamespaceAndPath(
                "hbm", "textures/models/weapons/amat_subtlety.png"))), ModItems.GUN_AMAT_SUBTLETY.get());
        event.registerItem(weaponExtension(new AmatItemRenderer(PenanceItem.PENANCE_TEXTURE, true)),
                ModItems.GUN_AMAT_PENANCE.get());

        event.registerItem(new IClientItemExtensions() {
            private final TwentyTwoGunItemRenderer renderer = new TwentyTwoGunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_AM180.get(), ModItems.GUN_STAR_F.get());

        event.registerItem(new IClientItemExtensions() {
            private final FlamerGunItemRenderer renderer = new FlamerGunItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_FLAMER.get(), ModItems.GUN_FLAMER_TOPAZ.get(),
                ModItems.GUN_FLAMER_DAYBREAKER.get());

        event.registerItem(new IClientItemExtensions() {
            private final PanzerschreckItemRenderer renderer = new PanzerschreckItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_PANZERSCHRECK.get());

        event.registerItem(new IClientItemExtensions() {
            private final StingerItemRenderer renderer = new StingerItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_STINGER.get());

        event.registerItem(new IClientItemExtensions() {
            private final QuadRocketLauncherItemRenderer renderer =
                    new QuadRocketLauncherItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_QUADRO.get());

        event.registerItem(new IClientItemExtensions() {
            private final MissileLauncherItemRenderer renderer =
                    new MissileLauncherItemRenderer();

            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() { return renderer; }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_MISSILE_LAUNCHER.get());

        event.registerItem(new IClientItemExtensions() {
            private final DualStarFItemRenderer renderer = new DualStarFItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_STAR_F_AKIMBO.get());

        event.registerItem(new IClientItemExtensions() {
            private final DualUziItemRenderer renderer = new DualUziItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_UZI_AKIMBO.get());

        event.registerItem(new IClientItemExtensions() {
            private final MaresLegItemRenderer renderer = new MaresLegItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_MARESLEG.get());

        event.registerItem(new IClientItemExtensions() {
            private final DualMaresLegItemRenderer renderer = new DualMaresLegItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_MARESLEG_AKIMBO.get());

        event.registerItem(new IClientItemExtensions() {
            private final BrokenMaresLegItemRenderer renderer = new BrokenMaresLegItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_MARESLEG_BROKEN.get());

        event.registerItem(new IClientItemExtensions() {
            private final LiberatorItemRenderer renderer = new LiberatorItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_LIBERATOR.get());

        event.registerItem(new IClientItemExtensions() {
            private final SpasItemRenderer renderer = new SpasItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_SPAS12.get());

        event.registerItem(new IClientItemExtensions() {
            private final AutoShotgunItemRenderer renderer = new AutoShotgunItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_AUTOSHOTGUN.get());

        event.registerItem(new IClientItemExtensions() {
            private final ShredderItemRenderer renderer = new ShredderItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_AUTOSHOTGUN_SHREDDER.get());

        event.registerItem(new IClientItemExtensions() {
            private final SexyItemRenderer renderer = new SexyItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entity,
                                                    InteractionHand hand, ItemStack stack) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player,
                                                   HumanoidArm arm, ItemStack stack,
                                                   float partialTick, float equipProgress,
                                                   float swingProgress) {
                return true;
            }
        }, ModItems.GUN_AUTOSHOTGUN_SEXY.get());

        event.registerItem(new IClientItemExtensions() {
            private B92ItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new B92ItemRenderer();
                return renderer;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                B92ItemRenderer.applyLegacyFirstPersonTransform(
                        poses, player, arm, stack, partialTick, equipProgress, swingProgress);
                return true;
            }
        }, ModItems.GUN_B92.get());

        event.registerItem(new IClientItemExtensions() {
            private B93ItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new B93ItemRenderer();
                return renderer;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poses, LocalPlayer player, HumanoidArm arm,
                                                   ItemStack stack, float partialTick,
                                                   float equipProgress, float swingProgress) {
                B92ItemRenderer.applyLegacyFirstPersonTransform(
                        poses, player, arm, stack, partialTick, equipProgress, swingProgress);
                return true;
            }
        }, ModItems.GUN_B93.get());
    }
}
