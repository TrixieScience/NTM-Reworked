package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.B92BeamEntity;
import com.hbm.ntm.entity.B93BeamEntity;
import com.hbm.ntm.entity.BlackHoleEntity;
import com.hbm.ntm.entity.BulletEntity;
import com.hbm.ntm.entity.BuildingEntity;
import com.hbm.ntm.entity.FlattenedMobEntity;
import com.hbm.ntm.entity.FortyMillimeterProjectileEntity;
import com.hbm.ntm.entity.FlameProjectileEntity;
import com.hbm.ntm.entity.LingeringFireEntity;
import com.hbm.ntm.entity.RocketProjectileEntity;
import com.hbm.ntm.entity.CogEntity;
import com.hbm.ntm.entity.ChlorineCloudEntity;
import com.hbm.ntm.entity.MovingConveyorItemEntity;
import com.hbm.ntm.entity.MovingConveyorPackageEntity;
import com.hbm.ntm.entity.PowerFistBeamEntity;
import com.hbm.ntm.entity.PowerFistRubbleEntity;
import com.hbm.ntm.entity.ShredderBeamEntity;
import com.hbm.ntm.entity.ShredderSubmunitionEntity;
import com.hbm.ntm.entity.TeslaBeamEntity;
import com.hbm.ntm.entity.TeslaImpactEntity;
import com.hbm.ntm.entity.PrimedExplosiveEntity;
import com.hbm.ntm.entity.RagingVortexEntity;
import com.hbm.ntm.entity.SawbladeEntity;
import com.hbm.ntm.entity.ShrapnelEntity;
import com.hbm.ntm.entity.VortexEntity;
import com.hbm.ntm.nuclear.FalloutRainEntity;
import com.hbm.ntm.nuclear.FleijaCloudEntity;
import com.hbm.ntm.nuclear.FleijaExplosionEntity;
import com.hbm.ntm.nuclear.FleijaRainbowCloudEntity;
import com.hbm.ntm.nuclear.SoliniumCloudEntity;
import com.hbm.ntm.nuclear.SoliniumExplosionEntity;
import com.hbm.ntm.nuclear.MushroomCloudEntity;
import com.hbm.ntm.nuclear.BalefireEntity;
import com.hbm.ntm.nuclear.NuclearExplosionEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HbmNtm.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<NuclearExplosionEntity>> NUCLEAR_EXPLOSION = ENTITY_TYPES.register(
            "entity_nuke_explosion_mk5",
            () -> EntityType.Builder.<NuclearExplosionEntity>of(NuclearExplosionEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1).noSave()
                    .build("hbm:entity_nuke_explosion_mk5"));

    public static final DeferredHolder<EntityType<?>, EntityType<MushroomCloudEntity>> MUSHROOM_CLOUD = ENTITY_TYPES.register(
            "entity_effect_torex",
            () -> EntityType.Builder.<MushroomCloudEntity>of(MushroomCloudEntity::new, MobCategory.MISC)
                    .fireImmune().sized(1.0F, 50.0F).clientTrackingRange(64).updateInterval(1).noSave()
                    .build("hbm:entity_effect_torex"));

    public static final DeferredHolder<EntityType<?>, EntityType<FalloutRainEntity>> FALLOUT_RAIN = ENTITY_TYPES.register(
            "entity_fallout_rain",
            () -> EntityType.Builder.<FalloutRainEntity>of(FalloutRainEntity::new, MobCategory.MISC)
                    .fireImmune().sized(1.0F, 1.0F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_fallout_rain"));

    public static final DeferredHolder<EntityType<?>, EntityType<ChlorineCloudEntity>> CHLORINE_CLOUD =
            ENTITY_TYPES.register("entity_chlorine_fx",
                    () -> EntityType.Builder.<ChlorineCloudEntity>of(ChlorineCloudEntity::new, MobCategory.MISC)
                            .sized(0.2F, 0.2F).clientTrackingRange(63).updateInterval(1)
                            .build("hbm:entity_chlorine_fx"));

    public static final DeferredHolder<EntityType<?>, EntityType<PrimedExplosiveEntity>> PRIMED_EXPLOSIVE = ENTITY_TYPES.register(
            "entity_ntm_tnt_primed",
            () -> EntityType.Builder.<PrimedExplosiveEntity>of(PrimedExplosiveEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hbm:entity_ntm_tnt_primed")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<B92BeamEntity>> B92_BEAM = ENTITY_TYPES.register(
            "entity_beam_bomb",
            () -> EntityType.Builder.<B92BeamEntity>of(B92BeamEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1)
                    .build("hbm:entity_beam_bomb")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<B93BeamEntity>> B93_BEAM = ENTITY_TYPES.register(
            "entity_mod_beam",
            () -> EntityType.Builder.<B93BeamEntity>of(B93BeamEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1)
                    .build("hbm:entity_mod_beam")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<PowerFistBeamEntity>> POWER_FIST_LASER_BEAM =
            ENTITY_TYPES.register("entity_laser_beam",
                    () -> EntityType.Builder.<PowerFistBeamEntity>of(PowerFistBeamEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1)
                            .build("hbm:entity_laser_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<PowerFistBeamEntity>> POWER_FIST_MINER_BEAM =
            ENTITY_TYPES.register("entity_miner_beam",
                    () -> EntityType.Builder.<PowerFistBeamEntity>of(PowerFistBeamEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1)
                            .build("hbm:entity_miner_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<PowerFistRubbleEntity>> POWER_FIST_RUBBLE =
            ENTITY_TYPES.register("entity_rubble",
                    () -> EntityType.Builder.<PowerFistRubbleEntity>of(PowerFistRubbleEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F).clientTrackingRange(63).updateInterval(1)
                            .build("hbm:entity_rubble"));

    public static final DeferredHolder<EntityType<?>, EntityType<FlattenedMobEntity>> FLATTENED_MOB =
            ENTITY_TYPES.register("entity_flattened_mob",
                    () -> EntityType.Builder.<FlattenedMobEntity>of(FlattenedMobEntity::new, MobCategory.MISC)
                            .fireImmune().sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(10)
                            .build("hbm:entity_flattened_mob"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE = ENTITY_TYPES.register(
            "entity_black_hole",
            () -> EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1)
                    .build("hbm:entity_black_hole")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<VortexEntity>> VORTEX = ENTITY_TYPES.register(
            "entity_vortex",
            () -> EntityType.Builder.<VortexEntity>of(VortexEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1)
                    .build("hbm:entity_vortex")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RagingVortexEntity>> RAGING_VORTEX = ENTITY_TYPES.register(
            "entity_raging_vortex",
            () -> EntityType.Builder.<RagingVortexEntity>of(RagingVortexEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1)
                    .build("hbm:entity_raging_vortex")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<FleijaExplosionEntity>> FLEIJA_EXPLOSION = ENTITY_TYPES.register(
            "entity_nuke_mk3",
            () -> EntityType.Builder.<FleijaExplosionEntity>of(FleijaExplosionEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_nuke_mk3")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<FleijaRainbowCloudEntity>> FLEIJA_RAINBOW_CLOUD = ENTITY_TYPES.register(
            "entity_cloud_rainbow",
            () -> EntityType.Builder.<FleijaRainbowCloudEntity>of(FleijaRainbowCloudEntity::new, MobCategory.MISC)
                    .fireImmune().sized(1.0F, 4.0F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_cloud_rainbow")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<FleijaCloudEntity>> FLEIJA_CLOUD = ENTITY_TYPES.register(
            "entity_cloud_fleija",
            () -> EntityType.Builder.<FleijaCloudEntity>of(FleijaCloudEntity::new, MobCategory.MISC)
                    .fireImmune().sized(1.0F, 4.0F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_cloud_fleija")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SoliniumExplosionEntity>> SOLINIUM_EXPLOSION = ENTITY_TYPES.register(
            "entity_nuke_solinium",
            () -> EntityType.Builder.<SoliniumExplosionEntity>of(SoliniumExplosionEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_nuke_solinium")
    );

    // Source EntityMappings registers EntityCloudSolinium under the same "entity_cloud_rainbow"
    // name as the FLEIJA rainbow cloud (a 1.7.10 duplicate-name quirk). Modern registry names must
    // be unique, so the placed Blue Rinse cloud takes its own id.
    public static final DeferredHolder<EntityType<?>, EntityType<SoliniumCloudEntity>> SOLINIUM_CLOUD = ENTITY_TYPES.register(
            "entity_cloud_solinium",
            () -> EntityType.Builder.<SoliniumCloudEntity>of(SoliniumCloudEntity::new, MobCategory.MISC)
                    .fireImmune().sized(1.0F, 4.0F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_cloud_solinium")
    );

    // Source EntityMappings: EntityBalefire registered as "entity_balefire" (tracking 1000; the family's
    // logic entities are capped at 64 in the target, and this one is invisible).
    public static final DeferredHolder<EntityType<?>, EntityType<BalefireEntity>> BALEFIRE = ENTITY_TYPES.register(
            "entity_balefire",
            () -> EntityType.Builder.<BalefireEntity>of(BalefireEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1)
                    .build("hbm:entity_balefire")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BulletEntity>> BULLET = ENTITY_TYPES.register(
            "entity_bullet_mk4",
            () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .build("hbm:entity_bullet_mk4")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BuildingEntity>> BUILDING =
            ENTITY_TYPES.register("entity_falling_building",
                    () -> EntityType.Builder.<BuildingEntity>of(BuildingEntity::new, MobCategory.MISC)
                            .fireImmune().sized(0.25F, 0.25F).clientTrackingRange(63).updateInterval(1)
                            .build("hbm:entity_falling_building"));

    public static final DeferredHolder<EntityType<?>, EntityType<FortyMillimeterProjectileEntity>> FORTY_MILLIMETER_PROJECTILE =
            ENTITY_TYPES.register("entity_bullet_40mm",
                    () -> EntityType.Builder.<FortyMillimeterProjectileEntity>of(
                                    FortyMillimeterProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F).clientTrackingRange(16).updateInterval(1)
                            .build("hbm:entity_bullet_40mm"));

    public static final DeferredHolder<EntityType<?>, EntityType<FlameProjectileEntity>> FLAME_PROJECTILE =
            ENTITY_TYPES.register("entity_bullet_flamer",
                    () -> EntityType.Builder.<FlameProjectileEntity>of(
                                    FlameProjectileEntity::new, MobCategory.MISC)
                            .fireImmune().sized(0.125F, 0.125F).clientTrackingRange(16).updateInterval(1)
                            .build("hbm:entity_bullet_flamer"));

    public static final DeferredHolder<EntityType<?>, EntityType<RocketProjectileEntity>> ROCKET_PROJECTILE =
            ENTITY_TYPES.register("entity_bullet_rocket",
                    () -> EntityType.Builder.<RocketProjectileEntity>of(
                                    RocketProjectileEntity::new, MobCategory.MISC)
                            .fireImmune().sized(0.5F, 0.5F).clientTrackingRange(16).updateInterval(1)
                            .build("hbm:entity_bullet_rocket"));

    public static final DeferredHolder<EntityType<?>, EntityType<LingeringFireEntity>> LINGERING_FIRE =
            ENTITY_TYPES.register("entity_fire_lingering",
                    () -> EntityType.Builder.<LingeringFireEntity>of(LingeringFireEntity::new, MobCategory.MISC)
                            .fireImmune().sized(0.1F, 0.1F).clientTrackingRange(16).updateInterval(1)
                            .build("hbm:entity_fire_lingering"));

    public static final DeferredHolder<EntityType<?>, EntityType<ShredderBeamEntity>> SHREDDER_BEAM =
            ENTITY_TYPES.register("entity_bullet_beam",
                    () -> EntityType.Builder.<ShredderBeamEntity>of(ShredderBeamEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1).noSave()
                            .build("hbm:entity_bullet_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<ShredderSubmunitionEntity>> SHREDDER_SUBMUNITION =
            ENTITY_TYPES.register("entity_shredder_submunition",
                    () -> EntityType.Builder.<ShredderSubmunitionEntity>of(ShredderSubmunitionEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(16).updateInterval(1)
                            .build("hbm:entity_shredder_submunition"));

    // The source shared EntityBulletBeamBase between Shredder and Tesla. Registries prefer one name per thing.
    public static final DeferredHolder<EntityType<?>, EntityType<TeslaBeamEntity>> TESLA_BEAM =
            ENTITY_TYPES.register("entity_tesla_beam",
                    () -> EntityType.Builder.<TeslaBeamEntity>of(TeslaBeamEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(63).updateInterval(1).noSave()
                            .build("hbm:entity_tesla_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<TeslaImpactEntity>> TESLA_IMPACT =
            ENTITY_TYPES.register("entity_tesla_impact",
                    () -> EntityType.Builder.<TeslaImpactEntity>of(TeslaImpactEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F).clientTrackingRange(63).updateInterval(1).noSave()
                            .build("hbm:entity_tesla_impact"));

    public static final DeferredHolder<EntityType<?>, EntityType<CogEntity>> COG = ENTITY_TYPES.register(
            "cog",
            () -> EntityType.Builder.<CogEntity>of(CogEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    // Modern tracking range is measured in chunks; ten chunks approximates
                    // Keep the 150-block tracking radius or distant rockets become philosophy.
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("hbm:cog")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<SawbladeEntity>> SAWBLADE = ENTITY_TYPES.register(
            "entity_stray_saw",
            () -> EntityType.Builder.<SawbladeEntity>of(SawbladeEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).clientTrackingRange(10).updateInterval(1)
                    .build("hbm:entity_stray_saw")
    );

    // Source EntityShrapnel: fire-immune and never saved (writeToNBTOptional() == false).
    public static final DeferredHolder<EntityType<?>, EntityType<ShrapnelEntity>> SHRAPNEL = ENTITY_TYPES.register(
            "entity_shrapnel",
            () -> EntityType.Builder.<ShrapnelEntity>of(ShrapnelEntity::new, MobCategory.MISC)
                    .fireImmune().sized(0.25F, 0.25F).clientTrackingRange(63).updateInterval(1).noSave()
                    .build("hbm:entity_shrapnel")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<MovingConveyorItemEntity>> MOVING_CONVEYOR_ITEM =
            ENTITY_TYPES.register("moving_conveyor_item",
                    () -> EntityType.Builder.<MovingConveyorItemEntity>of(MovingConveyorItemEntity::new,
                                    MobCategory.MISC)
                            .sized(0.375F, 0.375F).clientTrackingRange(10).updateInterval(1)
                            .build("hbm:moving_conveyor_item"));

    public static final DeferredHolder<EntityType<?>, EntityType<MovingConveyorPackageEntity>> MOVING_CONVEYOR_PACKAGE =
            ENTITY_TYPES.register("moving_conveyor_package",
                    () -> EntityType.Builder.<MovingConveyorPackageEntity>of(MovingConveyorPackageEntity::new,
                                    MobCategory.MISC)
                            .sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(1)
                            .build("hbm:moving_conveyor_package"));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
