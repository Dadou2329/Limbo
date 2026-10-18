package fr.leslimbes.mixin;

import com.mojang.serialization.Lifecycle;
import fr.leslimbes.LesLimbes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft considère comme « expérimentale » toute dimension autre que les trois dimensions vanilla,
 * ce qui affiche un avertissement bloquant à chaque ouverture d'un monde en solo. Les Limbes sont
 * fournies par le mod lui-même et validées par ses tests : la dimension est déclarée stable.
 */
@Mixin(DimensionOptionsRegistryHolder.class)
public abstract class DimensionOptionsRegistryHolderMixin {
	@Inject(
		method = "getLifecycle(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionOptions;)Lcom/mojang/serialization/Lifecycle;",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void lesLimbes$dimensionStable(RegistryKey<DimensionOptions> cle, DimensionOptions options, CallbackInfoReturnable<Lifecycle> cir) {
		if (cle.getValue().getNamespace().equals(LesLimbes.MOD_ID)) {
			cir.setReturnValue(Lifecycle.stable());
		}
	}
}
