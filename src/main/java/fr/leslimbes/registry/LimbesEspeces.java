package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.region.CreatureAquatiqueEntity;
import fr.leslimbes.entity.region.CreaturePaisibleEntity;
import fr.leslimbes.entity.region.CreatureVolanteEntity;
import fr.leslimbes.entity.region.EclatProjectileEntity;
import fr.leslimbes.entity.region.EnnemiAnnonceEntity;
import fr.leslimbes.entity.region.EnnemiTireurEntity;
import fr.leslimbes.entity.region.Espece;
import fr.leslimbes.entity.region.MontureEntity;
import fr.leslimbes.entity.region.SoutienEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Créatures et montures des six régions (étape 5). Chaque espèce est une fiche {@link Espece} servie par la
 * classe de son archétype ; ce registre crée les types d'entités, leurs attributs, leurs règles d'apparition
 * et leurs œufs d'apparition. Les modèles et textures sont produits par {@code tools/models/modeles_regions.py}
 * sous le même identifiant.
 */
public final class LimbesEspeces {
	private static final Map<String, Espece> ESPECES = new LinkedHashMap<>();
	private static final Map<String, EntityType<? extends MobEntity>> TYPES = new LinkedHashMap<>();
	private static final List<Item> OEUFS = new ArrayList<>();
	private static final Map<EntityType<?>, Espece> PAR_TYPE = new java.util.HashMap<>();

	/** Éclat lancé par les ennemis tireurs. */
	public static final EntityType<EclatProjectileEntity> ECLAT_PROJECTILE = Registry.register(Registries.ENTITY_TYPE, LesLimbes.id("eclat_projectile"),
			EntityType.Builder.<EclatProjectileEntity>create(EclatProjectileEntity::new, SpawnGroup.MISC).dimensions(0.25F, 0.25F).maxTrackingRange(4).trackingTickInterval(10).build("eclat_projectile"));

