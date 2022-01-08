package plugin.arcwolf.skullturrets;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import plugin.arcwolf.skullturrets.data.DataStore;
import plugin.arcwolf.skullturrets.listener.*;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.util.*;
import java.util.logging.Logger;

public class SkullTurret extends JavaPlugin {
    public static final Logger LOGGER;
    public static String PROFILE_URL;
    public static SkullTurret plugin;
    public static int DEBUG;
    public static String LANGUAGE;
    public static boolean DROP;
    public static boolean TARGET_OWNED;
    public static int MAX_SKULL_PER_PLAYER;
    public static boolean WATCH_ONLY;
    public static int MAX_RANGE;
    public static boolean MOB_DROPS;
    public static boolean SKULLVFX;
    public static boolean SKULLSFX;
    public static boolean ALLOW_FRIENDLY_FIRE;
    public static boolean DROP_BOOKS_ON_DEATH;
    public static boolean ALLOW_REDSTONE_DETECT;
    public static Material REDSTONE_BLOCK_MAT;
    public static boolean ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK;
    public static boolean DROP_BOOK_ON_BREAK;
    public static boolean USE_AMMO_CHESTS;
    public static boolean ALLOW_FIRE_CHARGE;
    public static boolean ALLOW_ARROWS;
    public static boolean ALLOW_SNOWBALLS;
    public static boolean ALLOW_FIREBOW;
    public static boolean ALLOW_INFINITE_BOW;
    public static boolean ALLOW_TEMP_TURRETS;
    public static boolean ALLOW_TEMPTURRET_REARM;
    public static boolean NO_PERMISSIONS;
    public static boolean INCENDIARY_FIRECHARGE;
    public static boolean ONLINE_UUID_CHECK;
    public static boolean ALLOW_SKULL_DAMAGE;
    public static boolean ALLOW_DAMAGED_SKULL_DESTRUCT;
    public static boolean CONSUME_REPAIR_ITEM;
    public static boolean ALLOW_ARROW_DAMAGE;
    public static boolean SKULLS_RETALIATE;
    public static int SKULL_DAMAGE_RECOVERY_TIME;
    public static double SKULL_REPAIR_AMOUNT;
    public static boolean USE_VAULT_ECON;
    public static double ECON_CRAZED_COST;
    public static double ECON_DEVIOUS_COST;
    public static double ECON_MASTER_COST;
    public static double ECON_WIZARD_COST;
    public static double ECON_BOW_COST;
    public static int FIRETICKS;
    public static int BOW_DUR;
    public static int DROP_CHANCE;
    public static int PATROL_TIME;
    public static int DESTRUCT_TIMER;
    public static int CRAZED_STACK_SIZE;
    public static int DEVIOUS_STACK_SIZE;
    public static int MASTER_STACK_SIZE;
    public static int WIZARD_STACK_SIZE;
    public static int BOW_STACK_SIZE;
    public static boolean OFFLINE_PLAYERS;
    public static boolean PER_PLAYER_SETTINGS;
    public static boolean MOB_LOOT;
    public static boolean RELOAD;
    public static boolean RELOAD_QUESTION;
    public static boolean ONLY_BOW;
    public static boolean DB_UPDATE;
    public static boolean PPS_UPDATE;
    public static boolean DB_UPDATE_DONE;
    public static List<String> NO_DROP_WORLDS;
    public static String pluginName;

