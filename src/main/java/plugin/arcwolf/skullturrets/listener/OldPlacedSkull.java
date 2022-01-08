package plugin.arcwolf.skullturrets.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import plugin.arcwolf.skullturrets.SkullTurret;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class OldPlacedSkull {
    private final Character SEPERATOR;
    private final SkullIntelligence intelligence;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    public Map<EntityType, EntityType> enemies;
    public Map<EntityType, EntityType> friends;
    public Map<String, String> playerFrenemies;
    public boolean failed;
    private SkullType type;
    private String skullCreator;
    private String skullSkinData;
    private String worldName;
    private World world;
    private Block skullBlock;
    private EntityType ammoType;
    private boolean patrol;
    private boolean redstone;
    private boolean commandFireArrow;

    public OldPlacedSkull(final String inString) {
        this.SEPERATOR = '\u001f';
        this.type = SkullType.SKELETON;
        this.skullCreator = "";
        this.skullSkinData = "";
        this.worldName = "";
        this.world = null;
        this.ammoType = EntityType.ARROW;
        this.patrol = true;
        this.redstone = false;
        this.commandFireArrow = false;
        this.enemies = new HashMap<EntityType, EntityType>();
        this.friends = new HashMap<EntityType, EntityType>();
        this.playerFrenemies = new HashMap<String, String>();
        this.failed = false;
        String[] split = inString.split("@");
        final String[] newSplit = inString.split(this.SEPERATOR.toString());
        if (newSplit.length == 15) {
            split = newSplit;
        }
        this.worldName = split[0];
        this.centerX = Integer.parseInt(split[1]);
        this.centerY = Integer.parseInt(split[2]);
        this.centerZ = Integer.parseInt(split[3]);
        this.skullCreator = split[4];
        this.type = SkullType.valueOf(split[6]);
        this.skullSkinData = (split[7].equals("-") ? "" : split[7]);
        String[] split2;
        for (int length = (split2 = split[8].split(",")).length, i = 0; i < length; ++i) {
            final String nme = split2[i];
            final String[] val = nme.split(":");
            if (val.length == 2 && val[1].equals("-") && val[0].equals("PLAYER")) {
                this.enemies.put(EntityType.PLAYER, EntityType.PLAYER);
            } else {
                EntityType t = null;
                try {
                    if (val[0].equals("-")) {
                        continue;
                    }
                    t = EntityType.valueOf(val[0].toUpperCase());
                } catch (Exception e) {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": " + val[0] + " is not a valid enemy EntityType!");
                    continue;
                }
                if (t != null) {
                    if (t.equals(EntityType.PLAYER)) {
                        this.playerFrenemies.put(val[1], "ENEMY");
                    } else {
                        this.enemies.put(t, t);
                    }
                }
            }
        }
        String[] split3;
        for (int length2 = (split3 = split[9].split(",")).length, j = 0; j < length2; ++j) {
            final String fnd = split3[j];
            final String[] val = fnd.split(":");
            if (val.length == 2 && val[1].equals("-") && val[0].equals("PLAYER")) {
                this.friends.put(EntityType.PLAYER, EntityType.PLAYER);
            } else {
                EntityType t = null;
                try {
                    if (val[0].equals("-")) {
                        continue;
                    }
                    t = EntityType.valueOf(val[0].toUpperCase());
                } catch (Exception e) {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": " + val[0] + " is not a valid friendly EntityType!");
                    continue;
                }
                if (t != null) {
                    if (t.equals(EntityType.PLAYER)) {
                        this.playerFrenemies.put(val[1], "FRIEND");
                    } else {
                        this.friends.put(t, t);
                    }
                }
            }
        }
        this.ammoType = EntityType.valueOf(split[10]);
        this.intelligence = SkullIntelligence.valueOf(split[11].toUpperCase());
        this.patrol = Boolean.parseBoolean(split[12]);
        this.redstone = Boolean.parseBoolean(split[13]);
        this.commandFireArrow = Boolean.parseBoolean(split[14]);
        this.world = SkullTurret.plugin.getServer().getWorld(this.worldName);
        this.failed = (this.world == null);
        if (this.failed) {
            if (SkullTurret.DEBUG == 4) {
                SkullTurret.LOGGER.log(Level.SEVERE, "=== Failed with : world? " + (this.world == null) + " nm = " + this.worldName + " skull? " + (this.getSkull() == null) + "loc = " + this.getLocation() + " ===");
            }
        }
    }

    public Skull getSkull() {
        return (this.skullBlock.getType() == Material.SKELETON_SKULL) ? ((Skull) this.skullBlock.getState()) : null;
    }

    public Location getLocation() {
        return new Location(this.world, this.centerX, this.centerY, this.centerZ);
    }

    public SkullType getType() {
        return this.type;
    }

    public SkullIntelligence getIntelligence() {
        return this.intelligence;
    }

    public String getSkullCreator() {
        return this.skullCreator;
    }

    public String getSkullSkinData() {
        return this.skullSkinData;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public World getWorld() {
        return this.world;
    }

    public int getCenterX() {
        return this.centerX;
    }

    public int getCenterY() {
        return this.centerY;
    }

    public int getCenterZ() {
        return this.centerZ;
    }

    public EntityType getAmmoType() {
        return this.ammoType;
    }

    public boolean isPatrol() {
        return this.patrol;
    }

    public boolean isRedstone() {
        return this.redstone;
    }

    public boolean isCommandFireArrow() {
        return this.commandFireArrow;
    }

    public Map<EntityType, EntityType> getEnemies() {
        return this.enemies;
    }

    public Map<EntityType, EntityType> getFriends() {
        return this.friends;
    }

    public Map<String, String> getPlayerFrenemies() {
        return this.playerFrenemies;
    }
}