	static {
		// --- Mer suspendue ---
		paisible("crabe_de_nacre").taille(0.7F, 0.45F).vie(8).vitesse(0.22).appat(LimbesItems.POISSON_LANTERNE, 3).sons(LimbesSounds.CRABE_CLIC, null, null).xp(2).oeuf(0xE8D6E4, 0xB98FB4);
		volant("meduse_celeste").taille(0.6F, 0.9F).vie(6).vitesseVol(0.25).sons(LimbesSounds.POISSON_SCINTILLE, null, null).xp(2).oeuf(0x9FD3F0, 0xFFF1B8);
		aquatique("poisson_lanterne").taille(0.5F, 0.4F).vie(4).vitesse(0.7).sons(null, null, null).xp(1).oeuf(0x4F7FB8, 0xFFF3B0);
		ennemi("gardien_des_epaves").taille(0.9F, 2.2F).yeux(1.9F).vie(36).vitesse(0.22).combat(6, 3, 0.5).annonce(20, 26, 3.0, 0.9).xp(10).oeuf(0x5B4A3A, 0x7FE6FF);
		monture("tortue_des_lagons").taille(1.3F, 0.9F).yeux(0.6F).passager(0.95F).vie(40).vitesse(0.2).nage().pas(1.0, 4).appat(LimbesItems.POISSON_LANTERNE, 3).oeuf(0x3F6F7F, 0x7FB58A);
		// --- Jardins des Géants ---
		paisible("geant_des_jardins").taille(2.2F, 4.6F).yeux(4.1F).vie(200).vitesse(0.14).geant().pas(2.0, 10).sons(LimbesSounds.GEANT_PAS, null, null).xp(20).oeuf(0x6B5540, 0x5F9A4E);
		volant("libellule_de_rosee").taille(0.5F, 0.3F).vie(4).vitesseVol(0.6).sons(LimbesSounds.INSECTE_BOURDON, null, null).xp(1).oeuf(0x4FB8A8, 0xDFFFB0);
		paisible("escargot_des_racines").taille(0.6F, 0.6F).vie(10).vitesse(0.08).sansFuite().sons(null, null, null).xp(1).oeuf(0x7A5A3C, 0xA48B6E);
		ennemi("rodeur_des_racines").taille(0.7F, 2.0F).yeux(1.7F).vie(30).vitesse(0.3).combat(5, 1, 0.2).annonce(14, 20, 2.8, 0.7).xp(8).oeuf(0x4A3B2C, 0xFF8A3C);
		monture("scarabee_de_seve").taille(1.2F, 0.9F).yeux(0.6F).passager(0.95F).vie(36).vitesse(0.3).pas(1.0, 6).appat(LimbesItems.FRUIT_DE_GEANT, 3).sons(LimbesSounds.INSECTE_BOURDON, null, null).oeuf(0x4E8A3A, 0xDFF7A0);
		// --- Désert des Heures ---
		paisible("fennec_des_dunes").taille(0.5F, 0.5F).vie(8).vitesse(0.33).appat(LimbesItems.DATTES_DES_HEURES, 3).sons(LimbesSounds.PETIT_CRI, null, null).xp(2).oeuf(0xE9CFA3, 0x2B1E12);
		ennemi("scorpion_des_heures").taille(1.0F, 0.6F).yeux(0.4F).vie(28).vitesse(0.27).combat(4, 2, 0.3).annonce(12, 18, 2.4, 0.5).effet(StatusEffects.POISON, 100).xp(8).oeuf(0xC98B3C, 0x3A2A1A);
		paisible("lezard_cadran").taille(0.8F, 0.4F).vie(8).vitesse(0.26).seChauffe().sons(null, null, null).xp(2).oeuf(0xC9B07E, 0xE07A3C);
		tireur("sentinelle_de_sable").taille(0.8F, 2.0F).yeux(1.7F).vie(32).vitesse(0.22).combat(3, 2, 0.4).tir(16, 50, 14, 3, 0.2).effet(StatusEffects.SLOWNESS, 60).xp(9).oeuf(0xD9C08C, 0x1F2A4D);
		monture("gazelle_d_opale").taille(0.9F, 1.6F).yeux(1.4F).passager(1.35F).vie(30).vitesse(0.36).pas(1.0, 6).appat(LimbesItems.DATTES_DES_HEURES, 3).sons(LimbesSounds.PETIT_CRI, null, null).oeuf(0xE3CDA6, 0xDDE8F0);
		// --- Montagnes de Verre ---
		paisible("bouquetin_de_verre").taille(0.9F, 1.3F).yeux(1.1F).vie(16).vitesse(0.28).pas(1.5, 12).appat(LimbesItems.THE_DES_CIMES, 3).sons(LimbesSounds.PETIT_CRI, null, null).xp(3).oeuf(0xC9D3DE, 0xBFE6F2);
		volant("papillon_de_cristal").taille(0.4F, 0.3F).vie(3).vitesseVol(0.45).sons(null, null, null).xp(1).oeuf(0xC8F0FF, 0x6F7A86);
		ennemi("colosse_de_cristal").taille(1.4F, 3.0F).yeux(2.6F).vie(60).vitesse(0.2).combat(9, 6, 0.9).annonce(24, 30, 3.4, 1.3).pas(1.0, 3).xp(16).oeuf(0x6B7A8A, 0xAEE6F8);
		tireur("echo_resonnant").taille(0.6F, 1.4F).yeux(1.0F).vie(26).vitesse(0.24).combat(2, 0, 0.2).tir(14, 45, 12, 2, 1.1).xp(8).oeuf(0xB8E8F6, 0xDFF6FF);
		monture("griffon_de_verre").taille(1.4F, 1.5F).yeux(1.3F).passager(1.3F).vie(40).vitesse(0.25).vitesseVol(0.6).vol().appat(LimbesItems.THE_DES_CIMES, 3).oeuf(0xD9C7A8, 0xE6F2F8);
		// --- Couronne Immobile ---
		volant("colombe_astrale").taille(0.4F, 0.4F).vie(4).vitesseVol(0.5).sons(LimbesSounds.OISEAU_CHANT, null, null).xp(1).oeuf(0xF2F4FA, 0xFFF3C8);
		paisible("chat_de_palais").taille(0.5F, 0.6F).vie(10).vitesse(0.3).appat(LimbesItems.POISSON_LANTERNE, 3).sons(LimbesSounds.PETIT_CRI, null, null).xp(2).oeuf(0x8E8FA6, 0xC9A227);
		ennemi("sentinelle_de_la_couronne").taille(0.9F, 2.3F).yeux(2.0F).vie(50).vitesse(0.23).combat(8, 8, 0.7).annonce(20, 24, 3.0, 0.9).xp(14).oeuf(0xD8DDE6, 0xC9A227);
		tireur("spectre_de_la_cour").taille(0.7F, 2.0F).yeux(1.7F).vie(28).vitesse(0.26).combat(2, 0, 0.3).tir(18, 55, 14, 3, 0.3).effet(StatusEffects.WEAKNESS, 100).xp(10).oeuf(0xC9D0E8, 0x7FE6FF);
		monture("cerf_royal").taille(1.1F, 1.8F).yeux(1.6F).passager(1.5F).vie(40).vitesse(0.3).pas(1.0, 6).appat(LimbesItems.PAIN_DE_LA_COUR, 3).sons(LimbesSounds.CERF_APPEL, null, null).oeuf(0xF2EBDD, 0xE6C76A);
		// --- Renforts et soutiens des duels (étape 6) ---
		ennemi("chevalier_de_bois_blanc").taille(0.7F, 2.1F).yeux(1.8F).vie(30).vitesse(0.3).combat(5, 4, 0.6).annonce(14, 20, 2.8, 0.8).xp(8).oeuf(0xEDE6D6, 0x5F9A4E);
		ennemi("reflet_de_verre").taille(0.6F, 1.9F).yeux(1.7F).vie(12).vitesse(0.32).combat(3, 0, 0.2).annonce(12, 16, 2.6, 0.4).xp(4).oeuf(0xC8F0FF, 0xFFFFFF);
		ennemi("double_des_heures").taille(0.6F, 1.9F).yeux(1.7F).vie(24).vitesse(0.28).combat(4, 2, 0.4).annonce(16, 20, 2.8, 0.6).xp(6).oeuf(0xD9C08C, 0x3A2A1A);
		ennemi("legionnaire_astral").taille(0.7F, 2.1F).yeux(1.8F).vie(34).vitesse(0.3).combat(6, 6, 0.7).annonce(16, 22, 2.8, 0.9).protegeParBanniere().xp(10).oeuf(0x2E3A6E, 0xC9A227);
		ennemi("champion_des_heures").taille(0.8F, 2.2F).yeux(1.9F).vie(80).vitesse(0.3).combat(8, 6, 0.9).annonce(18, 22, 3.2, 1.0).xp(20).oeuf(0xC98B3C, 0xFFE08A);
		soutien("fleur_de_vie").taille(0.8F, 0.9F).vie(10).soin(3.0F).xp(2).oeuf(0xF2A5C8, 0xDFFFB0);
		soutien("point_de_rupture").taille(0.5F, 0.5F).vie(6).rupture().xp(2).oeuf(0xAEE6F8, 0x6B7A8A);
		soutien("banniere_astrale").taille(0.6F, 2.0F).vie(20).banniere().xp(3).oeuf(0x2E3A6E, 0xFFF3C8);
	}

