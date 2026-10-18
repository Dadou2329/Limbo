# Guide d'installation

## Pour jouer

1. Installez **Minecraft Java 1.21.1**.
2. Installez **Fabric Loader** (0.20.0 ou plus récent) pour 1.21.1 : https://fabricmc.net/use/
3. Téléchargez **Fabric API** pour 1.21.1 (0.116.17+1.21.1 ou plus récent) : https://modrinth.com/mod/fabric-api
4. Placez `fabric-api-*.jar` et `les-limbes-*.jar` dans le dossier `mods` de votre profil Fabric.
5. Lancez le jeu avec le profil Fabric. Le mod est entièrement en français ; aucune autre dépendance n'est nécessaire. Les shaders sont facultatifs.

Le JAR du mod se trouve dans `build/libs/` après compilation (voir ci-dessous), ou dans les publications du dépôt.

## Nouvelle partie

Créez une nouvelle sauvegarde de survie normale. La dimension des Limbes est accessible sans commande et sans carte externe : vous recevez un carnet à la première connexion. Les mondes créés avant l'installation du mod reçoivent aussi la dimension (elle est définie par données), mais les vestiges n'apparaissent que dans les chunks encore non générés.

## Serveur dédié

Même procédure : `fabric-server-launcher` pour 1.21.1, puis `fabric-api` et `les-limbes` dans `mods/`. Toute la logique de jeu est côté serveur ; les clients doivent aussi installer le mod (interface, rendu).

## Compiler soi-même

- JDK 21 (Temurin ou OpenJDK). Gradle 9.8.0 est téléchargé par le wrapper.
- `./gradlew build` (Linux/macOS) ou `gradlew.bat build` (Windows).
- Première compilation : quelques minutes (téléchargement de Minecraft, des mappings Yarn et de Fabric API).

Versions utilisées (vérifiées sur les dépôts officiels le 18 octobre 2026) :

| Composant | Version |
|---|---|
| Minecraft | 1.21.1 |
| Fabric Loader | 0.20.0 |
| Fabric API | 0.116.17+1.21.1 |
| Yarn | 1.21.1+build.3 |
| Fabric Loom | 1.17.21 |
| Gradle | 9.8.0 |
| JDK | 21 |

Note : Loom 1.18 et suivants exigent un JVM 25 pour Gradle ; Loom 1.17.21 a été retenu pour rester sur JDK 21 avec une chaîne simple.
