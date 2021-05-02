package plugin.arcwolf.skullturrets;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.imp.Permissions;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.*;
import java.util.logging.Logger;

public class SkullTurret extends JavaPlugin {
    public static final Logger LOGGER = Logger.getLogger("Minecraft.SkullTurret");

    public static String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";

    public static SkullTurret plugin;

    public static int DEBUG = 0;

    public static String LANGUAGE = "enUS";

    public static boolean DROP = false;

    public static boolean TARGET_OWNED = false;

    public static int MAX_SKULL_PER_PLAYER = 5;

    public static boolean WATCH_ONLY = false;

    public static int MAX_RANGE = 15;

    public static boolean MOB_DROPS = false;

    public static boolean SKULLVFX = true;

    public static boolean SKULLSFX = true;

    public static boolean ALLOW_FRIENDLY_FIRE = false;

    public static boolean DROP_BOOKS_ON_DEATH = false;

    public static boolean ALLOW_REDSTONE_DETECT = true;

    public static Material REDSTONE_BLOCK_MAT = Material.LAPIS_BLOCK;

    public static boolean ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK = false;

    public static boolean DROP_BOOK_ON_BREAK = true;

    public static boolean USE_AMMO_CHESTS = false;

    public static boolean ALLOW_FIRE_CHARGE = false;

    public static boolean ALLOW_ARROWS = true;

    public static boolean ALLOW_SNOWBALLS = true;

    public static boolean ALLOW_FIREBOW = false;

    public static boolean ALLOW_INFINITE_BOW = false;

    public static boolean ALLOW_TEMP_TURRETS = true;

    public static boolean ALLOW_TEMPTURRET_REARM = true;

    public static boolean NO_PERMISSIONS = false;

    public static boolean INCENDIARY_FIRECHARGE = false;

    public static boolean ONLINE_UUID_CHECK = true;

    public static boolean ALLOW_SKULL_DAMAGE = false;

    public static boolean ALLOW_DAMAGED_SKULL_DESTRUCT = false;

    public static boolean CONSUME_REPAIR_ITEM = true;

    public static boolean ALLOW_ARROW_DAMAGE = true;

    public static boolean SKULLS_RETALIATE = true;

    public static int SKULL_DAMAGE_RECOVERY_TIME = 15000;

    public static double SKULL_REPAIR_AMOUNT = 5.0D;

    public static boolean USE_VAULT_ECON = false;

    public static double ECON_CRAZED_COST = 1.0D;

    public static double ECON_DEVIOUS_COST = 2.0D;

    public static double ECON_MASTER_COST = 3.0D;

    public static double ECON_WIZARD_COST = 4.0D;

    public static double ECON_BOW_COST = 2.0D;

    public static int FIRETICKS = 100;

    public static int BOW_DUR = 5;

    public static int DROP_CHANCE = 50;

    public static int PATROL_TIME = 1800;

    public static int DESTRUCT_TIMER = 90000;

    public static int CRAZED_STACK_SIZE = 1;

    public static int DEVIOUS_STACK_SIZE = 1;

    public static int MASTER_STACK_SIZE = 1;

    public static int WIZARD_STACK_SIZE = 1;

    public static int BOW_STACK_SIZE = 1;

    public static boolean OFFLINE_PLAYERS = false;

    public static boolean PER_PLAYER_SETTINGS = true;

    public static boolean MOB_LOOT = true;

    public static boolean RELOAD = false;

    public static boolean RELOAD_QUESTION = false;

    public static boolean ONLY_BOW = false;

    public static boolean ALLOW_FACTIONS = false;

    public static boolean ALLOW_TOWNY = false;

    public static boolean ALLOW_DISGUISE = false;

    public static boolean ALLOW_VANISH = false;

    public static boolean DB_UPDATE = false;

    public static boolean PPS_UPDATE = false;

    public static boolean DB_UPDATE_DONE = false;

    public static boolean FACT_TARGET_NEUTRAL = false;

    public static boolean FACT_TARGET_TRUCE = false;

    public static boolean FACT_TARGET_ENEMY = true;

    public static boolean FACT_TARGET_ALLY = false;

    public static boolean FACT_TARGET_PEACEFUL = false;

    public static boolean FACT_TARGET_UNAFFILIATED = true;

    public static boolean FACT_ALLOW_SKULL_DESTRUCT = true;

