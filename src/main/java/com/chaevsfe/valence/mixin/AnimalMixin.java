package com.chaevsfe.valence.mixin;

import com.chaevsfe.valence.modules.trough.AnimalTrough;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Animal.class)
public class AnimalMixin
{
    @WrapOperation(
        method = "finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean valence$suppressBreedingXp (ServerLevel level, Entity orb, Operation<Boolean> original,
                                                ServerLevel methodLevel, Animal otherParent, AgeableMob child) {
        if (AnimalTrough.consumeSuppression((Animal) (Object) this, otherParent))
            return false;
        return original.call(level, orb);
    }
}
