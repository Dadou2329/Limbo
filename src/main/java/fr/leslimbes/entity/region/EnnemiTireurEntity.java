package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Ennemi régional à distance : il se prépare visiblement (état d'animation 1, même signal que le Veilleur),
 * puis lance un éclat vers sa cible. Dégâts, effet (ralentissement, faiblesse...) et recul viennent de l'espèce.
 * Le joueur a le temps de s'écarter de la trajectoire ou de se mettre à couvert.
 */
public class EnnemiTireurEntity extends HostileEntity implements RangedAttackMob, EntiteAnimee {
	private static final TrackedData<Integer> PREPARATION = DataTracker.registerData(EnnemiTireurEntity.class, TrackedDataHandlerRegistry.INTEGER);
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;
	@Nullable
	private LivingEntity cibleTir;

	public EnnemiTireurEntity(EntityType<? extends HostileEntity> type, World world, Espece espece) {
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
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, e.vie)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, e.vitesse)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, Math.max(1.0, e.degats))
				.add(EntityAttributes.GENERIC_ARMOR, e.armure)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, e.resistanceRecul)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, e.pas);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(PREPARATION, 0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(2, new ProjectileAttackGoal(this, 1.0, espece().intervalleTir, (float) espece().porteeTir));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 12.0F));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	public int getPreparation() {
		return this.dataTracker.get(PREPARATION);
	}

	@Override
	public int etatAnimation() {
		return getPreparation() > 0 ? 1 : 0;
	}

	/** Commence la préparation d'un tir vers {@code target} ; le tir part à la fin du décompte. */
	@Override
	public void shootAt(LivingEntity target, float pullProgress) {
		if (getPreparation() > 0 || this.getWorld().isClient) {
			return;
		}
		cibleTir = target;
		this.dataTracker.set(PREPARATION, Math.max(1, espece().preparation));
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_PREPARE, SoundCategory.HOSTILE, 0.7F, 1.5F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_prepare", this.getPos().add(0, this.getHeight() * 0.8, 0), this.getRotationVector(), espece().preparation);
		}
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (this.getWorld().isClient) {
			return;
		}
		int prep = getPreparation();
		if (prep > 0) {
			this.getNavigation().stop();
			if (cibleTir != null) {
				getLookControl().lookAt(cibleTir, 30.0F, 30.0F);
			}
			prep--;
			this.dataTracker.set(PREPARATION, prep);
			if (prep == 0) {
				tirer();
			}
		}
	}

	private void tirer() {
		LivingEntity cible = cibleTir != null && cibleTir.isAlive() ? cibleTir : getTarget();
		cibleTir = null;
		if (cible == null) {
			return;
		}
		if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld monde && !fr.leslimbes.LimbesConfig.peutLancerEclat(monde)) {
			return;
		}
		EclatProjectileEntity eclat = new EclatProjectileEntity(this.getWorld(), this, (float) espece().degatsProjectile, espece().effet, espece().dureeEffet, espece().reculProjectile);
		eclat.setPosition(this.getX(), this.getEyeY() - 0.2, this.getZ());
		double dx = cible.getX() - this.getX();
		double dy = cible.getBodyY(0.5) - eclat.getY();
		double dz = cible.getZ() - this.getZ();
		double h = Math.sqrt(dx * dx + dz * dz);
		eclat.setVelocity(dx, dy + h * 0.15, dz, 1.4F, 3.0F);
		this.getWorld().spawnEntity(eclat);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.TIREUR_TIR, SoundCategory.HOSTILE, 0.9F, 0.9F + this.random.nextFloat() * 0.2F);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return espece().ambiant;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return espece().blesse != null ? espece().blesse : LimbesSounds.VEILLEUR_BLESSE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return espece().mort != null ? espece().mort : LimbesSounds.VEILLEUR_MORT;
	}
}
