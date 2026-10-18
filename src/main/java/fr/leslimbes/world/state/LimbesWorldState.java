package fr.leslimbes.world.state;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.arrivee.ArriveeEvenement;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * État partagé du monde (toutes dimensions) : drapeaux globaux, ancrages des personnages essentiels
 * et archivage des événements. Versionné pour permettre des migrations futures.
 */
public final class LimbesWorldState extends PersistentState {
	public static final int VERSION = 1;
	private static final String ID = LesLimbes.MOD_ID + "_monde";
	private static final Type<LimbesWorldState> TYPE = new Type<>(LimbesWorldState::new, LimbesWorldState::fromNbt, DataFixTypes.LEVEL);

	private final Set<String> drapeaux = new HashSet<>();
	/** Rôle d'un personnage essentiel -> position d'ancrage (dans les Limbes). */
	private final Map<String, BlockPos> ancragesPnj = new HashMap<>();
	private final Map<String, Long> derniereReapparition = new HashMap<>();
	/** Arrivées (actives et archivées), numéro d'instance suivant, dernier déclenchement et types déjà survenus. */
	private final List<ArriveeEvenement> arrivees = new ArrayList<>();
	private int numeroArrivee;
	private long derniereArrivee;
	private final Map<String, Long> arriveesDeclenchees = new HashMap<>();

	public static LimbesWorldState get(MinecraftServer server) {
		ServerWorld overworld = server.getWorld(World.OVERWORLD);
		return overworld.getPersistentStateManager().getOrCreate(TYPE, ID);
	}

	public boolean aDrapeau(String d) {
		return drapeaux.contains(d);
	}

	public void poserDrapeau(String d) {
		if (drapeaux.add(d)) {
			markDirty();
		}
	}

	public void ancrerPnj(String role, BlockPos pos) {
		ancragesPnj.put(role, pos.toImmutable());
		markDirty();
	}

	public Optional<BlockPos> ancragePnj(String role) {
		return Optional.ofNullable(ancragesPnj.get(role));
	}

	public Map<String, BlockPos> ancrages() {
		return Map.copyOf(ancragesPnj);
	}

	public long derniereReapparition(String role) {
		return derniereReapparition.getOrDefault(role, 0L);
	}

	public void noterReapparition(String role, long temps) {
		derniereReapparition.put(role, temps);
		markDirty();
	}

	// --- Arrivées ------------------------------------------------------------------------------
	public List<ArriveeEvenement> arriveesActives() {
		List<ArriveeEvenement> actives = new ArrayList<>();
		for (ArriveeEvenement e : arrivees) {
			if (e.active()) {
				actives.add(e);
			}
		}
		return actives;
	}

	public List<ArriveeEvenement> toutesArrivees() {
		return List.copyOf(arrivees);
	}

	public void ajouterArrivee(ArriveeEvenement e) {
		arrivees.add(e);
		markDirty();
	}

	public int prochainNumeroArrivee() {
		numeroArrivee++;
		markDirty();
		return numeroArrivee;
	}

	public long derniereArrivee() {
		return derniereArrivee;
	}

	public void noterArrivee(String definition, long temps) {
		derniereArrivee = temps;
		arriveesDeclenchees.put(definition, temps);
		markDirty();
	}

	public boolean arriveeDejaDeclenchee(String definition) {
		return arriveesDeclenchees.containsKey(definition);
	}

	public boolean siteOccupe(String siteId) {
		for (ArriveeEvenement e : arrivees) {
			if (e.active() && e.site.equals(siteId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		nbt.putInt("version", VERSION);
		NbtList flags = new NbtList();
		for (String d : drapeaux) {
			flags.add(net.minecraft.nbt.NbtString.of(d));
		}
		nbt.put("drapeaux", flags);
		NbtCompound anc = new NbtCompound();
		ancragesPnj.forEach((role, pos) -> anc.put(role, NbtHelper.fromBlockPos(pos)));
		nbt.put("ancrages_pnj", anc);
		NbtCompound reapp = new NbtCompound();
		derniereReapparition.forEach(reapp::putLong);
		nbt.put("reapparitions", reapp);
		NbtList arr = new NbtList();
		for (ArriveeEvenement e : arrivees) {
			arr.add(e.toNbt());
		}
		nbt.put("arrivees", arr);
		nbt.putInt("numero_arrivee", numeroArrivee);
		nbt.putLong("derniere_arrivee", derniereArrivee);
		NbtCompound decl = new NbtCompound();
		arriveesDeclenchees.forEach(decl::putLong);
		nbt.put("arrivees_declenchees", decl);
		return nbt;
	}

	public static LimbesWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		LimbesWorldState state = new LimbesWorldState();
		NbtList flags = nbt.getList("drapeaux", NbtElement.STRING_TYPE);
		for (int i = 0; i < flags.size(); i++) {
			state.drapeaux.add(flags.getString(i));
		}
		NbtCompound anc = nbt.getCompound("ancrages_pnj");
		for (String role : anc.getKeys()) {
			NbtHelper.toBlockPos(anc, role).ifPresent(pos -> state.ancragesPnj.put(role, pos));
		}
		NbtCompound reapp = nbt.getCompound("reapparitions");
		for (String role : reapp.getKeys()) {
			state.derniereReapparition.put(role, reapp.getLong(role));
		}
		NbtList arr = nbt.getList("arrivees", NbtElement.COMPOUND_TYPE);
		for (int i = 0; i < arr.size(); i++) {
			state.arrivees.add(ArriveeEvenement.fromNbt(arr.getCompound(i)));
		}
		state.numeroArrivee = nbt.getInt("numero_arrivee");
		state.derniereArrivee = nbt.getLong("derniere_arrivee");
		NbtCompound decl = nbt.getCompound("arrivees_declenchees");
		for (String id : decl.getKeys()) {
			state.arriveesDeclenchees.put(id, decl.getLong(id));
		}
		return state;
	}
}
