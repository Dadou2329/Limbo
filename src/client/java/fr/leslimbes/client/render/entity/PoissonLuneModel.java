package fr.leslimbes.client.render.entity;

import fr.leslimbes.entity.PoissonLuneEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/** Modèle du poisson-lune (texture 32x32, voir gen_textures.py : tex_poisson_lune). */
public class PoissonLuneModel extends SinglePartEntityModel<PoissonLuneEntity> {
	private final ModelPart root;
	private final ModelPart corps;
	private final ModelPart queue;
	private final ModelPart nageoireGauche;
	private final ModelPart nageoireDroite;

	public PoissonLuneModel(ModelPart root) {
		this.root = root;
		this.corps = root.getChild("corps");
		this.queue = corps.getChild("queue");
		this.nageoireGauche = corps.getChild("nageoire_gauche");
		this.nageoireDroite = corps.getChild("nageoire_droite");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		ModelPartData corps = root.addChild("corps", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0F, -2.0F, -5.0F, 6.0F, 4.0F, 10.0F), ModelTransform.pivot(0.0F, 21.0F, 0.0F));
		corps.addChild("dorsale", ModelPartBuilder.create().uv(0, 14).cuboid(-0.5F, -3.0F, -3.0F, 1.0F, 3.0F, 6.0F), ModelTransform.pivot(0.0F, -2.0F, 0.0F));
		corps.addChild("queue", ModelPartBuilder.create().uv(16, 14).cuboid(-0.5F, -2.0F, 0.0F, 1.0F, 4.0F, 4.0F), ModelTransform.pivot(0.0F, 0.0F, 5.0F));
		corps.addChild("nageoire_gauche", ModelPartBuilder.create().uv(16, 24).cuboid(0.0F, 0.0F, 0.0F, 3.0F, 1.0F, 3.0F), ModelTransform.pivot(3.0F, 0.5F, -2.0F));
		corps.addChild("nageoire_droite", ModelPartBuilder.create().uv(16, 24).mirrored().cuboid(-3.0F, 0.0F, 0.0F, 3.0F, 1.0F, 3.0F), ModelTransform.pivot(-3.0F, 0.5F, -2.0F));
		return TexturedModelData.of(data, 32, 32);
	}

	@Override
	public void setAngles(PoissonLuneEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		float vitesse = 0.25F + Math.min(1.0F, limbDistance) * 0.6F;
		queue.yaw = MathHelper.sin(animationProgress * vitesse) * 0.45F;
		corps.roll = MathHelper.cos(animationProgress * 0.1F) * 0.08F;
		corps.pivotY = 21.0F + MathHelper.sin(animationProgress * 0.09F) * 0.8F;
		corps.pitch = headPitch * ((float) Math.PI / 180F) * 0.5F;
		float battement = MathHelper.sin(animationProgress * 0.35F) * 0.5F;
		nageoireGauche.roll = -0.4F + battement;
		nageoireDroite.roll = 0.4F - battement;
	}

	@Override
	public ModelPart getPart() {
		return root;
	}
}
