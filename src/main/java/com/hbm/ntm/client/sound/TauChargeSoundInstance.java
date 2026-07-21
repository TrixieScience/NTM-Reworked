package com.hbm.ntm.client.sound;

import com.hbm.ntm.item.TauGunItem;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class TauChargeSoundInstance extends AbstractTickableSoundInstance {
    private static final Map<Integer, TauChargeSoundInstance> SOUNDS = new HashMap<>();
    private final LivingEntity shooter;
    private int grace = 2;

    public static void keepAlive(LivingEntity shooter, int chargeTicks) {
        SOUNDS.entrySet().removeIf(entry -> entry.getValue().isStopped());
        TauChargeSoundInstance sound = SOUNDS.get(shooter.getId());
        if (sound == null) {
            sound = new TauChargeSoundInstance(shooter);
            SOUNDS.put(shooter.getId(), sound);
            Minecraft.getInstance().getSoundManager().play(sound);
        }
        sound.grace = 2;
        sound.pitch = 0.75F + chargeTicks * 0.01F;
    }

    private TauChargeSoundInstance(LivingEntity shooter) {
        super(ModSounds.GUN_TAU_LOOP.get(), SoundSource.PLAYERS, RandomSource.create());
        this.shooter = shooter;
        looping = true;
        delay = 0;
        volume = 1.0F;
        pitch = 0.75F;
        attenuation = Attenuation.LINEAR;
        updatePosition();
    }

    @Override
    public void tick() {
        if (!shooter.isAlive() || --grace < 0
                || !(shooter.getMainHandItem().getItem() instanceof TauGunItem)
                || TauGunItem.animation(shooter.getMainHandItem()) != TauGunItem.GunAnimation.SPINUP
                || TauGunItem.animationTimer(shooter.getMainHandItem()) >= 300) {
            stop();
            return;
        }
        updatePosition();
    }

    private void updatePosition() {
        x = shooter.getX();
        y = shooter.getY() + shooter.getBbHeight() * 0.5D;
        z = shooter.getZ();
    }
}
