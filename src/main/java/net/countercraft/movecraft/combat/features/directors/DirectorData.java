package net.countercraft.movecraft.combat.features.directors;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.entity.Player;

import java.util.List;

public class DirectorData {
    private Player player;
    private PlayerCraft craft;
    private List<String> selectedSigns;

    public DirectorData(Player player, PlayerCraft craft, List<String> selectedSigns) {
        this.player = player;
        this.craft = craft;
        this.selectedSigns = selectedSigns;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerCraft getCraft() {
        return craft;
    }

    public List<String> getSelectedSigns() {
        return selectedSigns;
    }
}