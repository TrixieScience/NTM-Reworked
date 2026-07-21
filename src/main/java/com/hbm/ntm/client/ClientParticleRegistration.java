package com.hbm.ntm.client;

import com.hbm.ntm.block.ChlorineGasBlock;
import com.hbm.ntm.client.particle.AshParticle;
import com.hbm.ntm.client.particle.BlackPowderSmokeParticle;
import com.hbm.ntm.client.particle.BlackPowderSparkParticle;
import com.hbm.ntm.client.particle.ChlorineCloudParticle;
import com.hbm.ntm.client.particle.GibletParticle;
import com.hbm.ntm.client.particle.GasFlameParticle;
import com.hbm.ntm.client.particle.FlamethrowerParticle;
import com.hbm.ntm.client.particle.TauSparkParticle;
import com.hbm.ntm.client.particle.TauHadronParticle;
import com.hbm.ntm.network.VomitPayload;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public final class ClientParticleRegistration {
    private ClientParticleRegistration() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientParticleRegistration::registerParticleProviders);
        ClientDisintegrationEffects.register();
        VomitPayload.installClientParticleLimiter(count -> {
            // ClientProxy divided by particleSetting + 1: all/decreased/minimal = 25/12/8.
            int divisor = Minecraft.getInstance().options.particles().get().getId() + 1;
            return count / divisor;
        });
        ChlorineGasBlock.setClientParticleSpawner((level, pos) -> {
            var player = Minecraft.getInstance().player;
            if (player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ASHGLASSES.get())) {
                level.addParticle(ModParticles.CHLORINE_CLOUD.get(),
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        0.0D, 0.0D, 0.0D);
            }
        });
    }

    private static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticles.VOMIT.get(), (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> {
            BlockState state = level.random.nextBoolean()
                    ? Blocks.LIME_TERRACOTTA.defaultBlockState()
                    : Blocks.GREEN_TERRACOTTA.defaultBlockState();
            return createVomitParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, state);
        });
        event.registerSpecial(ModParticles.BLOOD_VOMIT.get(),
                (type, level, x, y, z, xSpeed, ySpeed, zSpeed) -> createVomitParticle(
                        level,
                        x,
                        y,
                        z,
                        xSpeed,
                        ySpeed,
                        zSpeed,
                        Blocks.REDSTONE_BLOCK.defaultBlockState()
                ));
        event.registerSpriteSet(ModParticles.GIBLET.get(), GibletParticle.Provider::new);
        event.registerSpriteSet(ModParticles.ASHES.get(), AshParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLACK_POWDER_SMOKE.get(), BlackPowderSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticles.BLACK_POWDER_SPARK.get(), BlackPowderSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.TAU_SPARK.get(), TauSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticles.TAU_HADRON.get(), TauHadronParticle.Provider::new);
        event.registerSpriteSet(ModParticles.CHLORINE_CLOUD.get(), ChlorineCloudParticle.Provider::new);
        event.registerSpriteSet(ModParticles.GAS_FLAME_LARGE.get(),
                sprites -> new GasFlameParticle.Provider(sprites, 8.0F));
        event.registerSpriteSet(ModParticles.GAS_FLAME_SMALL.get(),
                sprites -> new GasFlameParticle.Provider(sprites, 4.0F));
        event.registerSpriteSet(ModParticles.FLAMETHROWER_FIRE.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, false));
        event.registerSpriteSet(ModParticles.FLAMETHROWER_BALEFIRE.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, true));
    }

    private static Particle createVomitParticle(
            net.minecraft.client.multiplayer.ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            BlockState state
    ) {
        TerrainParticle particle = new TerrainParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, state);
        particle.setParticleSpeed(xSpeed, ySpeed, zSpeed);
        particle.setLifetime(150 + level.random.nextInt(50));
        return particle;
    }
}
