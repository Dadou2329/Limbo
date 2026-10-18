package fr.leslimbes.power;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Une technique royale. L'exécution est strictement serveur ; le client ne reçoit que des effets.
 *
 * @param id        identifiant court (clé de langue : pouvoir.les_limbes.&lt;id&gt;)
 * @param branche   branche de la magie royale (arsenal, portes, decrets, garde, main, pactes)
 * @param cout      majesté consommée
 * @param recharge  ticks avant réutilisation
 */
public record Ability(String id, String branche, float cout, int recharge, Execution execution) {
	@FunctionalInterface
	public interface Execution {
		/** @return true si la technique a bien été exécutée (le coût est alors prélevé). */
		boolean executer(ServerPlayerEntity player);
	}
}
