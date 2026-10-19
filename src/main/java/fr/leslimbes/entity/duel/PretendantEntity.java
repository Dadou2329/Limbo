package fr.leslimbes.entity.duel;

import fr.leslimbes.duel.Duel;
import fr.leslimbes.duel.DuelDefinition;
import fr.leslimbes.duel.DuelManager;
import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.entity.region.EclatProjectileEntity;
import fr.leslimbes.entity.region.SoutienEntity;
import fr.leslimbes.power.Abilities;
import fr.leslimbes.registry.LimbesBlocks;
import fr.leslimbes.registry.LimbesPretendants;
import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Boss d'un duel de prétendant. Toutes les attaques sont annoncées (préparation visible, même signal que le
 * Veilleur) ; les phases viennent de la définition du duel (seuils de vie) et sont interprétées selon le
 * {@link StyleDuel} : racines et fleurs d'Aélis, portes et escales de Kaël, reflets et armure d'Orsane, salves
 * suspendues et fenêtres de Sahir, légions, bannières et forteresse de Vaëron. Les blocs posés sont temporaires
 * et toujours retirés par le gestionnaire. Les dégâts d'un coup sont plafonnés : la victoire dépend de la
 * maîtrise des annonces, pas d'un seul coup énorme.
 */
public class PretendantEntity extends HostileEntity implements EntiteAnimee {
	private static final TrackedData<Integer> PREPARATION = DataTracker.registerData(PretendantEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> PHASE = DataTracker.registerData(PretendantEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Boolean> INVULNERABLE = DataTracker.registerData(PretendantEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	/** Geste en cours pour le modèle : 0 rien, 1 coup de mêlée, 2 tir, 3 porte, 4 armure/garde. */
	private static final TrackedData<Integer> GESTE = DataTracker.registerData(PretendantEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final float PLAFOND_DEGATS_PAR_COUP = 14.0F;

	private StyleDuel style;
	private int recuperation;
	private int cooldownSpecial = 40;
	private int fenetreVulnerable;
	private int suspension;
	private int gesteRestant;
	private int pulsation;
	private boolean envolees;
	private final List<UUID> eclatsSuspendus = new ArrayList<>();
	@Nullable
	private Duel duelCache;

	public PretendantEntity(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
		this.experiencePoints = 60;
		this.setPersistent();
	}

	public StyleDuel style() {
		if (style == null) {
			style = LimbesPretendants.styleDe(this.getType());
		}
		return style;
	}

	public static DefaultAttributeContainer.Builder creerAttributs(LimbesPretendants.Fiche f) {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, f.vie())
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, f.vitesse())
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, f.degats())
				.add(EntityAttributes.GENERIC_ARMOR, f.armure())
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
				.add(EntityAttributes.GENERIC_STEP_HEIGHT, 1.0);
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(PREPARATION, 0);
		builder.add(PHASE, 0);
		builder.add(INVULNERABLE, false);
		builder.add(GESTE, 0);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(2, new AttaqueDuelGoal());
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 16.0F));
		this.goalSelector.add(7, new LookAroundGoal(this));
		this.targetSelector.add(1, new RevengeGoal(this));
		this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	// --- état visible ----------------------------------------------------------------------------

	public int getPreparation() {
		return this.dataTracker.get(PREPARATION);
	}

	public boolean enPreparation() {
		return getPreparation() > 0;
	}

	public int getPhase() {
		return this.dataTracker.get(PHASE);
	}

	public boolean estInvulnerable() {
		return this.dataTracker.get(INVULNERABLE);
	}

	public void setInvulnerableDuel(boolean valeur) {
		this.dataTracker.set(INVULNERABLE, valeur);
		this.setGlowing(!valeur && style() == StyleDuel.SAHIR);
	}

	@Override
	public int etatAnimation() {
		if (enPreparation()) {
			return this.dataTracker.get(GESTE) == 2 ? 2 : 1;
		}
		int g = this.dataTracker.get(GESTE);
		if (g == 3 || g == 4) {
			return g;
		}
		return recuperation > 0 ? 5 : 0;
	}

	@Nullable
	private Duel duel() {
		if (duelCache == null || !duelCache.boss.equals(this.getUuid())) {
			duelCache = DuelManager.duelDe(this);
		}
		return duelCache;
	}

	// --- tick ------------------------------------------------------------------------------------

