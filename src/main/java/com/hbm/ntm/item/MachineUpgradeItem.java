package com.hbm.ntm.item;

import net.minecraft.world.item.Item;

public final class MachineUpgradeItem extends Item {
    private final Type type;
    private final int level;

    public MachineUpgradeItem(Type type, int level) {
        super(new Properties().stacksTo(1));
        this.type = type;
        this.level = level;
    }

    public Type type() { return type; }
    public int level() { return level; }

    public enum Type { SPEED, POWER, EFFECT, AFTERBURN, OVERDRIVE, EJECTOR, STACK }
}
