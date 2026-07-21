package com.hbm.ntm.weapon;

import com.hbm.ntm.network.DisintegrationPayload;
import com.hbm.ntm.radiation.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WeaponDeathEffects {
    private static final String DISINTEGRATED = "hbm_disintegrated";

    private WeaponDeathEffects() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(WeaponDeathEffects::onDeath);
    }

    private static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level)
                || victim.getPersistentData().getBoolean(DISINTEGRATED)) return;

        Effect effect = effectFor(victim, event.getSource());
        if (effect == Effect.NONE) return;
        victim.getPersistentData().putBoolean(DISINTEGRATED, true);
        PacketDistributor.sendToPlayersNear(level, null, victim.getX(), victim.getY(), victim.getZ(),
                100.0D, new DisintegrationPayload(victim.getId(), effect == Effect.CREMATE));
    }

    static Effect effectFor(LivingEntity victim, DamageSource source) {
        if (source.is(ModDamageTypes.LASER) || source.is(ModDamageTypes.ELECTRIC)) {
            return Effect.PULVERIZE;
        }
        if (source.is(ModDamageTypes.PLASMA) || source.is(ModDamageTypes.FLAMETHROWER)) {
            return Effect.CREMATE;
        }
        if (source.is(DamageTypeTags.IS_FIRE)
                && (WeaponStatusEvents.fireTicks(victim) > 0
                || WeaponStatusEvents.phosphorusTicks(victim) > 0
                || WeaponStatusEvents.balefireTicks(victim) > 0)) {
            return Effect.CREMATE;
        }
        return Effect.NONE;
    }

    enum Effect { NONE, PULVERIZE, CREMATE }
}
