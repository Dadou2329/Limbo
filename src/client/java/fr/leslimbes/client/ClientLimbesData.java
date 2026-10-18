package fr.leslimbes.client;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.List;

/** Copie côté client des données du joueur, uniquement pour l'affichage. */
public final class ClientLimbesData {
	private static PlayerLimbesData donnees = new PlayerLimbesData();
	/** Instant (ms) de la dernière notification de quête, pour l'animation du suivi. */
	private static long derniereNotification;
	private static String texteNotification = "";
	private static String typeNotification = "";
	private static List<ProjetResume> projets = List.of();

	/** Projet du domaine tel que le serveur le résume pour le journal. */
	public record ProjetResume(String id, String titre, String lieu, boolean fait) {
	}

	public static List<ProjetResume> projets() {
		return projets;
	}

	/** Lieux, relais et projets reçus du serveur (carte et onglet Domaine du journal). */
	public static void recevoirLieux(NbtCompound nbt) {
		fr.leslimbes.domaine.SyncLieux.recevoir(nbt);
		List<ProjetResume> l = new ArrayList<>();
		net.minecraft.nbt.NbtList liste = nbt.getList("projets", net.minecraft.nbt.NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < liste.size(); i++) {
			NbtCompound p = liste.getCompound(i);
			l.add(new ProjetResume(p.getString("id"), p.getString("titre"), p.getString("lieu"), p.getBoolean("fait")));
		}
		projets = List.copyOf(l);
	}

	public static PlayerLimbesData get() {
		return donnees;
	}

	public static void recevoir(NbtCompound nbt) {
		PlayerLimbesData.CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(LesLimbes.LOGGER::error).ifPresent(d -> donnees = d);
	}

	public static void notifier(String type, String texte) {
		derniereNotification = System.currentTimeMillis();
		texteNotification = texte;
		typeNotification = type;
	}

	public static long derniereNotification() {
		return derniereNotification;
	}

	public static String texteNotification() {
		return texteNotification;
	}

	public static String typeNotification() {
		return typeNotification;
	}

	public static void reinitialiser() {
		donnees = new PlayerLimbesData();
		projets = List.of();
	}

	private ClientLimbesData() {
	}
}
