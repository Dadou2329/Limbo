package fr.leslimbes.client;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

/** Copie côté client des données du joueur, uniquement pour l'affichage. */
public final class ClientLimbesData {
	private static PlayerLimbesData donnees = new PlayerLimbesData();
	/** Instant (ms) de la dernière notification de quête, pour l'animation du suivi. */
	private static long derniereNotification;
	private static String texteNotification = "";
	private static String typeNotification = "";

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
	}

	private ClientLimbesData() {
	}
}
