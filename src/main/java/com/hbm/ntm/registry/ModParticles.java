package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, HbmNtm.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VOMIT =
            PARTICLES.register("vomit", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_VOMIT =
            PARTICLES.register("blood_vomit", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GIBLET =
            PARTICLES.register("giblet", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ASHES =
            PARTICLES.register("ashes", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLACK_POWDER_SMOKE =
            PARTICLES.register("black_powder_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLACK_POWDER_SPARK =
            PARTICLES.register("black_powder_spark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TAU_SPARK =
            PARTICLES.register("tau_spark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TAU_HADRON =
            PARTICLES.register("tau_hadron", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHLORINE_CLOUD =
            PARTICLES.register("chlorine_cloud", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAS_FLAME_LARGE =
            PARTICLES.register("gas_flame_large", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GAS_FLAME_SMALL =
            PARTICLES.register("gas_flame_small", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLAMETHROWER_FIRE =
            PARTICLES.register("flamethrower_fire", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FLAMETHROWER_BALEFIRE =
            PARTICLES.register("flamethrower_balefire", () -> new SimpleParticleType(true));

    private ModParticles() {
    }

    public static void register(IEventBus modEventBus) {
        PARTICLES.register(modEventBus);
    }
}
