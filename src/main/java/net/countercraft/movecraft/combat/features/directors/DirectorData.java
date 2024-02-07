package net.countercraft.movecraft.combat.features.directors;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.PlayerCraft;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class DirectorData {
    private final Player player;
    private final PlayerCraft craft;
    private final Set<String> selectedNodes;
    private Set<Vector> signLocations = new HashSet<>();
    private MovecraftLocation currentLocation;

    public DirectorData(Player player, PlayerCraft craft, Set<String> selectedNodes) {
        this.player = player;
        this.craft = craft;
        this.selectedNodes = selectedNodes;
    }

    @NotNull
    public Set<Vector> getSignLocations() {
        if (selectedNodes.isEmpty()) return signLocations;
        //If the craft stays in the same position, return the already known sign locations.
        MovecraftLocation midpoint = craft.getHitBox().getMidPoint();
        if (currentLocation == null) currentLocation = midpoint;
        if (currentLocation.equals(midpoint) && !signLocations.isEmpty()) {
            return signLocations;
        }

        currentLocation = midpoint;
        for (MovecraftLocation location : craft.getHitBox()) {
            Block block = craft.getWorld().getBlockAt(
                    location.getX(),
                    location.getY(),
                    location.getZ()
            );
            if (!(block.getState() instanceof Sign)) continue;
            Sign sign = (Sign) block.getState();

            if (!sign.getLine(0).equalsIgnoreCase("subcraft rotate")) continue;
            if (sign.getLine(3).isBlank()) continue;
            if (!selectedNodes.contains(sign.getLine(3))) continue;

            Vector relativeVector = sign.getLocation().toVector();
            signLocations.add(relativeVector);
        }
        return signLocations;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public PlayerCraft getCraft() {
        return craft;
    }

    @NotNull
    public Set<String> getSelectedNodes() {
        return selectedNodes;
    }
}