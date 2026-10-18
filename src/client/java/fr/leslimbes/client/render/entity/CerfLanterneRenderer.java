package fr.leslimbes.client.render.entity;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.CerfLanterneEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class CerfLanterneRenderer extends MobEntityRenderer<CerfLanterneEntity, CerfLanterneModel> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(LesLimbes.id("cerf_lanterne"), "main");
	private static final Identifier TEXTURE = LesLimbes.id("textures/entity/cerf_lanterne.png");

	public CerfLanterneRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new CerfLanterneModel(ctx.getPart(LAYER)), 0.6F);
		this.addFeature(new LueurFeature(this));
	}

	@Override
	public Identifier getTexture(CerfLanterneEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(CerfLanterneEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(1.15F, 1.15F, 1.15F);
	}

	/** Le soir, les lanternes des bois brillent : second rendu émissif du modèle (texture identique, seules les lanternes sont claires). */
	private static final class LueurFeature extends FeatureRenderer<CerfLanterneEntity, CerfLanterneModel> {
		LueurFeature(FeatureRendererContext<CerfLanterneEntity, CerfLanterneModel> context) {
			super(context);
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CerfLanterneEntity entity,
						   float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
			if (!entity.estAllume()) {
				return;
			}
			VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEyes(TEXTURE));
			this.getContextModel().render(matrices, vc, 0x00F000F0, net.minecraft.client.render.OverlayTexture.DEFAULT_UV, 0x60FFFFFF);
		}
	}
}
