package com.chaevsfe.valence.core.data;

import com.chaevsfe.valence.core.ModConstants;
import com.chaevsfe.valence.core.module.Modules;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.RegistryOps;

public record ModuleEnabledCondition (String module) implements ResourceCondition
{
    public static final MapCodec<ModuleEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("module").forGetter(ModuleEnabledCondition::module)
    ).apply(instance, ModuleEnabledCondition::new));

    public static final ResourceConditionType<ModuleEnabledCondition> TYPE =
        ResourceConditionType.create(ModConstants.loc("module_enabled"), CODEC);

    @Override
    public ResourceConditionType<?> getType () {
        return TYPE;
    }

    @Override
    public boolean test (RegistryOps.RegistryInfoLookup registryInfo) {
        return Modules.isEnabled(module);
    }
}
