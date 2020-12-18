package net.countercraft.movecraft.combat.movecraftcombat.utils;

import net.countercraft.movecraft.combat.movecraftcombat.MovecraftCombat;
import org.bukkit.Material;

import javax.annotation.Nullable;

public class LegacyUtils {
    private static Material web;
    private boolean isLegacy;
    private static LegacyUtils instance;

    public static LegacyUtils getInstance() {
        return instance;
    }

    public LegacyUtils() {
        instance = this;
        String packageName = MovecraftCombat.getInstance().getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);

        try {
            String[] parts = version.split("_");
            int versionNumber = Integer.valueOf(parts[1]);
            //Check if the server is 1.12 and lower or 1.13 and higher
            isLegacy = versionNumber <= 12;

            if(isLegacy) {
                web = Material.getMaterial("WEB");
            }
            else {
                web = Material.getMaterial("COBWEB");
            }
            if(web == null) {
                throw new Exception("Failed to load either WEB or COBWEB");
            }
            MovecraftCombat.getInstance().getLogger().info("Loaded version: " + version);
        }
        catch(Exception e) {
            MovecraftCombat.getInstance().getLogger().info("Failed to load version: " + version);
        }
    }


    @Nullable
    public Material getCobweb() {
        return web;
    }

    public boolean isLegacy() {
        return isLegacy;
    }
}
