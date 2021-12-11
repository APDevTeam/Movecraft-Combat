package net.countercraft.movecraft.combat.config;

public class Config {
    public static boolean Debug = false;

    // Localisation
    public static String Locale = "en";

    // Contact Explosives
    public static boolean EnableContactExplosives = true;
    public static double ContactExplosivesMaxImpulseFactor = 10;

    // Cannon Directors
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorRange = 120;

    // Damage Tracking
    public static boolean EnableFireballTracking = false;
    public static boolean EnableTNTTracking = true;
    public static boolean EnableTorpedoTracking = false;
    public static int DamageTimeout = 300;

    // Combat Releasing
    public static boolean EnableCombatReleaseTracking = false;
    public static boolean EnableCombatReleaseKick = true;
    public static long CombatReleaseBanLength = 60;
    public static boolean CombatReleaseScuttle = true;
}
