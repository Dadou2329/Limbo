package fr.leslimbes.entity;

import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;

/** Poisson-lune : petit poisson lumineux qui nage dans l'air des Prairies de l'Aube. Inoffensif. */
public class PoissonLuneEntity extends PathAwareEntity {
	public PoissonLuneEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.moveControl = new FlightMoveControl(this, 20, true);
		this.setNoGravity(true);
	}

	public static DefaultAttributeContainer.Builder creerAttributs() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 4.0)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.5)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2);
	}

	public static boolean peutApparaitre(EntityType<PoissonLuneEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return pos.getY() > 30 && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new FlyGoal(this, 0.8));
		this.goalSelector.add(2, new LookAroundGoal(this));
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		BirdNavigation nav = new BirdNavigation(this, world);
		nav.setCanPathThroughDoors(false);
		nav.setCanSwim(false);
		nav.setCanEnterOpenDoors(true);
		return nav;
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return LimbesSounds.POISSON_SCINTILLE;
	}

	@Override
	public int getMinAmbientSoundDelay() {
		return 200;
	}

	@Override
	protected float getSoundVolume() {
		return 0.35F;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}
}