    public static boolean FACT_ALLOW_PLACE_ENEMY = false;

    public static boolean FACT_ALLOW_PLACE_NEUTRAL = false;

    public static boolean FACT_ALLOW_PLACE_TRUCE = false;

    public static boolean FACT_ALLOW_PLACE_OWN = true;

    public static boolean FACT_ALLOW_PLACE_ALLY = true;

    public static boolean FACT_ALLOW_PLACE_PEACEFUL = true;

    public static boolean FACT_ALLOW_PLACE_WILDERNESS = true;

    public static boolean FACT_USE_FACTION_POWER = false;

    public static double FACT_POWER_PER_TURRET = 1.0D;

    public static boolean TOWN_NOMAD_PLACE = true;

    public static boolean TOWN_NATIONLESS_TOWN_PLACE = true;

    public static boolean TOWN_SKULLS_RESPECT_PVP = false;

    public static boolean TOWN_SKULLS_IGNORE_EMBASSY_OWNER = true;

    public static boolean TOWN_TOWN_SKULLS_IGNORE_NOMADS = false;

    public static boolean TOWN_WILDERNESS_FREEFORALL = true;

    public static boolean TOWN_ALLOW_SKULL_DESTRUCT = true;

    public static List<String> NO_DROP_WORLDS = new ArrayList<String>();

    public static FactionsDetector fd;

    public Map<Location, PlacedSkull> skullMap = new HashMap<Location, PlacedSkull>();

    public Map<UUID, SkullCounts> playersSkullNumber = new HashMap<UUID, SkullCounts>();

    public Map<UUID, PerPlayerSettings> perPlayerSettings = new HashMap<UUID, PerPlayerSettings>();

    public Map<String, PerPlayerGroups> perPlayerGroups = new HashMap<String, PerPlayerGroups>();

    public Map<EntityType, EntitySettings> entities = new HashMap<EntityType, EntitySettings>();

    public Map<String, List<String>> customNames = new HashMap<String, List<String>>();

    public Map<Material, Double> weapons = new HashMap<Material, Double>();

    public List<ItemStack> ammoList = new ArrayList<ItemStack>();

    public PluginDescriptionFile pdf;

    public Server server;

    public static String pluginName;

    public BukkitScheduler scheduler;

    public Random rand = new Random();

    public SkullTurretRecipe recipes;

    private DataStore ds;

    private boolean datafileLoaded = false;

    private GroupManager groupManager;

    private Permission vaultPerms;

    private Permissions permissionsPlugin;

    private PermissionsEx permissionsExPlugin;

    private Permissions bPermissions;

    public Economy econ = null;

    private boolean permissionsEr = false;

    private boolean permissionsSet = false;

    public void onEnable() {
        plugin = this;
        this.server = plugin.getServer();
        fd = new FactionsDetector(this);
        PluginManager pm = this.server.getPluginManager();
        pm.registerEvents(new STListeners(plugin), plugin);
        this.pdf = plugin.getDescription();
        pluginName = this.pdf.getName();
        this.ds = new DataStore(plugin);
        this.scheduler = this.server.getScheduler();
        this.recipes = new SkullTurretRecipe(plugin);
        runTasks();
    }

