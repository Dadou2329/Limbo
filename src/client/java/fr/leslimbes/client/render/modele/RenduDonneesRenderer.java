package fr.leslimbes.client.render.modele;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.EntiteAnimee;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Rendu générique d'une créature dont le modèle est décrit en données. Texture :
 * textures/entity/&lt;id&gt;.png (ou &lt;id&gt;_&lt;variante&gt;.png), couche émissive &lt;id&gt;_lueur.png.
 */
public class RenduDonneesRenderer<T extends MobEntity> extends MobEntityRenderer<T, ModeleDonneesEntityModel<T>> {
	private final String id;
	private final Identifier texture;
	private final Map<String, Identifier> variantes = new HashMap<>();
	private int generation;

	public RenduDonneesRenderer(EntityRendererFactory.Context ctx, String id) {
		super(ctx, new ModeleDonneesEntityModel<>(ModeleDonneesRegistry.get(id)), ModeleDonneesRegistry.get(id).ombre());
		this.id = id;
		this.texture = LesLimbes.id("textures/entity/" + id + ".png");
		this.generation = ModeleDonneesRegistry.generation();
		if (this.model.definition().lueur()) {
			this.addFeature(new LueurFeature<>(this, LesLimbes.id("textures/entity/" + id + "_lueur.png")));
		}
	}

	@Override
	public Identifier getTexture(T entity) {
		if (entity instanceof EntiteAnimee a && !a.varianteTexture().isEmpty()) {
			return variantes.computeIfAbsent(a.varianteTexture(), v -> LesLimbes.id("textures/entity/" + id + "_" + v + ".png"));
		}
		return texture;
	}

	@Override
	protected void scale(T entity, MatrixStack matrices, float amount) {
		float e = this.model.definition().echelle();
		if (e != 1.0F) {
			matrices.scale(e, e, e);
		}
	}

	@Override
	public void render(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		if (generation != ModeleDonneesRegistry.generation()) {
			this.model = new ModeleDonneesEntityModel<>(ModeleDonneesRegistry.get(id));
			this.generation = ModeleDonneesRegistry.generation();
		}
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}

	/** Couche émissive : seules les parties lumineuses de la texture _lueur sont opaques. */
	private static final class LueurFeature<T extends MobEntity> extends FeatureRenderer<T, ModeleDonneesEntityModel<T>> {
		private final Identifier texture;

		LueurFeature(FeatureRendererContext<T, ModeleDonneesEntityModel<T>> context, Identifier texture) {
			super(context);
			this.texture = texture;
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity,
						   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			if (entity.isInvisible()) {
				return;
			}
			VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEyes(texture));
			this.getContextModel().render(matrices, vc, 0x00F000F0, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
		}
	}
}
