package net.countercraft.movecraft.combat.features.tracers.config;

import net.countercraft.movecraft.combat.MovecraftCombat;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerConfig extends YamlConfiguration {
    private final File configFile;
    private final UUID owner;
    private String setting = "HIGH";
    private String mode = "BLOCKS";

    public PlayerConfig(UUID owner) {
        super();
        configFile = new File(MovecraftCombat.getInstance().getDataFolder().getAbsolutePath() + "/userdata/" + owner + ".yml");
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getSetting() {
        return setting;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }


    public void load() {
        byte[] bytebuffer = new byte[1024];
        try {
            try(FileInputStream inputStream = new FileInputStream(configFile)) {
                long startSize = configFile.length();
                if (startSize > Integer.MAX_VALUE)
                    throw new InvalidConfigurationException("File too big");

                ByteBuffer buffer = ByteBuffer.allocate((int) startSize);
                int length;
                while ((length = inputStream.read(bytebuffer)) != -1) {
                    if (length > buffer.remaining()) {
                        ByteBuffer resize = ByteBuffer.allocate(buffer.capacity() + length - buffer.remaining());
                        int resizePosition = buffer.position();
                        // Fix builds compiled against Java 9+ breaking on Java 8
                        ((Buffer) buffer).rewind();
                        resize.put(buffer);
                        resize.position(resizePosition);
                        buffer = resize;
                    }
                    buffer.put(bytebuffer, 0, length);
                }
                ((Buffer) buffer).rewind();
                final CharBuffer data = CharBuffer.allocate(buffer.capacity());
                CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                CoderResult result = decoder.decode(buffer, data, true);
                if(result.isError()) {
                    ((Buffer) buffer).rewind();
                    ((Buffer) data).clear();
                    MovecraftCombat.getInstance().getLogger().log(Level.INFO, "File " + configFile.getAbsolutePath() + " is not utf-8 encoded, trying " + Charset.defaultCharset().displayName());
                    decoder = Charset.defaultCharset().newDecoder();
                    result = decoder.decode(buffer, data, true);
                    if(result.isError())
                        throw new InvalidConfigurationException("Invalid Characters in file " + configFile.getAbsolutePath());
                    else
                        decoder.flush(data);

                }
                else
                    decoder.flush(data);

                final int end = data.position();
                ((Buffer) data).rewind();
                super.loadFromString(data.subSequence(0, end).toString());
            }
        }
        catch(FileNotFoundException ex) {
            save();
        }
        catch(IOException ex) {
            MovecraftCombat.getInstance().getLogger().log(Level.SEVERE, ex.getMessage(), ex);
        }
        catch(InvalidConfigurationException ex) {
            File broken = new File(configFile.getAbsolutePath() + ".broken." + System.currentTimeMillis());
            boolean ignored = configFile.renameTo(broken);
            MovecraftCombat.getInstance().getLogger().log(Level.SEVERE, "The file " + configFile.toString() + " is broken, it has been renamed to " + broken.toString(), ex.getCause());
        }
        setting = getString("setting");
        mode = getString("mode");
    }

    public void save() {
        set("setting", setting);
        set("mode", mode);
        String data = saveToString();
        try(FileOutputStream fos = new FileOutputStream(configFile)) {
            try(OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                writer.write(data);
            }
        }
        catch(FileNotFoundException ex) {
            try {
                save(configFile);
            }
            catch(IOException e) {
                MovecraftCombat.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
            }
        }
        catch(IOException e) {
            MovecraftCombat.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
