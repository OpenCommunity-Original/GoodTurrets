package plugin.arcwolf.skullturrets.data;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import plugin.arcwolf.skullturrets.SkullTurret;
import plugin.arcwolf.skullturrets.listener.*;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DataStore {
    private final int PPSVERSION = 3;
    private final int PPGVERSION = 1;
    private final int RCPVERSION = 1;
    private final int ETVERSION = 1;
    private final int CFGVERSION = 1;
    private final int LANGVERSION = 1;
    private final ConcurrentHashMap<String, UUID> playerNameUUID;
    private final String databaseOld;
    private final String databaseNew;
    private final String tmpDatabase;
    private final String config;
    private final File databaseFileOld;
    private final File databaseFileNew;
    private final File tmpDatabaseFile;
    private final File directory;
    public SkullTurret plugin;
    private FileConfiguration perPlayerConfig;
    private FileConfiguration perGroupConfig;
    private FileConfiguration recipeConfig;
    private FileConfiguration entityConfig;
    private File perPlayerConfigFile;
    private File perGroupConfigFile;
    private File recipeConfigFile;
    private File entityConfigFile;
    private File configFile;

    public DataStore(final SkullTurret plugin) {
        this.databaseOld = "skulls.bin";
        this.databaseNew = "skullsdat.bin";
        this.tmpDatabase = "skullsdat.tmp";
        this.config = "config.yml";
        this.perPlayerConfig = null;
        this.perGroupConfig = null;
        this.recipeConfig = null;
        this.entityConfig = null;
        this.perPlayerConfigFile = null;
        this.perGroupConfigFile = null;
        this.recipeConfigFile = null;
        this.entityConfigFile = null;
        this.configFile = null;
        this.playerNameUUID = new ConcurrentHashMap<String, UUID>(256);
        this.plugin = plugin;
        this.directory = plugin.getDataFolder();
        this.databaseFileOld = new File(this.directory, this.databaseOld);
        this.databaseFileNew = new File(this.directory, this.databaseNew);
        this.tmpDatabaseFile = new File(this.directory, this.tmpDatabase);
        this.configFile = new File(this.directory, this.config);
    }

    public boolean init() {
        boolean newSave = false;
        if (!this.directory.exists()) {
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Folder does not exist - creating it... ");
            final boolean chk = this.directory.mkdir();
            if (chk) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created folder.");
            } else {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create folder!");
            }
        }
        if (this.databaseFileOld.exists()) {
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": is updating old skullturret database to UUID Standard.");
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": This will take at least 1 minute.");
            this.updateToUUID();
            return false;
        }
        if (!this.databaseFileNew.exists()) {
            try {
                final ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.databaseFileNew));
                outputStream.writeObject("");
                outputStream.close();
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created new database file.");
            } catch (IOException e) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create database file!");
                return false;
            }
            newSave = true;
        }
        this.loadConfig();
        this.loadRecipies();
        this.loadEntities();
        this.loadPerGroupSettings();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Config Loaded.");
        if (SkullTurret.PER_PLAYER_SETTINGS) {
            this.loadPerPlayerSettings();
        }
        return this.loadDataFiles(newSave);
    }

    private void updateToUUID() {
        SkullTurret.DB_UPDATE = true;
        final String binDatabase = this.loadOldDatabaseBin();
        final Scanner scanner = new Scanner(binDatabase);
        int count = 0;
        final List<OldPlacedSkull> opslist = new ArrayList<OldPlacedSkull>();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final OldPlacedSkull ops = new OldPlacedSkull(line);
            if (ops.failed) {
                continue;
            }
            opslist.add(ops);
            ++count;
        }
        scanner.close();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": a total of " + count + " skulls in datafile to update.");
        this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                int failCount = 0;
                int successCount = 0;
                final List<PlacedSkull> skullList = new ArrayList<PlacedSkull>();
                for (final OldPlacedSkull skull : opslist) {
                    try {
                        UUID creatorID = DataStore.this.playerNameUUID.get(skull.getSkullCreator());
                        if (creatorID == null) {
                            creatorID = FindUUID.getUUIDFromPlayerName(skull.getSkullCreator());
                            DataStore.this.playerNameUUID.put(skull.getSkullCreator(), creatorID);
                        }
                        final PlacedSkull ps = new PlacedSkull(skull, creatorID);
                        final Map<UUID, PlayerNamesFoF> pnfof = new HashMap<UUID, PlayerNamesFoF>();
                        for (final Map.Entry<String, String> pfoe : skull.playerFrenemies.entrySet()) {
                            UUID playerUUID = DataStore.this.playerNameUUID.get(pfoe.getKey());
                            if (playerUUID == null) {
                                playerUUID = FindUUID.getUUIDFromPlayerName(pfoe.getKey());
                                DataStore.this.playerNameUUID.put(pfoe.getKey(), playerUUID);
                            }
                            pnfof.put(playerUUID, new PlayerNamesFoF(pfoe.getKey(), pfoe.getValue()));
                        }
                        ps.friends = skull.friends;
                        ps.enemies = skull.enemies;
                        ps.playerFrenemies = pnfof;
                        skullList.add(ps);
                    } catch (Exception e) {
                        ++failCount;
                    }
                }
                final StringBuffer databin = new StringBuffer();
                if (!DataStore.this.databaseFileNew.exists()) {
                    try {
                        final ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(DataStore.this.databaseFileNew));
                        outputStream.writeObject("");
                        outputStream.close();
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created new database file.");
                    } catch (IOException e2) {
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create database file!");
                        return;
                    }
                }
                if (DataStore.this.databaseFileNew.exists()) {
                    int count = 0;
                    for (final PlacedSkull pc : skullList) {
                        if (!pc.failed) {
                            databin.append(pc.toString() + "\n");
                            ++count;
                        }
                    }
                    DataStore.this.quickSaveDatabase(databin);
                    successCount = count;
                }
                SkullTurret.DB_UPDATE = false;
                if (failCount > 0) {
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Failed to update " + failCount + " skulls in datafile.");
                }
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Updated " + successCount + " skulls in datafile.");
            }
        });
    }

    private boolean loadDataFiles(final boolean newSave) {
        if (newSave) {
            this.saveDatabase(false);
        }
        final String binDatabase = this.loadDatabaseBin();
        final Scanner scanner = new Scanner(binDatabase);
        int count = 0;
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final Character designation = '\u001e';
            PlacedSkull pc = null;
            if (line.contains(designation.toString())) {
                pc = new MobileSkull(line);
            } else {
                pc = new PlacedSkull(line);
            }
            if (pc.failed) {
                continue;
            }
            final Location loc = pc.getSkullBlock().getLocation();
            this.plugin.skullMap.put(loc, pc);
            final UUID playerUUID = pc.getSkullCreator();
            final Player player = this.plugin.getServer().getPlayer(playerUUID);
            OfflinePlayer oPlayer = null;
            String playerName = "";
            if (player == null) {
                oPlayer = this.plugin.getServer().getOfflinePlayer(playerUUID);
            }
            if (player != null) {
                playerName = player.getName();
            } else if (player == null && oPlayer != null) {
                playerName = oPlayer.getName();
            }
            pc.setSkullCreatorLastKnowName(playerName);
            if (this.plugin.playersSkullNumber.containsKey(playerUUID)) {
                final SkullCounts sc = this.plugin.playersSkullNumber.get(playerUUID);
                int numSkulls = sc.getActiveSkulls();
                ++numSkulls;
                sc.setActiveSkulls(numSkulls);
            } else {
                this.plugin.playersSkullNumber.put(playerUUID, new SkullCounts(1, 0));
            }
            ++count;
        }
        scanner.close();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " skulls from datafile.");
        return true;
    }

    private void loadConfig() {
        if (!this.configFile.exists()) {
            this.configFile = this.saveDefaultConfig(this.configFile, "config.yml");
        }
        SkullTurret.DEBUG = this.plugin.getConfig().getInt("debug", 0);
        SkullTurret.ONLINE_UUID_CHECK = this.plugin.getConfig().getBoolean("online_uuid_check", true);
        SkullTurret.LANGUAGE = this.plugin.getConfig().getString("Language_iso_code", "enUS");
        SkullTurret.PROFILE_URL = this.plugin.getConfig().getString("profile_url", "https://api.mojang.com/profiles/minecraft");
        SkullTurret.MAX_RANGE = this.getMaxRange(false, true, "", this.plugin.getConfig().getInt("default_max_range", 15));
        SkullTurret.MAX_SKULL_PER_PLAYER = this.plugin.getConfig().getInt("skulls_per_player", 5);
        SkullTurret.TARGET_OWNED = this.plugin.getConfig().getBoolean("target_owned_animals", false);
        SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK = this.plugin.getConfig().getBoolean("crazed_devious_target_players", false);
        SkullTurret.WATCH_ONLY = this.plugin.getConfig().getBoolean("only_watch", false);
        SkullTurret.ONLY_BOW = this.plugin.getConfig().getBoolean("only_use_bow_for_targets", false);
        SkullTurret.MOB_DROPS = this.plugin.getConfig().getBoolean("mob_drops", false);
        SkullTurret.DROP = this.plugin.getConfig().getBoolean("break_skull_drop", true);
        SkullTurret.SKULLVFX = this.plugin.getConfig().getBoolean("show_skullVFX", true);
        SkullTurret.SKULLSFX = this.plugin.getConfig().getBoolean("play_skullSFX", true);
        SkullTurret.ALLOW_FRIENDLY_FIRE = this.plugin.getConfig().getBoolean("allow_friendly_fire", true);
        SkullTurret.ALLOW_TEMP_TURRETS = this.plugin.getConfig().getBoolean("allow_craft_temp_turrets", true);
        SkullTurret.ALLOW_TEMPTURRET_REARM = this.plugin.getConfig().getBoolean("allow_temp_skull_rearm", true);
        SkullTurret.DROP_BOOKS_ON_DEATH = this.plugin.getConfig().getBoolean("drop_books_on_death", false);
        SkullTurret.ALLOW_REDSTONE_DETECT = this.plugin.getConfig().getBoolean("allow_redstone_detect", true);
        SkullTurret.REDSTONE_BLOCK_MAT = this.getMaterial(this.plugin.getConfig().getString("redstone_block_mat", Material.LAPIS_BLOCK.name()));
        SkullTurret.DROP_BOOK_ON_BREAK = this.plugin.getConfig().getBoolean("drop_book_on_break", true);
        SkullTurret.USE_AMMO_CHESTS = this.plugin.getConfig().getBoolean("use_ammo_chests", true);
        SkullTurret.ALLOW_FIRE_CHARGE = this.plugin.getConfig().getBoolean("allow_fire_charge", false);
        SkullTurret.ALLOW_ARROWS = this.plugin.getConfig().getBoolean("allow_arrows", true);
        SkullTurret.ALLOW_SNOWBALLS = this.plugin.getConfig().getBoolean("allow_snowballs", false);
        SkullTurret.ALLOW_FIREBOW = this.plugin.getConfig().getBoolean("allow_fire_bow", false);
        SkullTurret.ALLOW_INFINITE_BOW = this.plugin.getConfig().getBoolean("allow_infinite_bow", false);
        SkullTurret.INCENDIARY_FIRECHARGE = this.plugin.getConfig().getBoolean("fire_charge_is_incendiary", false);
        SkullTurret.FIRETICKS = this.plugin.getConfig().getInt("fire_arrow_ticks", 100);
        SkullTurret.BOW_DUR = this.plugin.getConfig().getInt("bow_durability_per_shot", 5);
        SkullTurret.DROP_CHANCE = this.plugin.getConfig().getInt("skull_drop_chance", 50);
        SkullTurret.OFFLINE_PLAYERS = this.plugin.getConfig().getBoolean("offline_players_skulls_offline", false);
        SkullTurret.NO_PERMISSIONS = this.plugin.getConfig().getBoolean("disable_permissions_checks", false);
        SkullTurret.PER_PLAYER_SETTINGS = this.plugin.getConfig().getBoolean("allow_per_player_settings", true);
        SkullTurret.MOB_LOOT = this.plugin.getConfig().getBoolean("allow_mob_loot", true);
        SkullTurret.USE_VAULT_ECON = this.plugin.getConfig().getBoolean("use_vault_economy", false);
        SkullTurret.ECON_BOW_COST = this.plugin.getConfig().getDouble("econ_bow_price", 2.0);
        SkullTurret.ECON_CRAZED_COST = this.plugin.getConfig().getDouble("econ_crazed_skull_price", 1.0);
        SkullTurret.ECON_DEVIOUS_COST = this.plugin.getConfig().getDouble("econ_devious_skull_price", 2.0);
        SkullTurret.ECON_MASTER_COST = this.plugin.getConfig().getDouble("econ_master_skull_price", 3.0);
        SkullTurret.ECON_WIZARD_COST = this.plugin.getConfig().getDouble("econ_wizard_skull_price", 4.0);
        SkullTurret.PATROL_TIME = this.plugin.getConfig().getInt("patrol_timer", 1800);
        SkullIntelligence.CRAZED.setDamageMod(this.plugin.getConfig().getDouble("crazed_damage_mod", 6.0));
        SkullIntelligence.DEVIOUS.setDamageMod(this.plugin.getConfig().getDouble("devious_damage_mod", 8.5));
        SkullIntelligence.MASTER.setDamageMod(this.plugin.getConfig().getDouble("master_damage_mod", 9.5));
        SkullIntelligence.WIZARD.setDamageMod(this.plugin.getConfig().getDouble("wizard_damage_mod", 9.0));
        SkullIntelligence.CRAZED.setSpread(this.plugin.getConfig().getInt("crazed_accuracy_mod", 15));
        SkullIntelligence.DEVIOUS.setSpread(this.plugin.getConfig().getInt("devious_accuracy_mod", 10));
        SkullIntelligence.MASTER.setSpread(this.plugin.getConfig().getInt("master_accuracy_mod", 0));
        SkullIntelligence.WIZARD.setSpread(this.plugin.getConfig().getInt("wizard_accuracy_mod", 5));
        SkullIntelligence.CRAZED.setCooldown(this.plugin.getConfig().getInt("crazed_fire_speed", 1000));
        SkullIntelligence.DEVIOUS.setCooldown(this.plugin.getConfig().getInt("devious_fire_speed", 1300));
        SkullIntelligence.MASTER.setCooldown(this.plugin.getConfig().getInt("master_fire_speed", 1000));
        SkullIntelligence.WIZARD.setCooldown(this.plugin.getConfig().getInt("wizard_fire_speed", 2000));
        SkullIntelligence.CRAZED.setFireRangeMultiplier(this.plugin.getConfig().getDouble("crazed_firerange_mod", 0.5));
        SkullIntelligence.DEVIOUS.setFireRangeMultiplier(this.plugin.getConfig().getDouble("devious_firerange_mod", 0.75));
        SkullIntelligence.MASTER.setFireRangeMultiplier(this.plugin.getConfig().getDouble("master_firerange_mod", 0.9));
        SkullIntelligence.WIZARD.setFireRangeMultiplier(this.plugin.getConfig().getDouble("wizard_firerange_mod", 0.85));
        SkullTurret.ALLOW_SKULL_DAMAGE = this.plugin.getConfig().getBoolean("allow_skull_damage", false);
        SkullTurret.ALLOW_DAMAGED_SKULL_DESTRUCT = this.plugin.getConfig().getBoolean("allow_damaged_skull_destruct", false);
        SkullTurret.SKULLS_RETALIATE = this.plugin.getConfig().getBoolean("allow_skull_retaliate", false);
        SkullTurret.DESTRUCT_TIMER = this.plugin.getConfig().getInt("skull_destruct_timer", 90000);
        SkullIntelligence.CRAZED.setRepair_item(this.getMaterial(this.plugin.getConfig().getString("crazed_repair_item", Material.STICK.name())));
        SkullIntelligence.DEVIOUS.setRepair_item(this.getMaterial(this.plugin.getConfig().getString("devious_repair_item", Material.BONE.name())));
        SkullIntelligence.MASTER.setRepair_item(this.getMaterial(this.plugin.getConfig().getString("master_repair_item", Material.DIAMOND.name())));
        SkullIntelligence.WIZARD.setRepair_item(this.getMaterial(this.plugin.getConfig().getString("wizard_repair_item", Material.GOLD_INGOT.name())));
        SkullTurret.SKULL_REPAIR_AMOUNT = this.plugin.getConfig().getDouble("skull_repair_amount", 5.0);
        SkullTurret.SKULL_DAMAGE_RECOVERY_TIME = this.plugin.getConfig().getInt("skull_damage_recovery_time", 15000);
        SkullIntelligence.CRAZED.setHealth(this.plugin.getConfig().getDouble("crazed_health", 30.0));
        SkullIntelligence.DEVIOUS.setHealth(this.plugin.getConfig().getDouble("devious_health", 40.0));
        SkullIntelligence.MASTER.setHealth(this.plugin.getConfig().getDouble("master_health", 60.0));
        SkullIntelligence.WIZARD.setHealth(this.plugin.getConfig().getDouble("wizard_health", 50.0));
        SkullTurret.ALLOW_ARROW_DAMAGE = this.plugin.getConfig().getBoolean("allow_arrow_damage", true);
        this.loadWeapons();
        this.loadAmmoList();
        this.loadNoDropWorlds();
        this.loadDefaultLang();
        if (!SkullTurret.RELOAD) {
            this.rewriteConfig();
            SkullTurret.RELOAD = false;
        }
    }

    private void loadDefaultLang() {
        final File localeFile = new File(this.plugin.getDataFolder(), "locale_" + SkullTurret.LANGUAGE + ".yml");
        if (!localeFile.exists()) {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Could not find locale_" + SkullTurret.LANGUAGE + ".yml Defaulting to enUS.");
            SkullTurret.LANGUAGE = "enUS";
            this.saveDefaultConfig(localeFile, "locale_" + SkullTurret.LANGUAGE + ".yml");
        } else if (!Utils.getLocalization("Version").equals(Integer.toString(1))) {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Wrong localization file version found. Expected " + 1);
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": default locale_enUS.yml updated in plugin directory.");
            localeFile.delete();
            this.saveDefaultConfig(localeFile, "locale_enUS.yml");
        }
        final String version = Utils.getLocalization("Version");
        final String author = Utils.getLocalization("Author");
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded Language File: " + SkullTurret.LANGUAGE + " Version: " + version + " Author: " + author);
    }

    public void loadNoDropWorlds() {
        final List<String> worldsSec = (List<String>) this.plugin.getConfig().getList("NO_SKULL_DROP_WORLD_LIST");
        if (worldsSec != null) {
            SkullTurret.NO_DROP_WORLDS.addAll(worldsSec);
        }
    }

    private void rewriteConfig() {
        FileWriter fwriter = null;
        BufferedWriter writer = null;
        try {
            fwriter = new FileWriter(this.configFile);
            writer = new BufferedWriter(fwriter);
            writer.write("# Do not modify this value!\r\n");
            writer.write("Version: 1\r\n");
            writer.write("\r\n");
            writer.write("# Used for alpha / beta debugging\r\n");
            writer.write("debug: " + SkullTurret.DEBUG + "\r\n");
            writer.write("\r\n");
            writer.write("# Allow plugin to check online for UUID\r\n");
            writer.write("online_uuid_check: " + SkullTurret.ONLINE_UUID_CHECK + "\r\n");
            writer.write("\r\n");
            writer.write("#Changes language for skull turret player message interactions\r\n");
            writer.write("Language_iso_code: " + SkullTurret.LANGUAGE + "\r\n");
            writer.write("\r\n");
            writer.write("# The URL for user profiles for use with UUID retrieval\r\n");
            writer.write("profile_url: " + SkullTurret.PROFILE_URL + "\r\n");
            writer.write("\r\n");
            writer.write("# Maximum range a skull will detect targets\r\n");
            writer.write("# Must be a multiple of 3\r\n");
            writer.write("default_max_range: " + SkullTurret.MAX_RANGE + "\r\n");
            writer.write("\r\n");
            writer.write("# Maximum skulls allow to be placed by a single player\r\n");
            writer.write("skulls_per_player: " + SkullTurret.MAX_SKULL_PER_PLAYER + "\r\n");
            writer.write("\r\n");
            writer.write("# If skulls should be allowed to target ownened animals\r\n");
            writer.write("# Owned animals are animals with custom names or on leashes\r\n");
            writer.write("target_owned_animals: " + SkullTurret.TARGET_OWNED + "\r\n");
            writer.write("\r\n");
            writer.write("# If true crazed and devious skulls will target and attack players\r\n");
            writer.write("crazed_devious_target_players: " + SkullTurret.ALLOW_CRAZED_DEVIOUS_PLAYER_ATTACK + "\r\n");
            writer.write("\r\n");
            writer.write("# Skulls will only target and never shoot\r\n");
            writer.write("# Kind of creepy\r\n");
            writer.write("only_watch: " + SkullTurret.WATCH_ONLY + "\r\n");
            writer.write("\r\n");
            writer.write("# Skulls will only target Living Entities shot with the skull bow\r\n");
            writer.write("only_use_bow_for_targets: " + SkullTurret.ONLY_BOW + "\r\n");
            writer.write("\r\n");
            writer.write("# If true skeletons have a chance of dropping a skull\r\n");
            writer.write("# that can be used to craft skull turrets\r\n");
            writer.write("mob_drops: " + SkullTurret.MOB_DROPS + "\r\n");
            writer.write("\r\n");
            writer.write("# The probability that a skeleton will drop a skull\r\n");
            writer.write("# Default is 50 or a 1 in 50 chance a skeleton will drop a skull\r\n");
            writer.write("skull_drop_chance: " + SkullTurret.DROP_CHANCE + "\r\n");
            writer.write("\r\n");
            writer.write("# This is a Black List of worlds in which skulls should never drop\r\n");
            writer.write("# Example list:\r\n");
            writer.write("# NO_SKULL_DROP_WORLD_LIST:\r\n");
            writer.write("#   - MyWorld\r\n");
            writer.write("#   - AnotherWorldName\r\n");
            writer.write("#   - Some Bogus World\r\n");
            writer.write("NO_SKULL_DROP_WORLD_LIST:\r\n");
            for (final String worlds : SkullTurret.NO_DROP_WORLDS) {
                writer.write("  - " + worlds + "\r\n");
            }
            writer.write("##############\r\n");
            writer.write("\r\n");
            writer.write("# If true skull turrets will drop naturally if they are broken\r\n");
            writer.write("break_skull_drop: " + SkullTurret.DROP + "\r\n");
            writer.write("\r\n");
            writer.write("# Disables or enables the Visual Effects displayed by a\r\n");
            writer.write("# activated Skull Turret\r\n");
            writer.write("show_skullVFX: " + SkullTurret.SKULLVFX + "\r\n");
            writer.write("\r\n");
            writer.write("# Disables or enables the Sound Effects played by a\r\n");
            writer.write("# activated Skull Turret\r\n");
            writer.write("play_skullSFX: " + SkullTurret.SKULLSFX + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to prevent Skulls from accidently shooting you in the back\r\n");
            writer.write("allow_friendly_fire: " + SkullTurret.ALLOW_FRIENDLY_FIRE + "\r\n");
            writer.write("\r\n");
            writer.write("# If true allows Temporary turrets to be crafted\r\n");
            writer.write("allow_craft_temp_turrets: " + SkullTurret.ALLOW_TEMP_TURRETS + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to allow Temporary Skull to be rearmed with arrows\r\n");
            writer.write("# If this is false Temporary Skull will self destruct when out of ammo\r\n");
            writer.write("allow_temp_skull_rearm: " + SkullTurret.ALLOW_TEMPTURRET_REARM + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to enable Vault based economy support\r\n");
            writer.write("use_vault_economy: " + SkullTurret.USE_VAULT_ECON + "\r\n");
            writer.write("\r\n");
            writer.write("# ***VAULT SPECIFIC OPTIONS***\r\n");
            writer.write("# Only enabled if Vault economy support is enabled\r\n");
            writer.write("# \r\n");
            writer.write("econ_bow_price: " + SkullTurret.ECON_BOW_COST + "\r\n");
            writer.write("econ_crazed_skull_price: " + SkullTurret.ECON_CRAZED_COST + "\r\n");
            writer.write("econ_devious_skull_price: " + SkullTurret.ECON_DEVIOUS_COST + "\r\n");
            writer.write("econ_master_skull_price: " + SkullTurret.ECON_MASTER_COST + "\r\n");
            writer.write("econ_wizard_skull_price: " + SkullTurret.ECON_WIZARD_COST + "\r\n");
            writer.write("# ***END VAULT CONFIG****\r\n");
            writer.write("\r\n");
            writer.write("# Setting to allow skull knowledge books to drop from players who die\r\n");
            writer.write("drop_books_on_death: " + SkullTurret.DROP_BOOKS_ON_DEATH + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to allow skull knowledge books to drop from broken skulls\r\n");
            writer.write("drop_book_on_break: " + SkullTurret.DROP_BOOK_ON_BREAK + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows skulls to detect redstone\r\n");
            writer.write("allow_redstone_detect: " + SkullTurret.ALLOW_REDSTONE_DETECT + "\r\n");
            writer.write("\r\n");
            writer.write("# The redstone block material name to use for redstone detection\r\n");
            writer.write("# The CraftBukkit material values can be found at:\r\n");
            writer.write("# http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html\r\n");
            writer.write("# AIR is also valid. Any block will be used\r\n");
            writer.write("redstone_block_mat: " + SkullTurret.REDSTONE_BLOCK_MAT.name() + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to make skulls use ammo chests\r\n");
            writer.write("use_ammo_chests: " + SkullTurret.USE_AMMO_CHESTS + "\r\n");
            writer.write("\r\n");
            writer.write("# Types of ammo allowed in ammo chests\r\n");
            writer.write("allow_fire_charge: " + SkullTurret.ALLOW_FIRE_CHARGE + "\r\n");
            writer.write("allow_arrows: " + SkullTurret.ALLOW_ARROWS + "\r\n");
            writer.write("allow_snowballs: " + SkullTurret.ALLOW_SNOWBALLS + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to make fire charges incendiary (can spread fire)\r\n");
            writer.write("fire_charge_is_incendiary: " + SkullTurret.INCENDIARY_FIRECHARGE + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows fire enchanted bows in ammo chest to set fire aspect of\r\n");
            writer.write("# Skull arrow ammo\r\n");
            writer.write("allow_fire_bow: " + SkullTurret.ALLOW_FIREBOW + "\r\n");
            writer.write("\r\n");
            writer.write("# Length of time a target is on fire from a fire arrow\r\n");
            writer.write("# Default 100 server ticks (5 seconds)\r\n");
            writer.write("fire_arrow_ticks: " + SkullTurret.FIRETICKS + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows Infinity enchanted bows in ammo chest to allow skulls to temporarily\r\n");
            writer.write("# shoot infinite arrows\r\n");
            writer.write("allow_infinite_bow: " + SkullTurret.ALLOW_INFINITE_BOW + "\r\n");
            writer.write("\r\n");
            writer.write("# Amount of durability to subtract from enchanted bows in ammo\r\n");
            writer.write("# chests\r\n");
            writer.write("bow_durability_per_shot: " + SkullTurret.BOW_DUR + "\r\n");
            writer.write("\r\n");
            writer.write("# If set to true any player that is offline will have their skulls offline\r\n");
            writer.write("# as well\r\n");
            writer.write("offline_players_skulls_offline: " + SkullTurret.OFFLINE_PLAYERS + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to disable all permissions checks\r\n");
            writer.write("disable_permissions_checks: " + SkullTurret.NO_PERMISSIONS + "\r\n");
            writer.write("\r\n");
            writer.write("# Enables per player settings for things like Range and Max Turrets\r\n");
            writer.write("# Settings in playerinfo.yml\r\n");
            writer.write("allow_per_player_settings: " + SkullTurret.PER_PLAYER_SETTINGS + "\r\n");
            writer.write("\r\n");
            writer.write("# Any mob that has ever been shot by a skull turret will not drop loot\r\n");
            writer.write("# or exp after death. Regardless of who got the killing blow\r\n");
            writer.write("allow_mob_loot: " + SkullTurret.MOB_LOOT + "\r\n");
            writer.write("\r\n");
            writer.write("# Sets amount of time a skull remains looking in a direction\r\n");
            writer.write("# Time in Milliseconds\r\n");
            writer.write("patrol_timer: " + SkullTurret.PATROL_TIME + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows changing of damage of skull arrows\r\n");
            writer.write("crazed_damage_mod: " + SkullIntelligence.CRAZED.getDamageMod() + "\r\n");
            writer.write("devious_damage_mod: " + SkullIntelligence.DEVIOUS.getDamageMod() + "\r\n");
            writer.write("master_damage_mod: " + SkullIntelligence.MASTER.getDamageMod() + "\r\n");
            writer.write("wizard_damage_mod: " + SkullIntelligence.WIZARD.getDamageMod() + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows changing of the accuracy of a skulls arrows\r\n");
            writer.write("# Lower values are better\r\n");
            writer.write("crazed_accuracy_mod: " + SkullIntelligence.CRAZED.getSpread() + "\r\n");
            writer.write("devious_accuracy_mod: " + SkullIntelligence.DEVIOUS.getSpread() + "\r\n");
            writer.write("master_accuracy_mod: " + SkullIntelligence.MASTER.getSpread() + "\r\n");
            writer.write("wizard_accuracy_mod: " + SkullIntelligence.WIZARD.getSpread() + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows changing of the firing speed of a skulls\r\n");
            writer.write("# Time in Millisecond, quicker then 1000 wont have much effect\r\n");
            writer.write("crazed_fire_speed: " + SkullIntelligence.CRAZED.getCooldown() + "\r\n");
            writer.write("devious_fire_speed: " + SkullIntelligence.DEVIOUS.getCooldown() + "\r\n");
            writer.write("master_fire_speed: " + SkullIntelligence.MASTER.getCooldown() + "\r\n");
            writer.write("wizard_fire_speed: " + SkullIntelligence.WIZARD.getCooldown() + "\r\n");
            writer.write("\r\n");
            writer.write("# Allows changing of the firing range of skulls\r\n");
            writer.write("# Firing range is a percentage of max range, the area around the skull where\r\n");
            writer.write("# Skulls will track and fire at targets\r\n");
            writer.write("# 0.9 is 90% of max range no values greater then 1.0 will work\r\n");
            writer.write("crazed_firerange_mod: " + SkullIntelligence.CRAZED.getFireRangeMultiplier() + "\r\n");
            writer.write("devious_firerange_mod: " + SkullIntelligence.DEVIOUS.getFireRangeMultiplier() + "\r\n");
            writer.write("master_firerange_mod: " + SkullIntelligence.MASTER.getFireRangeMultiplier() + "\r\n");
            writer.write("wizard_firerange_mod: " + SkullIntelligence.WIZARD.getFireRangeMultiplier() + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting allows skulls to take damage from players\r\n");
            writer.write("allow_skull_damage: " + SkullTurret.ALLOW_SKULL_DAMAGE + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting allows damaged skulls to be destroyed\r\n");
            writer.write("allow_damaged_skull_destruct: " + SkullTurret.ALLOW_SKULL_DAMAGE + "\r\n");
            writer.write("\r\n");
            writer.write("# Like pig zombies, If you attack a skull all skulls owned by that player will choose\r\n");
            writer.write("# you as a target. However, unlike pig zombies skulls forget after a minute of not being\r\n");
            writer.write("# attacked\r\n");
            writer.write("allow_skull_retaliate: " + SkullTurret.SKULLS_RETALIATE + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting is used to set the time before a skull destructs from damage\r\n");
            writer.write("# Time is in milliseconds\r\n");
            writer.write("skull_destruct_timer: " + SkullTurret.DESTRUCT_TIMER + "\r\n");
            writer.write("\r\n");
            writer.write("# These are the items that can be used to repair a skull\r\n");
            writer.write("crazed_repair_item: " + SkullIntelligence.CRAZED.getRepair_item() + "\r\n");
            writer.write("devious_repair_item: " + SkullIntelligence.DEVIOUS.getRepair_item() + "\r\n");
            writer.write("master_repair_item: " + SkullIntelligence.MASTER.getRepair_item() + "\r\n");
            writer.write("wizard_repair_item: " + SkullIntelligence.WIZARD.getRepair_item() + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting is used to set if the repair consumes the repair item\r\n");
            writer.write("consume_repair_item: " + SkullTurret.CONSUME_REPAIR_ITEM + "\r\n");
            writer.write("\r\n");
            writer.write("# This is the amount of health that is readded to the skulls health per repair hit\r\n");
            writer.write("skull_repair_amount: " + SkullTurret.SKULL_REPAIR_AMOUNT + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting sets the amount of time before a slightly damaged skull will recover\r\n");
            writer.write("# If a skull is damage but does not have a health of 0 then the Skull naturally recovers all health\r\n");
            writer.write("# after 15 seconds of not being attacked\r\n");
            writer.write("# Time is in milliseconds\r\n");
            writer.write("skull_damage_recovery_time: " + SkullTurret.SKULL_DAMAGE_RECOVERY_TIME + "\r\n");
            writer.write("\r\n");
            writer.write("# This is a list of Skull Health\r\n");
            writer.write("# These values are roughly based on the Health of various hostile mobs from minecraft wiki\r\n");
            writer.write("# http://minecraft.gamepedia.com/Health\r\n");
            writer.write("crazed_health: " + SkullIntelligence.CRAZED.getHealth() + "\r\n");
            writer.write("devious_health: " + SkullIntelligence.DEVIOUS.getHealth() + "\r\n");
            writer.write("master_health: " + SkullIntelligence.MASTER.getHealth() + "\r\n");
            writer.write("wizard_health: " + SkullIntelligence.WIZARD.getHealth() + "\r\n");
            writer.write("\r\n");
            writer.write("# This setting enables or disables skulls taking damage from player bow shots\r\n");
            writer.write("# Arrow damage is scaled based on force (fully charged = 9.0 no charge = 1.0) No random chance for 10\r\n");
            writer.write("# Damage based on minecraft wiki http://minecraft.gamepedia.com/Arrow\r\n");
            writer.write("allow_arrow_damage: " + SkullTurret.ALLOW_ARROW_DAMAGE + "\r\n");
            writer.write("\r\n");
            writer.write("# This is a list of weapons that will hurt Skulls and their damage values\r\n");
            writer.write("# Note Air is for an empty hand\r\n");
            writer.write("# A list of valid materials are found here http://jd.bukkit.org/rb/apidocs/org/bukkit/Material.html\r\n");
            writer.write("Weapons:\r\n");
            for (final Map.Entry<Material, Double> weap : this.plugin.weapons.entrySet()) {
                final Material mat = weap.getKey();
                final double damage = weap.getValue();
                writer.write("  " + mat.name() + ":\r\n");
                writer.write("    Damage: " + damage + "\r\n");
            }
        } catch (Exception e) {
            SkullTurret.LOGGER.log(Level.SEVERE, "Exception while creating " + this.configFile, e);
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e2) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
                }
            }
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (IOException e2) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
                }
            }
            return;
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e2) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
                }
            }
            if (fwriter != null) {
                try {
                    fwriter.close();
                } catch (IOException e2) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
                }
            }
        }
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e2) {
                SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
            }
        }
        if (fwriter != null) {
            try {
                fwriter.close();
            } catch (IOException e2) {
                SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e2);
            }
        }
    }

    public void loadRecipies() {
        final int version = this.getRecipeConfig().getInt("Version", 0);
        final char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
        final String[] matrixString = {"abc", "def", "ghi"};
        if (version == 1) {
            final ConfigurationSection crazed = this.getRecipeConfig().getConfigurationSection("Crazed");
            final String[] matrix = new String[9];
            Material[] matrixDefault = {Material.AIR, Material.REDSTONE, Material.AIR, Material.SPIDER_EYE, Material.SKELETON_SKULL, Material.SPIDER_EYE, Material.AIR, Material.STICK, Material.AIR};
            matrix[0] = crazed.getString("TopLeft", "AIR");
            matrix[1] = crazed.getString("TopMiddle", "REDSTONE");
            matrix[2] = crazed.getString("TopRight", "AIR");
            matrix[3] = crazed.getString("CenterLeft", "SPIDER_EYE");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = crazed.getString("CenterRight", "SPIDER_EYE");
            matrix[6] = crazed.getString("BottomLeft", "AIR");
            matrix[7] = crazed.getString("BottomMiddle", "STICK");
            matrix[8] = crazed.getString("BottomRight", "AIR");
            final ShapedRecipe crazedSkull = new ShapedRecipe(this.plugin.recipes.crazedSkullItem);
            crazedSkull.shape(matrixString);
            for (int i = 0; i < 9; ++i) {
                final Material mat = Material.getMaterial(matrix[i]);
                if (mat != Material.AIR) {
                    if (mat != null) {
                        crazedSkull.setIngredient(letters[i], mat);
                    } else {
                        final Dye dye = new Dye();
                        try {
                            final DyeColor dc = DyeColor.valueOf(matrix[i]);
                            dye.setColor(dc);
                            crazedSkull.setIngredient(letters[i], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[i] + " Using default: " + matrixDefault[i]);
                            crazedSkull.setIngredient(letters[i], matrixDefault[i]);
                        }
                    }
                }
            }
            final ConfigurationSection devious = this.getRecipeConfig().getConfigurationSection("Devious");
            matrixDefault = new Material[]{Material.AIR, Material.GLOWSTONE_DUST, Material.AIR, Material.ENDER_PEARL, Material.SKELETON_SKULL, Material.ENDER_PEARL, Material.AIR, Material.BONE, Material.AIR};
            matrix[0] = devious.getString("TopLeft", "AIR");
            matrix[1] = devious.getString("TopMiddle", "GLOWSTONE_DUST");
            matrix[2] = devious.getString("TopRight", "AIR");
            matrix[3] = devious.getString("CenterLeft", "ENDER_PEARL");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = devious.getString("CenterRight", "ENDER_PEARL");
            matrix[6] = devious.getString("BottomLeft", "AIR");
            matrix[7] = devious.getString("BottomMiddle", "BONE");
            matrix[8] = devious.getString("BottomRight", "AIR");
            final ShapedRecipe deviousSkull = new ShapedRecipe(this.plugin.recipes.deviousSkullItem);
            deviousSkull.shape(matrixString);
            for (int j = 0; j < 9; ++j) {
                final Material mat2 = Material.getMaterial(matrix[j]);
                if (mat2 != Material.AIR) {
                    if (mat2 != null) {
                        deviousSkull.setIngredient(letters[j], mat2);
                    } else {
                        final Dye dye2 = new Dye();
                        try {
                            final DyeColor dc2 = DyeColor.valueOf(matrix[j]);
                            dye2.setColor(dc2);
                            deviousSkull.setIngredient(letters[j], dye2.toItemStack().getData());
                        } catch (Exception e2) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[j] + " Using default: " + matrixDefault[j]);
                            deviousSkull.setIngredient(letters[j], matrixDefault[j]);
                        }
                    }
                }
            }
            final ConfigurationSection master = this.getRecipeConfig().getConfigurationSection("Master");
            matrixDefault = new Material[]{Material.GHAST_TEAR, Material.WRITABLE_BOOK, Material.GHAST_TEAR, Material.ENDER_EYE, Material.SKELETON_SKULL, Material.ENDER_EYE, Material.AIR, Material.BLAZE_ROD, Material.AIR};
            matrix[0] = master.getString("TopLeft", "GHAST_TEAR");
            matrix[1] = master.getString("TopMiddle", "BOOK_AND_QUILL");
            matrix[2] = master.getString("TopRight", "GHAST_TEAR");
            matrix[3] = master.getString("CenterLeft", "EYE_OF_ENDER");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = master.getString("CenterRight", "EYE_OF_ENDER");
            matrix[6] = master.getString("BottomLeft", "AIR");
            matrix[7] = master.getString("BottomMiddle", "BLAZE_ROD");
            matrix[8] = master.getString("BottomRight", "AIR");
            final ShapedRecipe masterSkull = new ShapedRecipe(this.plugin.recipes.masterSkullItem);
            masterSkull.shape(matrixString);
            for (int k = 0; k < 9; ++k) {
                final Material mat3 = Material.getMaterial(matrix[k]);
                if (mat3 != Material.AIR) {
                    if (mat3 != null) {
                        masterSkull.setIngredient(letters[k], mat3);
                    } else {
                        final Dye dye3 = new Dye();
                        try {
                            final DyeColor dc3 = DyeColor.valueOf(matrix[k]);
                            dye3.setColor(dc3);
                            masterSkull.setIngredient(letters[k], dye3.toItemStack().getData());
                        } catch (Exception e3) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[k] + " Using default: " + matrixDefault[k]);
                            masterSkull.setIngredient(letters[k], matrixDefault[k]);
                        }
                    }
                }
            }
            final ConfigurationSection wizard = this.getRecipeConfig().getConfigurationSection("Wizard");
            matrixDefault = new Material[]{null, Material.AIR, Material.BLAZE_ROD, Material.AIR, Material.SKELETON_SKULL, Material.AIR, Material.GLOWSTONE_DUST, Material.MAGMA_CREAM, Material.GLOWSTONE_DUST};
            matrix[0] = wizard.getString("TopLeft", "GRAY");
            matrix[1] = wizard.getString("TopMiddle", "AIR");
            matrix[2] = wizard.getString("TopRight", "BLAZE_ROD");
            matrix[3] = wizard.getString("CenterLeft", "AIR");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = wizard.getString("CenterRight", "AIR");
            matrix[6] = wizard.getString("BottomLeft", "GLOWSTONE_DUST");
            matrix[7] = wizard.getString("BottomMiddle", "MAGMA_CREAM");
            matrix[8] = wizard.getString("BottomRight", "GLOWSTONE_DUST");
            final ShapedRecipe wizardSkull = new ShapedRecipe(this.plugin.recipes.wizardSkullItem);
            wizardSkull.shape(matrixString);
            for (int l = 0; l < 9; ++l) {
                final Material mat4 = Material.getMaterial(matrix[l]);
                if (mat4 != Material.AIR) {
                    if (mat4 != null) {
                        wizardSkull.setIngredient(letters[l], mat4);
                    } else {
                        final Dye dye4 = new Dye();
                        try {
                            final DyeColor dc4 = DyeColor.valueOf(matrix[l]);
                            dye4.setColor(dc4);
                            wizardSkull.setIngredient(letters[l], dye4.toItemStack().getData());
                        } catch (Exception e4) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[l] + " Using default: " + matrixDefault[l]);
                            if (matrixDefault[l] != null) {
                                wizardSkull.setIngredient(letters[l], matrixDefault[l]);
                            } else {
                                dye4.setColor(DyeColor.GRAY);
                                wizardSkull.setIngredient(letters[l], dye4.toItemStack().getData());
                            }
                        }
                    }
                }
            }
            final ConfigurationSection skullBow = this.getRecipeConfig().getConfigurationSection("Skull_Bow");
            matrixDefault = new Material[]{Material.AIR, Material.AIR, Material.AIR, Material.SLIME_BALL, Material.BOW, Material.ENDER_EYE, Material.AIR, Material.AIR, Material.AIR};
            matrix[0] = skullBow.getString("TopLeft", "AIR");
            matrix[1] = skullBow.getString("TopMiddle", "AIR");
            matrix[2] = skullBow.getString("TopRight", "AIR");
            matrix[3] = skullBow.getString("CenterLeft", "SLIME_BALL");
            matrix[4] = "BOW";
            matrix[5] = skullBow.getString("CenterRight", "EYE_OF_ENDER");
            matrix[6] = skullBow.getString("BottomLeft", "AIR");
            matrix[7] = skullBow.getString("BottomMiddle", "AIR");
            matrix[8] = skullBow.getString("BottomRight", "AIR");
            final ShapedRecipe skullBowRecipe = new ShapedRecipe(this.plugin.recipes.bowTargetItem);
            skullBowRecipe.shape(matrixString);
            for (int m = 0; m < 9; ++m) {
                final Material mat5 = Material.getMaterial(matrix[m]);
                if (mat5 != Material.AIR) {
                    if (mat5 != null) {
                        skullBowRecipe.setIngredient(letters[m], mat5);
                    } else {
                        final Dye dye5 = new Dye();
                        try {
                            final DyeColor dc5 = DyeColor.valueOf(matrix[m]);
                            dye5.setColor(dc5);
                            skullBowRecipe.setIngredient(letters[m], dye5.toItemStack().getData());
                        } catch (Exception e5) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[m] + " Using default: " + matrixDefault[m]);
                            skullBowRecipe.setIngredient(letters[m], matrixDefault[m]);
                        }
                    }
                }
            }
            final Iterator<Recipe> rec = this.plugin.server.recipeIterator();
            boolean recupdated = false;
            while (rec.hasNext()) {
                final Recipe r = rec.next();
                if (r.getResult().equals(this.plugin.recipes.getSkullBow()) || r.getResult().equals(this.plugin.recipes.getCrazedSkull()) || r.getResult().equals(this.plugin.recipes.getDeviousSkull()) || r.getResult().equals(this.plugin.recipes.getMasterSkull()) || r.getResult().equals(this.plugin.recipes.getWizardSkull())) {
                    rec.remove();
                    recupdated = true;
                }
            }
            if (recupdated) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": updated recipes...");
            }
            if (crazed.getBoolean("Enabled", true)) {
                this.plugin.server.addRecipe(crazedSkull);
            }
            if (devious.getBoolean("Enabled", true)) {
                this.plugin.server.addRecipe(deviousSkull);
            }
            if (master.getBoolean("Enabled", true)) {
                this.plugin.server.addRecipe(masterSkull);
            }
            if (wizard.getBoolean("Enabled", true)) {
                this.plugin.server.addRecipe(wizardSkull);
            }
            if (skullBow.getBoolean("Enabled", true)) {
                this.plugin.server.addRecipe(skullBowRecipe);
            }
            SkullTurret.CRAZED_STACK_SIZE = this.validateStackSize(crazed.getInt("CraftedAmount", 1));
            SkullTurret.DEVIOUS_STACK_SIZE = this.validateStackSize(devious.getInt("CraftedAmount", 1));
            SkullTurret.MASTER_STACK_SIZE = this.validateStackSize(master.getInt("CraftedAmount", 1));
            SkullTurret.WIZARD_STACK_SIZE = this.validateStackSize(wizard.getInt("CraftedAmount", 1));
            SkullTurret.BOW_STACK_SIZE = this.validateStackSize(skullBow.getInt("CraftedAmount", 1));
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Recipe Config version. Found " + version + " expected " + 1);
        }
    }

    private int validateStackSize(final int amount) {
        if (amount > 0 && amount <= 64) {
            return amount;
        }
        return 1;
    }

    public void loadWeapons() {
        final int version = this.plugin.getConfig().getInt("Version", 0);
        int count = 0;
        if (version == 1) {
            final ConfigurationSection weapons = this.plugin.getConfig().getConfigurationSection("Weapons");
            for (final String weaponName : weapons.getKeys(false)) {
                final ConfigurationSection pSec = weapons.getConfigurationSection(weaponName);
                final Material wep = this.getMaterial(weaponName);
                if (wep != null) {
                    final double damage = pSec.getDouble("Damage", 1.0);
                    this.plugin.weapons.put(wep, damage);
                    ++count;
                } else {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Weapon type " + weaponName);
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " Weapons from config.");
        } else if (version == 0) {
            this.plugin.weapons.put(Material.AIR, 1.0);
            this.plugin.weapons.put(Material.WOODEN_SWORD, 5.0);
            this.plugin.weapons.put(Material.GOLDEN_SWORD, 5.0);
            this.plugin.weapons.put(Material.STONE_SWORD, 6.0);
            this.plugin.weapons.put(Material.IRON_SWORD, 7.0);
            this.plugin.weapons.put(Material.DIAMOND_SWORD, 8.0);
            this.plugin.weapons.put(Material.WOODEN_AXE, 4.0);
            this.plugin.weapons.put(Material.GOLDEN_AXE, 4.0);
            this.plugin.weapons.put(Material.STONE_AXE, 5.0);
            this.plugin.weapons.put(Material.IRON_AXE, 6.0);
            this.plugin.weapons.put(Material.DIAMOND_AXE, 7.0);
            count = this.plugin.weapons.size();
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Weapon config version. Found: " + version + " Expected: " + 1);
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " default Weapons.");
        }
    }

    public void loadEntities() {
        final int version = this.getEntityConfig().getInt("Version", 0);
        if (version == 1) {
            final ConfigurationSection entities = this.getEntityConfig().getConfigurationSection("EntityType");
            int count = 0;
            for (final String entityName : entities.getKeys(false)) {
                final ConfigurationSection pSec = entities.getConfigurationSection(entityName);
                final EntityType et = this.getEntityType(entityName);
                if (et != null) {
                    final int rating = pSec.getInt("Rating");
                    final boolean canPoison = pSec.getBoolean("CanPoison", true);
                    final boolean mustHeal = pSec.getBoolean("MustHeal", false);
                    this.plugin.entities.put(et, new EntitySettings(rating, canPoison, mustHeal));
                    ++count;
                } else {
                    SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Entity type " + entityName);
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " entities from datafile.");
            final ConfigurationSection names = this.getEntityConfig().getConfigurationSection("CustomNames");
            count = 0;
            for (final String customName : names.getKeys(false)) {
                final ConfigurationSection pSec2 = names.getConfigurationSection(customName);
                final String[] name = pSec2.getString("Entities").split(",");
                final List<String> checkedNames = new ArrayList<>();
                String[] array;
                for (int length = (array = name).length, i = 0; i < length; ++i) {
                    final String n = array[i];
                    final String formated = n.toUpperCase().trim();
                    if (this.getEntityType(formated) == null) {
                        SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Entity type in CustomNames " + n);
                    } else {
                        checkedNames.add(formated);
                    }
                }
                this.plugin.customNames.put(customName, checkedNames);
                ++count;
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " CustomNames from datafile.");
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown EntityConfig version. Found " + version + " expected " + 1);
        }
    }

    private EntityType getEntityType(final String entityName) {
        EntityType et = null;
        try {
            et = EntityType.valueOf(entityName);
            return et;
        } catch (Exception e) {
            return null;
        }
    }

    public void loadPerPlayerSettings() {
        final int version = this.getPerPlayerConfig().getInt("Version", 0);
        if (version == 3) {
            final ConfigurationSection players = this.getPerPlayerConfig().getConfigurationSection("Players");
            int count = 0;
            for (final String playerUUIDString : players.getKeys(false)) {
                final UUID playerUUID = UUID.fromString(playerUUIDString);
                final ConfigurationSection pSec = players.getConfigurationSection(playerUUIDString);
                final String lastKnownName = pSec.getString("LastKnownName", "Unknown");
                final int maxTurret = pSec.getInt("MaxTurrets", SkullTurret.MAX_SKULL_PER_PLAYER);
                int maxRange = pSec.getInt("MaxRange", SkullTurret.MAX_RANGE);
                final boolean ppsSet = pSec.getBoolean("PPS", false);
                final boolean masterDefaults = pSec.getBoolean("MasterDefaults", false);
                final String masterSkinName = pSec.getString("MasterDefaultSkin", "");
                final String ammoType = pSec.getString("DefaultAmmoType", "arrow");
                final boolean masterRedstone = pSec.getBoolean("MasterDefaultRedstone", false);
                final boolean masterPatrol = pSec.getBoolean("MasterDefaultPatrol", true);
                final boolean wizardDefaults = pSec.getBoolean("WizardDefaults", false);
                final String wizardSkinName = pSec.getString("WizardDefaultSkin", "");
                final boolean wizardRedstone = pSec.getBoolean("WizardDefaultRedstone", false);
                final boolean wizardPatrol = pSec.getBoolean("WizardDefaultPatrol", true);
                PerPlayerSettings pps = null;
                maxRange = this.getMaxRange(false, false, playerUUIDString, maxRange);
                if (ppsSet) {
                    pps = new PerPlayerSettings(playerUUID, maxTurret, maxRange);
                    if (masterDefaults) {
                        pps.setMasterDefaults(true);
                        pps.setMasterSkinName(masterSkinName);
                        pps.setAmmoTypeName(ammoType);
                        pps.setMasterRedstone(masterRedstone);
                        pps.setMasterPatrol(masterPatrol);
                    }
                    if (wizardDefaults) {
                        pps.setWizardDefaults(true);
                        pps.setWizardSkinName(wizardSkinName);
                        pps.setWizardPatrol(wizardPatrol);
                        pps.setWizardRedstone(wizardRedstone);
                    }
                } else {
                    pps = new PerPlayerSettings(playerUUID);
                    if (masterDefaults) {
                        pps.setMasterDefaults(true);
                        pps.setMasterSkinName(masterSkinName);
                        pps.setAmmoTypeName(ammoType);
                        pps.setMasterRedstone(masterRedstone);
                        pps.setMasterPatrol(masterPatrol);
                    }
                    if (wizardDefaults) {
                        pps.setWizardDefaults(true);
                        pps.setWizardSkinName(wizardSkinName);
                        pps.setWizardPatrol(wizardPatrol);
                        pps.setWizardRedstone(wizardRedstone);
                    }
                }
                if (pps != null) {
                    pps.setLastKnownPlayerName(lastKnownName);
                    this.plugin.perPlayerSettings.put(playerUUID, pps);
                    ++count;
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
        } else if (version == 1) {
            SkullTurret.PPS_UPDATE = true;
            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                @Override
                public void run() {
                    final ConfigurationSection players = DataStore.this.getPerPlayerConfig().getConfigurationSection("Players");
                    int count = 0;
                    int failCount = 0;
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 1 datafile detected, updating...");
                    for (final String playerName : players.getKeys(false)) {
                        final ConfigurationSection pSec = players.getConfigurationSection(playerName);
                        final int maxTurret = pSec.getInt("MaxTurrets");
                        final int maxRange = pSec.getInt("MaxRange");
                        try {
                            UUID playerUUID = DataStore.this.playerNameUUID.get(playerName);
                            if (playerUUID == null) {
                                playerUUID = FindUUID.getUUIDFromPlayerName(playerName);
                                DataStore.this.playerNameUUID.put(playerName, playerUUID);
                            }
                            if (DataStore.this.getMaxRange(false, false, playerUUID.toString(), maxRange) != maxRange) {
                                continue;
                            }
                            final PerPlayerSettings pps = new PerPlayerSettings(playerUUID, maxTurret, maxRange);
                            pps.setLastKnownPlayerName(playerName);
                            DataStore.this.plugin.perPlayerSettings.put(playerUUID, pps);
                            ++count;
                        } catch (Exception e) {
                            ++failCount;
                        }
                    }
                    if (failCount > 0) {
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": failed to Load " + failCount + " players from datafile.");
                    }
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
                    DataStore.this.savePerPlayerSettings();
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 3 datafile saved...");
                    SkullTurret.PPS_UPDATE = false;
                }
            });
        } else if (version == 2) {
            SkullTurret.PPS_UPDATE = true;
            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                @Override
                public void run() {
                    final ConfigurationSection players = DataStore.this.getPerPlayerConfig().getConfigurationSection("Players");
                    int count = 0;
                    int failCount = 0;
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 2 datafile detected, updating...");
                    for (final String playerName : players.getKeys(false)) {
                        final ConfigurationSection pSec = players.getConfigurationSection(playerName);
                        final int maxTurret = pSec.getInt("MaxTurrets", SkullTurret.MAX_SKULL_PER_PLAYER);
                        int maxRange = pSec.getInt("MaxRange", SkullTurret.MAX_RANGE);
                        final boolean ppsSet = pSec.getBoolean("PPS", false);
                        final boolean masterDefaults = pSec.getBoolean("MasterDefaults", false);
                        final String masterSkinName = pSec.getString("MasterDefaultSkin", "");
                        final String ammoType = pSec.getString("DefaultAmmoType", "arrow");
                        final boolean masterRedstone = pSec.getBoolean("MasterDefaultRedstone", false);
                        final boolean masterPatrol = pSec.getBoolean("MasterDefaultPatrol", true);
                        final boolean wizardDefaults = pSec.getBoolean("WizardDefaults", false);
                        final String wizardSkinName = pSec.getString("WizardDefaultSkin", "");
                        final boolean wizardRedstone = pSec.getBoolean("WizardDefaultRedstone", false);
                        final boolean wizardPatrol = pSec.getBoolean("WizardDefaultPatrol", true);
                        PerPlayerSettings pps = null;
                        try {
                            UUID playerUUID = DataStore.this.playerNameUUID.get(playerName);
                            if (playerUUID == null) {
                                playerUUID = FindUUID.getUUIDFromPlayerName(playerName);
                                DataStore.this.playerNameUUID.put(playerName, playerUUID);
                            }
                            maxRange = DataStore.this.getMaxRange(false, false, playerUUID.toString(), maxRange);
                            if (ppsSet) {
                                pps = new PerPlayerSettings(playerUUID, maxTurret, maxRange);
                                if (masterDefaults) {
                                    pps.setMasterDefaults(true);
                                    pps.setMasterSkinName(masterSkinName);
                                    pps.setAmmoTypeName(ammoType);
                                    pps.setMasterRedstone(masterRedstone);
                                    pps.setMasterPatrol(masterPatrol);
                                }
                                if (wizardDefaults) {
                                    pps.setWizardDefaults(true);
                                    pps.setWizardSkinName(wizardSkinName);
                                    pps.setWizardPatrol(wizardPatrol);
                                    pps.setWizardRedstone(wizardRedstone);
                                }
                            } else {
                                pps = new PerPlayerSettings(playerUUID);
                                if (masterDefaults) {
                                    pps.setMasterDefaults(true);
                                    pps.setMasterSkinName(masterSkinName);
                                    pps.setAmmoTypeName(ammoType);
                                    pps.setMasterRedstone(masterRedstone);
                                    pps.setMasterPatrol(masterPatrol);
                                }
                                if (wizardDefaults) {
                                    pps.setWizardDefaults(true);
                                    pps.setWizardSkinName(wizardSkinName);
                                    pps.setWizardPatrol(wizardPatrol);
                                    pps.setWizardRedstone(wizardRedstone);
                                }
                            }
                            if (pps == null) {
                                continue;
                            }
                            pps.setLastKnownPlayerName(playerName);
                            DataStore.this.plugin.perPlayerSettings.put(playerUUID, pps);
                            ++count;
                        } catch (Exception e) {
                            ++failCount;
                        }
                    }
                    if (failCount > 0) {
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": failed to Load " + failCount + " players from datafile.");
                    }
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
                    DataStore.this.savePerPlayerSettings();
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 3 datafile saved...");
                    SkullTurret.PPS_UPDATE = false;
                }
            });
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown playerinfo version. Found " + version + " expected " + 3);
        }
    }

    public void savePerPlayerSettings() {
        this.getPerPlayerConfig().set("Version", 3);
        final ConfigurationSection playersSec = this.getPerPlayerConfig().createSection("Players");
        for (final PerPlayerSettings pps : this.plugin.perPlayerSettings.values()) {
            final ConfigurationSection pSec = playersSec.createSection(pps.getPlayerUUID().toString());
            pSec.set("LastKnownName", pps.getLastKnownPlayerName());
            if (pps.isPps()) {
                pSec.set("PPS", true);
                pSec.set("MaxTurrets", pps.getMaxTurrets());
                pSec.set("MaxRange", pps.getMaxRange());
            }
            if (pps.isMasterDefaults()) {
                pSec.set("MasterDefaults", true);
                pSec.set("MasterDefaultSkin", pps.getMasterSkinName());
                pSec.set("DefaultAmmoType", pps.getAmmoTypeName());
                pSec.set("MasterDefaultRedstone", pps.isMasterRedstone());
                pSec.set("MasterDefaultPatrol", pps.isMasterPatrol());
            }
            if (pps.isWizardDefaults()) {
                pSec.set("WizardDefaults", true);
                pSec.set("WizardDefaultSkin", pps.getWizardSkinName());
                pSec.set("WizardDefaultRedstone", pps.isWizardRedstone());
                pSec.set("WizardDefaultPatrol", pps.isWizardPatrol());
            }
        }
        this.saveConfig(this.perPlayerConfig, this.perPlayerConfigFile);
    }

    public void loadPerGroupSettings() {
        final int version = this.getPerGroupConfig().getInt("Version", 0);
        if (version == 1) {
            final ConfigurationSection groups = this.getPerGroupConfig().getConfigurationSection("Groups");
            int count = 0;
            for (final String groupName : groups.getKeys(false)) {
                final ConfigurationSection pSec = groups.getConfigurationSection(groupName);
                final int maxTurret = pSec.getInt("MaxTurrets");
                final int maxRange = pSec.getInt("MaxRange");
                if (this.getMaxRange(true, false, groupName, maxRange) == maxRange) {
                    final PerPlayerGroups ppg = new PerPlayerGroups(groupName, maxTurret, maxRange);
                    this.plugin.perPlayerGroups.put(groupName, ppg);
                    ++count;
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " groups from datafile.");
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown groupinfo version. Found " + version + " expected " + 1);
        }
    }

    private void loadAmmoList() {
        if (SkullTurret.ALLOW_SNOWBALLS) {
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.SNOWBALL, 1));
        }
        if (SkullTurret.ALLOW_FIRE_CHARGE) {
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.FIRE_CHARGE, 1));
        }
        if (SkullTurret.ALLOW_ARROWS) {
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.ARROW, 1));
        }
    }

    private String loadOldDatabaseBin() {
        try {
            final ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(this.databaseFileOld));
            final String input = (String) inputStream.readObject();
            inputStream.close();
            this.databaseFileOld.renameTo(new File(this.directory, "preUUID.bin"));
            return input;
        } catch (FileNotFoundException e2) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Find " + this.databaseFileOld.getAbsolutePath());
        } catch (IOException e3) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Access " + this.databaseFileOld.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String loadDatabaseBin() {
        try {
            final FileInputStream fileInputStream = new FileInputStream(this.databaseFileNew);
            final GZIPInputStream zipInputStream = new GZIPInputStream(fileInputStream);
            final ObjectInputStream inputStream = new ObjectInputStream(zipInputStream);
            final String input = (String) inputStream.readObject();
            inputStream.close();
            fileInputStream.close();
            return input;
        } catch (FileNotFoundException e2) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Find " + this.databaseFileNew.getAbsolutePath());
        } catch (IOException e3) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Access " + this.databaseFileNew.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public int saveDatabase(final boolean exitSave) {
        final StringBuffer databin = new StringBuffer();
        if (this.databaseFileNew.exists()) {
            int count = 0;
            for (final PlacedSkull pc : this.plugin.skullMap.values()) {
                if (!pc.failed) {
                    if (pc instanceof MobileSkull) {
                        final MobileSkull ms = (MobileSkull) pc;
                        databin.append(ms.toString() + "\n");
                    } else {
                        databin.append(pc.toString() + "\n");
                    }
                    ++count;
                }
            }
            if (exitSave) {
                this.safeSaveDatabase(databin);
            } else {
                this.quickSaveDatabase(databin);
            }
            return count;
        }
        return -1;
    }

    public void safeSaveDatabase(final StringBuffer databin) {
        if (this.databaseFileNew.exists()) {
            ObjectOutputStream outputStream = null;
            GZIPOutputStream zipOutputStream = null;
            FileOutputStream fileOutputStream = null;
            FileInputStream fileInputStream = null;
            GZIPInputStream zipInputStream = null;
            ObjectInputStream inputStream = null;
            Label_0539:
            {
                try {
                    fileOutputStream = new FileOutputStream(this.tmpDatabaseFile);
                    zipOutputStream = new GZIPOutputStream(fileOutputStream);
                    outputStream = new ObjectOutputStream(zipOutputStream);
                    outputStream.writeObject(databin.toString());
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fileInputStream = new FileInputStream(this.tmpDatabaseFile);
                    zipInputStream = new GZIPInputStream(fileInputStream);
                    inputStream = new ObjectInputStream(zipInputStream);
                    final String tmpDatabin = (String) inputStream.readObject();
                    inputStream.close();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    final File oldFile = new File(this.directory, "skullturret.old");
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                    if (!tmpDatabin.equals(databin.toString())) {
                        SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": detected database corruption. Database was not saved correctly!\n ");
                        break Label_0539;
                    }
                    if (this.databaseFileNew.exists()) {
                        this.databaseFileNew.renameTo(oldFile);
                    }
                    this.tmpDatabaseFile.renameTo(this.databaseFileNew);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                } catch (IOException e) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access door database\n", e);
                } catch (ClassNotFoundException e2) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not read temp File. Database was not saved!\n ", e2);
                    e2.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.flush();
                            outputStream.close();
                        }
                    } catch (IOException ex) {
                        SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access " + this.databaseFileNew.getAbsolutePath(), ex);
                    }
                }
                try {
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (IOException ex) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access " + this.databaseFileNew.getAbsolutePath(), ex);
                }
            }
        }
    }

    private void quickSaveDatabase(final StringBuffer databin) {
        FileOutputStream fileOutputStream = null;
        GZIPOutputStream zipOutputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            fileOutputStream = new FileOutputStream(this.databaseFileNew);
            zipOutputStream = new GZIPOutputStream(fileOutputStream);
            outputStream = new ObjectOutputStream(zipOutputStream);
            outputStream.writeObject(databin.toString());
        } catch (IOException e) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access " + this.databaseFileNew.getAbsolutePath(), e);
        }
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (IOException ex) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access " + this.databaseFileNew.getAbsolutePath(), ex);
        }
    }

    public void reInit() {
        this.plugin.playersSkullNumber.clear();
        this.plugin.perPlayerSettings.clear();
        this.plugin.skullMap.clear();
        this.plugin.reloadConfig();
        CustomPlayer.playerSettings.clear();
        this.init();
    }

    private int getMaxRange(final boolean isGroup, final boolean maxRange, final String playerUUID, final int testRange) {
        if (testRange % 3 != 0) {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad Range Value for " + (maxRange ? "MaxRange" : playerUUID) + ".");
            SkullTurret.LOGGER.warning(testRange + " Not a Multiple of 3.");
            if (maxRange) {
                SkullTurret.LOGGER.warning("Default Used: " + SkullTurret.MAX_RANGE);
            } else {
                SkullTurret.LOGGER.warning((isGroup ? "Player " : "Group: ") + playerUUID + " max range ignored.");
            }
            return SkullTurret.MAX_RANGE;
        }
        return testRange;
    }

    public void reloadPerPlayerConfig() {
        if (this.perPlayerConfigFile == null) {
            this.perPlayerConfigFile = new File(this.plugin.getDataFolder(), "playerinfo.yml");
            if (!this.perPlayerConfigFile.exists()) {
                this.saveDefaultConfig(this.perPlayerConfigFile, "playerinfo.yml");
            }
        }
        this.perPlayerConfig = YamlConfiguration.loadConfiguration(this.perPlayerConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("playerinfo.yml");
        final InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.perPlayerConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getPerPlayerConfig() {
        if (this.perPlayerConfig == null) {
            this.reloadPerPlayerConfig();
        }
        return this.perPlayerConfig;
    }

    public FileConfiguration getRecipeConfig() {
        if (this.recipeConfig == null) {
            this.reloadRecipeConfig();
        }
        return this.recipeConfig;
    }

    public void reloadRecipeConfig() {
        if (this.recipeConfigFile == null) {
            this.recipeConfigFile = new File(this.plugin.getDataFolder(), "recipes.yml");
            if (!this.recipeConfigFile.exists()) {
                this.saveDefaultConfig(this.recipeConfigFile, "recipes.yml");
            }
        }
        this.recipeConfig = YamlConfiguration.loadConfiguration(this.recipeConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("recipes.yml");
        final InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.recipeConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getEntityConfig() {
        if (this.entityConfig == null) {
            this.reloadEntityConfig();
        }
        return this.entityConfig;
    }

    public void reloadEntityConfig() {
        if (this.entityConfigFile == null) {
            this.entityConfigFile = new File(this.plugin.getDataFolder(), "entities.yml");
            if (!this.entityConfigFile.exists()) {
                this.saveDefaultConfig(this.entityConfigFile, "entities.yml");
            }
        }
        this.entityConfig = YamlConfiguration.loadConfiguration(this.entityConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("entities.yml");
        final InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.entityConfig.setDefaults(defConfig);
        }
    }

    public void reloadPerGroupConfig() {
        if (this.perGroupConfigFile == null) {
            this.perGroupConfigFile = new File(this.plugin.getDataFolder(), "groupinfo.yml");
            if (!this.perGroupConfigFile.exists()) {
                this.saveDefaultConfig(this.perGroupConfigFile, "groupinfo.yml");
            }
        }
        this.perGroupConfig = YamlConfiguration.loadConfiguration(this.perGroupConfigFile);
        final InputStream defConfigStream = this.plugin.getResource("groupinfo.yml");
        final InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            final YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.perGroupConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getPerGroupConfig() {
        if (this.perGroupConfig == null) {
            this.reloadPerGroupConfig();
        }
        return this.perGroupConfig;
    }

    public File saveDefaultConfig(File toTest, final String fileName) {
        if (toTest == null) {
            toTest = new File(this.plugin.getDataFolder(), fileName);
        }
        if (!toTest.exists()) {
            this.plugin.saveResource(fileName, false);
        }
        return toTest;
    }

    public void saveConfig(final FileConfiguration fc, final File f) {
        if (fc == null || f == null) {
            return;
        }
        try {
            fc.save(f);
        } catch (IOException ex) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Could not save config to " + f, ex);
        }
    }

    private Material getMaterial(final String matString) {
        final Material material = Material.matchMaterial(matString);
        return (material == null) ? Material.LAPIS_BLOCK : material;
    }
}
