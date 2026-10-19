package fr.leslimbes.npc;

import fr.leslimbes.npc.dialogue.DialogueSession;
import fr.leslimbes.world.state.LimbesWorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Habitant des Limbes : personnage non joueur avec un rôle (texture, dialogue, nom).
 *
 * <p>Les personnages essentiels (voir {@link #ROLES_ESSENTIELS}) ne peuvent pas mourir d'un accident :
 * ils sont invulnérables hors commandes d'administration et réapparaissent à leur domicile s'ils
 * venaient à disparaître (voir {@link EssentialNpcs}).</p>
 */
public class HabitantEntity extends PathAwareEntity {
	public static final Set<String> ROLES_ESSENTIELS = Set.of("maelle", "thibault", "orielle", "brann", "vaeron", "gaspard", "emile", "lisbeth", "rosalie",
			"sabine", "loic", "hanna", "theo", "marguerite", "ysolde", "marchand_1", "marchand_2",
			"kael", "aelis", "oriane", "sahir", "ismael", "nerine", "orsane", "intendant_garde", "elouan", "anselme");
	private static final TrackedData<String> ROLE = DataTracker.registerData(HabitantEntity.class, TrackedDataHandlerRegistry.STRING);
	private static final TrackedData<Boolean> EN_CONVERSATION = DataTracker.registerData(HabitantEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	@Nullable
	private BlockPos domicile;
	@Nullable
	private PlayerEntity interlocuteur;
	private boolean ancrageEnregistre;

	public HabitantEntity(EntityType<? extends PathAwareEntity> type, World world) {
		super(type, world);
		this.setPersistent();
	}

	public static DefaultAttributeContainer.Builder creerAttributs() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(ROLE, "habitant_1");
		builder.add(EN_CONVERSATION, false);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new ConverserGoal());
		this.goalSelector.add(1, new RentrerGoal());
		this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F, 0.6F));
		this.goalSelector.add(4, new WanderAroundFarGoal(this, 0.6, 0.004F));
		this.goalSelector.add(5, new LookAroundGoal(this));
	}

	public String getRole() {
		return this.dataTracker.get(ROLE);
	}

	public void setRole(String role) {
		this.dataTracker.set(ROLE, role);
	}

	public boolean estEssentiel() {
		return ROLES_ESSENTIELS.contains(getRole());
	}

	public boolean estEnConversation() {
		return this.dataTracker.get(EN_CONVERSATION);
	}

	public Optional<BlockPos> getDomicile() {
		return Optional.ofNullable(domicile);
	}

	public void setDomicile(@Nullable BlockPos pos) {
		this.domicile = pos == null ? null : pos.toImmutable();
	}

	@Override
	public Text getName() {
		if (this.hasCustomName()) {
			return super.getName();
		}
		return Text.translatable("pnj.les_limbes." + getRole());
	}

	@Override
	protected ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		if (player.getWorld().isClient) {
			return ActionResult.SUCCESS;
		}
		if (player instanceof ServerPlayerEntity serverPlayer) {
			DialogueSession.ouvrir(serverPlayer, this);
		}
		return ActionResult.CONSUME;
	}

	public void commencerConversation(PlayerEntity player) {
		this.interlocuteur = player;
		this.dataTracker.set(EN_CONVERSATION, true);
		this.getNavigation().stop();
	}

	public void terminerConversation() {
		this.interlocuteur = null;
		this.dataTracker.set(EN_CONVERSATION, false);
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.getWorld().isClient && !ancrageEnregistre && estEssentiel() && this.getWorld() instanceof ServerWorld sw) {
			ancrageEnregistre = true;
			if (domicile == null) {
				domicile = this.getBlockPos();
			}
			LimbesWorldState.get(sw.getServer()).ancrerPnj(getRole(), domicile);
		}
		if (interlocuteur != null && (!interlocuteur.isAlive() || this.squaredDistanceTo(interlocuteur) > 100.0)) {
			if (interlocuteur instanceof ServerPlayerEntity sp) {
				DialogueSession.fermer(sp);
			}
			terminerConversation();
		}
	}

	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		if (source.isOf(DamageTypes.OUT_OF_WORLD) || source.isOf(DamageTypes.GENERIC_KILL)) {
			return false;
		}
		return estEssentiel() || super.isInvulnerableTo(source);
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return !estEnConversation();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putString("Role", getRole());
		if (domicile != null) {
			nbt.put("Domicile", NbtHelper.fromBlockPos(domicile));
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("Role")) {
			setRole(nbt.getString("Role"));
		}
		domicile = NbtHelper.toBlockPos(nbt, "Domicile").orElse(null);
	}

	/** Pendant une conversation : rester immobile et regarder le joueur. */
	private final class ConverserGoal extends Goal {
		ConverserGoal() {
			this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			return interlocuteur != null;
		}

		@Override
		public void tick() {
			if (interlocuteur != null) {
				getLookControl().lookAt(interlocuteur, 30.0F, 30.0F);
				getNavigation().stop();
			}
		}
	}

	/** Revenir vers son domicile lorsqu'on s'en est trop éloigné. */
	private final class RentrerGoal extends Goal {
		RentrerGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			return domicile != null && interlocuteur == null && squaredDistanceTo(Vec3d.ofCenter(domicile)) > 20.0 * 20.0;
		}

		@Override
		public boolean shouldContinue() {
			return domicile != null && !getNavigation().isIdle() && squaredDistanceTo(Vec3d.ofCenter(domicile)) > 6.0 * 6.0;
		}

		@Override
		public void start() {
			if (domicile != null) {
				getNavigation().startMovingTo(domicile.getX() + 0.5, domicile.getY(), domicile.getZ() + 0.5, 0.7);
			}
		}
	}
}
