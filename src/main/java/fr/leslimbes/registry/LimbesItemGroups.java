package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

/** Onglet créatif « Les Limbes ». */
public final class LimbesItemGroups {
	public static final ItemGroup LES_LIMBES = FabricItemGroup.builder()
			.displayName(Text.translatable("itemGroup.les_limbes.les_limbes"))
			.icon(() -> new ItemStack(LimbesItems.FRAGMENT_DE_COURONNE))
			.entries((context, entries) -> {
				LimbesItems.TOUS.forEach(entries::add);
				LimbesItems.OBJETS_DE_BLOC.values().forEach(entries::add);
			})
			.build();

	public static void init() {
		Registry.register(Registries.ITEM_GROUP, LesLimbes.id("les_limbes"), LES_LIMBES);
	}

	private LimbesItemGroups() {
	}
}
