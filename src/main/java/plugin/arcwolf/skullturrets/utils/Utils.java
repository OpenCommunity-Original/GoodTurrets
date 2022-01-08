package plugin.arcwolf.skullturrets.utils;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.Wool;
import plugin.arcwolf.skullturrets.SkullTurret;

import java.io.File;
import java.util.UUID;

public class Utils {
    public static String getLocalization(final String key) {
        final File localeFile = new File(SkullTurret.plugin.getDataFolder(), "locale_" + SkullTurret.LANGUAGE + ".yml");
        final FileConfiguration localeFileConfig = YamlConfiguration.loadConfiguration(localeFile);
        return localeFileConfig.getString(key, "SkullTurret: Could Not Find Localization Key " + key + " Update your localization file.");
    }

    public static String parseText(final String line) {
        return line.replaceAll("&([0-9a-fk-or])([^&])", "\\§$1$2");
    }

    public static Player getPlayerFromUUID(final UUID uuid) {
        final OfflinePlayer oPlayer = SkullTurret.plugin.getServer().getOfflinePlayer(uuid);
        return (oPlayer != null && oPlayer.isOnline()) ? oPlayer.getPlayer() : null;
    }

    public static OfflinePlayer getOfflinePlayerFromUUID(final UUID uuid) {
        return SkullTurret.plugin.getServer().getOfflinePlayer(uuid);
    }

    public static Player getPlayer(final String name) {
        for (final Player test : Bukkit.matchPlayer(name)) {
            if (test.getName().equals(name)) {
                return test;
            }
        }
        return null;
    }

    @Deprecated
    public static void clearBlock(final Block block) {
        block.setType(Material.WHITE_WOOL);
        final BlockState state = block.getState();
        final Wool w = (Wool) state.getData();
        w.setColor(DyeColor.WHITE);
        state.update();
        block.setType(Material.AIR);
    }
}
