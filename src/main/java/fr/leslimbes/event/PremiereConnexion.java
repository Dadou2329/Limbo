package fr.leslimbes.event;

import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/** Première connexion : le joueur reçoit le Carnet du voyageur et la quête « Le Passage ». */
public final class PremiereConnexion {
	public static void accueillir(ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (data.carnetRecu) {
			return;
		}
		data.carnetRecu = true;
		ItemStack carnet = carnetDuVoyageur();
		if (!player.giveItemStack(carnet)) {
			player.dropItem(carnet, false);
		}
		player.sendMessage(Text.translatable("message.les_limbes.carnet.recu").formatted(Formatting.AQUA), false);
		player.getServerWorld().playSound(null, player.getBlockPos(), LimbesSounds.FRAGMENT, SoundCategory.PLAYERS, 0.8F, 1.0F);
		QuestManager.demarrer(player, "le_passage");
		PlayerDataSync.envoyer(player);
	}

	/** Livre écrit en français : indices pour trouver un vestige, sans révéler la suite. */
	public static ItemStack carnetDuVoyageur() {
		ItemStack livre = new ItemStack(Items.WRITTEN_BOOK);
		List<RawFilteredPair<Text>> pages = List.of(
				RawFilteredPair.of(Text.literal("Carnet d'un voyageur\n\nJ'ai trouvé une pierre sombre dans une ruine, gravée d'une porte. Elle semblait attendre quelque chose.\n\nLes anciens parlent de « vestiges » : de petits sanctuaires oubliés, posés dans les prairies et les forêts.")),
				RawFilteredPair.of(Text.literal("La nacre.\n\nDans la ruine, j'ai ramassé des éclats de nacre. Quand je les approchais de la pierre, le glyphe s'éclairait un peu.\n\nIl en faudrait trois, je crois. Trois éclats pour réveiller la porte.")),
				RawFilteredPair.of(Text.literal("Pour retrouver un vestige :\n\nune boussole ordinaire et quatre éclats d'améthyste, assemblés sur une table d'artisanat, donnent une boussole nacrée.\n\nTenue en main, elle murmure la direction du vestige le plus proche.")),
				RawFilteredPair.of(Text.literal("Si vous lisez ceci, allez-y.\n\nCe qui disparaît ailleurs ne disparaît pas vraiment. Il y a un endroit où tout cela finit par arriver.\n\nOn l'appelle les Limbes."))
		);
		livre.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(
				RawFilteredPair.of("Carnet du voyageur"), "Un voyageur inconnu", 0, pages, true));
		return livre;
	}

	private PremiereConnexion() {
	}
}
