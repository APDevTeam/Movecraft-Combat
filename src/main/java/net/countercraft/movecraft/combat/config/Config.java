package net.countercraft.movecraft.combat.config;

import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.EnumSet;


public class Config {
    public static boolean Debug = false;

    // Localisation
    public static String Locale = "en";

    // AA Directors
    public static int AADirectorDistance = 50;
    public static int AADirectorRange = 120;

    // Contact Explosives
    public static boolean EnableContactExplosives = true;
    public static double ContactExplosivesMaxImpulseFactor = 10;

    // Cannon Directors
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorRange = 120;

    // Directors
    public static Material DirectorTool = null;
    public static EnumSet<Material> Transparent = EnumSet.noneOf(Material.class);

    // TNT Tracers
    public static double TracerRateTicks = 5.0;
    public static long TracerMinDistanceSqrd = 360;
    public static Particle TracerParticle = null;
    public static Particle ExplosionParticle = null;

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
    
    // 1.12+ TNT-Translocation
    public static boolean ReImplementTNTTranslocation = false;
}