	private static Espece declarer(Espece e) {
		ESPECES.put(e.id, e);
		return e;
	}

	private static Espece paisible(String id) {
		return declarer(new Espece(id, Espece.Archetype.PAISIBLE));
	}

	private static Espece volant(String id) {
		return declarer(new Espece(id, Espece.Archetype.VOLANT));
	}

	private static Espece aquatique(String id) {
		return declarer(new Espece(id, Espece.Archetype.AQUATIQUE));
	}

	private static Espece ennemi(String id) {
		return declarer(new Espece(id, Espece.Archetype.ENNEMI_ANNONCE));
	}

	private static Espece tireur(String id) {
		return declarer(new Espece(id, Espece.Archetype.ENNEMI_TIREUR));
	}

	private static Espece monture(String id) {
		return declarer(new Espece(id, Espece.Archetype.MONTURE));
	}

	private static Espece soutien(String id) {
		return declarer(new Espece(id, Espece.Archetype.SOUTIEN));
	}

	public static void init() {
		for (Espece e : ESPECES.values()) {
			enregistrer(e);
		}
		LesLimbes.LOGGER.info("{} espèces régionales enregistrées", TYPES.size());
	}

	public static Map<String, Espece> especes() {
		return Collections.unmodifiableMap(ESPECES);
	}

	public static Map<String, EntityType<? extends MobEntity>> types() {
		return Collections.unmodifiableMap(TYPES);
	}

	public static List<Item> oeufs() {
		return Collections.unmodifiableList(OEUFS);
	}

	/** Fiche d'une entité régionale d'après son type (lue dès le constructeur parent, avant l'affectation des champs). */
	public static Espece especeDe(EntityType<?> type) {
		Espece e = PAR_TYPE.get(type);
		if (e == null) {
			throw new IllegalStateException("type d'entité sans fiche d'espèce : " + type);
		}
		return e;
	}

	@Nullable
	public static Espece espece(String id) {
		return ESPECES.get(id);
	}

	public static EntityType<? extends MobEntity> type(String id) {
		EntityType<? extends MobEntity> type = TYPES.get(id);
		if (type == null) {
			throw new IllegalArgumentException("espèce inconnue : " + id);
		}
		return type;
	}

