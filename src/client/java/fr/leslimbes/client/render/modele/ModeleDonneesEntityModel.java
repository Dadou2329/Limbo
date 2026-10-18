package fr.leslimbes.client.render.modele;

import fr.leslimbes.entity.EntiteAnimee;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

/** Modèle d'entité construit à partir d'un {@link ModeleDonnees} ; les animations sont évaluées chaque image. */
public class ModeleDonneesEntityModel<T extends LivingEntity> extends SinglePartEntityModel<T> {
	private final ModeleDonnees definition;
	private final ModelPart racine;
	private final Map<String, ModelPart> parties = new HashMap<>();
	private float tickDelta;

	public ModeleDonneesEntityModel(ModeleDonnees definition) {
		this.definition = definition;
		this.racine = definition.creerModele();
		for (ModeleDonnees.Os o : definition.os()) {
			ModelPart parent = o.parent().isEmpty() ? racine : parties.get(o.parent());
			if (parent != null && parent.hasChild(o.nom())) {
				parties.put(o.nom(), parent.getChild(o.nom()));
			}
		}
	}

	public ModeleDonnees definition() {
		return definition;
	}

	@Override
	public void animateModel(T entity, float limbAngle, float limbDistance, float tickDelta) {
		this.tickDelta = tickDelta;
	}

	@Override
	public void setAngles(T entity, float limbAngle, float limbDistance, float age, float headYaw, float headPitch) {
		for (ModelPart p : parties.values()) {
			p.resetTransform();
		}
		boolean auSol = entity.isOnGround();
		float vitesseY = (float) entity.getVelocity().y;
		int etat = entity instanceof EntiteAnimee a ? a.etatAnimation() : 0;
		float attaque = entity.getHandSwingProgress(tickDelta);
		float vie = entity.getMaxHealth() > 0 ? 1.0F - entity.getHealth() / entity.getMaxHealth() : 0.0F;
		for (ModeleDonnees.Animation a : definition.animations()) {
			ModelPart p = parties.get(a.os());
			if (p == null) {
				continue;
			}
			float t = switch (a.pilote()) {
				case "marche" -> MathHelper.cos(limbAngle * a.frequence() + a.phase()) * limbDistance;
				case "marche_abs" -> Math.abs(MathHelper.cos(limbAngle * a.frequence() + a.phase())) * limbDistance;
				case "temps" -> MathHelper.sin(age * a.frequence() + a.phase());
				case "temps_cos" -> MathHelper.cos(age * a.frequence() + a.phase());
				case "vol" -> MathHelper.sin(age * a.frequence() + a.phase()) * (auSol ? 0.15F : 1.0F);
				case "regard_yaw" -> headYaw * ((float) Math.PI / 180.0F);
				case "regard_pitch" -> headPitch * ((float) Math.PI / 180.0F);
				case "attaque" -> MathHelper.sin(MathHelper.sqrt(attaque) * (float) Math.PI);
				case "etat" -> etat == a.valeur() ? 1.0F : 0.0F;
				case "etat_temps" -> etat == a.valeur() ? MathHelper.sin(age * a.frequence() + a.phase()) : 0.0F;
				case "vitesse" -> limbDistance;
				case "vitesse_verticale" -> MathHelper.clamp(vitesseY * 2.0F, -1.0F, 1.0F);
				case "vie" -> vie;
				case "monte" -> entity.hasPassengers() ? 1.0F : 0.0F;
				case "bebe" -> entity.isBaby() ? 1.0F : 0.0F;
				case "constante" -> 1.0F;
				default -> 0.0F;
			};
			float v = a.base() + a.amplitude() * t;
			switch (a.propriete()) {
				case "rx" -> p.pitch += v;
				case "ry" -> p.yaw += v;
				case "rz" -> p.roll += v;
				case "x" -> p.pivotX += v;
				case "y" -> p.pivotY += v;
				case "z" -> p.pivotZ += v;
				case "echelle" -> {
					p.xScale += v;
					p.yScale += v;
					p.zScale += v;
				}
				default -> {
				}
			}
		}
	}

	@Override
	public ModelPart getPart() {
		return racine;
	}
}
