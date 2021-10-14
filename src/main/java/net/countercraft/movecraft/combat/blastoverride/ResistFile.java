package net.countercraft.movecraft.combat.blastoverride;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ResistFile {

    private File file;
    private ImmutableMap<Material, Float> map;

    public ResistFile(File parent, String path) {
        HashMap<Material, Float> tempMap = new HashMap<>();
        file = new File(parent, path);
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line; (line = br.readLine()) != null; ) {
                line = line.trim();
                if(line.isEmpty())
                    continue;
                if(line.startsWith("//"))
                    continue;
                String[] args = line.split("=");
                for(Material mat : getBlocks(args[0]))
                    tempMap.put(mat, Float.parseFloat(args[1]));
            }
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        map = ImmutableMap.copyOf(tempMap);
    }

    public ImmutableMap<Material, Float> getMap(){
        return map;
    }

    public static ArrayList<Material> getBlocks(String name){
        ArrayList<Material> blocks = new ArrayList<>();
        if(name.startsWith("#")){
            name = name.substring(1);
            if(!name.contains(":"))
                name = "minecraft:" + name;
            Iterable<Tag<Material>> tags = Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class);
            for(Tag<Material> tag : tags){
                if(tag.getKey().toString().equalsIgnoreCase(name)){
                    blocks.addAll(tag.getValues());
                    return blocks;
                }
            }
            Bukkit.getLogger().warning("Could not find tag \"" + name + "\"");
        }else{
            Material mat = Material.matchMaterial(name);
            if(mat == null) {
                Bukkit.getLogger().warning("Could not find block \"" + name + "\"");
                return blocks;
            }
            blocks.add(Material.matchMaterial(name));
        }
        return blocks;
    }

}
