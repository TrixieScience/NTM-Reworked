package com.hbm.ntm.mixin;

import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Keeps compatibility mixins out of class loading when their target mod is absent. */
public final class HbmMixinPlugin implements IMixinConfigPlugin {
    private boolean sableLoaded;
    private boolean jeiLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        LoadingModList loadingMods = LoadingModList.get();
        sableLoaded = loadingMods != null && loadingMods.getModFileById("sable") != null;
        jeiLoaded = loadingMods != null && loadingMods.getModFileById("jei") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.hbm.ntm.mixin.compat.sable.")) return sableLoaded;
        if (mixinClassName.startsWith("com.hbm.ntm.mixin.compat.jei.")) return jeiLoaded;
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) { }
}
