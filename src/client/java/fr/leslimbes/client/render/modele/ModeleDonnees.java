package fr.leslimbes.client.render.modele;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modèle d'entité décrit en JSON (assets/les_limbes/models/entite/&lt;id&gt;.json, produit par
 * tools/models/gen_models.py) : os hiérarchiques, cubes dépliés en UV Minecraft et animations
 * paramétriques. La texture est peinte par le même outil, cube par cube.
 */
public record ModeleDonnees(int largeurTexture, int hauteurTexture, float echelle, float ombre, boolean lueur,
							List<Os> os, List<Animation> animations) {

	public record Cube(float ox, float oy, float oz, int sx, int sy, int sz, int u, int v, float gonflement, boolean miroir) {
	}

	public record Os(String nom, String parent, float px, float py, float pz, float rx, float ry, float rz, List<Cube> cubes) {
	}

	/**
	 * @param os        os animé
	 * @param propriete rx, ry, rz (radians), x, y, z (pixels), echelle
	 * @param pilote    marche, marche_abs, temps, temps_cos, vol, regard_yaw, regard_pitch, attaque, etat, etat_temps,
	 *                  vitesse, vitesse_verticale, vie, monte, constante, bebe
	 */
	public record Animation(String os, String propriete, String pilote, float amplitude, float frequence, float phase, int valeur, float base) {
	}

	public static ModeleDonnees vide() {
		return new ModeleDonnees(16, 16, 1.0F, 0.5F, false, List.of(new Os("racine", "", 0, 24, 0, 0, 0, 0,
				List.of(new Cube(-4, -8, -4, 8, 8, 8, 0, 0, 0, false)))), List.of());
	}

	public static ModeleDonnees parse(JsonObject json) {
		JsonArray tex = json.getAsJsonArray("texture");
		int tw = tex == null ? 64 : tex.get(0).getAsInt();
		int th = tex == null ? 64 : tex.get(1).getAsInt();
		float echelle = json.has("echelle") ? json.get("echelle").getAsFloat() : 1.0F;
		float ombre = json.has("ombre") ? json.get("ombre").getAsFloat() : 0.5F;
		boolean lueur = json.has("lueur") && json.get("lueur").getAsBoolean();
		List<Os> os = new ArrayList<>();
		for (JsonElement e : json.getAsJsonArray("os")) {
			JsonObject o = e.getAsJsonObject();
			float[] p = floats(o.getAsJsonArray("pivot"), 3);
			float[] r = o.has("rotation") ? floats(o.getAsJsonArray("rotation"), 3) : new float[3];
			List<Cube> cubes = new ArrayList<>();
			if (o.has("cubes")) {
				for (JsonElement ce : o.getAsJsonArray("cubes")) {
					JsonObject c = ce.getAsJsonObject();
					float[] origine = floats(c.getAsJsonArray("origine"), 3);
					float[] taille = floats(c.getAsJsonArray("taille"), 3);
					float[] uv = floats(c.getAsJsonArray("uv"), 2);
					cubes.add(new Cube(origine[0], origine[1], origine[2], Math.round(taille[0]), Math.round(taille[1]), Math.round(taille[2]),
							Math.round(uv[0]), Math.round(uv[1]), c.has("gonflement") ? c.get("gonflement").getAsFloat() : 0.0F,
							c.has("miroir") && c.get("miroir").getAsBoolean()));
				}
			}
			os.add(new Os(o.get("nom").getAsString(), o.has("parent") ? o.get("parent").getAsString() : "", p[0], p[1], p[2], r[0], r[1], r[2], List.copyOf(cubes)));
		}
		List<Animation> animations = new ArrayList<>();
		if (json.has("animations")) {
			for (JsonElement e : json.getAsJsonArray("animations")) {
				JsonObject a = e.getAsJsonObject();
				animations.add(new Animation(a.get("os").getAsString(), a.get("propriete").getAsString(), a.get("pilote").getAsString(),
						a.get("amplitude").getAsFloat(), a.has("frequence") ? a.get("frequence").getAsFloat() : 1.0F,
						a.has("phase") ? a.get("phase").getAsFloat() : 0.0F, a.has("valeur") ? a.get("valeur").getAsInt() : 0,
						a.has("base") ? a.get("base").getAsFloat() : 0.0F));
			}
		}
		return new ModeleDonnees(tw, th, echelle, ombre, lueur, List.copyOf(os), List.copyOf(animations));
	}

	private static float[] floats(JsonArray array, int n) {
		float[] out = new float[n];
		for (int i = 0; i < n && array != null && i < array.size(); i++) {
			out[i] = array.get(i).getAsFloat();
		}
		return out;
	}

	/** Construit l'arbre de {@link ModelPart} (les os sont ajoutés après leur parent, quel que soit l'ordre du fichier). */
	public ModelPart creerModele() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();
		Map<String, ModelPartData> parties = new HashMap<>();
		List<Os> restants = new ArrayList<>(os);
		int garde = 0;
		while (!restants.isEmpty() && garde++ < 64) {
			for (Os o : new ArrayList<>(restants)) {
				ModelPartData parent = o.parent().isEmpty() ? root : parties.get(o.parent());
				if (parent == null) {
					continue;
				}
				ModelPartBuilder b = ModelPartBuilder.create();
				int i = 0;
				for (Cube c : o.cubes()) {
					b.uv(c.u(), c.v()).mirrored(c.miroir()).cuboid("c" + i++, c.ox(), c.oy(), c.oz(), c.sx(), c.sy(), c.sz(), new Dilation(c.gonflement()));
				}
				parties.put(o.nom(), parent.addChild(o.nom(), b, ModelTransform.of(o.px(), o.py(), o.pz(), o.rx(), o.ry(), o.rz())));
				restants.remove(o);
			}
		}
		for (Os o : restants) {
			root.addChild(o.nom(), ModelPartBuilder.create(), ModelTransform.NONE);
		}
		return TexturedModelData.of(data, largeurTexture, hauteurTexture).createModel();
	}
}
