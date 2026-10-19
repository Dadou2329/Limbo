package fr.leslimbes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Colonne porteuse (sève des géants, courant cristallin) : traversable, elle soulève doucement toute
 * entité qui s'y trouve et annule sa chute. Sert d'ascenseur végétal dans les troncs creux et de remontée
 * de cristal dans les montagnes. Calculée des deux côtés (le client simule la poussée), sans paquet.
 */
public class SeveAscendanteBlock extends Block {
	private final double poussee;

	public SeveAscendanteBlock(Settings settings, double poussee) {
		super(settings);
		this.poussee = poussee;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return VoxelShapes.empty();
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		Vec3d v = entity.getVelocity();
		double cible = entity.isSneaking() ? -0.05 : poussee;
		if (entity instanceof LivingEntity living && living.isSneaking()) {
			cible = -0.05;
		}
		entity.setVelocity(v.x * 0.9, Math.max(v.y, cible) == v.y && v.y > cible ? v.y : cible, v.z * 0.9);
		entity.fallDistance = 0.0F;
		if (entity instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
			sp.velocityModified = true;
		}
	}
}
