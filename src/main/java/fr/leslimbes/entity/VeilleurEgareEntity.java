package fr.leslimbes.entity;

import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/**
 * Veilleur égaré : ancien gardien construit, désorienté depuis l'extinction des routes royales.
 * Ses coups sont lents et annoncés : il lève les bras pendant {@value #PREPARATION_TICKS} ticks avant
 * d'abattre un coup de masse en arc. Le joueur apprend à reculer ou à placer un Pas court.
 */
public class VeilleurEgareEntity extends HostileEntity {
	public static final int PREPARATION_TICKS = 18;
	public static final int RECUPERATION_TICKS = 24;
	private static final double PORTEE_FRAPPE = 3.2;
	private static final TrackedData<Integer> PREPARATION = DataTracker.registerData(VeilleurEgareEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private int recuperation;

	public VeilleurEgareEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 12;
	}

	public static DefaultAttributeContainer.Builder creerAttributs() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 44.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.24)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
				.add(EntityAttributes.GENERIC_ARMOR, 4.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(PREPARATION, 0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(2, new FrappeAnnonceeGoal());
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	/** Ticks de préparation restants (0 = pas en train de préparer). Visible côté client pour l'animation. */
	public int getPreparation() {
		return this.dataTracker.get(PREPARATION);
	}

	public boolean enPreparation() {
		return getPreparation() > 0;
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
			LivingEntity cible = getTarget();
			if (cible != null) {
				getLookControl().lookAt(cible, 30.0F, 30.0F);
			}
			prep--;
			this.dataTracker.set(PREPARATION, prep);
			if (prep == 0) {
				abattre();
			}
		} else if (recuperation > 0) {
			recuperation--;
		}
	}

	private void commencerPreparation() {
		this.dataTracker.set(PREPARATION, PREPARATION_TICKS);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_PREPARE, SoundCategory.HOSTILE, 1.0F, 1.0F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_prepare", this.getPos().add(0, 1.5, 0), this.getRotationVector(), PREPARATION_TICKS);
		}
	}

	private void abattre() {
		recuperation = RECUPERATION_TICKS;
		Vec3d avant = this.getPos().add(this.getRotationVector().multiply(1.6, 0, 1.6));
		Box zone = new Box(avant.x - PORTEE_FRAPPE, this.getY() - 1, avant.z - PORTEE_FRAPPE, avant.x + PORTEE_FRAPPE, this.getY() + 2.5, avant.z + PORTEE_FRAPPE);
		List<LivingEntity> touches = this.getWorld().getEntitiesByClass(LivingEntity.class, zone,
				e -> e != this && e.isAlive() && !(e instanceof VeilleurEgareEntity) && this.squaredDistanceTo(e) <= (PORTEE_FRAPPE + 1.0) * (PORTEE_FRAPPE + 1.0));
		for (LivingEntity e : touches) {
			this.tryAttack(e);
			Vec3d dir = e.getPos().subtract(this.getPos()).normalize();
			e.takeKnockback(0.9, -dir.x, -dir.z);
		}
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_FRAPPE, SoundCategory.HOSTILE, 1.0F, 0.9F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_frappe", avant.add(0, 0.1, 0), this.getRotationVector(), touches.size());
		}
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return LimbesSounds.VEILLEUR_BLESSE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return LimbesSounds.VEILLEUR_MORT;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	/** Attaque au corps à corps avec préparation visible. */
	private final class FrappeAnnonceeGoal extends MeleeAttackGoal {
		FrappeAnnonceeGoal() {
			super(VeilleurEgareEntity.this, 1.0, true);
		}

		@Override
		protected void attack(LivingEntity target) {
			if (enPreparation() || recuperation > 0) {
				return;
			}
			if (VeilleurEgareEntity.this.squaredDistanceTo(target) <= (PORTEE_FRAPPE - 0.4) * (PORTEE_FRAPPE - 0.4) && this.isCooledDown()) {
				this.resetCooldown();
				commencerPreparation();
			}
		}

		@Override
		protected int getMaxCooldown() {
			return 30;
		}
	}
}
