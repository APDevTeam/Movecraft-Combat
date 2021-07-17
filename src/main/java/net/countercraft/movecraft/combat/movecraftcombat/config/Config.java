package net.countercraft.movecraft.combat.movecraftcombat.config;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.Material;
import net.countercraft.movecraft.craft.CraftType;
import org.bukkit.Particle;


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
    public static double ContactExplosivesMaxImpulseFactor = 10;

    // Cannon Directors
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorRange = 120;
    public static HashSet<CraftType> CannonDirectorsAllowed = new HashSet<>();

    // Directors
    public static Material DirectorTool = null;
    public static HashSet<Material> Transparent = new HashSet<>();

    // Durability Override
    public static HashMap<Material, Integer> DurabilityOverride = null;

    // Fireball Lifespan
    public static int FireballLifespan = 6;

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
    
    // Anti-Player Radar
    public static boolean EnableAntiRadar = false;

    // Fireball Penetration
    public static boolean EnableFireballPenetration = true;

    // Add fires to craft hitbox
    public static boolean AddFiresToHitbox = true;

    // 1.12+ TNT-Translocation
    public static boolean ReImplementTNTTranslocation = false;
}
