package fr.leslimbes.world.structure;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.registry.LimbesEntities;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

/**
 * Interprétation des blocs de structure en mode DATA des gabarits :
 * <ul>
 *   <li>{@code pnj:<role>} : fait apparaître un habitant avec ce rôle (persistant, domicile = position) ;</li>
 *   <li>{@code entite:<id>} : fait apparaître une entité quelconque (persistante si créature).</li>
 * </ul>
 * Utilisée par les pièces de structures composées (génération) et par les Arrivées (placement en jeu).
 */
public final class MarqueursDeStructure {
	public static void appliquer(ServerWorldAccess world, String metadata, BlockPos pos, Random random) {
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
		if (metadata.startsWith("pnj:")) {
			String role = metadata.substring(4).trim();
			HabitantEntity pnj = LimbesEntities.HABITANT.create(world.toServerWorld());
			if (pnj == null) {
				return;
			}
			pnj.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
			pnj.setRole(role);
			pnj.setDomicile(pos);
			pnj.setPersistent();
			pnj.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
			world.spawnEntityAndPassengers(pnj);
		} else if (metadata.startsWith("entite:")) {
			Identifier id = Identifier.tryParse(metadata.substring(7).trim());
			if (id == null) {
				return;
			}
			Registries.ENTITY_TYPE.getOrEmpty(id).ifPresentOrElse(type -> {
				Entity e = type.create(world.toServerWorld());
				if (e == null) {
					return;
				}
				e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
				if (e instanceof MobEntity mob) {
					mob.setPersistent();
					mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
				}
				world.spawnEntityAndPassengers(e);
			}, () -> LesLimbes.LOGGER.warn("Métadonnée de structure : entité inconnue {}", id));
		}
	}

	/** Applique tous les marqueurs DATA d'un gabarit déjà placé à {@code origine} avec {@code placement}. */
	public static int appliquerTous(ServerWorldAccess world, StructureTemplate template, StructurePlacementData placement, BlockPos origine, Random random) {
		int n = 0;
		for (StructureTemplate.StructureBlockInfo info : template.getInfosForBlock(origine, placement, Blocks.STRUCTURE_BLOCK)) {
			if (info.nbt() != null && "DATA".equals(info.nbt().getString("mode"))) {
				appliquer(world, info.nbt().getString("metadata"), info.pos(), random);
				n++;
			}
		}
		return n;
	}

	private MarqueursDeStructure() {
	}
}
