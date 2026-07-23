package com.hbm.ntm.client;

import com.hbm.ntm.block.ElectricHeaterBlock;
import com.hbm.ntm.block.AirIntakeBlock;
import com.hbm.ntm.block.HeatBoilerBlock;
import com.hbm.ntm.block.HeatExchangerBlock;
import com.hbm.ntm.block.HighPowerCondenserBlock;
import com.hbm.ntm.block.FluidBurnerBlock;
import com.hbm.ntm.block.FluidStorageTankBlock;
import com.hbm.ntm.block.DrainagePipeBlock;
import com.hbm.ntm.block.GasTurbineBlock;
import com.hbm.ntm.block.IndustrialTurbineBlock;
import com.hbm.ntm.block.SteamEngineBlock;
import com.hbm.ntm.block.PumpBlock;
import com.hbm.ntm.block.ThermalMultiblockBlock;
import com.hbm.ntm.blockentity.ElectricHeaterBlockEntity;
import com.hbm.ntm.blockentity.AirIntakeBlockEntity;
import com.hbm.ntm.blockentity.HeatBoilerBlockEntity;
import com.hbm.ntm.blockentity.HeatExchangerBlockEntity;
import com.hbm.ntm.blockentity.HighPowerCondenserBlockEntity;
import com.hbm.ntm.blockentity.FluidBurnerBlockEntity;
import com.hbm.ntm.blockentity.FluidDuctBlockEntity;
import com.hbm.ntm.blockentity.FluidUtilityBlockEntity;
import com.hbm.ntm.blockentity.FluidPumpBlockEntity;
import com.hbm.ntm.blockentity.DrainagePipeBlockEntity;
import com.hbm.ntm.blockentity.PowerGaugeBlockEntity;
import com.hbm.ntm.blockentity.FoundryOutletBlockEntity;
import com.hbm.ntm.blockentity.FluidStorageTankBlockEntity;
import com.hbm.ntm.blockentity.GasTurbineBlockEntity;
import com.hbm.ntm.blockentity.IndustrialTurbineBlockEntity;
import com.hbm.ntm.blockentity.SteamEngineBlockEntity;
import com.hbm.ntm.blockentity.SteamCondenserBlockEntity;
import com.hbm.ntm.blockentity.PumpBlockEntity;
import com.hbm.ntm.blockentity.ConverterFEtoHEBlockEntity;
import com.hbm.ntm.blockentity.ConverterHEtoFEBlockEntity;
import com.hbm.ntm.blockentity.StirlingBlockEntity;
import com.hbm.ntm.blockentity.SawmillBlockEntity;
import com.hbm.ntm.recipe.FluidBurnerFuels;
import com.hbm.ntm.item.OreDensityScannerItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientLookOverlay {
    private ClientLookOverlay() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ClientLookOverlay::render);
    }

    private static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.level == null) return;
        renderOreDensityScanner(event.getGuiGraphics(), minecraft);
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) return;
        var state = minecraft.level.getBlockState(hit.getBlockPos());
        if (state.is(ModBlocks.FOUNDRY_OUTLET.get()) || state.is(ModBlocks.FOUNDRY_SLAGTAP.get())) {
            if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof FoundryOutletBlockEntity outlet) {
                renderFoundryOutlet(event.getGuiGraphics(), minecraft, outlet);
            }
            return;
        }
        if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof FluidUtilityBlockEntity utility) {
            renderFluidUtility(event.getGuiGraphics(), minecraft, utility);
            return;
        }
        if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof PowerGaugeBlockEntity gauge) {
            renderPowerGauge(event.getGuiGraphics(), minecraft, gauge);
            return;
        }
        if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof FluidPumpBlockEntity pump) {
            renderFluidPump(event.getGuiGraphics(), minecraft, pump);
            return;
        }
        if (state.is(ModBlocks.MACHINE_DRAIN.get())) {
            var core = DrainagePipeBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof DrainagePipeBlockEntity drain) {
                renderDrainagePipe(event.getGuiGraphics(), minecraft, drain);
            }
            return;
        }
        if (state.is(ModBlocks.FLUID_DUCT_NEO.get())) {
            if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof FluidDuctBlockEntity duct) {
                renderFluidDuct(event.getGuiGraphics(), minecraft, duct);
            }
            return;
        }
        if (state.is(ModBlocks.HEATER_ELECTRIC.get())) {
            var core = ElectricHeaterBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof ElectricHeaterBlockEntity heater) {
                renderElectricHeater(event.getGuiGraphics(), minecraft, heater);
            }
            return;
        }
        if (state.is(ModBlocks.HEATER_OILBURNER.get())) {
            var core = FluidBurnerBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof FluidBurnerBlockEntity burner) {
                renderFluidBurner(event.getGuiGraphics(), minecraft, burner);
            }
            return;
        }
        if (state.is(ModBlocks.HEATER_HEATEX.get())) {
            var core = HeatExchangerBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof HeatExchangerBlockEntity exchanger) {
                renderHeatExchanger(event.getGuiGraphics(), minecraft, exchanger);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_BOILER.get())) {
            var core = HeatBoilerBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof HeatBoilerBlockEntity boiler) {
                renderBoiler(event.getGuiGraphics(), minecraft, boiler);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_SAWMILL.get())) {
            var core = ThermalMultiblockBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof SawmillBlockEntity sawmill) {
                renderSawmill(event.getGuiGraphics(), minecraft, sawmill);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_STEAM_ENGINE.get())) {
            var core = SteamEngineBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof SteamEngineBlockEntity engine) {
                renderSteamEngine(event.getGuiGraphics(), minecraft, engine);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_INDUSTRIAL_TURBINE.get())) {
            var core = IndustrialTurbineBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof IndustrialTurbineBlockEntity turbine) {
                renderIndustrialTurbine(event.getGuiGraphics(), minecraft, turbine);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_TURBINE_GAS.get())) {
            var core = GasTurbineBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof GasTurbineBlockEntity turbine) {
                renderGasTurbinePort(event.getGuiGraphics(), minecraft, turbine,
                        GasTurbineBlock.port(state));
            }
            return;
        }
        if (state.is(ModBlocks.PUMP_STEAM.get()) || state.is(ModBlocks.PUMP_ELECTRIC.get())) {
            var core = PumpBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof PumpBlockEntity pump) {
                renderPump(event.getGuiGraphics(), minecraft, pump);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_INTAKE.get())) {
            var core = AirIntakeBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof AirIntakeBlockEntity intake) {
                renderAirIntake(event.getGuiGraphics(), minecraft, intake);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_CONDENSER.get())) {
            if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof SteamCondenserBlockEntity condenser) {
                renderSteamCondenser(event.getGuiGraphics(), minecraft, condenser);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_CONDENSER_POWERED.get())) {
            var core = HighPowerCondenserBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof HighPowerCondenserBlockEntity condenser) {
                renderHighPowerCondenser(event.getGuiGraphics(), minecraft, condenser);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_FLUIDTANK.get())) {
            var core = FluidStorageTankBlock.corePosition(hit.getBlockPos(), state);
            if (minecraft.level.getBlockEntity(core) instanceof FluidStorageTankBlockEntity tank
                    && tank.damaged() && minecraft.player != null
                    && minecraft.player.getMainHandItem().is(ModItems.BLOWTORCH.get())) {
                renderTankRepair(event.getGuiGraphics(), minecraft);
            }
            return;
        }
        if (state.is(ModBlocks.MACHINE_CONVERTER_HE_FE.get())) {
            if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof ConverterHEtoFEBlockEntity converter) {
                renderConverterHEtoFE(event.getGuiGraphics(), minecraft, converter);
            };
            return;
        }
        if (state.is(ModBlocks.MACHINE_CONVERTER_FE_HE.get())) {
            if (minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof ConverterFEtoHEBlockEntity converter) {
                renderConverterFEtoHE(event.getGuiGraphics(), minecraft, converter);
            };
            return;
        }
        if (!state.is(ModBlocks.MACHINE_STIRLING.get())) return;
        var core = ThermalMultiblockBlock.corePosition(hit.getBlockPos(), state);
        if (!(minecraft.level.getBlockEntity(core) instanceof StirlingBlockEntity stirling)) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_stirling").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font, stirling.heat() + "TU/t", x, y, 0xFFFFFF, true);
        y += 10;
        graphics.drawString(minecraft.font, (stirling.hasCog() ? stirling.powerBuffer() : 0) + "HE/t",
                x, y, 0xFFFFFF, true);
        y += 10;

        double percent = (double) stirling.heat() / StirlingBlockEntity.MAX_HEAT;
        int color = percent > 1.0D ? 0xFF0000
                : ((int) (255 - 255 * percent) << 16) | ((int) (255 * percent) << 8);
        double displayed = (stirling.heat() * 1000 / StirlingBlockEntity.MAX_HEAT) / 10.0D;
        graphics.drawString(minecraft.font, displayed + "%", x, y, color, true);
        y += 10;
        if (stirling.heat() > StirlingBlockEntity.MAX_HEAT) {
            int blink = (System.currentTimeMillis() / 500L & 1L) == 0L ? 0xFF0000 : 0xFFFF00;
            graphics.drawString(minecraft.font, "! ! ! OVERSPEED ! ! !", x, y, blink, true);
            y += 10;
        }
        if (!stirling.hasCog()) {
            graphics.drawString(minecraft.font, "Gear missing!", x, y, 0xFF0000, true);
        }
    }

    private static void renderFluidDuct(GuiGraphics graphics, Minecraft minecraft,
                                        FluidDuctBlockEntity duct) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.fluid_duct_neo").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        var selection = duct.selection();
        graphics.drawString(minecraft.font,
                Component.translatable(selection.translationKey()), x, y, selection.color(), true);
    }

    private static void renderFluidUtility(GuiGraphics graphics, Minecraft minecraft,
                                           FluidUtilityBlockEntity utility) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        Component title = utility.getBlockState().getBlock().getName();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        var selection = utility.selection();
        graphics.drawString(minecraft.font, Component.translatable(selection.translationKey()),
                x, y, selection.color(), true);
        if (utility.kind() == com.hbm.ntm.block.FluidUtilityBlock.Kind.COUNTER) {
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "Counter: %,d", utility.counter()), x, y + 10, 0xFFFFFF, true);
        } else if (utility.kind() == com.hbm.ntm.block.FluidUtilityBlock.Kind.GAUGE) {
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "%,d mB/t", utility.deltaTick()), x, y + 10, 0xFFFFFF, true);
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "%,d mB/s", utility.deltaSecond()), x, y + 20, 0xFFFFFF, true);
        }
    }

    private static void renderPowerGauge(GuiGraphics graphics, Minecraft minecraft,
                                         PowerGaugeBlockEntity gauge) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        Component title = gauge.getBlockState().getBlock().getName();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font,
                com.hbm.ntm.item.BatteryPackItem.shortNumber(gauge.deltaTick()) + "HE/t",
                x, y, 0xFFFFFF, true);
        graphics.drawString(minecraft.font,
                com.hbm.ntm.item.BatteryPackItem.shortNumber(gauge.deltaSecond()) + "HE/s",
                x, y + 10, 0xFFFFFF, true);
    }

    private static void renderFluidPump(GuiGraphics graphics, Minecraft minecraft,
                                        FluidPumpBlockEntity pump) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        Component title = pump.getBlockState().getBlock().getName();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        MutableComponent flow = Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable(pump.selection().translationKey())
                        .withStyle(style -> style.withColor(pump.selection().color())))
                .append(Component.literal(" (" + pump.pressure() + " PU): "
                        + String.format(java.util.Locale.US, "%,d", pump.throughput()) + "mB/t")
                        .withStyle(ChatFormatting.RESET))
                .append(Component.literal(" ->").withStyle(ChatFormatting.RED));
        graphics.drawString(minecraft.font, flow, x, y, 0xFFFFFF, true);
        graphics.drawString(minecraft.font,
                Component.literal("Priority: ").append(Component.literal(pump.priorityName())
                        .withStyle(ChatFormatting.YELLOW)), x, y + 10, 0xFFFFFF, true);
        if (pump.amount() > 0) {
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "%,d mB buffered", pump.amount()), x, y + 20, 0xFFFFFF, true);
        }
    }

    private static void renderDrainagePipe(GuiGraphics graphics, Minecraft minecraft,
                                           DrainagePipeBlockEntity drain) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        Component title = drain.getBlockState().getBlock().getName();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        MutableComponent fluid = Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable(drain.selection().translationKey())
                        .withStyle(ChatFormatting.RESET))
                .append(Component.literal(": " + drain.amount() + "/"
                        + DrainagePipeBlockEntity.CAPACITY + "mB")
                        .withStyle(ChatFormatting.RESET));
        graphics.drawString(minecraft.font, fluid, x, y, 0xFFFFFF, true);
    }

    private static void renderFoundryOutlet(GuiGraphics graphics, Minecraft minecraft,
                                            FoundryOutletBlockEntity outlet) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String key = outlet.isSlagTap() ? "block.hbm.foundry_slagtap" : "block.hbm.foundry_outlet";
        String title = Component.translatable(key).getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x401000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFF4000, false);
        if (outlet.filter() != null) {
            Component material = Component.translatable("hbmmat." + outlet.filter().id());
            graphics.drawString(minecraft.font, Component.translatable("foundry.filter", material),
                    x, y, 0xFFFF00, true);
            y += 10;
        }
        if (outlet.invertFilter()) {
            graphics.drawString(minecraft.font, Component.translatable("foundry.invertFilter"),
                    x, y, 0xFFFF00, true);
            y += 10;
        }
        if (outlet.invertRedstone()) {
            graphics.drawString(minecraft.font, Component.translatable("foundry.inverted"),
                    x, y, 0xAA0000, true);
        }
    }

    /** Replaces the old seven persistent PlayerInformPacket rows without a per-tick network roundtrip. */
    private static void renderOreDensityScanner(GuiGraphics graphics, Minecraft minecraft) {
        if (minecraft.player == null || !minecraft.player.getInventory()
                .contains(stack -> stack.is(ModItems.ORE_DENSITY_SCANNER.get()))) return;

        int blockX = (int) Math.floor(minecraft.player.getX());
        int blockZ = (int) Math.floor(minecraft.player.getZ());
        OreDensityScannerItem.Reading reading = OreDensityScannerItem.reading(blockX, blockZ);
        Component[] lines = new Component[OreDensityScannerItem.OreType.values().length + 1];
        int longest = 0;

        for (OreDensityScannerItem.OreType type : OreDensityScannerItem.OreType.values()) {
            double density = reading.densities()[type.ordinal()];
            double truncated = (int) (density * 100.0D) / 100.0D;
            MutableComponent line = Component.empty()
                    .append(Component.translatable("item.hbm.bedrock_ore.type." + type.suffix() + ".name"))
                    .append(": " + truncated + " (")
                    .append(Component.translatable(OreDensityScannerItem.densityKey(density))
                            .withStyle(OreDensityScannerItem.densityColor(density)))
                    .append(")");
            lines[type.ordinal()] = line;
            longest = Math.max(longest, minecraft.font.width(line));
        }

        MutableComponent tier = Component.literal("Tier " + reading.tier()).withStyle(ChatFormatting.YELLOW);
        if (reading.boreFluidAmount() > 0) {
            tier.append(Component.literal(" - " + reading.boreFluidAmount() + "mB ")
                    .withStyle(ChatFormatting.RESET));
            tier.append(Component.translatable(reading.boreFluidKey()).withStyle(ChatFormatting.RESET));
        }
        lines[lines.length - 1] = tier;
        longest = Math.max(longest, minecraft.font.width(tier));

        int x = 15;
        int y = 15;
        graphics.fill(x - 5, y - 5, x + longest + 5, y + lines.length * 10 + 2, 0x803F3F3F);
        for (int index = 0; index < lines.length; index++) {
            graphics.drawString(minecraft.font, lines[index], x, y + index * 10, 0xFFFFFF, false);
        }
    }

    private static void renderTankRepair(GuiGraphics graphics, Minecraft minecraft) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_fluidtank").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font, "Repair with:", x, y, 0xFFAA00, true);
        graphics.drawString(minecraft.font, "- "
                        + Component.translatable("item.hbm.plate_steel").getString() + " x6",
                x, y + 10, 0xFFFFFF, true);
    }

    private static void renderSawmill(GuiGraphics graphics, Minecraft minecraft, SawmillBlockEntity sawmill) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_sawmill").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font, sawmill.heat() + "TU/t", x, y, 0xFFFFFF, true);
        y += 10;

        double percent = (double) sawmill.heat() / SawmillBlockEntity.MAX_HEAT;
        int color = percent > 1.0D ? 0xFF0000
                : ((int) (255 - 255 * percent) << 16) | ((int) (255 * percent) << 8);
        double displayed = (sawmill.heat() * 1000 / SawmillBlockEntity.MAX_HEAT) / 10.0D;
        graphics.drawString(minecraft.font, displayed + "%", x, y, color, true);
        y += 10;

        int limiter = sawmill.progress() * 26 / SawmillBlockEntity.PROCESSING_TIME;
        StringBuilder bar = new StringBuilder("[ ");
        for (int index = 0; index < 25; index++) bar.append(index < limiter ? '▏' : ' ');
        bar.append(" ]");
        graphics.drawString(minecraft.font, bar.toString(), x, y, 0x55FF55, true);
        y += 10;

        for (int slot = 0; slot < 3; slot++) {
            var stack = sawmill.getItem(slot);
            if (stack.isEmpty()) continue;
            String marker = slot == 0 ? "-> " : "<- ";
            int stackColor = slot == 0 ? 0x55FF55 : 0xFF5555;
            String count = stack.getCount() > 1 ? " x" + stack.getCount() : "";
            graphics.drawString(minecraft.font, marker + stack.getHoverName().getString() + count,
                    x, y, stackColor, true);
            y += 10;
        }
        if (sawmill.heat() > SawmillBlockEntity.MAX_HEAT) {
            int blink = (System.currentTimeMillis() / 500L & 1L) == 0L ? 0xFF0000 : 0xFFFF00;
            graphics.drawString(minecraft.font, "! ! ! OVERSPEED ! ! !", x, y, blink, true);
            y += 10;
        }
        if (!sawmill.hasBlade()) {
            graphics.drawString(minecraft.font, "Blade missing!", x, y, 0xFF0000, true);
        }
    }

    private static void renderSteamEngine(GuiGraphics graphics, Minecraft minecraft,
                                          SteamEngineBlockEntity engine) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_steam_engine").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        String steam = Component.translatable("hbmfluid.steam").getString();
        String spent = Component.translatable("hbmfluid.spentsteam").getString();
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "-> %s: %,d / %,dmB", steam, engine.steamTank().getFluidAmount(),
                engine.steamTank().getCapacity()), x, y, 0x55FF55, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- %s: %,d / %,dmB", spent, engine.spentSteamTank().getFluidAmount(),
                engine.spentSteamTank().getCapacity()), x, y, 0xFF5555, true);
    }

    private static void renderIndustrialTurbine(GuiGraphics graphics, Minecraft minecraft,
                                                IndustrialTurbineBlockEntity turbine) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_industrial_turbine").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);

        String input = Component.translatable(turbine.grade().input().translationKey()).getString();
        String output = Component.translatable(turbine.grade().output().translationKey()).getString();
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "-> %s: %,d / %,dmB", input, turbine.inputTank().getFluidAmount(),
                turbine.inputTank().getCapacity()), x, y, 0x55FF55, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- %s: %,d / %,dmB", output, turbine.outputTank().getFluidAmount(),
                turbine.outputTank().getCapacity()), x, y, 0xFF5555, true);
        y += 10;
        double spin = net.minecraft.util.Mth.clamp(turbine.spin(), 0.0D, 1.0D);
        int spinColor = ((int) (255 - 255 * spin) << 16) | ((int) (255 * spin) << 8);
        String[] spinner = {"▖ ", "▘ ", " ▘", " ▖"};
        int frame = turbine.getPower() <= 0L || minecraft.level == null
                ? 0 : (int) ((minecraft.level.getGameTime() / 4L) % spinner.length);
        MutableComponent power = Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(com.hbm.ntm.item.BatteryPackItem.shortNumber(turbine.getPower())
                        + "HE (").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(spinner[frame] + Math.round(turbine.spin() * 100.0D) + "%")
                        .withStyle(style -> style.withColor(spinColor)))
                .append(Component.literal(")").withStyle(ChatFormatting.WHITE));
        graphics.drawString(minecraft.font, power, x, y, 0xFFFFFF, true);
    }

    private static void renderGasTurbinePort(GuiGraphics graphics, Minecraft minecraft,
                                             GasTurbineBlockEntity turbine, GasTurbineBlock.Port port) {
        if (port == GasTurbineBlock.Port.NONE) return;
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_turbinegas").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        switch (port) {
            case FUEL_LUBE -> {
                drawPortLine(graphics, minecraft, x, y, true, "hbmfluid.gas",
                        turbine.fuelAmount(), GasTurbineBlockEntity.FUEL_CAPACITY);
                drawPortLine(graphics, minecraft, x, y + 10, true, "hbmfluid.lubricant",
                        turbine.lubricantAmount(), GasTurbineBlockEntity.LUBRICANT_CAPACITY);
            }
            case WATER -> drawPortLine(graphics, minecraft, x, y, true, "block.minecraft.water",
                    turbine.waterAmount(), GasTurbineBlockEntity.WATER_CAPACITY);
            case STEAM -> drawPortLine(graphics, minecraft, x, y, false, "hbmfluid.hotsteam",
                    turbine.steamAmount(), GasTurbineBlockEntity.STEAM_CAPACITY);
            case POWER -> graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "<- Power: %,d / %,dHE", turbine.getPower(), GasTurbineBlockEntity.MAX_POWER),
                    x, y, 0xFF5555, true);
            default -> { }
        }
    }

    private static void drawPortLine(GuiGraphics graphics, Minecraft minecraft, int x, int y,
                                     boolean input, String fluidKey, int amount, int capacity) {
        String marker = input ? "-> " : "<- ";
        int color = input ? 0x55FF55 : 0xFF5555;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "%s%s: %,d / %,dmB", marker, Component.translatable(fluidKey).getString(), amount, capacity),
                x, y, color, true);
    }

    private static void renderPump(GuiGraphics graphics, Minecraft minecraft, PumpBlockEntity pump) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String key = pump.electric() ? "block.hbm.pump_electric" : "block.hbm.pump_steam";
        String title = Component.translatable(key).getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        if (pump.electric()) {
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "-> %,d / %,dHE", pump.getPower(), pump.getMaxPower()), x, y, 0x55FF55, true);
            y += 10;
        } else {
            String steam = Component.translatable("hbmfluid.steam").getString();
            String spent = Component.translatable("hbmfluid.spentsteam").getString();
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "-> %s: %,d / %,dmB", steam, pump.steamTank().getFluidAmount(),
                    pump.steamTank().getCapacity()), x, y, 0x55FF55, true);
            y += 10;
            graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                    "<- %s: %,d / %,dmB", spent, pump.spentSteamTank().getFluidAmount(),
                    pump.spentSteamTank().getCapacity()), x, y, 0xFF5555, true);
            y += 10;
        }
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- Water: %,d / %,dmB", pump.waterTank().getFluidAmount(),
                pump.waterTank().getCapacity()), x, y, 0xFF5555, true);
        y += 10;
        int blink = (System.currentTimeMillis() / 500L & 1L) == 0L ? 0xFF0000 : 0xFFFF00;
        // The old overlay hard-coded 70 even though the operating height was configurable.
        if (pump.getBlockPos().getY() > 70) {
            graphics.drawString(minecraft.font, "! ! ! ALTITUDE ! ! !", x, y, blink, true);
            y += 10;
        }
        if (!pump.onGround()) {
            graphics.drawString(minecraft.font, "! ! ! NO VALID GROUND ! ! !", x, y, blink, true);
        }
    }

    private static void renderSteamCondenser(GuiGraphics graphics, Minecraft minecraft,
                                             SteamCondenserBlockEntity condenser) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_condenser").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        String spent = Component.translatable("hbmfluid.spentsteam").getString();
        graphics.drawString(minecraft.font, "-> " + spent + ": "
                + condenser.spentSteamTank().getFluidAmount() + "/"
                + condenser.spentSteamTank().getCapacity() + "mB", x, y, 0x55FF55, true);
        y += 10;
        graphics.drawString(minecraft.font, "<- Water: " + condenser.waterTank().getFluidAmount() + "/"
                + condenser.waterTank().getCapacity() + "mB", x, y, 0xFF5555, true);
    }

    private static void renderHighPowerCondenser(GuiGraphics graphics, Minecraft minecraft,
                                                  HighPowerCondenserBlockEntity condenser) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_condenser_powered").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US, "%,dHE / %,dHE",
                condenser.getPower(), condenser.getMaxPower()), x, y, 0xFFFFFF, true);
        y += 10;
        String spent = Component.translatable("hbmfluid.spentsteam").getString();
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "-> %s: %,d/%,dmB", spent, condenser.spentSteamTank().getFluidAmount(),
                condenser.spentSteamTank().getCapacity()), x, y, 0x55FF55, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- Water: %,d/%,dmB", condenser.waterTank().getFluidAmount(),
                condenser.waterTank().getCapacity()), x, y, 0xFF5555, true);
    }

    private static void renderAirIntake(GuiGraphics graphics, Minecraft minecraft,
                                        AirIntakeBlockEntity intake) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_intake").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        int powerColor = intake.getPower() < AirIntakeBlockEntity.POWER_PER_TICK ? 0xFF5555 : 0x55FF55;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "Power: %,dHE", intake.getPower()), x, y, powerColor, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- %s: %,d/%,dmB", Component.translatable("hbmfluid.air").getString(),
                intake.airTank().getFluidAmount(), intake.airTank().getCapacity()),
                x, y, 0xFFFFFF, true);
    }


    private static void renderConverterHEtoFE(GuiGraphics graphics, Minecraft minecraft,
                                        ConverterHEtoFEBlockEntity converter) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2 + 2;

        long power = converter.getPower();
        int currentEnergy = converter.getEnergy().getEnergyStored();

        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "-> %,dHE", power), x, y, 0xFF5555, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- %,dFE", currentEnergy), x, y, 0x55FF55, true);
    }

    private static void renderConverterFEtoHE(GuiGraphics graphics, Minecraft minecraft,
                                              ConverterFEtoHEBlockEntity converter) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2 + 2;

        long power = converter.getPower();
        int currentEnergy = converter.getEnergy().getEnergyStored();

        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "-> %,dFE", currentEnergy), x, y, 0xFF5555, true);
        y += 10;
        graphics.drawString(minecraft.font, String.format(java.util.Locale.US,
                "<- %,dHE", power), x, y, 0x55FF55, true);
    }

    private static void renderBoiler(GuiGraphics graphics, Minecraft minecraft, HeatBoilerBlockEntity boiler) {
        if (boiler.hasExploded()) return;
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.machine_boiler").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font, boiler.heat() + "TU", x, y, 0xFFFFFF, true);
        y += 10;
        String inputName = Component.translatable(boiler.inputSelection().translationKey()).getString();
        String outputName = Component.translatable(boiler.outputSelection().translationKey()).getString();
        graphics.drawString(minecraft.font, "-> " + inputName + ": " + boiler.inputTank().getFluidAmount() + "/"
                + boiler.inputTank().getCapacity() + "mB", x, y, 0x55FF55, true);
        y += 10;
        graphics.drawString(minecraft.font, "<- " + outputName + ": " + boiler.outputTank().getFluidAmount() + "/"
                + boiler.outputTank().getCapacity() + "mB", x, y, 0xFF5555, true);
    }

    private static void renderElectricHeater(GuiGraphics graphics, Minecraft minecraft,
                                              ElectricHeaterBlockEntity heater) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.heater_electric").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font,
                String.format(java.util.Locale.US, "%,d", heater.heatEnergy()) + " TU",
                x, y, 0xFFFFFF, true);
        y += 10;
        Component consumption = Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(heater.getConsumption() + " HE/t").withStyle(ChatFormatting.WHITE));
        graphics.drawString(minecraft.font, consumption, x, y, 0xFFFFFF, true);
        y += 10;
        Component output = Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal(heater.getHeatGen() + " TU/t").withStyle(ChatFormatting.WHITE));
        graphics.drawString(minecraft.font, output, x, y, 0xFFFFFF, true);
    }

    private static void renderFluidBurner(GuiGraphics graphics, Minecraft minecraft,
                                          FluidBurnerBlockEntity burner) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.heater_oilburner").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        Component consumption = Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(burner.setting() + " mB/t").withStyle(ChatFormatting.WHITE));
        graphics.drawString(minecraft.font, consumption, x, y, 0xFFFFFF, true);
        if (FluidBurnerFuels.flammable(burner.selectedFluid())) {
            y += 10;
            Component output = Component.literal("<- ").withStyle(ChatFormatting.RED)
                    .append(Component.literal(String.format(java.util.Locale.US, "%,d",
                            FluidBurnerFuels.heatPerMb(burner.selectedFluid()) * burner.setting())
                            + " TU/t").withStyle(ChatFormatting.WHITE));
            graphics.drawString(minecraft.font, output, x, y, 0xFFFFFF, true);
        }
    }

    private static void renderHeatExchanger(GuiGraphics graphics, Minecraft minecraft,
                                             HeatExchangerBlockEntity exchanger) {
        int x = graphics.guiWidth() / 2 + 8;
        int y = graphics.guiHeight() / 2;
        String title = Component.translatable("block.hbm.heater_heatex").getString();
        graphics.drawString(minecraft.font, title, x + 1, y - 9, 0x404000, false);
        graphics.drawString(minecraft.font, title, x, y - 10, 0xFFFF00, false);
        graphics.drawString(minecraft.font,
                String.format(java.util.Locale.US, "%,d", exchanger.heatEnergy()) + " TU",
                x, y, 0xFFFFFF, true);
    }
}
