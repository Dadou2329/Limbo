package fr.leslimbes.registry;

import fr.leslimbes.world.dimension.LimbesDimension;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.entity.LocationPredicate;

/** Ajouts aux tables de butin vanilla : la pêche dans les Limbes remonte des poissons-lanternes. */
public final class LimbesLoot {
	public static void init() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && key.equals(LootTables.FISHING_FISH_GAMEPLAY)) {
				tableBuilder.pool(LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1))
						.conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().dimension(LimbesDimension.MONDE)))
						.conditionally(RandomChanceLootCondition.builder(0.6F))
						.with(ItemEntry.builder(LimbesItems.POISSON_LANTERNE)));
			}
		});
	}

	private LimbesLoot() {
	}
}
