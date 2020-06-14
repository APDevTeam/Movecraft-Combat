package net.countercraft.movecraft.combat.movecraftcombat.config;

import java.util.HashMap;
import java.util.HashSet;

import net.countercraft.movecraft.craft.CraftType;


public class Config {
    public static boolean Debug = false;

    // AA Directors
    public static int AADirectorDistance = 50;
    public static int AADirectorRange = 120;
    public static HashSet<CraftType> AADirectorsAllowed = new HashSet<>();

    // Cannon Directors
    public static int CannonDirectorDistance = 100;
    public static int CannonDirectorRange = 120;
    public static HashSet<CraftType> CannonDirectorsAllowed = new HashSet<>();

    // Durability Override
    public static HashMap<Integer, Integer> DurabilityOverride;

    // Fireball Lifespan
    public static int FireballLifespan = 6;

    // Tracers
    public static double TracerRateTicks = 5.0;
    public static long TracerMinDistanceSqrd = 360;
}
