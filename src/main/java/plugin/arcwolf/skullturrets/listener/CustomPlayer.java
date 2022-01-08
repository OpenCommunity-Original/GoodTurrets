package plugin.arcwolf.skullturrets.listener;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CustomPlayer {
    public static Map<String, CustomPlayer> playerSettings;

    static {
        CustomPlayer.playerSettings = new HashMap<String, CustomPlayer>();
    }

    public String command;
    public PlacedSkull pc;
    public PlacedSkull skull_to_damage;
    public String playerName;
    public boolean UUIDLookup;
    public boolean updateAll;
    public long attackTimer;
    public boolean running;
    public boolean clearArrows;
    public boolean invUpdate;
    public int ammoAmount;
    public boolean skinSetSuccess;

    public CustomPlayer() {
        this.command = "";
        this.pc = null;
        this.skull_to_damage = null;
        this.playerName = "";
        this.UUIDLookup = false;
        this.updateAll = false;
        this.attackTimer = 0L;
        this.running = false;
        this.clearArrows = false;
        this.invUpdate = false;
        this.ammoAmount = 0;
        this.skinSetSuccess = false;
    }

    public static CustomPlayer getSettings(final CommandSender sender) {
        CustomPlayer settings = CustomPlayer.playerSettings.get(sender.getName());
        if (settings == null) {
            CustomPlayer.playerSettings.put(sender.getName(), new CustomPlayer());
            settings = CustomPlayer.playerSettings.get(sender.getName());
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
