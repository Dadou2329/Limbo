package fr.leslimbes.duel;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** État d'un duel en cours : boss, participants, phase atteinte, renforts, blocs temporaires, barre de boss. */
public final class Duel {
	public final DuelDefinition definition;
	public final RegistryKey<World> monde;
	public final UUID boss;
	public final Set<UUID> participants = new LinkedHashSet<>();
	/** Références des participants (un joueur factice de test n'est pas dans la liste du serveur). */
	public final Map<UUID, net.minecraft.server.network.ServerPlayerEntity> joueurs = new HashMap<>();
	public final List<UUID> renforts = new ArrayList<>();
	public final Map<UUID, Integer> ticksHorsArene = new HashMap<>();
	public final BlocsTemporaires blocs = new BlocsTemporaires();
	public final ServerBossBar barre;
	public int phase = 0;
	public long debut;

	public Duel(DuelDefinition definition, RegistryKey<World> monde, UUID boss, long debut) {
		this.definition = definition;
		this.monde = monde;
		this.boss = boss;
		this.debut = debut;
		this.barre = new ServerBossBar(Text.literal(definition.titre()), definition.amical() ? BossBar.Color.GREEN : BossBar.Color.RED, BossBar.Style.NOTCHED_6);
	}
}