	@Override
	public void tickMovement() {
		super.tickMovement();
		if (!(this.getWorld() instanceof ServerWorld world)) {
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
				if (this.dataTracker.get(GESTE) == 2) {
					tirer(world);
				} else {
					frapper(world);
				}
			}
		} else if (recuperation > 0) {
			recuperation--;
		}
		if (cooldownSpecial > 0) {
			cooldownSpecial--;
		}
		if (gesteRestant > 0 && --gesteRestant == 0 && !enPreparation()) {
			this.dataTracker.set(GESTE, 0);
		}
		// Sahir : les éclats suspendus partent après leur délai ; puis une fenêtre où il est vulnérable.
		if (suspension > 0 && --suspension == 0) {
			relacherEclats(world);
		}
		if (fenetreVulnerable > 0) {
			fenetreVulnerable--;
			if (fenetreVulnerable == 0 && style() == StyleDuel.SAHIR) {
				setInvulnerableDuel(true);
			}
		}
		// Orsane : l'armure tient tant qu'un point de rupture subsiste.
		if (style() == StyleDuel.ORSANE && estInvulnerable() && getPhase() >= 2 && this.age % 10 == 0) {
			boolean ruptures = !world.getEntitiesByClass(SoutienEntity.class, new Box(this.getBlockPos()).expand(24), s -> s.isAlive() && s.espece().rupture).isEmpty();
			if (!ruptures) {
				setInvulnerableDuel(false);
				this.dataTracker.set(GESTE, 0);
				world.playSound(null, this.getBlockPos(), LimbesSounds.CRISTAL_RESONNE, SoundCategory.HOSTILE, 1.2F, 0.6F);
				Abilities.effet(world, "veilleur_frappe", this.getPos().add(0, 1.5, 0), Vec3d.ZERO, 3);
			}
		}
		// Aélis : l'arbre soulève l'arène, par pulsations annoncées.
		if (envolees && ++pulsation >= 220) {
			pulsation = 0;
			Duel duel = duel();
			if (duel != null) {
				for (UUID id : duel.participants) {
					ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(id);
					if (p != null && p.getBlockPos().isWithinDistance(duel.definition.centre(), duel.definition.rayon() + 4)) {
						p.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 22, 1, false, true, true));
						p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 90, 0, false, false, true));
					}
				}
				world.playSound(null, this.getBlockPos(), LimbesSounds.BRUMES, SoundCategory.HOSTILE, 1.0F, 0.8F);
			}
		}
		if (style() == StyleDuel.SAHIR && this.age == 1) {
			setInvulnerableDuel(true);
		}
	}

	// --- attaques ------------------------------------------------------------------------------------

	private void commencerPreparation(int geste, int duree) {
		this.dataTracker.set(GESTE, geste);
		this.dataTracker.set(PREPARATION, duree);
		this.getWorld().playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_PREPARE, SoundCategory.HOSTILE, 1.0F, geste == 2 ? 1.4F : 0.9F);
		if (this.getWorld() instanceof ServerWorld world) {
			Abilities.effet(world, "veilleur_prepare", this.getPos().add(0, this.getHeight() * 0.8, 0), this.getRotationVector(), duree);
		}
	}

	/** Coup de mêlée annoncé (utilisé par l'IA et les tests). */
	public void preparerCoup(LivingEntity cible) {
		setTarget(cible);
		commencerPreparation(1, style() == StyleDuel.CHAMPION ? 14 : 16);
	}

	/** Tir annoncé (utilisé par l'IA et les tests). */
	public void preparerTir(LivingEntity cible) {
		setTarget(cible);
		commencerPreparation(2, 14);
	}

	private void frapper(ServerWorld world) {
		recuperation = 18;
		double portee = 3.4;
		Vec3d avant = this.getPos().add(this.getRotationVector().multiply(portee * 0.5, 0, portee * 0.5));
		Box zone = new Box(avant.x - portee, this.getY() - 1, avant.z - portee, avant.x + portee, this.getY() + 3, avant.z + portee);
		List<LivingEntity> touches = world.getEntitiesByClass(LivingEntity.class, zone,
				e -> e != this && e.isAlive() && e instanceof PlayerEntity && this.squaredDistanceTo(e) <= (portee + 1.0) * (portee + 1.0));
		for (LivingEntity e : touches) {
			this.tryAttack(e);
			Vec3d dir = e.getPos().subtract(this.getPos()).normalize();
			e.takeKnockback(1.0, -dir.x, -dir.z);
		}
		world.playSound(null, this.getBlockPos(), LimbesSounds.VEILLEUR_FRAPPE, SoundCategory.HOSTILE, 1.0F, 0.9F);
		Abilities.effet(world, "veilleur_frappe", avant.add(0, 0.1, 0), this.getRotationVector(), touches.size());
		if (style() == StyleDuel.SAHIR) {
			ouvrirFenetre(50);
		}
		this.dataTracker.set(GESTE, 0);
	}

	private void tirer(ServerWorld world) {
		LivingEntity cible = getTarget();
		this.dataTracker.set(GESTE, 0);
		if (cible == null) {
			return;
		}
		switch (style()) {
			case SAHIR -> suspendreEclats(world, cible, 3 + 2 * Math.min(getPhase(), 2));
			case VAERON -> lancer(world, cible, 5.0F, StatusEffects.WEAKNESS, 60, 0.8, this.getPos().add(0, this.getStandingEyeHeight() - 0.2, 0));
			case ORSANE -> lancer(world, cible, 4.0F, StatusEffects.SLOWNESS, 50, 1.0, this.getPos().add(0, this.getStandingEyeHeight() - 0.2, 0));
			default -> lancer(world, cible, 3.0F, null, 0, 0.5, this.getPos().add(0, this.getStandingEyeHeight() - 0.2, 0));
		}
		recuperation = 12;
	}

	private void lancer(ServerWorld world, LivingEntity cible, float degats, @Nullable net.minecraft.registry.entry.RegistryEntry<net.minecraft.entity.effect.StatusEffect> effet, int duree,
						double recul, Vec3d depart) {
		EclatProjectileEntity eclat = new EclatProjectileEntity(world, this, degats, effet, duree, recul);
		eclat.setPosition(depart);
		double dx = cible.getX() - depart.x;
		double dy = cible.getBodyY(0.5) - depart.y;
		double dz = cible.getZ() - depart.z;
		double h = Math.sqrt(dx * dx + dz * dz);
		eclat.setVelocity(dx, dy + h * 0.12, dz, 1.5F, 2.0F);
		world.spawnEntity(eclat);
		world.playSound(null, this.getBlockPos(), LimbesSounds.TIREUR_TIR, SoundCategory.HOSTILE, 1.0F, 0.8F);
	}

	/** Sahir : des éclats restent suspendus en arc autour de lui, visibles, puis partent tous ensemble. */
	private void suspendreEclats(ServerWorld world, LivingEntity cible, int nombre) {
		eclatsSuspendus.clear();
		for (int i = 0; i < nombre; i++) {
			double angle = Math.toRadians(this.getYaw() + 90) + (i - (nombre - 1) / 2.0) * 0.45;
			Vec3d p = this.getPos().add(Math.cos(angle) * 1.8, 1.6 + 0.2 * Math.abs(i - (nombre - 1) / 2.0), Math.sin(angle) * 1.8);
			EclatProjectileEntity eclat = new EclatProjectileEntity(world, this, 3.0F, StatusEffects.SLOWNESS, 40, 0.3);
			eclat.setPosition(p);
			eclat.setNoGravity(true);
			eclat.setVelocity(Vec3d.ZERO);
			world.spawnEntity(eclat);
			eclatsSuspendus.add(eclat.getUuid());
		}
		suspension = 30;
		setInvulnerableDuel(true);
		world.playSound(null, this.getBlockPos(), LimbesSounds.CRISTAL_RESONNE, SoundCategory.HOSTILE, 1.0F, 0.7F);
	}

	private void relacherEclats(ServerWorld world) {
		LivingEntity cible = getTarget();
		for (UUID id : eclatsSuspendus) {
			Entity e = world.getEntity(id);
			if (e instanceof EclatProjectileEntity eclat && eclat.isAlive()) {
				eclat.setNoGravity(false);
				if (cible != null) {
					double dx = cible.getX() - eclat.getX();
					double dy = cible.getBodyY(0.5) - eclat.getY();
					double dz = cible.getZ() - eclat.getZ();
					double h = Math.sqrt(dx * dx + dz * dz);
					eclat.setVelocity(dx, dy + h * 0.1, dz, 1.4F, 1.0F);
				}
			}
		}
		eclatsSuspendus.clear();
		world.playSound(null, this.getBlockPos(), LimbesSounds.TIREUR_TIR, SoundCategory.HOSTILE, 1.0F, 0.6F);
		ouvrirFenetre(80);
	}

	private void ouvrirFenetre(int ticks) {
		fenetreVulnerable = ticks;
		setInvulnerableDuel(false);
	}

	/** Kaël : une porte s'ouvre et il réapparaît derrière sa cible, lame déjà levée. */
	private void porteDerriere(ServerWorld world, LivingEntity cible) {
		Vec3d regard = cible.getRotationVector();
		Vec3d dest = cible.getPos().subtract(regard.x * 2.2, 0, regard.z * 2.2);
		BlockPos sol = BlockPos.ofFloored(dest);
		int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, sol.getX(), sol.getZ());
		if (Math.abs(y - cible.getY()) > 3) {
			y = (int) Math.floor(cible.getY());
		}
		Abilities.effet(world, "porte_depart", this.getPos().add(0, 1, 0), Vec3d.ZERO, 0);
		this.teleport(world, dest.x, y, dest.z, Set.of(), (float) Math.toDegrees(Math.atan2(-(cible.getX() - dest.x), cible.getZ() - dest.z)), 0.0F);
		Abilities.effet(world, "porte_depart", this.getPos().add(0, 1, 0), Vec3d.ZERO, 0);
		world.playSound(null, this.getBlockPos(), LimbesSounds.POUVOIR_PAS, SoundCategory.HOSTILE, 1.0F, 0.8F);
		this.dataTracker.set(GESTE, 3);
		gesteRestant = 8;
		cooldownSpecial = 90;
		commencerPreparation(1, 14);
	}

	// --- phases ------------------------------------------------------------------------------------------

	/** Appelé par le gestionnaire quand un seuil de vie est franchi. */
	public void entrerPhase(int numero, DuelDefinition.Phase phase, Duel duel) {
		this.dataTracker.set(PHASE, numero);
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (!phase.blocs().isBlank()) {
			poserBlocs(world, duel, phase.blocs(), phase.dureeBlocs());
		}
		switch (style()) {
			case AELIS -> {
				if (numero >= 2) {
					envolees = true;
				}
			}
			case KAEL -> escale(world, duel, numero);
			case ORSANE -> {
				if (numero >= 2) {
					setInvulnerableDuel(true);
					this.dataTracker.set(GESTE, 4);
				} else {
					// Elle se glisse parmi ses reflets.
					Vec3d c = Vec3d.ofCenter(duel.definition.centre());
					double a = world.getRandom().nextDouble() * Math.PI * 2;
					this.teleport(world, c.x + Math.cos(a) * 5, this.getY(), c.z + Math.sin(a) * 5, Set.of(), this.getYaw(), 0.0F);
					Abilities.effet(world, "porte_depart", this.getPos().add(0, 1, 0), Vec3d.ZERO, 0);
				}
			}
			case VAERON -> {
				if (numero >= 3) {
					this.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE) + 2.0);
				}
			}
			default -> {
			}
		}
		this.heal(0.0F);
	}

	/** Kaël : escales sur les îlots du navire ; le boss et les participants sont transportés ensemble. */
	private void escale(ServerWorld world, Duel duel, int numero) {
		Vec3d c = Vec3d.ofBottomCenter(duel.definition.centre());
		Vec3d dest = switch (numero) {
			case 1 -> c.add(16, 3, 0);
			case 2 -> c.add(-16, 5, 0);
			default -> c;
		};
		BlockPos sol = BlockPos.ofFloored(dest);
		if (!world.getBlockState(sol.down()).isSolidBlock(world, sol.down())) {
			return;
		}
		Abilities.effet(world, "porte_depart", this.getPos().add(0, 1, 0), Vec3d.ZERO, 0);
		this.teleport(world, dest.x + 2, dest.y, dest.z, Set.of(), this.getYaw(), 0.0F);
		for (UUID id : duel.participants) {
			ServerPlayerEntity p = world.getServer().getPlayerManager().getPlayer(id);
			if (p != null) {
				Abilities.effet(world, "porte_depart", p.getPos(), Vec3d.ZERO, 0);
				p.teleport(world, dest.x - 2, dest.y, dest.z, Set.of(), p.getYaw(), p.getPitch());
				p.fallDistance = 0.0F;
				p.sendMessage(net.minecraft.text.Text.translatable("message.les_limbes.duel.escale").formatted(net.minecraft.util.Formatting.AQUA), true);
			}
		}
		world.playSound(null, sol, LimbesSounds.PORTE_PASSAGE, SoundCategory.HOSTILE, 1.0F, 1.0F);
	}

	/** Motifs de blocs temporaires : racines (piliers), forteresse (murs à brèches), miroirs (piliers de verre), sables (murets). */
	private void poserBlocs(ServerWorld world, Duel duel, String motif, int duree) {
		BlockPos centre = duel.definition.centre();
		switch (motif) {
			case "racines" -> anneau(world, duel, centre, 6.0, 8, 3, LimbesBlocks.BUCHE_DE_GEANT.getDefaultState(), duree, 0);
			case "forteresse" -> {
				anneau(world, duel, centre, 9.5, 40, 3, LimbesBlocks.PIERRE_DE_COURONNE_TAILLEE.getDefaultState(), duree, 4);
			}
			case "miroirs" -> anneau(world, duel, centre, 7.0, 6, 3, LimbesBlocks.VERRE_DE_MONTAGNE.getDefaultState(), duree, 0);
			case "sables" -> anneau(world, duel, centre, 6.5, 30, 1, LimbesBlocks.GRES_DES_HEURES.getDefaultState(), duree, 3);
			default -> {
			}
		}
		world.playSound(null, centre, LimbesSounds.DALLE_EMERGE, SoundCategory.HOSTILE, 1.2F, 0.7F);
	}

	private void anneau(ServerWorld world, Duel duel, BlockPos centre, double rayon, int points, int hauteur, BlockState bloc, int duree, int breches) {
		for (int i = 0; i < points; i++) {
			if (breches > 0 && i % (points / breches) == 0) {
				continue;
			}
			double a = i * 2 * Math.PI / points;
			int x = centre.getX() + (int) Math.round(Math.cos(a) * rayon);
			int z = centre.getZ() + (int) Math.round(Math.sin(a) * rayon);
			int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
			if (Math.abs(y - centre.getY()) > 4) {
				continue;
			}
			for (int h = 0; h < hauteur; h++) {
				duel.blocs.poser(world, new BlockPos(x, y + h, z), bloc, duree);
			}
		}
	}

	// --- dégâts ------------------------------------------------------------------------------------------

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (estInvulnerable() && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			if (this.getWorld() instanceof ServerWorld world && this.age % 5 == 0) {
				world.playSound(null, this.getBlockPos(), LimbesSounds.POUVOIR_REFUS, SoundCategory.HOSTILE, 0.8F, 1.3F);
				Abilities.effet(world, "veilleur_frappe", this.getPos().add(0, 1.2, 0), Vec3d.ZERO, 0);
			}
			return false;
		}
		return super.damage(source, source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) ? amount : Math.min(amount, PLAFOND_DEGATS_PAR_COUP));
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
		if (!this.getWorld().isClient) {
			DuelManager.victoire(this);
		}
	}

	@Override
	protected void dropLoot(DamageSource source, boolean causedByPlayer) {
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean cannotDespawn() {
		return true;
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
	protected float getSoundVolume() {
		return 1.0F;
	}

	/** Choix d'attaque selon le style : mêlée au contact, portes, tirs ou salves à distance. */
	private final class AttaqueDuelGoal extends Goal {
		AttaqueDuelGoal() {
			this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			LivingEntity cible = getTarget();
			return cible != null && cible.isAlive();
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity cible = getTarget();
			if (cible == null || !(getWorld() instanceof ServerWorld world)) {
				return;
			}
			if (enPreparation() || recuperation > 0 || suspension > 0) {
				return;
			}
			double d2 = squaredDistanceTo(cible);
			getLookControl().lookAt(cible, 30.0F, 30.0F);
			if (d2 <= 3.0 * 3.0) {
				getNavigation().stop();
				preparerCoup(cible);
				return;
			}
			switch (style()) {
				case KAEL -> {
					if (cooldownSpecial == 0 && d2 > 5.0 * 5.0) {
						porteDerriere(world, cible);
						return;
					}
				}
				case SAHIR -> {
					if (cooldownSpecial == 0 && d2 > 4.0 * 4.0 && d2 < 18.0 * 18.0) {
						cooldownSpecial = 110;
						preparerTir(cible);
						return;
					}
				}
				case VAERON, ORSANE -> {
					if (cooldownSpecial == 0 && d2 > 5.0 * 5.0 && d2 < 20.0 * 20.0) {
						cooldownSpecial = 70;
						preparerTir(cible);
						return;
					}
				}
				default -> {
				}
			}
			getNavigation().startMovingTo(cible, 1.05);
		}
	}
}
