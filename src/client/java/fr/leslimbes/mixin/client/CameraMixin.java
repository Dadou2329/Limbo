package fr.leslimbes.mixin.client;

import fr.leslimbes.client.fx.Secousses;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Secousses de caméra (option désactivable) : léger décalage d'angle après le calcul vanilla de la caméra. */
@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	protected abstract void setRotation(float yaw, float pitch);

	@Shadow
	public abstract float getYaw();

	@Shadow
	public abstract float getPitch();

	@Inject(method = "update", at = @At("TAIL"))
	private void lesLimbes$secousse(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
		float[] d = Secousses.decalage();
		if (d != null) {
			setRotation(getYaw() + d[0], getPitch() + d[1]);
		}
	}
}
