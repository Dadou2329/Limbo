package fr.leslimbes.arrivee;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

/** Une Arrivée concrète dans le monde : type, site, position, état. Persistée dans l'état du monde. */
public final class ArriveeEvenement {
	public enum Etat {
		ACTIVE, RESOLUE, EXPIREE
	}

	public final String instance;
	public final String definition;
	public final String site;
	public final BlockPos pos;
	public final long debut;
	public Etat etat;
	public long fin;

	public ArriveeEvenement(String instance, String definition, String site, BlockPos pos, long debut) {
		this.instance = instance;
		this.definition = definition;
		this.site = site;
		this.pos = pos.toImmutable();
		this.debut = debut;
		this.etat = Etat.ACTIVE;
	}

	public boolean active() {
		return etat == Etat.ACTIVE;
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("instance", instance);
		nbt.putString("definition", definition);
		nbt.putString("site", site);
		nbt.put("pos", NbtHelper.fromBlockPos(pos));
		nbt.putLong("debut", debut);
		nbt.putString("etat", etat.name());
		nbt.putLong("fin", fin);
		return nbt;
	}

	public static ArriveeEvenement fromNbt(NbtCompound nbt) {
		ArriveeEvenement e = new ArriveeEvenement(nbt.getString("instance"), nbt.getString("definition"), nbt.getString("site"),
				NbtHelper.toBlockPos(nbt, "pos").orElse(BlockPos.ORIGIN), nbt.getLong("debut"));
		try {
			e.etat = Etat.valueOf(nbt.getString("etat"));
		} catch (IllegalArgumentException ex) {
			e.etat = Etat.ACTIVE;
		}
		e.fin = nbt.getLong("fin");
		return e;
	}
}
