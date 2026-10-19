package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.registry.LimbesEspeces;
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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Ennemi régional au corps à corps dont chaque coup est annoncé (comme le Veilleur égaré) : il se fige et
 * prépare pendant {@code espece().preparation} ticks (état d'animation 1), frappe en arc, puis récupère
 * (état 2). La durée, la portée, le recul et l'effet appliqué (poison du scorpion...) viennent de l'espèce.
 */
public class EnnemiAnnonceEntity extends HostileEntity implements EntiteAnimee {
	private static final TrackedData<Integer> PREPARATION = DataTracker.registerData(EnnemiAnnonceEntity.class, TrackedDataHandlerRegistry.INTEGER);
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;
	private int recuperation;
	/** Diagnostic (tests) : coups portés et cibles touchées au dernier coup. */
	public int coupsPortes;
	public int dernieresTouches;

	public EnnemiAnnonceEntity(EntityType<? extends HostileEntity> type, World world, Espece espece) {
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
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, e.degats)
				.add(EntityAttributes.GENERIC_ARMOR, e.armure)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, e.resistanceRecul)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, e.pas);
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

	public int getPreparation() {
		return this.dataTracker.get(PREPARATION);
	}

	public boolean enPreparation() {
		return getPreparation() > 0;
	}

	@Override
	public int etatAnimation() {
		if (enPreparation()) {
			return 1;
		}
		return recuperation > 0 ? 2 : 0;
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

	/** Lance une attaque annoncée contre {@code cible} (utilisé par l'IA et les tests). */
	public void preparer(LivingEntity cible) {
		setTarget(cible);
		commencerPreparation();
	}

	private void commencerPreparation() {
		this.dataTracker.set(PREPARATION, espece().preparation);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_PREPARE, SoundCategory.HOSTILE, 1.0F, espece().vie >= 50 ? 0.7F : 1.1F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_prepare", this.getPos().add(0, this.getHeight() * 0.7, 0), this.getRotationVector(), espece().preparation);
		}
	}

	private void abattre() {
		recuperation = espece().recuperation;
		coupsPortes++;
		double portee = espece().portee;
		Vec3d avant = this.getPos().add(this.getRotationVector().multiply(portee * 0.5, 0, portee * 0.5));
		Box zone = new Box(avant.x - portee, this.getY() - 1, avant.z - portee, avant.x + portee, this.getY() + 2.5, avant.z + portee);
		List<LivingEntity> touches = this.getWorld().getEntitiesByClass(LivingEntity.class, zone,
				e -> e != this && e.isAlive() && !(e instanceof EnnemiAnnonceEntity) && !(e instanceof EnnemiTireurEntity)
						&& this.squaredDistanceTo(e) <= (portee + 1.0) * (portee + 1.0));
		dernieresTouches = touches.size();
		for (LivingEntity e : touches) {
			if (this.tryAttack(e) && espece().effet != null && espece().dureeEffet > 0) {
				e.addStatusEffect(new StatusEffectInstance(espece().effet, espece().dureeEffet), this);
			}
			Vec3d dir = e.getPos().subtract(this.getPos()).normalize();
			e.takeKnockback(espece().forceRecul, -dir.x, -dir.z);
		}
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_FRAPPE, SoundCategory.HOSTILE, 1.0F, espece().vie >= 50 ? 0.8F : 1.0F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_frappe", avant.add(0, 0.1, 0), this.getRotationVector(), touches.size());
		}
	}

	/** Un légionnaire astral ne peut être blessé tant qu'une bannière se dresse à moins de dix blocs. */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (espece().protegeParBanniere && this.getWorld() instanceof ServerWorld world && !source.isIn(net.minecraft.registry.tag.DamageTypeTags.BYPASSES_INVULNERABILITY)
				&& SoutienEntity.protegee(world, this.getPos())) {
			if (this.age % 5 == 0) {
				world.playSound(null, this.getBlockPos(), LimbesSounds.POUVOIR_REFUS, SoundCategory.HOSTILE, 0.6F, 1.4F);
			}
			return false;
		}
		return super.damage(source, amount);
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

	/** Attaque au corps à corps avec préparation visible. */
	private final class FrappeAnnonceeGoal extends MeleeAttackGoal {
		FrappeAnnonceeGoal() {
			super(EnnemiAnnonceEntity.this, 1.0, true);
		}

		@Override
		protected void attack(LivingEntity target) {
			if (enPreparation() || recuperation > 0) {
				return;
			}
			double d = espece().portee - 0.4;
			if (EnnemiAnnonceEntity.this.squaredDistanceTo(target) <= d * d && this.isCooledDown()) {
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
