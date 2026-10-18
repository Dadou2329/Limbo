package fr.leslimbes.domaine;

import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

/** Envoi des lieux d'intérêt et des relais au client (carte du journal sur serveur dédié). */
public final class SyncLieux {
	public static void envoyer(ServerPlayerEntity player) {
		if (!ServerPlayNetworking.canSend(player, LimbesPayloads.SyncLieux.ID)) {
			return;
		}
		ServerPlayNetworking.send(player, new LimbesPayloads.SyncLieux(encoder(player.server)));
	}

	/** Après un changement d'état partagé (projet construit), tous les joueurs reçoivent la mise à jour. */
	public static void envoyerATous(net.minecraft.server.MinecraftServer server) {
		NbtCompound nbt = encoder(server);
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			if (ServerPlayNetworking.canSend(p, LimbesPayloads.SyncLieux.ID)) {
				ServerPlayNetworking.send(p, new LimbesPayloads.SyncLieux(nbt));
			}
		}
	}

	public static NbtCompound encoder(net.minecraft.server.MinecraftServer server) {
		fr.leslimbes.world.state.LimbesWorldState etat = fr.leslimbes.world.state.LimbesWorldState.get(server);
		NbtCompound nbt = new NbtCompound();
		NbtList projets = new NbtList();
		for (ProjetDefinition p : DomaineLoader.projets().values()) {
			NbtCompound c = new NbtCompound();
			c.putString("id", p.id());
			c.putString("titre", p.titre());
			c.putString("lieu", p.lieu());
			c.putBoolean("fait", etat.aDrapeau(p.drapeauMonde()));
			projets.add(c);
		}
		nbt.put("projets", projets);
		NbtList lieux = new NbtList();
		for (LieuDefinition l : DomaineLoader.lieux().values()) {
			NbtCompound c = new NbtCompound();
			c.putString("id", l.id());
			c.putString("nom", l.nom());
			c.putString("description", l.description());
			c.putIntArray("pos", new int[] {l.pos().getX(), l.pos().getY(), l.pos().getZ()});
			c.putInt("rayon", l.rayon());
			c.putString("region", l.region());
			c.putString("dimension", l.dimension());
			c.putString("relais", l.relais());
			lieux.add(c);
		}
		nbt.put("lieux", lieux);
		NbtList relais = new NbtList();
		for (RelaisDefinition r : DomaineLoader.relais().values()) {
			NbtCompound c = new NbtCompound();
			c.putString("id", r.id());
			c.putString("nom", r.nom());
			c.putString("description", r.description());
			c.putIntArray("pos", new int[] {r.pos().getX(), r.pos().getY(), r.pos().getZ()});
			c.putString("dimension", r.dimension());
			c.putInt("ordre", r.ordre());
			relais.add(c);
		}
		nbt.put("relais", relais);
		return nbt;
	}

	/** Décodage côté client : remplace les définitions locales par celles du serveur. */
	public static void recevoir(NbtCompound nbt) {
		Map<String, LieuDefinition> lieux = new LinkedHashMap<>();
		NbtList l = nbt.getList("lieux", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < l.size(); i++) {
			NbtCompound c = l.getCompound(i);
			int[] p = c.getIntArray("pos");
			lieux.put(c.getString("id"), new LieuDefinition(c.getString("id"), c.getString("nom"), c.getString("description"),
					new BlockPos(p[0], p[1], p[2]), c.getInt("rayon"), c.getString("region"), c.getString("dimension"), c.getString("relais")));
		}
		Map<String, RelaisDefinition> relais = new LinkedHashMap<>();
		NbtList r = nbt.getList("relais", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < r.size(); i++) {
			NbtCompound c = r.getCompound(i);
			int[] p = c.getIntArray("pos");
			relais.put(c.getString("id"), new RelaisDefinition(c.getString("id"), c.getString("nom"), c.getString("description"),
					new BlockPos(p[0], p[1], p[2]), c.getString("dimension"), c.getInt("ordre")));
		}
		DomaineLoader.recevoir(lieux, relais);
	}

	private SyncLieux() {
	}
}
