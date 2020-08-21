package net.countercraft.movecraft.combat.movecraftcombat.config;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import net.countercraft.movecraft.craft.CraftType;


public class Config {
    public static boolean Debug = false;

    // Localisation
    public static String Locale = "en";

    // AA Directors
    public static int AADirectorDistance = 50;
    public static int AADirectorRange = 120;
    public static HashSet<CraftType> AADirectorsAllowed = new HashSet<>();

    // Contact Explosives
    public static boolean EnableContactExplosives = true;

    // Cannon Directors
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorRange = 120;
    public static HashSet<CraftType> CannonDirectorsAllowed = new HashSet<>();

    // Directors
    public static HashSet<Material> Transparent = new HashSet<>();

    // Durability Override
    public static HashMap<Material, Integer> DurabilityOverride;

    // Fireball Lifespan
    public static int FireballLifespan = 6;

    // TNT Tracers
    public static double TracerRateTicks = 5.0;
    public static long TracerMinDistanceSqrd = 360;

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
    
    // Anti-Player Radar
    public static boolean EnableAntiRadar = false;
}
