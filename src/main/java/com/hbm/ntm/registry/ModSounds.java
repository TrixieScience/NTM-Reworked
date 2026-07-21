package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, HbmNtm.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_VOMIT = SOUNDS.register(
            "player.vomit",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "player.vomit")
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> PRESS_OPERATE = SOUNDS.register(
            "block.pressoperate",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.pressoperate")
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> RESEARCH_REACTOR_COVER =
            register("block.rbmk_az5_cover");

    public static final DeferredHolder<SoundEvent, SoundEvent> WARN_OVERSPEED = SOUNDS.register(
            "block.warn_overspeed",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.warn_overspeed")
            )
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> TECH_BOOP = register("item.tech_boop");
    public static final DeferredHolder<SoundEvent, SoundEvent> TECH_BLEEP = register("item.tech_bleep");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_1 = register("item.geiger1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_2 = register("item.geiger2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_3 = register("item.geiger3");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_4 = register("item.geiger4");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_5 = register("item.geiger5");
    public static final DeferredHolder<SoundEvent, SoundEvent> GEIGER_6 = register("item.geiger6");
    public static final DeferredHolder<SoundEvent, SoundEvent> SYRINGE = register("item.syringe");
    public static final DeferredHolder<SoundEvent, SoundEvent> RADAWAY = register("item.radaway");
    public static final DeferredHolder<SoundEvent, SoundEvent> GASMASK_SCREW = register("item.gasmask_screw");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGE_START = register("weapon.fstbmb_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHARGE_PING = register("weapon.fstbmb_ping");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_SMALL_NEAR = register("weapon.explosion_small_near");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_SMALL_FAR = register("weapon.explosion_small_far");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_LARGE_NEAR = register("weapon.explosion_large_near");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXPLOSION_LARGE_FAR = register("weapon.explosion_large_far");
    public static final DeferredHolder<SoundEvent, SoundEvent> NUCLEAR_EXPLOSION = register("weapon.nuclear_explosion");
    // Source Landmine mine_fat blast: hbm:weapon.mukeExplosion (the mini-nuke "muke" report).
    public static final DeferredHolder<SoundEvent, SoundEvent> MUKE_EXPLOSION = register("weapon.muke_explosion");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_POWDER_FIRE = register("weapon.fire.blackpowder");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_PISTOL_FIRE = register("weapon.fire.pistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RIFLE_FIRE = register("weapon.fire.rifle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_HEAVY_RIFLE_FIRE =
            register("weapon.fire.rifleheavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MINIGUN_FIRE = register("weapon.calshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ASSAULT_FIRE = register("weapon.fire.assault");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RIFLE_SILENCER =
            register("weapon.fire.silenced");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_AMAT_FIRE =
            register("weapon.fire.amat");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_AMAT_SILENCER =
            register("weapon.fire.amat_silenced");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_M2_FIRE =
            register("turret.chekhov_fire");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_M2_EQUIP =
            register("turret.howard_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SWITCH_MODE_1 = register("weapon.switchmode1");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SWITCH_MODE_2 = register("weapon.switchmode2");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GREASEGUN_FIRE = register("weapon.fire.greasegun");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_STAR_F_FIRE = register("weapon.fire.pistollight");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_UZI_FIRE = register("weapon.fire.uzi");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_FIRE = register("weapon.fire.shotgun");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LIBERATOR_FIRE = register("weapon.fire.shotgunalt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SPAS_FIRE = register("weapon.shotgunshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHREDDER_FIRE = register("weapon.fire.shotgunauto");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHREDDER_CYCLE = register("weapon.fire.shreddercycle");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_TESLA_FIRE = register("weapon.fire.tesla");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LASER_PISTOL_FIRE =
            register("weapon.fire.laserpistol");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISINTEGRATION =
            register("weapon.fire.disintegration");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESLA_BLAST = register("entity.ufo_blast");
    public static final DeferredHolder<SoundEvent, SoundEvent> TESLA_YOMI = register("weapon.tesla_yomi");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_UNDERBARREL_FIRE = register("weapon.hk_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_CONGO_FIRE = register("weapon.gl_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MK108_FIRE = register("weapon.fire.mk108");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_ROCKET_FIRE = register("weapon.rpg_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_STINGER_LOCK =
            register("weapon.fire.lockon");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_GRENADE_RELOAD = register("weapon.gl_reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_HEAVY_REVOLVER_FIRE =
            register("weapon.fire.heavy_revolver");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SMACK = register("weapon.fire.smack");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_B92_FIRE = register("weapon.sparkshoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_B92_RELOAD = register("weapon.b92reload");
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_BANG = register("weapon.laserbang");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMMOLATOR_IGNITE =
            register("weapon.immolator_ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> IMMOLATOR_SHOOT =
            register("weapon.immolator_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_METAL = register("step.metal");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_IRON_JUMP = register("step.iron_jump");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEP_IRON_LAND = register("step.iron_land");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_COCK = register("weapon.reload.revolvercock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_CLOSE = register("weapon.reload.revolverclose");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_REVOLVER_SPIN = register("weapon.reload.revolverspin");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_SMALL_INSERT = register("weapon.reload.magsmallinsert");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_SMALL_REMOVE = register("weapon.reload.magsmallremove");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_DRY_FIRE = register("weapon.reload.dryfireclick");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LEVER_COCK = register("weapon.reload.levercock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_LOAD = register("weapon.reload.shotgunreload");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_COCK = register("weapon.reload.shotguncock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_OPEN = register("weapon.reload.shotguncockopen");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_SHOTGUN_CLOSE = register("weapon.reload.shotguncockclose");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_WHACK = register("weapon.foley.gunwhack");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_PISTOL_COCK = register("weapon.reload.pistolcock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_REMOVE = register("weapon.reload.magremove");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_MAG_INSERT = register("weapon.reload.maginsert");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_IMPACT = register("weapon.reload.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_LATCH_OPEN = register("weapon.reload.openlatch");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_CANISTER_INSERT =
            register("weapon.reload.insert_canister");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_PRESSURE_VALVE =
            register("weapon.reload.pressure_valve");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_FLAMER_LOOP =
            register("weapon.fire.flame_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_BOLT_OPEN = register("weapon.reload.boltopen");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_BOLT_CLOSE = register("weapon.reload.boltclose");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RIFLE_COCK = register("weapon.reload.riflecock");
    public static final DeferredHolder<SoundEvent, SoundEvent> GUN_RICOCHET = register("weapon.ricochet");
    // Sexy (legendary auto shotgun) whiskey inspect drinking foley.
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_GULP = register("player.gulp");
    public static final DeferredHolder<SoundEvent, SoundEvent> PLAYER_GROAN = register("player.groan");

    public static final DeferredHolder<SoundEvent, SoundEvent> MOTOR = register("block.motor");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEBRIS = register("block.debris");
    public static final DeferredHolder<SoundEvent, SoundEvent> BUILDING_EXPLOSION =
            register("entity.old_explosion");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENGINE = SOUNDS.register(
            "block.engine", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.engine"), 10F));
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_HUM = SOUNDS.register(
            "block.electrichum", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.electrichum"), 7.5F));
    public static final DeferredHolder<SoundEvent, SoundEvent> FENSU_HUM = SOUNDS.register(
            "block.fensu_hum", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.fensu_hum"), 25F));
    public static final DeferredHolder<SoundEvent, SoundEvent> ARC_FURNACE_LID_START = SOUNDS.register(
            "block.arc_furnace_lid_start", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.arc_furnace_lid_start"), 15F));
    public static final DeferredHolder<SoundEvent, SoundEvent> ARC_FURNACE_LID_STOP = SOUNDS.register(
            "block.arc_furnace_lid_stop", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.arc_furnace_lid_stop"), 15F));
    public static final DeferredHolder<SoundEvent, SoundEvent> PIPE_PLACED = register("block.pipe_placed");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER = register("block.boiler");
    public static final DeferredHolder<SoundEvent, SoundEvent> BOILER_GROAN = register("block.boiler_groan");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEMPLANT_OPERATE = register("block.chemplantoperate");
    public static final DeferredHolder<SoundEvent, SoundEvent> FURNACE_COMBINATION =
            register("weapon.flamethrower_shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> CENTRIFUGE_OPERATE = SOUNDS.register(
            "block.centrifugeoperate", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.centrifugeoperate"), 10F));
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM_ENGINE_OPERATE =
            register("block.steamengineoperate");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBUSTION_ENGINE_OPERATE =
            register("block.combustion_engine_operate");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LEVER = register("block.turbine_lever");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_LARGE_LOOP = SOUNDS.register(
            "block.turbine_large_loop", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.turbine_large_loop"), 20F));
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_GAS_STARTUP =
            register("block.turbinegas_startup");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_GAS_RUNNING = SOUNDS.register(
            "block.turbinegas_running", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.turbinegas_running"), 20F));
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_GAS_SHUTDOWN =
            register("block.turbinegas_shutdown");
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBOFAN_OPERATE = SOUNDS.register(
            "block.turbofanoperate", () -> SoundEvent.createFixedRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block.turbofanoperate"), 50F));
    public static final DeferredHolder<SoundEvent, SoundEvent> TURBOFAN_DAMAGE = register("block.damage");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_START = register("block.assemblerstart");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_STOP = register("block.assemblerstop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_STRIKE_1 = register("block.assemblerstrike1");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASSEMBLER_STRIKE_2 = register("block.assemblerstrike2");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_HATCH = register("alarm.hatch");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_AUTOPILOT = register("alarm.autopilot");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_AMS = register("alarm.ams_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_BLAST_DOOR = register("alarm.blast_door");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_APC_LOOP = register("alarm.apc_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_KLAXON = register("alarm.klaxon");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_KLAXON_A = register("alarm.klaxon_a");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_KLAXON_B = register("alarm.klaxon_b");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_REGULAR = register("alarm.regular_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_CLASSIC = register("alarm.classic");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_BANK = register("alarm.bank_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_BEEP = register("alarm.beep_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_CONTAINER = register("alarm.container_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_SWEEP = register("alarm.sweep_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_STRIDER = register("alarm.strider_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_AIR_RAID = register("alarm.air_raid");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_NOSTROMO = register("alarm.nostromo_siren");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_EAS = register("alarm.eas_alarm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_APC_PASS = register("alarm.apc_pass");
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_RAZORTRAIN = register("alarm.razortrain_horn");

    public static final DeferredHolder<SoundEvent, SoundEvent> UPGRADE_PLUG = SOUNDS.register(
            "item.upgrade_plug",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "item.upgrade_plug")
            )
    );

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, id)));
    }

    public static void register(IEventBus modEventBus) {
        SOUNDS.register(modEventBus);
    }
}
