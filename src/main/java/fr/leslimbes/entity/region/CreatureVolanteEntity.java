package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.registry.LimbesEspeces;
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
import org.jetbrains.annotations.Nullable;

/** Petite créature d'ambiance qui nage dans l'air (méduse céleste, libellule, papillon de cristal, colombe). */
public class CreatureVolanteEntity extends PathAwareEntity implements EntiteAnimee {
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;

	public CreatureVolanteEntity(EntityType<? extends PathAwareEntity> type, World world, Espece espece) {
		super(type, world);
				this.moveControl = new FlightMoveControl(this, 20, true);
		this.setNoGravity(true);
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
				.add(EntityAttributes.GENERIC_FLYING_SPEED, e.vitesseVol)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, e.vitesse);
	}

	/** Apparition en l'air, au-dessus des brumes et sous le plafond du monde. */
	public static <T extends MobEntity> boolean peutApparaitreEnVol(EntityType<T> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return pos.getY() > 30 && pos.getY() < 200 && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
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
		return 360;
	}

	@Override
	protected float getSoundVolume() {
		return 0.5F;
	}
}
