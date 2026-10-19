package fr.leslimbes.entity.region;

import fr.leslimbes.entity.EntiteAnimee;
import fr.leslimbes.entity.duel.PretendantEntity;
import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesParticles;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * Entité de soutien d'un duel : fleur de vie (soigne le prétendant tant qu'elle vit), point de rupture (tourne
 * autour d'Orsane et tient son armure), bannière astrale (rend les légionnaires proches invulnérables). Elles
 * ne se déplacent pas d'elles-mêmes ; il faut les détruire pour avancer.
 */
public class SoutienEntity extends PathAwareEntity implements EntiteAnimee {
	private Espece espece;

	public SoutienEntity(EntityType<? extends PathAwareEntity> type, World world, Espece espece) {
		super(type, world);
		this.experiencePoints = espece().xp;
		if (espece().flotte) {
			this.setNoGravity(true);
		}
	}

	public Espece espece() {
		if (espece == null) {
			espece = LimbesEspeces.especeDe(this.getType());
		}
		return espece;
	}

	public static DefaultAttributeContainer.Builder creerAttributs(Espece e) {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, e.vie)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
				.add(EntityAttributes.GENERIC_ARMOR, e.armure);
	}

	@Override
	protected void initGoals() {
	}

	@Override
	public void tick() {
		super.tick();
		if (!(this.getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (espece().flotte) {
			this.setNoGravity(true);
			this.setVelocity(Vec3d.ZERO);
		}
		Espece e = espece();
		if (e.soin > 0 && this.age % 40 == 0) {
			PretendantEntity boss = bossProche(world, 14.0);
			if (boss != null && boss.getHealth() < boss.getMaxHealth()) {
				boss.heal(e.soin);
				world.spawnParticles(LimbesParticles.LUEUR, boss.getX(), boss.getY() + 1.5, boss.getZ(), 6, 0.5, 0.5, 0.5, 0.0);
				world.spawnParticles(LimbesParticles.LUEUR, this.getX(), this.getY() + 0.8, this.getZ(), 3, 0.3, 0.3, 0.3, 0.0);
			}
		}
		if (e.rupture) {
			PretendantEntity boss = bossProche(world, 24.0);
			if (boss != null) {
				double a = (this.getId() * 2.1) + this.age * 0.06;
				Vec3d p = boss.getPos().add(Math.cos(a) * 2.4, 1.4 + 0.4 * Math.sin(this.age * 0.1), Math.sin(a) * 2.4);
				this.setPosition(p);
				this.setVelocity(Vec3d.ZERO);
			}
		}
		if (e.banniere && this.age % 20 == 0) {
			world.spawnParticles(LimbesParticles.ETOILE_ROYALE, this.getX(), this.getY() + 1.8, this.getZ(), 2, 0.3, 0.3, 0.3, 0.0);
		}
	}

	@Nullable
	private PretendantEntity bossProche(ServerWorld world, double rayon) {
		List<PretendantEntity> l = world.getEntitiesByClass(PretendantEntity.class, new Box(this.getBlockPos()).expand(rayon), PretendantEntity::isAlive);
		return l.stream().min(Comparator.comparingDouble(this::squaredDistanceTo)).orElse(null);
	}

	/** Une bannière protège-t-elle cette position ? */
	public static boolean protegee(ServerWorld world, Vec3d pos) {
		return !world.getEntitiesByClass(SoutienEntity.class, new Box(pos, pos).expand(10.0), s -> s.isAlive() && s.espece().banniere).isEmpty();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canImmediatelyDespawn(double distanceSquared) {
		return false;
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	protected void dropLoot(DamageSource source, boolean causedByPlayer) {
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return espece().ambiant;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return espece().blesse != null ? espece().blesse : fr.leslimbes.registry.LimbesSounds.CRISTAL_RESONNE;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return espece().mort != null ? espece().mort : fr.leslimbes.registry.LimbesSounds.VEILLEUR_MORT;
	}
}
