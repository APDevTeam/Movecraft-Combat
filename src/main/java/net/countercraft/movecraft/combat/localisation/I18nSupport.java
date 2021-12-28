package net.countercraft.movecraft.combat.localisation;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

public class I18nSupport {
    public static String Locale = "en";

    private static Properties langFile;

    public static void load(@NotNull FileConfiguration config) {
        Locale = config.getString("Locale", "en");

        langFile = new Properties();

        File langDirectory = new File(MovecraftCombat.getInstance().getDataFolder().getAbsolutePath() + "/localisation");
        if (!langDirectory.exists())
            langDirectory.mkdirs();

        InputStream stream = null;
        try {
            stream = new FileInputStream(langDirectory.getAbsolutePath() + "/mcclang_" + Locale + ".properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (stream == null) {
            MovecraftCombat.getInstance().getLogger().log(Level.SEVERE, "Critical Error in localisation system!");
            MovecraftCombat.getInstance().getServer().shutdown();
        }

        try {
            langFile.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getInternationalisedString(String key) {
        String ret = langFile.getProperty(key);
        if (ret != null) {
            return ret;
        } else {
            return key;
        }
    }
}
