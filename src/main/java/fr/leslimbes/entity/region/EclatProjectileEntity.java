package fr.leslimbes.entity.region;

import fr.leslimbes.registry.LimbesEspeces;
import fr.leslimbes.registry.LimbesItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** Éclat lancé par un ennemi tireur : dégâts, effet de statut et recul définis par le tireur ; se brise à l'impact. */
public class EclatProjectileEntity extends ThrownItemEntity {
	private float degats = 2.0F;
	@Nullable
	private RegistryEntry<StatusEffect> effet;
	private int dureeEffet;
	private double recul;

	public EclatProjectileEntity(EntityType<? extends ThrownItemEntity> type, World world) {
		super(type, world);
	}

	public EclatProjectileEntity(World world, LivingEntity tireur, float degats, @Nullable RegistryEntry<StatusEffect> effet, int dureeEffet, double recul) {
		super(LimbesEspeces.ECLAT_PROJECTILE, tireur, world);
		this.degats = degats;
		this.effet = effet;
		this.dureeEffet = dureeEffet;
		this.recul = recul;
	}

	@Override
	protected Item getDefaultItem() {
		return LimbesItems.ECLAT_RESONNANT;
	}

	@Override
	public void handleStatus(byte status) {
		if (status == 3) {
			ParticleEffect p = new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack());
			for (int i = 0; i < 8; i++) {
				this.getWorld().addParticle(p, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult hit) {
		super.onEntityHit(hit);
		Entity touche = hit.getEntity();
		Entity tireur = this.getOwner();
		touche.damage(this.getDamageSources().thrown(this, tireur), degats);
		if (touche instanceof LivingEntity vivant) {
			if (effet != null && dureeEffet > 0) {
				vivant.addStatusEffect(new StatusEffectInstance(effet, dureeEffet), tireur);
			}
			if (recul > 0) {
				Vec3d d = this.getVelocity().normalize();
				vivant.takeKnockback(recul, -d.x, -d.z);
			}
		}
	}

	@Override
	protected void onCollision(HitResult hit) {
		super.onCollision(hit);
		if (!this.getWorld().isClient) {
			this.getWorld().sendEntityStatus(this, (byte) 3);
			this.discard();
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("Degats", degats);
		nbt.putInt("DureeEffet", dureeEffet);
		nbt.putDouble("Recul", recul);
		if (effet != null) {
			Identifier id = Registries.STATUS_EFFECT.getId(effet.value());
			if (id != null) {
				nbt.putString("Effet", id.toString());
			}
		}
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		degats = nbt.contains("Degats") ? nbt.getFloat("Degats") : 2.0F;
		dureeEffet = nbt.getInt("DureeEffet");
		recul = nbt.getDouble("Recul");
		effet = null;
		if (nbt.contains("Effet")) {
			Identifier id = Identifier.tryParse(nbt.getString("Effet"));
			if (id != null) {
				effet = Registries.STATUS_EFFECT.getEntry(id).map(e -> (RegistryEntry<StatusEffect>) e).orElse(null);
			}
		}
	}
}
