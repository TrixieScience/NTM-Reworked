package com.hbm.ntm;

import com.hbm.ntm.armor.ArmorModEvents;
import com.hbm.ntm.armor.EnvsuitArmorEvents;
import com.hbm.ntm.armor.DntArmorEvents;
import com.hbm.ntm.client.ClientFluidRegistration;
import com.hbm.ntm.client.ClientMachineRegistration;
import com.hbm.ntm.client.ClientMoonEvents;
import com.hbm.ntm.client.ClientSunEvents;
import com.hbm.ntm.client.GuideBookClient;
import com.hbm.ntm.client.ClientParticleRegistration;
import com.hbm.ntm.client.ClientRadiationRegistration;
import com.hbm.ntm.client.ClientWeaponRegistration;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.config.HbmConfig;
import com.hbm.ntm.compat.BombImpactFuseEvents;
import com.hbm.ntm.data.ModDataGenerators;
import com.hbm.ntm.energy.HeEnergyEvents;
import com.hbm.ntm.hazard.HazardRegistry;
import com.hbm.ntm.hazard.CoalDustEvents;
import com.hbm.ntm.guide.GuideBookEvents;
import com.hbm.ntm.inventory.PressCapabilities;
import com.hbm.ntm.item.DetonatorEvents;
import com.hbm.ntm.network.ModNetwork;
import com.hbm.ntm.pollution.PollutionEvents;
import com.hbm.ntm.block.TankLadderEvents;
import com.hbm.ntm.radiation.RadiationEvents;
import com.hbm.ntm.recipe.AssemblyRecipes;
import com.hbm.ntm.ror.RttySystem;
import com.hbm.ntm.registry.ModArmorMaterials;
import com.hbm.ntm.registry.ModAttachments;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModCreativeTabs;
import com.hbm.ntm.registry.ModEntities;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModFeatures;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenus;
import com.hbm.ntm.registry.ModParticles;
import com.hbm.ntm.registry.ModPlacementModifiers;
import com.hbm.ntm.registry.ModRecipeSerializers;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.registry.ModStats;
import com.hbm.ntm.thermal.FuelEvents;
import com.hbm.ntm.weapon.WeaponDeathEffects;
import com.hbm.ntm.weapon.WeaponStatusEvents;
import com.hbm.ntm.world.MoonDestructionEvents;
import com.hbm.ntm.world.SunDestructionEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.Random;

@Mod(HbmNtm.MOD_ID)
public final class HbmNtm {
    public static final String MOD_ID = "hbm";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final int POLAROID_ID = rollPolaroidId();

    private static int rollPolaroidId() {
        Random random = new Random();
        int id;
        do {
            id = random.nextInt(18) + 1;
        } while (id == 4 || id == 9);
        return id;
    }

    public HbmNtm(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModFluids.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModPlacementModifiers.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModParticles.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModStats.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientFluidRegistration.register(modEventBus);
            ClientParticleRegistration.register(modEventBus);
            ClientMachineRegistration.register(modEventBus);
            ClientRadiationRegistration.register(modEventBus);
            ClientWeaponRegistration.register(modEventBus);
            ClientMoonEvents.register(modEventBus);
            ClientSunEvents.register();
            GuideBookClient.register();
        }
        modEventBus.addListener(ModDataGenerators::gatherData);
        modEventBus.addListener(ModNetwork::register);
        modEventBus.addListener(PressCapabilities::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, HbmConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, HbmClientConfig.SPEC);
        HazardRegistry.bootstrap();
        CoalDustEvents.register();
        ArmorModEvents.register();
        EnvsuitArmorEvents.register();
        DntArmorEvents.register();
        BombImpactFuseEvents.register();
        HeEnergyEvents.register();
        RttySystem.register();
        RadiationEvents.register();
        PollutionEvents.register();
        TankLadderEvents.register();
        FuelEvents.register();
        DetonatorEvents.register();
        GuideBookEvents.register();
        MoonDestructionEvents.register();
        SunDestructionEvents.register();
        AssemblyRecipes.register();
        WeaponStatusEvents.register();
        WeaponDeathEffects.register();

        LOGGER.info("Loaded HBM's Nuclear Tech Mod");
    }
}
