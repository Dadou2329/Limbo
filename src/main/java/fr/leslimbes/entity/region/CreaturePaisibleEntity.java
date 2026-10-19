package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Animal terrestre tranquille des régions (crabe, fennec, bouquetin, géant des jardins...). Le comportement
 * commun vient de l'espèce : fuite, appât, pas franchissable, chute sans dommage, sons. Le géant ne fuit pas,
 * ne recule pas et fait trembler le sol à chaque pas.
 */
public class CreaturePaisibleEntity extends AnimalEntity implements EntiteAnimee {
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;

	public CreaturePaisibleEntity(EntityType<? extends AnimalEntity> type, World world, Espece espece) {
		super(type, world);
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
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, e.vitesse)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, e.geant ? 1.0 : e.resistanceRecul)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, e.pas)
				.add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, e.chuteSure);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		if (espece().fuit) {
			this.goalSelector.add(1, new EscapeDangerGoal(this, 1.5));
		}
		if (espece().appat != null) {
			this.goalSelector.add(2, new TemptGoal(this, 1.1, Ingredient.ofItems(espece().appat), false));
		}
		this.goalSelector.add(4, new WanderAroundFarGoal(this, espece().geant ? 0.5 : 0.9, 0.01F));
		this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, espece().geant ? 16.0F : 8.0F));
		this.goalSelector.add(6, new LookAroundGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (espece().geant && this.getWorld() instanceof ServerWorld world && this.age % 16 == 0 && this.isOnGround()
				&& this.getVelocity().horizontalLengthSquared() > 0.002) {
			world.playSound(null, this.getBlockPos(), LimbesSounds.GEANT_PAS, SoundCategory.NEUTRAL, 0.9F, 0.8F + this.random.nextFloat() * 0.2F);
		}
	}

	/** Le lézard-cadran se dresse vers le soleil au milieu du jour (état 1 pour le modèle). */
	@Override
	public int etatAnimation() {
		if (espece().seChauffe) {
			long t = this.getWorld().getTimeOfDay() % 24000L;
			return t >= 4500L && t <= 7500L ? 1 : 0;
		}
		return 0;
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return false;
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return null;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return !espece().geant;
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
	public int getMinAmbientSoundDelay() {
		return espece().geant ? 600 : 320;
	}

	@Override
	protected float getSoundVolume() {
		return espece().geant ? 1.2F : 0.8F;
	}
}
