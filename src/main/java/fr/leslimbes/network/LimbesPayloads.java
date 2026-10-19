package fr.leslimbes.network;

import fr.leslimbes.LesLimbes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

/**
 * Paquets du mod. Les paquets client → serveur ne transportent que des intentions (index, slot,
 * identifiant) ; le serveur valide tout avant d'agir.
 */
public final class LimbesPayloads {
	/** C2S : utiliser le pouvoir équipé dans un emplacement. */
	public record UtiliserPouvoir(int slot) implements CustomPayload {
		public static final Id<UtiliserPouvoir> ID = new Id<>(LesLimbes.id("utiliser_pouvoir"));
		public static final PacketCodec<RegistryByteBuf, UtiliserPouvoir> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, UtiliserPouvoir::slot, UtiliserPouvoir::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : utiliser un pouvoir par identifiant (sélection radiale) ; le serveur vérifie qu'il est débloqué. */
	public record UtiliserPouvoirId(String pouvoir) implements CustomPayload {
		public static final Id<UtiliserPouvoirId> ID = new Id<>(LesLimbes.id("utiliser_pouvoir_id"));
		public static final PacketCodec<RegistryByteBuf, UtiliserPouvoirId> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UtiliserPouvoirId::pouvoir, UtiliserPouvoirId::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : équiper un pouvoir débloqué dans un emplacement (chaîne vide = retirer). */
	public record EquiperPouvoir(int slot, String pouvoir) implements CustomPayload {
		public static final Id<EquiperPouvoir> ID = new Id<>(LesLimbes.id("equiper_pouvoir"));
		public static final PacketCodec<RegistryByteBuf, EquiperPouvoir> CODEC = PacketCodec.tuple(
				PacketCodecs.VAR_INT, EquiperPouvoir::slot, PacketCodecs.STRING, EquiperPouvoir::pouvoir, EquiperPouvoir::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : choix dans le dialogue en cours (index du choix). -1 = fermer. */
	public record ChoixDialogue(int index) implements CustomPayload {
		public static final Id<ChoixDialogue> ID = new Id<>(LesLimbes.id("choix_dialogue"));
		public static final PacketCodec<RegistryByteBuf, ChoixDialogue> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, ChoixDialogue::index, ChoixDialogue::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : suivre une quête dans le journal (chaîne vide = ne rien suivre). */
	public record SuivreQuete(String quete) implements CustomPayload {
		public static final Id<SuivreQuete> ID = new Id<>(LesLimbes.id("suivre_quete"));
		public static final PacketCodec<RegistryByteBuf, SuivreQuete> CODEC = PacketCodec.tuple(PacketCodecs.STRING, SuivreQuete::quete, SuivreQuete::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : copie des données du joueur (affichage uniquement). */
	public record SyncJoueur(NbtCompound donnees) implements CustomPayload {
		public static final Id<SyncJoueur> ID = new Id<>(LesLimbes.id("sync_joueur"));
		public static final PacketCodec<RegistryByteBuf, SyncJoueur> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, SyncJoueur::donnees, SyncJoueur::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : ouvrir ou mettre à jour l'écran de dialogue. Contenu : speaker, lignes, choix. */
	public record OuvrirDialogue(NbtCompound contenu) implements CustomPayload {
		public static final Id<OuvrirDialogue> ID = new Id<>(LesLimbes.id("ouvrir_dialogue"));
		public static final PacketCodec<RegistryByteBuf, OuvrirDialogue> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, OuvrirDialogue::contenu, OuvrirDialogue::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : fermer l'écran de dialogue. */
	public record FermerDialogue() implements CustomPayload {
		public static final Id<FermerDialogue> ID = new Id<>(LesLimbes.id("fermer_dialogue"));
		public static final PacketCodec<RegistryByteBuf, FermerDialogue> CODEC = PacketCodec.unit(new FermerDialogue());

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : déclenchement d'un effet visuel/sonore (le client décline particules et sons). */
	public record Effet(String type, Vec3d pos, Vec3d direction, int donnee) implements CustomPayload {
		public static final Id<Effet> ID = new Id<>(LesLimbes.id("effet"));
		public static final PacketCodec<RegistryByteBuf, Effet> CODEC = PacketCodec.tuple(
				PacketCodecs.STRING, Effet::type,
				VEC3D, Effet::pos,
				VEC3D, Effet::direction,
				PacketCodecs.VAR_INT, Effet::donnee,
				Effet::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : notification de quête (étape franchie, quête terminée...). */
	public record NotificationQuete(String type, String quete, String texte) implements CustomPayload {
		public static final Id<NotificationQuete> ID = new Id<>(LesLimbes.id("notification_quete"));
		public static final PacketCodec<RegistryByteBuf, NotificationQuete> CODEC = PacketCodec.tuple(
				PacketCodecs.STRING, NotificationQuete::type,
				PacketCodecs.STRING, NotificationQuete::quete,
				PacketCodecs.STRING, NotificationQuete::texte,
				NotificationQuete::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : écran des projets d'un lieu du domaine (liste, états, coûts, conflits). */
	public record OuvrirChantier(NbtCompound contenu) implements CustomPayload {
		public static final Id<OuvrirChantier> ID = new Id<>(LesLimbes.id("ouvrir_chantier"));
		public static final PacketCodec<RegistryByteBuf, OuvrirChantier> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, OuvrirChantier::contenu, OuvrirChantier::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : aperçu de l'emprise d'un projet (boîte et blocs en conflit), affiché dix secondes. */
	public record ApercuProjet(NbtCompound contenu) implements CustomPayload {
		public static final Id<ApercuProjet> ID = new Id<>(LesLimbes.id("apercu_projet"));
		public static final PacketCodec<RegistryByteBuf, ApercuProjet> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, ApercuProjet::contenu, ApercuProjet::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : écran de la salle des passages (relais connus). */
	public record OuvrirRelais(NbtCompound contenu) implements CustomPayload {
		public static final Id<OuvrirRelais> ID = new Id<>(LesLimbes.id("ouvrir_relais"));
		public static final PacketCodec<RegistryByteBuf, OuvrirRelais> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, OuvrirRelais::contenu, OuvrirRelais::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** S2C : lieux d'intérêt et relais du monde (noms, positions) pour la carte du journal. */
	public record SyncLieux(NbtCompound contenu) implements CustomPayload {
		public static final Id<SyncLieux> ID = new Id<>(LesLimbes.id("sync_lieux"));
		public static final PacketCodec<RegistryByteBuf, SyncLieux> CODEC = PacketCodec.tuple(PacketCodecs.NBT_COMPOUND, SyncLieux::contenu, SyncLieux::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : lancer un projet du chantier ouvert ({@code ignorer} : construire autour des blocs occupés). */
	public record LancerProjet(String projet, boolean ignorer) implements CustomPayload {
		public static final Id<LancerProjet> ID = new Id<>(LesLimbes.id("lancer_projet"));
		public static final PacketCodec<RegistryByteBuf, LancerProjet> CODEC = PacketCodec.tuple(
				PacketCodecs.STRING, LancerProjet::projet, PacketCodecs.BOOL, LancerProjet::ignorer, LancerProjet::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : demander l'aperçu de l'emprise d'un projet. */
	public record DemanderApercu(String projet) implements CustomPayload {
		public static final Id<DemanderApercu> ID = new Id<>(LesLimbes.id("demander_apercu"));
		public static final PacketCodec<RegistryByteBuf, DemanderApercu> CODEC = PacketCodec.tuple(PacketCodecs.STRING, DemanderApercu::projet, DemanderApercu::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	/** C2S : voyager vers un relais connu depuis une porte de relais. */
	public record VoyagerRelais(String relais) implements CustomPayload {
		public static final Id<VoyagerRelais> ID = new Id<>(LesLimbes.id("voyager_relais"));
		public static final PacketCodec<RegistryByteBuf, VoyagerRelais> CODEC = PacketCodec.tuple(PacketCodecs.STRING, VoyagerRelais::relais, VoyagerRelais::new);

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static final PacketCodec<RegistryByteBuf, Vec3d> VEC3D = PacketCodec.tuple(
			PacketCodecs.DOUBLE, Vec3d::getX, PacketCodecs.DOUBLE, Vec3d::getY, PacketCodecs.DOUBLE, Vec3d::getZ, Vec3d::new);

	private LimbesPayloads() {
	}
}
