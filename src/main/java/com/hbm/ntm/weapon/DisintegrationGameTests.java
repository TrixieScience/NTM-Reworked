package com.hbm.ntm.weapon;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.radiation.ModDamageTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(HbmNtm.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DisintegrationGameTests {
    private DisintegrationGameTests() { }

    @GameTest(template = "empty")
    public static void energyDamageChoosesTheSourceBonesAndAshTreatment(GameTestHelper helper) {
        Player victim = helper.makeMockPlayer(GameType.SURVIVAL);
        var damageSources = helper.getLevel().damageSources();
        helper.assertTrue(WeaponDeathEffects.effectFor(victim,
                                damageSources.source(ModDamageTypes.LASER))
                                == WeaponDeathEffects.Effect.PULVERIZE
                        && WeaponDeathEffects.effectFor(victim,
                                damageSources.source(ModDamageTypes.ELECTRIC))
                                == WeaponDeathEffects.Effect.PULVERIZE,
                "laser and electric deaths must use the bright pulverized skeleton");
        helper.assertTrue(WeaponDeathEffects.effectFor(victim,
                                damageSources.source(ModDamageTypes.PLASMA))
                                == WeaponDeathEffects.Effect.CREMATE
                        && WeaponDeathEffects.effectFor(victim,
                                damageSources.source(ModDamageTypes.FLAMETHROWER))
                                == WeaponDeathEffects.Effect.CREMATE,
                "plasma and flamethrower deaths must use the dim cremated skeleton");
        helper.assertTrue(WeaponDeathEffects.effectFor(victim,
                                damageSources.source(ModDamageTypes.BULLET))
                                == WeaponDeathEffects.Effect.NONE,
                "ordinary bullet deaths must not trigger energy disintegration");

        WeaponStatusEvents.applyFire(victim, 100);
        helper.assertTrue(WeaponDeathEffects.effectFor(victim, damageSources.onFire())
                        == WeaponDeathEffects.Effect.CREMATE,
                "a delayed HBM fire-status death must retain the source cremation effect");
        helper.succeed();
    }
}
