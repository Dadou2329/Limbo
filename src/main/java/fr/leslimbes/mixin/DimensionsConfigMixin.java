package fr.leslimbes.mixin;

import com.mojang.serialization.Lifecycle;
import fr.leslimbes.LesLimbes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Complément de {@link DimensionOptionsRegistryHolderMixin} : le registre des dimensions reçoit un
 * cycle de vie « expérimental » dès qu'il compte plus de trois dimensions, indépendamment du cycle de
 * vie de chaque entrée. Quand les seules dimensions non vanilla sont celles du mod, le cycle de vie
 * est recalculé à partir des entrées elles-mêmes (stable pour les Limbes, inchangé pour les autres).
 */
@Mixin(DimensionOptionsRegistryHolder.DimensionsConfig.class)
public abstract class DimensionsConfigMixin {
	@Shadow
	public abstract Registry<DimensionOptions> dimensions();

	@Inject(method = "getLifecycle", at = @At("HEAD"), cancellable = true)
	private void lesLimbes$cycleDeVieDesEntrees(CallbackInfoReturnable<Lifecycle> cir) {
		Registry<DimensionOptions> registre = this.dimensions();
		Lifecycle resultat = Lifecycle.stable();
		for (RegistryKey<DimensionOptions> cle : registre.getKeys()) {
			boolean vanilla = cle == DimensionOptions.OVERWORLD || cle == DimensionOptions.NETHER || cle == DimensionOptions.END;
			if (!vanilla && !cle.getValue().getNamespace().equals(LesLimbes.MOD_ID)) {
				return;
			}
			resultat = resultat.add(registre.getEntryInfo(cle).map(RegistryEntryInfo::lifecycle).orElse(Lifecycle.stable()));
		}
		cir.setReturnValue(resultat);
	}
}
