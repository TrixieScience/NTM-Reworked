package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.hbm.ntm.registry.ModFluids;

public class ClientFluidRegistration {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    private static final ResourceLocation LIQUID_STILL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/liquid_still");
    private static final ResourceLocation LIQUID_FLOW = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/liquid_flow");

    private static final ResourceLocation OIL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/oil");
    private static final ResourceLocation HOTOIL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/hotoil");
    private static final ResourceLocation HEAVYOIL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/heavyoil");
    private static final ResourceLocation NAPHTHA = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/naphtha");
    private static final ResourceLocation BITUMEN = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/bitumen");
    private static final ResourceLocation SMEAR = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/smear");
    private static final ResourceLocation HEATINGOIL = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/heatingoil");
    private static final ResourceLocation GASOLINE = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/gasoline");
    private static final ResourceLocation GASOLINE_LEADED = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/gasoline_leaded");
    private static final ResourceLocation COALGAS = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/coalgas");
    private static final ResourceLocation COALGAS_LEADED = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/coalgas_leaded");
    private static final ResourceLocation PETROLEUM = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/petroleum");
    private static final ResourceLocation UNSATURATEDS = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/unsaturateds");
    private static final ResourceLocation FLUE = ResourceLocation.fromNamespaceAndPath(HbmNtm.MOD_ID, "block/fluid/flue");

    private ClientFluidRegistration() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ClientFluidRegistration::registerExtensions);
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0x96FFDADA; }
        }, ModFluids.AIR_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0x966e6e6e; }
        }, ModFluids.SMOKE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFFFDADA; }
        }, ModFluids.AIRBLAST_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFE5E5E5; }
        }, ModFluids.STEAM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFE7D6D6; }
        }, ModFluids.HOTSTEAM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFE7B7B7; }
        }, ModFluids.SUPERHOTSTEAM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFE39393; }
        }, ModFluids.ULTRAHOTSTEAM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFFD8FCFF; }
        }, ModFluids.COOLANT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF99525E; }
        }, ModFluids.COOLANT_HOT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFFFF7AA; }
        }, ModFluids.PEROXIDE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFFB0AA64; }
        }, ModFluids.SULFURIC_ACID_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return OIL; }
            @Override
            public ResourceLocation getFlowingTexture() {return OIL; }
        }, ModFluids.OIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return HOTOIL; }
            @Override
            public ResourceLocation getFlowingTexture() {return HOTOIL; }
        }, ModFluids.HOTOIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return HEAVYOIL; }
            @Override
            public ResourceLocation getFlowingTexture() {return HEAVYOIL; }
        }, ModFluids.HEAVYOIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return NAPHTHA; }
            @Override
            public ResourceLocation getFlowingTexture() {return NAPHTHA; }
        }, ModFluids.NAPHTHA_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF8C7451; }
        }, ModFluids.LIGHTOIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return BITUMEN; }
            @Override
            public ResourceLocation getFlowingTexture() {return BITUMEN; }
        }, ModFluids.BITUMEN_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return SMEAR; }
            @Override
            public ResourceLocation getFlowingTexture() {return SMEAR; }
        }, ModFluids.SMEAR_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return HEATINGOIL; }
            @Override
            public ResourceLocation getFlowingTexture() {return HEATINGOIL; }
        }, ModFluids.HEATINGOIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF847D54; }
        }, ModFluids.WOODOIL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF51694F; }
        }, ModFluids.COALCREOSOTE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF606060; }
        }, ModFluids.LUBRICANT_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFFF2EED5; }
        }, ModFluids.DIESEL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFFFFA5D2; }
        }, ModFluids.KEROSENE_TYPE.get());

        event.registerFluidType(textured(GASOLINE), ModFluids.GASOLINE_TYPE.get());
        event.registerFluidType(textured(GASOLINE_LEADED), ModFluids.GASOLINE_LEADED_TYPE.get());
        event.registerFluidType(textured(COALGAS), ModFluids.COALGAS_TYPE.get());
        event.registerFluidType(textured(COALGAS_LEADED), ModFluids.COALGAS_LEADED_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return PETROLEUM; }
            @Override
            public ResourceLocation getFlowingTexture() {return PETROLEUM; }
        }, ModFluids.PETROLEUM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFFFFFEED; }
        }, ModFluids.GAS_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF141414; }
        }, ModFluids.CARBONDIOXIDE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF4286F4; }
        }, ModFluids.HYDROGEN_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF3A6EA5; }
        }, ModFluids.DEUTERIUM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF2FB24C; }
        }, ModFluids.TRITIUM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF4FFFFC; }
        }, ModFluids.SAS3_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF7DE7FF; }
        }, ModFluids.CRYOGEL_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return UNSATURATEDS; }
            @Override
            public ResourceLocation getFlowingTexture() {return UNSATURATEDS; }
        }, ModFluids.UNSATURATEDS_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF445772; }
        }, ModFluids.SPENTSTEAM_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return FLUE; }
            @Override
            public ResourceLocation getFlowingTexture() {return FLUE; }
        }, ModFluids.FLUE_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFF808080; }
        }, ModFluids.MERCURY_TYPE.get());


        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return LIQUID_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return LIQUID_FLOW; }
            @Override
            public int getTintColor() {return 0xFFB22424; }
        }, ModFluids.BLOOD_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF98BDF9; }
        }, ModFluids.OXYGEN_TYPE.get());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {return WATER_STILL; }
            @Override
            public ResourceLocation getFlowingTexture() {return WATER_FLOW; }
            @Override
            public int getTintColor() {return 0xFF938541; }
        }, ModFluids.PAIN_TYPE.get());
    }

    private static IClientFluidTypeExtensions textured(ResourceLocation texture) {
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return texture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return texture;
            }
        };
    }
}
