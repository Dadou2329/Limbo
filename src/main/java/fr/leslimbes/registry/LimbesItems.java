package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.item.BoussoleNacreeItem;
import fr.leslimbes.item.EpeeDuSouverainItem;
import fr.leslimbes.item.FragmentDeCouronneItem;
import fr.leslimbes.item.SiffletDeRaieItem;
import net.minecraft.block.Block;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Rarity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Objets des Limbes (hors objets de bloc, créés automatiquement pour chaque bloc). */
public final class LimbesItems {
	public static final List<Item> TOUS = new ArrayList<>();
	public static final Map<Block, BlockItem> OBJETS_DE_BLOC = new LinkedHashMap<>();

	public static final Item FRAGMENT_DE_NACRE = register("fragment_de_nacre", new Item(new Item.Settings().rarity(Rarity.UNCOMMON)));
	public static final Item BOUSSOLE_NACREE = register("boussole_nacree", new BoussoleNacreeItem(new Item.Settings().maxCount(1)));
	public static final Item FIBRES_NACREES = register("fibres_nacrees", new Item(new Item.Settings()));
	public static final Item HERBE_DE_L_AUBE = register("herbe_de_l_aube", new Item(new Item.Settings()));
	public static final Item BOUILLON_DE_L_AUBE = register("bouillon_de_l_aube", new Item(new Item.Settings().maxCount(1)
			.food(new FoodComponent.Builder().nutrition(7).saturationModifier(0.7F)
					.statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0), 1.0F)
					.usingConvertsTo(Items.BOWL).build())));
	public static final Item FRAGMENT_DE_COURONNE = register("fragment_de_couronne", new FragmentDeCouronneItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));
	public static final Item EPEE_DU_SOUVERAIN = register("epee_du_souverain", new EpeeDuSouverainItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));
	public static final Item CLE_DU_MANOIR = register("cle_du_manoir", new Item(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
	public static final Item SIFFLET_DE_RAIE = register("sifflet_de_raie", new SiffletDeRaieItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
	public static final Item FARINE = register("farine", new Item(new Item.Settings()));
	public static final Item PAIN_D_EMILE = register("pain_d_emile", new Item(new Item.Settings()
			.food(new FoodComponent.Builder().nutrition(6).saturationModifier(0.8F)
					.statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 600, 0), 1.0F).build())));
	public static final Item JOURNAL_DE_BORD = register("journal_de_bord", new Item(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON)));
	public static final Item RAGOUT_DU_DOMAINE = register("ragout_du_domaine", new Item(new Item.Settings().maxCount(1)
			.food(new FoodComponent.Builder().nutrition(9).saturationModifier(0.9F)
					.statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 1200, 0), 1.0F)
					.usingConvertsTo(Items.BOWL).build())));

	public static final Item OEUF_HABITANT = register("oeuf_habitant", new SpawnEggItem(LimbesEntities.HABITANT, 0xE3C9AF, 0x22305E, new Item.Settings()));
	public static final Item OEUF_POISSON_LUNE = register("oeuf_poisson_lune", new SpawnEggItem(LimbesEntities.POISSON_LUNE, 0xFFF8E2, 0x3C57A0, new Item.Settings()));
	public static final Item OEUF_CERF_LANTERNE = register("oeuf_cerf_lanterne", new SpawnEggItem(LimbesEntities.CERF_LANTERNE, 0xB99C86, 0xE3C97E, new Item.Settings()));
	public static final Item OEUF_VEILLEUR_EGARE = register("oeuf_veilleur_egare", new SpawnEggItem(LimbesEntities.VEILLEUR_EGARE, 0x4A4858, 0xA9D2EC, new Item.Settings()));
	public static final Item OEUF_RAIE_CELESTE = register("oeuf_raie_celeste", new SpawnEggItem(LimbesEntities.RAIE_CELESTE, 0x8FB5DE, 0xFFF3C8, new Item.Settings()));

	private static <I extends Item> I register(String path, I item) {
		Registry.register(Registries.ITEM, LesLimbes.id(path), item);
		TOUS.add(item);
		return item;
	}

	public static void init() {
		for (Block block : LimbesBlocks.TOUS) {
			BlockItem item = new BlockItem(block, new Item.Settings());
			Registry.register(Registries.ITEM, Registries.BLOCK.getId(block), item);
			OBJETS_DE_BLOC.put(block, item);
		}
	}

	private LimbesItems() {
	}
}
