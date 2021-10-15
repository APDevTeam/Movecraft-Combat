package net.countercraft.movecraft.combat.blastoverride;

import net.countercraft.movecraft.combat.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.HashSet;

public class BlastResistanceOverride{

    private static HashSet<BlockOverride> overrideSet = new HashSet<BlockOverride>();
    private static String version;

    public static void enable(){

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf('.') + 1);

        if(Config.ForceRevertToVanilla)
            revertAllToVanilla();

        try {
            for (Material mat : Config.BlastResistanceOverride.keySet()) {
                BlockOverride bo = new BlockOverride(mat);
                if (!bo.isValid()){
                    Bukkit.getLogger().warning("Block \"" + mat.name() + "\" is not applicable");
                    continue;
                }
                bo.setBlastResistance(Config.BlastResistanceOverride.get(mat));
                overrideSet.add(bo);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void disable(){
        for(BlockOverride bo : overrideSet) {
            bo.revertToVanilla();
        }
    }

    public static String getVersion(){return version;}

    public static void revertAllToVanilla(){
        try {
            for (Material mat : Material.values()) {
                BlockOverride bo = new BlockOverride(mat);
                if (!bo.isValid())
                    continue;
                bo.revertToVanilla();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
