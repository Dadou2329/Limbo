package fr.leslimbes.entity;

import fr.leslimbes.registry.LimbesParticles;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Cerf-lanterne : grand animal paisible des Prairies. À la tombée du jour, ses bois s'illuminent et il
 * laisse derrière lui une traînée de lueurs. (Les sentiers vers des secrets viendront avec la suite.)
 */
public class CerfLanterneEntity extends AnimalEntity {
	private static final TrackedData<Boolean> ALLUME = DataTracker.registerData(CerfLanterneEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public CerfLanterneEntity(EntityType<? extends AnimalEntity> type, World world) {
		super(type, world);
	}

	public static DefaultAttributeContainer.Builder creerAttributs() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ALLUME, false);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.6));
		this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.8, 0.01F));
		this.goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 10.0F));
		this.goalSelector.add(6, new LookAroundGoal(this));
	}

	public boolean estAllume() {
		return this.dataTracker.get(ALLUME);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld() instanceof ServerWorld world) {
			if (world.getTime() % 40L == 0L) {
				boolean soir = !world.isDay() || world.getTimeOfDay() % 24000L > 11500L;
				this.dataTracker.set(ALLUME, soir);
			}
			if (estAllume() && world.getTime() % 8L == 0L && this.getVelocity().horizontalLengthSquared() > 0.001) {
				world.spawnParticles(LimbesParticles.LUEUR, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.3, 0.1, 0.3, 0.0);
			}
		}
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
	protected SoundEvent getAmbientSound() {
		return estAllume() ? LimbesSounds.CERF_APPEL : null;
	}

	@Override
	public int getMinAmbientSoundDelay() {
		return 400;
	}
}