    static {
        LOGGER = Logger.getLogger("Minecraft.SkullTurret");
        SkullTurret.PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
        SkullTurret.DEBUG = 0;
        SkullTurret.LANGUAGE = "enUS";
        SkullTurret.DROP = false;
        SkullTurret.TARGET_OWNED = false;
        SkullTurret.MAX_SKULL_PER_PLAYER = 5;
        SkullTurret.WATCH_ONLY = false;
        SkullTurret.MAX_RANGE = 15;
        SkullTurret.MOB_DROPS = false;
        SkullTurret.SKULLVFX = true;
        SkullTurret.SKULLSFX = true;
        SkullTurret.ALLOW_FRIENDLY_FIRE = false;
        SkullTurret.DROP_BOOKS_ON_DEATH = false;
        SkullTurret.ALLOW_REDSTONE_DETECT = true;
        SkullTurret.REDSTONE_BLOCK_MAT = Material.LAPIS_BLOCK;
        SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK = false;
        SkullTurret.DROP_BOOK_ON_BREAK = true;
        SkullTurret.USE_AMMO_CHESTS = false;
        SkullTurret.ALLOW_FIRE_CHARGE = false;
        SkullTurret.ALLOW_ARROWS = true;
        SkullTurret.ALLOW_SNOWBALLS = true;
        SkullTurret.ALLOW_FIREBOW = false;
        SkullTurret.ALLOW_INFINITE_BOW = false;
        SkullTurret.ALLOW_TEMP_TURRETS = true;
        SkullTurret.ALLOW_TEMPTURRET_REARM = true;
        SkullTurret.NO_PERMISSIONS = false;
        SkullTurret.INCENDIARY_FIRECHARGE = false;
        SkullTurret.ONLINE_UUID_CHECK = true;
        SkullTurret.ALLOW_SKULL_DAMAGE = false;
        SkullTurret.ALLOW_DAMAGED_SKULL_DESTRUCT = false;
        SkullTurret.CONSUME_REPAIR_ITEM = true;
        SkullTurret.ALLOW_ARROW_DAMAGE = true;
        SkullTurret.SKULLS_RETALIATE = true;
        SkullTurret.SKULL_DAMAGE_RECOVERY_TIME = 15000;
        SkullTurret.SKULL_REPAIR_AMOUNT = 5.0;
        SkullTurret.USE_VAULT_ECON = false;
        SkullTurret.ECON_CRAZED_COST = 1.0;
        SkullTurret.ECON_DEVIOUS_COST = 2.0;
        SkullTurret.ECON_MASTER_COST = 3.0;
        SkullTurret.ECON_WIZARD_COST = 4.0;
        SkullTurret.ECON_BOW_COST = 2.0;
        SkullTurret.FIRETICKS = 100;
        SkullTurret.BOW_DUR = 5;
        SkullTurret.DROP_CHANCE = 50;
        SkullTurret.PATROL_TIME = 1800;
        SkullTurret.DESTRUCT_TIMER = 90000;
        SkullTurret.CRAZED_STACK_SIZE = 1;
        SkullTurret.DEVIOUS_STACK_SIZE = 1;
        SkullTurret.MASTER_STACK_SIZE = 1;
        SkullTurret.WIZARD_STACK_SIZE = 1;
        SkullTurret.BOW_STACK_SIZE = 1;
        SkullTurret.OFFLINE_PLAYERS = false;
        SkullTurret.PER_PLAYER_SETTINGS = true;
        SkullTurret.MOB_LOOT = true;
        SkullTurret.RELOAD = false;
        SkullTurret.RELOAD_QUESTION = false;
        SkullTurret.ONLY_BOW = false;
        SkullTurret.DB_UPDATE = false;
        SkullTurret.PPS_UPDATE = false;
        SkullTurret.DB_UPDATE_DONE = false;
        SkullTurret.NO_DROP_WORLDS = new ArrayList<>();
    }

    public NamespacedKey key = new NamespacedKey(this, "GoodTurrets");
    public Map<Location, PlacedSkull> skullMap;
    public Map<UUID, SkullCounts> playersSkullNumber;
    public Map<UUID, PerPlayerSettings> perPlayerSettings;
    public Map<String, PerPlayerGroups> perPlayerGroups;
    public Map<EntityType, EntitySettings> entities;
    public Map<String, List<String>> customNames;
    public Map<Material, Double> weapons;
    public List<ItemStack> ammoList;
    public PluginDescriptionFile pdf;
    public Server server;
    public BukkitScheduler scheduler;
    public Random rand;
    public SkullTurretRecipe recipes;
    public Economy econ;
    private DataStore ds;
    private boolean datafileLoaded;
    private Permission vaultPerms;
    private boolean permissionsEr;
    private boolean permissionsSet;

    public SkullTurret() {
        this.skullMap = new HashMap<Location, PlacedSkull>();
        this.playersSkullNumber = new HashMap<UUID, SkullCounts>();
        this.perPlayerSettings = new HashMap<UUID, PerPlayerSettings>();
        this.perPlayerGroups = new HashMap<String, PerPlayerGroups>();
        this.entities = new HashMap<EntityType, EntitySettings>();
        this.customNames = new HashMap<String, List<String>>();
        this.weapons = new HashMap<Material, Double>();
        this.ammoList = new ArrayList<ItemStack>();
        this.rand = new Random();
        this.datafileLoaded = false;
        this.econ = null;
        this.permissionsEr = false;
        this.permissionsSet = false;
    }

