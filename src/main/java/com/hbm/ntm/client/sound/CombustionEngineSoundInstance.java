package com.hbm.ntm.client.sound;

import com.hbm.ntm.blockentity.CombustionEngineBlockEntity;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class CombustionEngineSoundInstance extends AbstractTickableSoundInstance {
    private static final double RANGE_SQUARED = 20D * 20D;
    private final CombustionEngineBlockEntity engine;

    public CombustionEngineSoundInstance(CombustionEngineBlockEntity engine) {
        super(ModSounds.COMBUSTION_ENGINE_OPERATE.get(), SoundSource.BLOCKS, RandomSource.create());
        this.engine = engine;
        looping = true;
        attenuation = Attenuation.LINEAR;
        x = engine.getBlockPos().getX() + 0.5D;
        y = engine.getBlockPos().getY() + 0.5D;
        z = engine.getBlockPos().getZ() + 0.5D;
        volume = 1F;
        pitch = 1F;
    }

    @Override public void tick() {
        if (engine.isRemoved() || !engine.active() || Minecraft.getInstance().player == null
                || Minecraft.getInstance().player.distanceToSqr(x, y, z) > RANGE_SQUARED) stop();
    }
}