    public void onDisable() {
        if (this.datafileLoaded)
            LOGGER.info(pluginName + ": Saved " + this.ds.saveDatabase(true) + " skulls to database.");
        this.ds.savePerPlayerSettings();
        this.scheduler.cancelTasks(this);
        Iterator<Recipe> rec = plugin.server.recipeIterator();
        while (rec.hasNext()) {
            Recipe r = rec.next();
            if (r.getResult().equals(this.recipes.getSkullBow()) || r.getResult().equals(this.recipes.getCrazedSkull()) || r.getResult().equals(this.recipes.getDeviousSkull()) || r.getResult().equals(this.recipes.getMasterSkull()) || r.getResult().equals(this.recipes.getWizardSkull()))
                rec.remove();
            byte b;
            int i;
            ShapelessRecipe[] arrayOfShapelessRecipe;
            for (i = (arrayOfShapelessRecipe = this.recipes.mobileDeviousSkull).length, b = 0; b < i; ) {
                ShapelessRecipe sc = arrayOfShapelessRecipe[b];
                if (r.getResult().equals(sc.getResult())) {
                    rec.remove();
                    break;
                }
                b++;
            }
            for (i = (arrayOfShapelessRecipe = this.recipes.mobileMasterSkull).length, b = 0; b < i; ) {
                ShapelessRecipe sc = arrayOfShapelessRecipe[b];
                if (r.getResult().equals(sc.getResult())) {
                    rec.remove();
                    break;
                }
                b++;
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
        this.scheduler.runTaskTimer(plugin, new Runnable() {
            public void run() {
                if (!SkullTurret.DB_UPDATE && !SkullTurret.PPS_UPDATE) {
                    Iterator<PlacedSkull> skulls = SkullTurret.this.skullMap.values().iterator();
                    boolean save = false;
                    while (skulls.hasNext()) {
                        boolean disable = SkullTurret.this.rand.nextBoolean();
                        PlacedSkull skull = skulls.next();
                        if (skull.getWorld().getPlayers().size() == 0 ||
                                !skull.isChunkLoaded() ||
                                skull.getSkullBlock().getType() != Material.SKULL)
                            continue;
                        UUID playerUUID = skull.getSkullCreator();
                        SkullCounts sc = SkullTurret.plugin.playersSkullNumber.get(playerUUID);
                        int skullLimit = SkullTurret.this.getSkullLimit(playerUUID);
                        if (sc.getActiveSkulls() > skullLimit && disable && !skull.isDisabled()) {
                            sc.disableSkull(skull);
                        } else if (sc.getActiveSkulls() < skullLimit && skull.isDisabled()) {
                            sc.enableSkull(skull);
                        }
                        if (skull.isDead()) {
                            skull.destruct();
                            sc.enableSkull(skull);
                            int numActiveSkulls = sc.getActiveSkulls();
                            numActiveSkulls--;
                            sc.setActiveSkulls(numActiveSkulls);
                            skulls.remove();
                            save = true;
                            continue;
                        }
                        skull.tick();
                    }
                    if (save)
                        SkullTurret.this.ds.saveDatabase(false);
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

    public int getSkullLimit(UUID uUID) {
        Player player = Utils.getPlayerFromUUID(uUID);
        if (player == null)
            return Integer.MAX_VALUE;
        if (hasPermission(player, "skullturret.admin"))
            return Integer.MAX_VALUE;
        int maxTurrets = MAX_SKULL_PER_PLAYER;
        PerPlayerGroups ppg = getPlayerGroup(player);
        PerPlayerSettings pps = this.perPlayerSettings.get(uUID);
        if (ppg != null && !player.isOp()) {
            maxTurrets = ppg.getMaxTurrets();
        } else if (pps != null && pps.isPps()) {
            maxTurrets = pps.getMaxTurrets();
        } else if (fd.hasFactions() && FACT_USE_FACTION_POWER) {
            int maxFactionTurrets = (int) Math.floor(Factions27Utils.getMaxPlayerTurrets(player));
            if (maxTurrets <= maxFactionTurrets)
                return maxTurrets;
            return maxFactionTurrets;
        }
        return maxTurrets;
    }

    public boolean hasTowny() {
        return (ALLOW_TOWNY && this.server.getPluginManager().getPlugin("Towny") != null);
    }

    public boolean hasDisguiseCraft() {
        return (ALLOW_DISGUISE && this.server.getPluginManager().getPlugin("DisguiseCraft") != null);
    }

    public boolean hasVanish() {
        return (ALLOW_VANISH && this.server.getPluginManager().getPlugin("VanishNoPacket") != null);
    }

    public Economy getEconomy() {
        if (USE_VAULT_ECON && getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                Economy e = rsp.getProvider();
                if (e != null)
                    LOGGER.info(pluginName + ": Economy Detected via Vault API.");
                return e;
            }
        }
        return null;
    }

    public boolean hasPermission(Player player, String permission) {
        if (NO_PERMISSIONS)
            return true;
        getPermissionsPlugin();
        if (DEBUG == 2)
            if (this.vaultPerms != null) {
                String pName = player.getName();
                String gName = this.vaultPerms.getPrimaryGroup(player);
                boolean permissions = player.hasPermission(permission);
                LOGGER.info("Vault permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            } else if (this.groupManager != null) {
                String pName = player.getName();
                String gName = this.groupManager.getWorldsHolder().getWorldData(player.getWorld().getName()).getPermissionsHandler().getGroup(player.getName());
                boolean permissions = player.hasPermission(permission);
                LOGGER.info("group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
                LOGGER.info("");
                LOGGER.info("permissions available to '" + pName + "' = " + this.groupManager.getWorldsHolder().getWorldData(player.getWorld().getName()).getGroup(gName).getPermissionList());
            } else if (this.permissionsPlugin != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String gName = Permissions.Security.getGroup(wName, pName);
                boolean permissions = player.hasPermission(permission);
                LOGGER.info("Niji permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            } else if (this.permissionsExPlugin != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String[] gNameA = PermissionsEx.getUser(player).getGroupsNames(wName);
                StringBuffer gName = new StringBuffer();
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = gNameA).length, b = 0; b < i; ) {
                    String groups = arrayOfString1[b];
                    gName.append(groups + " ");
                    b++;
                }
                boolean permissions = player.hasPermission(permission);
                LOGGER.info("PermissionsEx permissions, group for '" + pName + "' = " + gName);
                LOGGER.info("Permission for " + permission + " is " + permissions);
            } else if (this.bPermissions != null) {
                String pName = player.getName();
                String wName = player.getWorld().getName();
                String[] gNameA = ApiLayer.getGroups(wName, CalculableType.USER, pName);
                StringBuffer gName = new StringBuffer();
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = gNameA).length, b = 0; b < i; ) {
                    String groups = arrayOfString1[b];
                    gName.append(groups + " ");
                    b++;
                }
                boolean permissions = player.hasPermission(permission);
                LOGGER.info("bPermissions, group for '" + pName + "' = " + gName);
                LOGGER.info("bPermission for " + permission + " is " + permissions);
            } else if (this.server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
                LOGGER.info("Bukkit Permissions " + permission + " " + player.hasPermission(permission));
            } else if (this.permissionsEr && (player.isOp() || player.hasPermission(permission))) {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            } else {
                LOGGER.info("Unknown permissions plugin " + permission + " " + player.hasPermission(permission));
            }
        return !(!player.isOp() && !player.hasPermission(permission));
    }

    public PerPlayerGroups getPlayerGroup(UUID playerUUID) {
        Player player = Utils.getPlayerFromUUID(playerUUID);
        if (player != null)
            return getPlayerGroup(player);
        return null;
    }

    public PerPlayerGroups getPlayerGroup(Player player) {
        for (Map.Entry<String, PerPlayerGroups> g : this.perPlayerGroups.entrySet()) {
            if (hasPermission(player, "skullturret." + g.getKey()))
                return g.getValue();
        }
        return null;
    }

    private void getPermissionsPlugin() {
        if (this.server.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": Vault detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.vaultPerms = rsp.getProvider();
        } else if (this.server.getPluginManager().getPlugin("GroupManager") != null) {
            Plugin p = this.server.getPluginManager().getPlugin("GroupManager");
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": GroupManager detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.groupManager = (GroupManager) p;
        } else if (this.server.getPluginManager().getPlugin("Permissions") != null) {
            Plugin p = this.server.getPluginManager().getPlugin("Permissions");
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": Permissions detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.permissionsPlugin = (Permissions) p;
        } else if (this.server.getPluginManager().getPlugin("PermissionsBukkit") != null) {
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": Bukkit permissions detected, permissions enabled...");
                this.permissionsSet = true;
            }
        } else if (this.server.getPluginManager().getPlugin("PermissionsEx") != null) {
            Plugin p = this.server.getPluginManager().getPlugin("PermissionsEx");
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": PermissionsEx detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.permissionsExPlugin = (PermissionsEx) p;
        } else if (this.server.getPluginManager().getPlugin("bPermissions") != null) {
            Plugin p = this.server.getPluginManager().getPlugin("bPermissions");
            if (!this.permissionsSet) {
                LOGGER.info(pluginName + ": bPermissions detected, permissions enabled...");
                this.permissionsSet = true;
            }
            this.bPermissions = (Permissions) p;
        } else if (!this.permissionsEr) {
            LOGGER.info(pluginName + ": Unknown permissions detected, Using Generic Permissions...");
            this.permissionsEr = true;
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player)
            return (new CommandHandler(this)).inGame(sender, cmd, commandLabel, args);
        return (new CommandHandler(this)).inConsole(sender, cmd, commandLabel, args);
    }
}
