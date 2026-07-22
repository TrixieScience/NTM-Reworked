package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.TwentyTwoGunItem;
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

/** Shared wardrobe department for the AM180 and Star. */
public final class TwentyTwoGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    static final ResourceLocation STAR_MODEL = id("models/weapons/star_f.obj");
    static final ResourceLocation STAR_TEXTURE = id("textures/models/weapons/star_f.png");
    static final ResourceLocation STAR_ELITE_TEXTURE = id("textures/models/weapons/star_f_elite.png");
    static final Set<String> STAR_GROUPS = Set.of("Gun", "Hammer", "Slide", "Mag", "Bullet");

    private static final ResourceLocation AM180_MODEL = id("models/weapons/am180.obj");
    private static final ResourceLocation AM180_TEXTURE = id("textures/models/weapons/am180.png");
    private static final Set<String> AM180_GROUPS = Set.of(
            "Gun", "Trigger", "Bolt", "Mag", "MagPlate", "Silencer");

    private final Map<TwentyTwoGunItem.Variant, EnvsuitMesh> meshes =
            new EnumMap<>(TwentyTwoGunItem.Variant.class);

    public TwentyTwoGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof TwentyTwoGunItem gun)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        long duration = gun.variant() == TwentyTwoGunItem.Variant.AM180 ? 50L : 75L;

        poses.pushPose();
        setupContext(gun.variant(), context, poses);
        if (firstPerson) {
            if (gun.variant() == TwentyTwoGunItem.Variant.AM180) {
                renderAm180FirstPerson(stack, poses, buffers, light, overlay);
            } else {
                renderStarFirstPerson(stack, poses, buffers, light, overlay);
            }
        } else {
            renderStatic(gun.variant(), poses, buffers, light, overlay);
        }
        if (held && elapsed >= 0L && elapsed < duration) {
            if (gun.variant() == TwentyTwoGunItem.Variant.AM180) {
                renderAm180Flash(poses, buffers, elapsed / (float) duration,
                        ClientWeaponEvents.shotRandom(stack));
            } else {
                renderStarFlash(poses, buffers, elapsed / (float) duration,
                        ClientWeaponEvents.shotRandom(stack));
            }
        }
        poses.popPose();
    }

    private void renderAm180FirstPerson(ItemStack stack, PoseStack poses,
                                        MultiBufferSource buffers, int light, int overlay) {
        Am180Animation animation = am180Animation(stack);
        poses.scale(0.1875F, 0.1875F, 0.1875F);
        pivotX(poses, 0.0D, -2.0D, -6.0D, animation.equip.x);
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.turn.z));
        poses.translate(0.0D, 0.0D, animation.recoil.z);

        render(TwentyTwoGunItem.Variant.AM180, "Gun", poses, buffers, light, overlay);
        render(TwentyTwoGunItem.Variant.AM180, "Trigger", poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.bolt.z);
        render(TwentyTwoGunItem.Variant.AM180, "Bolt", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        poses.translate(0.0D, 2.0625D, 3.75D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.magTurn.x));
        poses.mulPose(Axis.ZP.rotationDegrees((float) animation.magTurn.z));
        poses.translate(0.0D, -2.0625D, -3.75D);
        poses.translate(0.0D, 2.3125D, 1.5D);
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.magSpin.x));
        poses.translate(0.0D, -2.3125D, -1.5D);
        poses.pushPose();
        poses.translate(0.0D, 0.0D, 1.5D);
        poses.mulPose(Axis.YN.rotationDegrees(TwentyTwoGunItem.rounds(stack) / 59.0F * 360.0F));
        poses.translate(0.0D, 0.0D, -1.5D);
        render(TwentyTwoGunItem.Variant.AM180, "Mag", poses, buffers, light, overlay);
        poses.popPose();
        render(TwentyTwoGunItem.Variant.AM180, "MagPlate", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 1.875D, 13.0D);
        poses.mulPose(Axis.ZN.rotationDegrees((float) animation.turn.z));
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.25D,
                WeaponSmokeRenderer.TWENTY_TWO, reloading(stack));
        poses.popPose();
    }

    private void renderStarFirstPerson(ItemStack stack, PoseStack poses,
                                       MultiBufferSource buffers, int light, int overlay) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        double time = (TwentyTwoGunItem.animationTimer(stack) + partial) * 50.0D;
        StarAnimation animation = starAnimation(TwentyTwoGunItem.animation(stack).ordinal(), time,
                TwentyTwoGunItem.aiming(stack), TwentyTwoGunItem.rounds(stack));
        poses.scale(0.25F, 0.25F, 0.25F);
        renderStarAnimated(mesh(TwentyTwoGunItem.Variant.STAR_F), STAR_TEXTURE,
                animation, 1, poses, buffers, light, overlay);

        poses.pushPose();
        poses.translate(0.0D, 3.0D, 6.125D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 0.75D,
                WeaponSmokeRenderer.TWENTY_TWO, reloading(stack));
        poses.popPose();
    }

    private static boolean reloading(ItemStack stack) {
        return TwentyTwoGunItem.state(stack) == TwentyTwoGunItem.GunState.RELOADING;
    }

    static void renderStarAnimated(EnvsuitMesh mesh, ResourceLocation texture,
                                   StarAnimation animation, int turnDirection, PoseStack poses,
                                   MultiBufferSource buffers, int light, int overlay) {
        pivotX(poses, 0.0D, -2.0D, -8.0D, animation.equip.x);
        poses.translate(0.0D, 1.0D, -3.0D);
        poses.mulPose(Axis.ZP.rotationDegrees((float) (animation.turn.z * turnDirection)));
        poses.mulPose(Axis.XP.rotationDegrees((float) animation.tilt.x));
        poses.translate(0.0D, -1.0D, 3.0D);
        poses.translate(0.0D, 0.0D, animation.recoil.z);

        render(mesh, texture, "Gun", poses, buffers, light, overlay);
        poses.pushPose();
        poses.translate(0.0D, 1.75D, -4.25D);
        poses.mulPose(Axis.XP.rotationDegrees((float) (60.0D * (animation.hammer.x - 1.0D))));
        poses.translate(0.0D, -1.75D, 4.25D);
        render(mesh, texture, "Hammer", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.slide.z * 2.3125D);
        render(mesh, texture, "Slide", poses, buffers, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.mag.x, animation.mag.y, animation.mag.z);
        render(mesh, texture, "Mag", poses, buffers, light, overlay);
        poses.translate(animation.bullet.x, animation.bullet.y, animation.bullet.z);
        if (animation.bullet.x < 50.0D) {
            render(mesh, texture, "Bullet", poses, buffers, light, overlay);
        }
        poses.popPose();
    }

    private void renderStatic(TwentyTwoGunItem.Variant variant, PoseStack poses,
                              MultiBufferSource buffers, int light, int overlay) {
        if (variant == TwentyTwoGunItem.Variant.AM180) {
            for (String group : Set.of("Gun", "Trigger", "Bolt", "Mag", "MagPlate")) {
                render(variant, group, poses, buffers, light, overlay);
            }
        } else {
            renderStarStatic(mesh(variant), STAR_TEXTURE, poses, buffers, light, overlay);
        }
    }

    static void renderStarStatic(EnvsuitMesh mesh, ResourceLocation texture, PoseStack poses,
                                 MultiBufferSource buffers, int light, int overlay) {
        for (String group : Set.of("Gun", "Slide", "Mag", "Hammer")) {
            render(mesh, texture, group, poses, buffers, light, overlay);
        }
    }

    private void render(TwentyTwoGunItem.Variant variant, String group, PoseStack poses,
                        MultiBufferSource buffers, int light, int overlay) {
        ResourceLocation texture = variant == TwentyTwoGunItem.Variant.AM180
                ? AM180_TEXTURE : STAR_TEXTURE;
        render(mesh(variant), texture, group, poses, buffers, light, overlay);
    }

    static void render(EnvsuitMesh mesh, ResourceLocation texture, String group, PoseStack poses,
                       MultiBufferSource buffers, int light, int overlay) {
        mesh.render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, -1);
    }

    private EnvsuitMesh mesh(TwentyTwoGunItem.Variant variant) {
        return meshes.computeIfAbsent(variant, key -> key == TwentyTwoGunItem.Variant.AM180
                ? EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                        AM180_MODEL, AM180_GROUPS, "American-180")
                : loadStarMesh());
    }

    static EnvsuitMesh loadStarMesh() {
        return EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                STAR_MODEL, STAR_GROUPS, "Star F");
    }

    private static void setupContext(TwentyTwoGunItem.Variant variant,
                                     ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = variant == TwentyTwoGunItem.Variant.AM180 ? 0.75F : 1.5F;
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                if (variant == TwentyTwoGunItem.Variant.AM180) poses.translate(1.5D, 0.0D, 0.0D);
                else poses.translate(-1.0D, -0.5D, 0.0D);
            }
            case GROUND, FIXED -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND ->
                    setupThirdPerson(variant, context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.translate(0.0D, 0.0D, 0.875D);
                Vec hip = variant == TwentyTwoGunItem.Variant.AM180
                        ? new Vec(side * 0.8D, -0.8D, 0.8D)
                        : new Vec(side * 1.4D, -1.4D, 2.0D);
                Vec aimed = variant == TwentyTwoGunItem.Variant.AM180
                        ? new Vec(0.0D, -0.5234375D, 0.25D)
                        : new Vec(0.0D, -0.953125D, 1.0D);
                poses.translate(lerp(hip.x, aimed.x, aim), lerp(hip.y, aimed.y, aim),
                        lerp(hip.z, aimed.z, aim));
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(TwentyTwoGunItem.Variant variant,
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
        if (variant == TwentyTwoGunItem.Variant.AM180) {
            poses.translate(0.0D, -0.5D, 3.0D);
        } else {
            poses.translate(0.0D, -0.25D, 1.75D);
            poses.scale(0.75F, 0.75F, 0.75F);
        }
    }

    private static void renderAm180Flash(PoseStack poses, MultiBufferSource buffers,
                                         float progress, float random) {
        poses.pushPose();
        poses.translate(0.0D, 1.875D, 12.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F * random));
        poses.scale(0.75F, 0.75F, 0.75F);
        SednaMuzzleFlash.render(poses, buffers, progress, 7.5F);
        poses.popPose();
    }

    static void renderStarFlash(PoseStack poses, MultiBufferSource buffers,
                                float progress, float random) {
        poses.pushPose();
        poses.translate(0.0D, 3.0D, 6.125D);
        poses.scale(0.75F, 0.75F, 0.75F);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(90.0F * random));
        SednaMuzzleFlash.render(poses, buffers, progress, 7.5F);
        poses.popPose();
    }

    private static Am180Animation am180Animation(ItemStack stack) {
        double time = animationTime(stack);
        boolean aiming = TwentyTwoGunItem.aiming(stack);
        return switch (TwentyTwoGunItem.animation(stack)) {
            case EQUIP -> new Am180Animation(
                    sequence(time, frame(45,0,0,0), frame(0,0,0,500,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new Am180Animation(ZERO,
                    sequence(time, frame(0,0,aiming ? -0.125D : -0.25D,15,Curve.SIN_DOWN),
                            frame(0,0,0,35,Curve.SIN_FULL)),
                    ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new Am180Animation(ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,550), frame(0,0,-1.5D,100,Curve.SIN_UP),
                            frame(0,0,0,100,Curve.SIN_UP)),
                    sequence(time, frame(0,0,0,300), frame(0,0,15,250,Curve.SIN_FULL),
                            frame(0,0,15,400), frame(0,0,0,250,Curve.SIN_FULL)));
            case RELOAD -> new Am180Animation(ZERO, ZERO,
                    sequence(time, frame(0,0,0,250), frame(2,0,-4,250,Curve.SIN_FULL),
                            frame(-10,2,-4,300,Curve.SIN_UP), frame(3,-6,-4,0),
                            frame(2,0,-4,500,Curve.SIN_FULL), frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(15,0,0,250,Curve.SIN_FULL), frame(15,0,0,250),
                            frame(15,0,70,300,Curve.SIN_FULL), frame(15,0,0,0),
                            frame(15,0,0,750), frame(0,0,0,250,Curve.SIN_FULL)), ZERO,
                    sequence(time, frame(0,0,0,2250), frame(0,0,-1.5D,100,Curve.SIN_UP),
                            frame(0,0,0,100,Curve.SIN_UP)),
                    sequence(time, frame(0,0,0,2000), frame(0,0,15,250,Curve.SIN_FULL),
                            frame(0,0,15,400), frame(0,0,0,250,Curve.SIN_FULL)));
            case JAMMED -> new Am180Animation(ZERO, ZERO, ZERO, ZERO, ZERO,
                    sequence(time, frame(0,0,0,750), frame(0,0,-1.5D,100,Curve.SIN_UP),
                            frame(0,0,0,100,Curve.SIN_UP)),
                    sequence(time, frame(0,0,0,500), frame(0,0,45,250,Curve.SIN_FULL),
                            frame(0,0,45,400), frame(0,0,0,250,Curve.SIN_FULL)));
            case INSPECT -> new Am180Animation(ZERO, ZERO,
                    sequence(time, frame(0,0,0,200), frame(4,-1,-4,200,Curve.SIN_FULL),
                            frame(4,-1.5D,-4,50), frame(4,0,-4,100),
                            frame(4,6,-4,250,Curve.SIN_DOWN), frame(4,0,-4,150,Curve.SIN_UP),
                            frame(4,-1,-4,100,Curve.SIN_DOWN), frame(4,-1,-4,250),
                            frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(15,0,0,250,Curve.SIN_FULL), frame(15,0,0,1400),
                            frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,600), frame(-400,0,0,500,Curve.SIN_FULL),
                            frame(-400,0,0,250), frame(-360,0,0,250)), ZERO, ZERO);
            default -> Am180Animation.NONE;
        };
    }

    static StarAnimation starAnimation(int animationOrdinal, double time,
                                       boolean aiming, int ammo) {
        TwentyTwoGunItem.GunAnimation[] animations = TwentyTwoGunItem.GunAnimation.values();
        TwentyTwoGunItem.GunAnimation animation = animationOrdinal >= 0
                && animationOrdinal < animations.length ? animations[animationOrdinal]
                : TwentyTwoGunItem.GunAnimation.CYCLE;
        return switch (animation) {
            case EQUIP -> new StarAnimation(
                    sequence(time, frame(45,0,0,0), frame(0,0,0,500,Curve.SIN_DOWN)),
                    ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new StarAnimation(ZERO,
                    sequence(time, frame(0,0,0,50),
                            frame(0,0,aiming ? -0.125D : -0.5D,15,Curve.SIN_DOWN),
                            frame(0,0,0,35,Curve.SIN_FULL)), ZERO, ZERO, ZERO,
                    ammo <= 1 ? new Vec(100,0,0)
                            : sequence(time, frame(0,0,0,90), frame(0,0.5D,2.25D,50)),
                    sequence(time, frame(1,0,0,50,Curve.SIN_UP),
                            frame(0,0,0,50,Curve.SIN_DOWN)),
                    sequence(time, frame(0,0,0,50),
                            frame(0,0,aiming ? -0.5D : -1.0D,25,Curve.SIN_DOWN),
                            frame(0,0,0,75,Curve.SIN_UP)));
            case CYCLE_DRY -> new StarAnimation(
                    sequence(time, frame(0,0,0,600), frame(-3,0,0,175,Curve.SIN_DOWN),
                            frame(0,0,0,100,Curve.SIN_FULL)), ZERO, ZERO, ZERO, ZERO,
                    new Vec(100,0,0),
                    sequence(time, frame(1,0,0,50,Curve.SIN_UP), frame(1,0,0,450),
                            frame(0,0,0,50,Curve.SIN_DOWN)),
                    sequence(time, frame(0,0,0,500),
                            frame(0,0,aiming ? -0.5D : -1.0D,100,Curve.SIN_FULL),
                            frame(0,0,aiming ? -0.5D : -1.0D,100),
                            frame(0,0,0,75,Curve.SIN_UP)));
            case RELOAD -> new StarAnimation(
                    sequence(time, frame(0,0,0,500), frame(3,0,0,750,Curve.SIN_FULL),
                            frame(-3,0,0,50,Curve.SIN_DOWN), frame(0,0,0,100,Curve.SIN_FULL)),
                    ZERO,
                    sequence(time, frame(-30,0,0,250,Curve.SIN_FULL), frame(-30,0,0,1500),
                            frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,200), frame(0,0,15,300,Curve.SIN_FULL),
                            frame(0,0,15,900), frame(0,0,0,150,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,250), frame(0,-7,-1.5D,300,Curve.SIN_UP),
                            frame(0,-7,-1.5D,400), frame(0,0,0,300,Curve.SIN_UP)),
                    sequence(time, frame(ammo <= 1 ? 100 : 0,0,0,0),
                            frame(ammo <= 1 ? 100 : 0,0,0,750), frame(0,0,0,0),
                            frame(0,0,0,750), frame(0,0.5D,2.25D,50)),
                    ZERO,
                    sequence(time, frame(0,0,0,250), frame(0,0,-1,100,Curve.SIN_FULL),
                            frame(0,0,-1,1125), frame(0,0,0,100,Curve.SIN_UP)));
            case JAMMED -> new StarAnimation(ZERO, ZERO,
                    sequence(time, frame(0,0,0,500), frame(-30,0,0,150,Curve.SIN_FULL),
                            frame(-30,0,0,800), frame(0,0,0,150,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,0,500), frame(0,0,25,150,Curve.SIN_FULL),
                            frame(0,0,25,800), frame(0,0,0,150,Curve.SIN_FULL)), ZERO,
                    sequence(time, frame(0,0.5D,2.25D,750),
                            frame(0,0.5D,1.25D,100,Curve.SIN_FULL),
                            frame(0,0.5D,1.25D,100), frame(0,0.5D,2.25D,100,Curve.SIN_UP),
                            frame(0,0.5D,2.25D,100), frame(0,0.5D,1.25D,100,Curve.SIN_FULL),
                            frame(0,0.5D,1.25D,100), frame(0,0.5D,2.25D,100,Curve.SIN_UP)),
                    ZERO,
                    sequence(time, frame(0,0,0,750), frame(0,0,-0.5D,100,Curve.SIN_FULL),
                            frame(0,0,-0.5D,100), frame(0,0,0,100,Curve.SIN_UP),
                            frame(0,0,0,100), frame(0,0,-0.5D,100,Curve.SIN_FULL),
                            frame(0,0,-0.5D,100), frame(0,0,0,100,Curve.SIN_UP)));
            case INSPECT -> new StarAnimation(ZERO, ZERO,
                    sequence(time, frame(-30,0,0,250,Curve.SIN_FULL), frame(-30,0,0,1500),
                            frame(0,0,0,250,Curve.SIN_FULL)),
                    sequence(time, frame(0,0,25,250,Curve.SIN_FULL), frame(0,0,25,1500),
                            frame(0,0,0,250,Curve.SIN_FULL)), ZERO,
                    ammo <= 1 ? new Vec(100,0,0)
                            : sequence(time, frame(0,0.5D,2.25D,350),
                                    frame(0,0.5D,1.25D,100,Curve.SIN_FULL),
                                    frame(0,0.5D,1.25D,1125),
                                    frame(0,0.5D,2.25D,100,Curve.SIN_UP)), ZERO,
                    sequence(time, frame(0,0,0,350), frame(0,0,-0.5D,100,Curve.SIN_FULL),
                            frame(0,0,-0.5D,1125), frame(0,0,0,100,Curve.SIN_UP)));
            default -> StarAnimation.NONE;
        };
    }

    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (TwentyTwoGunItem.animationTimer(stack) + partial) * 50.0D;
    }

    static void pivotX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
    }

    static Vec sequence(double time, Frame... frames) {
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

    static Frame frame(double x, double y, double z, double duration) {
        return frame(x, y, z, duration, Curve.LINEAR);
    }
    static Frame frame(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x, y, z), duration, curve);
    }
    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * progress;
    }
    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path);
    }

    static final Vec ZERO = new Vec(0, 0, 0);
    static record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double progress) {
            return new Vec(x + (other.x - x) * progress,
                    y + (other.y - y) * progress, z + (other.z - z) * progress);
        }
    }
    static record Frame(Vec value, double duration, Curve curve) { }
    enum Curve {
        LINEAR { double apply(double x) { return x; } },
        SIN_UP { double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }

    private record Am180Animation(Vec equip, Vec recoil, Vec mag, Vec magTurn,
                                  Vec magSpin, Vec bolt, Vec turn) {
        private static final Am180Animation NONE = new Am180Animation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
    static record StarAnimation(Vec equip, Vec recoil, Vec tilt, Vec turn,
                                Vec mag, Vec bullet, Vec hammer, Vec slide) {
        private static final StarAnimation NONE = new StarAnimation(
                ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO);
    }
}
