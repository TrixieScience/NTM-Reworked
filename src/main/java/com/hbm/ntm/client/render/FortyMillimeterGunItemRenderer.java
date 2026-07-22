package com.hbm.ntm.client.render;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.ClientWeaponEvents;
import com.hbm.ntm.client.model.EnvsuitMesh;
import com.hbm.ntm.item.FortyMillimeterGunItem;
import com.hbm.ntm.weapon.FortyMillimeterAmmoType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/** Flare Gun, Congo Lake and MK108: three tubes in a trench coat. */
public final class FortyMillimeterGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation FLARE_MODEL = id("models/weapons/flaregun.obj");
    private static final ResourceLocation CONGO_MODEL = id("models/weapons/congolake.obj");
    private static final ResourceLocation MK108_MODEL = id("models/weapons/mk108.obj");
    private static final ResourceLocation FLARE_TEXTURE = id("textures/models/weapons/flaregun.png");
    private static final ResourceLocation CONGO_TEXTURE = id("textures/models/weapons/congolake.png");
    private static final ResourceLocation MK108_TEXTURE = id("textures/models/weapons/mk108.png");
    private static final ResourceLocation CASINGS = id("textures/particle/casings.png");
    private static final ResourceLocation CONGO_ANIMATION = id("models/weapons/animations/congolake.json");

    private static final Set<String> FLARE_GROUPS = Set.of("Gun", "Hammer", "Barrel", "Flare");
    private static final Set<String> CONGO_GROUPS = Set.of(
            "Gun", "Pump", "Shell", "Sight", "Loop", "GuardOuter", "GuardInner", "ShellFore");
    private static final Set<String> MK108_GROUPS = Set.of("Gun", "Barrel", "Lid", "Belt", "Grenade", "Drum");

    private final Map<FortyMillimeterGunItem.Variant, EnvsuitMesh> meshes =
            new EnumMap<>(FortyMillimeterGunItem.Variant.class);
    private LegacyWeaponAnimation congoAnimations;

    public FortyMillimeterGunItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    /** Fail on reload, not when the launcher jumps into your face. */
    public static void validateAnimationResources(ResourceManager resources) {
        LegacyWeaponAnimation.load(resources, CONGO_ANIMATION);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
                             MultiBufferSource buffers, int light, int overlay) {
        if (!(stack.getItem() instanceof FortyMillimeterGunItem gun)) return;
        boolean firstPerson = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean held = firstPerson || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        long elapsed = System.currentTimeMillis() - ClientWeaponEvents.lastShot(stack);
        boolean flash = gun.variant() != FortyMillimeterGunItem.Variant.FLARE_GUN
                && held && elapsed >= 0 && elapsed < (gun.variant() == FortyMillimeterGunItem.Variant.MK108 ? 50 : 150);

        poses.pushPose();
        setupContext(gun.variant(), context, poses);
        if (firstPerson) renderFirstPerson(stack, gun.variant(), poses, buffers, light, overlay, flash, elapsed);
        else {
            renderStatic(gun.variant(), poses, buffers, light, overlay);
            if (flash) renderFlash(gun.variant(), poses, buffers, elapsed, ClientWeaponEvents.shotRandom(stack));
        }
        poses.popPose();
    }

    private void renderFirstPerson(ItemStack stack, FortyMillimeterGunItem.Variant variant,
                                   PoseStack poses, MultiBufferSource buffers, int light, int overlay,
                                   boolean flash, long elapsed) {
        switch (variant) {
            case FLARE_GUN -> renderFlare(stack, poses, buffers, light, overlay);
            case CONGO_LAKE -> renderCongo(stack, poses, buffers, light, overlay);
            case MK108 -> renderMk108(stack, poses, buffers, light, overlay);
        }
        if (flash) renderFlash(variant, poses, buffers, elapsed, ClientWeaponEvents.shotRandom(stack));
    }

    private void renderFlare(ItemStack stack, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        double time = animationTime(stack);
        FlareAnimation animation = flareAnimation(stack, time);
        poses.scale(0.125F, 0.125F, 0.125F);
        poses.translate(animation.recoil.x, animation.recoil.y, animation.recoil.z);
        poses.mulPose(Axis.XP.rotationDegrees((float) (animation.recoil.z * 10.0D + animation.flip.x)));
        pivotX(poses, 0.0D, 0.0D, -8.0D, -animation.equip.x);
        render(FortyMillimeterGunItem.Variant.FLARE_GUN, "Gun", FLARE_TEXTURE, poses, buffers, light, overlay, -1);

        poses.pushPose();
        pivotX(poses, 0.0D, 1.8125D, -4.0D, animation.hammer.x - 15.0D);
        render(FortyMillimeterGunItem.Variant.FLARE_GUN, "Hammer", FLARE_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();

        poses.pushPose();
        pivotX(poses, 0.0D, 2.156D, 1.78D, animation.open.x);
        render(FortyMillimeterGunItem.Variant.FLARE_GUN, "Barrel", FLARE_TEXTURE, poses, buffers, light, overlay, -1);
        poses.translate(animation.shell.x, animation.shell.y, animation.shell.z);
        render(FortyMillimeterGunItem.Variant.FLARE_GUN, "Flare", FLARE_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();

        poses.pushPose();
        poses.translate(0.0D, 4.0D, 9.0D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        boolean reloading = FortyMillimeterGunItem.state(stack) == FortyMillimeterGunItem.GunState.RELOADING;
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 2.5D,
                WeaponSmokeRenderer.FORTY_MM, reloading);
        poses.translate(0.0D, 0.0D, 0.1D);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 2.0D,
                WeaponSmokeRenderer.FORTY_MM, reloading);
        poses.popPose();
    }

    private void renderCongo(ItemStack stack, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        double time = animationTime(stack);
        String clip = congoClip(stack);
        poses.scale(0.5F, 0.5F, 0.5F);
        applyCongo(poses, clip, "Gun", time);
        render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "Gun", CONGO_TEXTURE, poses, buffers, light, overlay, -1);
        renderCongoChild("Pump", clip, time, poses, buffers, light, overlay, CONGO_TEXTURE, -1);

        poses.pushPose();
        applyCongo(poses, clip, "Sight", time);
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        pivotX(poses, 0.0D, 2.125D, 3.0D, ClientWeaponEvents.aimingProgress(partial) * -90.0D);
        render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "Sight", CONGO_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();

        renderCongoChild("Loop", clip, time, poses, buffers, light, overlay, CONGO_TEXTURE, -1);
        poses.pushPose();
        applyCongo(poses, clip, "GuardOuter", time);
        render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "GuardOuter", CONGO_TEXTURE, poses, buffers, light, overlay, -1);
        applyCongo(poses, clip, "GuardInner", time);
        render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "GuardInner", CONGO_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();

        boolean omitShell = FortyMillimeterGunItem.animation(stack) == FortyMillimeterGunItem.GunAnimation.INSPECT
                && FortyMillimeterGunItem.rounds(stack) <= 0;
        if (!omitShell) {
            int color = casingColor(FortyMillimeterGunItem.loadedAmmo(stack));
            poses.pushPose();
            applyCongo(poses, clip, "Shell", time);
            render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "Shell", CASINGS, poses, buffers, light, overlay,
                    0xFF000000 | color);
            render(FortyMillimeterGunItem.Variant.CONGO_LAKE, "ShellFore", CASINGS, poses, buffers, light, overlay,
                    0xFF000000 | color);
            poses.popPose();
        }

        poses.pushPose();
        poses.translate(0.0D, 1.75D, 4.25D);
        if (clip != null) {
            double[] gun = congoAnimations().transform(clip, "Gun", time).values();
            poses.mulPose(Axis.ZN.rotationDegrees((float) gun[5]));
            poses.mulPose(Axis.YN.rotationDegrees((float) gun[4]));
            poses.mulPose(Axis.XN.rotationDegrees((float) gun[3]));
        }
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.scale(0.5F, 0.5F, 0.5F);
        WeaponSmokeRenderer.render(stack, 0, poses, buffers, 1.0D,
                WeaponSmokeRenderer.FORTY_MM,
                FortyMillimeterGunItem.state(stack) == FortyMillimeterGunItem.GunState.RELOADING);
        poses.popPose();
    }

    private void renderCongoChild(String group, String clip, double time, PoseStack poses,
                                  MultiBufferSource buffers, int light, int overlay,
                                  ResourceLocation texture, int color) {
        poses.pushPose();
        applyCongo(poses, clip, group, time);
        render(FortyMillimeterGunItem.Variant.CONGO_LAKE, group, texture, poses, buffers, light, overlay, color);
        poses.popPose();
    }

    private void renderMk108(ItemStack stack, PoseStack poses, MultiBufferSource buffers, int light, int overlay) {
        double time = animationTime(stack);
        MkAnimation animation = mkAnimation(stack, time);
        poses.scale(0.375F, 0.375F, 0.375F);

        if (FortyMillimeterGunItem.animation(stack) == FortyMillimeterGunItem.GunAnimation.INSPECT) {
            for (int i = 0; i < 3; i++) renderYeetedGrenade(time, i, poses, buffers, light, overlay);
        }

        pivotX(poses, 0.0D, -1.0D, -8.0D, animation.equip.x);
        pivotX(poses, 0.0D, 1.0D, -4.0D, animation.lift.x);
        poses.translate(0.0D, 0.0D, animation.recoil.z);
        render(FortyMillimeterGunItem.Variant.MK108, "Gun", MK108_TEXTURE, poses, buffers, light, overlay, -1);

        poses.pushPose();
        poses.translate(0.0D, 0.0D, animation.barrel.z * 2.0D);
        render(FortyMillimeterGunItem.Variant.MK108, "Barrel", MK108_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();
        poses.pushPose();
        pivotX(poses, 0.0D, 0.6875D, -1.0D, animation.lid.x);
        render(FortyMillimeterGunItem.Variant.MK108, "Lid", MK108_TEXTURE, poses, buffers, light, overlay, -1);
        poses.popPose();

        poses.pushPose();
        poses.translate(animation.drum.x, animation.drum.y, animation.drum.z);
        render(FortyMillimeterGunItem.Variant.MK108, "Drum", MK108_TEXTURE, poses, buffers, light, overlay, -1);
        renderBelt(stack, animation, poses, buffers, light, overlay);
        poses.popPose();
    }

    private void renderBelt(ItemStack stack, MkAnimation animation, PoseStack poses,
                            MultiBufferSource buffers, int light, int overlay) {
        renderBelt(FortyMillimeterGunItem.rounds(stack), animation, poses, buffers, light, overlay);
    }

    private void renderBelt(int amount, MkAnimation animation, PoseStack poses,
                            MultiBufferSource buffers, int light, int overlay) {
        double[] loaded = {0, 0, -5, 0, -5, 60, 45, -10, 0};
        double[] unloaded = {0, -30, -60, -45, -45, 0, 0, 0, 0};
        double[][] shells = new double[loaded.length][3];
        double x = 1.375D;
        double y = -2.875D;
        double angle = 0.0D;
        double vx = 0.0D;
        double vy = 0.53125D;
        for (int i = 0; i < loaded.length; i++) {
            shells[i][0] = x;
            shells[i][1] = y;
            shells[i][2] = angle - 90.0D;
            double delta = unloaded[i] + (loaded[i] - unloaded[i]) * animation.belt.x;
            angle += delta;
            double radians = Math.toRadians(-delta);
            double nextX = vx * Math.cos(radians) - vy * Math.sin(radians);
            double nextY = vx * Math.sin(radians) + vy * Math.cos(radians);
            vx = nextX;
            vy = nextY;
            x += vx;
            y += vy;
        }
        for (int i = 0; i < shells.length - 1; i++) {
            double progress = animation.cycle.x;
            double sx = shells[i][0] + (shells[i + 1][0] - shells[i][0]) * progress;
            double sy = shells[i][1] + (shells[i + 1][1] - shells[i][1]) * progress;
            double sr = shells[i][2] + (shells[i + 1][2] - shells[i][2]) * progress;
            poses.pushPose();
            poses.translate(sx, sy, 0.0D);
            poses.mulPose(Axis.ZP.rotationDegrees((float) sr));
            render(FortyMillimeterGunItem.Variant.MK108, "Belt", MK108_TEXTURE, poses, buffers, light, overlay, -1);
            if (shells.length - i < amount + 2) {
                render(FortyMillimeterGunItem.Variant.MK108, "Grenade", MK108_TEXTURE,
                        poses, buffers, light, overlay, -1);
            }
            poses.popPose();
        }
    }

    private void renderYeetedGrenade(double time, int index, PoseStack poses,
                                     MultiBufferSource buffers, int light, int overlay) {
        double local = time - index * 250.0D;
        if (local < 0.0D || local > 750.0D) return;
        double progress = local / 750.0D;
        double horizontal = 9.0D - 15.0D * progress;
        double vertical = -2.0D + Math.sin(progress * Math.PI) * 8.0D + progress * 4.0D;
        poses.pushPose();
        poses.translate(horizontal, vertical, -2.3125D);
        poses.mulPose(Axis.XN.rotationDegrees(90.0F));
        poses.mulPose(Axis.YN.rotationDegrees((float) (progress * 1080.0D)));
        poses.translate(0.0D, 0.0D, 2.3125D);
        render(FortyMillimeterGunItem.Variant.MK108, "Grenade", MK108_TEXTURE,
                poses, buffers, light, overlay, -1);
        poses.popPose();
    }

    private void renderStatic(FortyMillimeterGunItem.Variant variant, PoseStack poses,
                              MultiBufferSource buffers, int light, int overlay) {
        // First person needs the half-turn. Everything else knows where forward is.
        ResourceLocation texture = texture(variant);
        Set<String> groups = switch (variant) {
            case FLARE_GUN -> FLARE_GROUPS;
            case CONGO_LAKE -> CONGO_GROUPS;
            case MK108 -> Set.of("Gun", "Barrel", "Lid", "Drum");
        };
        for (String group : groups) render(variant, group, texture, poses, buffers, light, overlay, -1);
        if (variant == FortyMillimeterGunItem.Variant.MK108) {
            MkAnimation staticAnimation = new MkAnimation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, new Vec(1, 0, 0));
            renderBeltStatic(staticAnimation, poses, buffers, light, overlay);
        }
    }

    private void renderBeltStatic(MkAnimation animation, PoseStack poses, MultiBufferSource buffers,
                                  int light, int overlay) {
        // Display models get the sales brochure belt.
        renderBelt(30, animation, poses, buffers, light, overlay);
    }

    private void applyCongo(PoseStack poses, String clip, String group, double time) {
        if (clip != null) LegacyWeaponAnimation.apply(poses, congoAnimations().transform(clip, group, time));
    }

    private void render(FortyMillimeterGunItem.Variant variant, String group, ResourceLocation texture,
                        PoseStack poses, MultiBufferSource buffers, int light, int overlay, int color) {
        mesh(variant).render(group, poses.last(), buffers.getBuffer(RenderType.entityCutout(texture)),
                1.0F, light, overlay, color);
    }

    private EnvsuitMesh mesh(FortyMillimeterGunItem.Variant variant) {
        return meshes.computeIfAbsent(variant, key -> switch (key) {
            case FLARE_GUN -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    FLARE_MODEL, FLARE_GROUPS, "Flare Gun");
            case CONGO_LAKE -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    CONGO_MODEL, CONGO_GROUPS, "Congo Lake");
            case MK108 -> EnvsuitMesh.load(Minecraft.getInstance().getResourceManager(),
                    MK108_MODEL, MK108_GROUPS, "MK108");
        });
    }

    private LegacyWeaponAnimation congoAnimations() {
        if (congoAnimations == null) congoAnimations = LegacyWeaponAnimation.load(
                Minecraft.getInstance().getResourceManager(), CONGO_ANIMATION);
        return congoAnimations;
    }

    private static void setupContext(FortyMillimeterGunItem.Variant variant,
                                     ItemDisplayContext context, PoseStack poses) {
        poses.translate(0.5D, 0.5D, 0.5D);
        switch (context) {
            case GUI -> {
                poses.scale(1.0F, -1.0F, 1.0F);
                poses.scale(1.0F, 1.0F, -1.0F);
                poses.mulPose(Axis.ZP.rotationDegrees(225.0F));
                poses.mulPose(Axis.YP.rotationDegrees(90.0F));
                float scale = switch (variant) {
                    case FLARE_GUN -> 1.0F;
                    case CONGO_LAKE -> 2.5F;
                    case MK108 -> 1.375F;
                };
                poses.scale(scale / 16.0F, scale / 16.0F, scale / 16.0F);
                poses.mulPose(Axis.XP.rotationDegrees(25.0F));
                poses.mulPose(Axis.YP.rotationDegrees(45.0F));
                if (variant == FortyMillimeterGunItem.Variant.FLARE_GUN) poses.translate(-0.5D, 0.0D, 0.0D);
                else if (variant == FortyMillimeterGunItem.Variant.CONGO_LAKE) poses.translate(0.0D, -1.25D, 0.0D);
                else poses.translate(0.0D, 0.5D, 0.25D);
            }
            case GROUND, FIXED -> {
                float scale = variant == FortyMillimeterGunItem.Variant.FLARE_GUN ? 0.0625F : 0.1F;
                poses.scale(scale, scale, scale);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> setupThirdPerson(variant, context, poses);
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
                // Turn first or the launcher hides behind the camera and laughs.
                poses.mulPose(Axis.YP.rotationDegrees(180.0F));
                float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
                float aim = ClientWeaponEvents.aimingProgress(partial);
                double side = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 1.0D : -1.0D;
                poses.translate(0.0D, 0.0D, 0.875D);
                Vec hip = switch (variant) {
                    case FLARE_GUN -> new Vec(side * 1.0D, -1.2D, 1.6D);
                    case CONGO_LAKE -> new Vec(side * 1.2D, -1.6D, 1.0D);
                    case MK108 -> new Vec(side * 0.8D, -1.2D, 2.0D);
                };
                Vec aimed = switch (variant) {
                    case FLARE_GUN -> new Vec(0.0D, -0.6875D, 0.5D);
                    case CONGO_LAKE -> new Vec(0.0D, -1.25D, 0.25D);
                    case MK108 -> new Vec(side * 0.75D, -0.75D, 1.5D);
                };
                poses.translate(lerp(hip.x, aimed.x, aim), lerp(hip.y, aimed.y, aim), lerp(hip.z, aimed.z, aim));
            }
            default -> {
                poses.scale(0.075F, 0.075F, 0.075F);
                poses.mulPose(Axis.YN.rotationDegrees(90.0F));
            }
        }
    }

    private static void setupThirdPerson(FortyMillimeterGunItem.Variant variant,
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
        if (variant == FortyMillimeterGunItem.Variant.FLARE_GUN) {
            poses.scale(0.5F, 0.5F, 0.5F);
            poses.translate(0.0D, 0.25D, 3.0D);
        } else if (variant == FortyMillimeterGunItem.Variant.CONGO_LAKE) {
            poses.translate(0.0D, -2.5D, 4.0D);
            poses.scale(2.5F, 2.5F, 2.5F);
        } else {
            poses.scale(2.0F, 2.0F, 2.0F);
            poses.translate(1.0D, -2.5D, 4.0D);
        }
    }

    private static void renderFlash(FortyMillimeterGunItem.Variant variant, PoseStack poses,
                                    MultiBufferSource buffers, long elapsed, float random) {
        float duration = variant == FortyMillimeterGunItem.Variant.MK108 ? 50.0F : 150.0F;
        float progress = elapsed / duration;
        float maximumLength = variant == FortyMillimeterGunItem.Variant.MK108 ? 5.0F : 7.5F;
        poses.pushPose();
        if (variant == FortyMillimeterGunItem.Variant.CONGO_LAKE) {
            poses.translate(0.0D, 1.75D, 4.25D);
            poses.scale(0.5F, 0.5F, 0.5F);
        } else poses.translate(0.0D, 0.0D, 8.125D);
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.mulPose(Axis.XP.rotationDegrees(random * 90.0F));
        SednaMuzzleFlash.render(poses, buffers, progress, maximumLength);
        poses.popPose();
    }

    private static void flashQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                  float width, float length, boolean vertical) {
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, 0, 0, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, 0, 1, 1);
        flashVertex(consumer, pose, vertical ? 0 : width, vertical ? width : 0, length, 1, 0);
        flashVertex(consumer, pose, vertical ? 0 : -width, vertical ? -width : 0, length, 0, 0);
    }

    private static void flashVertex(VertexConsumer consumer, PoseStack.Pose pose,
                                    float x, float y, float z, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0.0F, 1.0F, 0.0F);
    }

    private static FlareAnimation flareAnimation(ItemStack stack, double time) {
        return switch (FortyMillimeterGunItem.animation(stack)) {
            case EQUIP -> new FlareAnimation(seq(time, f(-90,0,0,0), f(0,0,0,350,Curve.SIN_DOWN)), ZERO, ZERO, ZERO, ZERO, ZERO);
            case CYCLE -> new FlareAnimation(ZERO,
                    seq(time, f(0,0,0,50), f(0,0,-3,50), f(0,0,0,250)),
                    seq(time, f(15,0,0,50), f(15,0,0,550), f(0,0,0,100)), ZERO, ZERO, ZERO);
            case CYCLE_DRY -> new FlareAnimation(ZERO, ZERO,
                    seq(time, f(15,0,0,50), f(15,0,0,550), f(0,0,0,100)), ZERO, ZERO, ZERO);
            case RELOAD -> new FlareAnimation(ZERO, ZERO, ZERO,
                    seq(time, f(45,0,0,200,Curve.SIN_FULL), f(45,0,0,750), f(0,0,0,200,Curve.SIN_UP)),
                    seq(time, f(4,-8,-4,0), f(4,-8,-4,200), f(0,0,-5,500,Curve.SIN_DOWN), f(0,0,0,200,Curve.SIN_UP)),
                    seq(time, f(0,0,0,200), f(25,0,0,200,Curve.SIN_DOWN), f(25,0,0,800), f(0,0,0,200,Curve.SIN_DOWN)));
            case JAMMED -> new FlareAnimation(ZERO, ZERO, ZERO,
                    seq(time, f(0,0,0,500), f(45,0,0,200,Curve.SIN_FULL), f(45,0,0,500), f(0,0,0,200,Curve.SIN_UP)), ZERO,
                    seq(time, f(0,0,0,700), f(25,0,0,200,Curve.SIN_DOWN), f(25,0,0,550), f(0,0,0,200,Curve.SIN_DOWN)));
            case INSPECT -> new FlareAnimation(ZERO, ZERO, ZERO, ZERO, ZERO,
                    seq(time, f(-1080,0,0,1500,Curve.SIN_FULL)));
            default -> FlareAnimation.NONE;
        };
    }

    private static MkAnimation mkAnimation(ItemStack stack, double time) {
        return switch (FortyMillimeterGunItem.animation(stack)) {
            case EQUIP -> new MkAnimation(seq(time, f(45,0,0,0), f(0,0,0,1000,Curve.SIN_DOWN)), ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, new Vec(1,0,0));
            case CYCLE -> new MkAnimation(ZERO, ZERO,
                    seq(time, f(0,0,0,50), f(0,0,-0.25,100,Curve.SIN_DOWN), f(0,0,0,150,Curve.SIN_FULL)),
                    seq(time, f(0,0,-1,100,Curve.SIN_DOWN), f(0,0,0,250,Curve.SIN_FULL)), ZERO, ZERO,
                    seq(time, f(0,0,0,100), f(1,0,0,150)), new Vec(1,0,0));
            case RELOAD -> new MkAnimation(ZERO,
                    seq(time, f(10,0,0,500,Curve.SIN_FULL), f(10,0,0,750), f(-50,0,0,750,Curve.SIN_FULL),
                            f(-50,0,0,3500), f(0,0,0,500,Curve.SIN_FULL), f(0,0,0,500), f(1,0,0,100,Curve.SIN_UP), f(0,0,0,150,Curve.SIN_FULL)), ZERO, ZERO,
                    seq(time, f(60,0,0,500,Curve.SIN_FULL), f(60,0,0,5500), f(0,0,0,500,Curve.SIN_UP)),
                    seq(time, f(0,0,0,2000), f(2.5,0,0,500,Curve.SIN_DOWN), f(2.5,-2,-8,500,Curve.SIN_UP),
                            f(4,-3,-8,0), f(2.5,0,0,1000,Curve.SIN_FULL), f(0,0,0,500,Curve.SIN_UP)), ZERO,
                    seq(time, f(1,0,0,500), f(0,0,0,750,Curve.SIN_UP), f(0,0,0,3250), f(1,0,0,750,Curve.SIN_UP)));
            case JAMMED -> new MkAnimation(ZERO,
                    seq(time, f(0,0,0,1000), f(1,0,0,100,Curve.SIN_UP), f(0,0,0,150,Curve.SIN_FULL)), ZERO, ZERO,
                    seq(time, f(0,0,0,250), f(45,0,0,500,Curve.SIN_FULL), f(0,0,0,250,Curve.SIN_UP)), ZERO, ZERO, new Vec(1,0,0));
            case INSPECT -> new MkAnimation(ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, ZERO, new Vec(1,0,0));
            default -> MkAnimation.NONE;
        };
    }

    private static String congoClip(ItemStack stack) {
        return switch (FortyMillimeterGunItem.animation(stack)) {
            case EQUIP -> "Equip";
            case CYCLE -> FortyMillimeterGunItem.rounds(stack) <= 1 ? "FireEmpty" : "Fire";
            case RELOAD -> FortyMillimeterGunItem.amountBeforeReload(stack) == 0 ? "ReloadEmpty" : "ReloadStart";
            case RELOAD_CYCLE -> "Reload";
            case RELOAD_END -> "ReloadEnd";
            case JAMMED -> "Jammed";
            case INSPECT -> "Inspect";
            default -> null;
        };
    }

    private static int casingColor(FortyMillimeterAmmoType type) {
        return switch (type) {
            case SIGNAL_FLARE -> 0x9E1616;
            case HIGH_EXPLOSIVE -> 0x777777;
            case SHAPED_CHARGE -> 0x5E6854;
            case DEMOLITION -> 0xE30000;
            case INCENDIARY -> 0xE86F20;
            case WHITE_PHOSPHORUS -> 0xC8C8C8;
        };
    }

    private static void pivotX(PoseStack poses, double x, double y, double z, double degrees) {
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees((float) degrees));
        poses.translate(-x, -y, -z);
    }
    private static double animationTime(ItemStack stack) {
        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
        return (FortyMillimeterGunItem.animationTimer(stack) + partial) * 50.0D;
    }
    private static Vec seq(double time, Frame... frames) {
        double elapsed = 0.0D;
        Vec previous = ZERO;
        for (Frame frame : frames) {
            if (frame.duration <= 0.0D) { previous = frame.value; continue; }
            if (time < elapsed + frame.duration) {
                double p = Math.max(0.0D, Math.min((time - elapsed) / frame.duration, 1.0D));
                return previous.lerp(frame.value, frame.curve.apply(p));
            }
            elapsed += frame.duration;
            previous = frame.value;
        }
        return previous;
    }
    private static Frame f(double x, double y, double z, double duration) { return f(x,y,z,duration,Curve.LINEAR); }
    private static Frame f(double x, double y, double z, double duration, Curve curve) {
        return new Frame(new Vec(x,y,z), duration, curve);
    }
    private static double lerp(double a, double b, double p) { return a + (b - a) * p; }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, path); }
    private static ResourceLocation texture(FortyMillimeterGunItem.Variant variant) {
        return switch (variant) {
            case FLARE_GUN -> FLARE_TEXTURE;
            case CONGO_LAKE -> CONGO_TEXTURE;
            case MK108 -> MK108_TEXTURE;
        };
    }

    private static final Vec ZERO = new Vec(0,0,0);
    private record Vec(double x, double y, double z) {
        Vec lerp(Vec other, double p) { return new Vec(
                FortyMillimeterGunItemRenderer.lerp(x, other.x, p),
                FortyMillimeterGunItemRenderer.lerp(y, other.y, p),
                FortyMillimeterGunItemRenderer.lerp(z, other.z, p)); }
    }
    private record Frame(Vec value, double duration, Curve curve) { }
    private enum Curve {
        LINEAR { double apply(double x) { return x; } },
        SIN_UP { double apply(double x) { return 1.0D - Math.cos(x * Math.PI * 0.5D); } },
        SIN_DOWN { double apply(double x) { return Math.sin(x * Math.PI * 0.5D); } },
        SIN_FULL { double apply(double x) { return (-Math.cos(x * Math.PI) + 1.0D) * 0.5D; } };
        abstract double apply(double x);
    }
    private record FlareAnimation(Vec equip, Vec recoil, Vec hammer, Vec open, Vec shell, Vec flip) {
        private static final FlareAnimation NONE = new FlareAnimation(ZERO,ZERO,ZERO,ZERO,ZERO,ZERO);
    }
    private record MkAnimation(Vec equip, Vec lift, Vec recoil, Vec barrel, Vec lid, Vec drum, Vec cycle, Vec belt) {
        private static final MkAnimation NONE = new MkAnimation(ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,ZERO,new Vec(1,0,0));
    }
}
