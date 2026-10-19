package fr.leslimbes.duel;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.duel.PretendantEntity;
import fr.leslimbes.npc.dialogue.Actions;
import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesPortal;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Duels et épreuves des prétendants. Un duel se lance depuis un dialogue (action {@code duel}) : le joueur est
 * placé à l'entrée de l'arène, le boss apparaît, une barre de boss suit sa vie. Les phases (seuils de vie) sont
 * franchies ici et transmises au boss ; la victoire exécute les actions de la définition pour chaque participant
 * et pose le drapeau {@code duel_<id>_gagne}. Un duel amical renvoie le joueur épuisé à l'entrée (aucune mort) ;
 * dans tous les cas l'échec ou l'abandon retire le boss, ses renforts et les blocs temporaires : on peut recommencer.
 */
public final class DuelManager {
	private static final Map<UUID, Duel> ACTIFS = new HashMap<>();
	private static final int TICKS_HORS_ARENE_MAX = 200;

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(DuelManager::tick);
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
			if (entity instanceof ServerPlayerEntity player) {
				Duel duel = duelDuParticipant(player.getUuid());
				if (duel != null) {
					echouer(player.server, duel, "mort");
				}
			}
		});
	}

	/** Lance (ou rejoint) un duel pour ce joueur. @return true si le joueur participe désormais. */
	public static boolean commencer(ServerPlayerEntity player, String duelId) {
		DuelDefinition def = DuelLoader.get(duelId);
		if (def == null) {
			LesLimbes.LOGGER.warn("Duel inconnu : {}", duelId);
			return false;
		}
		MinecraftServer server = player.server;
		ServerWorld world = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, def.dimension()));
		if (world == null) {
			return false;
		}
		Duel existant = duelDe(def.id());
		if (existant != null) {
			rejoindre(existant, player, world);
			return true;
		}
		EntityType<?> type = Registries.ENTITY_TYPE.getOrEmpty(def.boss()).orElse(null);
		if (type == null) {
			LesLimbes.LOGGER.warn("Duel {} : boss inconnu {}", duelId, def.boss());
			return false;
		}
		// Les chunks de l'arène doivent être chargés : le joueur y est téléporté d'abord.
		placer(player, world, def.entree(), def.yaw());
		Entity boss = type.create(world);
		if (!(boss instanceof MobEntity mob)) {
			return false;
		}
		mob.refreshPositionAndAngles(def.apparition().getX() + 0.5, def.apparition().getY(), def.apparition().getZ() + 0.5, def.yaw() + 180.0F, 0.0F);
		mob.setPersistent();
		mob.initialize(world, world.getLocalDifficulty(def.apparition()), SpawnReason.EVENT, null);
		world.spawnEntity(mob);
		Duel duel = new Duel(def, world.getRegistryKey(), mob.getUuid(), world.getTime());
		ACTIFS.put(mob.getUuid(), duel);
		for (DuelDefinition.Renfort r : def.renforts()) {
			invoquer(world, duel, r, Vec3d.ofBottomCenter(def.apparition()));
		}
		rejoindre(duel, player, world);
		world.playSound(null, def.centre(), LimbesSounds.QUETE_ETAPE, SoundCategory.PLAYERS, 1.0F, 0.7F);
		LesLimbes.LOGGER.info("Duel « {} » commencé par {}", def.titre(), player.getName().getString());
		return true;
	}

	private static void rejoindre(Duel duel, ServerPlayerEntity player, ServerWorld world) {
		duel.joueurs.put(player.getUuid(), player);
		if (duel.participants.add(player.getUuid())) {
			duel.barre.addPlayer(player);
			PlayerLimbesData.of(player).poserDrapeau("duel_" + duel.definition.id() + "_commence");
			LimbesPortal.titre(player, Text.literal(duel.definition.titre()).formatted(Formatting.GOLD),
					Text.translatable(duel.definition.amical() ? "message.les_limbes.duel.amical" : "message.les_limbes.duel.serieux"));
			PlayerDataSync.envoyer(player);
		}
		if (player.getServerWorld() != world || player.getBlockPos().getSquaredDistance(duel.definition.centre()) > (double) (duel.definition.rayon() + 8) * (duel.definition.rayon() + 8)) {
			placer(player, world, duel.definition.entree(), duel.definition.yaw());
		}
	}

	private static void placer(ServerPlayerEntity player, ServerWorld world, BlockPos pos, float yaw) {
		player.stopRiding();
		player.teleport(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), yaw, 0.0F);
		player.setVelocity(Vec3d.ZERO);
		player.fallDistance = 0.0F;
	}

	/** Fait apparaître un renfort autour d'une position (dans l'arène) et le rattache au duel. */
	public static void invoquer(ServerWorld world, Duel duel, DuelDefinition.Renfort renfort, Vec3d autour) {
		EntityType<?> type = Registries.ENTITY_TYPE.getOrEmpty(renfort.type()).orElse(null);
		if (type == null) {
			LesLimbes.LOGGER.warn("Duel {} : renfort inconnu {}", duel.definition.id(), renfort.type());
			return;
		}
		for (int i = 0; i < renfort.nombre(); i++) {
			double angle = world.getRandom().nextDouble() * Math.PI * 2;
			double dist = 2.5 + world.getRandom().nextDouble() * 3.0;
			double x = autour.x + Math.cos(angle) * dist;
			double z = autour.z + Math.sin(angle) * dist;
			int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
			if (Math.abs(y - autour.y) > 6) {
				y = (int) Math.floor(autour.y);
			}
			Entity e = type.create(world);
			if (e == null) {
				continue;
			}
			e.refreshPositionAndAngles(x, y, z, world.getRandom().nextFloat() * 360.0F, 0.0F);
			if (e instanceof MobEntity mob) {
				mob.setPersistent();
				mob.initialize(world, world.getLocalDifficulty(e.getBlockPos()), SpawnReason.EVENT, null);
			}
			world.spawnEntity(e);
			duel.renforts.add(e.getUuid());
		}
	}

	private static void tick(MinecraftServer server) {
		if (ACTIFS.isEmpty()) {
			return;
		}
		for (Duel duel : new ArrayList<>(ACTIFS.values())) {
			ServerWorld world = server.getWorld(duel.monde);
			if (world == null) {
				continue;
			}
			Entity e = world.getEntity(duel.boss);
			if (e instanceof LivingEntity mort && mort.isDead()) {
				// Boss ordinaire (épreuve de tournoi) : sa mort est la victoire.
				terminer(server, duel, true, "victoire");
				continue;
			}
			if (!(e instanceof LivingEntity boss) || !boss.isAlive()) {
				if (ACTIFS.containsKey(duel.boss)) {
					// Boss disparu sans mourir (déchargé, retiré) : le duel est abandonné proprement.
					terminer(server, duel, false, "disparu");
				}
				continue;
			}
			duel.blocs.expirer(world);
			float fraction = boss.getHealth() / boss.getMaxHealth();
			duel.barre.setPercent(Math.max(0.0F, Math.min(1.0F, fraction)));
			// Phases : chaque seuil franchi une seule fois, dans l'ordre.
			while (duel.phase < duel.definition.phases().size() && fraction <= duel.definition.phases().get(duel.phase).seuil()) {
				DuelDefinition.Phase phase = duel.definition.phases().get(duel.phase);
				duel.phase++;
				entrerPhase(world, duel, boss, duel.phase, phase);
			}
			// Participants : hors de l'arène trop longtemps = abandon ; épuisé en duel amical = renvoyé à l'entrée.
			for (UUID id : new ArrayList<>(duel.participants)) {
				ServerPlayerEntity player = joueur(server, duel, id);
				if (player == null) {
					retirer(duel, id);
					continue;
				}
				boolean dehors = player.getServerWorld() != world
						|| !player.getBlockPos().isWithinDistance(duel.definition.centre(), duel.definition.rayon() + 12);
				int t = dehors ? duel.ticksHorsArene.merge(id, 1, Integer::sum) : 0;
				if (!dehors) {
					duel.ticksHorsArene.remove(id);
				}
				if (t > TICKS_HORS_ARENE_MAX) {
					player.sendMessage(Text.translatable("message.les_limbes.duel.abandon").formatted(Formatting.RED), false);
					retirer(duel, id);
					duel.barre.removePlayer(player);
					continue;
				}
				if (duel.definition.amical() && player.getHealth() <= 3.0F && !player.isCreative()) {
					player.setHealth(player.getMaxHealth());
					player.clearStatusEffects();
					echouer(server, duel, "epuise");
					break;
				}
			}
			if (ACTIFS.containsKey(duel.boss) && duel.participants.isEmpty()) {
				terminer(server, duel, false, "abandon");
			}
		}
	}

	private static void entrerPhase(ServerWorld world, Duel duel, LivingEntity boss, int numero, DuelDefinition.Phase phase) {
		for (UUID id : duel.participants) {
			ServerPlayerEntity player = joueur(world.getServer(), duel, id);
			if (player != null && !phase.message().isBlank()) {
				player.sendMessage(Text.literal(phase.message()).formatted(Formatting.LIGHT_PURPLE), false);
			}
		}
		for (DuelDefinition.Renfort r : phase.invocations()) {
			invoquer(world, duel, r, boss.getPos());
		}
		world.playSound(null, boss.getBlockPos(), LimbesSounds.VEILLEUR_PREPARE, SoundCategory.HOSTILE, 1.2F, 0.6F);
		if (boss instanceof PretendantEntity pretendant) {
			pretendant.entrerPhase(numero, phase, duel);
		}
	}

	/** Victoire : appelée à la mort du boss. */
	public static void victoire(LivingEntity boss) {
		Duel duel = ACTIFS.get(boss.getUuid());
		if (duel == null || !(boss.getWorld() instanceof ServerWorld world)) {
			return;
		}
		terminer(world.getServer(), duel, true, "victoire");
	}

	private static void echouer(MinecraftServer server, Duel duel, String raison) {
		ServerWorld world = server.getWorld(duel.monde);
		for (UUID id : new ArrayList<>(duel.participants)) {
			ServerPlayerEntity player = joueur(server, duel, id);
			if (player != null) {
				Actions.executer(player, duel.definition.echec());
				player.sendMessage(Text.translatable("message.les_limbes.duel.echec").formatted(Formatting.RED), false);
				if (world != null && duel.definition.amical() && player.isAlive()) {
					placer(player, world, duel.definition.entree(), duel.definition.yaw());
				}
			}
		}
		terminer(server, duel, false, raison);
	}

	private static void terminer(MinecraftServer server, Duel duel, boolean victoire, String raison) {
		ACTIFS.remove(duel.boss);
		ServerWorld world = server.getWorld(duel.monde);
		if (world != null) {
			duel.blocs.tout(world);
			for (UUID r : duel.renforts) {
				Entity e = world.getEntity(r);
				if (e != null) {
					e.discard();
				}
			}
			if (!victoire) {
				Entity boss = world.getEntity(duel.boss);
				if (boss != null) {
					boss.discard();
				}
			}
			// Projectiles suspendus et soutiens oubliés dans l'arène.
			for (Entity e : world.getEntitiesByClass(Entity.class, new Box(duel.definition.centre()).expand(duel.definition.rayon() + 16),
					x -> x instanceof fr.leslimbes.entity.region.EclatProjectileEntity || x instanceof fr.leslimbes.entity.region.SoutienEntity)) {
				e.discard();
			}
		}
		for (UUID id : new ArrayList<>(duel.participants)) {
			ServerPlayerEntity player = joueur(server, duel, id);
			if (player == null) {
				continue;
			}
			if (victoire) {
				PlayerLimbesData data = PlayerLimbesData.of(player);
				data.poserDrapeau(duel.definition.drapeauVictoire());
				QuestManager.signaler(player, QuestSignal.drapeau(duel.definition.drapeauVictoire()));
				Actions.executer(player, duel.definition.victoire());
				LimbesPortal.titre(player, Text.translatable("message.les_limbes.duel.victoire").formatted(Formatting.GOLD), Text.literal(duel.definition.titre()));
				player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.QUETE_TERMINEE, SoundCategory.PLAYERS, 1.0F, 1.0F);
				PlayerDataSync.envoyer(player);
			}
		}
		duel.barre.clearPlayers();
		duel.barre.setVisible(false);
		LesLimbes.LOGGER.info("Duel « {} » terminé ({}).", duel.definition.titre(), raison);
	}

	/** Le joueur par identifiant : la liste du serveur, sinon la référence gardée (joueur factice des tests). */
	@Nullable
	private static ServerPlayerEntity joueur(MinecraftServer server, Duel duel, UUID id) {
		ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
		if (p != null) {
			return p;
		}
		ServerPlayerEntity garde = duel.joueurs.get(id);
		return garde != null && !garde.isRemoved() ? garde : null;
	}

	private static void retirer(Duel duel, UUID id) {
		duel.participants.remove(id);
		duel.ticksHorsArene.remove(id);
	}

	@Nullable
	public static Duel duelDe(Entity boss) {
		return ACTIFS.get(boss.getUuid());
	}

	@Nullable
	public static Duel duelDe(String definitionId) {
		for (Duel d : ACTIFS.values()) {
			if (d.definition.id().equals(definitionId)) {
				return d;
			}
		}
		return null;
	}

	@Nullable
	public static Duel duelDuParticipant(UUID joueur) {
		for (Duel d : ACTIFS.values()) {
			if (d.participants.contains(joueur)) {
				return d;
			}
		}
		return null;
	}

	public static List<Duel> actifs() {
		return List.copyOf(ACTIFS.values());
	}

	/** Oublie tous les duels (changement de monde, tests). */
	public static void reinitialiser() {
		ACTIFS.clear();
	}

	private DuelManager() {
	}
}
