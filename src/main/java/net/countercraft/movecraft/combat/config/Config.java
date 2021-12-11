package net.countercraft.movecraft.combat.config;

@Deprecated(forRemoval = true)
public class Config {
    // Localisation
    public static String Locale = "en";

    // Contact Explosives
    public static boolean EnableContactExplosives = true;
    public static double ContactExplosivesMaxImpulseFactor = 10;

    // Combat Releasing
    public static boolean EnableCombatReleaseTracking = false;
    public static boolean EnableCombatReleaseKick = true;
    public static long CombatReleaseBanLength = 60;
    public static boolean CombatReleaseScuttle = true;
}
