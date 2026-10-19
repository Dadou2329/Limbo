package fr.leslimbes.capitale;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.block.LanterneRoyaleEteinteBlock;
import fr.leslimbes.block.PontRoyalBlock;
import fr.leslimbes.block.PorteRoyaleBlock;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.dimension.LimbesDimension;
import fr.leslimbes.world.dimension.LimbesSites;
import fr.leslimbes.world.state.LimbesWorldState;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * Éveil de la capitale endormie, par niveaux partagés par le monde :
 * <ol>
 *   <li>quartiers extérieurs : les portes royales de niveau 1 s'ouvrent (acte II) ;</li>
 *   <li>derniers accès : les portes du palais s'ouvrent (acte IV) ;</li>
 *   <li>couronnement : les lanternes royales s'allument et les ponts se reforment.</li>
 * </ol>
 * Les blocs de la capitale lisent eux-mêmes le niveau au fil de leurs ticks aléatoires (la ville s'éveille en
 * une minute environ, de proche en proche) ; cette classe applique en plus le niveau tout de suite autour des
 * joueurs présents pour un retour immédiat.
 */
public final class CapitaleManager {
	public static final int RAYON = 120;

	public static int niveau(MinecraftServer server) {
		LimbesWorldState etat = LimbesWorldState.get(server);
		for (int n = 3; n >= 1; n--) {
			if (etat.aDrapeau("capitale_niveau_" + n)) {
				return n;
			}
		}
		return 0;
	}

	public static void eveiller(MinecraftServer server, int niveau, boolean annoncer) {
		LimbesWorldState etat = LimbesWorldState.get(server);
		for (int n = 1; n <= niveau; n++) {
			etat.poserDrapeau("capitale_niveau_" + n);
		}
		ServerWorld limbes = server.getWorld(LimbesDimension.MONDE);
		if (limbes == null) {
			return;
		}
		int appliques = appliquerAutourDesJoueurs(limbes, niveau);
		if (annoncer) {
			Text message = Text.translatable("message.les_limbes.capitale.niveau_" + Math.min(3, Math.max(1, niveau))).formatted(Formatting.GOLD);
			for (ServerPlayerEntity p : limbes.getPlayers()) {
				p.sendMessage(message, false);
			}
			limbes.playSound(null, LimbesSites.PLACE_DES_CEREMONIES, LimbesSounds.PORTE_EVEIL, SoundCategory.BLOCKS, 2.0F, 0.8F);
		}
		LesLimbes.LOGGER.info("Capitale : niveau d'éveil {} ({} blocs mis à jour immédiatement).", niveau, appliques);
	}

	/** Applique le niveau aux chunks de la capitale déjà chargés (les autres suivront par ticks aléatoires). */
	private static int appliquerAutourDesJoueurs(ServerWorld world, int niveau) {
		int n = 0;
		BlockPos centre = LimbesSites.PLACE_DES_CEREMONIES;
		ChunkPos min = new ChunkPos(centre.add(-RAYON, 0, -RAYON));
		ChunkPos max = new ChunkPos(centre.add(RAYON, 0, RAYON));
		for (int cx = min.x; cx <= max.x; cx++) {
			for (int cz = min.z; cz <= max.z; cz++) {
				if (!world.isChunkLoaded(cx, cz)) {
					continue;
				}
				net.minecraft.world.chunk.WorldChunk chunk = world.getChunk(cx, cz);
				for (BlockPos p : BlockPos.iterate(cx << 4, 70, cz << 4, (cx << 4) + 15, 140, (cz << 4) + 15)) {
					BlockState s = chunk.getBlockState(p);
					if (s.isAir()) {
						continue;
					}
					if (appliquer(world, p, s, niveau)) {
						n++;
					}
				}
			}
		}
		return n;
	}

	/** Met un bloc de la capitale au niveau d'éveil donné. @return true si le bloc a changé. */
	public static boolean appliquer(ServerWorld world, BlockPos pos, BlockState s, int niveau) {
		if (s.getBlock() instanceof PorteRoyaleBlock) {
			if (!s.get(PorteRoyaleBlock.OUVERTE) && s.get(PorteRoyaleBlock.NIVEAU) <= niveau) {
				world.setBlockState(pos, s.with(PorteRoyaleBlock.OUVERTE, true), 3);
				return true;
			}
		} else if (s.getBlock() instanceof LanterneRoyaleEteinteBlock) {
			if (niveau >= 3) {
				world.setBlockState(pos, LanterneRoyaleEteinteBlock.allumee(s), 3);
				return true;
			}
		} else if (s.getBlock() instanceof PontRoyalBlock) {
			if (niveau >= 3 && !s.get(PontRoyalBlock.FORME)) {
				world.setBlockState(pos, s.with(PontRoyalBlock.FORME, true), 3);
				return true;
			}
		}
		return false;
	}

	private CapitaleManager() {
	}
}
