package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.registry.LimbesEspeces;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.SwimAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Créature d'eau (poisson-lanterne des bancs de la Mer suspendue) : nage en banc, fuit le joueur, se débat hors de l'eau. */
public class CreatureAquatiqueEntity extends WaterCreatureEntity implements EntiteAnimee {
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;

	public CreatureAquatiqueEntity(EntityType<? extends WaterCreatureEntity> type, World world, Espece espece) {
		super(type, world);
				this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02F, 0.1F, true);
		this.experiencePoints = espece().xp;
	}

	public Espece espece() {
		if (espece == null) {
			espece = LimbesEspeces.especeDe(this.getType());
		}
		return espece;
	}

	public static DefaultAttributeContainer.Builder creerAttributs(Espece e) {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, e.vie)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, e.vitesse);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new EscapeDangerGoal(this, 1.25));
		this.goalSelector.add(2, new FleeEntityGoal<>(this, PlayerEntity.class, 8.0F, 1.6, 1.4, EntityPredicates.EXCEPT_SPECTATOR::test));
		this.goalSelector.add(4, new SwimAroundGoal(this, 1.0, 10));
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		return new SwimNavigation(this, world);
	}

	@Override
	public void tickMovement() {
		if (!this.isTouchingWater() && this.isOnGround() && this.verticalCollision) {
			this.setVelocity(this.getVelocity().add((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F, 0.4F, (this.random.nextFloat() * 2.0F - 1.0F) * 0.05F));
			this.setOnGround(false);
			this.velocityDirty = true;
		}
		super.tickMovement();
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return espece().ambiant;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return espece().blesse != null ? espece().blesse : super.getHurtSound(source);
	}

	@Override
	protected SoundEvent getDeathSound() {
		return espece().mort != null ? espece().mort : super.getDeathSound();
	}

	@Override
	protected float getSoundVolume() {
		return 0.4F;
	}
}
