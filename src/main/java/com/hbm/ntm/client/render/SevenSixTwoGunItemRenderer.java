package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.SevenSixTwoGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/** Original grouped Carbine, Minigun, and MAS-36 models with their Sedna poses and buses. */
public final class SevenSixTwoGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation CARBINE_MODEL = id("models/weapons/carbine.obj");
    private static final ResourceLocation MINIGUN_MODEL = id("models/weapons/minigun.obj");
    private static final ResourceLocation MAS36_MODEL = id("models/weapons/mas36.obj");
    private static final ResourceLocation CARBINE_TEXTURE = id("textures/models/weapons/huntsman.png");
    private static final ResourceLocation MINIGUN_TEXTURE = id("textures/models/weapons/minigun.png");
    private static final ResourceLocation MAS36_TEXTURE = id("textures/models/weapons/mas36.png");

    private static final Set<String> CARBINE_GROUPS = Set.of(
            "Gun", "Slide", "Magazine", "Bullet", "IronSight", "Scope", "Bayonet");
    private static final Set<String> MINIGUN_GROUPS = Set.of("Gun", "Grip", "Barrels", "GunDual");
    private static final Set<String> MAS36_GROUPS = Set.of(
            "Gun", "Stock", "Bolt", "Bullet", "Clip", "Bullets", "Scope", "Bayonet");

    private final Map<SevenSixTwoGunItem.Variant, EnvsuitMesh> meshes =
            new EnumMap<>(SevenSixTwoGunItem.Variant.class);

    public SevenSixTwoGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof SevenSixTwoGunItem gun)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        long flashDuration = gun.variant() == SevenSixTwoGunItem.Variant.MINIGUN ? 50L : 75L;
        boolean flash = held && elapsed >= 0L && elapsed < flashDuration;

        poses.pushPose();
        setupContext(gun.variant(), context, poses);
        if (firstPerson) renderFirstPerson(stack, gun.variant(), poses, buffers, light, overlay);
        else renderStatic(gun.variant(), poses, buffers, light, overlay);
        if (flash) renderFlash(gun.variant(), firstPerson, poses, buffers, elapsed,
                ClientWeaponEvents.shotRandom(stack));
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, SevenSixTwoGunItem.Variant variant,
                                   PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        switch (variant) {
            case CARBINE -> renderCarbine(stack, poses, buffers, light, overlay);
            case MINIGUN -> renderMinigun(stack, poses, buffers, light, overlay);
            case MAS36 -> renderMas36(stack, poses, buffers, light, overlay);
        }
    }

    private void renderCarbine(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                                int light, int overlay) {
        CarbineAnimation animation = carbineAnimation(stack, animationTime(stack));
        poses.scale(0.5F, 0.5F, 0.5F);
        pivotX(poses, 0.0D, -1.0D, -2.0D, animation.equip.x);
        pivotX(poses, 0.0D, 0.0D, -2.0D, animation.lift.x);
        poses.translate(0.0D, 0.0D, animation.recoil.z);
        render(SevenSixTwoGunItem.Variant.CARBINE, "Gun", poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.slide.z);
        render(SevenSixTwoGunItem.Variant.CARBINE, "Slide", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        render(SevenSixTwoGunItem.Variant.CARBINE, "Magazine", poses, buffers, light, overlay);
        poses.translate(animation.rel.x, animation.rel.y, animation.rel.z);
        if (animation.bullet.x < 0.5D) {
            render(SevenSixTwoGunItem.Variant.CARBINE, "Bullet", poses, buffers, light, overlay);
        }
        poses.popPose();
        render(SevenSixTwoGunItem.Variant.CARBINE, "IronSight", poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 1.0D, 8.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.25D,
                WeaponSmokeRenderer.SEVEN_SIX_TWO, reloading(stack));
        poses.popPose();
    }

    private void renderMinigun(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                               int light, int overlay) {
        MinigunAnimation animation = minigunAnimation(stack, animationTime(stack));
        poses.scale(0.375F, 0.375F, 0.375F);
        pivotX(poses, 0.0D, 3.0D, -6.0D, animation.equip.x);
        poses.translate(0.0D, 0.0D, animation.recoil.z);
        render(SevenSixTwoGunItem.Variant.MINIGUN, "Gun", poses, buffers, light, overlay);
        render(SevenSixTwoGunItem.Variant.MINIGUN, "Grip", poses, buffers, light, overlay);
        poses.pushPose();
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.rotate.z));
        render(SevenSixTwoGunItem.Variant.MINIGUN, "Barrels", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(-2.0D, 1.25D, -3.5D);
        poses.mulPose(Axis.YP.rotationDegrees(45.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.5D,
                WeaponSmokeRenderer.SEVEN_SIX_TWO, reloading(stack));
        poses.popPose();
    }

    private void renderMas36(ItemStack stack, PoseStack poses, MultiBufferSource buffers,
                             int light, int overlay) {
        MasAnimation animation = masAnimation(stack, animationTime(stack));
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.translate(0.0D, -3.0D, -3.0D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.equip.x));
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.lift.x));
        poses.translate(0.0D, 3.0D, 3.0D);
        poses.translate(0.0D, 0.0D, animation.recoil.z);
        render(SevenSixTwoGunItem.Variant.MAS36, "Gun", poses, buffers, light, overlay);

        poses.pushPose();
        pivotX(poses, 0.0D, 0.3125D, -2.125D, animation.stock.x);
        render(SevenSixTwoGunItem.Variant.MAS36, "Stock", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 1.15625D, 0.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.boltTurn.z));
        poses.translate(0.0D, -1.15625D, 0.0D);
        poses.translate(0.0D, 0.0D, animation.boltPull.z);
        render(SevenSixTwoGunItem.Variant.MAS36, "Bolt", poses, buffers, light, overlay);
        poses.popPose();

        if (animation.bullet.x > -50.0D) {
            poses.pushPose();
            poses.translate(animation.bullet.x, animation.bullet.y, animation.bullet.z);
            render(SevenSixTwoGunItem.Variant.MAS36, "Bullet", poses, buffers, light, overlay);
            poses.popPose();
        }
        if (animation.showClip.x != 0.0D) {
            poses.pushPose();
            poses.translate(animation.clip.x, animation.clip.y, animation.clip.z);
            render(SevenSixTwoGunItem.Variant.MAS36, "Clip", poses, buffers, light, overlay);
            poses.popPose();
            poses.pushPose();
            poses.translate(animation.bullets.x, animation.bullets.y, animation.bullets.z);
            render(SevenSixTwoGunItem.Variant.MAS36, "Bullets", poses, buffers, light, overlay);
            poses.popPose();
        }

        poses.pushPose();
        poses.translate(0.0D, 1.125D, 8.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.25F, 0.25F, 0.25F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D,
                WeaponSmokeRenderer.SEVEN_SIX_TWO, reloading(stack));
        poses.popPose();
    }

    private static boolean reloading(ItemStack stack) {
        return SevenSixTwoGunItem.state(stack) == SevenSixTwoGunItem.GunState.RELOADING;
    }

    private void renderStatic(SevenSixTwoGunItem.Variant variant, PoseStack poses,
                              MultiBufferSource buffers, int light, int overlay) {
        Set<String> groups = switch (variant) {
            case CARBINE -> Set.of("Gun", "Slide", "Magazine", "IronSight");
            case MINIGUN -> Set.of("Gun", "Grip", "Barrels");
            case MAS36 -> Set.of("Gun", "Stock", "Bolt");
        };
        for (String group : groups) render(variant, group, poses, buffers, light, overlay);
    }

    private void render(SevenSixTwoGunItem.Variant variant, String group, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation texture = switch (variant) {
            case CARBINE -> CARBINE_TEXTURE;
            case MINIGUN -> MINIGUN_TEXTURE;
            case MAS36 -> MAS36_TEXTURE;
        };
        mesh(variant).render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh(SevenSixTwoGunItem.Variant variant) {
        return meshes.computeIfAbsent(variant, key -> switch (key) {
            case CARBINE -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    CARBINE_MODEL, CARBINE_GROUPS, "Carbine");
            case MINIGUN -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    MINIGUN_MODEL, MINIGUN_GROUPS, "Minigun");
            case MAS36 -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    MAS36_MODEL, MAS36_GROUPS, "MAS-36");
        });
    }

    private static void setupContext(SevenSixTwoGunItem.Variant variant,
                                     ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = switch (variant) {
                    case CARBINE -> 1.375F;
                    case MINIGUN -> 0.875F;
                    case MAS36 -> 1.5F;
                };
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                if (variant == SevenSixTwoGunItem.Variant.CARBINE) poses.translate(-0.5D, 0.0D, 0.0D);
                else if (variant == SevenSixTwoGunItem.Variant.MINIGUN) poses.translate(-0.25D, 0.5D, 0.0D);
                else poses.translate(-0.5D, 0.5D, 0.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(variant, context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                // ItemRenderWeaponBase applied this half turn before each concrete setupFirstPerson.
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.translate(0.0D, 0.0D, 0.875D);
                Vec hip = switch (variant) {
                    case CARBINE -> new Vec(side * 1.2D, -1.2D, 0.7D);
                    case MINIGUN -> new Vec(side * 1.4D, -1.4D, 2.8D);
                    case MAS36 -> new Vec(side * 1.2D, -1.0D, 1.4D);
                };
                Vec aimed = switch (variant) {
                    case CARBINE -> new Vec(0.0D, -0.78125D, 0.25D);
                    case MINIGUN -> new Vec(0.0D, -0.78125D, 1.0D);
                    case MAS36 -> new Vec(0.0D, -0.5853125D, 0.75D);
                };
                poses.translate(lerp(hip.x, aimed.x, aim), lerp(hip.y, aimed.y, aim),
                        lerp(hip.z, aimed.z, aim));
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(SevenSixTwoGunItem.Variant variant,
                                         ItemDisplayContext context, PoseStack poses) {
        float side = context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ? -1.0F : 1.0F;
        poses.translate(-side / 16.0D, -0.125D, 0.625D);
        poses.mulPose(Axis.YN.rotationDegrees(180.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F));
        poses.translate(-side / 16.0D, 0.4375D, 0.0625D);
        poses.translate(side * 0.25D, 0.1875D, -0.1875D);
        poses.scale(0.375F, 0.375F, 0.375F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 60.0F));
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 20.0F));
        poses.translate(0.0D, -0.3D, 0.0D);
        poses.scale(1.5F, 1.5F, 1.5F);
        poses.mulPose(Axis.YP.rotationDegrees(side * 50.0F));
        poses.mulPose(Axis.ZP.rotationDegrees(side * 335.0F));
        poses.translate(-side * 0.9375D, -0.0625D, 0.0D);
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.mulPose(Axis.ZP.rotationDegrees(side * 15.0F));
        poses.mulPose(Axis.YP.rotationDegrees(side * 12.5F));
        poses.mulPose(Axis.XP.rotationDegrees(15.0F));
        poses.translate(side * 3.5D, 0.0D, 0.0D);

        if (variant == SevenSixTwoGunItem.Variant.CARBINE) {
            poses.scale(1.375F, 1.375F, 1.375F);
            poses.translate(0.0D, 0.0D, 2.0D);
        } else if (variant == SevenSixTwoGunItem.Variant.MINIGUN) {
            poses.scale(1.75F, 1.75F, 1.75F);
            poses.translate(1.0D, -3.5D, 8.0D);
        } else {
            poses.scale(1.5F, 1.5F, 1.5F);
            poses.translate(0.0D, 0.5D, 3.0D);
        }
    }

    private static void renderFlash(SevenSixTwoGunItem.Variant variant, boolean firstPerson,
                                    PoseStack poses, MultiBufferSource buffers, long elapsed, float random) {
        float duration = variant == SevenSixTwoGunItem.Variant.MINIGUN ? 50.0F : 75.0F;
        float progress = elapsed / duration;
        poses.pushPose();
        if (variant == SevenSixTwoGunItem.Variant.MINIGUN) {
            poses.translate(0.0D, 0.5D, firstPerson ? 12.0D : 12.25D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.mulPose(Axis.XP.rotationDegrees(random * 90.0F));
            poses.scale(1.5F, 1.5F, 1.5F);
        } else {
            poses.translate(0.0D, 1.0D, 8.0D);
            poses.mulPose(Axis.YP.rotationDegrees(90.0F));
            poses.mulPose(Axis.XP.rotationDegrees(random * 90.0F));
            poses.scale(0.5F, 0.5F, 0.5F);
        }
        float baseLength = variant == SevenSixTwoGunItem.Variant.MAS36 && !firstPerson ? 10.0F : 7.5F;
        SednaMuzzleFlash.render(poses, buffers, progress, baseLength);
        poses.popPose();
    }

    private static CarbineAnimation carbineAnimation(ItemStack stack, double time) {
        boolean aiming = SevenSixTwoGunItem.aiming(stack);
        int ammo = SevenSixTwoGunItem.rounds(stack);
        return switch (SevenSixTwoGunItem.animation(stack)) {
            case EQUIP -> new CarbineAnimation(
                    seq(time, f(45,0,0,0), f(0,0,0,500,Curve.SIN_FULL)), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new CarbineAnimation(ZERO,
                    seq(time, f(0,0,aiming ? -0.25 : -0.5,50,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)),
                    seq(time, f(0,0,-1,50,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_UP)), ZERO, ZERO, ZERO,
                    ammo <= 1 ? ZERO : seq(time, f(0,0,0.25,50), f(0,0.125,1.25,100,Curve.SIN_UP)));
            case CYCLE_DRY -> new CarbineAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,500), f(0,0,-1,100,Curve.SIN_DOWN), f(0,0,-1,50), f(0,0,0,100,Curve.SIN_UP)),
                    ZERO, ZERO, ZERO, ZERO);
            case RELOAD -> new CarbineAnimation(ZERO, ZERO, ZERO,
                    seq(time, f(0,-4,0,250,Curve.SIN_UP), f(0,-4,0,750), f(0,0,0,500,Curve.SIN_DOWN)),
                    seq(time, f(0,0,0,500), f(-25,0,0,250,Curve.SIN_FULL), f(-25,0,0,1000)),
                    seq(time, f(ammo == 0 ? 1 : 0,0,0,0), f(0,0,0,1000)), ZERO);
            case RELOAD_END -> new CarbineAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-1,100,Curve.SIN_DOWN), f(0,0,-1,50), f(0,0,0,100,Curve.SIN_UP)),
                    ZERO, seq(time, f(-25,0,0,0), f(-25,0,0,750), f(0,0,0,500,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,250), f(0,0,0.25,150), f(0,0.125,1.25,100,Curve.SIN_UP)));
            case JAMMED -> new CarbineAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-1,100,Curve.SIN_DOWN), f(0,0,-1,50),
                            f(0,0,-0.25,100,Curve.SIN_UP), f(0,0,-0.25,1250), f(0,0,-1,100,Curve.SIN_DOWN),
                            f(0,0,-1,50), f(0,0,0,100,Curve.SIN_UP)), ZERO,
                    seq(time, f(-25,0,0,0), f(-25,0,0,750), f(0,0,0,500,Curve.SIN_FULL),
                            f(0,0,0,250), f(-25,0,0,250,Curve.SIN_FULL), f(-25,0,0,750),
                            f(0,0,0,500,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,250), f(0,0,0.25,150), f(0,0.125,1,100,Curve.SIN_UP),
                            f(0,0.125,1,1250), f(0,0.125,0.25,100,Curve.SIN_DOWN),
                            f(0,0.125,1,100,Curve.SIN_UP)));
            case INSPECT -> new CarbineAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,500), f(0,0,-0.75,150,Curve.SIN_DOWN), f(0,0,-0.75,1000),
                            f(0,0,0,100,Curve.SIN_UP)), ZERO,
                    seq(time, f(-25,0,0,250,Curve.SIN_FULL), f(-25,0,0,1500), f(0,0,0,500,Curve.SIN_FULL)),
                    ZERO, ammo == 0 ? ZERO : seq(time, f(0,0.125,1.25,0), f(0,0.125,1.25,500),
                            f(0,0.125,0.5,150,Curve.SIN_DOWN), f(0,0.125,0.5,1000),
                            f(0,0.125,1.25,100,Curve.SIN_UP)));
            default -> CarbineAnimation.NONE;
        };
    }

    private static MinigunAnimation minigunAnimation(ItemStack stack, double time) {
        boolean aiming = SevenSixTwoGunItem.aiming(stack);
        return switch (SevenSixTwoGunItem.animation(stack)) {
            case EQUIP -> new MinigunAnimation(
                    seq(time, f(45,0,0,0), f(0,0,0,1000,Curve.SIN_FULL)), ZERO, ZERO);
            case CYCLE -> new MinigunAnimation(ZERO,
                    seq(time, f(0,0,aiming ? -0.25 : -0.5,0), f(0,0,aiming ? -0.25 : -0.5,100),
                            f(0,0,0,150,Curve.SIN_FULL)),
                    seq(time, f(0,0,60,50), f(0,0,720,1000,Curve.SIN_DOWN)));
            case CYCLE_DRY -> new MinigunAnimation(ZERO, ZERO,
                    seq(time, f(0,0,60,50), f(0,0,720,1000,Curve.SIN_DOWN)));
            case RELOAD -> new MinigunAnimation(
                    seq(time, f(-15,0,0,250,Curve.SIN_DOWN), f(0,0,0,500,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,60,50), f(0,0,720,1000,Curve.SIN_DOWN)));
            case INSPECT -> new MinigunAnimation(
                    seq(time, f(3,0,0,150,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,-720,1000,Curve.SIN_DOWN)));
            default -> MinigunAnimation.NONE;
        };
    }

    private static MasAnimation masAnimation(ItemStack stack, double time) {
        boolean aiming = SevenSixTwoGunItem.aiming(stack);
        int mag = SevenSixTwoGunItem.rounds(stack);
        double pull = aiming ? -1.0D : -1.5D;
        return switch (SevenSixTwoGunItem.animation(stack)) {
            case EQUIP -> new MasAnimation(
                    seq(time, f(45,0,0,0), f(0,0,0,500,Curve.SIN_FULL), f(0,0,0,500),
                            f(1,0,0,100,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)), ZERO,
                    seq(time, f(-158,0,0,0), f(-158,0,0,500), f(0,0,0,500,Curve.SIN_FULL)),
                    ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO);
            case CYCLE -> new MasAnimation(ZERO,
                    seq(time, f(0,0,-0.5,50,Curve.SIN_DOWN), f(0,0,0,100,Curve.SIN_FULL)), ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-90,150), f(0,0,-90,700), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP), f(0,0,pull,250), f(0,0,0,200)),
                    seq(time, f(0,0,0,600), f(-3,0,0,150,Curve.SIN_DOWN), f(-3,0,0,300),
                            f(0,0,0,250,Curve.SIN_FULL)),
                    mag <= 1 ? new Vec(-100,0,0) : seq(time, f(0,0,0,850), f(0,0.1875,1.5,200)),
                    ZERO,ZERO,ZERO);
            case CYCLE_DRY -> new MasAnimation(ZERO,ZERO,ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-90,150), f(0,0,-90,700), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP), f(0,0,pull,250), f(0,0,0,200)),
                    seq(time, f(0,0,0,600), f(-3,0,0,150,Curve.SIN_DOWN), f(-3,0,0,300),
                            f(0,0,0,250,Curve.SIN_FULL)), new Vec(-100,0,0),ZERO,ZERO,ZERO);
            case RELOAD -> new MasAnimation(ZERO,ZERO,ZERO,
                    seq(time, f(0,0,-90,150), f(0,0,-90,1850), f(0,0,0,150)),
                    seq(time, f(0,0,0,100), f(0,0,-1.5,250,Curve.SIN_UP), f(0,0,-1.5,1450), f(0,0,0,200)),
                    seq(time, f(0,0,0,200), f(30,0,0,500,Curve.SIN_FULL), f(30,0,0,500),
                            f(0,0,0,500,Curve.SIN_FULL)),
                    seq(time, f(-100,0,0,0), f(-100,0,0,1200), f(0,0,0,0), f(0,0,0,600),
                            f(0,0.1875,1.5,200)), new Vec(1,1,1),
                    seq(time, f(2,-3,0,0), f(2,-3,0,250), f(0.5,1,0,500,Curve.SIN_DOWN),
                            f(0,0,0,250,Curve.SIN_FULL), f(0,0,0,400), f(-0.5,0.5,0,150),
                            f(-3,-3,0,250,Curve.SIN_UP)),
                    seq(time, f(2,-3,0,0), f(2,-3,0,250), f(0.5,1,0,500,Curve.SIN_DOWN),
                            f(0,0,0,250,Curve.SIN_FULL), f(0,0,0,150), f(0,-1.5,0,250,Curve.SIN_DOWN)));
            case JAMMED -> new MasAnimation(ZERO,ZERO,ZERO,
                    seq(time, f(0,0,0,250), f(0,0,-90,150), f(0,0,-90,850), f(0,0,0,150)),
                    seq(time, f(0,0,0,350), f(0,0,pull,250,Curve.SIN_UP), f(0,0,0,200),
                            f(0,0,pull,250,Curve.SIN_UP), f(0,0,0,200)),
                    seq(time, f(0,0,0,250), f(-15,0,0,500,Curve.SIN_FULL), f(-15,0,0,900),
                            f(0,0,0,500,Curve.SIN_FULL)), ZERO,ZERO,ZERO,ZERO);
            case INSPECT -> new MasAnimation(ZERO,ZERO,ZERO,
                    seq(time, f(0,0,-90,150), f(0,0,-90,900), f(0,0,0,150)),
                    seq(time, f(0,0,0,100), f(0,0,-1,250,Curve.SIN_UP), f(0,0,-1,500), f(0,0,0,200)),
                    seq(time, f(0,0,0,350), f(-3,0,0,150,Curve.SIN_DOWN), f(-3,0,0,550),
                            f(0,0,0,250,Curve.SIN_FULL)),
                    mag == 0 ? new Vec(-100,0,0) : seq(time, f(0,0.1875,1.5,0), f(0,0.1875,1.5,100),
                            f(0,0.125,0.5,250,Curve.SIN_UP), f(0,0.125,0.5,500),
                            f(0,0.1875,1.5,200)), ZERO,ZERO,ZERO);
            default -> MasAnimation.NONE;
        };
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (SevenSixTwoGunItem.animationTimer(stack) + partial) * 50.0D;
    }

    private static Vec seq(double time, Frame... frames) {
        double elapsed = 0.0D;
        Vec previous = ZERO;
        for (Frame frame : frames) {
            if (frame.duration <= 0.0D) {
                previous = frame.value;
                continue;
            }
            if (time < elapsed + frame.duration) {
                double progress = Math.max(0.0D, Math.min((time - elapsed) / frame.duration, 1.0D));
                return previous.lerp(frame.value, frame.curve.apply(progress));
            }
            elapsed += frame.duration;
            previous = frame.value;
        }
        return previous;
    }

    private static Frame f(double x, double y, double z, double duration) {
        return f(x, y, z, duration, Curve.LINEAR);
    }
    private static Frame f(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x, y, z), duration, curve);
    }
    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    private static final Vec ZERO = new Vec(0,0,0);
    private record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double progress) {
            return new Vec(SevenSixTwoGunItemRenderer.lerp(x, other.x, progress),
                    SevenSixTwoGunItemRenderer.lerp(y, other.y, progress),
                    SevenSixTwoGunItemRenderer.lerp(z, other.z, progress));
        }
    }
    private record Frame(Vec value, double duration, Curve curve) { }
    private enum Curve {
        LINEAR { double apply(double x) { return x; } },
        SIN_UP { double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    private record CarbineAnimation(Vec equip, Vec recoil, Vec slide, Vec mag, Vec lift, Vec bullet, Vec rel) {
        private static final CarbineAnimation NONE = new CarbineAnimation(ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO);
    }
    private record MinigunAnimation(Vec equip, Vec recoil, Vec rotate) {
        private static final MinigunAnimation NONE = new MinigunAnimation(ZERO,ZERO,ZERO);
    }
    private record MasAnimation(Vec equip, Vec recoil, Vec stock, Vec boltTurn, Vec boltPull,
                                Vec lift, Vec bullet, Vec showClip, Vec clip, Vec bullets) {
        private static final MasAnimation NONE = new MasAnimation(
                ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO);
    }
}
