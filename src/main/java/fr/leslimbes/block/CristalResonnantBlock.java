package fr.leslimbes.block;

import fr.leslimbes.registry.LimbesSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Cristal résonnant des Montagnes de Verre : lumineux, il chante une note (liée à sa position) quand on marche dessus. */
public class CristalResonnantBlock extends Block {
	public CristalResonnantBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
		if (!world.isClient && entity instanceof LivingEntity && world.getTime() % 10L == 0L) {
			int note = Math.floorMod(pos.getX() * 7 + pos.getZ() * 13 + pos.getY() * 3, 12);
			float hauteur = (float) Math.pow(2.0, (note - 6) / 12.0);
			world.playSound(null, pos, LimbesSounds.CRISTAL_RESONNE, SoundCategory.BLOCKS, 0.5F, hauteur);
		}
		super.onSteppedOn(world, pos, state, entity);
	}
}
