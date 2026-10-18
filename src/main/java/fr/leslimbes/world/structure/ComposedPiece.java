package fr.leslimbes.world.structure;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.HabitantEntity;
import fr.leslimbes.registry.LimbesEntities;
import fr.leslimbes.registry.LimbesWorldgen;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.SimpleStructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;

/**
 * Pièce d'une structure composée : un gabarit NBT. Les blocs de structure en mode DATA du gabarit
 * portent des métadonnées interprétées ici :
 * <ul>
 *   <li>{@code pnj:<role>} : fait apparaître un habitant avec ce rôle (persistant).</li>
 *   <li>{@code entite:<id>} : fait apparaître une entité quelconque (persistante si créature).</li>
 * </ul>
 */
public class ComposedPiece extends SimpleStructurePiece {
	public ComposedPiece(StructureTemplateManager manager, Identifier gabarit, BlockPos pos, BlockRotation rotation) {
		super(LimbesWorldgen.PIECE_COMPOSEE, 0, manager, gabarit, gabarit.toString(), placement(rotation), pos);
	}

	public ComposedPiece(StructureTemplateManager manager, NbtCompound nbt) {
		super(LimbesWorldgen.PIECE_COMPOSEE, nbt, manager, id -> placement(BlockRotation.valueOf(nbt.getString("Rot"))));
	}

	private static StructurePlacementData placement(BlockRotation rotation) {
		return new StructurePlacementData().setRotation(rotation).setMirror(BlockMirror.NONE)
				.addProcessor(BlockIgnoreStructureProcessor.IGNORE_STRUCTURE_BLOCKS).setIgnoreEntities(false);
	}

	@Override
	protected void writeNbt(net.minecraft.structure.StructureContext context, NbtCompound nbt) {
		super.writeNbt(context, nbt);
		nbt.putString("Rot", this.placementData.getRotation().name());
	}

	@Override
	protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {
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
			net.minecraft.registry.Registries.ENTITY_TYPE.getOrEmpty(id).ifPresentOrElse(type -> {
				net.minecraft.entity.Entity e = type.create(world.toServerWorld());
				if (e == null) {
					return;
				}
				e.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0F, 0.0F);
				if (e instanceof net.minecraft.entity.mob.MobEntity mob) {
					mob.setPersistent();
					mob.initialize(world, world.getLocalDifficulty(pos), SpawnReason.STRUCTURE, null);
				}
				world.spawnEntityAndPassengers(e);
			}, () -> LesLimbes.LOGGER.warn("Métadonnée de structure : entité inconnue {}", id));
		}
	}
}
