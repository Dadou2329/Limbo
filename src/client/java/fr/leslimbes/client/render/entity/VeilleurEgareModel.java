package fr.leslimbes.client.render.entity;

import fr.leslimbes.entity.VeilleurEgareEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;

/** Modèle du Veilleur égaré (texture 64x64, voir gen_textures.py : tex_veilleur_egare). Attaque annoncée : bras levés. */
public class VeilleurEgareModel extends SinglePartEntityModel<VeilleurEgareEntity> {
	private final ModelPart root;
	private final ModelPart torse;
	private final ModelPart tete;
	private final ModelPart brasG;
	private final ModelPart brasD;
	private final ModelPart jambeG;
	private final ModelPart jambeD;

	public VeilleurEgareModel(ModelPart root) {
		this.root = root;
		this.torse = root.getChild("torse");
		this.tete = torse.getChild("tete");
		this.brasG = torse.getChild("bras_g");
		this.brasD = torse.getChild("bras_d");
		this.jambeG = root.getChild("jambe_g");
		this.jambeD = root.getChild("jambe_d");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		ModelPartData torse = root.addChild("torse", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -10.0F, -3.0F, 10.0F, 10.0F, 6.0F), ModelTransform.pivot(0.0F, 16.0F, 0.0F));
		torse.addChild("tete", ModelPartBuilder.create().uv(32, 0).cuboid(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F), ModelTransform.pivot(0.0F, -10.0F, 0.0F));
		ModelPartData brasG = torse.addChild("bras_g", ModelPartBuilder.create().uv(16, 16).cuboid(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(7.0F, -8.0F, 0.0F));
		ModelPartData brasD = torse.addChild("bras_d", ModelPartBuilder.create().uv(0, 16).cuboid(-2.0F, -1.0F, -2.0F, 4.0F, 12.0F, 4.0F), ModelTransform.pivot(-7.0F, -8.0F, 0.0F));
		brasG.addChild("epaule_g", ModelPartBuilder.create().uv(24, 32).cuboid(-2.5F, -1.5F, -3.5F, 5.0F, 3.0F, 7.0F), ModelTransform.pivot(0.0F, -1.0F, 0.0F));
		brasD.addChild("epaule_d", ModelPartBuilder.create().uv(0, 32).cuboid(-2.5F, -1.5F, -3.5F, 5.0F, 3.0F, 7.0F), ModelTransform.pivot(0.0F, -1.0F, 0.0F));
		root.addChild("jambe_g", ModelPartBuilder.create().uv(32, 12).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F), ModelTransform.pivot(2.5F, 16.0F, 0.0F));
		root.addChild("jambe_d", ModelPartBuilder.create().uv(48, 12).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F), ModelTransform.pivot(-2.5F, 16.0F, 0.0F));
		return TexturedModelData.of(data, 64, 64);
	}

	@Override
	public void setAngles(VeilleurEgareEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		tete.yaw = headYaw * ((float) Math.PI / 180F);
		tete.pitch = headPitch * ((float) Math.PI / 180F);
		float marche = MathHelper.cos(limbAngle * 0.5F) * 0.8F * limbDistance;
		jambeG.pitch = marche;
		jambeD.pitch = -marche;
		int prep = entity.getPreparation();
		if (prep > 0) {
			// Préparation : les bras montent progressivement au-dessus de la tête (télégraphe lisible).
			float t = 1.0F - (float) prep / VeilleurEgareEntity.PREPARATION_TICKS;
			float leve = -(float) Math.PI * (0.55F + 0.45F * t);
			brasG.pitch = leve;
			brasD.pitch = leve;
			brasG.roll = -0.25F;
			brasD.roll = 0.25F;
			torse.pitch = -0.15F * t;
		} else {
			brasG.pitch = -marche * 0.6F;
			brasD.pitch = marche * 0.6F;
			brasG.roll = -0.08F;
			brasD.roll = 0.08F;
			torse.pitch = 0.08F + MathHelper.sin(animationProgress * 0.06F) * 0.03F;
		}
	}

	@Override
	public ModelPart getPart() {
		return root;
	}
}
