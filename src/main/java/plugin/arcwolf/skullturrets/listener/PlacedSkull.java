package plugin.arcwolf.skullturrets.listener;

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
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import plugin.arcwolf.skullturrets.SkullTurret;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class PlacedSkull {
    protected final Character SEPERATOR;
    private final EntityType PINGER;
    private final int centerX;
    private final int centerY;
    private final int centerZ;
    private final Map<RangeCoord, FacingRange> directions;
    public Map<EntityType, EntityType> enemies;
    public Map<EntityType, EntityType> friends;
    public Map<UUID, PlayerNamesFoF> playerFrenemies;
    public boolean failed;
    protected EntityType ammoType;
    private SkullType type;
    private SkullIntelligence intelligence;
    private UUID skullCreator;
    private String skullCreatorLastKnowName;
    private String skullSkinData;
    private String worldName;
    private World world;
    private Block skullBlock;
    private int maxRange;
    private int currentRotDirId;
    private int dirMod;
    private double fireRange;
    private long targetLockTimer;
    private long rotTimer;
    private long rotUpdateTimer;
    private long deathTimer;
    private long coolDown;
    private Vector firingSolution;
    private LivingEntity target;
    private boolean patrol;
    private boolean redstone;
    private boolean fireArrow;
    private boolean commandFireArrow;
    private boolean dead;
    private boolean dying;
    private boolean settingSkin;
    private boolean disabled;
    private MyChunk chunk;
    private Block redstoneBlock;
    private double health;
    private long recoveryTimer;
    private long destructTimer;

    public PlacedSkull(final Block skullBlock, final UUID skullCreator, final int maxRange, final SkullIntelligence intelligence) {
        this.SEPERATOR = '\u001f';
        this.PINGER = EntityType.EXPERIENCE_ORB;
        this.type = SkullType.SKELETON;
        this.skullCreator = null;
        this.skullCreatorLastKnowName = "";
        this.skullSkinData = "";
        this.worldName = "";
        this.world = null;
        this.dirMod = 1;
        this.coolDown = 0L;
        this.ammoType = EntityType.ARROW;
        this.firingSolution = null;
        this.patrol = true;
        this.redstone = false;
        this.fireArrow = false;
        this.commandFireArrow = false;
        this.dead = false;
        this.dying = false;
        this.settingSkin = false;
        this.disabled = false;
        this.directions = new HashMap<RangeCoord, FacingRange>();
        this.enemies = new HashMap<EntityType, EntityType>();
        this.friends = new HashMap<EntityType, EntityType>();
        this.playerFrenemies = new HashMap<UUID, PlayerNamesFoF>();
        this.failed = false;
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
        this.failed = (this.world == null || this.getSkull() == null);
        if (this.failed) {
            return;
        }
        final PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(skullCreator);
        this.chunk = new MyChunk(this.getLocation().getChunk());
        this.init(pps);
        if (SkullTurret.DEBUG == 1) {
            this.debug();
        }
        final long currentTimeMillis = System.currentTimeMillis();
        this.rotUpdateTimer = currentTimeMillis;
        this.deathTimer = currentTimeMillis;
        this.redstoneBlock = skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        this.health = intelligence.getHealth();
    }

    public PlacedSkull(final OldPlacedSkull skull, final UUID creatorUUID) {
        this.SEPERATOR = '\u001f';
        this.PINGER = EntityType.EXPERIENCE_ORB;
        this.type = SkullType.SKELETON;
        this.skullCreator = null;
        this.skullCreatorLastKnowName = "";
        this.skullSkinData = "";
        this.worldName = "";
        this.world = null;
        this.dirMod = 1;
        this.coolDown = 0L;
        this.ammoType = EntityType.ARROW;
        this.firingSolution = null;
        this.patrol = true;
        this.redstone = false;
        this.fireArrow = false;
        this.commandFireArrow = false;
        this.dead = false;
        this.dying = false;
        this.settingSkin = false;
        this.disabled = false;
        this.directions = new HashMap<RangeCoord, FacingRange>();
        this.enemies = new HashMap<EntityType, EntityType>();
        this.friends = new HashMap<EntityType, EntityType>();
        this.playerFrenemies = new HashMap<UUID, PlayerNamesFoF>();
        this.failed = false;
        this.worldName = skull.getWorldName();
        this.centerX = skull.getCenterX();
        this.centerY = skull.getCenterY();
        this.centerZ = skull.getCenterZ();
        this.skullCreator = creatorUUID;
        final PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        final PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
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

    public PlacedSkull(final String inString) {
        this.SEPERATOR = '\u001f';
        this.PINGER = EntityType.EXPERIENCE_ORB;
        this.type = SkullType.SKELETON;
        this.skullCreator = null;
        this.skullCreatorLastKnowName = "";
        this.skullSkinData = "";
        this.worldName = "";
        this.world = null;
        this.dirMod = 1;
        this.coolDown = 0L;
        this.ammoType = EntityType.ARROW;
        this.firingSolution = null;
        this.patrol = true;
        this.redstone = false;
        this.fireArrow = false;
        this.commandFireArrow = false;
        this.dead = false;
        this.dying = false;
        this.settingSkin = false;
        this.disabled = false;
        this.directions = new HashMap<RangeCoord, FacingRange>();
        this.enemies = new HashMap<EntityType, EntityType>();
        this.friends = new HashMap<EntityType, EntityType>();
        this.playerFrenemies = new HashMap<UUID, PlayerNamesFoF>();
        this.failed = false;
        final String[] split = inString.split(this.SEPERATOR.toString());
        this.worldName = split[0];
        this.centerX = Integer.parseInt(split[1]);
        this.centerY = Integer.parseInt(split[2]);
        this.centerZ = Integer.parseInt(split[3]);
        this.skullCreator = UUID.fromString(split[4]);
        final PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        final PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        if (pps != null && pps.isPps()) {
            this.maxRange = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator).getMaxRange();
        } else if (ppg != null) {
            this.maxRange = ppg.getMaxRange();
        } else {
            this.maxRange = SkullTurret.MAX_RANGE;
        }
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
                    if (t.equals(EntityType.PLAYER) && val.length == 3) {
                        final PlayerNamesFoF pnf = new PlayerNamesFoF(val[2], "ENEMY");
                        this.playerFrenemies.put(UUID.fromString(val[1]), pnf);
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
                    if (t.equals(EntityType.PLAYER) && val.length == 3) {
                        final PlayerNamesFoF pnf = new PlayerNamesFoF(val[2], "FRIEND");
                        this.playerFrenemies.put(UUID.fromString(val[1]), pnf);
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
        this.fireRange = this.maxRange * this.intelligence.getFireRangeMultiplier();
        this.health = this.intelligence.getHealth();
        this.world = SkullTurret.plugin.getServer().getWorld(this.worldName);
        this.failed = (this.world == null);
        this.skullBlock = this.getLocation().getBlock();
        this.skullBlock.getChunk().load();
        this.failed = (this.getSkull() == null);
        if (this.failed) {
            if (SkullTurret.DEBUG == 4) {
                SkullTurret.LOGGER.log(Level.SEVERE, "=== Failed with : world? " + (this.world == null) + " nm = " + this.worldName + " skull? " + (this.getSkull() == null) + "loc = " + this.getLocation() + " ===");
            }
            return;
        }
        final long currentTimeMillis = System.currentTimeMillis();
        this.rotUpdateTimer = currentTimeMillis;
        this.deathTimer = currentTimeMillis;
        this.chunk = new MyChunk(this.getLocation().getChunk());
        this.redstoneBlock = this.skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
        this.init(pps);
    }

    static /* synthetic */ void access$3(final PlacedSkull placedSkull, final boolean settingSkin) {
        placedSkull.settingSkin = settingSkin;
    }

    private void init(final PerPlayerSettings pps) {
        final int topLeftX = this.centerX - this.maxRange;
        final int topLeftZ = this.centerZ - this.maxRange;
        final int bottomRightX = this.centerX + this.maxRange;
        final int bottomRightZ = this.centerZ + this.maxRange;
        final int topRightX = this.centerX + this.maxRange;
        final int topRightZ = this.centerZ - this.maxRange;
        final int bottomLeftX = this.centerX - this.maxRange;
        final int bottomLeftZ = this.centerZ + this.maxRange;
        int count;
        final int thirdMaxRange = count = (int) Math.ceil(this.maxRange / 3.0);
        final List<EdgeCoords> edgeCoords = new ArrayList<EdgeCoords>();
        final List<SliceSection> slices = new ArrayList<SliceSection>();
        edgeCoords.add(new EdgeCoords(topLeftX, topLeftZ));
        for (int x = topLeftX; x <= topRightX; ++x) {
            if (x >= topLeftX + count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(x, topLeftZ));
            }
        }
        count = thirdMaxRange;
        for (int z = topRightZ; z <= bottomRightZ; ++z) {
            if (z >= topRightZ + count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(topRightX, z));
            }
        }
        count = thirdMaxRange;
        for (int x = bottomRightX; x >= bottomLeftX; --x) {
            if (x <= bottomRightX - count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(x, bottomRightZ));
            }
        }
        count = thirdMaxRange;
        for (int z = bottomLeftZ; z >= topLeftZ; --z) {
            if (z <= bottomLeftZ - count) {
                count += thirdMaxRange;
                edgeCoords.add(new EdgeCoords(topLeftX, z));
            }
        }
        for (int i = 0; i < edgeCoords.size() && i + 1 != edgeCoords.size(); ++i) {
            final EdgeCoords ec1 = edgeCoords.get(i);
            final EdgeCoords ec2 = edgeCoords.get(i + 1);
            final int[] xPoints = {ec1.x, this.centerX, ec2.x};
            final int[] zPoints = {ec1.z, this.centerZ, ec2.z};
            final BlockFace face = this.getSliceFace(i);
            slices.add(new SliceSection(new Polygon(xPoints, zPoints, 3), face));
        }
        final Map<RangeCoord, RangeCoord> ranges = new HashMap<RangeCoord, RangeCoord>();
        for (int x2 = topLeftX; x2 <= bottomRightX; ++x2) {
            for (int z2 = topLeftZ; z2 <= bottomRightZ; ++z2) {
                final RangeCoord rc = new RangeCoord(this.world, x2, this.centerY, z2);
                final int pointX = x2;
                final int pointZ = z2;
                if (!rc.getLocation().equals(this.skullBlock.getLocation())) {
                    for (final SliceSection s : slices) {
                        if (this.sliceContains(s, pointX, this.centerY, pointZ)) {
                            this.directions.put(rc, new FacingRange(s.face, 0));
                            ranges.put(rc, rc);
                            break;
                        }
                    }
                }
            }
        }
        for (int j = 0; j <= this.maxRange; ++j) {
            final int max = this.maxRange - j;
            for (int x3 = topLeftX + j; x3 <= bottomRightX - j; ++x3) {
                for (int z3 = topLeftZ + j; z3 <= bottomRightZ - j; ++z3) {
                    if (x3 == topLeftX + j || z3 == topLeftZ + j || x3 == bottomRightX - j || z3 == bottomRightZ - j) {
                        final RangeCoord rc2 = new RangeCoord(this.world, x3, this.centerY, z3);
                        if (!rc2.getLocation().equals(this.skullBlock.getLocation()) && this.directions.containsKey(rc2)) {
                            this.directions.get(rc2).rangeId = max;
                        }
                    }
                }
            }
        }
        if (this.chunk.isLoaded()) {
            this.currentRotDirId = this.getCurrentRotation();
        }
        if (pps != null && (pps.isMasterDefaults() || pps.isWizardDefaults())) {
            this.updateToDefaults(pps);
        }
    }

    private void updateToDefaults(final PerPlayerSettings pps) {
        if (pps.isMasterDefaults() && this.intelligence == SkullIntelligence.MASTER && !(this instanceof MobileSkull)) {
            if (!this.skullSkinData.equals(pps.getMasterSkinName())) {
                this.threadedSetSkin(this.skullSkinData = pps.getMasterSkinName(), null);
            }
            this.patrol = pps.isMasterPatrol();
            this.redstone = pps.isMasterRedstone();
            final EntityType at = this.getAmmoTypeFromString(pps.getAmmoTypeName());
            this.ammoType = ((at != null) ? at : EntityType.ARROW);
            if (at == null) {
                this.setCommandFireArrow(false);
            }
        }
        if (pps.isWizardDefaults() && this.intelligence == SkullIntelligence.WIZARD) {
            if (!this.skullSkinData.equals(pps.getMasterSkinName())) {
                this.threadedSetSkin(this.skullSkinData = pps.getWizardSkinName(), null);
            }
            this.patrol = pps.isWizardPatrol();
            this.redstone = pps.isWizardRedstone();
        }
    }

    public void reInitSkull() {
        this.firingSolution = null;
        this.target = null;
        final PerPlayerGroups ppg = SkullTurret.plugin.getPlayerGroup(this.skullCreator);
        final PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
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
        this.init(pps);
    }

    private BlockFace getSliceFace(final int num) {
        switch (num) {
            case 0: {
                return BlockFace.NORTH_WEST;
            }
            case 1: {
                return BlockFace.NORTH_NORTH_WEST;
            }
            case 2:
            case 3: {
                return BlockFace.NORTH;
            }
            case 4: {
                return BlockFace.NORTH_NORTH_EAST;
            }
            case 5:
            case 6: {
                return BlockFace.NORTH_EAST;
            }
            case 7: {
                return BlockFace.EAST_NORTH_EAST;
            }
            case 8:
            case 9: {
                return BlockFace.EAST;
            }
            case 10: {
                return BlockFace.EAST_SOUTH_EAST;
            }
            case 11:
            case 12: {
                return BlockFace.SOUTH_EAST;
            }
            case 13: {
                return BlockFace.SOUTH_SOUTH_EAST;
            }
            case 14:
            case 15: {
                return BlockFace.SOUTH;
            }
            case 16: {
                return BlockFace.SOUTH_SOUTH_WEST;
            }
            case 17:
            case 18: {
                return BlockFace.SOUTH_WEST;
            }
            case 19: {
                return BlockFace.WEST_SOUTH_WEST;
            }
            case 20:
            case 21: {
                return BlockFace.WEST;
            }
            case 22: {
                return BlockFace.WEST_NORTH_WEST;
            }
            case 23: {
                return BlockFace.NORTH_WEST;
            }
            default: {
                return BlockFace.UP;
            }
        }
    }

    private boolean sliceContains(final SliceSection q, final int pointX, final int pointY, final int pointZ) {
        final Polygon poly = q.poly;
        final Vector a = new Vector(poly.xpoints[0], pointY, poly.ypoints[0]);
        final Vector b = new Vector(poly.xpoints[1], pointY, poly.ypoints[1]);
        final Vector c = new Vector(poly.xpoints[2], pointY, poly.ypoints[2]);
        final Vector p = new Vector(pointX, pointY, pointZ);
        final Vector v0 = c.subtract(a);
        final Vector v2 = b.subtract(a);
        final Vector v3 = p.subtract(a);
        final double dot00 = v0.dot(v0);
        final double dot2 = v0.dot(v2);
        final double dot3 = v0.dot(v3);
        final double dot4 = v2.dot(v2);
        final double dot5 = v2.dot(v3);
        final double invDenom = 1.0 / (dot00 * dot4 - dot2 * dot2);
        final double u = (dot4 * dot3 - dot2 * dot5) * invDenom;
        final double v4 = (dot00 * dot5 - dot2 * dot3) * invDenom;
        return u >= 0.0 && v4 >= 0.0 && u + v4 < 1.0;
    }

    public void tick() {
        final long cTime = System.currentTimeMillis();
        if (this.disabled) {
            return;
        }
        final PerPlayerSettings pps = SkullTurret.plugin.perPlayerSettings.get(this.skullCreator);
        this.deathTimer = cTime;
        if (SkullTurret.OFFLINE_PLAYERS) {
            final Player player = Utils.getPlayerFromUUID(this.skullCreator);
            if (player == null) {
                return;
            }
        }
        if (this.doHealthLowDeath(cTime)) {
            return;
        }
        if (this.findTarget()) {
            if (!SkullTurret.WATCH_ONLY) {
                this.autoFire();
            }
        } else if (cTime - this.coolDown > 3000L && this.patrol) {
            if (!this.redstone) {
                this.patrol();
            } else if (this.isRedstonePowered()) {
                this.patrol();
            }
        }
        if (SkullTurret.SKULLVFX) {
            if (!this.redstone) {
                this.showVisualEffect();
            } else if (this.isRedstonePowered()) {
                this.showVisualEffect();
            }
        }
        if (pps != null) {
            this.updateToDefaults(pps);
        }
    }

    private void rotate2(final Entity e, final boolean livingTest) {
        if (livingTest && e instanceof LivingEntity) {
            final LivingEntity le = (LivingEntity) e;
            if (le == null || le.isDead() || !this.patrol) {
                return;
            }
        }
        final Location entityLoc = e.getLocation();
        final Skull skull = this.getSkull();
        final RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        final FacingRange fr = this.directions.get(r);
        if (fr != null) {
            final BlockFace face = fr.face;
            if (!skull.getRotation().equals(face)) {
                skull.setRotation(face);
                skull.update(true);
                if (!this.skullSkinData.equals("") && skull.hasOwner() && !skull.getOwner().equals(this.skullSkinData)) {
                    this.threadedSetSkin(this.skullSkinData, null);
                } else {
                    skull.setSkullType(this.type);
                }
                skull.update(true);
                this.currentRotDirId = this.getCurrentRotation();
            }
        }
    }

    private void doAntiFireball(final Entity e) {
        Vector vect = null;
        final Location skullLoc = this.getAxisAlignment();
        final Location targetLoc = e.getLocation();
        Vector direction = null;
        final Vector toTarget = targetLoc.toVector().subtract(skullLoc.toVector());
        vect = new Vector(toTarget.getX(), toTarget.getY(), toTarget.getZ());
        direction = vect.multiply(1.1);
        this.rotate2(e, false);
        final int distance = this.getDistance(e.getLocation());
        final float distMod = (float) (distance - distance / 4);
        final float velocity = (float) (distMod * 0.3);
        final Entity arrow = this.world.spawnArrow(this.getAxisAlignment(), direction, velocity * 2.0f, 0.0f);
        arrow.setMetadata("SkullTurretantiFireball", new FixedMetadataValue(SkullTurret.plugin, this));
        if (this instanceof MobileSkull) {
            final MobileSkull ms = (MobileSkull) this;
            int ammoAmount = ms.getAmmoAmount();
            --ammoAmount;
            ms.setAmmoAmount(ammoAmount);
        }
    }

    private boolean findTarget() {
        boolean inRange = false;
        boolean inFireRange = false;
        final long currentTime = System.currentTimeMillis();
        if (SkullTurret.ALLOW_REDSTONE_DETECT && this.redstone && !this.isRedstonePowered()) {
            this.target = null;
            return false;
        }
        if (this.target == null) {
            final Map<Integer, LivingEntity> rankedTargets = new HashMap<Integer, LivingEntity>();
            final Entity entity = this.world.spawnEntity(this.getCenterPoint(), this.PINGER);
            if (entity == null) {
                return false;
            }
            final List<Entity> targets = entity.getNearbyEntities(this.fireRange, this.fireRange, this.fireRange);
            entity.remove();
            for (final Entity e : targets) {
                if (e instanceof Player && SkullTurret.plugin.hasPermission((Player) e, "skullturret.ignoreme") && !SkullTurret.NO_PERMISSIONS) {
                    continue;
                }
                if (e.getType() == EntityType.FIREBALL && SkullTurret.DEBUG == 99) {
                    this.doAntiFireball(e);
                    return false;
                }
                if (!(e instanceof LivingEntity) || (e instanceof Player && e.getUniqueId().equals(this.skullCreator) && SkullTurret.DEBUG != 5)) {
                    continue;
                }
                final LivingEntity le = (LivingEntity) e;
                if (SkullTurret.ONLY_BOW) {
                    if (this.intelligence == SkullIntelligence.CRAZED || this.intelligence == SkullIntelligence.DEVIOUS) {
                        if (this.isPlayer(le) && !SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK) {
                            continue;
                        }
                        if (!SkullTurret.TARGET_OWNED) {
                            if (this.isLeashed(le)) {
                                continue;
                            }
                            if (le.getCustomName() != null) {
                                continue;
                            }
                        }
                        if (this.isAnimal(le) || this.isWaterMob(le) || this.isGolem(le)) {
                            continue;
                        }
                        if (this.isNPC(le)) {
                            continue;
                        }
                    }
                    if (this.doBowTest(le, currentTime)) {
                        return true;
                    }
                    continue;
                } else if (this.intelligence == SkullIntelligence.CRAZED || this.intelligence == SkullIntelligence.DEVIOUS) {
                    if (this.isPlayer(le) && !SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK) {
                        continue;
                    }
                    if (!SkullTurret.TARGET_OWNED) {
                        if (this.isLeashed(le)) {
                            continue;
                        }
                        if (le.getCustomName() != null) {
                            continue;
                        }
                    }
                    if (this.isAnimal(le) || this.isWaterMob(le) || this.isGolem(le)) {
                        continue;
                    }
                    if (this.isNPC(le)) {
                        continue;
                    }
                    if (this.canRotate(le) && this.isInFireRange(le)) {
                        this.rotate(this.target = le);
                        this.targetLockTimer = currentTime;
                        if (SkullTurret.SKULLSFX) {
                            this.playSoundEffect();
                        }
                        return true;
                    }
                    continue;
                } else {
                    final List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretsTarget");
                    if (targetedMeta.size() > 0) {
                        if (this.doBowTest(le, currentTime)) {
                            return true;
                        }
                        continue;
                    } else {
                        if (!SkullTurret.TARGET_OWNED) {
                            if (this.isLeashed(le)) {
                                continue;
                            }
                            if (le.getCustomName() != null) {
                                continue;
                            }
                        }
                        if (le instanceof Player && ((Player) le).getGameMode() != GameMode.CREATIVE) {
                            if (this.playerFrenemies.size() != 0) {
                                final Player pl = (Player) le;
                                String playerName = le.getName();
                                final String iff = this.getFriendOrFoeFromName(playerName, pl);
                                if (iff != null) {
                                    if (iff.equals("FRIEND")) {
                                        continue;
                                    }
                                } else {
                                    if (this.friends.containsKey(this.getType(le))) {
                                        continue;
                                    }
                                    if (!this.enemies.containsKey(this.getType(le))) {
                                        continue;
                                    }
                                }
                            } else {
                                if (this.friends.containsKey(this.getType(le))) {
                                    continue;
                                }
                                if (!this.enemies.containsKey(this.getType(le))) {
                                    continue;
                                }
                            }
                        } else {
                            if (!this.enemies.containsKey(this.getType(le))) {
                                continue;
                            }
                            if (this.friends.containsKey(this.getType(le))) {
                                continue;
                            }
                            if (!SkullTurret.TARGET_OWNED) {
                                if (this.isLeashed(le)) {
                                    continue;
                                }
                                if (le.getCustomName() != null) {
                                    continue;
                                }
                            }
                        }
                        final Integer hash = this.hashCode();
                        if (this.checkTargetMissedMeta(le, hash.toString(), currentTime)) {
                            continue;
                        }
                        if (this.checkWizardPotionMeta(le, hash.toString() + ",SkullTurretsWZIgnore", currentTime)) {
                            continue;
                        }
                        rankedTargets.put(this.rateTarget(le), le);
                    }
                }
            }
            if (rankedTargets.size() == 0) {
                return false;
            }
            for (int score = 100; score >= 0; --score) {
                final LivingEntity le2 = rankedTargets.get(score);
                if (le2 != null && this.canRotate(le2) && this.isInFireRange(le2)) {
                    this.rotate(this.target = le2);
                    this.targetLockTimer = currentTime;
                    if (SkullTurret.SKULLSFX) {
                        this.playSoundEffect();
                    }
                    return true;
                }
            }
        } else {
            if (this.target.isDead() || !this.canRotate(this.target)) {
                this.target = null;
                return false;
            }
            inRange = this.isInRange(this.target);
            inFireRange = this.isInFireRange(this.target);
            if (inRange && inFireRange) {
                this.rotate(this.target);
                this.targetLockTimer = currentTime;
                return true;
            }
            if (inRange && !inFireRange) {
                if (currentTime - this.targetLockTimer > 2000L) {
                    this.target = null;
                    this.targetLockTimer = currentTime;
                    return false;
                }
                this.rotate(this.target);
            }
        }
        return false;
    }

    private boolean checkTargetMissedMeta(final LivingEntity le, final String metaName, final long currentTime) {
        final List<MetadataValue> meta = le.getMetadata(metaName);
        if (meta.size() <= 0) {
            return false;
        }
        final int hash = Integer.parseInt(metaName);
        final long time = meta.get(0).asLong();
        if (hash != this.hashCode()) {
            return false;
        }
        if (currentTime - time > 5000L) {
            le.removeMetadata(metaName, SkullTurret.plugin);
            return false;
        }
        return true;
    }

    private boolean checkWizardPotionMeta(final LivingEntity le, final String metaName, final long currentTime) {
        final List<MetadataValue> meta = le.getMetadata(metaName);
        if (meta.size() <= 0) {
            return false;
        }
        final String[] split = metaName.split(",");
        final Integer hash = Integer.parseInt(split[0]);
        if (hash != this.hashCode()) {
            return false;
        }
        final long time = meta.get(0).asLong();
        if (currentTime - time > 5000L) {
            le.removeMetadata(metaName, SkullTurret.plugin);
            return false;
        }
        return true;
    }

    private String getFriendOrFoeFromName(final String playerName, final Player target) {
        for (final Map.Entry<UUID, PlayerNamesFoF> pfre : this.playerFrenemies.entrySet()) {
            final PlayerNamesFoF pnf = pfre.getValue();
            final UUID uuid = pfre.getKey();
            final String playerCurrentName = target.getName();
            if (playerCurrentName.equals(playerName) && uuid.equals(target.getUniqueId())) {
                pnf.setPlayerName(playerCurrentName);
                return pnf.getFriendOrEnemy();
            }
            if (playerName.equals(pnf.getPlayerName())) {
                return pnf.getFriendOrEnemy();
            }
        }
        return null;
    }

    private boolean isLeashed(final LivingEntity le) {
        return le.isLeashed();
    }

    private boolean doBowTest(final LivingEntity le, final long currentTime) {
        final List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretsTarget");
        if (targetedMeta.size() > 0) {
            try {
                final Object obj = targetedMeta.get(0).value();
                if (obj instanceof BowTargetInfo) {
                    final BowTargetInfo playerInfo = (BowTargetInfo) obj;
                    final UUID playerUUID = playerInfo.playerUUID;
                    final long timer = playerInfo.timer;
                    if (System.currentTimeMillis() - timer > 60000L) {
                        le.removeMetadata("SkullTurretsTarget", SkullTurret.plugin);
                        return false;
                    }
                    if (playerUUID.equals(this.skullCreator) && le != null && !le.isDead() && this.canRotate(le) && this.isInFireRange(le)) {
                        this.rotate(this.target = le);
                        this.targetLockTimer = currentTime;
                        if (SkullTurret.SKULLSFX) {
                            this.playSoundEffect();
                        }
                        return true;
                    }
                }
            } catch (Exception ex) {
                le.removeMetadata("SkullTurretsTarget", SkullTurret.plugin);
                return false;
            }
        }
        return false;
    }

    private boolean isPlayer(final LivingEntity le) {
        return le instanceof Player;
    }

    private boolean isAnimal(final LivingEntity le) {
        return false;
    }

    private boolean isGolem(final LivingEntity le) {
        return false;
    }

    private boolean isWaterMob(final LivingEntity le) {
        return false;
    }

    private boolean isNPC(final LivingEntity le) {
        return false;
    }

    private EntityType getType(final LivingEntity le) {
        return EntityType.PLAYER;
    }

    private String getDisguiseName(final LivingEntity le) {
        if (le.getType() != EntityType.PLAYER) {
            return "";
        }
        final Player player = (Player) le;
        return player.getName();
    }

    private int rateTarget(final LivingEntity entity) {
        int score = 0;
        double health = entity.getHealth();
        if (health > 100.0) {
            health /= 5.0;
        }
        health *= 10.0;
        int equipment = 0;
        final EntityEquipment ee = entity.getEquipment();
        if (ee != null) {
            final ItemStack helm = ee.getHelmet();
            if (helm != null) {
                equipment += 5;
                if (helm.getType().equals(Material.DIAMOND_HELMET)) {
                    equipment += 15;
                } else if (helm.getType().equals(Material.IRON_HELMET)) {
                    equipment += 5;
                }
            }
            final ItemStack chest = ee.getChestplate();
            if (chest != null) {
                equipment += 5;
                if (chest.getType().equals(Material.DIAMOND_CHESTPLATE)) {
                    equipment += 15;
                } else if (chest.getType().equals(Material.IRON_CHESTPLATE)) {
                    equipment += 5;
                }
            }
            final ItemStack legs = ee.getLeggings();
            if (legs != null) {
                equipment += 5;
                if (legs.getType().equals(Material.DIAMOND_LEGGINGS)) {
                    equipment += 15;
                } else if (legs.getType().equals(Material.IRON_LEGGINGS)) {
                    equipment += 5;
                }
            }
            final ItemStack feet = ee.getBoots();
            if (feet != null) {
                equipment += 5;
                if (feet.getType().equals(Material.DIAMOND_BOOTS)) {
                    equipment += 10;
                }
            }
            final ItemStack weapon = ee.getItemInHand();
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
        final Location entLoc = entity.getLocation();
        final RangeCoord rc = new RangeCoord(this.world, entLoc.getBlockX(), this.centerY, entLoc.getBlockZ());
        final FacingRange fr = this.directions.get(rc);
        int range = this.maxRange;
        if (fr != null) {
            range -= fr.rangeId;
            range *= 30;
        }
        int mobType = 0;
        if (SkullTurret.plugin.entities.containsKey(entity.getType())) {
            mobType += SkullTurret.plugin.entities.get(entity.getType()).getRating();
        }
        mobType *= 40;
        score = (int) ((range + health + mobType + equipment) / 100.0);
        return score;
    }

    private boolean isInRange(final LivingEntity e) {
        final Location entityLoc = e.getLocation();
        int e_X = entityLoc.getBlockX();
        int e_Y = entityLoc.getBlockY();
        int e_Z = entityLoc.getBlockZ();
        int skull_X = this.skullBlock.getLocation().getBlockX();
        int skull_Y = this.skullBlock.getLocation().getBlockY();
        int skull_Z = this.skullBlock.getLocation().getBlockZ();
        if (skull_X > e_X) {
            final int temp = skull_X;
            skull_X = e_X;
            e_X = temp;
        }
        if (skull_Y > e_Y) {
            final int temp = skull_Y;
            skull_Y = e_Y;
            e_Y = temp;
        }
        if (skull_Z > e_Z) {
            final int temp = skull_Z;
            skull_Z = e_Z;
            e_Z = temp;
        }
        return e_X - skull_X < this.maxRange && e_Y - skull_Y < this.maxRange && e_Z - skull_Z < this.maxRange;
    }

    private boolean isInFireRange(final LivingEntity potentialTarget) {
        final Location entityLoc = potentialTarget.getLocation();
        int e_X = entityLoc.getBlockX();
        int e_Y = entityLoc.getBlockY();
        int e_Z = entityLoc.getBlockZ();
        int skull_X = this.skullBlock.getLocation().getBlockX();
        int skull_Y = this.skullBlock.getLocation().getBlockY();
        int skull_Z = this.skullBlock.getLocation().getBlockZ();
        if (skull_X > e_X) {
            final int temp = skull_X;
            skull_X = e_X;
            e_X = temp;
        }
        if (skull_Y > e_Y) {
            final int temp = skull_Y;
            skull_Y = e_Y;
            e_Y = temp;
        }
        if (skull_Z > e_Z) {
            final int temp = skull_Z;
            skull_Z = e_Z;
            e_Z = temp;
        }
        if (!potentialTarget.isDead() && e_X - skull_X < this.fireRange && e_Y - skull_Y < this.fireRange && e_Z - skull_Z < this.fireRange) {
            this.firingSolution = this.getFiringSolution(potentialTarget);
            if (this.firingSolution == null) {
                potentialTarget.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, System.currentTimeMillis()));
            }
            return this.firingSolution != null;
        }
        return false;
    }

    private Vector getFiringSolution(final LivingEntity potentialTarget) {
        final int ptDistance = this.getDistance(potentialTarget.getEyeLocation());
        Vector vect = null;
        final Location skullLoc = this.getAxisAlignment();
        final Location targetLoc = potentialTarget.getEyeLocation();
        Vector direction = null;
        final Vector toTarget = targetLoc.toVector().subtract(skullLoc.toVector());
        vect = new Vector(toTarget.getX(), toTarget.getY(), toTarget.getZ());
        direction = vect.multiply(1.1);
        final Entity lineOfSigntEntity = this.world.spawnEntity(skullLoc, this.PINGER);
        if (lineOfSigntEntity == null) {
            return null;
        }
        final boolean hasLineOfSignt = potentialTarget.hasLineOfSight(lineOfSigntEntity);
        lineOfSigntEntity.remove();
        if (!hasLineOfSignt) {
            if (SkullTurret.DEBUG == 1) {
                System.out.println("No line of sight " + potentialTarget.getType().name() + " int = " + this.intelligence.getNormalName());
            }
            return null;
        }
        if (SkullTurret.ALLOW_FRIENDLY_FIRE) {
            return vect;
        }
        try {
            final Vector start = skullLoc.toVector();
            final BlockIterator lineOfSight = new BlockIterator(this.world, start, direction, 0.0, (int) this.fireRange);
            while (lineOfSight.hasNext()) {
                final Block b = lineOfSight.next();
                final int bDistance = this.getDistance(b.getLocation());
                if (b.getLocation().equals(this.getLocation())) {
                    continue;
                }
                if (bDistance > ptDistance) {
                    return vect;
                }
                final Entity ping = this.world.spawnEntity(b.getLocation(), this.PINGER);
                if (ping == null) {
                    return vect;
                }
                final List<Entity> around = ping.getNearbyEntities(0.1, 0.1, 0.1);
                ping.remove();
                for (final Entity e : around) {
                    if (e instanceof LivingEntity) {
                        if (e instanceof Player) {
                            String name = e.getName();
                            final UUID playerUUID = e.getUniqueId();
                            if (name.equals(this.skullCreatorLastKnowName)) {
                                return null;
                            }
                            if (this.playerFrenemies.containsKey(playerUUID) && this.playerFrenemies.get(playerUUID).equals("FRIEND")) {
                                return null;
                            }
                        }
                        if (this.friends.containsKey(e.getType())) {
                            return null;
                        }
                        continue;
                    }
                }
            }
            return vect;
        } catch (Exception e2) {
            return null;
        }
    }

    private Vector getSpecialVector() {
        final Vector toTarget = this.target.getEyeLocation().toVector().subtract(this.getAxisAlignment().toVector());
        final double speed = this.fireRange * 0.25;
        double d0 = toTarget.getX();
        double d2 = toTarget.getY();
        double d3 = toTarget.getZ();
        final float f2 = (float) Math.sqrt(d0 * d0 + d2 * d2 + d3 * d3);
        d0 /= f2;
        d2 /= f2;
        d3 /= f2;
        d0 *= speed;
        d2 *= speed;
        d3 *= speed;
        return new Vector(d0, d2, d3);
    }

    protected void autoFire() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.coolDown > this.intelligence.getCooldown()) {
            this.coolDown = currentTime;
            final int distance = this.getDistance(this.target.getEyeLocation());
            final float distMod = (float) (distance - distance / 4);
            float velocity = (float) (distMod * 0.3);
            Sound sound = Sound.ENTITY_ARROW_SHOOT;
            float pitch = 1.0f;
            final double health = this.target.getHealth();
            if (this.intelligence == SkullIntelligence.WIZARD && (health > 5.0 || this.getType(this.target) == EntityType.ENDERMAN)) {
                final Potion potion = this.getPotion();
                if (potion != null) {
                    if (this.getType(this.target) == EntityType.ENDERMAN) {
                        this.target.setMetadata("SkullTurretEnder", new FixedMetadataValue(SkullTurret.plugin, potion));
                    }
                    if (SkullTurret.DEBUG == 3) {
                        System.out.println("I chose a " + potion.getType().name() + " potion to fire.");
                    }
                    final Entity e = this.world.spawnEntity(this.getAxisAlignment(), EntityType.SNOWBALL);
                    e.setMetadata("SkullTurretWizard", new FixedMetadataValue(SkullTurret.plugin, potion));
                    e.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                    this.firingSolution = this.getSpecialVector();
                    velocity = 0.3675f + distMod / 100.0f;
                    e.setVelocity(this.firingSolution.multiply(velocity));
                    if (SkullTurret.SKULLSFX) {
                        this.world.playSound(this.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.5f, 2.0f);
                    }
                }
                return;
            }
            if (this.intelligence == SkullIntelligence.WIZARD && health <= 5.0) {
                this.fireArrow = true;
            }
            EntityType ammo = this.ammoType;
            if (SkullTurret.USE_AMMO_CHESTS && !(this instanceof MobileSkull)) {
                ammo = this.getAmmo();
                if (ammo == null) {
                    return;
                }
            }
            if (SkullTurret.DEBUG == 1) {
                ammo = EntityType.SNOWBALL;
            }
            if (ammo == EntityType.ARROW) {
                final Entity e = this.world.spawnArrow(this.getAxisAlignment(), this.firingSolution, velocity, (float) this.intelligence.getSpread());
                if (SkullTurret.SKULLSFX) {
                    this.world.playSound(this.getLocation(), sound, 0.5f, pitch);
                }
                if (this.fireArrow || this.commandFireArrow) {
                    e.setFireTicks(SkullTurret.FIRETICKS);
                    this.fireArrow = false;
                }
                if (this.intelligence != SkullIntelligence.CRAZED) {
                    e.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                }
                e.setMetadata("SkullTurretDAMAGE", new FixedMetadataValue(SkullTurret.plugin, this));
                if (!SkullTurret.MOB_DROPS) {
                    e.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, this));
                }
                if (this instanceof MobileSkull) {
                    final MobileSkull ms = (MobileSkull) this;
                    int ammoAmount = ms.getAmmoAmount();
                    --ammoAmount;
                    ms.setAmmoAmount(ammoAmount);
                }
            } else {
                final Entity e = this.world.spawnEntity(this.getAxisAlignment(), ammo);
                if (this.intelligence != SkullIntelligence.CRAZED) {
                    e.setMetadata("SkullTurretsSMART", new FixedMetadataValue(SkullTurret.plugin, this));
                }
                if (!SkullTurret.MOB_DROPS) {
                    e.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, this));
                }
                e.setMetadata("SkullTurretDAMAGE", new FixedMetadataValue(SkullTurret.plugin, this));
                if (ammo == EntityType.SNOWBALL) {
                    this.firingSolution = this.getSpecialVector();
                    velocity = 0.3675f + distMod / 100.0f;
                    sound = Sound.ENTITY_CREEPER_HURT;
                    pitch = 0.55f;
                } else if (ammo == EntityType.SMALL_FIREBALL) {
                    final Fireball fb = (Fireball) e;
                    fb.setIsIncendiary(SkullTurret.INCENDIARY_FIRECHARGE);
                    fb.setDirection(this.firingSolution);
                    velocity = 0.2f;
                    sound = Sound.ENTITY_GHAST_SHOOT;
                } else if (ammo == EntityType.WITHER_SKULL) {
                    final WitherSkull ws = (WitherSkull) e;
                    ws.setDirection(this.firingSolution);
                    velocity = 0.3675f + distMod / 300.0f;
                    sound = Sound.ENTITY_GHAST_SHOOT;
                    pitch = 0.25f;
                }
                if (SkullTurret.SKULLSFX) {
                    this.world.playSound(this.getLocation(), sound, 0.5f, pitch);
                }
                e.setVelocity(this.firingSolution.multiply(velocity));
            }
        }
    }

    private Location getAxisAlignment() {
        final double x = this.centerX;
        final double y = this.centerY;
        final double z = this.centerZ;
        switch (this.getSkull().getRotation()) {
            case NORTH: {
                return new Location(this.world, x + 0.5, y + 0.2, z + 0.1);
            }
            case NORTH_NORTH_EAST: {
                return new Location(this.world, x + 0.67, y + 0.2, z + 0.1);
            }
            case NORTH_EAST: {
                return new Location(this.world, x + 0.8, y + 0.2, z + 0.24);
            }
            case EAST_NORTH_EAST: {
                return new Location(this.world, x + 0.9, y + 0.2, z + 0.35);
            }
            case EAST: {
                return new Location(this.world, x + 0.91, y + 0.2, z + 0.5);
            }
            case EAST_SOUTH_EAST: {
                return new Location(this.world, x + 0.89, y + 0.2, z + 0.65);
            }
            case SOUTH_EAST: {
                return new Location(this.world, x + 0.8, y + 0.2, z + 0.8);
            }
            case SOUTH_SOUTH_EAST: {
                return new Location(this.world, x + 0.68, y + 0.2, z + 0.87);
            }
            case SOUTH: {
                return new Location(this.world, x + 0.5, y + 0.2, z + 0.93);
            }
            case SOUTH_SOUTH_WEST: {
                return new Location(this.world, x + 0.374, y + 0.2, z + 0.9);
            }
            case SOUTH_WEST: {
                return new Location(this.world, x + 0.23, y + 0.2, z + 0.8);
            }
            case WEST_SOUTH_WEST: {
                return new Location(this.world, x + 0.13, y + 0.2, z + 0.67);
            }
            case WEST: {
                return new Location(this.world, x + 0.13, y + 0.2, z + 0.53);
            }
            case WEST_NORTH_WEST: {
                return new Location(this.world, x + 0.15, y + 0.2, z + 0.38);
            }
            case NORTH_WEST: {
                return new Location(this.world, x + 0.24, y + 0.2, z + 0.24);
            }
            case NORTH_NORTH_WEST: {
                return new Location(this.world, x + 0.35, y + 0.2, z + 0.15);
            }
            default: {
                return new Location(this.world, x, y + 0.2, z);
            }
        }
    }

    private int getCurrentRotation() {
        final BlockFace face = this.getSkull().getRotation();
        switch (face) {
            case NORTH: {
                return 0;
            }
            case NORTH_NORTH_EAST: {
                return 1;
            }
            case NORTH_EAST: {
                return 2;
            }
            case EAST_NORTH_EAST: {
                return 3;
            }
            case EAST: {
                return 4;
            }
            case EAST_SOUTH_EAST: {
                return 5;
            }
            case SOUTH_EAST: {
                return 6;
            }
            case SOUTH_SOUTH_EAST: {
                return 7;
            }
            case SOUTH: {
                return 8;
            }
            case SOUTH_SOUTH_WEST: {
                return 9;
            }
            case SOUTH_WEST: {
                return 10;
            }
            case WEST_SOUTH_WEST: {
                return 11;
            }
            case WEST: {
                return 12;
            }
            case WEST_NORTH_WEST: {
                return 13;
            }
            case NORTH_WEST: {
                return 14;
            }
            default: {
                return 15;
            }
        }
    }

    private void patrol() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.rotUpdateTimer > SkullTurret.PATROL_TIME) {
            this.rotUpdateTimer = currentTime;
            final Skull skull = this.getSkull();
            if (currentTime - this.rotTimer > 30000L) {
                final boolean clockWise = new Random().nextBoolean();
                if (clockWise) {
                    this.dirMod = 1;
                } else {
                    this.dirMod = -1;
                }
                this.rotTimer = currentTime;
            }
            int lookAheadDirId = this.currentRotDirId + this.dirMod;
            if (lookAheadDirId > 15) {
                lookAheadDirId = 0;
            }
            final BlockFace nextRotDir = this.getNewRotDir(lookAheadDirId);
            if (skull.getBlock().getRelative(nextRotDir).getType() != Material.AIR) {
                this.dirMod = -this.dirMod;
                this.rotTimer = currentTime;
            }
            skull.setRotation(this.getNewRotDir(this.currentRotDirId));
            this.currentRotDirId += this.dirMod;
            if (this.currentRotDirId > 15 && this.dirMod > 0) {
                this.currentRotDirId = 0;
            } else if (this.currentRotDirId < 0 && this.dirMod < 0) {
                this.currentRotDirId = 15;
            }
            skull.update();
        }
    }

    public void setSkullRotation(final BlockFace face) {
        final Skull skull = this.getSkull();
        skull.setRotation(face);
        skull.update();
        this.currentRotDirId = this.getRotationId(face);
    }

    private BlockFace getNewRotDir(final int dirId) {
        switch (dirId) {
            case 0: {
                return BlockFace.NORTH_NORTH_EAST;
            }
            case 1: {
                return BlockFace.NORTH_EAST;
            }
            case 2: {
                return BlockFace.EAST_NORTH_EAST;
            }
            case 3: {
                return BlockFace.EAST;
            }
            case 4: {
                return BlockFace.EAST_SOUTH_EAST;
            }
            case 5: {
                return BlockFace.SOUTH_EAST;
            }
            case 6: {
                return BlockFace.SOUTH_SOUTH_EAST;
            }
            case 7: {
                return BlockFace.SOUTH;
            }
            case 8: {
                return BlockFace.SOUTH_SOUTH_WEST;
            }
            case 9: {
                return BlockFace.SOUTH_WEST;
            }
            case 10: {
                return BlockFace.WEST_SOUTH_WEST;
            }
            case 11: {
                return BlockFace.WEST;
            }
            case 12: {
                return BlockFace.WEST_NORTH_WEST;
            }
            case 13: {
                return BlockFace.NORTH_WEST;
            }
            case 14: {
                return BlockFace.NORTH_NORTH_WEST;
            }
            case 15: {
                return BlockFace.NORTH;
            }
            default: {
                return BlockFace.NORTH;
            }
        }
    }

    private boolean canRotate(final LivingEntity le) {
        if (le == null || le.isDead()) {
            return false;
        }
        final Location entityLoc = le.getLocation();
        final Skull skull = this.getSkull();
        final RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        final FacingRange fr = this.directions.get(r);
        if (fr != null) {
            final BlockFace newDirection = fr.face;
            if (skull.getRotation().equals(newDirection)) {
                return true;
            }
            final int newRot = this.getRotationId(newDirection);
            int startRot = this.getCurrentRotation();
            int dir = 1;
            if (Math.abs(startRot - newRot) > 8) {
                dir = -1;
            }
            for (int j = 0; j < 2; ++j) {
                for (int i = 0; i < 16; ++i) {
                    int lookAheadDirId = startRot + dir;
                    if (lookAheadDirId > 15) {
                        lookAheadDirId = 0;
                    }
                    final BlockFace nextRotDir = this.getNewRotDir(lookAheadDirId);
                    if (skull.getBlock().getRelative(nextRotDir).getType() != Material.AIR) {
                        break;
                    }
                    if (newRot == lookAheadDirId) {
                        return true;
                    }
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

    protected int getRotationId(final BlockFace direction) {
        switch (direction) {
            case NORTH: {
                return 0;
            }
            case NORTH_NORTH_EAST: {
                return 1;
            }
            case NORTH_EAST: {
                return 2;
            }
            case EAST_NORTH_EAST: {
                return 3;
            }
            case EAST: {
                return 4;
            }
            case EAST_SOUTH_EAST: {
                return 5;
            }
            case SOUTH_EAST: {
                return 6;
            }
            case SOUTH_SOUTH_EAST: {
                return 7;
            }
            case SOUTH: {
                return 8;
            }
            case SOUTH_SOUTH_WEST: {
                return 9;
            }
            case SOUTH_WEST: {
                return 10;
            }
            case WEST_SOUTH_WEST: {
                return 11;
            }
            case WEST: {
                return 12;
            }
            case WEST_NORTH_WEST: {
                return 13;
            }
            case NORTH_WEST: {
                return 14;
            }
            default: {
                return 15;
            }
        }
    }

    private void rotate(final LivingEntity le) {
        if (le == null || le.isDead() || !this.patrol) {
            return;
        }
        final Location entityLoc = le.getLocation();
        final Skull skull = this.getSkull();
        final RangeCoord r = new RangeCoord(entityLoc.getWorld(), entityLoc.getBlockX(), skull.getY(), entityLoc.getBlockZ());
        final FacingRange fr = this.directions.get(r);
        if (fr != null) {
            final BlockFace face = fr.face;
            if (!skull.getRotation().equals(face)) {
                skull.setRotation(face);
                skull.update(true);
                if (!this.skullSkinData.equals("") && skull.hasOwner() && !skull.getOwner().equals(this.skullSkinData)) {
                    this.threadedSetSkin(this.skullSkinData, null);
                } else {
                    skull.setSkullType(this.type);
                }
                skull.update(true);
                this.currentRotDirId = this.getCurrentRotation();
            }
        }
    }

    private void showVisualEffect() {
        if (this.intelligence == SkullIntelligence.CRAZED) {
            return;
        }
        this.world.playEffect(this.getCenterPoint(), this.intelligence.getEffect(), 4, (int) this.fireRange);
    }

    private void deathSmokeFX() {
        if (SkullTurret.SKULLVFX) {
            for (int i = 0; i < 9; ++i) {
                this.world.playEffect(this.getCenterPoint(), Effect.SMOKE, i, 10);
                this.getSkullBlock().getState().update();
            }
        }
    }

    private void playSoundEffect() {
    }

    private boolean doHealthLowDeath(final long cTime) {
        if (this.health == 0.0 || this.dying) {
            if (!SkullTurret.ALLOW_DAMAGED_SKULL_DESTRUCT) {
                this.destructTimer = cTime;
            }
            this.doDeathRattle(this.destructTimer, cTime, SkullTurret.DESTRUCT_TIMER);
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

    public boolean doDeathRattle(final long dt, final long cTime, final long timerAmount) {
        if (cTime - dt < timerAmount) {
            this.deathSmokeFX();
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
            final Location loc = this.getCenterPoint();
            final double x = loc.getX();
            final double y = loc.getY();
            final double z = loc.getZ();
            final World world = loc.getWorld();
            world.createExplosion(x, y, z, 0.0f, false, false);
        }
        Utils.clearBlock(this.getSkull().getBlock());
    }

    public ItemStack getInfoBook(final boolean brokeDrop, final Player writer) {
        final int MAXPAGELENGTH = 182;
        final ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        final BookMeta meta = (BookMeta) writtenBook.getItemMeta();
        meta.setTitle(this.intelligence.getNormalName() + " Skull Book.");
        meta.setAuthor("§a" + (brokeDrop ? "Unknown" : writer.getName()));
        final StringBuilder infoText = new StringBuilder();
        infoText.append("§n");
        infoText.append(brokeDrop ? "BROKEN" : this.intelligence.getNormalName());
        infoText.append(" Skull Info§r\n\n");
        infoText.append("Owner: ");
        infoText.append(brokeDrop ? "Unknown" : (writer.getUniqueId().equals(this.skullCreator) ? writer.getName() : this.skullCreator.toString()));
        infoText.append("\n§0World: ");
        infoText.append(brokeDrop ? "Unknown" : this.world.getName());
        infoText.append("\n§0Loc: ");
        infoText.append(brokeDrop ? "?" : Integer.toString(this.getLocation().getBlockX()));
        infoText.append(", ");
        infoText.append(brokeDrop ? "?" : Integer.toString(this.getLocation().getBlockY()));
        infoText.append(", ");
        infoText.append(brokeDrop ? "?" : Integer.toString(this.getLocation().getBlockZ()));
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
        for (final EntityType et : this.enemies.keySet()) {
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
        if (index != -1) {
            enemiesString.replace(index, enemiesString.length() - 1, ".|");
        }
        if (!hasEnemies) {
            enemiesString.append("\n#|");
        }
        meta.addPage(enemiesString.toString());
        StringBuilder friendsString = new StringBuilder();
        friendsString.append("§nMobs Skull Ignores:§r\n\n");
        boolean hasFriends = false;
        for (final EntityType et2 : this.friends.keySet()) {
            if (friendsString.length() + et2.name().length() > MAXPAGELENGTH) {
                meta.addPage(friendsString.toString());
                friendsString = new StringBuilder();
                friendsString.append(et2.name());
                friendsString.append(", ");
            }
            friendsString.append(et2.name());
            friendsString.append(", ");
            hasFriends = true;
        }
        index = friendsString.lastIndexOf(",");
        if (index != -1) {
            friendsString.replace(index, friendsString.length() - 1, ".|");
        }
        if (!hasFriends) {
            friendsString.append("\n#|");
        }
        meta.addPage(friendsString.toString());
        StringBuilder enemyPlayerNames = new StringBuilder();
        StringBuilder enemyPlayerUUID = new StringBuilder();
        StringBuilder friendPlayerNames = new StringBuilder();
        StringBuilder friendPlayerUUID = new StringBuilder();
        final List<String> epnPages = new ArrayList<>();
        final List<String> epUUIDPages = new ArrayList<>();
        final List<String> fpnPages = new ArrayList<>();
        final List<String> fpUUIDPages = new ArrayList<>();
        enemyPlayerNames.append("§nPlayers Skull Attacks:§r\n\n");
        enemyPlayerUUID.append("§nUUID Skull Attacks:§r\n\n");
        friendPlayerNames.append("§nPlayers Skull Ignores:§r\n\n");
        friendPlayerUUID.append("§nUUID Skull Ignores:§r\n\n");
        PlayerNamesFoF pfof = null;
        String playerName = "";
        String playerUUID = "";
        for (final Map.Entry<UUID, PlayerNamesFoF> playerTargets : this.playerFrenemies.entrySet()) {
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
                } else {
                    enemyPlayerUUID.append(playerUUID);
                    enemyPlayerUUID.append(", ");
                }
            } else {
                if (!pfof.getFriendOrEnemy().equals("FRIEND")) {
                    continue;
                }
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
                } else {
                    friendPlayerUUID.append(playerUUID);
                    friendPlayerUUID.append(", ");
                }
            }
        }
        index = enemyPlayerNames.lastIndexOf(",");
        if (index != -1) {
            enemyPlayerNames.replace(index, enemyPlayerNames.length() - 1, ".|");
        }
        index = enemyPlayerUUID.lastIndexOf(",");
        if (index != -1) {
            enemyPlayerUUID.replace(index, enemyPlayerUUID.length() - 1, ".|");
        }
        index = friendPlayerNames.lastIndexOf(",");
        if (index != -1) {
            friendPlayerNames.replace(index, friendPlayerNames.length() - 1, ".|");
        }
        index = friendPlayerUUID.lastIndexOf(",");
        if (index != -1) {
            friendPlayerUUID.replace(index, friendPlayerUUID.length() - 1, ".|");
        }
        boolean hasPlayerEnemy = true;
        boolean hasPlayerFriend = true;
        if (enemyPlayerNames.length() == 28) {
            enemyPlayerNames.append("\n#|");
            hasPlayerEnemy = false;
        }
        if (enemyPlayerUUID.length() == 25) {
            enemyPlayerUUID.append("\n#|");
        }
        if (friendPlayerNames.length() == 28) {
            friendPlayerNames.append("\n#|");
            hasPlayerFriend = false;
        }
        if (friendPlayerUUID.length() == 25) {
            friendPlayerUUID.append("\n#|");
        }
        for (final String page : epnPages) {
            meta.addPage(page);
        }
        meta.addPage(enemyPlayerNames.toString());
        for (final String page : fpnPages) {
            meta.addPage(page);
        }
        meta.addPage(friendPlayerNames.toString());
        for (final String page : epUUIDPages) {
            meta.addPage(page);
        }
        meta.addPage(enemyPlayerUUID.toString());
        for (final String page : fpUUIDPages) {
            meta.addPage(page);
        }
        meta.addPage(friendPlayerUUID.toString());
        final List<String> lore = new ArrayList<>();
        lore.add(this.intelligence.getNormalName() + " Skull Turret");
        lore.add("Skull Knowledge Book");
        meta.setLore(lore);
        writtenBook.setItemMeta(meta);
        if (!hasEnemies && !hasFriends && !hasPlayerEnemy && !hasPlayerFriend && brokeDrop) {
            return null;
        }
        return writtenBook;
    }

    public void parseBook(final ItemStack writtenBook, final Player player) {
        if (writtenBook != null && writtenBook.getType() == Material.WRITTEN_BOOK) {
            if (!writtenBook.hasItemMeta()) {
                return;
            }
            final BookMeta meta = (BookMeta) writtenBook.getItemMeta();
            if (!meta.hasLore()) {
                return;
            }
            if (meta.getLore().size() < 2) {
                return;
            }
            if (meta.getLore().get(1).equals("Skull Knowledge Book")) {
                try {
                    final String page1 = meta.getPage(1);
                    String[] split = page1.split("\\*");
                    if (!player.getUniqueId().equals(this.skullCreator) && !SkullTurret.plugin.hasPermission(player, "skullturret.admin")) {
                        player.sendMessage(Utils.parseText(Utils.getLocalization("notSkullOwner")));
                        return;
                    }
                    final List<String> pages = meta.getPages();
                    final StringBuilder sb = new StringBuilder();
                    for (final String page2 : pages) {
                        sb.append(page2);
                    }
                    split = sb.toString().split(":");
                    final String skin = split[4].trim().split("\\*")[0];
                    if (this.intelligence.canSkinChange()) {
                        if (!skin.equals("-")) {
                            this.threadedSetSkin(skin, null);
                        } else {
                            this.setSkin("");
                        }
                    }
                    split = sb.toString().split("\\|");
                    this.enemies.clear();
                    String mobEnemiesString = split[1].split(":")[1];
                    if (!mobEnemiesString.contains("#")) {
                        final int mesLength = mobEnemiesString.length();
                        mobEnemiesString = mobEnemiesString.substring(4, mesLength - 1).trim();
                        final String[] enemyStringSplit = mobEnemiesString.split(",");
                        String[] array;
                        for (int length = (array = enemyStringSplit).length, i = 0; i < length; ++i) {
                            final String es = array[i];
                            final EntityType type = EntityType.valueOf(es.trim());
                            if (SkullTurret.plugin.hasPermission(player, "skullturret.target." + type.name().toLowerCase()) || this.customTypePermission(type, player)) {
                                this.enemies.put(type, type);
                            }
                        }
                    }
                    this.friends.clear();
                    String mobFriendsString = split[2].split(":")[1];
                    if (!mobFriendsString.contains("#")) {
                        final int mfsLength = mobFriendsString.length();
                        mobFriendsString = mobFriendsString.substring(4, mfsLength - 1).trim();
                        final String[] friendStringSplit = mobFriendsString.split(",");
                        String[] array2;
                        for (int length2 = (array2 = friendStringSplit).length, j = 0; j < length2; ++j) {
                            final String es2 = array2[j];
                            final EntityType type2 = EntityType.valueOf(es2.trim());
                            if (SkullTurret.plugin.hasPermission(player, "skullturret.target." + type2.name().toLowerCase()) || this.customTypePermission(type2, player)) {
                                this.friends.put(type2, type2);
                            }
                        }
                    }
                    if (!SkullTurret.plugin.hasPermission(player, "skullturret.target.player")) {
                        return;
                    }
                    this.playerFrenemies.clear();
                    String playerEnemiesNameString = split[3].split(":")[1];
                    String playerEnemiesUUIDString = split[5].split(":")[1];
                    if (!playerEnemiesNameString.contains("#")) {
                        final int pens = playerEnemiesNameString.length();
                        final int peus = playerEnemiesUUIDString.length();
                        playerEnemiesNameString = playerEnemiesNameString.substring(4, pens - 1).trim();
                        playerEnemiesUUIDString = playerEnemiesUUIDString.substring(4, peus - 1).trim();
                        final String[] playerEnemiesNameStringSplit = playerEnemiesNameString.split(",");
                        final String[] playerEnemiesUUIDStringSplit = playerEnemiesUUIDString.split(",");
                        int count = 0;
                        String[] array3;
                        for (int length3 = (array3 = playerEnemiesNameStringSplit).length, k = 0; k < length3; ++k) {
                            final String name = array3[k];
                            final PlayerNamesFoF pnfof = new PlayerNamesFoF(name.trim(), "ENEMY");
                            final UUID playerUUID = UUID.fromString(playerEnemiesUUIDStringSplit[count].trim());
                            this.playerFrenemies.put(playerUUID, pnfof);
                            ++count;
                        }
                    }
                    String playerFriendsNameString = split[4].split(":")[1];
                    String playerFriendsUUIDString = split[6].split(":")[1];
                    if (!playerFriendsNameString.contains("#")) {
                        final int pfns = playerFriendsNameString.length();
                        final int pfus = playerFriendsUUIDString.length();
                        playerFriendsNameString = playerFriendsNameString.substring(4, pfns - 1).trim();
                        playerFriendsUUIDString = playerFriendsUUIDString.substring(4, pfus - 1).trim();
                        final String[] playerFriendsNameStringSplit = playerFriendsNameString.split(",");
                        final String[] playerFriendsUUIDStringSplit = playerFriendsUUIDString.split(",");
                        int count2 = 0;
                        String[] array4;
                        for (int length4 = (array4 = playerFriendsNameStringSplit).length, l = 0; l < length4; ++l) {
                            final String name2 = array4[l];
                            final PlayerNamesFoF pnfof2 = new PlayerNamesFoF(name2.trim(), "FRIEND");
                            final UUID playerUUID2 = UUID.fromString(playerFriendsUUIDStringSplit[count2].trim());
                            this.playerFrenemies.put(playerUUID2, pnfof2);
                            ++count2;
                        }
                    }
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdated")));
                    return;
                } catch (Exception e) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("updateSkullEr")));
                    e.printStackTrace();
                    return;
                }
            }
            player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdateFail")));
        } else {
            player.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdateFail")));
        }
    }

    private boolean customTypePermission(final EntityType ent, final Player player) {
        for (final Map.Entry<String, List<String>> en : SkullTurret.plugin.customNames.entrySet()) {
            final String permission = "skullturret.target." + en.getKey().toLowerCase();
            for (final String name : en.getValue()) {
                if (ent == EntityType.valueOf(name)) {
                    return SkullTurret.plugin.hasPermission(player, permission);
                }
            }
        }
        return false;
    }

    private Potion getPotion() {
        final Block bottomBlock = this.getLowestBlock();
        final Block northBlock = bottomBlock.getRelative(BlockFace.NORTH);
        final Block southBlock = bottomBlock.getRelative(BlockFace.SOUTH);
        final Block westBlock = bottomBlock.getRelative(BlockFace.WEST);
        final Block eastBlock = bottomBlock.getRelative(BlockFace.EAST);
        final List<Chest> ammoBox = new ArrayList<Chest>();
        if (bottomBlock.getType() == Material.CHEST || bottomBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox.add((Chest) bottomBlock.getState());
        }
        if (northBlock.getType() == Material.CHEST || northBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox.add((Chest) northBlock.getState());
        }
        if (southBlock.getType() == Material.CHEST || southBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox.add((Chest) southBlock.getState());
        }
        if (westBlock.getType() == Material.CHEST || westBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox.add((Chest) westBlock.getState());
        }
        if (eastBlock.getType() == Material.CHEST || eastBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox.add((Chest) eastBlock.getState());
        }
        final Potion ammo = this.pickPotion(ammoBox);
        if (ammo != null) {
            return ammo;
        }
        final Integer hash = this.hashCode();
        this.target.setMetadata(hash.toString() + ",SkullTurretsWZIgnore", new FixedMetadataValue(SkullTurret.plugin, System.currentTimeMillis()));
        this.target = null;
        return null;
    }

    private Potion pickPotion(final List<Chest> ammoBox) {
        if (this.target == null) {
            return null;
        }
        final Collection<PotionEffect> effects = this.target.getActivePotionEffects();
        final Potion ammo = null;
        ItemStack[] items = null;
        final List<Potion> randomPotion = new ArrayList<Potion>();
        for (final Chest c : ammoBox) {
            items = c.getInventory().getContents();
            ItemStack[] array;
            for (int length = (array = items).length, i = 0; i < length; ++i) {
                final ItemStack item = array[i];
                if (item != null) {
                    if (item.getType() == Material.POTION) {
                        try {
                            final Potion potion = Potion.fromItemStack(this.unRevert(item));
                            if (potion.isSplash() && this.isValidPotion(potion)) {
                                boolean found = false;
                                for (final PotionEffect e : potion.getEffects()) {
                                    if (effects == null) {
                                        break;
                                    }
                                    for (final PotionEffect e2 : effects) {
                                        found = e.getType().equals(e2.getType());
                                        if (found) {
                                            break;
                                        }
                                    }
                                    if (found) {
                                        break;
                                    }
                                }
                                if (!found) {
                                    randomPotion.add(potion);
                                }
                            }
                        } catch (Exception e3) {
                            if (SkullTurret.DEBUG == 3) {
                                System.out.println("Potion Choice Error");
                                e3.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if (randomPotion.size() > 0) {
            final int random = new Random().nextInt(randomPotion.size());
            return randomPotion.get(random);
        }
        return ammo;
    }

    private ItemStack unRevert(final ItemStack item) {
        final int damage = item.getDurability();
        if (damage == 16453 || damage == 16460) {
            final ItemStack deRevert = item.clone();
            deRevert.setDurability((short) (damage - 64));
            return deRevert;
        }
        return item;
    }

    private boolean isValidPotion(final Potion potion) {
        final Collection<PotionEffect> effects = potion.getEffects();
        for (final PotionEffect ef : effects) {
            if (this.isValidPotionEffect(ef.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean canUsePoison() {
        if (this.target == null) {
            return false;
        }
        final EntityType type = this.target.getType();
        if (SkullTurret.plugin.entities.containsKey(type)) {
            final EntitySettings es = SkullTurret.plugin.entities.get(type);
            return es.canPoison();
        }
        return false;
    }

    private boolean mustUseHeal() {
        if (this.target == null) {
            return false;
        }
        final EntityType type = this.target.getType();
        if (SkullTurret.plugin.entities.containsKey(type)) {
            final EntitySettings es = SkullTurret.plugin.entities.get(type);
            return es.mustHeal();
        }
        return false;
    }

    private boolean isEndermen() {
        return this.target != null && this.target.getType() == EntityType.ENDERMAN;
    }

    private boolean isValidPotionEffect(final PotionEffectType type) {
        return (type.equals(PotionEffectType.POISON) && this.canUsePoison() && !this.isEndermen()) || (type.equals(PotionEffectType.SLOW) && !this.isEndermen()) || (type.equals(PotionEffectType.WEAKNESS) && !this.isEndermen()) || (type.equals(PotionEffectType.HEAL) && this.mustUseHeal()) || (type.equals(PotionEffectType.HARM) && (!this.mustUseHeal() || this.isEndermen()));
    }

    private EntityType getAmmo() {
        final Block bottomBlock = this.getLowestBlock();
        final Block northBlock = bottomBlock.getRelative(BlockFace.NORTH);
        final Block southBlock = bottomBlock.getRelative(BlockFace.SOUTH);
        final Block westBlock = bottomBlock.getRelative(BlockFace.WEST);
        final Block eastBlock = bottomBlock.getRelative(BlockFace.EAST);
        Chest ammoBox = null;
        if (bottomBlock.getType() == Material.CHEST || bottomBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) bottomBlock.getState();
            final ItemStack ammo = this.withdrawAmmo(ammoBox);
            if (ammo != null) {
                return this.getEntityType(ammo);
            }
        }
        if (northBlock.getType() == Material.CHEST || northBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) northBlock.getState();
            final ItemStack ammo = this.withdrawAmmo(ammoBox);
            if (ammo != null) {
                return this.getEntityType(ammo);
            }
        }
        if (southBlock.getType() == Material.CHEST || southBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) southBlock.getState();
            final ItemStack ammo = this.withdrawAmmo(ammoBox);
            if (ammo != null) {
                return this.getEntityType(ammo);
            }
        }
        if (westBlock.getType() == Material.CHEST || westBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) westBlock.getState();
            final ItemStack ammo = this.withdrawAmmo(ammoBox);
            if (ammo != null) {
                return this.getEntityType(ammo);
            }
        }
        if (eastBlock.getType() == Material.CHEST || eastBlock.getType() == Material.TRAPPED_CHEST) {
            ammoBox = (Chest) eastBlock.getState();
            final ItemStack ammo = this.withdrawAmmo(ammoBox);
            if (ammo != null) {
                return this.getEntityType(ammo);
            }
        }
        return null;
    }

    private ItemStack withdrawAmmo(final Chest ammoBox) {
        ItemStack ammo = null;
        boolean foundAmmo = false;
        boolean foundInfinite = false;
        for (final ItemStack item : SkullTurret.plugin.ammoList) {
            if (!foundAmmo && ammoBox.getInventory().containsAtLeast(item, 1)) {
                ammo = item;
                foundAmmo = true;
            }
            if (foundAmmo) {
                if (ammo.getType() != Material.ARROW) {
                    ammoBox.getInventory().removeItem(item);
                    break;
                }
                final ItemStack[] contents;
                final int length = (contents = ammoBox.getInventory().getContents()).length;
                int i = 0;
                while (i < length) {
                    final ItemStack bow = contents[i];
                    if (bow != null && bow.getType() == Material.BOW) {
                        this.fireArrow = (SkullTurret.ALLOW_FIREBOW && bow.containsEnchantment(Enchantment.ARROW_FIRE));
                        foundInfinite = (SkullTurret.ALLOW_INFINITE_BOW && bow.containsEnchantment(Enchantment.ARROW_INFINITE));
                        if (this.commandFireArrow && !foundInfinite) {
                            break;
                        }
                        final short dur = bow.getDurability();
                        if (dur + SkullTurret.BOW_DUR >= 385) {
                            ammoBox.getInventory().remove(bow);
                            break;
                        }
                        bow.setDurability((short) (dur + SkullTurret.BOW_DUR));
                        break;
                    } else {
                        ++i;
                    }
                }
                if (foundInfinite) {
                    break;
                }
                ammoBox.getInventory().removeItem(item);
            }
        }
        return ammo;
    }

    private EntityType getEntityType(final ItemStack ammo) {
        if (ammo == null) {
            return null;
        }
        if (ammo.getType() == Material.ARROW) {
            return EntityType.ARROW;
        }
        if (ammo.getType() == Material.FIRE_CHARGE) {
            return EntityType.SMALL_FIREBALL;
        }
        if (ammo.getType() == Material.SNOWBALL) {
            return EntityType.SNOWBALL;
        }
        return null;
    }

    public SkullType getType() {
        return this.type;
    }

    public void setType(final SkullType type) {
        this.type = type;
    }

    public String getSkinData() {
        return this.skullSkinData;
    }

    public Block getSkullBlock() {
        return this.skullBlock;
    }

    public void setSkullBlock(final Block skullBlock) {
        this.skullBlock = skullBlock;
    }

    public Skull getSkull() {
        return (this.skullBlock.getType() == Material.SKELETON_SKULL) ? ((Skull) this.skullBlock.getState()) : null;
    }

    public UUID getSkullCreator() {
        return this.skullCreator;
    }

    public void setSkullCreator(final UUID skullCreator) {
        this.skullCreator = skullCreator;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean setSkin(final String owner) {
        final Skull skull = this.getSkull();
        if (owner.equals("")) {
            this.type = SkullType.SKELETON;
            this.skullSkinData = "";
            skull.setSkullType(this.type);
            skull.update(true, true);
            return true;
        }
        this.type = SkullType.PLAYER;
        this.skullSkinData = owner;
        if (!skull.setOwner(owner)) {
            return false;
        }
        skull.update(true, true);
        if (skull.getOwner().equalsIgnoreCase(this.skullSkinData)) {
            this.skullSkinData = skull.getOwner();
            return true;
        }
        return false;
    }

    public void threadedSetSkin(final String owner, final Player player) {
        final SkullTurret plugin = SkullTurret.plugin;
        if (this.settingSkin) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                PlacedSkull.access$3(PlacedSkull.this, true);
                success = PlacedSkull.this.setSkin(owner);
                PlacedSkull.access$3(PlacedSkull.this, false);
                if (player != null) {
                    if (success) {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinUpdated"), owner)));
                    } else {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinSetErr"), owner)));
                    }
                }
            }
        }, 0L);
    }

    public Location getLocation() {
        return new Location(this.world, this.centerX, this.centerY, this.centerZ);
    }

    public Location getCenterPoint() {
        return new Location(this.world, this.centerX + 0.5, this.centerY, this.centerZ + 0.5);
    }

    public int getDistance(final Location location) {
        final RangeCoord rc = new RangeCoord(this.world, location.getBlockX(), this.centerY, location.getBlockZ());
        final FacingRange fr = this.directions.get(rc);
        return (fr != null) ? fr.rangeId : this.maxRange;
    }

    public LivingEntity getTarget() {
        return this.target;
    }

    public void setTarget(final LivingEntity target) {
        this.target = target;
    }

    public EntityType getAmmoType() {
        return this.ammoType;
    }

    public void setAmmoType(final EntityType ammoType) {
        this.ammoType = ammoType;
    }

    public void setAmmoType(final String ammoType) {
        this.ammoType = this.getAmmoTypeFromString(ammoType);
    }

    public SkullIntelligence getIntelligence() {
        return this.intelligence;
    }

    public void setIntelligence(final SkullIntelligence intelligence) {
        this.intelligence = intelligence;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public void setMaxRange(final int maxRange) {
        this.maxRange = maxRange;
    }

    public double getFireRange() {
        return this.fireRange;
    }

    public void setFireRange(final double fireRange) {
        this.fireRange = fireRange;
    }

    public String getStringLocation() {
        final Location skullLoc = this.getLocation();
        final StringBuilder stringLoc = new StringBuilder();
        stringLoc.append(skullLoc.getWorld().getName());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockX());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockY());
        stringLoc.append(",");
        stringLoc.append(skullLoc.getBlockZ());
        return stringLoc.toString();
    }

    public void setPatrol(final boolean patrol) {
        this.patrol = patrol;
    }

    public boolean doPatrol() {
        return this.patrol;
    }

    public boolean isRedstone() {
        return this.redstone;
    }

    public void setRedstone(final boolean redstone) {
        this.redstone = redstone;
    }

    public boolean isRedstonePowered() {
        return (SkullTurret.REDSTONE_BLOCK_MAT == Material.AIR || SkullTurret.REDSTONE_BLOCK_MAT == this.redstoneBlock.getType()) && (this.redstoneBlock.isBlockIndirectlyPowered() || this.redstoneBlock.isBlockPowered());
    }

    public Block getLowestBlock() {
        return this.skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN);
    }

    public void setCommandFireArrow(final boolean commandFireArrow) {
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

    public EntityType getAmmoTypeFromString(final String ammoName) {
        if (ammoName.equalsIgnoreCase("arrow")) {
            this.setCommandFireArrow(false);
            return EntityType.ARROW;
        }
        if (ammoName.equalsIgnoreCase("firearrow")) {
            this.setCommandFireArrow(true);
            return EntityType.ARROW;
        }
        if (ammoName.equalsIgnoreCase("firecharge") || ammoName.equalsIgnoreCase("fireball")) {
            this.setCommandFireArrow(false);
            return EntityType.SMALL_FIREBALL;
        }
        if (ammoName.equalsIgnoreCase("snowball")) {
            this.setCommandFireArrow(false);
            return EntityType.SNOWBALL;
        }
        if (ammoName.equalsIgnoreCase("witherskull") && SkullTurret.DEBUG == 10) {
            this.setCommandFireArrow(false);
            return EntityType.WITHER_SKULL;
        }
        return null;
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(final double health) {
        this.health = health;
    }

    public long getRecoveryTimer() {
        return this.recoveryTimer;
    }

    public void setRecoveryTimer(final long recoveryTimer) {
        this.recoveryTimer = recoveryTimer;
    }

    public long getDestructTimer() {
        return this.destructTimer;
    }

    public void setDestructTimer(final long destructTimer) {
        this.destructTimer = destructTimer;
    }

    public long getDeathTimer() {
        return this.deathTimer;
    }

    public String getSkullCreatorLastKnowName() {
        return this.skullCreatorLastKnowName;
    }

    public void setSkullCreatorLastKnowName(final String skullCreatorLastKnowName) {
        this.skullCreatorLastKnowName = skullCreatorLastKnowName;
    }

    public boolean isDying() {
        return this.dying;
    }

    public void setDying(final boolean dying) {
        this.dying = dying;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.worldName);
        sb.append(this.SEPERATOR.toString());
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
        if (this.enemies.size() != 0) {
            for (final Map.Entry<EntityType, EntityType> nme : this.enemies.entrySet()) {
                sb.append(nme.getKey().name());
                sb.append(":");
                sb.append("-");
                sb.append(",");
                found = true;
            }
        }
        if (this.playerFrenemies.size() != 0) {
            for (final Map.Entry<UUID, PlayerNamesFoF> fnme : this.playerFrenemies.entrySet()) {
                final PlayerNamesFoF pnf = fnme.getValue();
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
        }
        if (!found) {
            sb.append("-");
        }
        sb.append(this.SEPERATOR);
        found = false;
        if (this.friends.size() != 0) {
            for (final Map.Entry<EntityType, EntityType> nme : this.friends.entrySet()) {
                sb.append(nme.getKey().name());
                sb.append(":");
                sb.append("-");
                sb.append(",");
                found = true;
            }
        }
        if (this.playerFrenemies.size() != 0) {
            for (final Map.Entry<UUID, PlayerNamesFoF> fnme : this.playerFrenemies.entrySet()) {
                final PlayerNamesFoF pnf = fnme.getValue();
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
        }
        if (!found) {
            sb.append("-");
        }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + this.centerX;
        result = 31 * result + this.centerY;
        result = 31 * result + this.centerZ;
        result = 31 * result + ((this.worldName == null) ? 0 : this.worldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PlacedSkull other = (PlacedSkull) obj;
        if (this.centerX != other.centerX) {
            return false;
        }
        if (this.centerY != other.centerY) {
            return false;
        }
        if (this.centerZ != other.centerZ) {
            return false;
        }
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

        public SliceSection(final Polygon poly, final BlockFace face) {
            this.poly = poly;
            this.face = face;
        }
    }

    private class EdgeCoords {
        int x;
        int z;

        public EdgeCoords(final int x, final int z) {
            this.x = x;
            this.z = z;
        }
    }

    private class FacingRange {
        BlockFace face;
        int rangeId;

        public FacingRange(final BlockFace face, final int rangeId) {
            this.face = face;
            this.rangeId = rangeId;
        }
    }

    private class RangeCoord {
        int x;
        int y;
        int z;
        World world;

        public RangeCoord(final World world, final int x, final int y, final int z) {
            this.world = null;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Location getLocation() {
            return new Location(this.world, this.x, this.y, this.z);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + ((this.world == null) ? 0 : this.world.hashCode());
            result = 31 * result + this.x;
            result = 31 * result + this.y;
            result = 31 * result + this.z;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final RangeCoord other = (RangeCoord) obj;
            if (this.world == null) {
                if (other.world != null) {
                    return false;
                }
            } else if (!this.world.equals(other.world)) {
                return false;
            }
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
    }

    private class MyChunk {
        int x;
        int z;

        public MyChunk(final Chunk chunk) {
            this.x = chunk.getX();
            this.z = chunk.getZ();
        }

        public boolean isLoaded() {
            return PlacedSkull.this.world != null && PlacedSkull.this.world.isChunkLoaded(this.x, this.z);
        }
    }
}
