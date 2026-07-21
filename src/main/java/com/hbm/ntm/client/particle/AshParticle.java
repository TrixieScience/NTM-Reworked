package com.hbm.ntm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

/** Long-lived source ash fleck left behind by energy-weapon disintegration. */
public final class AshParticle extends TextureSheetParticle {
    private final float rollMomentum;

    private AshParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        lifetime = 1_200 + random.nextInt(20);
        quadSize = 0.1125F + random.nextFloat() * 0.2F;
        float shade = random.nextFloat() * 0.1F + 0.1F;
        setColor(shade, shade, shade);
        rollMomentum = random.nextBoolean() ? 0.017453292F : -0.017453292F;
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        oRoll = roll;
        if (age++ >= lifetime) {
            remove();
            return;
        }

        yd -= 0.01D;
        if (!onGround) roll += rollMomentum;
        move(xd, yd, zd);
        xd *= 0.95D;
        yd *= 0.99D;
        zd *= 0.95D;
        alpha = age > lifetime - 40 ? Math.max(0.0F, (lifetime - age) / 40.0F) : 1.0F;
        if (onGround && random.nextInt(75) == 0) {
            level.addParticle(ParticleTypes.SMOKE, x, y + 0.125D, z, 0.0D, 0.05D, 0.0D);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public AshParticle createParticle(SimpleParticleType type, ClientLevel level,
                                          double x, double y, double z,
                                          double xSpeed, double ySpeed, double zSpeed) {
            return new AshParticle(level, x, y, z, sprites);
        }
    }
}
