package fr.leslimbes.entity;

import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesEntities;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Raie céleste : grande raie qui nage dans l'air des Prairies. Elle s'approche d'un joueur qui tient
 * une fleur de rosée ; nourrie trois fois, elle conclut un pacte et devient une monture volante.
 *
 * <p>Vol : on regarde où l'on veut aller (haut = monter, bas = descendre) et l'on avance. L'endurance
 * baisse en vol monté et se refait au repos ; à zéro, la raie descend doucement et ne peut plus monter.
 * Un démontage en l'air donne la chute lente au cavalier. Le sifflet rappelle la raie, et la recrée
 * si elle a disparu (chunk déchargé, accident) : une monture liée ne se perd jamais définitivement.</p>
 */
public class RaieCelesteEntity extends TameableEntity implements EntiteAnimee {
	public static final String CLE_MONTURE = "raie_celeste";
	public static final float ENDURANCE_MAX = 100.0F;
	private static final int NOURRITURES_PACTE = 3;
	private static final TrackedData<Float> ENDURANCE = DataTracker.registerData(RaieCelesteEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Integer> NOURRIE = DataTracker.registerData(RaieCelesteEntity.class, TrackedDataHandlerRegistry.INTEGER);

	@Nullable
	private BlockPos repos;

	public RaieCelesteEntity(EntityType<? extends TameableEntity> type, World world) {
		super(type, world);
		this.moveControl = new FlightMoveControl(this, 20, true);
		this.setNoGravity(true);
	}

	public static DefaultAttributeContainer.Builder creerAttributs() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5);
	}

	public static boolean peutApparaitre(EntityType<RaieCelesteEntity> type, ServerWorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
		return pos.getY() > 40 && pos.getY() < 150 && world.getBlockState(pos).isAir() && world.getBlockState(pos.up()).isAir();
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ENDURANCE, ENDURANCE_MAX);
		builder.add(NOURRIE, 0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new TemptGoal(this, 1.1, Ingredient.ofItems(LimbesBlocks.FLEUR_DE_ROSEE), false));
		this.goalSelector.add(2, new RetourAuReposGoal());
		this.goalSelector.add(3, new FlyGoal(this, 0.7));
		this.goalSelector.add(4, new LookAroundGoal(this));
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		BirdNavigation nav = new BirdNavigation(this, world);
		nav.setCanPathThroughDoors(false);
		nav.setCanSwim(false);
		nav.setCanEnterOpenDoors(true);
		return nav;
	}

	// --- état --------------------------------------------------------------------------------
	public float getEndurance() {
		return this.dataTracker.get(ENDURANCE);
	}

	public void setEndurance(float valeur) {
		this.dataTracker.set(ENDURANCE, MathHelper.clamp(valeur, 0.0F, ENDURANCE_MAX));
	}

	public int getNourrie() {
		return this.dataTracker.get(NOURRIE);
	}

	public boolean estMontee() {
		return this.getControllingPassenger() instanceof PlayerEntity;
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.getWorld() instanceof ServerWorld world) {
			if (estMontee()) {
				boolean enMouvement = this.getVelocity().lengthSquared() > 0.01;
				if (enMouvement) {
					setEndurance(getEndurance() - 0.09F);
				}
			} else {
				setEndurance(getEndurance() + (this.isOnGround() ? 0.4F : 0.2F));
			}
			if (this.age % 100 == 0) {
				verifierLien(world);
			}
			if (estMontee() && this.age % 6 == 0 && this.getVelocity().lengthSquared() > 0.02) {
				world.spawnParticles(fr.leslimbes.registry.LimbesParticles.LUEUR, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.6, 0.1, 0.6, 0.0);
			}
		}
	}

	/** Une raie apprivoisée dont le propriétaire a lié une autre raie redevient libre (pas de doublons liés). */
	private void verifierLien(ServerWorld world) {
		if (!isTamed() || getOwnerUuid() == null) {
			return;
		}
		ServerPlayerEntity owner = world.getServer().getPlayerManager().getPlayer(getOwnerUuid());
		if (owner == null) {
			return;
		}
		String liee = PlayerLimbesData.of(owner).montures.get(CLE_MONTURE);
		if (liee != null && !liee.equals(this.getUuidAsString())) {
			setTamed(false, true);
			setOwnerUuid(null);
		}
	}

	// --- interaction : pacte, soins, monte ------------------------------------------------------
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		ItemStack stack = player.getStackInHand(hand);
		boolean fleur = stack.isOf(LimbesBlocks.FLEUR_DE_ROSEE.asItem());
		if (this.getWorld().isClient) {
			return fleur || isTamed() ? ActionResult.SUCCESS : ActionResult.PASS;
		}
		if (!isTamed()) {
			if (!fleur) {
				return ActionResult.PASS;
			}
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			int n = getNourrie() + 1;
			this.dataTracker.set(NOURRIE, n);
			this.heal(2.0F);
			this.showEmoteParticle(true);
			this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.RAIE_APPEL, SoundCategory.NEUTRAL, 0.8F, 1.2F);
			if (n >= NOURRITURES_PACTE && player instanceof ServerPlayerEntity sp) {
				conclurePacte(sp);
			} else {
				player.sendMessage(Text.translatable("message.les_limbes.raie.nourrie", n, NOURRITURES_PACTE), true);
			}
			return ActionResult.CONSUME;
		}
		if (fleur && this.getHealth() < this.getMaxHealth()) {
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			this.heal(6.0F);
			this.showEmoteParticle(true);
			return ActionResult.CONSUME;
		}
		if (player.getUuid().equals(getOwnerUuid()) && !this.hasPassengers()) {
			if (player.startRiding(this)) {
				this.repos = null;
				return ActionResult.CONSUME;
			}
		}
		return ActionResult.PASS;
	}

	private void conclurePacte(ServerPlayerEntity player) {
		setOwner(player);
		setTamed(true, true);
		setPersistent();
		setEndurance(ENDURANCE_MAX);
		PlayerLimbesData data = PlayerLimbesData.of(player);
		data.montures.put(CLE_MONTURE, this.getUuidAsString());
		data.poserDrapeau("pacte_raie");
		if (!player.getInventory().contains(new ItemStack(LimbesItems.SIFFLET_DE_RAIE))) {
			ItemStack sifflet = new ItemStack(LimbesItems.SIFFLET_DE_RAIE);
			if (!player.giveItemStack(sifflet)) {
				player.dropItem(sifflet, false);
			}
		}
		player.sendMessage(Text.translatable("message.les_limbes.raie.pacte").formatted(Formatting.GOLD), false);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.RAIE_PACTE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		Abilities.effet((ServerWorld) this.getWorld(), "pacte", this.getPos().add(0, 0.5, 0), Vec3d.ZERO, 0);
		QuestManager.signaler(player, QuestSignal.drapeau("pacte_raie"));
		PlayerDataSync.envoyer(player);
	}

	/** Rappel par le sifflet : téléporte la raie liée, ou la recrée si elle est introuvable. */
	public static void rappeler(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String uuid = data.montures.get(CLE_MONTURE);
		if (uuid == null) {
			player.sendMessage(Text.translatable("message.les_limbes.raie.aucune").formatted(Formatting.RED), true);
			return;
		}
		ServerWorld world = player.getServerWorld();
		Entity existante = null;
		try {
			existante = world.getEntity(UUID.fromString(uuid));
		} catch (IllegalArgumentException ignored) {
		}
		Vec3d regard = player.getRotationVector();
		Vec3d dest = player.getPos().add(regard.x * 2.5, 1.2, regard.z * 2.5);
		if (existante instanceof RaieCelesteEntity raie && raie.isAlive()) {
			raie.removeAllPassengers();
			raie.teleport(world, dest.x, dest.y, dest.z, Set.of(), player.getYaw(), 0.0F);
			raie.setVelocity(Vec3d.ZERO);
			raie.repos = null;
			player.sendMessage(Text.translatable("message.les_limbes.raie.rappel"), true);
		} else {
			RaieCelesteEntity raie = LimbesEntities.RAIE_CELESTE.create(world);
			if (raie == null) {
				return;
			}
			raie.refreshPositionAndAngles(dest.x, dest.y, dest.z, player.getYaw(), 0.0F);
			raie.setOwner(player);
			raie.setTamed(true, true);
			raie.setPersistent();
			raie.setEndurance(ENDURANCE_MAX);
			world.spawnEntity(raie);
			data.montures.put(CLE_MONTURE, raie.getUuidAsString());
			PlayerDataSync.envoyer(player);
			player.sendMessage(Text.translatable("message.les_limbes.raie.recree"), true);
		}
		world.playSound(null, BlockPos.ofFloored(dest), LimbesSounds.RAIE_APPEL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		Abilities.effet(world, "pacte_appel", dest, Vec3d.ZERO, 0);
	}

	// --- vol -----------------------------------------------------------------------------------
	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		if (isTamed() && this.getFirstPassenger() instanceof PlayerEntity player) {
			return player;
		}
		return super.getControllingPassenger();
	}

	@Override
	protected Vec3d getControlledMovementInput(PlayerEntity rider, Vec3d input) {
		float avant = rider.forwardSpeed;
		float cote = rider.sidewaysSpeed * 0.5F;
		double vertical = 0.0;
		if (avant > 0.0F) {
			vertical = -Math.sin(rider.getPitch() * (Math.PI / 180.0)) * avant;
		} else if (avant < 0.0F) {
			avant *= 0.4F;
		}
		if (getEndurance() <= 0.0F) {
			vertical = Math.min(vertical, -0.25);
		}
		return new Vec3d(cote, vertical, avant);
	}

	@Override
	protected void tickControlled(PlayerEntity rider, Vec3d input) {
		super.tickControlled(rider, input);
		this.setRotation(rider.getYaw(), rider.getPitch() * 0.5F);
		this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
	}

	@Override
	public void travel(Vec3d input) {
		if (this.isLogicalSideForUpdatingMovement()) {
			boolean montee = estMontee();
			float vitesse = montee ? 0.085F : 0.035F;
			if (montee && getEndurance() <= 0.0F) {
				vitesse *= 0.6F;
			}
			this.updateVelocity(vitesse, input);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply(0.88));
		}
		this.updateLimbs(false);
	}

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (passenger instanceof LivingEntity living && !this.getWorld().isClient) {
			protegerChute(living);
			this.repos = this.getBlockPos();
		}
	}

	private void protegerChute(LivingEntity living) {
		if (hauteurSousMoi() > 3) {
			living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 240, 0, false, false, true));
		}
	}

	private int hauteurSousMoi() {
		BlockPos pos = this.getBlockPos();
		for (int i = 0; i < 24; i++) {
			BlockPos p = pos.down(i);
			if (p.getY() < this.getWorld().getBottomY() || !this.getWorld().getBlockState(p).getCollisionShape(this.getWorld(), p).isEmpty()) {
				return i;
			}
		}
		return 24;
	}

	@Override
	public void onDeath(DamageSource source) {
		if (!this.getWorld().isClient) {
			for (Entity e : this.getPassengerList()) {
				if (e instanceof LivingEntity living) {
					protegerChute(living);
				}
			}
		}
		super.onDeath(source);
	}

	// --- divers ----------------------------------------------------------------------------------
	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
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
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return LimbesSounds.RAIE_APPEL;
	}

	@Override
	public int getMinAmbientSoundDelay() {
		return 300;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("Endurance", getEndurance());
		nbt.putInt("Nourrie", getNourrie());
		if (repos != null) {
			nbt.put("Repos", NbtHelper.fromBlockPos(repos));
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		setEndurance(nbt.contains("Endurance") ? nbt.getFloat("Endurance") : ENDURANCE_MAX);
		this.dataTracker.set(NOURRIE, nbt.getInt("Nourrie"));
		repos = NbtHelper.toBlockPos(nbt, "Repos").orElse(null);
	}

	/** Une raie apprivoisée reste près de l'endroit où on l'a laissée. */
	private final class RetourAuReposGoal extends Goal {
		RetourAuReposGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			return isTamed() && repos != null && !hasPassengers() && squaredDistanceTo(Vec3d.ofCenter(repos)) > 10.0 * 10.0;
		}

		@Override
		public boolean shouldContinue() {
			return repos != null && !getNavigation().isIdle() && !hasPassengers();
		}

		@Override
		public void start() {
			if (repos != null) {
				getNavigation().startMovingTo(repos.getX() + 0.5, repos.getY() + 1.5, repos.getZ() + 0.5, 0.9);
			}
		}
	}
}
