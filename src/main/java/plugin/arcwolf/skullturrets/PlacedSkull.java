package plugin.arcwolf.skullturrets;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class PlacedSkull {
    protected final Character SEPERATOR = Character.valueOf('\037');

    private final EntityType PINGER = EntityType.EXPERIENCE_ORB;

    private SkullType type = SkullType.SKELETON;

    private SkullIntelligence intelligence;

    private UUID skullCreator = null;

    private String skullCreatorLastKnowName = "";

    private String skullSkinData = "";

    private String worldName = "";

    private World world = null;

    private Block skullBlock;

    private final int centerX;

    private final int centerY;

    private final int centerZ;

    private int maxRange;

    private int currentRotDirId;

    private int dirMod = 1;

    private double fireRange;

    private long targetLockTimer;

    private long rotTimer;

    private long rotUpdateTimer;

    private long deathTimer;

    private long coolDown = 0L;

    protected EntityType ammoType = EntityType.ARROW;

    private Vector firingSolution = null;

    private LivingEntity target;

    private boolean patrol = true;

    private boolean redstone = false;

    private boolean fireArrow = false;

    private boolean commandFireArrow = false;

    private boolean dead = false;

    private boolean dying = false;

    private boolean settingSkin = false;

    private boolean disabled = false;

    private MyChunk chunk;

    private Block redstoneBlock;

    private double health;

    private long recoveryTimer;

    private long destructTimer;

    private final Map<RangeCoord, FacingRange> directions = new HashMap<RangeCoord, FacingRange>();

    public Map<EntityType, EntityType> enemies = new HashMap<EntityType, EntityType>();

    public Map<EntityType, EntityType> friends = new HashMap<EntityType, EntityType>();

    public Map<UUID, PlayerNamesFoF> playerFrenemies = new HashMap<UUID, PlayerNamesFoF>();

    public boolean failed = false;

    public PlacedSkull(Block skullBlock, UUID skullCreator, int maxRange, SkullIntelligence intelligence) {
        this.skullCreator = skullCreator;
        this.skullBlock = skullBlock;
        this.maxRange = maxRange;
        this.intelligence = intelligence;
        this.fireRange = maxRange * intelligence.getFireRangeMultiplier();
        this.world = skullBlock.getWorld();
        this.worldName = this.world.getName();
        this.centerX = skullBlock.getLocation().getBlockX();
        this.centerZ = skullBlock.getLocation().getBlockZ();
        this.centerY = skullBlock.getLocation().getBlockY();
        this.failed = !(this.world != null && getSkull() != null);
        if (this.failed)
            return;
        PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(skullCreator);
        this.chunk = new MyChunk(getLocation().getChunk());
        init(pps);
        if (SkullTurret.DEBUG == 1)
            debug();
        this.deathTimer = this.rotUpdateTimer = System.currentTimeMillis();
        this.redstoneBlock = skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        this.health = intelligence.getHealth();
    }

    public PlacedSkull(OldPlacedSkull skull, UUID creatorUUID) {
        this.worldName = skull.getWorldName();
        this.centerX = skull.getCenterX();
        this.centerY = skull.getCenterY();
        this.centerZ = skull.getCenterZ();
        this.skullCreator = creatorUUID;
        PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        if (pps != null && pps.isPps()) {
            this.maxRange = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator).getMaxRange();
        } else if (ppg != null) {
            this.maxRange = ppg.getMaxRange();
        } else {
            this.maxRange = SkullTurret.MAX_RANGE;
        }
        this.type = skull.getType();
        this.skullSkinData = skull.getSkullSkinData();
        this.ammoType = skull.getAmmoType();
        this.intelligence = skull.getIntelligence();
        this.patrol = skull.isPatrol();
        this.redstone = skull.isRedstone();
        this.commandFireArrow = skull.isCommandFireArrow();
        this.fireRange = this.maxRange * this.intelligence.getFireRangeMultiplier();
        this.health = this.intelligence.getHealth();
    }

    public PlacedSkull(String inString) {
        String[] split = inString.split(this.SEPERATOR.toString());
        this.worldName = split[0];
        this.centerX = Integer.parseInt(split[1]);
        this.centerY = Integer.parseInt(split[2]);
        this.centerZ = Integer.parseInt(split[3]);
        this.skullCreator = UUID.fromString(split[4]);
        PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        if (pps != null && pps.isPps()) {
            this.maxRange = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator).getMaxRange();
        } else if (ppg != null) {
            this.maxRange = ppg.getMaxRange();
        } else {
            this.maxRange = SkullTurret.MAX_RANGE;
        }
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
                            if (t.equals(EntityType.PLAYER) && val.length == 3) {
                                PlayerNamesFoF pnf = new PlayerNamesFoF(val[2], "ENEMY");
                                this.playerFrenemies.put(UUID.fromString(val[1]), pnf);
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
                            if (t.equals(EntityType.PLAYER) && val.length == 3) {
                                PlayerNamesFoF pnf = new PlayerNamesFoF(val[2], "FRIEND");
                                this.playerFrenemies.put(UUID.fromString(val[1]), pnf);
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
        this.fireRange = this.maxRange * this.intelligence.getFireRangeMultiplier();
        this.health = this.intelligence.getHealth();
        this.world = SkullTurret.plugin.getServer().getWorld(this.worldName);
        this.failed = (this.world == null);
        this.skullBlock = getLocation().getBlock();
        this.skullBlock.getChunk().load();
        this.failed = (getSkull() == null);
        if (this.failed) {
            if (SkullTurret.DEBUG == 4)
                SkullTurret.LOGGER.log(Level.SEVERE, "=== Failed with : world? " + ((this.world == null) ? 1 : 0) + " nm = " + this.worldName + " skull? " + ((getSkull() == null) ? 1 : 0) + "loc = " + getLocation() + " ===");
            return;
        }
        this.deathTimer = this.rotUpdateTimer = System.currentTimeMillis();
        this.chunk = new MyChunk(getLocation().getChunk());
        this.redstoneBlock = this.skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        init(pps);
    }

    private void init(PerPlayerSettings pps) {
        int topLeftX = this.centerX - this.maxRange;
        int topLeftZ = this.centerZ - this.maxRange;
        int bottomRightX = this.centerX + this.maxRange;
        int bottomRightZ = this.centerZ + this.maxRange;
        int topRightX = this.centerX + this.maxRange;
        int topRightZ = this.centerZ - this.maxRange;
        int bottomLeftX = this.centerX - this.maxRange;
        int bottomLeftZ = this.centerZ + this.maxRange;
        int thirdMaxRange = (int) Math.ceil(this.maxRange / 3.0D);
        int count = thirdMaxRange;
        List<EdgeCoords> edgeCoords = new ArrayList<EdgeCoords>();
        List<SliceSection> slices = new ArrayList<SliceSection>();
        edgeCoords.add(new EdgeCoords(topLeftX, topLeftZ));
        for (int k = topLeftX; k <= topRightX; k++) {
            if (k >= topLeftX + count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(k, topLeftZ));
            }
        }
        count = thirdMaxRange;
        for (int j = topRightZ; j <= bottomRightZ; j++) {
            if (j >= topRightZ + count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(topRightX, j));
            }
        }
        count = thirdMaxRange;
        for (int x = bottomRightX; x >= bottomLeftX; x--) {
            if (x <= bottomRightX - count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(x, bottomRightZ));
            }
        }
        count = thirdMaxRange;
        for (int z = bottomLeftZ; z >= topLeftZ; z--) {
            if (z <= bottomLeftZ - count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(topLeftX, z));
            }
        }
        for (int i = 0; i < edgeCoords.size() &&
                i + 1 != edgeCoords.size(); i++) {
            EdgeCoords ec1 = edgeCoords.get(i), ec2 = edgeCoords.get(i + 1);
            int[] xPoints = {ec1.x, this.centerX, ec2.x};
            int[] zPoints = {ec1.z, this.centerZ, ec2.z};
            BlockFace face = getSliceFace(i);
            slices.add(new SliceSection(new Polygon(xPoints, zPoints, 3), face));
        }
        Map<RangeCoord, RangeCoord> ranges = new HashMap<RangeCoord, RangeCoord>();
        for (int n = topLeftX; n <= bottomRightX; n++) {
            for (int i1 = topLeftZ; i1 <= bottomRightZ; i1++) {
                RangeCoord rc = new RangeCoord(this.world, n, this.centerY, i1);
                int pointX = n;
                int pointZ = i1;
                if (!rc.getLocation().equals(this.skullBlock.getLocation()))
                    for (SliceSection s : slices) {
                        if (sliceContains(s, pointX, this.centerY, pointZ)) {
                            this.directions.put(rc, new FacingRange(s.face, 0));
                            ranges.put(rc, rc);
                            break;
                        }
                    }
            }
        }
        for (int m = 0; m <= this.maxRange; m++) {
            int max = this.maxRange - m;
            for (int i1 = topLeftX + m; i1 <= bottomRightX - m; i1++) {
                for (int i2 = topLeftZ + m; i2 <= bottomRightZ - m; i2++) {
                    if (i1 == topLeftX + m || i2 == topLeftZ + m || i1 == bottomRightX - m || i2 == bottomRightZ - m) {
                        RangeCoord rc = new RangeCoord(this.world, i1, this.centerY, i2);
                        if (!rc.getLocation().equals(this.skullBlock.getLocation()) &&
                                this.directions.containsKey(rc))
                            this.directions.get(rc).rangeId = max;
                    }
                }
            }
        }
        if (this.chunk.isLoaded())
            this.currentRotDirId = getCurrentRotation();
        if (pps != null && (pps.isMasterDefaults() || pps.isWizardDefaults()))
            updateToDefaults(pps);
    }

    private void updateToDefaults(PerPlayerSettings pps) {
        if (pps.isMasterDefaults() && this.intelligence == SkullIntelligence.MASTER && !(this instanceof MobileSkull)) {
            if (!this.skullSkinData.equals(pps.getMasterSkinName())) {
                this.skullSkinData = pps.getMasterSkinName();
                threadedSetSkin(this.skullSkinData, null);
            }
            this.patrol = pps.isMasterPatrol();
            this.redstone = pps.isMasterRedstone();
            EntityType at = getAmmoTypeFromString(pps.getAmmoTypeName());
            this.ammoType = (at != null) ? at : EntityType.ARROW;
            if (at == null)
                setCommandFireArrow(false);
        }
        if (pps.isWizardDefaults() && this.intelligence == SkullIntelligence.WIZARD) {
            if (!this.skullSkinData.equals(pps.getMasterSkinName())) {
                this.skullSkinData = pps.getWizardSkinName();
                threadedSetSkin(this.skullSkinData, null);
            }
            this.patrol = pps.isWizardPatrol();
            this.redstone = pps.isWizardRedstone();
        }
    }

    public void reInitSkull() {
        this.firingSolution = null;
        this.target = null;
        PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        if (pps != null && pps.isPps()) {
            this.maxRange = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator).getMaxRange();
        } else if (ppg != null) {
            this.maxRange = ppg.getMaxRange();
        } else {
            this.maxRange = SkullTurret.MAX_RANGE;
        }
        this.directions.clear();
        this.fireRange = this.maxRange * this.intelligence.getFireRangeMultiplier();
        this.health = this.intelligence.getHealth();
        init(pps);
    }

    private BlockFace getSliceFace(int num) {
        switch (num) {
            case 0:
                return BlockFace.NORTH_WEST;
            case 1:
                return BlockFace.NORTH_NORTH_WEST;
            case 2:
            case 3:
                return BlockFace.NORTH;
            case 4:
                return BlockFace.NORTH_NORTH_EAST;
            case 5:
            case 6:
                return BlockFace.NORTH_EAST;
            case 7:
                return BlockFace.EAST_NORTH_EAST;
            case 8:
            case 9:
                return BlockFace.EAST;
            case 10:
                return BlockFace.EAST_SOUTH_EAST;
            case 11:
            case 12:
                return BlockFace.SOUTH_EAST;
            case 13:
                return BlockFace.SOUTH_SOUTH_EAST;
            case 14:
            case 15:
                return BlockFace.SOUTH;
            case 16:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 17:
            case 18:
                return BlockFace.SOUTH_WEST;
            case 19:
                return BlockFace.WEST_SOUTH_WEST;
            case 20:
            case 21:
                return BlockFace.WEST;
            case 22:
                return BlockFace.WEST_NORTH_WEST;
            case 23:
                return BlockFace.NORTH_WEST;
        }
        return BlockFace.UP;
    }

    private boolean sliceContains(SliceSection q, int pointX, int pointY, int pointZ) {
        Polygon poly = q.poly;
        Vector a = new Vector(poly.xpoints[0], pointY, poly.ypoints[0]);
        Vector b = new Vector(poly.xpoints[1], pointY, poly.ypoints[1]);
        Vector c = new Vector(poly.xpoints[2], pointY, poly.ypoints[2]);
        Vector p = new Vector(pointX, pointY, pointZ);
        Vector v0 = c.subtract(a);
        Vector v1 = b.subtract(a);
        Vector v2 = p.subtract(a);
        double dot00 = v0.dot(v0);
        double dot01 = v0.dot(v1);
        double dot02 = v0.dot(v2);
        double dot11 = v1.dot(v1);
        double dot12 = v1.dot(v2);
        double invDenom = 1.0D / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;
        return (u >= 0.0D && v >= 0.0D && u + v < 1.0D);
    }

    public void tick() {
        long cTime = System.currentTimeMillis();
        if (this.disabled)
            return;
        PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        this.deathTimer = cTime;
        if (SkullTurret.OFFLINE_PLAYERS) {
            Player player = Utils.getPlayerFromUUID(this.skullCreator);
            if (player == null)
                return;
        }
        if (doHealthLowDeath(cTime))
            return;
        if (findTarget()) {
            if (!SkullTurret.WATCH_ONLY)
                autoFire();
        } else if (cTime - this.coolDown > 3000L && this.patrol) {
            if (!this.redstone) {
                patrol();
            } else if (isRedstonePowered()) {
                patrol();
            }
        }
        if (SkullTurret.SKULLVFX)
            if (!this.redstone) {
                showVisualEffect();
            } else if (isRedstonePowered()) {
                showVisualEffect();
            }
        if (pps != null)
            updateToDefaults(pps);
    }

    private void rotate2(Entity e, boolean livingTest) {
        if (livingTest && e instanceof LivingEntity) {
            LivingEntity le = (LivingEntity) e;
            if (le == null || le.isDead() || !this.patrol)
                return;
        }
        Location entityLoc = e.getLocation();
        Skull skull = getSkull();
        RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        FacingRange fr = this.directions.get(r);
        if (fr != null) {
            BlockFace face = fr.face;
            if (!skull.getRotation().equals(face)) {
                skull.setRotation(face);
                skull.update(true);
                if (!this.skullSkinData.equals("") && skull.hasOwner() && !skull.getOwner().equals(this.skullSkinData)) {
                    threadedSetSkin(this.skullSkinData, null);
                } else {
                    skull.setSkullType(this.type);
                }
                skull.update(true);
                this.currentRotDirId = getCurrentRotation();
            }
        }
    }

    private void doAntiFireball(Entity e) {
        Vector vect = null;
        Location skullLoc = getAxisAlignment();
        Location targetLoc = e.getLocation();
        Vector direction = null;
        Vector toTarget = targetLoc.toVector().subtract(skullLoc.toVector());
        vect = new Vector(toTarget.getX(), toTarget.getY(), toTarget.getZ());
        direction = vect.multiply(1.1D);
        rotate2(e, false);
        int distance = getDistance(e.getLocation());
        float distMod = (distance - distance / 4);
        float velocity = (float) (distMod * 0.3D);
        Arrow arrow = this.world.spawnArrow(getAxisAlignment(), direction, velocity * 2.0F, 0.0F);
        arrow.setMetadata("SkullTurretantiFireball", new FixedMetadataValue(SkullTurret.plugin, this));
        if (this instanceof MobileSkull) {
            MobileSkull ms = (MobileSkull) this;
            int ammoAmount = ms.getAmmoAmount();
            ammoAmount--;
            ms.setAmmoAmount(ammoAmount);
        }
    }

    private boolean findTarget() {
        boolean inRange = false;
        boolean inFireRange = false;
        long currentTime = System.currentTimeMillis();
        if (SkullTurret.ALLOW_REDSTONE_DETECT && this.redstone && !isRedstonePowered()) {
            this.target = null;
            return false;
        }
        if (this.target == null) {
            Map<Integer, LivingEntity> rankedTargets = new HashMap<Integer, LivingEntity>();
            Entity entity = this.world.spawnEntity(getCenterPoint(), this.PINGER);
            if (entity == null)
                return false;
            List<Entity> targets = entity.getNearbyEntities(this.fireRange, this.fireRange, this.fireRange);
            entity.remove();
            for (Entity e : targets) {
                if ((e instanceof Player && SkullTurret.plugin.hasPermission((Player) e, "skullturret.ignoreme") && !SkullTurret.NO_PERMISSIONS) ||
                        isVanished(e))
                    continue;
                if (e.getType() == EntityType.FIREBALL && SkullTurret.DEBUG == 99) {
                    doAntiFireball(e);
                    return false;
                }
                if (e instanceof LivingEntity && (!(e instanceof Player) || !e.getUniqueId().equals(this.skullCreator) || SkullTurret.DEBUG == 5)) {
                    LivingEntity le = (LivingEntity) e;
                    if (SkullTurret.ONLY_BOW) {
                        if ((this.intelligence == SkullIntelligence.CRAZED || this.intelligence == SkullIntelligence.DEVIOUS) && ((
                                isPlayer(le) && !SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK) || (
                                !SkullTurret.TARGET_OWNED && (isLeashed(le) || le.getCustomName() != null)) ||
                                isAnimal(le) || isWaterMob(le) || isGolem(le) || isNPC(le)))
                            continue;
                        if (doBowTest(le, currentTime))
                            return true;
                        continue;
                    }
                    if (this.intelligence == SkullIntelligence.CRAZED || this.intelligence == SkullIntelligence.DEVIOUS) {
                        if ((!isPlayer(le) || SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK) && (
                                SkullTurret.TARGET_OWNED || (!isLeashed(le) && le.getCustomName() == null)) &&
                                !isAnimal(le) && !isWaterMob(le) && !isGolem(le) && !isNPC(le) &&
                                canRotate(le) && isInFireRange(le)) {
                            this.target = le;
                            rotate(this.target);
                            this.targetLockTimer = currentTime;
                            if (SkullTurret.SKULLSFX)
                                playSoundEffect();
                            return true;
                        }
                        continue;
                    }
                    List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretsTarget");
                    if (targetedMeta.size() > 0) {
                        if (doBowTest(le, currentTime))
                            return true;
                        continue;
                    }
                    if (!SkullTurret.TARGET_OWNED && (isLeashed(le) || le.getCustomName() != null))
                        continue;
                    if (le instanceof Player && ((Player) le).getGameMode() != GameMode.CREATIVE) {
                        if (this.playerFrenemies.size() != 0) {
                            Player pl = (Player) le;
                            String playerName = le.getName();
                            if (SkullTurret.plugin.hasDisguiseCraft())
                                playerName = getDisguiseName(le);
                            String iff = getFriendOrFoeFromName(playerName, pl);
                            if (iff != null) {
                                if (iff.equals("FRIEND"))
                                    continue;
                            } else {
                                if (this.friends.containsKey(getType(le)))
                                    continue;
                                if (!this.enemies.containsKey(getType(le)))
                                    continue;
                            }
                        } else {
                            if (this.friends.containsKey(getType(le)))
                                continue;
                            if (!this.enemies.containsKey(getType(le)))
                                continue;
                        }
                    } else {
                        if (!this.enemies.containsKey(getType(le)) || this.friends.containsKey(getType(le)))
                            continue;
                        if (!SkullTurret.TARGET_OWNED && (isLeashed(le) || le.getCustomName() != null))
                            continue;
                    }
                    Integer hash = Integer.valueOf(hashCode());
                    if (checkTargetMissedMeta(le, hash.toString(), currentTime) || checkWizardPotionMeta(le, hash.toString() + ",SkullTurretsWZIgnore", currentTime))
                        continue;
                    rankedTargets.put(Integer.valueOf(rateTarget(le)), le);
                }
            }
            if (rankedTargets.size() == 0)
                return false;
            for (int score = 100; score >= 0; score--) {
                LivingEntity le = rankedTargets.get(Integer.valueOf(score));
                if (le != null && canRotate(le) && isInFireRange(le)) {
                    this.target = le;
                    rotate(this.target);
                    this.targetLockTimer = currentTime;
                    if (SkullTurret.SKULLSFX)
                        playSoundEffect();
                    return true;
                }
            }
        } else {
            if (this.target.isDead() || !canRotate(this.target)) {
                this.target = null;
                return false;
            }
            inRange = isInRange(this.target);
            inFireRange = isInFireRange(this.target);
            if (inRange && inFireRange) {
                rotate(this.target);
                this.targetLockTimer = currentTime;
                return true;
            }
            if (inRange && !inFireRange) {
                if (currentTime - this.targetLockTimer > 2000L) {
                    this.target = null;
                    this.targetLockTimer = currentTime;
                    return false;
                }
                rotate(this.target);
            }
        }
        return false;
    }

    private boolean checkTargetMissedMeta(LivingEntity le, String metaName, long currentTime) {
        List<MetadataValue> meta = le.getMetadata(metaName);
        if (meta.size() > 0) {
            int hash = Integer.parseInt(metaName);
            long time = meta.get(0).asLong();
            if (hash != hashCode())
                return false;
            if (currentTime - time > 5000L) {
                le.removeMetadata(metaName, SkullTurret.plugin);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean checkWizardPotionMeta(LivingEntity le, String metaName, long currentTime) {
        List<MetadataValue> meta = le.getMetadata(metaName);
        if (meta.size() > 0) {
            String[] split = metaName.split(",");
            Integer hash = Integer.valueOf(Integer.parseInt(split[0]));
            if (hash.intValue() != hashCode())
                return false;
            long time = meta.get(0).asLong();
            if (currentTime - time > 5000L) {
                le.removeMetadata(metaName, SkullTurret.plugin);
                return false;
            }
            return true;
        }
        return false;
    }

    private String getFriendOrFoeFromName(String playerName, Player target) {
        for (Map.Entry<UUID, PlayerNamesFoF> pfre : this.playerFrenemies.entrySet()) {
            PlayerNamesFoF pnf = pfre.getValue();
            UUID uuid = pfre.getKey();
            String playerCurrentName = target.getName();
            if (playerCurrentName.equals(playerName) && uuid.equals(target.getUniqueId())) {
                pnf.setPlayerName(playerCurrentName);
                return pnf.getFriendOrEnemy();
            }
            if (playerName.equals(pnf.getPlayerName()))
                return pnf.getFriendOrEnemy();
        }
        return null;
    }

    private boolean isLeashed(LivingEntity le) {
        return le.isLeashed();
    }

    private boolean doBowTest(LivingEntity le, long currentTime) {
        List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretsTarget");
        if (targetedMeta.size() > 0)
            try {
                Object obj = targetedMeta.get(0).value();
                if (obj instanceof BowTargetInfo) {
                    BowTargetInfo playerInfo = (BowTargetInfo) obj;
                    UUID playerUUID = playerInfo.playerUUID;
                    long timer = playerInfo.timer;
                    if (System.currentTimeMillis() - timer > 60000L) {
                        le.removeMetadata("SkullTurretsTarget", SkullTurret.plugin);
                        return false;
                    }
                    if (playerUUID.equals(this.skullCreator) &&
                            le != null && !le.isDead() && canRotate(le) && isInFireRange(le)) {
                        this.target = le;
                        rotate(this.target);
                        this.targetLockTimer = currentTime;
                        if (SkullTurret.SKULLSFX)
                            playSoundEffect();
                        return true;
                    }
                }
            } catch (Exception ex) {
                le.removeMetadata("SkullTurretsTarget", SkullTurret.plugin);
                return false;
            }
        return false;
    }

    private boolean isVanished(Entity e) {
            return false;
    }

    private boolean isPlayer(LivingEntity le) {
        if (SkullTurret.plugin.hasDisguiseCraft() && le instanceof Player) {
        }
        return le instanceof Player;
    }

    private boolean isAnimal(LivingEntity le) {
        if (SkullTurret.plugin.hasDisguiseCraft() && le instanceof Player) {
        }
        return !(!(le instanceof org.bukkit.entity.Animals) && !(le instanceof org.bukkit.entity.Ambient));
    }

    private boolean isGolem(LivingEntity le) {
        if (SkullTurret.plugin.hasDisguiseCraft() && le instanceof Player) {
        }
        return le instanceof org.bukkit.entity.Golem;
    }

    private boolean isWaterMob(LivingEntity le) {
        return le instanceof org.bukkit.entity.WaterMob;
    }

    private boolean isNPC(LivingEntity le) {
        if (SkullTurret.plugin.hasDisguiseCraft() && le instanceof Player) {
            return false;
        }
        return le instanceof org.bukkit.entity.NPC;
    }

    private EntityType getType(LivingEntity le) {
        if (SkullTurret.plugin.hasDisguiseCraft() && le instanceof Player) {
        }
        return le.getType();
    }

    private int rateTarget(LivingEntity entity) {
        int score = 0;
        double health = entity.getHealth();
        if (health > 100.0D)
            health /= 5.0D;
        health *= 10.0D;
        int equipment = 0;
        EntityEquipment ee = entity.getEquipment();
        if (ee != null) {
            ItemStack helm = ee.getHelmet();
            if (helm != null) {
                equipment += 5;
                if (helm.getType().equals(Material.DIAMOND_HELMET)) {
                    equipment += 15;
                } else if (helm.getType().equals(Material.IRON_HELMET)) {
                    equipment += 5;
                }
            }
            ItemStack chest = ee.getChestplate();
            if (chest != null) {
                equipment += 5;
                if (chest.getType().equals(Material.DIAMOND_CHESTPLATE)) {
                    equipment += 15;
                } else if (chest.getType().equals(Material.IRON_CHESTPLATE)) {
                    equipment += 5;
                }
            }
            ItemStack legs = ee.getLeggings();
            if (legs != null) {
                equipment += 5;
                if (legs.getType().equals(Material.DIAMOND_LEGGINGS)) {
                    equipment += 15;
                } else if (legs.getType().equals(Material.IRON_LEGGINGS)) {
                    equipment += 5;
                }
            }
            ItemStack feet = ee.getBoots();
            if (feet != null) {
                equipment += 5;
                if (feet.getType().equals(Material.DIAMOND_BOOTS))
                    equipment += 10;
            }
            ItemStack weapon = ee.getItemInHand();
            if (weapon != null) {
                equipment += 10;
                if (weapon.getType().equals(Material.DIAMOND_SWORD)) {
                    equipment += 15;
                } else if (weapon.getType().equals(Material.DIAMOND_AXE)) {
                    equipment += 12;
                } else if (weapon.getType().equals(Material.BOW)) {
                    equipment += 10;
                } else if (weapon.getType().equals(Material.IRON_SWORD)) {
                    equipment += 8;
                } else if (weapon.getType().equals(Material.IRON_AXE)) {
                    equipment += 5;
                }
            }
        }
        equipment *= 20;
        Location entLoc = entity.getLocation();
        RangeCoord rc = new RangeCoord(this.world, entLoc.getBlockX(), this.centerY, entLoc.getBlockZ());
        FacingRange fr = this.directions.get(rc);
        int range = this.maxRange;
        if (fr != null) {
            range -= fr.rangeId;
            range *= 30;
        }
        int mobType = 0;
        if (SkullTurret.plugin.entities.containsKey(entity.getType()))
            mobType += SkullTurret.plugin.entities.get(entity.getType()).getRating();
        mobType *= 40;
        score = (int) ((range + health + mobType + equipment) / 100.0D);
        return score;
    }

    private boolean isInRange(LivingEntity e) {
        Location entityLoc = e.getLocation();
        int e_X = entityLoc.getBlockX();
        int e_Y = entityLoc.getBlockY();
        int e_Z = entityLoc.getBlockZ();
        int skull_X = this.skullBlock.getLocation().getBlockX();
        int skull_Y = this.skullBlock.getLocation().getBlockY();
        int skull_Z = this.skullBlock.getLocation().getBlockZ();
        if (skull_X > e_X) {
            int temp = skull_X;
            skull_X = e_X;
            e_X = temp;
        }
        if (skull_Y > e_Y) {
            int temp = skull_Y;
            skull_Y = e_Y;
            e_Y = temp;
        }
        if (skull_Z > e_Z) {
            int temp = skull_Z;
            skull_Z = e_Z;
            e_Z = temp;
        }
        return (e_X - skull_X < this.maxRange && e_Y - skull_Y < this.maxRange && e_Z - skull_Z < this.maxRange);
    }

    private boolean isInFireRange(LivingEntity potentialTarget) {
        Location entityLoc = potentialTarget.getLocation();
        int e_X = entityLoc.getBlockX();
        int e_Y = entityLoc.getBlockY();
        int e_Z = entityLoc.getBlockZ();
        int skull_X = this.skullBlock.getLocation().getBlockX();
        int skull_Y = this.skullBlock.getLocation().getBlockY();
        int skull_Z = this.skullBlock.getLocation().getBlockZ();
        if (skull_X > e_X) {
            int temp = skull_X;
            skull_X = e_X;
            e_X = temp;
        }
        if (skull_Y > e_Y) {
            int temp = skull_Y;
            skull_Y = e_Y;
            e_Y = temp;
        }
        if (skull_Z > e_Z) {
            int temp = skull_Z;
            skull_Z = e_Z;
            e_Z = temp;
        }
        if (!potentialTarget.isDead() && (e_X - skull_X) < this.fireRange && (e_Y - skull_Y) < this.fireRange && (e_Z - skull_Z) < this.fireRange) {
            this.firingSolution = getFiringSolution(potentialTarget);
            if (this.firingSolution == null)
                potentialTarget.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, Long.valueOf(System.currentTimeMillis())));
            return (this.firingSolution != null);
        }
        return false;
    }

    private Vector getFiringSolution(LivingEntity potentialTarget) {
        int ptDistance = getDistance(potentialTarget.getEyeLocation());
        Vector vect = null;
        Location skullLoc = getAxisAlignment();
        Location targetLoc = potentialTarget.getEyeLocation();
        Vector direction = null;
        Vector toTarget = targetLoc.toVector().subtract(skullLoc.toVector());
        vect = new Vector(toTarget.getX(), toTarget.getY(), toTarget.getZ());
        direction = vect.multiply(1.1D);
        Entity lineOfSigntEntity = this.world.spawnEntity(skullLoc, this.PINGER);
        if (lineOfSigntEntity == null)
            return null;
        boolean hasLineOfSignt = potentialTarget.hasLineOfSight(lineOfSigntEntity);
        lineOfSigntEntity.remove();
        if (!hasLineOfSignt) {
            if (SkullTurret.DEBUG == 1)
                System.out.println("No line of sight " + potentialTarget.getType().name() + " int = " + this.intelligence.getNormalName());
            return null;
        }
        if (SkullTurret.ALLOW_FRIENDLY_FIRE)
            return vect;
        try {
            Vector start = skullLoc.toVector();
            BlockIterator lineOfSight = new BlockIterator(this.world, start, direction, 0.0D, (int) this.fireRange);
            while (lineOfSight.hasNext()) {
                Block b = lineOfSight.next();
                int bDistance = getDistance(b.getLocation());
                if (b.getLocation().equals(getLocation()))
                    continue;
                if (bDistance > ptDistance)
                    return vect;
                Entity ping = this.world.spawnEntity(b.getLocation(), this.PINGER);
                if (ping == null)
                    return vect;
                List<Entity> around = ping.getNearbyEntities(0.1D, 0.1D, 0.1D);
                ping.remove();
                for (Entity e : around) {
                    if (e instanceof LivingEntity) {
                        if (e instanceof Player) {
                            String name = e.getName();
                            UUID playerUUID = e.getUniqueId();
                            if (name.equals(this.skullCreatorLastKnowName))
                                return null;
                            if (this.playerFrenemies.containsKey(playerUUID) && this.playerFrenemies.get(playerUUID).equals("FRIEND"))
                                return null;
                        }
                        if (this.friends.containsKey(e.getType()))
                            return null;
                    }
                }
            }
            return vect;
        } catch (Exception e) {
            return null;
        }
    }

    private Vector getSpecialVector() {
        Vector toTarget = this.target.getEyeLocation().toVector().subtract(getAxisAlignment().toVector());
        double speed = this.fireRange * 0.25D;
        double d0 = toTarget.getX();
        double d1 = toTarget.getY();
        double d2 = toTarget.getZ();
        float f2 = (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
        d0 /= f2;
        d1 /= f2;
        d2 /= f2;
        d0 *= speed;
        d1 *= speed;
        d2 *= speed;
        return new Vector(d0, d1, d2);
    }

    protected void autoFire() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.coolDown > this.intelligence.getCooldown()) {
            this.coolDown = currentTime;
            int distance = getDistance(this.target.getEyeLocation());
            float distMod = (distance - distance / 4);
            float velocity = (float) (distMod * 0.3D);
            Sound sound = Sound.ENTITY_ARROW_SHOOT;
            float pitch = 1.0F;
            double health = this.target.getHealth();
            if (this.intelligence == SkullIntelligence.WIZARD && (health > 5.0D || getType(this.target) == EntityType.ENDERMAN)) {
                Potion potion = getPotion();
                if (potion != null) {
                    if (getType(this.target) == EntityType.ENDERMAN)
                        this.target.setMetadata("SkullTurretEnder", new FixedMetadataValue(SkullTurret.plugin, potion));
                    if (SkullTurret.DEBUG == 3)
                        System.out.println("I chose a " + potion.getType().name() + " potion to fire.");
                    Entity e = this.world.spawnEntity(getAxisAlignment(), EntityType.SNOWBALL);
                    e.setMetadata("SkullTurretWizard", new FixedMetadataValue(SkullTurret.plugin, potion));
                    e.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                    this.firingSolution = getSpecialVector();
                    velocity = Float.valueOf(0.3675F + distMod / 100.0F).floatValue();
                    e.setVelocity(this.firingSolution.multiply(velocity));
                    if (SkullTurret.SKULLSFX)
                        this.world.playSound(getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5F, 2.0F);
                }
                return;
            }
            if (this.intelligence == SkullIntelligence.WIZARD && health <= 5.0D)
                this.fireArrow = true;
            EntityType ammo = this.ammoType;
            if (SkullTurret.USE_AMMO_CHESTS && !(this instanceof MobileSkull)) {
                ammo = getAmmo();
                if (ammo == null)
                    return;
            }
            if (SkullTurret.DEBUG == 1)
                ammo = EntityType.SNOWBALL;
            if (ammo == EntityType.ARROW) {
                Arrow arrow = this.world.spawnArrow(getAxisAlignment(), this.firingSolution, velocity, this.intelligence.getSpread());
                if (SkullTurret.SKULLSFX)
                    this.world.playSound(getLocation(), sound, 0.5F, pitch);
                if (this.fireArrow || this.commandFireArrow) {
                    arrow.setFireTicks(SkullTurret.FIRETICKS);
                    this.fireArrow = false;
                }
                if (this.intelligence != SkullIntelligence.CRAZED)
                    arrow.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                arrow.setMetadata("SkullTurretDAMAGE", new FixedMetadataValue(SkullTurret.plugin, this));
                if (!SkullTurret.MOB_DROPS)
                    arrow.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, this));
                if (this instanceof MobileSkull) {
                    MobileSkull ms = (MobileSkull) this;
                    int ammoAmount = ms.getAmmoAmount();
                    ammoAmount--;
                    ms.setAmmoAmount(ammoAmount);
                }
            } else {
                Entity e = this.world.spawnEntity(getAxisAlignment(), ammo);
                if (this.intelligence != SkullIntelligence.CRAZED)
                    e.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                if (!SkullTurret.MOB_DROPS)
                    e.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, this));
                e.setMetadata("SkullTurretDAMAGE", new FixedMetadataValue(SkullTurret.plugin, this));
                if (ammo == EntityType.SNOWBALL) {
                    this.firingSolution = getSpecialVector();
                    velocity = Float.valueOf(0.3675F + distMod / 100.0F).floatValue();
                    sound = Sound.ENTITY_CREEPER_HURT;
                    pitch = 0.55F;
                } else if (ammo == EntityType.SMALL_FIREBALL) {
                    Fireball fb = (Fireball) e;
                    fb.setIsIncendiary(SkullTurret.INCENDIARY_FIRECHARGE);
                    fb.setDirection(this.firingSolution);
                    velocity = 0.2F;
                    sound = Sound.ENTITY_GHAST_SHOOT;
                } else if (ammo == EntityType.WITHER_SKULL) {
                    WitherSkull ws = (WitherSkull) e;
                    ws.setDirection(this.firingSolution);
                    velocity = Float.valueOf(0.3675F + distMod / 300.0F).floatValue();
                    sound = Sound.ENTITY_GHAST_SHOOT;
                    pitch = 0.25F;
                }
                if (SkullTurret.SKULLSFX)
                    this.world.playSound(getLocation(), sound, 0.5F, pitch);
                e.setVelocity(this.firingSolution.multiply(velocity));
            }
        }
    }

    private Location getAxisAlignment() {
        double x = this.centerX;
        double y = this.centerY;
        double z = this.centerZ;
        switch (getSkull().getRotation()) {
            case NORTH:
                return new Location(this.world, x + 0.5D, y + 0.2D, z + 0.1D);
            case NORTH_NORTH_EAST:
                return new Location(this.world, x + 0.67D, y + 0.2D, z + 0.1D);
            case NORTH_EAST:
                return new Location(this.world, x + 0.8D, y + 0.2D, z + 0.24D);
            case EAST_NORTH_EAST:
                return new Location(this.world, x + 0.9D, y + 0.2D, z + 0.35D);
            case EAST:
                return new Location(this.world, x + 0.91D, y + 0.2D, z + 0.5D);
            case EAST_SOUTH_EAST:
                return new Location(this.world, x + 0.89D, y + 0.2D, z + 0.65D);
            case SOUTH_EAST:
                return new Location(this.world, x + 0.8D, y + 0.2D, z + 0.8D);
            case SOUTH_SOUTH_EAST:
                return new Location(this.world, x + 0.68D, y + 0.2D, z + 0.87D);
            case SOUTH:
                return new Location(this.world, x + 0.5D, y + 0.2D, z + 0.93D);
            case SOUTH_SOUTH_WEST:
                return new Location(this.world, x + 0.374D, y + 0.2D, z + 0.9D);
            case SOUTH_WEST:
                return new Location(this.world, x + 0.23D, y + 0.2D, z + 0.8D);
            case WEST_SOUTH_WEST:
                return new Location(this.world, x + 0.13D, y + 0.2D, z + 0.67D);
            case WEST:
                return new Location(this.world, x + 0.13D, y + 0.2D, z + 0.53D);
            case WEST_NORTH_WEST:
                return new Location(this.world, x + 0.15D, y + 0.2D, z + 0.38D);
            case NORTH_WEST:
                return new Location(this.world, x + 0.24D, y + 0.2D, z + 0.24D);
            case NORTH_NORTH_WEST:
                return new Location(this.world, x + 0.35D, y + 0.2D, z + 0.15D);
        }
        return new Location(this.world, x, y + 0.2D, z);
    }

    private int getCurrentRotation() {
        BlockFace face = getSkull().getRotation();
        switch (face) {
            case NORTH:
                return 0;
            case NORTH_NORTH_EAST:
                return 1;
            case NORTH_EAST:
                return 2;
            case EAST_NORTH_EAST:
                return 3;
            case EAST:
                return 4;
            case EAST_SOUTH_EAST:
                return 5;
            case SOUTH_EAST:
                return 6;
            case SOUTH_SOUTH_EAST:
                return 7;
            case SOUTH:
                return 8;
            case SOUTH_SOUTH_WEST:
                return 9;
            case SOUTH_WEST:
                return 10;
            case WEST_SOUTH_WEST:
                return 11;
            case WEST:
                return 12;
            case WEST_NORTH_WEST:
                return 13;
            case NORTH_WEST:
                return 14;
        }
        return 15;
    }

    private void patrol() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.rotUpdateTimer > SkullTurret.PATROL_TIME) {
            this.rotUpdateTimer = currentTime;
            Skull skull = getSkull();
            if (currentTime - this.rotTimer > 30000L) {
                boolean clockWise = (new Random()).nextBoolean();
                if (clockWise) {
                    this.dirMod = 1;
                } else {
                    this.dirMod = -1;
                }
                this.rotTimer = currentTime;
            }
            int lookAheadDirId = this.currentRotDirId + this.dirMod;
            if (lookAheadDirId > 15)
                lookAheadDirId = 0;
            BlockFace nextRotDir = getNewRotDir(lookAheadDirId);
            if (skull.getBlock().getRelative(nextRotDir).getType() != Material.AIR) {
                this.dirMod = -this.dirMod;
                this.rotTimer = currentTime;
            }
            skull.setRotation(getNewRotDir(this.currentRotDirId));
            this.currentRotDirId += this.dirMod;
            if (this.currentRotDirId > 15 && this.dirMod > 0) {
                this.currentRotDirId = 0;
            } else if (this.currentRotDirId < 0 && this.dirMod < 0) {
                this.currentRotDirId = 15;
            }
            skull.update();
        }
    }

    public void setSkullRotation(BlockFace face) {
        Skull skull = getSkull();
        skull.setRotation(face);
        skull.update();
        this.currentRotDirId = getRotationId(face);
    }

    private BlockFace getNewRotDir(int dirId) {
        switch (dirId) {
            case 0:
                return BlockFace.NORTH_NORTH_EAST;
            case 1:
                return BlockFace.NORTH_EAST;
            case 2:
                return BlockFace.EAST_NORTH_EAST;
            case 3:
                return BlockFace.EAST;
            case 4:
                return BlockFace.EAST_SOUTH_EAST;
            case 5:
                return BlockFace.SOUTH_EAST;
            case 6:
                return BlockFace.SOUTH_SOUTH_EAST;
            case 7:
                return BlockFace.SOUTH;
            case 8:
                return BlockFace.SOUTH_SOUTH_WEST;
            case 9:
                return BlockFace.SOUTH_WEST;
            case 10:
                return BlockFace.WEST_SOUTH_WEST;
            case 11:
                return BlockFace.WEST;
            case 12:
                return BlockFace.WEST_NORTH_WEST;
            case 13:
                return BlockFace.NORTH_WEST;
            case 14:
                return BlockFace.NORTH_NORTH_WEST;
            case 15:
                return BlockFace.NORTH;
        }
        return BlockFace.NORTH;
    }

    private boolean canRotate(LivingEntity le) {
        if (le == null || le.isDead())
            return false;
        Location entityLoc = le.getLocation();
        Skull skull = getSkull();
        RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        FacingRange fr = this.directions.get(r);
        if (fr != null) {
            BlockFace newDirection = fr.face;
            if (skull.getRotation().equals(newDirection))
                return true;
            int newRot = getRotationId(newDirection);
            int startRot = getCurrentRotation();
            int dir = 1;
            if (Math.abs(startRot - newRot) > 8)
                dir = -1;
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 16; i++) {
                    int lookAheadDirId = startRot + dir;
                    if (lookAheadDirId > 15)
                        lookAheadDirId = 0;
                    BlockFace nextRotDir = getNewRotDir(lookAheadDirId);
                    if (skull.getBlock().getRelative(nextRotDir).getType() != Material.AIR)
                        break;
                    if (newRot == lookAheadDirId)
                        return true;
                    startRot += dir;
                    if (startRot > 15 && dir > 0) {
                        startRot = 0;
                    } else if (startRot < 0 && dir < 0) {
                        startRot = 15;
                    }
                }
                dir = -dir;
            }
        }
        return false;
    }

    protected int getRotationId(BlockFace direction) {
        switch (direction) {
            case NORTH:
                return 0;
            case NORTH_NORTH_EAST:
                return 1;
            case NORTH_EAST:
                return 2;
            case EAST_NORTH_EAST:
                return 3;
            case EAST:
                return 4;
            case EAST_SOUTH_EAST:
                return 5;
            case SOUTH_EAST:
                return 6;
            case SOUTH_SOUTH_EAST:
                return 7;
            case SOUTH:
                return 8;
            case SOUTH_SOUTH_WEST:
                return 9;
            case SOUTH_WEST:
                return 10;
            case WEST_SOUTH_WEST:
                return 11;
            case WEST:
                return 12;
            case WEST_NORTH_WEST:
                return 13;
            case NORTH_WEST:
                return 14;
        }
        return 15;
    }

    private void rotate(LivingEntity le) {
        if (le == null || le.isDead() || !this.patrol)
            return;
        Location entityLoc = le.getLocation();
        Skull skull = getSkull();
        RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        FacingRange fr = this.directions.get(r);
        if (fr != null) {
            BlockFace face = fr.face;
            if (!skull.getRotation().equals(face)) {
                skull.setRotation(face);
                skull.update(true);
                if (!this.skullSkinData.equals("") && skull.hasOwner() && !skull.getOwner().equals(this.skullSkinData)) {
                    threadedSetSkin(this.skullSkinData, null);
                } else {
                    skull.setSkullType(this.type);
                }
                skull.update(true);
                this.currentRotDirId = getCurrentRotation();
            }
        }
    }

    private void showVisualEffect() {
        if (this.intelligence == SkullIntelligence.CRAZED)
            return;
        this.world.playEffect(getCenterPoint(), this.intelligence.getEffect(), 4, (int) this.fireRange);
    }

    private void deathSmokeFX() {
        if (SkullTurret.SKULLVFX)
            for (int i = 0; i < 9; i++) {
                this.world.playEffect(getCenterPoint(), Effect.SMOKE, i, 10);
                getSkullBlock().getState().update();
            }
    }

    private void playSoundEffect() {
    }

    private boolean doHealthLowDeath(long cTime) {
        if (this.health == 0.0D || this.dying) {
            if (!SkullTurret.ALLOW_DAMAGED_SKULL_DESTRUCT)
                this.destructTimer = cTime;
            doDeathRattle(this.destructTimer, cTime, SkullTurret.DESTRUCT_TIMER);
            this.recoveryTimer = cTime;
            return true;
        }
        if (!this.dying) {
            this.destructTimer = cTime;
            if (this.health < this.intelligence.getHealth() && cTime - this.recoveryTimer > SkullTurret.SKULL_DAMAGE_RECOVERY_TIME) {
                this.health = this.intelligence.getHealth();
                this.recoveryTimer = cTime;
            }
        }
        return false;
    }

    public boolean doDeathRattle(long dt, long cTime, long timerAmount) {
        if (cTime - dt < timerAmount) {
            deathSmokeFX();
        } else {
            this.dead = true;
        }
        return false;
    }

    public void die() {
        this.dead = true;
    }

    public void destruct() {
        if (SkullTurret.SKULLVFX) {
            Location loc = getCenterPoint();
            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();
            World world = loc.getWorld();
            world.createExplosion(x, y, z, 0.0F, false, false);
        }
        Utils.clearBlock(getSkull().getBlock());
    }

    public ItemStack getInfoBook(boolean brokeDrop, Player writer) {
        int MAXPAGELENGTH = 182;
        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) writtenBook.getItemMeta();
        meta.setTitle(this.intelligence.getNormalName() + " Skull Book.");
        meta.setAuthor("§a" + (brokeDrop ? "Unknown" : writer.getName()));
        StringBuilder infoText = new StringBuilder();
        infoText.append("§n");
        infoText.append(brokeDrop ? "BROKEN" : this.intelligence.getNormalName());
        infoText.append(" Skull Info§r\n\n");
        infoText.append("Owner: ");
        infoText.append(brokeDrop ? "Unknown" : (writer.getUniqueId().equals(this.skullCreator) ? writer.getName() : this.skullCreator.toString()));
        infoText.append("\n§0World: ");
        infoText.append(brokeDrop ? "Unknown" : this.world.getName());
        infoText.append("\n§0Loc: ");
        infoText.append(brokeDrop ? "?" : Integer.toString(getLocation().getBlockX()));
        infoText.append(", ");
        infoText.append(brokeDrop ? "?" : Integer.toString(getLocation().getBlockY()));
        infoText.append(", ");
        infoText.append(brokeDrop ? "?" : Integer.toString(getLocation().getBlockZ()));
        infoText.append("\n");
        infoText.append("Skin: ");
        infoText.append((this.type == SkullType.SKELETON) ? "-" : this.skullSkinData);
        infoText.append("*\nRange: ");
        infoText.append(this.maxRange);
        infoText.append("\nFireRange: ");
        infoText.append((float) this.fireRange);
        infoText.append("\n|");
        meta.addPage(infoText.toString());
        StringBuilder enemiesString = new StringBuilder();
        enemiesString.append("§nMobs Skull Attacks:§r\n\n");
        boolean hasEnemies = false;
        for (EntityType et : this.enemies.keySet()) {
            if (enemiesString.length() + et.name().length() > MAXPAGELENGTH) {
                meta.addPage(enemiesString.toString());
                enemiesString = new StringBuilder();
                enemiesString.append(et.name());
                enemiesString.append(", ");
            }
            enemiesString.append(et.name());
            enemiesString.append(", ");
            hasEnemies = true;
        }
        int index = enemiesString.lastIndexOf(",");
        if (index != -1)
            enemiesString.replace(index, enemiesString.length() - 1, ".|");
        if (!hasEnemies)
            enemiesString.append("\n#|");
        meta.addPage(enemiesString.toString());
        StringBuilder friendsString = new StringBuilder();
        friendsString.append("§nMobs Skull Ignores:§r\n\n");
        boolean hasFriends = false;
        for (EntityType et : this.friends.keySet()) {
            if (friendsString.length() + et.name().length() > MAXPAGELENGTH) {
                meta.addPage(friendsString.toString());
                friendsString = new StringBuilder();
                friendsString.append(et.name());
                friendsString.append(", ");
            }
            friendsString.append(et.name());
            friendsString.append(", ");
            hasFriends = true;
        }
        index = friendsString.lastIndexOf(",");
        if (index != -1)
            friendsString.replace(index, friendsString.length() - 1, ".|");
        if (!hasFriends)
            friendsString.append("\n#|");
        meta.addPage(friendsString.toString());
        StringBuilder enemyPlayerNames = new StringBuilder();
        StringBuilder enemyPlayerUUID = new StringBuilder();
        StringBuilder friendPlayerNames = new StringBuilder();
        StringBuilder friendPlayerUUID = new StringBuilder();
        List<String> epnPages = new ArrayList<String>();
        List<String> epUUIDPages = new ArrayList<String>();
        List<String> fpnPages = new ArrayList<String>();
        List<String> fpUUIDPages = new ArrayList<String>();
        enemyPlayerNames.append("§nPlayers Skull Attacks:§r\n\n");
        enemyPlayerUUID.append("§nUUID Skull Attacks:§r\n\n");
        friendPlayerNames.append("§nPlayers Skull Ignores:§r\n\n");
        friendPlayerUUID.append("§nUUID Skull Ignores:§r\n\n");
        PlayerNamesFoF pfof = null;
        String playerName = "";
        String playerUUID = "";
        for (Map.Entry<UUID, PlayerNamesFoF> playerTargets : this.playerFrenemies.entrySet()) {
            pfof = playerTargets.getValue();
            playerName = pfof.getPlayerName();
            playerUUID = playerTargets.getKey().toString();
            if (pfof.getFriendOrEnemy().equals("ENEMY")) {
                if (enemyPlayerNames.length() + playerName.length() > MAXPAGELENGTH) {
                    epnPages.add(enemyPlayerNames.toString());
                    enemyPlayerNames = new StringBuilder();
                    enemyPlayerNames.append(playerName);
                    enemyPlayerNames.append(", ");
                } else {
                    enemyPlayerNames.append(playerName);
                    enemyPlayerNames.append(", ");
                }
                if (enemyPlayerUUID.length() + playerUUID.length() > MAXPAGELENGTH) {
                    epUUIDPages.add(enemyPlayerUUID.toString());
                    enemyPlayerUUID = new StringBuilder();
                    enemyPlayerUUID.append(playerUUID);
                    enemyPlayerUUID.append(", ");
                    continue;
                }
                enemyPlayerUUID.append(playerUUID);
                enemyPlayerUUID.append(", ");
                continue;
            }
            if (pfof.getFriendOrEnemy().equals("FRIEND")) {
                if (friendPlayerNames.length() + playerName.length() > MAXPAGELENGTH) {
                    fpnPages.add(friendPlayerNames.toString());
                    friendPlayerNames = new StringBuilder();
                    friendPlayerNames.append(playerName);
                    friendPlayerNames.append(", ");
                } else {
                    friendPlayerNames.append(playerName);
                    friendPlayerNames.append(", ");
                }
                if (friendPlayerUUID.length() + playerUUID.length() > MAXPAGELENGTH) {
                    fpUUIDPages.add(friendPlayerUUID.toString());
                    friendPlayerUUID = new StringBuilder();
                    friendPlayerUUID.append(playerUUID);
                    friendPlayerUUID.append(", ");
                    continue;
                }
                friendPlayerUUID.append(playerUUID);
                friendPlayerUUID.append(", ");
            }
        }
        index = enemyPlayerNames.lastIndexOf(",");
        if (index != -1)
            enemyPlayerNames.replace(index, enemyPlayerNames.length() - 1, ".|");
        index = enemyPlayerUUID.lastIndexOf(",");
        if (index != -1)
            enemyPlayerUUID.replace(index, enemyPlayerUUID.length() - 1, ".|");
        index = friendPlayerNames.lastIndexOf(",");
        if (index != -1)
            friendPlayerNames.replace(index, friendPlayerNames.length() - 1, ".|");
        index = friendPlayerUUID.lastIndexOf(",");
        if (index != -1)
            friendPlayerUUID.replace(index, friendPlayerUUID.length() - 1, ".|");
        boolean hasPlayerEnemy = true;
        boolean hasPlayerFriend = true;
        if (enemyPlayerNames.length() == 28) {
            enemyPlayerNames.append("\n#|");
            hasPlayerEnemy = false;
        }
        if (enemyPlayerUUID.length() == 25)
            enemyPlayerUUID.append("\n#|");
        if (friendPlayerNames.length() == 28) {
            friendPlayerNames.append("\n#|");
            hasPlayerFriend = false;
        }
        if (friendPlayerUUID.length() == 25)
            friendPlayerUUID.append("\n#|");
        for (String page : epnPages) {
            meta.addPage(page);
        }
        meta.addPage(enemyPlayerNames.toString());
        for (String page : fpnPages) {
            meta.addPage(page);
        }
        meta.addPage(friendPlayerNames.toString());
        for (String page : epUUIDPages) {
            meta.addPage(page);
        }
        meta.addPage(enemyPlayerUUID.toString());
        for (String page : fpUUIDPages) {
            meta.addPage(page);
        }
        meta.addPage(friendPlayerUUID.toString());
        List<String> lore = new ArrayList<String>();
        lore.add(this.intelligence.getNormalName() + " Skull Turret");
        lore.add("Skull Knowledge Book");
        meta.setLore(lore);
        writtenBook.setItemMeta(meta);
        if (!hasEnemies && !hasFriends && !hasPlayerEnemy && !hasPlayerFriend && brokeDrop)
            return null;
        return writtenBook;
    }

    public void parseBook(ItemStack writtenBook, Player player) {
        if (writtenBook != null && writtenBook.getType() == Material.WRITTEN_BOOK) {
            if (!writtenBook.hasItemMeta())
                return;
            BookMeta meta = (BookMeta) writtenBook.getItemMeta();
            if (!meta.hasLore())
                return;
            if (meta.getLore().size() < 2)
                return;
            if (meta.getLore().get(1).equals("Skull Knowledge Book"))
                try {
                    String page1 = meta.getPage(1);
                    String[] split = page1.split("\\*");
                    if (!player.getUniqueId().equals(this.skullCreator) && !SkullTurret.plugin.hasPermission(player, "skullturret.admin")) {
                        player.sendMessage(Utils.parseText(Utils.getLocalization("notSkullOwner")));
                        return;
                    }
                    List<String> pages = meta.getPages();
                    StringBuilder sb = new StringBuilder();
                    for (String page : pages)
                        sb.append(page);
                    split = sb.toString().split(":");
                    String skin = split[4].trim().split("\\*")[0];
                    if (this.intelligence.canSkinChange())
                        if (!skin.equals("-")) {
                            threadedSetSkin(skin, null);
                        } else {
                            setSkin("");
                        }
                    split = sb.toString().split("\\|");
                    this.enemies.clear();
                    String mobEnemiesString = split[1].split(":")[1];
                    if (!mobEnemiesString.contains("#")) {
                        int mesLength = mobEnemiesString.length();
                        mobEnemiesString = mobEnemiesString.substring(4, mesLength - 1).trim();
                        String[] enemyStringSplit = mobEnemiesString.split(",");
                        byte b;
                        int i;
                        String[] arrayOfString1;
                        for (i = (arrayOfString1 = enemyStringSplit).length, b = 0; b < i; ) {
                            String es = arrayOfString1[b];
                            EntityType type = EntityType.valueOf(es.trim());
                            if (SkullTurret.plugin.hasPermission(player, "skullturret.target." + type.name().toLowerCase()) || customTypePermission(type, player))
                                this.enemies.put(type, type);
                            b++;
                        }
                    }
                    this.friends.clear();
                    String mobFriendsString = split[2].split(":")[1];
                    if (!mobFriendsString.contains("#")) {
                        int mfsLength = mobFriendsString.length();
                        mobFriendsString = mobFriendsString.substring(4, mfsLength - 1).trim();
                        String[] friendStringSplit = mobFriendsString.split(",");
                        byte b;
                        int i;
                        String[] arrayOfString1;
                        for (i = (arrayOfString1 = friendStringSplit).length, b = 0; b < i; ) {
                            String es = arrayOfString1[b];
                            EntityType type = EntityType.valueOf(es.trim());
                            if (SkullTurret.plugin.hasPermission(player, "skullturret.target." + type.name().toLowerCase()) || customTypePermission(type, player))
                                this.friends.put(type, type);
                            b++;
                        }
                    }
                    if (!SkullTurret.plugin.hasPermission(player, "skullturret.target.player"))
                        return;
                    this.playerFrenemies.clear();
                    String playerEnemiesNameString = split[3].split(":")[1];
                    String playerEnemiesUUIDString = split[5].split(":")[1];
                    if (!playerEnemiesNameString.contains("#")) {
                        int pens = playerEnemiesNameString.length();
                        int peus = playerEnemiesUUIDString.length();
                        playerEnemiesNameString = playerEnemiesNameString.substring(4, pens - 1).trim();
                        playerEnemiesUUIDString = playerEnemiesUUIDString.substring(4, peus - 1).trim();
                        String[] playerEnemiesNameStringSplit = playerEnemiesNameString.split(",");
                        String[] playerEnemiesUUIDStringSplit = playerEnemiesUUIDString.split(",");
                        int count = 0;
                        byte b;
                        int i;
                        String[] arrayOfString1;
                        for (i = (arrayOfString1 = playerEnemiesNameStringSplit).length, b = 0; b < i; ) {
                            String name = arrayOfString1[b];
                            PlayerNamesFoF pnfof = new PlayerNamesFoF(name.trim(), "ENEMY");
                            UUID playerUUID = UUID.fromString(playerEnemiesUUIDStringSplit[count].trim());
                            this.playerFrenemies.put(playerUUID, pnfof);
                            count++;
                            b++;
                        }
                    }
                    String playerFriendsNameString = split[4].split(":")[1];
                    String playerFriendsUUIDString = split[6].split(":")[1];
                    if (!playerFriendsNameString.contains("#")) {
                        int pfns = playerFriendsNameString.length();
                        int pfus = playerFriendsUUIDString.length();
                        playerFriendsNameString = playerFriendsNameString.substring(4, pfns - 1).trim();
                        playerFriendsUUIDString = playerFriendsUUIDString.substring(4, pfus - 1).trim();
                        String[] playerFriendsNameStringSplit = playerFriendsNameString.split(",");
                        String[] playerFriendsUUIDStringSplit = playerFriendsUUIDString.split(",");
                        int count = 0;
                        byte b;
                        int i;
                        String[] arrayOfString1;
                        for (i = (arrayOfString1 = playerFriendsNameStringSplit).length, b = 0; b < i; ) {
                            String name = arrayOfString1[b];
                            PlayerNamesFoF pnfof = new PlayerNamesFoF(name.trim(), "FRIEND");
                            UUID playerUUID = UUID.fromString(playerFriendsUUIDStringSplit[count].trim());
                            this.playerFrenemies.put(playerUUID, pnfof);
                            count++;
                            b++;
                        }
                    }
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdated")));
                    return;
                } catch (Exception e) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("updateSkullEr")));
                    e.printStackTrace();
                    return;
                }
            player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdateFail")));
        } else {
            player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdateFail")));
        }
    }

    private boolean customTypePermission(EntityType ent, Player player) {
        for (Map.Entry<String, List<String>> en : SkullTurret.plugin.customNames.entrySet()) {
            String permission = "skullturret.target." + en.getKey().toLowerCase();
            for (String name : en.getValue()) {
                if (ent == EntityType.valueOf(name))
                    return SkullTurret.plugin.hasPermission(player, permission);
            }
        }
        return false;
    }

    private Potion getPotion() {
        Block bottomBlock = getLowestBlock();
        Block northBlock = bottomBlock.getRelative(BlockFace.NORTH);
        Block southBlock = bottomBlock.getRelative(BlockFace.SOUTH);
        Block westBlock = bottomBlock.getRelative(BlockFace.WEST);
        Block eastBlock = bottomBlock.getRelative(BlockFace.EAST);
        List<Chest> ammoBox = new ArrayList<Chest>();
        if (bottomBlock.getType() == Material.CHEST || bottomBlock.getType() == Material.TRAPPED_CHEST)
            ammoBox.add((Chest) bottomBlock.getState());
        if (northBlock.getType() == Material.CHEST || northBlock.getType() == Material.TRAPPED_CHEST)
            ammoBox.add((Chest) northBlock.getState());
        if (southBlock.getType() == Material.CHEST || southBlock.getType() == Material.TRAPPED_CHEST)
            ammoBox.add((Chest) southBlock.getState());
        if (westBlock.getType() == Material.CHEST || westBlock.getType() == Material.TRAPPED_CHEST)
            ammoBox.add((Chest) westBlock.getState());
        if (eastBlock.getType() == Material.CHEST || eastBlock.getType() == Material.TRAPPED_CHEST)
            ammoBox.add((Chest) eastBlock.getState());
        Potion ammo = pickPotion(ammoBox);
        if (ammo != null)
            return ammo;
        Integer hash = Integer.valueOf(hashCode());
        this.target.setMetadata(hash.toString() + ",SkullTurretsWZIgnore", new FixedMetadataValue(SkullTurret.plugin, Long.valueOf(System.currentTimeMillis())));
        this.target = null;
        return null;
    }

    private Potion pickPotion(List<Chest> ammoBox) {
        if (this.target == null)
            return null;
        Collection<PotionEffect> effects = this.target.getActivePotionEffects();
        Potion ammo = null;
        ItemStack[] items = null;
        List<Potion> randomPotion = new ArrayList<Potion>();
        for (Chest c : ammoBox) {
            items = c.getInventory().getContents();
            byte b;
            int i;
            ItemStack[] arrayOfItemStack;
            for (i = (arrayOfItemStack = items).length, b = 0; b < i; ) {
                ItemStack item = arrayOfItemStack[b];
                if (item != null &&
                        item.getType() == Material.POTION)
                    try {
                        Potion potion = Potion.fromItemStack(unRevert(item));
                        if (potion.isSplash() && isValidPotion(potion)) {
                            boolean found = false;
                            for (PotionEffect e : potion.getEffects()) {
                                if (effects == null)
                                    break;
                                for (PotionEffect e2 : effects) {
                                    found = e.getType().equals(e2.getType());
                                    if (found)
                                        break;
                                }
                                if (found)
                                    break;
                            }
                            if (!found)
                                randomPotion.add(potion);
                        }
                    } catch (Exception e) {
                        if (SkullTurret.DEBUG == 3) {
                            System.out.println("Potion Choice Error");
                            e.printStackTrace();
                        }
                    }
                b++;
            }
        }
        if (randomPotion.size() > 0) {
            int random = (new Random()).nextInt(randomPotion.size());
            return randomPotion.get(random);
        }
        return ammo;
    }

    private ItemStack unRevert(ItemStack item) {
        int damage = item.getDurability();
        if (damage == 16453 || damage == 16460) {
            ItemStack deRevert = item.clone();
            deRevert.setDurability((short) (damage - 64));
            return deRevert;
        }
        return item;
    }

    private boolean isValidPotion(Potion potion) {
        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect ef : effects) {
            if (isValidPotionEffect(ef.getType()))
                return true;
        }
        return false;
    }

    private boolean canUsePoison() {
        if (this.target == null)
            return false;
        EntityType type = this.target.getType();
        if (SkullTurret.plugin.entities.containsKey(type)) {
            EntitySettings es = SkullTurret.plugin.entities.get(type);
            return es.canPoison();
        }
        return false;
    }

    private boolean mustUseHeal() {
        if (this.target == null)
            return false;
        EntityType type = this.target.getType();
        if (SkullTurret.plugin.entities.containsKey(type)) {
            EntitySettings es = SkullTurret.plugin.entities.get(type);
            return es.mustHeal();
        }
        return false;
    }

    private boolean isEndermen() {
        return (this.target != null && this.target.getType() == EntityType.ENDERMAN);
    }

    private boolean isValidPotionEffect(PotionEffectType type) {
        if (type.equals(PotionEffectType.POISON) && canUsePoison() && !isEndermen())
            return true;
        if (type.equals(PotionEffectType.SLOW) && !isEndermen())
            return true;
        if (type.equals(PotionEffectType.WEAKNESS) && !isEndermen())
            return true;
        if (type.equals(PotionEffectType.HEAL) && mustUseHeal())
            return true;
      return type.equals(PotionEffectType.HARM) && (!mustUseHeal() || isEndermen());
    }

    private EntityType getAmmo() {
        Block bottomBlock = getLowestBlock();
        Block northBlock = bottomBlock.getRelative(BlockFace.NORTH);
        Block southBlock = bottomBlock.getRelative(BlockFace.SOUTH);
        Block westBlock = bottomBlock.getRelative(BlockFace.WEST);
        Block eastBlock = bottomBlock.getRelative(BlockFace.EAST);
        Chest ammoBox = null;
        if (bottomBlock.getType() == Material.CHEST || bottomBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) bottomBlock.getState();
            ItemStack ammo = withdrawAmmo(ammoBox);
            if (ammo != null)
                return getEntityType(ammo);
        }
        if (northBlock.getType() == Material.CHEST || northBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) northBlock.getState();
            ItemStack ammo = withdrawAmmo(ammoBox);
            if (ammo != null)
                return getEntityType(ammo);
        }
        if (southBlock.getType() == Material.CHEST || southBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) southBlock.getState();
            ItemStack ammo = withdrawAmmo(ammoBox);
            if (ammo != null)
                return getEntityType(ammo);
        }
        if (westBlock.getType() == Material.CHEST || westBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) westBlock.getState();
            ItemStack ammo = withdrawAmmo(ammoBox);
            if (ammo != null)
                return getEntityType(ammo);
        }
        if (eastBlock.getType() == Material.CHEST || eastBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) eastBlock.getState();
            ItemStack ammo = withdrawAmmo(ammoBox);
            if (ammo != null)
                return getEntityType(ammo);
        }
        return null;
    }

    private ItemStack withdrawAmmo(Chest ammoBox) {
        ItemStack ammo = null;
        boolean foundAmmo = false;
        boolean foundInfinite = false;
        for (ItemStack item : SkullTurret.plugin.ammoList) {
            if (!foundAmmo && ammoBox.getInventory().containsAtLeast(item, 1)) {
                ammo = item;
                foundAmmo = true;
            }
            if (foundAmmo) {
                if (ammo.getType() == Material.ARROW) {
                    byte b;
                    int i;
                    ItemStack[] arrayOfItemStack;
                    for (i = (arrayOfItemStack = ammoBox.getInventory().getContents()).length, b = 0; b < i; ) {
                        ItemStack bow = arrayOfItemStack[b];
                        if (bow != null && bow.getType() == Material.BOW) {
                            this.fireArrow = (SkullTurret.ALLOW_FIREBOW && bow.containsEnchantment(Enchantment.ARROW_FIRE));
                            foundInfinite = (SkullTurret.ALLOW_INFINITE_BOW && bow.containsEnchantment(Enchantment.ARROW_INFINITE));
                            if (!this.commandFireArrow || foundInfinite) {
                                short dur = bow.getDurability();
                                if (dur + SkullTurret.BOW_DUR >= 385) {
                                    ammoBox.getInventory().remove(bow);
                                    break;
                                }
                                bow.setDurability((short) (dur + SkullTurret.BOW_DUR));
                            }
                            break;
                        }
                        b++;
                    }
                    if (foundInfinite)
                        break;
                    ammoBox.getInventory().removeItem(item);
                    continue;
                }
                ammoBox.getInventory().removeItem(item);
                break;
            }
        }
        return ammo;
    }

    private EntityType getEntityType(ItemStack ammo) {
        if (ammo == null)
            return null;
        if (ammo.getType() == Material.ARROW)
            return EntityType.ARROW;
        if (ammo.getType() == Material.FIREBALL)
            return EntityType.SMALL_FIREBALL;
        if (ammo.getType() == Material.SNOW_BALL)
            return EntityType.SNOWBALL;
        return null;
    }

    public SkullType getType() {
        return this.type;
    }

    public String getSkinData() {
        return this.skullSkinData;
    }

    public Block getSkullBlock() {
        return this.skullBlock;
    }

    public Skull getSkull() {
        return (this.skullBlock.getType() == Material.SKULL) ? (Skull) this.skullBlock.getState() : null;
    }

    public UUID getSkullCreator() {
        return this.skullCreator;
    }

    public World getWorld() {
        return this.world;
    }

    public void setSkullCreator(UUID skullCreator) {
        this.skullCreator = skullCreator;
    }

    public void setType(SkullType type) {
        this.type = type;
    }

    public boolean setSkin(String owner) {
        Skull skull = getSkull();
        if (owner.equals("")) {
            this.type = SkullType.SKELETON;
            this.skullSkinData = "";
            skull.setSkullType(this.type);
            skull.update(true, true);
            return true;
        }
        this.type = SkullType.PLAYER;
        this.skullSkinData = owner;
        if (skull.setOwner(owner)) {
            skull.update(true, true);
            if (skull.getOwner().equalsIgnoreCase(this.skullSkinData)) {
                this.skullSkinData = skull.getOwner();
                return true;
            }
            return false;
        }
        return false;
    }

    public void threadedSetSkin(final String owner, final Player player) {
        SkullTurret plugin = SkullTurret.plugin;
        if (this.settingSkin)
            return;
        plugin.getServer().getScheduler().runTaskLaterAsynchronously((Plugin) plugin, new Runnable() {
            public void run() {
                boolean success = false;
                PlacedSkull.this.settingSkin = true;
                success = PlacedSkull.this.setSkin(owner);
                PlacedSkull.this.settingSkin = false;
                if (player != null)
                    if (success) {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinUpdated"), new Object[]{this.val$owner})));
                    } else {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinSetErr"), new Object[]{this.val$owner})));
                    }
            }
        } 0L)
    }

    public void setSkullBlock(Block skullBlock) {
        this.skullBlock = skullBlock;
    }

    public Location getLocation() {
        return new Location(this.world, this.centerX, this.centerY, this.centerZ);
    }

    public Location getCenterPoint() {
        return new Location(this.world, this.centerX + 0.5D, this.centerY, this.centerZ + 0.5D);
    }

    public int getDistance(Location location) {
        RangeCoord rc = new RangeCoord(this.world, location.getBlockX(), this.centerY, location.getBlockZ());
        FacingRange fr = this.directions.get(rc);
        return (fr != null) ? fr.rangeId : this.maxRange;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public EntityType getAmmoType() {
        return this.ammoType;
    }

    public void setAmmoType(EntityType ammoType) {
        this.ammoType = ammoType;
    }

    public void setAmmoType(String ammoType) {
        this.ammoType = getAmmoTypeFromString(ammoType);
    }

    public SkullIntelligence getIntelligence() {
        return this.intelligence;
    }

    public void setIntelligence(SkullIntelligence intelligence) {
        this.intelligence = intelligence;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public double getFireRange() {
        return this.fireRange;
    }

    public void setFireRange(double fireRange) {
        this.fireRange = fireRange;
    }

    public String getStringLocation() {
        Location skullLoc = getLocation();
        StringBuilder stringLoc = new StringBuilder();
        stringLoc.append(skullLoc.getWorld().getName());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockX());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockY());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockZ());
        return stringLoc.toString();
    }

    public void setPatrol(boolean patrol) {
        this.patrol = patrol;
    }

    public boolean doPatrol() {
        return this.patrol;
    }

    public boolean isRedstone() {
        return this.redstone;
    }

    public void setRedstone(boolean redstone) {
        this.redstone = redstone;
    }

    public boolean isRedstonePowered() {
      return (SkullTurret.REDSTONE_BLOCK_MAT == Material.AIR || SkullTurret.REDSTONE_BLOCK_MAT == this.redstoneBlock.getType()) && (
              this.redstoneBlock.isBlockIndirectlyPowered() || this.redstoneBlock.isBlockPowered());
    }

    public Block getLowestBlock() {
        return this.skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
    }

    public void setCommandFireArrow(boolean commandFireArrow) {
        this.commandFireArrow = commandFireArrow;
    }

    public boolean isDead() {
        return this.dead;
    }

    public boolean isChunkLoaded() {
        return this.chunk.isLoaded();
    }

    public boolean unloadChunk() {
        return this.world.unloadChunk(this.chunk.x, this.chunk.z);
    }

    public EntityType getAmmoTypeFromString(String ammoName) {
        if (ammoName.equalsIgnoreCase("arrow")) {
            setCommandFireArrow(false);
            return EntityType.ARROW;
        }
        if (ammoName.equalsIgnoreCase("firearrow")) {
            setCommandFireArrow(true);
            return EntityType.ARROW;
        }
        if (ammoName.equalsIgnoreCase("firecharge") || ammoName.equalsIgnoreCase("fireball")) {
            setCommandFireArrow(false);
            return EntityType.SMALL_FIREBALL;
        }
        if (ammoName.equalsIgnoreCase("snowball")) {
            setCommandFireArrow(false);
            return EntityType.SNOWBALL;
        }
        if (ammoName.equalsIgnoreCase("witherskull") && SkullTurret.DEBUG == 10) {
            setCommandFireArrow(false);
            return EntityType.WITHER_SKULL;
        }
        return null;
    }

    public double getHealth() {
        return this.health;
    }

    public long getRecoveryTimer() {
        return this.recoveryTimer;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public void setRecoveryTimer(long recoveryTimer) {
        this.recoveryTimer = recoveryTimer;
    }

    public long getDestructTimer() {
        return this.destructTimer;
    }

    public void setDestructTimer(long destructTimer) {
        this.destructTimer = destructTimer;
    }

    public long getDeathTimer() {
        return this.deathTimer;
    }

    public void setSkullCreatorLastKnowName(String skullCreatorLastKnowName) {
        this.skullCreatorLastKnowName = skullCreatorLastKnowName;
    }

    public String getSkullCreatorLastKnowName() {
        return this.skullCreatorLastKnowName;
    }

    public boolean isDying() {
        return this.dying;
    }

    public void setDying(boolean dying) {
        this.dying = dying;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.worldName);
        sb.append(this.SEPERATOR);
        sb.append(this.centerX);
        sb.append(this.SEPERATOR);
        sb.append(this.centerY);
        sb.append(this.SEPERATOR);
        sb.append(this.centerZ);
        sb.append(this.SEPERATOR);
        sb.append(this.skullCreator.toString());
        sb.append(this.SEPERATOR);
        sb.append(this.maxRange);
        sb.append(this.SEPERATOR);
        sb.append(this.type.name());
        sb.append(this.SEPERATOR);
        sb.append(this.skullSkinData.equals("") ? "-" : this.skullSkinData);
        sb.append(this.SEPERATOR);
        boolean found = false;
        if (this.enemies.size() != 0)
            for (Map.Entry<EntityType, EntityType> nme : this.enemies.entrySet()) {
                sb.append(nme.getKey().name());
                sb.append(":");
                sb.append("-");
                sb.append(",");
                found = true;
            }
        if (this.playerFrenemies.size() != 0)
            for (Map.Entry<UUID, PlayerNamesFoF> fnme : this.playerFrenemies.entrySet()) {
                PlayerNamesFoF pnf = fnme.getValue();
                if (pnf.getFriendOrEnemy().equals("ENEMY")) {
                    sb.append(EntityType.PLAYER.name());
                    sb.append(":");
                    sb.append(fnme.getKey());
                    sb.append(":");
                    sb.append(pnf.getPlayerName());
                    sb.append(",");
                    found = true;
                }
            }
        if (!found)
            sb.append("-");
        sb.append(this.SEPERATOR);
        found = false;
        if (this.friends.size() != 0)
            for (Map.Entry<EntityType, EntityType> nme : this.friends.entrySet()) {
                sb.append(nme.getKey().name());
                sb.append(":");
                sb.append("-");
                sb.append(",");
                found = true;
            }
        if (this.playerFrenemies.size() != 0)
            for (Map.Entry<UUID, PlayerNamesFoF> fnme : this.playerFrenemies.entrySet()) {
                PlayerNamesFoF pnf = fnme.getValue();
                if (pnf.getFriendOrEnemy().equals("FRIEND")) {
                    sb.append(EntityType.PLAYER.name());
                    sb.append(":");
                    sb.append(fnme.getKey());
                    sb.append(":");
                    sb.append(pnf.getPlayerName());
                    sb.append(",");
                    found = true;
                }
            }
        if (!found)
            sb.append("-");
        sb.append(this.SEPERATOR);
        sb.append(this.ammoType.name());
        sb.append(this.SEPERATOR);
        sb.append(this.intelligence.name());
        sb.append(this.SEPERATOR);
        sb.append(this.patrol);
        sb.append(this.SEPERATOR);
        sb.append(this.redstone);
        sb.append(this.SEPERATOR);
        sb.append(this.commandFireArrow);
        return sb.toString();
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.centerX;
        result = 31 * result + this.centerY;
        result = 31 * result + this.centerZ;
        result = 31 * result + ((this.worldName == null) ? 0 : this.worldName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PlacedSkull other = (PlacedSkull) obj;
        if (this.centerX != other.centerX)
            return false;
        if (this.centerY != other.centerY)
            return false;
        if (this.centerZ != other.centerZ)
            return false;
        if (this.worldName == null) {
          return other.worldName == null;
        } else return this.worldName.equals(other.worldName);
    }

    private void debug() {
    }

    public void clearDebug() {
    }

    private class SliceSection {
        Polygon poly;

        BlockFace face;

        public SliceSection(Polygon poly, BlockFace face) {
            this.poly = poly;
            this.face = face;
        }
    }

    private class EdgeCoords {
        int x;

        int z;

        public EdgeCoords(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    private class FacingRange {
        BlockFace face;

        int rangeId;

        public FacingRange(BlockFace face, int rangeId) {
            this.face = face;
            this.rangeId = rangeId;
        }
    }

    private class RangeCoord {
        int x;

        int y;

        int z;

        World world = null;

        public RangeCoord(World world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Location getLocation() {
            return new Location(this.world, this.x, this.y, this.z);
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + ((this.world == null) ? 0 : this.world.hashCode());
            result = 31 * result + this.x;
            result = 31 * result + this.y;
            result = 31 * result + this.z;
            return result;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            RangeCoord other = (RangeCoord) obj;
            if (this.world == null) {
                if (other.world != null)
                    return false;
            } else if (!this.world.equals(other.world)) {
                return false;
            }
            if (this.x != other.x)
                return false;
            if (this.y != other.y)
                return false;
          return this.z == other.z;
        }
    }

    private class MyChunk {
        int x;

        int z;

        public MyChunk(Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        public boolean isLoaded() {
            return PlacedSkull.this.world != null && PlacedSkull.this.world.isChunkLoaded(this.x, this.z);
        }
    }
}
