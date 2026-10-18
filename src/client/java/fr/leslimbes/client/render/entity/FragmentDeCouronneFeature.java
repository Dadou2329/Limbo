package fr.leslimbes.client.render.entity;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.client.ClientLimbesData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

/**
 * Fragment de couronne flottant au-dessus du joueur qui a éveillé sa magie royale.
 * (Seul le joueur local connaît sa progression ; les autres joueurs seront couverts par une
 * synchronisation d'état public dans une tranche ultérieure.)
 */
public class FragmentDeCouronneFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	private static final Identifier TEXTURE = LesLimbes.id("textures/entity/fragment_de_couronne.png");

	public FragmentDeCouronneFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity,
					   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || entity != client.player || ClientLimbesData.get().pouvoirs.isEmpty() || entity.isInvisible()) {
			return;
		}
		matrices.push();
		float flottement = (float) Math.sin(animationProgress * 0.08F) * 0.03F;
		float hauteur = entity.isInSneakingPose() ? -0.55F : -0.75F;
		matrices.translate(0.0F, hauteur + flottement, 0.0F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((animationProgress * 1.5F) % 360.0F));
		Matrix4f m = matrices.peek().getPositionMatrix();
		VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(TEXTURE));
		float s = 0.22F;
		int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		// Deux quads croisés pour que le fragment soit visible sous tous les angles.
		quad(vc, m, matrices, -s, s, 0.0F, 0.0F, l);
		quad(vc, m, matrices, 0.0F, 0.0F, -s, s, l);
		matrices.pop();
	}

	private static void quad(VertexConsumer vc, Matrix4f m, MatrixStack matrices, float x0, float x1, float z0, float z1, int light) {
		MatrixStack.Entry e = matrices.peek();
		vc.vertex(m, x0, 0.0F, z0).color(255, 255, 255, 230).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, 0.0F, z1).color(255, 255, 255, 230).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, 0.35F, z1).color(255, 255, 255, 230).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, 0.35F, z0).color(255, 255, 255, 230).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, 0.35F, z0).color(255, 255, 255, 230).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, 0.35F, z1).color(255, 255, 255, 230).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, 0.0F, z1).color(255, 255, 255, 230).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, 0.0F, z0).color(255, 255, 255, 230).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
	}
}
