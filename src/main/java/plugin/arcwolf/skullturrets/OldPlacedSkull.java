package plugin.arcwolf.skullturrets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class OldPlacedSkull {
    private final Character SEPERATOR = Character.valueOf('\037');

    private SkullType type = SkullType.SKELETON;

    private final SkullIntelligence intelligence;

    private String skullCreator = "";

    private String skullSkinData = "";

    private String worldName = "";

    private World world = null;

    private Block skullBlock;

    private final int centerX;

    private final int centerY;

    private final int centerZ;

    private EntityType ammoType = EntityType.ARROW;

    private boolean patrol = true;

    private boolean redstone = false;

    private boolean commandFireArrow = false;

    public Map<EntityType, EntityType> enemies = new HashMap<EntityType, EntityType>();

    public Map<EntityType, EntityType> friends = new HashMap<EntityType, EntityType>();

    public Map<String, String> playerFrenemies = new HashMap<String, String>();

    public boolean failed = false;

    public OldPlacedSkull(String inString) {
        String[] split = inString.split("@");
        String[] newSplit = inString.split(this.SEPERATOR.toString());
        if (newSplit.length == 15)
            split = newSplit;
        this.worldName = split[0];
        this.centerX = Integer.parseInt(split[1]);
        this.centerY = Integer.parseInt(split[2]);
        this.centerZ = Integer.parseInt(split[3]);
        this.skullCreator = split[4];
        this.type = SkullType.valueOf(split[6]);
        this.skullSkinData = split[7].equals("-") ? "" : split[7];
        byte b;
        int i;
        String[] arrayOfString1;
        for (i = (arrayOfString1 = split[8].split(",")).length, b = 0; b < i; ) {
            String nme = arrayOfString1[b];
            String[] val = nme.split(":");
            if (val.length == 2 && val[1].equals("-") && val[0].equals("PLAYER")) {
                this.enemies.put(EntityType.PLAYER, EntityType.PLAYER);
            } else {
                EntityType t = null;
                try {
                    if (!val[0].equals("-")) {
                        t = EntityType.valueOf(val[0].toUpperCase());
                        if (t != null)
                            if (t.equals(EntityType.PLAYER)) {
                                this.playerFrenemies.put(val[1], "ENEMY");
                            } else {
                                this.enemies.put(t, t);
                            }
                    }
                } catch (Exception e) {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": " + val[0] + " is not a valid enemy EntityType!");
                }
            }
            b++;
        }
        for (i = (arrayOfString1 = split[9].split(",")).length, b = 0; b < i; ) {
            String fnd = arrayOfString1[b];
            String[] val = fnd.split(":");
            if (val.length == 2 && val[1].equals("-") && val[0].equals("PLAYER")) {
                this.friends.put(EntityType.PLAYER, EntityType.PLAYER);
            } else {
                EntityType t = null;
                try {
                    if (!val[0].equals("-")) {
                        t = EntityType.valueOf(val[0].toUpperCase());
                        if (t != null)
                            if (t.equals(EntityType.PLAYER)) {
                                this.playerFrenemies.put(val[1], "FRIEND");
                            } else {
                                this.friends.put(t, t);
                            }
                    }
                } catch (Exception e) {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": " + val[0] + " is not a valid friendly EntityType!");
                }
            }
            b++;
        }
        this.ammoType = EntityType.valueOf(split[10]);
        this.intelligence = SkullIntelligence.valueOf(split[11].toUpperCase());
        this.patrol = Boolean.parseBoolean(split[12]);
        this.redstone = Boolean.parseBoolean(split[13]);
        this.commandFireArrow = Boolean.parseBoolean(split[14]);
        this.world = SkullTurret.plugin.getServer().getWorld(this.worldName);
        this.failed = (this.world == null);
        if (this.failed) {
            if (SkullTurret.DEBUG == 4)
                SkullTurret.LOGGER.log(Level.SEVERE, "=== Failed with : world? " + ((this.world == null) ? 1 : 0) + " nm = " + this.worldName + " skull? " + ((getSkull() == null) ? 1 : 0) + "loc = " + getLocation() + " ===");
            return;
        }
    }

    public Skull getSkull() {
        return (this.skullBlock.getType() == Material.SKELETON_SKULL) ? (Skull) this.skullBlock.getState() : null;
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
