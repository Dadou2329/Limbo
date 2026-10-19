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
 * Signes royaux au-dessus du joueur, lus depuis l'état public synchronisé (tous les joueurs les voient) :
 * niveau 1, le fragment de couronne flottant ; niveau 2, la couronne assemblée (anneau de pointes) ;
 * niveau 3, l'Avènement : couronne, halo tournant et pans de manteau de lumière.
 */
public class FragmentDeCouronneFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
	private static final Identifier TEXTURE = LesLimbes.id("textures/entity/fragment_de_couronne.png");
	private static final Identifier COURONNE = LesLimbes.id("textures/entity/couronne.png");
	private static final Identifier MANTEAU = LesLimbes.id("textures/entity/manteau_royal.png");

	public FragmentDeCouronneFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity,
					   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		int niveau = fr.leslimbes.registry.LimbesAttachments.niveauCouronne(entity);
		MinecraftClient client = MinecraftClient.getInstance();
		if (niveau <= 0 && client.player == entity && !ClientLimbesData.get().pouvoirs.isEmpty()) {
			niveau = 1;
		}
		if (niveau <= 0 || entity.isInvisible()) {
			return;
		}
		int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		float flottement = (float) Math.sin(animationProgress * 0.08F) * 0.03F;
		float hauteur = entity.isInSneakingPose() ? -0.55F : -0.75F;
		if (niveau == 1) {
			matrices.push();
			matrices.translate(0.0F, hauteur + flottement, 0.0F);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((animationProgress * 1.5F) % 360.0F));
			Matrix4f m = matrices.peek().getPositionMatrix();
			VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(TEXTURE));
			float s = 0.22F;
			quad(vc, m, matrices, -s, s, 0.0F, 0.0F, 0.0F, 0.35F, l, 230);
			quad(vc, m, matrices, 0.0F, 0.0F, -s, s, 0.0F, 0.35F, l, 230);
			matrices.pop();
			return;
		}
		// Couronne assemblée : huit pans formant un anneau autour du sommet de la tête, qui tourne lentement.
		matrices.push();
		matrices.translate(0.0F, hauteur + 0.08F + flottement * 0.5F, 0.0F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((animationProgress * 0.6F) % 360.0F));
		VertexConsumer couronne = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(COURONNE));
		anneau(couronne, matrices, 0.3F, 0.0F, 0.28F, 8, l, 240);
		matrices.pop();
		if (niveau >= 3) {
			// Avènement : halo d'étoiles au-dessus et manteau de lumière qui tombe des épaules.
			matrices.push();
			matrices.translate(0.0F, hauteur - 0.35F, 0.0F);
			matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((-animationProgress * 2.0F) % 360.0F));
			VertexConsumer halo = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(MANTEAU));
			anneau(halo, matrices, 0.55F, 0.0F, 0.12F, 12, l, 180);
			matrices.pop();
			matrices.push();
			float ondulation = (float) Math.sin(animationProgress * 0.12F) * 0.06F;
			matrices.translate(0.0F, entity.isInSneakingPose() ? 0.25F : 0.0F, 0.12F + ondulation);
			VertexConsumer manteau = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(MANTEAU));
			Matrix4f m = matrices.peek().getPositionMatrix();
			// Deux pans dans le dos, légèrement ouverts, du haut des épaules jusqu'aux genoux.
			quad(manteau, m, matrices, -0.42F, 0.0F, 0.0F, 0.08F, 0.05F, 1.25F, l, 150);
			quad(manteau, m, matrices, 0.0F, 0.42F, 0.08F, 0.0F, 0.05F, 1.25F, l, 150);
			matrices.pop();
		}
	}

	/** Anneau de {@code n} pans verticaux de hauteur {@code h} au rayon {@code r}. */
	private static void anneau(VertexConsumer vc, MatrixStack matrices, float r, float y, float h, int n, int light, int alpha) {
		Matrix4f m = matrices.peek().getPositionMatrix();
		for (int i = 0; i < n; i++) {
			double a0 = i * Math.PI * 2 / n;
			double a1 = (i + 1) * Math.PI * 2 / n;
			quad(vc, m, matrices, (float) (Math.cos(a0) * r), (float) (Math.cos(a1) * r), (float) (Math.sin(a0) * r), (float) (Math.sin(a1) * r), y, y + h, light, alpha);
		}
	}

	private static void quad(VertexConsumer vc, Matrix4f m, MatrixStack matrices, float x0, float x1, float z0, float z1, float y0, float y1, int light, int alpha) {
		MatrixStack.Entry e = matrices.peek();
		vc.vertex(m, x0, y0, z0).color(255, 255, 255, alpha).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, y0, z1).color(255, 255, 255, alpha).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, y1, z1).color(255, 255, 255, alpha).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, y1, z0).color(255, 255, 255, alpha).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, y1, z0).color(255, 255, 255, alpha).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, y1, z1).color(255, 255, 255, alpha).texture(1, 0).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x1, y0, z1).color(255, 255, 255, alpha).texture(1, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
		vc.vertex(m, x0, y0, z0).color(255, 255, 255, alpha).texture(0, 1).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(e, 0, 1, 0);
	}
}
