package fr.leslimbes.entity.duel;

/** Manière de combattre d'un prétendant : décide des attaques et de ce que font les phases. */
public enum StyleDuel {
	/** Lance végétale, racines qui se lèvent, fleurs qui soignent, l'arène soulevée par l'arbre (envolées). */
	AELIS,
	/** Portes : passe derrière sa cible, lames spatiales, escales sur les îlots autour du navire. */
	KAEL,
	/** Miroirs : reflets, lames de verre, puis armure colossale aux points de rupture. */
	ORSANE,
	/** Heures : salves d'éclats suspendus puis relâchés, doubles ; vulnérable seulement dans les fenêtres. */
	SAHIR,
	/** Épée solaire, légions astrales protégées par des bannières, forteresse invoquée. */
	VAERON,
	/** Champion d'épreuve (tournoi) : coups annoncés puissants, rien d'autre. */
	CHAMPION
}
