package net.countercraft.movecraft.combat.blastoverride;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashSet;

public class BlastResistanceOverride{

    private static HashSet<BlockOverride> overrideSet = new HashSet<BlockOverride>();
    private static ResistFile overrideDur;
    private static String version;

    public static void enable(){

        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        version = packageName.substring(packageName.lastIndexOf('.') + 1);

        saveResource("overrides.resist");
        saveResource("defaults.resist");
        overrideDur = new ResistFile(MovecraftCombat.getInstance().getDataFolder(), "overrides.resist");
        try {
            for (Material mat : overrideDur.getMap().keySet()) {
                BlockOverride bo = new BlockOverride(mat);
                if (!bo.isValid()){
                    Bukkit.getLogger().warning("Block \"" + mat.name() + "\" is not applicable");
                    continue;
                }
                bo.set("durability", overrideDur.getMap().get(mat));
                overrideSet.add(bo);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void disable(){
        for(BlockOverride bo : overrideSet) {
            bo.revertAll();
        }
    }

    public static String getVersion(){return version;}

    private static void saveResource(String path){
        File f = new File(MovecraftCombat.getInstance().getDataFolder(), path);
        if(!f.exists()){
            MovecraftCombat.getInstance().saveResource(path, false);
        }
    }

    private static void copyDefaultValues() throws IOException {
        File fout = new File(MovecraftCombat.getInstance().getDataFolder(), "defaults.resist");
        FileOutputStream fos = new FileOutputStream(fout);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        bw.write("//Native minecraft blast resistance values");
        bw.newLine();
        try {
            for (Material mat : Material.values()) {
                BlockOverride bo = new BlockOverride(mat);
                if (!bo.isValid())
                    continue;
                Float d = (Float) bo.get("durability");
                bw.write(mat.name() + "=" + String.valueOf(d));
                bw.newLine();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        bw.close();
    }

}
