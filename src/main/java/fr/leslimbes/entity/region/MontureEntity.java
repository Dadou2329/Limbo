package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.entity.RaieCelesteEntity;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesItems;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.FlyGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Monture régionale générique (tortue des lagons, scarabée de sève, gazelle d'opale, griffon de verre, cerf
 * royal). Même contrat que la raie céleste : on l'approche avec son appât, nourrie {@code espece().nourritures}
 * fois elle conclut un pacte et se lie au joueur ; le sifflet la rappelle ou la recrée si elle a disparu.
 *
 * <p>Au sol : on avance dans la direction du regard, la barre d'espace saute (barre de saut comme un cheval),
 * la monture franchit une marche d'un bloc. Une monture nageuse flotte en surface avec son cavalier. En vol
 * (griffon) : même pilotage que la raie, avec l'endurance.</p>
 */
public class MontureEntity extends TameableEntity implements EntiteAnimee, JumpingMount {
	public static final String CLE_ACTIVE = "active";
	public static final float ENDURANCE_MAX = 100.0F;
	private static final TrackedData<Float> ENDURANCE = DataTracker.registerData(MontureEntity.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Integer> NOURRIE = DataTracker.registerData(MontureEntity.class, TrackedDataHandlerRegistry.INTEGER);
	/** Fiche de l'espèce, résolue depuis le type : le constructeur parent appelle initGoals() avant l'affectation des champs. */
	private Espece espece;
	@Nullable
	private BlockPos repos;
	private float forceSaut;

	public MontureEntity(EntityType<? extends TameableEntity> type, World world, Espece espece) {
		super(type, world);
				if (espece().vol) {
			this.moveControl = new FlightMoveControl(this, 20, true);
			this.setNoGravity(true);
		}
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
				.add(EntityAttributes.GENERIC_FLYING_SPEED, e.vitesseVol)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, e.pas)
				.add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, e.chuteSure)
				.add(EntityAttributes.GENERIC_JUMP_STRENGTH, 0.7);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ENDURANCE, ENDURANCE_MAX);
		builder.add(NOURRIE, 0);
	}

	@Override
	protected void initGoals() {
		if (!espece().vol) {
			this.goalSelector.add(0, new SwimGoal(this));
		}
		if (espece().appat != null) {
			this.goalSelector.add(1, new TemptGoal(this, 1.1, Ingredient.ofItems(espece().appat), false));
		}
		this.goalSelector.add(2, new RetourAuReposGoal());
		if (espece().vol) {
			this.goalSelector.add(3, new FlyGoal(this, 0.7));
		} else {
			this.goalSelector.add(3, new WanderAroundFarGoal(this, 0.8, 0.01F));
		}
		this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(5, new LookAroundGoal(this));
	}

	@Override
	protected EntityNavigation createNavigation(World world) {
		if (espece().vol) {
			BirdNavigation nav = new BirdNavigation(this, world);
			nav.setCanPathThroughDoors(false);
			nav.setCanSwim(false);
			nav.setCanEnterOpenDoors(true);
			return nav;
		}
		if (espece().nage) {
			return new AmphibiousSwimNavigation(this, world);
		}
		return super.createNavigation(world);
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
		if (espece().vol) {
			this.setNoGravity(true);
		}
		if (this.getWorld() instanceof ServerWorld world) {
			if (espece().vol) {
				if (estMontee()) {
					if (this.getVelocity().lengthSquared() > 0.01) {
						setEndurance(getEndurance() - 0.08F);
					}
				} else {
					setEndurance(getEndurance() + (this.isOnGround() ? 0.4F : 0.2F));
				}
			}
			if (this.age % 100 == 0) {
				verifierLien(world);
			}
			if (estMontee() && this.age % 8 == 0 && this.getVelocity().lengthSquared() > 0.03) {
				world.spawnParticles(fr.leslimbes.registry.LimbesParticles.LUEUR, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.5, 0.1, 0.5, 0.0);
			}
		}
	}

	/** Une monture liée dont le propriétaire a lié une autre bête de la même espèce redevient libre. */
	private void verifierLien(ServerWorld world) {
		if (!isTamed() || getOwnerUuid() == null) {
			return;
		}
		ServerPlayerEntity owner = world.getServer().getPlayerManager().getPlayer(getOwnerUuid());
		if (owner == null) {
			return;
		}
		String liee = PlayerLimbesData.of(owner).montures.get(espece().id);
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
		boolean appat = espece().appat != null && stack.isOf(espece().appat);
		if (this.getWorld().isClient) {
			return appat || isTamed() ? ActionResult.SUCCESS : ActionResult.PASS;
		}
		if (!isTamed()) {
			if (!appat) {
				return ActionResult.PASS;
			}
			if (!player.isCreative()) {
				stack.decrement(1);
			}
			int n = getNourrie() + 1;
			this.dataTracker.set(NOURRIE, n);
			this.heal(2.0F);
			this.showEmoteParticle(true);
			this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.MONTURE_APPEL, SoundCategory.NEUTRAL, 0.8F, 1.2F);
			if (n >= espece().nourritures && player instanceof ServerPlayerEntity sp) {
				conclurePacte(sp);
			} else {
				player.sendMessage(Text.translatable("message.les_limbes.monture.nourrie", this.getName(), n, espece().nourritures), true);
			}
			return ActionResult.CONSUME;
		}
		if (appat && this.getHealth() < this.getMaxHealth()) {
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
		data.montures.put(espece().id, this.getUuidAsString());
		data.montures.put(CLE_ACTIVE, espece().id);
		data.poserDrapeau("pacte_" + espece().id);
		if (!player.getInventory().contains(new ItemStack(LimbesItems.SIFFLET_DE_RAIE))) {
			ItemStack sifflet = new ItemStack(LimbesItems.SIFFLET_DE_RAIE);
			if (!player.giveItemStack(sifflet)) {
				player.dropItem(sifflet, false);
			}
		}
		player.sendMessage(Text.translatable(espece().vol ? "message.les_limbes.monture.pacte_vol" : "message.les_limbes.monture.pacte", this.getName()).formatted(Formatting.GOLD), false);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.RAIE_PACTE, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		Abilities.effet((ServerWorld) this.getWorld(), "pacte", this.getPos().add(0, 0.5, 0), Vec3d.ZERO, 0);
		QuestManager.signaler(player, QuestSignal.drapeau("pacte_" + espece().id));
		PlayerDataSync.envoyer(player);
	}

	/** Rappel par le sifflet : téléporte la monture liée de cette espèce, ou la recrée si elle est introuvable. */
	public static void rappeler(ServerPlayerEntity player, Espece fiche) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String uuid = data.montures.get(fiche.id);
		if (uuid == null) {
			player.sendMessage(Text.translatable("message.les_limbes.monture.aucune").formatted(Formatting.RED), true);
			return;
		}
		ServerWorld world = player.getServerWorld();
		Entity existante = null;
		try {
			existante = world.getEntity(UUID.fromString(uuid));
		} catch (IllegalArgumentException ignored) {
		}
		Vec3d regard = player.getRotationVector();
		Vec3d dest = player.getPos().add(regard.x * 2.5, fiche.vol ? 1.2 : 0.2, regard.z * 2.5);
		Text nom = Text.translatable("entity.les_limbes." + fiche.id);
		if (existante instanceof MontureEntity monture && monture.isAlive() && monture.espece() == fiche) {
			monture.removeAllPassengers();
			monture.teleport(world, dest.x, dest.y, dest.z, Set.of(), player.getYaw(), 0.0F);
			monture.setVelocity(Vec3d.ZERO);
			monture.repos = null;
			player.sendMessage(Text.translatable("message.les_limbes.monture.rappel", nom), true);
		} else {
			Entity creee = LimbesEspeces.type(fiche.id).create(world);
			if (!(creee instanceof MontureEntity monture)) {
				return;
			}
			monture.refreshPositionAndAngles(dest.x, dest.y, dest.z, player.getYaw(), 0.0F);
			monture.setOwner(player);
			monture.setTamed(true, true);
			monture.setPersistent();
			monture.setEndurance(ENDURANCE_MAX);
			world.spawnEntity(monture);
			data.montures.put(fiche.id, monture.getUuidAsString());
			PlayerDataSync.envoyer(player);
			player.sendMessage(Text.translatable("message.les_limbes.monture.recree", nom), true);
		}
		world.playSound(null, BlockPos.ofFloored(dest), LimbesSounds.MONTURE_APPEL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
		Abilities.effet(world, "pacte_appel", dest, Vec3d.ZERO, 0);
	}

	/** Rappelle la monture « active » du joueur (raie céleste ou monture régionale). */
	public static void rappelerActive(ServerPlayerEntity player) {
		String cle = cleActive(PlayerLimbesData.of(player));
		if (cle == null) {
			player.sendMessage(Text.translatable("message.les_limbes.monture.aucune").formatted(Formatting.RED), true);
			return;
		}
		if (cle.equals(RaieCelesteEntity.CLE_MONTURE)) {
			RaieCelesteEntity.rappeler(player);
			return;
		}
		Espece e = LimbesEspeces.espece(cle);
		if (e != null) {
			rappeler(player, e);
		}
	}

	/** Clé de la monture active : celle choisie au sifflet, sinon la raie, sinon la première liée. */
	@Nullable
	public static String cleActive(PlayerLimbesData data) {
		String active = data.montures.get(CLE_ACTIVE);
		if (active != null && data.montures.containsKey(active)) {
			return active;
		}
		if (data.montures.containsKey(RaieCelesteEntity.CLE_MONTURE)) {
			return RaieCelesteEntity.CLE_MONTURE;
		}
		return data.montures.keySet().stream().filter(k -> !k.equals(CLE_ACTIVE)).sorted().findFirst().orElse(null);
	}

	// --- pilotage --------------------------------------------------------------------------------

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
		if (espece().vol) {
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
		if (avant <= 0.0F) {
			avant *= 0.3F;
		}
		return new Vec3d(cote, 0.0, avant);
	}

	@Override
	protected void tickControlled(PlayerEntity rider, Vec3d input) {
		super.tickControlled(rider, input);
		this.setRotation(rider.getYaw(), rider.getPitch() * 0.5F);
		this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
		if (!espece().vol && this.isLogicalSideForUpdatingMovement()) {
			if (forceSaut > 0.0F && this.isOnGround()) {
				sauter(forceSaut, input);
			}
			forceSaut = 0.0F;
			if (espece().nage && this.isSubmergedInWater()) {
				Vec3d v = this.getVelocity();
				this.setVelocity(v.x, Math.max(v.y, 0.12), v.z);
			}
		}
	}

	@Override
	protected float getSaddledSpeed(PlayerEntity rider) {
		return (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
	}

	private void sauter(float force, Vec3d input) {
		double hauteur = this.getAttributeValue(EntityAttributes.GENERIC_JUMP_STRENGTH) * force;
		Vec3d v = this.getVelocity();
		this.setVelocity(v.x, hauteur, v.z);
		this.velocityDirty = true;
		if (input.z > 0.0) {
			float s = MathHelper.sin(this.getYaw() * ((float) Math.PI / 180.0F));
			float c = MathHelper.cos(this.getYaw() * ((float) Math.PI / 180.0F));
			this.setVelocity(this.getVelocity().add(-0.4F * s * force, 0.0, 0.4F * c * force));
		}
	}

	@Override
	public void travel(Vec3d input) {
		if (!espece().vol) {
			super.travel(input);
			return;
		}
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

	// --- saut (barre de saut du cavalier) ------------------------------------------------------------

	@Override
	public void setJumpStrength(int strength) {
		if (strength < 0) {
			strength = 0;
		}
		forceSaut = strength >= 90 ? 1.0F : 0.4F + 0.4F * strength / 90.0F;
	}

	@Override
	public boolean canJump() {
		return !espece().vol && this.isOnGround();
	}

	@Override
	public void startJumping(int height) {
	}

	@Override
	public void stopJumping() {
	}

	// --- descente, chute ---------------------------------------------------------------------------------

	@Override
	protected void removePassenger(Entity passenger) {
		super.removePassenger(passenger);
		if (passenger instanceof LivingEntity living && !this.getWorld().isClient) {
			if (espece().vol) {
				protegerChute(living);
			}
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
		if (!this.getWorld().isClient && espece().vol) {
			for (Entity e : this.getPassengerList()) {
				if (e instanceof LivingEntity living) {
					protegerChute(living);
				}
			}
		}
		super.onDeath(source);
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return !espece().vol && super.handleFallDamage(fallDistance, damageMultiplier, damageSource);
	}

	@Override
	protected void fall(double heightDifference, boolean onGround, net.minecraft.block.BlockState state, BlockPos landedPosition) {
		if (!espece().vol) {
			super.fall(heightDifference, onGround, state, landedPosition);
		}
	}

	// --- divers ----------------------------------------------------------------------------------

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

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return espece().ambiant != null ? espece().ambiant : LimbesSounds.MONTURE_APPEL;
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
		return 400;
	}

	@Override
	protected float getSoundVolume() {
		return 0.7F;
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

	/** Une monture liée reste près de l'endroit où on l'a laissée. */
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
				getNavigation().startMovingTo(repos.getX() + 0.5, repos.getY() + (espece().vol ? 1.5 : 0.0), repos.getZ() + 0.5, 0.9);
			}
		}
	}
}
