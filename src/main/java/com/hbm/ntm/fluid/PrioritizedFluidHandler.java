package com.hbm.ntm.fluid;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public interface PrioritizedFluidHandler extends IFluidHandler {
    int fluidPriority();
}
