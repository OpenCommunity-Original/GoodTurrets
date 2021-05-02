package plugin.arcwolf.skullturrets;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CustomPlayer {
    public String command = "";

    public PlacedSkull pc = null;

    public PlacedSkull skull_to_damage = null;

    public String playerName = "";

    public boolean UUIDLookup = false;

    public boolean updateAll = false;

    public long attackTimer = 0L;

    public boolean running = false;

    public boolean clearArrows = false;

    public boolean invUpdate = false;

    public int ammoAmount = 0;

    public boolean skinSetSuccess = false;

    public static Map<String, CustomPlayer> playerSettings = new HashMap<String, CustomPlayer>();

    public static CustomPlayer getSettings(CommandSender sender) {
        CustomPlayer settings = playerSettings.get(sender.getName());
        if (settings == null) {
            playerSettings.put(sender.getName(), new CustomPlayer());
            settings = playerSettings.get(sender.getName());
        }
        return settings;
    }

    public void clearPlayer() {
        this.command = "";
        this.pc = null;
        this.playerName = "";
        this.skull_to_damage = null;
        this.attackTimer = 0L;
        this.clearArrows = false;
        this.invUpdate = false;
        this.ammoAmount = 0;
        this.skinSetSuccess = false;
    }
}
