package plugin.arcwolf.skullturrets;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.Dye;
import org.bukkit.plugin.Plugin;

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

    public SkullTurret plugin;

    private final String databaseOld = "skulls.bin";

    private final String databaseNew = "skullsdat.bin";

    private final String tmpDatabase = "skullsdat.tmp";

    private final String config = "config.yml";

    private final File databaseFileOld;

    private final File databaseFileNew;

    private final File tmpDatabaseFile;

    private final File directory;

    private FileConfiguration perPlayerConfig = null;

    private FileConfiguration perGroupConfig = null;

    private FileConfiguration recipeConfig = null;

    private FileConfiguration entityConfig = null;

    private File perPlayerConfigFile = null;

    private File perGroupConfigFile = null;

    private File recipeConfigFile = null;

    private File entityConfigFile = null;

    private File configFile = null;

    private final ConcurrentHashMap<String, UUID> playerNameUUID = new ConcurrentHashMap<String, UUID>(256);

    public DataStore(SkullTurret plugin) {
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
            boolean chk = this.directory.mkdir();
            if (chk) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created folder.");
            } else {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create folder!");
            }
        }
        if (this.databaseFileOld.exists()) {
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": is updating old skullturret database to UUID Standard.");
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": This will take at least 1 minute.");
            updateToUUID();
            return false;
        }
        if (!this.databaseFileNew.exists()) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.databaseFileNew));
                outputStream.writeObject("");
                outputStream.close();
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created new database file.");
            } catch (IOException e) {
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create database file!");
                return false;
            }
            newSave = true;
        }
        loadConfig();
        loadRecipies();
        loadEntities();
        loadPerGroupSettings();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Config Loaded.");
        if (SkullTurret.PER_PLAYER_SETTINGS)
            loadPerPlayerSettings();
        return loadDataFiles(newSave);
    }

    private void updateToUUID() {
        SkullTurret.DB_UPDATE = true;
        String binDatabase = loadOldDatabaseBin();
        Scanner scanner = new Scanner(binDatabase);
        int count = 0;
        final List<OldPlacedSkull> opslist = new ArrayList<OldPlacedSkull>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            OldPlacedSkull ops = new OldPlacedSkull(line);
            if (ops.failed)
                continue;
            opslist.add(ops);
            count++;
        }
        scanner.close();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": a total of " + count + " skulls in datafile to update.");
        this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
            public void run() {
                int failCount = 0;
                int successCount = 0;
                List<PlacedSkull> skullList = new ArrayList<PlacedSkull>();
                for (OldPlacedSkull skull : opslist) {
                    try {
                        UUID creatorID = DataStore.this.playerNameUUID.get(skull.getSkullCreator());
                        if (creatorID == null) {
                            creatorID = FindUUID.getUUIDFromPlayerName(skull.getSkullCreator());
                            DataStore.this.playerNameUUID.put(skull.getSkullCreator(), creatorID);
                        }
                        PlacedSkull ps = new PlacedSkull(skull, creatorID);
                        Map<UUID, PlayerNamesFoF> pnfof = new HashMap<UUID, PlayerNamesFoF>();
                        for (Map.Entry<String, String> pfoe : skull.playerFrenemies.entrySet()) {
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
                        failCount++;
                    }
                }
                StringBuffer databin = new StringBuffer();
                if (!DataStore.this.databaseFileNew.exists())
                    try {
                        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(DataStore.this.databaseFileNew));
                        outputStream.writeObject("");
                        outputStream.close();
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Successfully created new database file.");
                    } catch (IOException e) {
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Unable to create database file!");
                        return;
                    }
                if (DataStore.this.databaseFileNew.exists()) {
                    int count = 0;
                    for (PlacedSkull pc : skullList) {
                        if (!pc.failed) {
                            databin.append(pc.toString() + "\n");
                            count++;
                        }
                    }
                    DataStore.this.quickSaveDatabase(databin);
                    successCount = count;
                }
                SkullTurret.DB_UPDATE = false;
                if (failCount > 0)
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Failed to update " + failCount + " skulls in datafile.");
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Updated " + successCount + " skulls in datafile.");
            }
        });
    }

    private boolean loadDataFiles(boolean newSave) {
        if (newSave)
            saveDatabase(false);
        String binDatabase = loadDatabaseBin();
        Scanner scanner = new Scanner(binDatabase);
        int count = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Character designation = Character.valueOf('\036');
            PlacedSkull pc = null;
            if (line.contains(designation.toString())) {
                pc = new MobileSkull(line);
            } else {
                pc = new PlacedSkull(line);
            }
            if (pc.failed)
                continue;
            Location loc = pc.getSkullBlock().getLocation();
            this.plugin.skullMap.put(loc, pc);
            UUID playerUUID = pc.getSkullCreator();
            Player player = this.plugin.getServer().getPlayer(playerUUID);
            OfflinePlayer oPlayer = null;
            String playerName = "";
            if (player == null)
                oPlayer = this.plugin.getServer().getOfflinePlayer(playerUUID);
            if (player != null) {
                playerName = player.getName();
            } else if (player == null && oPlayer != null) {
                playerName = oPlayer.getName();
            }
            pc.setSkullCreatorLastKnowName(playerName);
            if (this.plugin.playersSkullNumber.containsKey(playerUUID)) {
                SkullCounts sc = this.plugin.playersSkullNumber.get(playerUUID);
                int numSkulls = sc.getActiveSkulls();
                numSkulls++;
                sc.setActiveSkulls(numSkulls);
            } else {
                this.plugin.playersSkullNumber.put(playerUUID, new SkullCounts(1, 0));
            }
            count++;
        }
        scanner.close();
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " skulls from datafile.");
        return true;
    }

    private void loadConfig() {
        if (!this.configFile.exists())
            this.configFile = saveDefaultConfig(this.configFile, "config.yml");
        SkullTurret.DEBUG = this.plugin.getConfig().getInt("debug", 0);
        SkullTurret.ONLINE_UUID_CHECK = this.plugin.getConfig().getBoolean("online_uuid_check", true);
        SkullTurret.LANGUAGE = this.plugin.getConfig().getString("Language_iso_code", "enUS");
        SkullTurret.PROFILE_URL = this.plugin.getConfig().getString("profile_url", "https://api.mojang.com/profiles/minecraft");
        SkullTurret.MAX_RANGE = getMaxRange(false, true, "", this.plugin.getConfig().getInt("default_max_range", 15));
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
        SkullTurret.REDSTONE_BLOCK_MAT = getMaterial(this.plugin.getConfig().getString("redstone_block_mat", Material.LAPIS_BLOCK.name()));
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
        SkullTurret.ALLOW_FACTIONS = this.plugin.getConfig().getBoolean("factions_support", false);
        SkullTurret.FACT_TARGET_NEUTRAL = this.plugin.getConfig().getBoolean("target_neutral", false);
        SkullTurret.FACT_TARGET_TRUCE = this.plugin.getConfig().getBoolean("target_truce", false);
        SkullTurret.FACT_TARGET_ENEMY = this.plugin.getConfig().getBoolean("target_enemy", true);
        SkullTurret.FACT_TARGET_ALLY = this.plugin.getConfig().getBoolean("target_ally", false);
        SkullTurret.FACT_TARGET_PEACEFUL = this.plugin.getConfig().getBoolean("target_peaceful", false);
        SkullTurret.FACT_TARGET_UNAFFILIATED = this.plugin.getConfig().getBoolean("target_unaffiliated", true);
        SkullTurret.FACT_ALLOW_PLACE_ENEMY = this.plugin.getConfig().getBoolean("allow_placement_enemy_territory", false);
        SkullTurret.FACT_ALLOW_PLACE_NEUTRAL = this.plugin.getConfig().getBoolean("allow_placement_neutral_territory", false);
        SkullTurret.FACT_ALLOW_PLACE_TRUCE = this.plugin.getConfig().getBoolean("allow_placement_truce_territory", false);
        SkullTurret.FACT_ALLOW_PLACE_OWN = this.plugin.getConfig().getBoolean("allow_placement_own_territory", true);
        SkullTurret.FACT_ALLOW_PLACE_ALLY = this.plugin.getConfig().getBoolean("allow_placement_ally_territory", true);
        SkullTurret.FACT_ALLOW_PLACE_PEACEFUL = this.plugin.getConfig().getBoolean("allow_placement_peaceful_territory", true);
        SkullTurret.FACT_ALLOW_PLACE_WILDERNESS = this.plugin.getConfig().getBoolean("allow_placement_wilderness", true);
        SkullTurret.FACT_USE_FACTION_POWER = this.plugin.getConfig().getBoolean("use_faction_power_to_limit_player_skulls", false);
        SkullTurret.FACT_POWER_PER_TURRET = this.plugin.getConfig().getDouble("faction_power_per_skull", 1.0D);
        SkullTurret.FACT_ALLOW_SKULL_DESTRUCT = this.plugin.getConfig().getBoolean("allow_faction_skulls_destruct", true);
        SkullTurret.ALLOW_TOWNY = this.plugin.getConfig().getBoolean("towny_support", false);
        SkullTurret.TOWN_NOMAD_PLACE = this.plugin.getConfig().getBoolean("nomads_can_place_skulls", true);
        SkullTurret.TOWN_NATIONLESS_TOWN_PLACE = this.plugin.getConfig().getBoolean("nationless_towns_can_place_skulls", true);
        SkullTurret.TOWN_SKULLS_RESPECT_PVP = this.plugin.getConfig().getBoolean("skulls_respect_pvp_rule", false);
        SkullTurret.TOWN_SKULLS_IGNORE_EMBASSY_OWNER = this.plugin.getConfig().getBoolean("skulls_ignore_embassy_owners", true);
        SkullTurret.TOWN_TOWN_SKULLS_IGNORE_NOMADS = this.plugin.getConfig().getBoolean("town_skulls_ignore_nomads", false);
        SkullTurret.TOWN_WILDERNESS_FREEFORALL = this.plugin.getConfig().getBoolean("skulls_in_wilderness_target_everyone", true);
        SkullTurret.TOWN_ALLOW_SKULL_DESTRUCT = this.plugin.getConfig().getBoolean("allow_towny_skulls_destruct", true);
        SkullTurret.ALLOW_DISGUISE = this.plugin.getConfig().getBoolean("disguisecraft_support", false);
        SkullTurret.ALLOW_VANISH = this.plugin.getConfig().getBoolean("vanishnopacket_support", false);
        SkullTurret.USE_VAULT_ECON = this.plugin.getConfig().getBoolean("use_vault_economy", false);
        SkullTurret.ECON_BOW_COST = this.plugin.getConfig().getDouble("econ_bow_price", 2.0D);
        SkullTurret.ECON_CRAZED_COST = this.plugin.getConfig().getDouble("econ_crazed_skull_price", 1.0D);
        SkullTurret.ECON_DEVIOUS_COST = this.plugin.getConfig().getDouble("econ_devious_skull_price", 2.0D);
        SkullTurret.ECON_MASTER_COST = this.plugin.getConfig().getDouble("econ_master_skull_price", 3.0D);
        SkullTurret.ECON_WIZARD_COST = this.plugin.getConfig().getDouble("econ_wizard_skull_price", 4.0D);
        SkullTurret.PATROL_TIME = this.plugin.getConfig().getInt("patrol_timer", 1800);
        SkullIntelligence.CRAZED.setDamageMod(this.plugin.getConfig().getDouble("crazed_damage_mod", 6.0D));
        SkullIntelligence.DEVIOUS.setDamageMod(this.plugin.getConfig().getDouble("devious_damage_mod", 8.5D));
        SkullIntelligence.MASTER.setDamageMod(this.plugin.getConfig().getDouble("master_damage_mod", 9.5D));
        SkullIntelligence.WIZARD.setDamageMod(this.plugin.getConfig().getDouble("wizard_damage_mod", 9.0D));
        SkullIntelligence.CRAZED.setSpread(this.plugin.getConfig().getInt("crazed_accuracy_mod", 15));
        SkullIntelligence.DEVIOUS.setSpread(this.plugin.getConfig().getInt("devious_accuracy_mod", 10));
        SkullIntelligence.MASTER.setSpread(this.plugin.getConfig().getInt("master_accuracy_mod", 0));
        SkullIntelligence.WIZARD.setSpread(this.plugin.getConfig().getInt("wizard_accuracy_mod", 5));
        SkullIntelligence.CRAZED.setCooldown(this.plugin.getConfig().getInt("crazed_fire_speed", 1000));
        SkullIntelligence.DEVIOUS.setCooldown(this.plugin.getConfig().getInt("devious_fire_speed", 1300));
        SkullIntelligence.MASTER.setCooldown(this.plugin.getConfig().getInt("master_fire_speed", 1000));
        SkullIntelligence.WIZARD.setCooldown(this.plugin.getConfig().getInt("wizard_fire_speed", 2000));
        SkullIntelligence.CRAZED.setFireRangeMultiplier(this.plugin.getConfig().getDouble("crazed_firerange_mod", 0.5D));
        SkullIntelligence.DEVIOUS.setFireRangeMultiplier(this.plugin.getConfig().getDouble("devious_firerange_mod", 0.75D));
        SkullIntelligence.MASTER.setFireRangeMultiplier(this.plugin.getConfig().getDouble("master_firerange_mod", 0.9D));
        SkullIntelligence.WIZARD.setFireRangeMultiplier(this.plugin.getConfig().getDouble("wizard_firerange_mod", 0.85D));
        SkullTurret.ALLOW_SKULL_DAMAGE = this.plugin.getConfig().getBoolean("allow_skull_damage", false);
        SkullTurret.ALLOW_DAMAGED_SKULL_DESTRUCT = this.plugin.getConfig().getBoolean("allow_damaged_skull_destruct", false);
        SkullTurret.SKULLS_RETALIATE = this.plugin.getConfig().getBoolean("allow_skull_retaliate", false);
        SkullTurret.DESTRUCT_TIMER = this.plugin.getConfig().getInt("skull_destruct_timer", 90000);
        SkullIntelligence.CRAZED.setRepair_item(getMaterial(this.plugin.getConfig().getString("crazed_repair_item", Material.STICK.name())));
        SkullIntelligence.DEVIOUS.setRepair_item(getMaterial(this.plugin.getConfig().getString("devious_repair_item", Material.BONE.name())));
        SkullIntelligence.MASTER.setRepair_item(getMaterial(this.plugin.getConfig().getString("master_repair_item", Material.DIAMOND.name())));
        SkullIntelligence.WIZARD.setRepair_item(getMaterial(this.plugin.getConfig().getString("wizard_repair_item", Material.GOLD_INGOT.name())));
        SkullTurret.SKULL_REPAIR_AMOUNT = this.plugin.getConfig().getDouble("skull_repair_amount", 5.0D);
        SkullTurret.SKULL_DAMAGE_RECOVERY_TIME = this.plugin.getConfig().getInt("skull_damage_recovery_time", 15000);
        SkullIntelligence.CRAZED.setHealth(this.plugin.getConfig().getDouble("crazed_health", 30.0D));
        SkullIntelligence.DEVIOUS.setHealth(this.plugin.getConfig().getDouble("devious_health", 40.0D));
        SkullIntelligence.MASTER.setHealth(this.plugin.getConfig().getDouble("master_health", 60.0D));
        SkullIntelligence.WIZARD.setHealth(this.plugin.getConfig().getDouble("wizard_health", 50.0D));
        SkullTurret.ALLOW_ARROW_DAMAGE = this.plugin.getConfig().getBoolean("allow_arrow_damage", true);
        loadWeapons();
        loadAmmoList();
        loadNoDropWorlds();
        loadDefaultLang();
        if (!SkullTurret.RELOAD) {
            rewriteConfig();
            SkullTurret.RELOAD = false;
        }
    }

    private void loadDefaultLang() {
        File localeFile = new File(this.plugin.getDataFolder(), "locale_" + SkullTurret.LANGUAGE + ".yml");
        if (!localeFile.exists()) {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Could not find locale_" + SkullTurret.LANGUAGE + ".yml Defaulting to enUS.");
            SkullTurret.LANGUAGE = "enUS";
            saveDefaultConfig(localeFile, "locale_" + SkullTurret.LANGUAGE + ".yml");
        } else if (!Utils.getLocalization("Version").equals(Integer.toString(1))) {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Wrong localization file version found. Expected " + '\001');
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": default locale_enUS.yml updated in plugin directory.");
            localeFile.delete();
            saveDefaultConfig(localeFile, "locale_enUS.yml");
        }
        String version = Utils.getLocalization("Version");
        String author = Utils.getLocalization("Author");
        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded Language File: " + SkullTurret.LANGUAGE + " Version: " + version + " Author: " + author);
    }

    public void loadNoDropWorlds() {
        List<String> worldsSec = this.plugin.getConfig().getList("NO_SKULL_DROP_WORLD_LIST");
        if (worldsSec != null)
            SkullTurret.NO_DROP_WORLDS.addAll(worldsSec);
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
            for (String worlds : SkullTurret.NO_DROP_WORLDS)
                writer.write("  - " + worlds + "\r\n");
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
            writer.write("# Setting to enable factions 2.7 support\r\n");
            writer.write("# Factions below 2.7 are not supported\r\n");
            writer.write("factions_support: " + SkullTurret.ALLOW_FACTIONS + "\r\n");
            writer.write("\r\n");
            writer.write("# ***FACTIONS SPECIFIC OPTIONS***\r\n");
            writer.write("# Only enabled if Faction is enabled\r\n");
            writer.write("# \r\n");
            writer.write("target_neutral: " + SkullTurret.FACT_TARGET_NEUTRAL + "\r\n");
            writer.write("target_truce: " + SkullTurret.FACT_TARGET_TRUCE + "\r\n");
            writer.write("target_enemy: " + SkullTurret.FACT_TARGET_ENEMY + "\r\n");
            writer.write("target_ally: " + SkullTurret.FACT_TARGET_ALLY + "\r\n");
            writer.write("target_peaceful: " + SkullTurret.FACT_TARGET_PEACEFUL + "\r\n");
            writer.write("target_unaffiliated: " + SkullTurret.FACT_TARGET_UNAFFILIATED + "\r\n");
            writer.write("allow_placement_enemy_territory: " + SkullTurret.FACT_ALLOW_PLACE_ENEMY + "\r\n");
            writer.write("allow_placement_neutral_territory: " + SkullTurret.FACT_ALLOW_PLACE_NEUTRAL + "\r\n");
            writer.write("allow_placement_truce_territory: " + SkullTurret.FACT_ALLOW_PLACE_TRUCE + "\r\n");
            writer.write("allow_placement_own_territory: " + SkullTurret.FACT_ALLOW_PLACE_OWN + "\r\n");
            writer.write("allow_placement_ally_territory: " + SkullTurret.FACT_ALLOW_PLACE_ALLY + "\r\n");
            writer.write("allow_placement_peaceful_territory: " + SkullTurret.FACT_ALLOW_PLACE_PEACEFUL + "\r\n");
            writer.write("allow_placement_wilderness: " + SkullTurret.FACT_ALLOW_PLACE_WILDERNESS + "\r\n");
            writer.write("use_faction_power_to_limit_player_skulls: " + SkullTurret.FACT_USE_FACTION_POWER + "\r\n");
            writer.write("faction_power_per_skull: " + SkullTurret.FACT_POWER_PER_TURRET + "\r\n");
            writer.write("allow_faction_skulls_destruct: " + SkullTurret.FACT_ALLOW_SKULL_DESTRUCT + "\r\n");
            writer.write("# ***END FACTIONS CONFIG****\r\n");
            writer.write("\r\n");
            writer.write("# Setting to enable Towny 0.84+ support\r\n");
            writer.write("towny_support: " + SkullTurret.ALLOW_TOWNY + "\r\n");
            writer.write("\r\n");
            writer.write("# ***TOWNY SPECIFIC OPTIONS***\r\n");
            writer.write("# Only enabled if Towny is enabled\r\n");
            writer.write("#\r\n");
            writer.write("nomads_can_place_skulls: " + SkullTurret.TOWN_NOMAD_PLACE + "\r\n");
            writer.write("nationless_towns_can_place_skulls: " + SkullTurret.TOWN_NATIONLESS_TOWN_PLACE + "\r\n");
            writer.write("skulls_respect_pvp_rule: " + SkullTurret.TOWN_SKULLS_RESPECT_PVP + "\r\n");
            writer.write("skulls_ignore_embassy_owners: " + SkullTurret.TOWN_SKULLS_IGNORE_EMBASSY_OWNER + "\r\n");
            writer.write("town_skulls_ignore_nomads: " + SkullTurret.TOWN_TOWN_SKULLS_IGNORE_NOMADS + "\r\n");
            writer.write("skulls_in_wilderness_target_everyone: " + SkullTurret.TOWN_WILDERNESS_FREEFORALL + "\r\n");
            writer.write("allow_towny_skulls_destruct: " + SkullTurret.TOWN_ALLOW_SKULL_DESTRUCT + "\r\n");
            writer.write("# ***END TOWNY CONFIG****\r\n");
            writer.write("\r\n");
            writer.write("# Setting to enable DisguiseCraft 4.9+ support\r\n");
            writer.write("disguisecraft_support: " + SkullTurret.ALLOW_DISGUISE + "\r\n");
            writer.write("\r\n");
            writer.write("# Setting to enable Vanish No Packet 3.18+ support\r\n");
            writer.write("vanishnopacket_support: " + SkullTurret.ALLOW_VANISH + "\r\n");
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
            for (Map.Entry<Material, Double> weap : this.plugin.weapons.entrySet()) {
                Material mat = weap.getKey();
                double damage = weap.getValue().doubleValue();
                writer.write("  " + mat.name() + ":\r\n");
                writer.write("    Damage: " + damage + "\r\n");
            }
        } catch (Exception e) {
            SkullTurret.LOGGER.log(Level.SEVERE, "Exception while creating " + this.configFile, e);
        } finally {
            if (writer != null)
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e);
                }
            if (fwriter != null)
                try {
                    fwriter.close();
                } catch (IOException e) {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": error " + e);
                }
        }
    }

    public void loadRecipies() {
        int version = getRecipeConfig().getInt("Version", 0);
        char[] letters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'};
        String[] matrixString = {"abc", "def", "ghi"};
        if (version == 1) {
            ConfigurationSection crazed = getRecipeConfig().getConfigurationSection("Crazed");
            String[] matrix = new String[9];
            Material[] matrixDefault = {Material.AIR, Material.REDSTONE, Material.AIR, Material.SPIDER_EYE, Material.SKULL_ITEM, Material.SPIDER_EYE, Material.AIR, Material.STICK, Material.AIR};
            matrix[0] = crazed.getString("TopLeft", "AIR");
            matrix[1] = crazed.getString("TopMiddle", "REDSTONE");
            matrix[2] = crazed.getString("TopRight", "AIR");
            matrix[3] = crazed.getString("CenterLeft", "SPIDER_EYE");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = crazed.getString("CenterRight", "SPIDER_EYE");
            matrix[6] = crazed.getString("BottomLeft", "AIR");
            matrix[7] = crazed.getString("BottomMiddle", "STICK");
            matrix[8] = crazed.getString("BottomRight", "AIR");
            ShapedRecipe crazedSkull = new ShapedRecipe(this.plugin.recipes.crazedSkullItem);
            crazedSkull.shape(matrixString);
            for (int i = 0; i < 9; i++) {
                Material mat = Material.getMaterial(matrix[i]);
                if (mat != Material.AIR)
                    if (mat != null) {
                        crazedSkull.setIngredient(letters[i], mat);
                    } else {
                        Dye dye = new Dye();
                        try {
                            DyeColor dc = DyeColor.valueOf(matrix[i]);
                            dye.setColor(dc);
                            crazedSkull.setIngredient(letters[i], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[i] + " Using default: " + matrixDefault[i]);
                            crazedSkull.setIngredient(letters[i], matrixDefault[i]);
                        }
                    }
            }
            ConfigurationSection devious = getRecipeConfig().getConfigurationSection("Devious");
            matrixDefault = new Material[]{Material.AIR, Material.GLOWSTONE_DUST, Material.AIR, Material.ENDER_PEARL, Material.SKULL_ITEM, Material.ENDER_PEARL, Material.AIR, Material.BONE, Material.AIR};
            matrix[0] = devious.getString("TopLeft", "AIR");
            matrix[1] = devious.getString("TopMiddle", "GLOWSTONE_DUST");
            matrix[2] = devious.getString("TopRight", "AIR");
            matrix[3] = devious.getString("CenterLeft", "ENDER_PEARL");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = devious.getString("CenterRight", "ENDER_PEARL");
            matrix[6] = devious.getString("BottomLeft", "AIR");
            matrix[7] = devious.getString("BottomMiddle", "BONE");
            matrix[8] = devious.getString("BottomRight", "AIR");
            ShapedRecipe deviousSkull = new ShapedRecipe(this.plugin.recipes.deviousSkullItem);
            deviousSkull.shape(matrixString);
            for (int j = 0; j < 9; j++) {
                Material mat = Material.getMaterial(matrix[j]);
                if (mat != Material.AIR)
                    if (mat != null) {
                        deviousSkull.setIngredient(letters[j], mat);
                    } else {
                        Dye dye = new Dye();
                        try {
                            DyeColor dc = DyeColor.valueOf(matrix[j]);
                            dye.setColor(dc);
                            deviousSkull.setIngredient(letters[j], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[j] + " Using default: " + matrixDefault[j]);
                            deviousSkull.setIngredient(letters[j], matrixDefault[j]);
                        }
                    }
            }
            ConfigurationSection master = getRecipeConfig().getConfigurationSection("Master");
            matrixDefault = new Material[]{Material.GHAST_TEAR, Material.BOOK_AND_QUILL, Material.GHAST_TEAR, Material.EYE_OF_ENDER, Material.SKULL_ITEM, Material.EYE_OF_ENDER, Material.AIR, Material.BLAZE_ROD, Material.AIR};
            matrix[0] = master.getString("TopLeft", "GHAST_TEAR");
            matrix[1] = master.getString("TopMiddle", "BOOK_AND_QUILL");
            matrix[2] = master.getString("TopRight", "GHAST_TEAR");
            matrix[3] = master.getString("CenterLeft", "EYE_OF_ENDER");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = master.getString("CenterRight", "EYE_OF_ENDER");
            matrix[6] = master.getString("BottomLeft", "AIR");
            matrix[7] = master.getString("BottomMiddle", "BLAZE_ROD");
            matrix[8] = master.getString("BottomRight", "AIR");
            ShapedRecipe masterSkull = new ShapedRecipe(this.plugin.recipes.masterSkullItem);
            masterSkull.shape(matrixString);
            for (int k = 0; k < 9; k++) {
                Material mat = Material.getMaterial(matrix[k]);
                if (mat != Material.AIR)
                    if (mat != null) {
                        masterSkull.setIngredient(letters[k], mat);
                    } else {
                        Dye dye = new Dye();
                        try {
                            DyeColor dc = DyeColor.valueOf(matrix[k]);
                            dye.setColor(dc);
                            masterSkull.setIngredient(letters[k], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[k] + " Using default: " + matrixDefault[k]);
                            masterSkull.setIngredient(letters[k], matrixDefault[k]);
                        }
                    }
            }
            ConfigurationSection wizard = getRecipeConfig().getConfigurationSection("Wizard");
            matrixDefault = new Material[]{null, Material.AIR, Material.NETHER_STALK, Material.AIR, Material.SKULL_ITEM, Material.AIR, Material.GLOWSTONE_DUST, Material.MAGMA_CREAM, Material.GLOWSTONE_DUST};
            matrix[0] = wizard.getString("TopLeft", "GRAY");
            matrix[1] = wizard.getString("TopMiddle", "AIR");
            matrix[2] = wizard.getString("TopRight", "NETHER_STALK");
            matrix[3] = wizard.getString("CenterLeft", "AIR");
            matrix[4] = "SKULL_ITEM";
            matrix[5] = wizard.getString("CenterRight", "AIR");
            matrix[6] = wizard.getString("BottomLeft", "GLOWSTONE_DUST");
            matrix[7] = wizard.getString("BottomMiddle", "MAGMA_CREAM");
            matrix[8] = wizard.getString("BottomRight", "GLOWSTONE_DUST");
            ShapedRecipe wizardSkull = new ShapedRecipe(this.plugin.recipes.wizardSkullItem);
            wizardSkull.shape(matrixString);
            for (int m = 0; m < 9; m++) {
                Material mat = Material.getMaterial(matrix[m]);
                if (mat != Material.AIR)
                    if (mat != null) {
                        wizardSkull.setIngredient(letters[m], mat);
                    } else {
                        Dye dye = new Dye();
                        try {
                            DyeColor dc = DyeColor.valueOf(matrix[m]);
                            dye.setColor(dc);
                            wizardSkull.setIngredient(letters[m], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[m] + " Using default: " + matrixDefault[m]);
                            if (matrixDefault[m] != null) {
                                wizardSkull.setIngredient(letters[m], matrixDefault[m]);
                            } else {
                                dye.setColor(DyeColor.GRAY);
                                wizardSkull.setIngredient(letters[m], dye.toItemStack().getData());
                            }
                        }
                    }
            }
            ConfigurationSection skullBow = getRecipeConfig().getConfigurationSection("Skull_Bow");
            matrixDefault = new Material[]{Material.AIR, Material.AIR, Material.AIR, Material.SLIME_BALL, Material.BOW, Material.EYE_OF_ENDER, Material.AIR, Material.AIR, Material.AIR};
            matrix[0] = skullBow.getString("TopLeft", "AIR");
            matrix[1] = skullBow.getString("TopMiddle", "AIR");
            matrix[2] = skullBow.getString("TopRight", "AIR");
            matrix[3] = skullBow.getString("CenterLeft", "SLIME_BALL");
            matrix[4] = "BOW";
            matrix[5] = skullBow.getString("CenterRight", "EYE_OF_ENDER");
            matrix[6] = skullBow.getString("BottomLeft", "AIR");
            matrix[7] = skullBow.getString("BottomMiddle", "AIR");
            matrix[8] = skullBow.getString("BottomRight", "AIR");
            ShapedRecipe skullBowRecipe = new ShapedRecipe(this.plugin.recipes.bowTargetItem);
            skullBowRecipe.shape(matrixString);
            for (int n = 0; n < 9; n++) {
                Material mat = Material.getMaterial(matrix[n]);
                if (mat != Material.AIR)
                    if (mat != null) {
                        skullBowRecipe.setIngredient(letters[n], mat);
                    } else {
                        Dye dye = new Dye();
                        try {
                            DyeColor dc = DyeColor.valueOf(matrix[n]);
                            dye.setColor(dc);
                            skullBowRecipe.setIngredient(letters[n], dye.toItemStack().getData());
                        } catch (Exception e) {
                            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Bad recipe value: " + matrix[n] + " Using default: " + matrixDefault[n]);
                            skullBowRecipe.setIngredient(letters[n], matrixDefault[n]);
                        }
                    }
            }
            Iterator<Recipe> rec = this.plugin.server.recipeIterator();
            boolean recupdated = false;
            while (rec.hasNext()) {
                Recipe r = rec.next();
                if (r.getResult().equals(this.plugin.recipes.getSkullBow()) || r.getResult().equals(this.plugin.recipes.getCrazedSkull()) || r.getResult().equals(this.plugin.recipes.getDeviousSkull()) || r.getResult().equals(this.plugin.recipes.getMasterSkull()) || r.getResult().equals(this.plugin.recipes.getWizardSkull())) {
                    rec.remove();
                    recupdated = true;
                }
            }
            if (recupdated)
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": updated recipes...");
            if (crazed.getBoolean("Enabled", true))
                this.plugin.server.addRecipe(crazedSkull);
            if (devious.getBoolean("Enabled", true))
                this.plugin.server.addRecipe(deviousSkull);
            if (master.getBoolean("Enabled", true))
                this.plugin.server.addRecipe(masterSkull);
            if (wizard.getBoolean("Enabled", true))
                this.plugin.server.addRecipe(wizardSkull);
            if (skullBow.getBoolean("Enabled", true))
                this.plugin.server.addRecipe(skullBowRecipe);
            SkullTurret.CRAZED_STACK_SIZE = validateStackSize(crazed.getInt("CraftedAmount", 1));
            SkullTurret.DEVIOUS_STACK_SIZE = validateStackSize(devious.getInt("CraftedAmount", 1));
            SkullTurret.MASTER_STACK_SIZE = validateStackSize(master.getInt("CraftedAmount", 1));
            SkullTurret.WIZARD_STACK_SIZE = validateStackSize(wizard.getInt("CraftedAmount", 1));
            SkullTurret.BOW_STACK_SIZE = validateStackSize(skullBow.getInt("CraftedAmount", 1));
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Recipe Config version. Found " + version + " expected " + '\001');
        }
    }

    private int validateStackSize(int amount) {
        if (amount > 0 && amount <= 64)
            return amount;
        return 1;
    }

    public void loadWeapons() {
        int version = this.plugin.getConfig().getInt("Version", 0);
        int count = 0;
        if (version == 1) {
            ConfigurationSection weapons = this.plugin.getConfig().getConfigurationSection("Weapons");
            for (String weaponName : weapons.getKeys(false)) {
                ConfigurationSection pSec = weapons.getConfigurationSection(weaponName);
                Material wep = getMaterial(weaponName);
                if (wep != null) {
                    double damage = pSec.getDouble("Damage", 1.0D);
                    this.plugin.weapons.put(wep, Double.valueOf(damage));
                    count++;
                    continue;
                }
                SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Weapon type " + weaponName);
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " Weapons from config.");
        } else if (version == 0) {
            this.plugin.weapons.put(Material.AIR, Double.valueOf(1.0D));
            this.plugin.weapons.put(Material.WOOD_SWORD, Double.valueOf(5.0D));
            this.plugin.weapons.put(Material.GOLD_SWORD, Double.valueOf(5.0D));
            this.plugin.weapons.put(Material.STONE_SWORD, Double.valueOf(6.0D));
            this.plugin.weapons.put(Material.IRON_SWORD, Double.valueOf(7.0D));
            this.plugin.weapons.put(Material.DIAMOND_SWORD, Double.valueOf(8.0D));
            this.plugin.weapons.put(Material.WOOD_AXE, Double.valueOf(4.0D));
            this.plugin.weapons.put(Material.GOLD_AXE, Double.valueOf(4.0D));
            this.plugin.weapons.put(Material.STONE_AXE, Double.valueOf(5.0D));
            this.plugin.weapons.put(Material.IRON_AXE, Double.valueOf(6.0D));
            this.plugin.weapons.put(Material.DIAMOND_AXE, Double.valueOf(7.0D));
            count = this.plugin.weapons.size();
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Weapon config version. Found: " + version + " Expected: " + '\001');
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " default Weapons.");
        }
    }

    public void loadEntities() {
        int version = getEntityConfig().getInt("Version", 0);
        if (version == 1) {
            ConfigurationSection entities = getEntityConfig().getConfigurationSection("EntityType");
            int count = 0;
            for (String entityName : entities.getKeys(false)) {
                ConfigurationSection pSec = entities.getConfigurationSection(entityName);
                EntityType et = getEntityType(entityName);
                if (et != null) {
                    int rating = pSec.getInt("Rating");
                    boolean canPoison = pSec.getBoolean("CanPoison", true);
                    boolean mustHeal = pSec.getBoolean("MustHeal", false);
                    this.plugin.entities.put(et, new EntitySettings(rating, canPoison, mustHeal));
                    count++;
                    continue;
                }
                SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Entity type " + entityName);
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " entities from datafile.");
            ConfigurationSection names = getEntityConfig().getConfigurationSection("CustomNames");
            count = 0;
            for (String customName : names.getKeys(false)) {
                ConfigurationSection pSec = names.getConfigurationSection(customName);
                String[] name = pSec.getString("Entities").split(",");
                List<String> checkedNames = new ArrayList<String>();
                byte b;
                int i;
                String[] arrayOfString1;
                for (i = (arrayOfString1 = name).length, b = 0; b < i; ) {
                    String n = arrayOfString1[b];
                    String formated = n.toUpperCase().trim();
                    if (getEntityType(formated) == null) {
                        SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown Entity type in CustomNames " + n);
                    } else {
                        checkedNames.add(formated);
                    }
                    b++;
                }
                this.plugin.customNames.put(customName, checkedNames);
                count++;
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " CustomNames from datafile.");
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown EntityConfig version. Found " + version + " expected " + '\001');
        }
    }

    private EntityType getEntityType(String entityName) {
        EntityType et = null;
        try {
            et = EntityType.valueOf(entityName);
            return et;
        } catch (Exception e) {
            return null;
        }
    }

    public void loadPerPlayerSettings() {
        int version = getPerPlayerConfig().getInt("Version", 0);
        if (version == 3) {
            ConfigurationSection players = getPerPlayerConfig().getConfigurationSection("Players");
            int count = 0;
            for (String playerUUIDString : players.getKeys(false)) {
                UUID playerUUID = UUID.fromString(playerUUIDString);
                ConfigurationSection pSec = players.getConfigurationSection(playerUUIDString);
                String lastKnownName = pSec.getString("LastKnownName", "Unknown");
                int maxTurret = pSec.getInt("MaxTurrets", SkullTurret.MAX_SKULL_PER_PLAYER);
                int maxRange = pSec.getInt("MaxRange", SkullTurret.MAX_RANGE);
                boolean ppsSet = pSec.getBoolean("PPS", false);
                boolean masterDefaults = pSec.getBoolean("MasterDefaults", false);
                String masterSkinName = pSec.getString("MasterDefaultSkin", "");
                String ammoType = pSec.getString("DefaultAmmoType", "arrow");
                boolean masterRedstone = pSec.getBoolean("MasterDefaultRedstone", false);
                boolean masterPatrol = pSec.getBoolean("MasterDefaultPatrol", true);
                boolean wizardDefaults = pSec.getBoolean("WizardDefaults", false);
                String wizardSkinName = pSec.getString("WizardDefaultSkin", "");
                boolean wizardRedstone = pSec.getBoolean("WizardDefaultRedstone", false);
                boolean wizardPatrol = pSec.getBoolean("WizardDefaultPatrol", true);
                PerPlayerSettings pps = null;
                maxRange = getMaxRange(false, false, playerUUIDString, maxRange);
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
                    count++;
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
        } else if (version == 1) {
            SkullTurret.PPS_UPDATE = true;
            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                public void run() {
                    ConfigurationSection players = DataStore.this.getPerPlayerConfig().getConfigurationSection("Players");
                    int count = 0;
                    int failCount = 0;
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 1 datafile detected, updating...");
                    for (String playerName : players.getKeys(false)) {
                        ConfigurationSection pSec = players.getConfigurationSection(playerName);
                        int maxTurret = pSec.getInt("MaxTurrets");
                        int maxRange = pSec.getInt("MaxRange");
                        try {
                            UUID playerUUID = DataStore.this.playerNameUUID.get(playerName);
                            if (playerUUID == null) {
                                playerUUID = FindUUID.getUUIDFromPlayerName(playerName);
                                DataStore.this.playerNameUUID.put(playerName, playerUUID);
                            }
                            if (DataStore.this.getMaxRange(false, false, playerUUID.toString(), maxRange) == maxRange) {
                                PerPlayerSettings pps = new PerPlayerSettings(playerUUID, maxTurret, maxRange);
                                pps.setLastKnownPlayerName(playerName);
                                DataStore.this.plugin.perPlayerSettings.put(playerUUID, pps);
                                count++;
                            }
                        } catch (Exception e) {
                            failCount++;
                        }
                    }
                    if (failCount > 0)
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": failed to Load " + failCount + " players from datafile.");
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
                    DataStore.this.savePerPlayerSettings();
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 3 datafile saved...");
                    SkullTurret.PPS_UPDATE = false;
                }
            });
        } else if (version == 2) {
            SkullTurret.PPS_UPDATE = true;
            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                public void run() {
                    ConfigurationSection players = DataStore.this.getPerPlayerConfig().getConfigurationSection("Players");
                    int count = 0;
                    int failCount = 0;
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 2 datafile detected, updating...");
                    for (String playerName : players.getKeys(false)) {
                        ConfigurationSection pSec = players.getConfigurationSection(playerName);
                        int maxTurret = pSec.getInt("MaxTurrets", SkullTurret.MAX_SKULL_PER_PLAYER);
                        int maxRange = pSec.getInt("MaxRange", SkullTurret.MAX_RANGE);
                        boolean ppsSet = pSec.getBoolean("PPS", false);
                        boolean masterDefaults = pSec.getBoolean("MasterDefaults", false);
                        String masterSkinName = pSec.getString("MasterDefaultSkin", "");
                        String ammoType = pSec.getString("DefaultAmmoType", "arrow");
                        boolean masterRedstone = pSec.getBoolean("MasterDefaultRedstone", false);
                        boolean masterPatrol = pSec.getBoolean("MasterDefaultPatrol", true);
                        boolean wizardDefaults = pSec.getBoolean("WizardDefaults", false);
                        String wizardSkinName = pSec.getString("WizardDefaultSkin", "");
                        boolean wizardRedstone = pSec.getBoolean("WizardDefaultRedstone", false);
                        boolean wizardPatrol = pSec.getBoolean("WizardDefaultPatrol", true);
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
                            if (pps != null) {
                                pps.setLastKnownPlayerName(playerName);
                                DataStore.this.plugin.perPlayerSettings.put(playerUUID, pps);
                                count++;
                            }
                        } catch (Exception e) {
                            failCount++;
                        }
                    }
                    if (failCount > 0)
                        SkullTurret.LOGGER.info(SkullTurret.pluginName + ": failed to Load " + failCount + " players from datafile.");
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " players from datafile.");
                    DataStore.this.savePerPlayerSettings();
                    SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Version 3 datafile saved...");
                    SkullTurret.PPS_UPDATE = false;
                }
            });
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown playerinfo version. Found " + version + " expected " + '\003');
        }
    }

    public void savePerPlayerSettings() {
        getPerPlayerConfig().set("Version", Integer.valueOf(3));
        ConfigurationSection playersSec = getPerPlayerConfig().createSection("Players");
        for (PerPlayerSettings pps : this.plugin.perPlayerSettings.values()) {
            ConfigurationSection pSec = playersSec.createSection(pps.getPlayerUUID().toString());
            pSec.set("LastKnownName", pps.getLastKnownPlayerName());
            if (pps.isPps()) {
                pSec.set("PPS", Boolean.valueOf(true));
                pSec.set("MaxTurrets", Integer.valueOf(pps.getMaxTurrets()));
                pSec.set("MaxRange", Integer.valueOf(pps.getMaxRange()));
            }
            if (pps.isMasterDefaults()) {
                pSec.set("MasterDefaults", Boolean.valueOf(true));
                pSec.set("MasterDefaultSkin", pps.getMasterSkinName());
                pSec.set("DefaultAmmoType", pps.getAmmoTypeName());
                pSec.set("MasterDefaultRedstone", Boolean.valueOf(pps.isMasterRedstone()));
                pSec.set("MasterDefaultPatrol", Boolean.valueOf(pps.isMasterPatrol()));
            }
            if (pps.isWizardDefaults()) {
                pSec.set("WizardDefaults", Boolean.valueOf(true));
                pSec.set("WizardDefaultSkin", pps.getWizardSkinName());
                pSec.set("WizardDefaultRedstone", Boolean.valueOf(pps.isWizardRedstone()));
                pSec.set("WizardDefaultPatrol", Boolean.valueOf(pps.isWizardPatrol()));
            }
        }
        saveConfig(this.perPlayerConfig, this.perPlayerConfigFile);
    }

    public void loadPerGroupSettings() {
        int version = getPerGroupConfig().getInt("Version", 0);
        if (version == 1) {
            ConfigurationSection groups = getPerGroupConfig().getConfigurationSection("Groups");
            int count = 0;
            for (String groupName : groups.getKeys(false)) {
                ConfigurationSection pSec = groups.getConfigurationSection(groupName);
                int maxTurret = pSec.getInt("MaxTurrets");
                int maxRange = pSec.getInt("MaxRange");
                if (getMaxRange(true, false, groupName, maxRange) == maxRange) {
                    PerPlayerGroups ppg = new PerPlayerGroups(groupName, maxTurret, maxRange);
                    this.plugin.perPlayerGroups.put(groupName, ppg);
                    count++;
                }
            }
            SkullTurret.LOGGER.info(SkullTurret.pluginName + ": Loaded " + count + " groups from datafile.");
        } else {
            SkullTurret.LOGGER.warning(SkullTurret.pluginName + ": Unknown groupinfo version. Found " + version + " expected " + '\001');
        }
    }

    private void loadAmmoList() {
        if (SkullTurret.ALLOW_SNOWBALLS)
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.SNOW_BALL, 1));
        if (SkullTurret.ALLOW_FIRE_CHARGE)
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.FIREBALL, 1));
        if (SkullTurret.ALLOW_ARROWS)
            SkullTurret.plugin.ammoList.add(new ItemStack(Material.ARROW, 1));
    }

    private String loadOldDatabaseBin() {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(this.databaseFileOld));
            String input = (String) inputStream.readObject();
            inputStream.close();
            this.databaseFileOld.renameTo(new File(this.directory, "preUUID.bin"));
            return input;
        } catch (FileNotFoundException e) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Find " + this.databaseFileOld.getAbsolutePath());
        } catch (IOException e) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Access " + this.databaseFileOld.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String loadDatabaseBin() {
        try {
            FileInputStream fileInputStream = new FileInputStream(this.databaseFileNew);
            GZIPInputStream zipInputStream = new GZIPInputStream(fileInputStream);
            ObjectInputStream inputStream = new ObjectInputStream(zipInputStream);
            String input = (String) inputStream.readObject();
            inputStream.close();
            fileInputStream.close();
            return input;
        } catch (FileNotFoundException e) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Find " + this.databaseFileNew.getAbsolutePath());
        } catch (IOException e) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Cant Access " + this.databaseFileNew.getAbsolutePath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public int saveDatabase(boolean exitSave) {
        StringBuffer databin = new StringBuffer();
        if (this.databaseFileNew.exists()) {
            int count = 0;
            for (PlacedSkull pc : this.plugin.skullMap.values()) {
                if (!pc.failed) {
                    if (pc instanceof MobileSkull) {
                        MobileSkull ms = (MobileSkull) pc;
                        databin.append(ms.toString() + "\n");
                    } else {
                        databin.append(pc.toString() + "\n");
                    }
                    count++;
                }
            }
            if (exitSave) {
                safeSaveDatabase(databin);
            } else {
                quickSaveDatabase(databin);
            }
            return count;
        }
        return -1;
    }

    public void safeSaveDatabase(StringBuffer databin) {
        if (this.databaseFileNew.exists()) {
            ObjectOutputStream outputStream = null;
            GZIPOutputStream zipOutputStream = null;
            FileOutputStream fileOutputStream = null;
            FileInputStream fileInputStream = null;
            GZIPInputStream zipInputStream = null;
            ObjectInputStream inputStream = null;
            try {
                fileOutputStream = new FileOutputStream(this.tmpDatabaseFile);
                zipOutputStream = new GZIPOutputStream(fileOutputStream);
                outputStream = new ObjectOutputStream(zipOutputStream);
                outputStream.writeObject(databin.toString());
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                if (fileOutputStream != null)
                    fileOutputStream.close();
                fileInputStream = new FileInputStream(this.tmpDatabaseFile);
                zipInputStream = new GZIPInputStream(fileInputStream);
                inputStream = new ObjectInputStream(zipInputStream);
                String tmpDatabin = (String) inputStream.readObject();
                inputStream.close();
                if (fileInputStream != null)
                    fileInputStream.close();
                File oldFile = new File(this.directory, "skullturret.old");
                if (oldFile.exists())
                    oldFile.delete();
                if (tmpDatabin.equals(databin.toString())) {
                    if (this.databaseFileNew.exists())
                        this.databaseFileNew.renameTo(oldFile);
                    this.tmpDatabaseFile.renameTo(this.databaseFileNew);
                    if (oldFile.exists())
                        oldFile.delete();
                } else {
                    SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": detected database corruption. Database was not saved correctly!\n ");
                }
            } catch (IOException e) {
                SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not Access door database\n", e);
            } catch (ClassNotFoundException e) {
                SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": can not read temp File. Database was not saved!\n ", e);
                e.printStackTrace();
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
        }
    }

    private void quickSaveDatabase(StringBuffer databin) {
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
            if (fileOutputStream != null)
                fileOutputStream.close();
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
        init();
    }

    private int getMaxRange(boolean isGroup, boolean maxRange, String playerUUID, int testRange) {
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
            if (!this.perPlayerConfigFile.exists())
                saveDefaultConfig(this.perPlayerConfigFile, "playerinfo.yml");
        }
        this.perPlayerConfig = YamlConfiguration.loadConfiguration(this.perPlayerConfigFile);
        InputStream defConfigStream = this.plugin.getResource("playerinfo.yml");
        InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.perPlayerConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getPerPlayerConfig() {
        if (this.perPlayerConfig == null)
            reloadPerPlayerConfig();
        return this.perPlayerConfig;
    }

    public FileConfiguration getRecipeConfig() {
        if (this.recipeConfig == null)
            reloadRecipeConfig();
        return this.recipeConfig;
    }

    public void reloadRecipeConfig() {
        if (this.recipeConfigFile == null) {
            this.recipeConfigFile = new File(this.plugin.getDataFolder(), "recipes.yml");
            if (!this.recipeConfigFile.exists())
                saveDefaultConfig(this.recipeConfigFile, "recipes.yml");
        }
        this.recipeConfig = YamlConfiguration.loadConfiguration(this.recipeConfigFile);
        InputStream defConfigStream = this.plugin.getResource("recipes.yml");
        InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.recipeConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getEntityConfig() {
        if (this.entityConfig == null)
            reloadEntityConfig();
        return this.entityConfig;
    }

    public void reloadEntityConfig() {
        if (this.entityConfigFile == null) {
            this.entityConfigFile = new File(this.plugin.getDataFolder(), "entities.yml");
            if (!this.entityConfigFile.exists())
                saveDefaultConfig(this.entityConfigFile, "entities.yml");
        }
        this.entityConfig = YamlConfiguration.loadConfiguration(this.entityConfigFile);
        InputStream defConfigStream = this.plugin.getResource("entities.yml");
        InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.entityConfig.setDefaults(defConfig);
        }
    }

    public void reloadPerGroupConfig() {
        if (this.perGroupConfigFile == null) {
            this.perGroupConfigFile = new File(this.plugin.getDataFolder(), "groupinfo.yml");
            if (!this.perGroupConfigFile.exists())
                saveDefaultConfig(this.perGroupConfigFile, "groupinfo.yml");
        }
        this.perGroupConfig = YamlConfiguration.loadConfiguration(this.perGroupConfigFile);
        InputStream defConfigStream = this.plugin.getResource("groupinfo.yml");
        InputStreamReader isr = new InputStreamReader(defConfigStream);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(isr);
            this.perGroupConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getPerGroupConfig() {
        if (this.perGroupConfig == null)
            reloadPerGroupConfig();
        return this.perGroupConfig;
    }

    public File saveDefaultConfig(File toTest, String fileName) {
        if (toTest == null)
            toTest = new File(this.plugin.getDataFolder(), fileName);
        if (!toTest.exists())
            this.plugin.saveResource(fileName, false);
        return toTest;
    }

    public void saveConfig(FileConfiguration fc, File f) {
        if (fc == null || f == null)
            return;
        try {
            fc.save(f);
        } catch (IOException ex) {
            SkullTurret.LOGGER.log(Level.SEVERE, SkullTurret.pluginName + ": Could not save config to " + f, ex);
        }
    }

    private Material getMaterial(String matString) {
        Material material = Material.matchMaterial(matString);
        return (material == null) ? Material.LAPIS_BLOCK : material;
    }
}
