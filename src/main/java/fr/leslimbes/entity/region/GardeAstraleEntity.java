package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.entity.duel.PretendantEntity;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesParticles;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Garde astrale invoquée par le souverain : chevalier (corps à corps), archer (éclats de lumière) ou protecteur
 * (robuste, attire les coups et protège les alliés proches). Elle suit son souverain, tient une position ou attaque
 * selon l'ordre donné, ne blesse jamais un allié et se dissipe au bout de sa durée ou si le souverain disparaît.
 */
public class GardeAstraleEntity extends PathAwareEntity implements EntiteAnimee, RangedAttackMob {
	public static final int ORDRE_SUIVRE = 0;
	public static final int ORDRE_TENIR = 1;
	public static final int ORDRE_ATTAQUER = 2;
	private static final TrackedData<Optional<UUID>> PROPRIETAIRE = DataTracker.registerData(GardeAstraleEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	private static final TrackedData<Integer> ORDRE = DataTracker.registerData(GardeAstraleEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private Espece espece;
	private int ticksRestants = 1200;
	private int sansSouverain;
	@Nullable
	private BlockPos poste;

	public GardeAstraleEntity(EntityType<? extends PathAwareEntity> type, World world, Espece espece) {
		super(type, world);
		this.setPersistent();
		this.experiencePoints = 0;
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
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, e.degats)
				.add(EntityAttributes.GENERIC_ARMOR, e.armure)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, e.resistanceRecul)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	public static String nomOrdre(int ordre) {
		return switch (ordre) {
			case ORDRE_TENIR -> "tenir";
			case ORDRE_ATTAQUER -> "attaquer";
			default -> "suivre";
		};
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(PROPRIETAIRE, Optional.empty());
		builder.add(ORDRE, ORDRE_SUIVRE);
	}

	@Override
	protected void initGoals() {
		Espece e = espece();
		if (e.garde == 1) {
			this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 40, 16.0F));
		} else {
			this.goalSelector.add(1, new MeleeAttackGoal(this, 1.2, true));
		}
		this.goalSelector.add(2, new TenirGoal());
		this.goalSelector.add(3, new SuivreGoal());
		this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(6, new LookAroundGoal(this));
		this.targetSelector.add(1, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, this::estEnnemi));
	}

	/** Un ennemi : hostile, prétendant en duel, soutien de duel, ou toute créature qui vise un allié du souverain. */
	private boolean estEnnemi(LivingEntity e) {
		if (e == this || e instanceof GardeAstraleEntity || e instanceof PlayerEntity || e instanceof fr.leslimbes.npc.HabitantEntity) {
			return false;
		}
		if (e instanceof TameableEntity t && t.isTamed()) {
			return false;
		}
		if (e instanceof HostileEntity || e instanceof PretendantEntity || e instanceof SoutienEntity) {
			return true;
		}
		return e instanceof MobEntity m && m.getTarget() != null && (m.getTarget() instanceof PlayerEntity || m.getTarget() instanceof GardeAstraleEntity
				|| m.getTarget() instanceof fr.leslimbes.npc.HabitantEntity);
	}

	// --- Lien avec le souverain --------------------------------------------------------------------

	public void lier(ServerPlayerEntity souverain, int duree) {
		this.dataTracker.set(PROPRIETAIRE, Optional.of(souverain.getUuid()));
		this.ticksRestants = duree;
	}

	@Nullable
	public UUID getProprietaire() {
		return this.dataTracker.get(PROPRIETAIRE).orElse(null);
	}

	@Nullable
	public ServerPlayerEntity souverain() {
		UUID id = getProprietaire();
		if (id == null || !(this.getWorld() instanceof ServerWorld world)) {
			return null;
		}
		return world.getServer().getPlayerManager().getPlayer(id);
	}

	public int getOrdre() {
		return this.dataTracker.get(ORDRE);
	}

	public void setOrdre(int ordre, BlockPos poste) {
		this.dataTracker.set(ORDRE, ordre);
		this.poste = ordre == ORDRE_TENIR ? this.getBlockPos() : poste;
		if (ordre != ORDRE_ATTAQUER) {
			this.setTarget(null);
		}
	}

	public int getTicksRestants() {
		return ticksRestants;
	}

	/** Dissipation : effet de lumière puis retrait, sans mort ni butin. */
	public void dissiper() {
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "garde_dissipation", this.getPos().add(0, this.getHeight() * 0.5, 0), Vec3d.ZERO, 0);
			world.playSound(null, this.getBlockPos(), LimbesSounds.POUVOIR_GARDE, SoundCategory.NEUTRAL, 0.6F, 0.6F);
		}
		this.discard();
	}

	// --- Comportement -------------------------------------------------------------------------------

	@Override
	public void tick() {
		super.tick();
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (--ticksRestants <= 0) {
			dissiper();
			return;
		}
		ServerPlayerEntity souverain = souverain();
		if (souverain == null || souverain.getServerWorld() != world) {
			if (++sansSouverain > 100) {
				dissiper();
			}
			return;
		}
		sansSouverain = 0;
		if (this.squaredDistanceTo(souverain) > 40.0 * 40.0 && getOrdre() != ORDRE_TENIR) {
			Vec3d p = fr.leslimbes.power.PortesDuPalais.autour(world, this, souverain.getPos(), 2.0);
			if (p != null) {
				this.refreshPositionAndAngles(p.x, p.y, p.z, this.getYaw(), 0.0F);
				this.getNavigation().stop();
			}
		}
		if (espece().garde == 2 && this.age % 20 == 0) {
			// Protecteur : aura de résistance pour les alliés proches, et il attire sur lui ceux qui visent le souverain.
			for (LivingEntity a : world.getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(4.0),
					e -> e != this && (e instanceof PlayerEntity || e instanceof fr.leslimbes.npc.HabitantEntity || e instanceof GardeAstraleEntity))) {
				a.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 40, 0, true, false), this);
			}
			for (MobEntity m : world.getEntitiesByClass(MobEntity.class, this.getBoundingBox().expand(8.0), e -> e.getTarget() == souverain && estEnnemi(e))) {
				if (this.random.nextFloat() < 0.5F) {
					m.setTarget(this);
				}
			}
			world.spawnParticles(LimbesParticles.LUEUR, this.getX(), this.getY() + 1.0, this.getZ(), 4, 1.5, 0.5, 1.5, 0.0);
		}
		if (this.age % 8 == 0) {
			world.spawnParticles(LimbesParticles.ETOILE_ROYALE, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.3, 0.1, 0.3, 0.0);
		}
		if (ticksRestants < 60 && this.age % 10 == 0) {
			world.spawnParticles(LimbesParticles.LUEUR, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.3, 0.5, 0.3, 0.0);
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof GardeAstraleEntity) {
			return false;
		}
		if (source.getAttacker() instanceof PlayerEntity p && p.getUuid().equals(getProprietaire())) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	public boolean tryAttack(net.minecraft.entity.Entity target) {
		if (target instanceof PlayerEntity || target instanceof fr.leslimbes.npc.HabitantEntity || target instanceof GardeAstraleEntity) {
			return false;
		}
		if (this.getWorld() instanceof ServerWorld world) {
			world.spawnParticles(LimbesParticles.ETOILE_ROYALE, target.getX(), target.getY() + target.getHeight() * 0.6, target.getZ(), 6, 0.4, 0.4, 0.4, 0.05);
		}
		return super.tryAttack(target);
	}

	@Override
	public void shootAt(LivingEntity cible, float pullProgress) {
		if (!estEnnemi(cible) || !(this.getWorld() instanceof ServerWorld monde) || !fr.leslimbes.LimbesConfig.peutLancerEclat(monde)) {
			return;
		}
		EclatProjectileEntity eclat = new EclatProjectileEntity(this.getWorld(), this, (float) espece().degatsProjectile, null, 0, 0.2);
		eclat.setPosition(this.getX(), this.getEyeY() - 0.2, this.getZ());
		double dx = cible.getX() - this.getX();
		double dy = cible.getBodyY(0.5) - eclat.getY();
		double dz = cible.getZ() - this.getZ();
		double h = Math.sqrt(dx * dx + dz * dz);
		eclat.setVelocity(dx, dy + h * 0.12, dz, 1.6F, 2.0F);
		this.getWorld().spawnEntity(eclat);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.POUVOIR_ARC, SoundCategory.NEUTRAL, 0.8F, 1.1F);
	}

	@Override
	public int etatAnimation() {
		return this.isAttacking() ? 1 : 0;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return LimbesSounds.POUVOIR_REFUS;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return LimbesSounds.POUVOIR_GARDE;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		UUID id = getProprietaire();
		if (id != null) {
			nbt.putUuid("Proprietaire", id);
		}
		nbt.putInt("TicksRestants", ticksRestants);
		nbt.putInt("Ordre", getOrdre());
		if (poste != null) {
			nbt.put("Poste", NbtHelper.fromBlockPos(poste));
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.containsUuid("Proprietaire")) {
			this.dataTracker.set(PROPRIETAIRE, Optional.of(nbt.getUuid("Proprietaire")));
		}
		this.ticksRestants = nbt.contains("TicksRestants") ? nbt.getInt("TicksRestants") : 200;
		this.dataTracker.set(ORDRE, nbt.getInt("Ordre"));
		this.poste = NbtHelper.toBlockPos(nbt, "Poste").orElse(null);
	}

	/** Suivre le souverain quand il s'éloigne (sauf ordre de tenir, ou cible en vue). */
	private final class SuivreGoal extends Goal {
		SuivreGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			ServerPlayerEntity s = souverain();
			return s != null && getOrdre() != ORDRE_TENIR && getTarget() == null && squaredDistanceTo(s) > 25.0;
		}

		@Override
		public boolean shouldContinue() {
			ServerPlayerEntity s = souverain();
			return s != null && getOrdre() != ORDRE_TENIR && getTarget() == null && squaredDistanceTo(s) > 9.0;
		}

		@Override
		public void tick() {
			ServerPlayerEntity s = souverain();
			if (s != null && age % 10 == 0) {
				getNavigation().startMovingTo(s, 1.1);
			}
		}

		@Override
		public void stop() {
			getNavigation().stop();
		}
	}

	/** Tenir la position donnée : y revenir si l'on s'en écarte. */
	private final class TenirGoal extends Goal {
		TenirGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			return getOrdre() == ORDRE_TENIR && poste != null && getTarget() == null && getBlockPos().getSquaredDistance(poste) > 9.0;
		}

		@Override
		public boolean shouldContinue() {
			return canStart();
		}

		@Override
		public void tick() {
			if (poste != null && age % 10 == 0) {
				getNavigation().startMovingTo(poste.getX() + 0.5, poste.getY(), poste.getZ() + 0.5, 1.0);
			}
		}

		@Override
		public void stop() {
			getNavigation().stop();
		}
	}
}
