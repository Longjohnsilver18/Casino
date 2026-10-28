ROYAL CASINO - Fabric Mod (Minecraft 1.21.1, nur Client)

Bauen:
 1. JDK 21 installieren.
 2. Gradle (>= 8.8) installieren und im Ordner einmal "gradle wrapper" ausfuehren.
    (Alternativ: Fabric-Template von https://fabricmc.net/develop/template/ generieren
     und src/, build.gradle, gradle.properties hier hinein kopieren.)
 3. ./gradlew build     (Windows: gradlew.bat build)
 4. build/libs/royalcasino-1.0.0.jar  (NICHT die -sources.jar) in den mods-Ordner legen.
    Benoetigt: Fabric Loader + Fabric API fuer 1.21.1.

Bedienung:
 - Taste "Ö" (deutsche Tastatur) oeffnet/schliesst das Casino. Aenderbar unter Steuerung > Royal Casino.
 - Coins + Zeitstempel liegen in config/royalcasino.json.