    public void onEnable() {
        SkullTurret.plugin = this;
        this.server = SkullTurret.plugin.getServer();
        final PluginManager pm = this.server.getPluginManager();
        pm.registerEvents(new STListeners(SkullTurret.plugin), SkullTurret.plugin);
        this.pdf = SkullTurret.plugin.getDescription();
        SkullTurret.pluginName = this.pdf.getName();
        this.ds = new DataStore(SkullTurret.plugin);
        this.scheduler = this.server.getScheduler();
        this.recipes = new SkullTurretRecipe(SkullTurret.plugin);
        this.runTasks();
    }

    public void onDisable() {
        if (this.datafileLoaded) {
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Saved " + this.ds.saveDatabase(true) + " skulls to database.");
        }
        this.ds.savePerPlayerSettings();
        this.scheduler.cancelTasks(this);
        final Iterator<Recipe> rec = SkullTurret.plugin.server.recipeIterator();
        while (rec.hasNext()) {
            final Recipe r = rec.next();
            if (r.getResult().equals(this.recipes.getSkullBow()) || r.getResult().equals(this.recipes.getCrazedSkull()) || r.getResult().equals(this.recipes.getDeviousSkull()) || r.getResult().equals(this.recipes.getMasterSkull()) || r.getResult().equals(this.recipes.getWizardSkull())) {
                rec.remove();
            }
            ShapelessRecipe[] mobileDeviousSkull;
            for (int length = (mobileDeviousSkull = this.recipes.mobileDeviousSkull).length, i = 0; i < length; ++i) {
                final ShapelessRecipe sc = mobileDeviousSkull[i];
                if (r.getResult().equals(sc.getResult())) {
                    rec.remove();
                    break;
                }
            }
            ShapelessRecipe[] mobileMasterSkull;
            for (int length2 = (mobileMasterSkull = this.recipes.mobileMasterSkull).length, j = 0; j < length2; ++j) {
                final ShapelessRecipe sc = mobileMasterSkull[j];
                if (r.getResult().equals(sc.getResult())) {
                    rec.remove();
                    break;
                }
            }
        }
        CustomPlayer.playerSettings.clear();
    }

    private void runTasks() {
        this.scheduler.runTaskLater(plugin, new Runnable() {
            public void run() {
                SkullTurret.this.ds.init();
                SkullTurret.this.econ = SkullTurret.this.getEconomy();
                if (!SkullTurret.DB_UPDATE)
                    SkullTurret.this.datafileLoaded = true;
                SkullTurret.this.doSkullHeartbeatTask();
                if (SkullTurret.DB_UPDATE)
                    SkullTurret.this.doDatabaseReload();
            }
        }, 1L);
    }

    private void doSkullHeartbeatTask() {
        this.scheduler.runTaskTimer(SkullTurret.plugin, new Runnable() {
            @Override
            public void run() {
                if (!SkullTurret.DB_UPDATE && !SkullTurret.PPS_UPDATE) {
                    final Iterator<PlacedSkull> skulls = SkullTurret.this.skullMap.values().iterator();
                    boolean save = false;
                    while (skulls.hasNext()) {
                        final boolean disable = SkullTurret.this.rand.nextBoolean();
                        final PlacedSkull skull = skulls.next();
                        if (skull.getWorld().getPlayers().size() == 0) {
                            continue;
                        }
                        if (!skull.isChunkLoaded()) {
                            continue;
                        }
                        if (skull.getSkullBlock().getType() != Material.SKELETON_SKULL) {
                            continue;
                        }
                        final UUID playerUUID = skull.getSkullCreator();
                        final SkullCounts sc = SkullTurret.plugin.playersSkullNumber.get(playerUUID);
                        final int skullLimit = SkullTurret.this.getSkullLimit(playerUUID);
                        if (sc.getActiveSkulls() > skullLimit && disable && !skull.isDisabled()) {
                            sc.disableSkull(skull);
                        } else if (sc.getActiveSkulls() < skullLimit && skull.isDisabled()) {
                            sc.enableSkull(skull);
                        }
                        if (skull.isDead()) {
                            skull.destruct();
                            sc.enableSkull(skull);
                            int numActiveSkulls = sc.getActiveSkulls();
                            --numActiveSkulls;
                            sc.setActiveSkulls(numActiveSkulls);
                            skulls.remove();
                            save = true;
                        } else {
                            skull.tick();
                        }
                    }
                    if (save) {
                        SkullTurret.this.ds.saveDatabase(false);
                    }
                }
            }
        }, 20L, 20L);
    }