	private static void enregistrer(Espece e) {
		switch (e.archetype) {
			case PAISIBLE -> enregistrer(e, SpawnGroup.CREATURE, (EntityType<CreaturePaisibleEntity> type, net.minecraft.world.World world) -> new CreaturePaisibleEntity(type, world, e),
					CreaturePaisibleEntity.creerAttributs(e), SpawnLocationTypes.ON_GROUND, AnimalEntity::isValidNaturalSpawn);
			case VOLANT -> enregistrer(e, SpawnGroup.AMBIENT, (EntityType<CreatureVolanteEntity> type, net.minecraft.world.World world) -> new CreatureVolanteEntity(type, world, e),
					CreatureVolanteEntity.creerAttributs(e), SpawnLocationTypes.UNRESTRICTED, CreatureVolanteEntity::peutApparaitreEnVol);
			case AQUATIQUE -> enregistrer(e, SpawnGroup.WATER_CREATURE, (EntityType<CreatureAquatiqueEntity> type, net.minecraft.world.World world) -> new CreatureAquatiqueEntity(type, world, e),
					CreatureAquatiqueEntity.creerAttributs(e), SpawnLocationTypes.IN_WATER, WaterCreatureEntity::canSpawn);
			case ENNEMI_ANNONCE -> enregistrer(e, SpawnGroup.MONSTER, (EntityType<EnnemiAnnonceEntity> type, net.minecraft.world.World world) -> new EnnemiAnnonceEntity(type, world, e),
					EnnemiAnnonceEntity.creerAttributs(e), SpawnLocationTypes.ON_GROUND, HostileEntity::canSpawnIgnoreLightLevel);
			case ENNEMI_TIREUR -> enregistrer(e, SpawnGroup.MONSTER, (EntityType<EnnemiTireurEntity> type, net.minecraft.world.World world) -> new EnnemiTireurEntity(type, world, e),
					EnnemiTireurEntity.creerAttributs(e), SpawnLocationTypes.ON_GROUND, HostileEntity::canSpawnIgnoreLightLevel);
			case SOUTIEN -> enregistrer(e, SpawnGroup.MISC, (EntityType<SoutienEntity> type, net.minecraft.world.World world) -> new SoutienEntity(type, world, e),
					SoutienEntity.creerAttributs(e), null, null);
			case MONTURE -> {
				if (e.vol) {
					enregistrer(e, SpawnGroup.CREATURE, (EntityType<MontureEntity> type, net.minecraft.world.World world) -> new MontureEntity(type, world, e),
							MontureEntity.creerAttributs(e), SpawnLocationTypes.UNRESTRICTED, CreatureVolanteEntity::peutApparaitreEnVol);
				} else {
					enregistrer(e, SpawnGroup.CREATURE, (EntityType<MontureEntity> type, net.minecraft.world.World world) -> new MontureEntity(type, world, e),
							MontureEntity.creerAttributs(e), SpawnLocationTypes.ON_GROUND, AnimalEntity::isValidNaturalSpawn);
				}
			}
		}
	}

	private static <T extends MobEntity> void enregistrer(Espece e, SpawnGroup groupe, EntityType.EntityFactory<T> fabrique, DefaultAttributeContainer.Builder attributs,
														  @Nullable SpawnLocation lieu, @Nullable SpawnRestriction.SpawnPredicate<T> predicat) {
		EntityType.Builder<T> builder = EntityType.Builder.create(fabrique, groupe).dimensions(e.largeur, e.hauteur).maxTrackingRange(10);
		if (e.yeux > 0.0F) {
			builder.eyeHeight(e.yeux);
		}
		if (e.attachePassager > 0.0F) {
			builder.passengerAttachments(e.attachePassager);
		}
		EntityType<T> type = Registry.register(Registries.ENTITY_TYPE, LesLimbes.id(e.id), builder.build(e.id));
		PAR_TYPE.put(type, e);
		FabricDefaultAttributeRegistry.register(type, attributs);
		if (lieu != null && predicat != null) {
			SpawnRestriction.register(type, lieu, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, predicat);
		}
		TYPES.put(e.id, type);
		Item oeuf = Registry.register(Registries.ITEM, LesLimbes.id("oeuf_" + e.id), new SpawnEggItem(type, e.couleurOeuf1, e.couleurOeuf2, new Item.Settings()));
		OEUFS.add(oeuf);
		LimbesItems.TOUS.add(oeuf);
	}

	private LimbesEspeces() {
	}
}