    private void doDatabaseReload() {
        this.scheduler.runTaskTimer(plugin, new Runnable() {
            public void run() {
                if (!SkullTurret.DB_UPDATE_DONE && !SkullTurret.PPS_UPDATE) {
                    SkullTurret.this.ds.init();
                    SkullTurret.DB_UPDATE_DONE = true;
                    SkullTurret.this.datafileLoaded = true;
                }
            }
        }, 1200L, 1200L);
    }

    public int getSkullLimit(final UUID UUID) {
        final Player player = Utils.getPlayerFromUUID(UUID);
        if (player == null) {
            return Integer.MAX_VALUE;
        }
        if (this.hasPermission(player, "skullturret.admin")) {
            return Integer.MAX_VALUE;
        }
        int maxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;
        final PerPlayerGroups ppg = this.getPlayerGroup(player);
        final PerPlayerSettings pps = this.perPlayerSettings.get(UUID);
        if (ppg != null && !player.isOp()) {
            maxTurrets = ppg.getMaxTurrets();
        } else if (pps != null && pps.isPps()) {
            maxTurrets = pps.getMaxTurrets();
        }
        return maxTurrets;
    }

    public Economy getEconomy() {
        if (SkullTurret.USE_VAULT_ECON && this.getServer().getPluginManager().getPlugin("Vault") != null) {
            final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>) this.getServer().getServicesManager().getRegistration((Class) Economy.class);
            if (rsp != null) {
                final Economy e = rsp.getProvider();
                if (e != null) {
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Economy Detected via Vault API.");
                }
                return e;
            }
        }
        return null;
    }

    public boolean hasPermission(final Player player, final String permission) {
        if (SkullTurret.NO_PERMISSIONS) {
            return true;
        }
        this.getPermissionsPlugin();
        if (SkullTurret.DEBUG == 2) {
            if (this.vaultPerms != null) {
                final String pName = player.getName();
                final String gName = this.vaultPerms.getPrimaryGroup(player);
                final boolean permissions = player.hasPermission(permission);
                SkullTurret.LOGGER.info("Vault permissions, group for '" + pName + "' = " + gName);
                SkullTurret.LOGGER.info("Permission for " + permission + " is " + permissions);
            } else if (this.server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
                SkullTurret.LOGGER.info("Bukkit Permissions " + permission + " " + player.hasPermission(permission));
            } else if (this.permissionsEr && (player.isOp() || player.hasPermission(permission))) {
                SkullTurret.LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            } else {
                SkullTurret.LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
        }
        return player.isOp() || player.hasPermission(permission);
    }

    public PerPlayerGroups getPlayerGroup(final UUID playerUUID) {
        final Player player = Utils.getPlayerFromUUID(playerUUID);
        if (player != null) {
            return this.getPlayerGroup(player);
        }
        return null;
    }

    public PerPlayerGroups getPlayerGroup(final Player player) {
        for (final Map.Entry<String, PerPlayerGroups> g : this.perPlayerGroups.entrySet()) {
            if (this.hasPermission(player, "skullturret." + g.getKey())) {
                return g.getValue();
            }
        }
        return null;
    }

    private void getPermissionsPlugin() {
        if (this.server.getPluginManager().getPlugin("Vault") != null) {
            final RegisteredServiceProvider<Permission> rsp = (RegisteredServiceProvider<Permission>) this.getServer().getServicesManager().getRegistration((Class) Permission.class);
            if (!this.permissionsSet) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Vault detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.vaultPerms = rsp.getProvider();
        } else if (this.server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
            if (!this.permissionsSet) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Bukkit permissions detected, permissions enabled...");
                this.permissionsSet = true;
            }
        } else if (!this.permissionsEr) {
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unknown permissions detected, Using Generic Permissions...");
            this.permissionsEr = true;
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (sender instanceof Player) {
            return new CommandHandler(this).inGame(sender, cmd, commandLabel, args);
        }
        return new CommandHandler(this).inConsole(sender, cmd, commandLabel, args);
    }
}
