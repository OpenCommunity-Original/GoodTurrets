package plugin.arcwolf.skullturrets;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandHandler {
    private final SkullTurret plugin;

    private final DataStore ds;

    public CommandHandler(SkullTurret plugin) {
        this.plugin = plugin;
        this.ds = new DataStore(plugin);
    }

    public boolean inGame(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;
        CustomPlayer cp = CustomPlayer.getSettings(player);
        String command = cmd.getName().toLowerCase();
        String legCommand = "";
        if (isLegacyCommand(command)) {
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("legacyCommandError"), SkullTurret.pluginName, "/skull ?")));
            return commands(cp, player, sender, command, commandLabel, args, false);
        }
        if (command.equals("sk") || command.equals("skull"))
            if (args.length == 0 || args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
                if (args.length == 0)
                    return simpleInGameHelp(player);
                inGameHelp(sender, cmd, commandLabel, args);
            } else {
                if (args.length == 1) {
                    if (args[0].equalsIgnoreCase("done")) {
                        legCommand = "skdone";
                    } else if (args[0].equalsIgnoreCase("rotate")) {
                        legCommand = "skrotate";
                    } else if (args[0].equalsIgnoreCase("redstone")) {
                        legCommand = "skredstone";
                    } else if (args[0].equalsIgnoreCase("patrol")) {
                        legCommand = "skpatrol";
                    } else if (args[0].equalsIgnoreCase("skin")) {
                        legCommand = "skskin";
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        legCommand = "skreload";
                    } else if (args[0].equalsIgnoreCase("list")) {
                        legCommand = "sklistplayer";
                    } else if (args[0].equalsIgnoreCase("listall")) {
                        legCommand = "sklistallplayer";
                    } else if (args[0].equalsIgnoreCase("edit")) {
                        legCommand = "skedit";
                    } else if (args[0].equalsIgnoreCase("ammo")) {
                        legCommand = "skammo";
                    } else if (args[0].equalsIgnoreCase("give")) {
                        legCommand = "skgive";
                    } else if (args[0].equalsIgnoreCase("debug")) {
                        legCommand = "skdebug";
                    } else if (args[0].equalsIgnoreCase("default")) {
                        legCommand = "skdefault";
                    } else if (args[0].equalsIgnoreCase("costs")) {
                        legCommand = "skcosts";
                    } else {
                        return simpleInGameHelp(player);
                    }
                    String[] legArgs = new String[0];
                    return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                }
                if (args.length == 2) {
                    String[] legArgs = {args[1]};
                    boolean notEnoughArgs = false;
                    if (args[0].equalsIgnoreCase("ammo")) {
                        legCommand = "skammo";
                    } else if (args[0].equalsIgnoreCase("give")) {
                        legCommand = "skgive";
                    } else if (args[0].equalsIgnoreCase("skin")) {
                        legCommand = "skskin";
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        legCommand = "skreload";
                    } else if (args[0].equalsIgnoreCase("debug")) {
                        legCommand = "skdebug";
                    } else if (args[0].equalsIgnoreCase("default")) {
                        legCommand = "skdefault";
                    } else if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("friend")) {
                        legCommand = "skaddfriend";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("enemy")) {
                        legCommand = "skaddenemy";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("add") && args[1].equalsIgnoreCase("player")) {
                        legCommand = "skaddplayer";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("rem") && args[1].equalsIgnoreCase("friend")) {
                        legCommand = "skremfriend";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("rem") && args[1].equalsIgnoreCase("enemy")) {
                        legCommand = "skremenemy";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("rem") && args[1].equalsIgnoreCase("player")) {
                        legCommand = "skremplayer";
                        notEnoughArgs = true;
                    } else if (args[0].equalsIgnoreCase("destruct")) {
                        legCommand = "skdestruct";
                    } else {
                        return simpleInGameHelp(player);
                    }
                    if (notEnoughArgs) {
                        String[] dumpLeg = new String[0];
                        legArgs = dumpLeg;
                    }
                    return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                }
                if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("add")) {
                        if (args[1].equalsIgnoreCase("player")) {
                            legCommand = "skaddplayer";
                        } else if (args[1].equalsIgnoreCase("friend")) {
                            legCommand = "skaddfriend";
                        } else if (args[1].equalsIgnoreCase("enemy")) {
                            legCommand = "skaddenemy";
                        } else {
                            return simpleInGameHelp(player);
                        }
                        String[] legArgs = {args[2]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equalsIgnoreCase("rem")) {
                        if (args[1].equalsIgnoreCase("player")) {
                            legCommand = "skremplayer";
                        } else if (args[1].equalsIgnoreCase("friend")) {
                            legCommand = "skremfriend";
                        } else if (args[1].equalsIgnoreCase("enemy")) {
                            legCommand = "skremenemy";
                        } else {
                            return simpleInGameHelp(player);
                        }
                        String[] legArgs = {args[2]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equals("give")) {
                        legCommand = "skgive";
                        String[] legArgs = {args[1], args[2]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equals("buy")) {
                        legCommand = "skbuy";
                        String[] legArgs = {args[1], args[2]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equalsIgnoreCase("default")) {
                        legCommand = "skdefault";
                        String[] legArgs = {args[1], args[2]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    return simpleInGameHelp(player);
                }
                if (args.length == 4) {
                    if (args[0].equalsIgnoreCase("add")) {
                        if (args[1].equalsIgnoreCase("friend")) {
                            legCommand = "skaddfriend";
                        } else if (args[1].equalsIgnoreCase("enemy")) {
                            legCommand = "skaddenemy";
                        } else {
                            return simpleInGameHelp(player);
                        }
                        String[] legArgs = {args[2], args[3]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equalsIgnoreCase("rem")) {
                        if (args[1].equalsIgnoreCase("friend")) {
                            legCommand = "skremfriend";
                        } else if (args[1].equalsIgnoreCase("enemy")) {
                            legCommand = "skremenemy";
                        } else {
                            return simpleInGameHelp(player);
                        }
                        String[] legArgs = {args[2], args[3]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equalsIgnoreCase("give")) {
                        legCommand = "skgive";
                        String[] legArgs = {args[1], args[2], args[3]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    return simpleInGameHelp(player);
                }
                if (args.length == 5) {
                    if (args[0].equalsIgnoreCase("add")) {
                        if (args[1].equalsIgnoreCase("player")) {
                            legCommand = "skaddplayer";
                        } else if (args[1].equals("group")) {
                            legCommand = "skaddgroup";
                        } else {
                            return simpleInGameHelp(player);
                        }
                        String[] legArgs = {args[2], args[3], args[4]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    if (args[0].equalsIgnoreCase("give")) {
                        legCommand = "skgive";
                        String[] legArgs = {args[1], args[2], args[3], args[4]};
                        return commands(cp, player, sender, legCommand, commandLabel, legArgs, false);
                    }
                    return simpleInGameHelp(player);
                }
            }
        return true;
    }

    public boolean inConsole(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String command = cmd.getName().toLowerCase();
        String legCommand = "";
        CustomPlayer cp = CustomPlayer.getSettings(sender);
        if (isLegacyCommand(command)) {
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("legacyCommandError"), SkullTurret.pluginName, "/skull ?")));
            return commands(cp, null, sender, command, commandLabel, args, true);
        }
        if (command.equals("sk") || command.equals("skull")) {
            if (args.length == 0 || args[0].equals("?") || args[0].equalsIgnoreCase("help")) {
                if (args.length == 0)
                    return simpleInConsoleHelp(sender);
                return inConsoleHelp(sender, cmd, commandLabel, args);
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    legCommand = "skreload";
                } else if (args[0].equalsIgnoreCase("listall")) {
                    legCommand = "sklistallplayer";
                } else if (args[0].equalsIgnoreCase("debug")) {
                    legCommand = "skdebug";
                } else {
                    return simpleInConsoleHelp(sender);
                }
                String[] legArgs = new String[0];
                return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
            }
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("reload")) {
                    legCommand = "skreload";
                } else if (args[0].equalsIgnoreCase("debug")) {
                    legCommand = "skdebug";
                } else if (args[0].equals("destruct")) {
                    legCommand = "skdestruct";
                } else {
                    return simpleInConsoleHelp(sender);
                }
                String[] legArgs = {args[1]};
                return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("add")) {
                    if (args[1].equalsIgnoreCase("player")) {
                        legCommand = "skaddplayer";
                    } else {
                        return simpleInConsoleHelp(sender);
                    }
                    String[] legArgs = {args[2]};
                    return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
                }
                if (args[0].equalsIgnoreCase("rem")) {
                    if (args[1].equalsIgnoreCase("player")) {
                        legCommand = "skremplayer";
                    } else {
                        return simpleInConsoleHelp(sender);
                    }
                    String[] legArgs = {args[2]};
                    return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
                }
                return simpleInConsoleHelp(sender);
            }
            if (args.length == 4) {
                if (args[0].equalsIgnoreCase("give")) {
                    legCommand = "skgive";
                    String[] legArgs = {args[1], args[2], args[3]};
                    return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
                }
                return simpleInConsoleHelp(sender);
            }
            if (args.length == 5) {
                if (args[0].equalsIgnoreCase("add")) {
                    if (args[1].equalsIgnoreCase("player")) {
                        legCommand = "skaddplayer";
                    } else {
                        return simpleInConsoleHelp(sender);
                    }
                    String[] legArgs = {args[2], args[3], args[4]};
                    return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
                }
                if (args[0].equalsIgnoreCase("give")) {
                    legCommand = "skgive";
                    String[] legArgs = {args[1], args[2], args[3], args[4]};
                    return commands(cp, null, sender, legCommand, commandLabel, legArgs, true);
                }
                return simpleInConsoleHelp(sender);
            }
        }
        return true;
    }

    private boolean isLegacyCommand(String name) {
        return !(!name.equals("skskin") && !name.equals("skdebug") && !name.equals("skgive") && !name.equals("skaddfriend") && !name.equals("skaddenemy") &&
                !name.equals("skremfriend") && !name.equals("skremenemy") && !name.equals("skedit") && !name.equals("skdone") && !name.equals("skreload") &&
                !name.equals("skpatrol") && !name.equals("skrotate") && !name.equals("skredstone") && !name.equals("skammo") && !name.equals("skaddplayer") &&
                !name.equals("skremplayer") && !name.equals("sklistplayer") && !name.equals("sklistallplayer"));
    }

    private boolean commands(final CustomPlayer cp, Player player, final CommandSender sender, String command, String commandLabel, final String[] args, boolean console) {
        if (!console && command.equals("skskin")) {
            if (this.plugin.hasPermission(player, "skullturret.skin")) {
                if (cp.pc == null) {
                    if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                        if (cp.updateAll) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                            return true;
                        }
                        updateAllOwnedSkulls(player, command, args);
                        return true;
                    }
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                    return true;
                }
                if (cp.pc.getIntelligence().canSkinChange()) {
                    if (args.length == 0) {
                        cp.pc.setSkin("");
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("skinUpdateSuccess")));
                    } else {
                        cp.pc.threadedSetSkin(args[0], player);
                    }
                } else {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinUpdateSuccess"), cp.pc.getIntelligence().getNormalName())));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
            }
            return true;
        }
        if (command.equals("skdestruct")) {
            if (!console && !this.plugin.hasPermission(player, "skullturret.destruct")) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull destruct <playerName>")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? destruct")));
            } else if (args.length == 1) {
                final String playerName = args[0];
                if (!cp.UUIDLookup) {
                    this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                        public void run() {
                            cp.UUIDLookup = true;
                            boolean fail = false;
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), new Object[]{this.val$playerName})));
                            try {
                                UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                                cp.UUIDLookup = false;
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), new Object[]{this.val$playerName, uuid.toString()})));
                                int kills = 0;
                                for (PlacedSkull pc : CommandHandler.this.plugin.skullMap.values()) {
                                    if (pc.getSkullCreator().equals(uuid)) {
                                        pc.die();
                                        kills++;
                                    }
                                }
                                if (kills > 0) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullsDestructed"), new Object[]{this.val$playerName, Integer.valueOf(kills)})));
                                } else {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noSkullsDestructed"), new Object[]{this.val$playerName})));
                                }
                                CommandHandler.this.ds.saveDatabase(false);
                            } catch (Exception e) {
                                fail = true;
                            }
                            if (fail)
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                            cp.UUIDLookup = false;
                        }
                    });
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                }
            }
            return true;
        }
        if (command.equals("skdefault")) {
            if (!this.plugin.hasPermission(player, "skullturret.default")) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("rightClickSkull")));
                cp.command = "default";
                return true;
            }
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reset")) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("rightClickSkull")));
                    cp.command = "resetDefault";
                    return true;
                }
                sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull default <reset (w / m)>")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? default")));
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("reset")) {
                    PerPlayerSettings ppsDefaults = this.plugin.perPlayerSettings.get(player.getUniqueId());
                    if (ppsDefaults == null) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noDefaultsReset")));
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("w") || args[1].equalsIgnoreCase("wizard")) {
                        if (ppsDefaults.isWizardDefaults()) {
                            ppsDefaults.setWizardDefaults(false);
                            ppsDefaults.cleanUpPPS();
                            cp.clearPlayer();
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), "Wizard")));
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("commandCompleted")));
                        }
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                        return true;
                    }
                    if (args[1].equalsIgnoreCase("m") || args[1].equalsIgnoreCase("master")) {
                        if (ppsDefaults.isMasterDefaults()) {
                            ppsDefaults.setMasterDefaults(false);
                            ppsDefaults.cleanUpPPS();
                            cp.clearPlayer();
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), "Master")));
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("commandCompleted")));
                        }
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                        return true;
                    }
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull default <reset (w / m)>")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? default")));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull default <reset (w / m)>")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? default")));
            }
        } else {
            if (command.equals("skdebug")) {
                if (!console && !this.plugin.hasPermission(player, "skullturret.debug"))
                    return true;
                if (args.length == 0) {
                    SkullTurret.DEBUG = 0;
                } else if (args.length == 1) {
                    try {
                        int d = Integer.parseInt(args[0]);
                        SkullTurret.DEBUG = d;
                    } catch (Exception e) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("debugNum")));
                    }
                }
                sender.sendMessage("SKDEBUG = " + SkullTurret.DEBUG);
                return true;
            }
            if (command.equals("skbuy")) {
                if (console)
                    return true;
                if (!this.plugin.hasPermission(player, "skullturret.buy.crazed") && !this.plugin.hasPermission(player, "skullturret.buy.devious") && !this.plugin.hasPermission(player, "skullturret.buy.master") && !this.plugin.hasPermission(player, "skullturret.buy.wizard") && !this.plugin.hasPermission(player, "skullturret.buy.bow")) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    return true;
                }
                if (this.plugin.econ == null) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("vaultNotInstalled"), SkullTurret.pluginName)));
                    SkullTurret.LOGGER.warning(Utils.parseText(String.format(Utils.getLocalization("vaultNotInstalled"), SkullTurret.pluginName)));
                    return true;
                }
                if (args.length == 2) {
                    String name = args[0];
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(args[1]);
                        if (amount < 1) {
                            amount = 1;
                        } else if (amount > 64) {
                            amount = 64;
                        }
                    } catch (Exception e) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("itemAmountNotNumber")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull buy <item> (amount)")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? buy")));
                        return true;
                    }
                    if (name.equalsIgnoreCase("crazed") || name.equalsIgnoreCase("c")) {
                        if (!this.plugin.hasPermission(player, "skullturret.buy.crazed")) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionToBuy")));
                            return true;
                        }
                        doTransaction(player, amount, name);
                        return true;
                    }
                    if (name.equalsIgnoreCase("devious") || name.equalsIgnoreCase("d")) {
                        if (!this.plugin.hasPermission(player, "skullturret.buy.devious")) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionToBuy")));
                            return true;
                        }
                        doTransaction(player, amount, name);
                        return true;
                    }
                    if (name.equalsIgnoreCase("master") || name.equalsIgnoreCase("m")) {
                        if (!this.plugin.hasPermission(player, "skullturret.buy.master")) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionToBuy")));
                            return true;
                        }
                        doTransaction(player, amount, name);
                        return true;
                    }
                    if (name.equalsIgnoreCase("wizard") || name.equalsIgnoreCase("w")) {
                        if (!this.plugin.hasPermission(player, "skullturret.buy.wizard")) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionToBuy")));
                            return true;
                        }
                        doTransaction(player, amount, name);
                        return true;
                    }
                    if (name.equalsIgnoreCase("bow") || name.equalsIgnoreCase("b")) {
                        if (!this.plugin.hasPermission(player, "skullturret.buy.bow")) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionToBuy")));
                            return true;
                        }
                        doTransaction(player, amount, name);
                        return true;
                    }
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("unknownItemType")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? buy")));
                    return true;
                }
                sender.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull buy <item> <amount>")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? buy")));
            } else {
                if (command.equals("skcosts")) {
                    if (console)
                        return true;
                    if (this.plugin.hasPermission(player, "skullturret.buy.crazed") || this.plugin.hasPermission(player, "skullturret.buy.devious") || this.plugin.hasPermission(player, "skullturret.buy.master") || this.plugin.hasPermission(player, "skullturret.buy.wizard") || this.plugin.hasPermission(player, "skullturret.buy.bow")) {
                        if (this.plugin.econ == null) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("vaultNotInstalled"), SkullTurret.pluginName)));
                            SkullTurret.LOGGER.warning(Utils.parseText(String.format(Utils.getLocalization("vaultNotInstalled"), SkullTurret.pluginName)));
                            return true;
                        }
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("purchaseHeader")));
                        sender.sendMessage("");
                        if (this.plugin.hasPermission(player, "skullturret.buy.crazed"))
                            sender.sendMessage("Crazed Skull: " + SkullTurret.ECON_CRAZED_COST);
                        if (this.plugin.hasPermission(player, "skullturret.buy.devious"))
                            sender.sendMessage("Devious Skull: " + SkullTurret.ECON_DEVIOUS_COST);
                        if (this.plugin.hasPermission(player, "skullturret.buy.master"))
                            sender.sendMessage("Master Skull: " + SkullTurret.ECON_MASTER_COST);
                        if (this.plugin.hasPermission(player, "skullturret.buy.wizard"))
                            sender.sendMessage("Wizard Skull: " + SkullTurret.ECON_WIZARD_COST);
                        if (this.plugin.hasPermission(player, "skullturret.buy.bow"))
                            sender.sendMessage("Skull Bow: " + SkullTurret.ECON_BOW_COST);
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("balanceInfo"), String.format("%s", this.plugin.econ.format(this.plugin.econ.getBalance(player))))));
                        return true;
                    }
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    return true;
                }
                if (command.equals("skgive")) {
                    if (!console && !this.plugin.hasPermission(player, "skullturret.give")) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                        return true;
                    }
                    if (args.length < 1) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughArgs")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                    } else {
                        int amount = 1;
                        int ammoAmount = 1;
                        if (args.length == 2) {
                            try {
                                if (args[0].equalsIgnoreCase("tempdevious") || args[0].equalsIgnoreCase("tempmaster") || args[0].equalsIgnoreCase("td") || args[0].equalsIgnoreCase("tm")) {
                                    ammoAmount = Integer.parseInt(args[1]);
                                    if (ammoAmount < 1) {
                                        ammoAmount = 1;
                                    } else if (ammoAmount > 448) {
                                        ammoAmount = 448;
                                    }
                                } else {
                                    amount = Integer.parseInt(args[1]);
                                    if (amount < 1) {
                                        amount = 1;
                                    } else if (amount > 64) {
                                        amount = 64;
                                    }
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidNumber")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                return true;
                            }
                        } else if (args.length == 3) {
                            try {
                                if (args[0].equalsIgnoreCase("tempdevious") || args[0].equalsIgnoreCase("tempmaster") || args[0].equalsIgnoreCase("td") || args[0].equalsIgnoreCase("tm")) {
                                    if (!console) {
                                        if (isNumber(args[1])) {
                                            amount = Integer.parseInt(args[1]);
                                            ammoAmount = Integer.parseInt(args[2]);
                                        } else {
                                            player = Utils.getPlayer(args[1]);
                                            amount = Integer.parseInt(args[2]);
                                            console = true;
                                            if (player == null) {
                                                sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidPlayer")));
                                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                                return true;
                                            }
                                        }
                                    } else {
                                        amount = Integer.parseInt(args[2]);
                                        player = Utils.getPlayer(args[1]);
                                        if (player == null) {
                                            sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidPlayer")));
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                            return true;
                                        }
                                    }
                                    if (amount < 1) {
                                        amount = 1;
                                    } else if (amount > 64) {
                                        amount = 64;
                                    }
                                    if (ammoAmount < 1) {
                                        ammoAmount = 1;
                                    } else if (ammoAmount > 448) {
                                        ammoAmount = 448;
                                    }
                                } else {
                                    amount = Integer.parseInt(args[2]);
                                    if (amount < 1) {
                                        amount = 1;
                                    } else if (amount > 64) {
                                        amount = 64;
                                    }
                                    player = Utils.getPlayer(args[1]);
                                    if (player == null) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidPlayer")));
                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                        return true;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidNumber")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                return true;
                            }
                        } else if (args.length == 4 && (args[0].equalsIgnoreCase("tempdevious") || args[0].equalsIgnoreCase("tempmaster") || args[0].equalsIgnoreCase("td") || args[0].equalsIgnoreCase("tm"))) {
                            try {
                                amount = Integer.parseInt(args[2]);
                                if (amount < 1) {
                                    amount = 1;
                                } else if (amount > 64) {
                                    amount = 64;
                                }
                                ammoAmount = Integer.parseInt(args[3]);
                                if (ammoAmount < 1) {
                                    ammoAmount = 1;
                                } else if (ammoAmount > 448) {
                                    ammoAmount = 448;
                                }
                            } catch (Exception e) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidNumber")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                return true;
                            }
                            player = Utils.getPlayer(args[1]);
                            if (player == null) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidPlayer")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount)")));
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                                return true;
                            }
                        }
                        String name = args[0];
                        boolean gaveItem = false;
                        boolean temp = false;
                        if (name.equalsIgnoreCase("crazed") || name.equalsIgnoreCase("c")) {
                            ItemStack is = this.plugin.recipes.getCrazedSkull(amount);
                            gaveItem = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("devious") || name.equalsIgnoreCase("d")) {
                            ItemStack is = this.plugin.recipes.getDeviousSkull(amount);
                            gaveItem = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("master") || name.equalsIgnoreCase("m")) {
                            ItemStack is = this.plugin.recipes.getMasterSkull(amount);
                            gaveItem = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("wizard") || name.equalsIgnoreCase("w")) {
                            ItemStack is = this.plugin.recipes.getWizardSkull(amount);
                            gaveItem = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("bow") || name.equalsIgnoreCase("b")) {
                            ItemStack is = this.plugin.recipes.getSkullBow(amount);
                            gaveItem = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("tempdevious") || name.equalsIgnoreCase("td")) {
                            ItemStack is = this.plugin.recipes.getMobileDeviousSkull(amount, ammoAmount);
                            gaveItem = true;
                            temp = true;
                            player.getInventory().addItem(is);
                        } else if (name.equalsIgnoreCase("tempmaster") || name.equalsIgnoreCase("tm")) {
                            ItemStack is = this.plugin.recipes.getMobileMasterSkull(amount, ammoAmount);
                            gaveItem = true;
                            temp = true;
                            player.getInventory().addItem(is);
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("unknownItemType")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? give")));
                        }
                        if (gaveItem) {
                            String type = name + " skull";
                            if (name.equalsIgnoreCase("bow") || name.equalsIgnoreCase("b"))
                                type = "skull bow";
                            if (name.equalsIgnoreCase("tempdevious") || name.equalsIgnoreCase("td"))
                                type = "Temporary Devious Skull Turret";
                            if (name.equalsIgnoreCase("tempmaster") || name.equalsIgnoreCase("tm"))
                                type = "Temporary Master Skull Turret";
                            if (name.equalsIgnoreCase("c"))
                                type = "Crazed Skull";
                            if (name.equalsIgnoreCase("d"))
                                type = "Devious Skull";
                            if (name.equalsIgnoreCase("m"))
                                type = "Master Skull";
                            if (name.equalsIgnoreCase("w"))
                                type = "Wizard Skull";
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("giveItem"), "( " + ChatColor.YELLOW + amount + ChatColor.GREEN + " ) " + type + (
                                    (amount > 1) ? "'s" : "") + (((args.length == 3 && (!temp || console)) || (args.length == 4 && temp)) ? (" to " +
                                    ChatColor.GOLD + args[1] + ChatColor.GREEN) : "") + (temp ? (" with ( " + ChatColor.YELLOW +
                                    ammoAmount + ChatColor.GREEN + " ) arrows.") : ""))));
                        }
                    }
                    return true;
                }
                if (!console && command.equals("skaddfriend")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (args.length != 1 && args.length != 2) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> <entityType> (playerName)")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add friend")));
                        } else {
                            if (cp.pc == null) {
                                if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                    if (cp.updateAll) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                        return true;
                                    }
                                    updateAllOwnedSkulls(player, command, args);
                                    return true;
                                }
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                                return true;
                            }
                            if (SkullTurret.ONLY_BOW) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                            } else if (cp.pc.getIntelligence() == SkullIntelligence.CRAZED || cp.pc.getIntelligence() == SkullIntelligence.DEVIOUS) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullTargetSetError")));
                            } else if (permissionsOK(sender, command, args)) {
                                if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                    addMobFriend(cp.pc, args, sender, false);
                                } else if (!args[0].toLowerCase().contains("player") && isValidMob(args.length, args[0])) {
                                    addMobFriend(cp.pc, args, sender, false);
                                } else if (!cp.UUIDLookup) {
                                    this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                        public void run() {
                                            CommandHandler.this.addPlayerFriend(cp.pc, args, sender, false, null);
                                            CommandHandler.this.ds.saveDatabase(false);
                                        }
                                    });
                                } else {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skremfriend")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (args.length != 1 && args.length != 2) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> <entityType> (playerName)")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem friend")));
                        } else {
                            if (cp.pc == null) {
                                if (this.plugin.hasPermission(player, "skullturret.multiskullupdate") && (args.length == 1 || args.length == 2)) {
                                    if (cp.updateAll) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                        return true;
                                    }
                                    updateAllOwnedSkulls(player, command, args);
                                    return true;
                                }
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                                return true;
                            }
                            if (SkullTurret.ONLY_BOW) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                            } else if (cp.pc.getIntelligence() == SkullIntelligence.CRAZED || cp.pc.getIntelligence() == SkullIntelligence.DEVIOUS) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullTargetSetError")));
                            } else if (permissionsOK(sender, command, args)) {
                                if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                    remMobFriend(cp.pc, args, sender, false, false);
                                } else if (!args[0].toLowerCase().contains("player") && isValidMob(args.length, args[0])) {
                                    remMobFriend(cp.pc, args, sender, false, false);
                                } else if (!cp.UUIDLookup) {
                                    this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                        public void run() {
                                            CommandHandler.this.remPlayerFriend(cp.pc, args, sender, false, null);
                                            CommandHandler.this.ds.saveDatabase(false);
                                        }
                                    });
                                } else {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skaddenemy")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (args.length != 1 && args.length != 2) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> <entityType> (playerName)")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add enemy")));
                        } else {
                            if (cp.pc == null) {
                                if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                    if (cp.updateAll) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                        return true;
                                    }
                                    updateAllOwnedSkulls(player, command, args);
                                    return true;
                                }
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                                return true;
                            }
                            if (SkullTurret.ONLY_BOW) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                            } else if (cp.pc.getIntelligence() == SkullIntelligence.CRAZED || cp.pc.getIntelligence() == SkullIntelligence.DEVIOUS) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullTargetSetError")));
                            } else if (permissionsOK(sender, command, args)) {
                                if (!args[0].toLowerCase().contains("player") && isValidMob(args.length, args[0])) {
                                    addMobEnemy(cp.pc, args, sender, false);
                                } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                    addMobEnemy(cp.pc, args, sender, false);
                                } else if (!cp.UUIDLookup) {
                                    this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                        public void run() {
                                            CommandHandler.this.addPlayerEnemy(cp.pc, args, sender, false, null);
                                            CommandHandler.this.ds.saveDatabase(false);
                                        }
                                    });
                                } else {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skremenemy")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (args.length != 1 && args.length != 2) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> <entityType> (playerName)")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem enemy")));
                        } else {
                            if (cp.pc == null) {
                                if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                    if (cp.updateAll) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                        return true;
                                    }
                                    updateAllOwnedSkulls(player, command, args);
                                    return true;
                                }
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                                return true;
                            }
                            if (SkullTurret.ONLY_BOW) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                            } else if (cp.pc.getIntelligence() == SkullIntelligence.CRAZED || cp.pc.getIntelligence() == SkullIntelligence.DEVIOUS) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullTargetSetError")));
                            } else if (permissionsOK(sender, command, args)) {
                                if (!args[0].toLowerCase().contains("player") && isValidMob(args.length, args[0])) {
                                    remMobEnemy(cp.pc, args, sender, false, false);
                                } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                    remMobEnemy(cp.pc, args, sender, false, false);
                                } else if (!cp.UUIDLookup) {
                                    this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                        public void run() {
                                            CommandHandler.this.remPlayerEnemy(cp.pc, args, sender, false, null);
                                            CommandHandler.this.ds.saveDatabase(false);
                                        }
                                    });
                                } else {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skedit")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        cp.command = "skedit";
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("clickToEdit")));
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skdone")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit") || this.plugin.hasPermission(player, "skullturret.default")) {
                        if (!cp.command.isEmpty()) {
                            if (cp.command.equalsIgnoreCase("default") || cp.command.equalsIgnoreCase("resetDefault")) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("finishedDefaultMod")));
                            } else {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("finishedEdit")));
                            }
                            cp.clearPlayer();
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skpatrol")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (cp.pc == null) {
                            if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                if (cp.updateAll) {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                    return true;
                                }
                                updateAllOwnedSkulls(player, command, args);
                                return true;
                            }
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                            return true;
                        }
                        cp.pc.setPatrol(!cp.pc.doPatrol());
                        if (cp.pc.doPatrol()) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("willPatrol")));
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wontPatrol")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skrotate")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (cp.command.equals("skroate")) {
                            cp.clearPlayer();
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("rotateCancel")));
                        } else {
                            cp.command = "skrotate";
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("clickRotate")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skredstone")) {
                    if (this.plugin.hasPermission(player, "skullturret.edit")) {
                        if (cp.pc == null) {
                            if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                if (cp.updateAll) {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                    return true;
                                }
                                updateAllOwnedSkulls(player, command, args);
                                return true;
                            }
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                            return true;
                        }
                        cp.pc.setRedstone(!cp.pc.isRedstone());
                        if (cp.pc.isRedstone()) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("willRedstone")));
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wontRedstone")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skammo")) {
                    if (this.plugin.hasPermission(player, "skullturret.changeammo")) {
                        if (args.length != 1) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull ammo <ammoType>")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? ammo")));
                        } else {
                            if (cp.pc == null) {
                                if (this.plugin.hasPermission(player, "skullturret.multiskullupdate")) {
                                    if (cp.updateAll) {
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                                        return true;
                                    }
                                    updateAllOwnedSkulls(player, command, args);
                                    return true;
                                }
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullUpdatePermission")));
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("selectSkullFirst")));
                                return true;
                            }
                            if (cp.pc.getIntelligence() != SkullIntelligence.WIZARD) {
                                if (cp.pc instanceof MobileSkull) {
                                    sender.sendMessage(Utils.parseText(Utils.getLocalization("tempSkullNoAmmoChange")));
                                    return true;
                                }
                                EntityType ammo = cp.pc.getAmmoTypeFromString(args[0]);
                                if (ammo != null) {
                                    cp.pc.setAmmoType(ammo);
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("ammoChanged"), args[0].toUpperCase())));
                                } else {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("invalidAmmo"), "arrow, firearrow, firecharge, snowball")));
                                }
                            } else {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullNoAmmoChange"), cp.pc.getIntelligence().getNormalName())));
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (command.equals("skaddplayer") && SkullTurret.PER_PLAYER_SETTINGS) {
                    if (!console && !this.plugin.hasPermission(player, "skullturret.addplayer")) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                        return true;
                    }
                    if (args.length == 1) {
                        final String playerName = args[0];
                        if (!cp.UUIDLookup) {
                            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                public void run() {
                                    cp.UUIDLookup = true;
                                    boolean fail = false;
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), new Object[]{this.val$playerName})));
                                    try {
                                        UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                                        cp.UUIDLookup = false;
                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), new Object[]{this.val$playerName, uuid.toString()})));
                                        PerPlayerSettings oldPPS = CommandHandler.this.plugin.perPlayerSettings.get(uuid);
                                        boolean contained = (oldPPS != null);
                                        if (!contained) {
                                            PerPlayerSettings perPlayerSettings = new PerPlayerSettings(uuid);
                                            perPlayerSettings.setLastKnownPlayerName(playerName);
                                            CommandHandler.this.plugin.perPlayerSettings.put(uuid, perPlayerSettings);
                                            CommandHandler.this.ds.savePerPlayerSettings();
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAdded"), new Object[]{this.val$playerName})));
                                        } else {
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAlreadyInList"), new Object[]{this.val$playerName})));
                                        }
                                        CommandHandler.this.ds.savePerPlayerSettings();
                                        CommandHandler.this.initOwnedSkulls(uuid);
                                    } catch (Exception e) {
                                        fail = true;
                                    }
                                    if (fail)
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                                    cp.UUIDLookup = false;
                                }
                            });
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidLookupWait")));
                        }
                    } else if (args.length == 3) {
                        try {
                            final String playerName = args[0];
                            if (!cp.UUIDLookup) {
                                this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                    public void run() {
                                        Player p = Utils.getPlayer(playerName);
                                        cp.UUIDLookup = true;
                                        boolean fail = false;
                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), new Object[]{this.val$playerName})));
                                        try {
                                            UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                                            cp.UUIDLookup = false;
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), new Object[]{this.val$playerName, uuid.toString()})));
                                            PerPlayerGroups ppg = CommandHandler.this.plugin.getPlayerGroup(uuid);
                                            if (ppg == null || (p != null && p.isOp())) {
                                                int maxTurret = Integer.parseInt(args[1]);
                                                int maxRange = Integer.parseInt(args[2]);
                                                if (maxRange % 3 == 0) {
                                                    PerPlayerSettings perPlayerSettings = new PerPlayerSettings(uuid, maxTurret, maxRange);
                                                    PerPlayerSettings oldPPS = CommandHandler.this.plugin.perPlayerSettings.get(uuid);
                                                    boolean contained = (oldPPS != null);
                                                    if (contained && (oldPPS.isMasterDefaults() || oldPPS.isWizardDefaults()))
                                                        perPlayerSettings.reloadDefaults(oldPPS);
                                                    perPlayerSettings.setLastKnownPlayerName(playerName);
                                                    CommandHandler.this.plugin.perPlayerSettings.put(uuid, perPlayerSettings);
                                                    CommandHandler.this.ds.savePerPlayerSettings();
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perPlayerInfo"), new Object[]{this.val$playerName, Integer.valueOf(maxTurret), Integer.valueOf(maxRange)})));
                                                    if (!contained) {
                                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAdded"), new Object[]{this.val$playerName})));
                                                    } else {
                                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerUpdatedInList"), new Object[]{this.val$playerName})));
                                                    }
                                                    CommandHandler.this.initOwnedSkulls(uuid);
                                                } else {
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("badRangeValue"), new Object[]{this.val$playerName})));
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("notMulThree"), Integer.valueOf(maxRange))));
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerNotAdded"), new Object[]{this.val$playerName})));
                                                }
                                                CommandHandler.this.ds.savePerPlayerSettings();
                                            } else {
                                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAlreadyInGroup"), new Object[]{this.val$playerName, ppg.getGroupName()})));
                                                sender.sendMessage(Utils.parseText(Utils.getLocalization("playerNotAdded")));
                                            }
                                        } catch (Exception e) {
                                            fail = true;
                                        }
                                        if (fail)
                                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                                        cp.UUIDLookup = false;
                                    }
                                });
                            } else {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                            }
                        } catch (Exception e) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurret> <maxRange>")));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add player")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurret> <maxRange>")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add player")));
                    }
                    return true;
                }
                if (command.equals("skremplayer") && SkullTurret.PER_PLAYER_SETTINGS) {
                    if (!console && !this.plugin.hasPermission(player, "skullturret.remplayer")) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                        return true;
                    }
                    if (args.length == 1) {
                        final String playerName = args[0];
                        if (!cp.UUIDLookup) {
                            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                                public void run() {
                                    cp.UUIDLookup = true;
                                    boolean fail = false;
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), new Object[]{this.val$playerName})));
                                    try {
                                        UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                                        cp.UUIDLookup = false;
                                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), new Object[]{this.val$playerName, uuid.toString()})));
                                        boolean contained = CommandHandler.this.plugin.perPlayerSettings.containsKey(uuid);
                                        if (!contained) {
                                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerNotFound"), new Object[]{this.val$playerName})));
                                        } else {
                                            PerPlayerSettings pps = CommandHandler.this.plugin.perPlayerSettings.get(uuid);
                                            if (pps != null) {
                                                if (pps.isMasterDefaults() || pps.isWizardDefaults()) {
                                                    pps.setPps(false);
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerRemovedDefaults"), new Object[]{this.val$playerName})));
                                                } else {
                                                    CommandHandler.this.plugin.perPlayerSettings.remove(uuid);
                                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerRemoved"), new Object[]{this.val$playerName})));
                                                }
                                                CommandHandler.this.initOwnedSkulls(uuid);
                                                CommandHandler.this.ds.savePerPlayerSettings();
                                            }
                                        }
                                    } catch (Exception e) {
                                        fail = true;
                                    }
                                    if (fail)
                                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                                    cp.UUIDLookup = false;
                                }
                            });
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("updateInProgress")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <player> <playerName>")));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem player")));
                    }
                    return true;
                }
                if (command.equals("sklistallplayer") && SkullTurret.PER_PLAYER_SETTINGS) {
                    if (!console && !this.plugin.hasPermission(player, "skullturret.listallplayer")) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                        return true;
                    }
                    if (this.plugin.perPlayerSettings.size() == 0) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPlayerSettings")));
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("playerListHeader")));
                        for (PerPlayerSettings p : this.plugin.perPlayerSettings.values()) {
                            final String playerName = p.getLastKnownPlayerName();
                            if (!p.isPps()) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerNotSet"), playerName)));
                                continue;
                            }
                            int maxRange = p.getMaxRange();
                            int maxTurret = p.getMaxTurrets();
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perPlayerInfo"), playerName, Integer.valueOf(maxTurret), Integer.valueOf(maxRange))));
                        }
                    }
                    return true;
                }
                if (!console && command.equals("sklistplayer") && SkullTurret.PER_PLAYER_SETTINGS) {
                    if (this.plugin.hasPermission(player, "skullturret.listplayer")) {
                        if (this.plugin.perPlayerSettings.size() == 0 || !this.plugin.perPlayerSettings.containsKey(player.getUniqueId())) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("notInPlayerList")));
                        } else {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("playerListHeader")));
                            PerPlayerSettings pps = this.plugin.perPlayerSettings.get(player.getUniqueId());
                            final String playerName = pps.getLastKnownPlayerName();
                            String currentPlayerName = player.getName();
                            if (!playerName.equals(currentPlayerName)) {
                                pps.setLastKnownPlayerName(currentPlayerName);
                                playerName = currentPlayerName;
                            }
                            if (pps.isPps()) {
                                int maxRange = pps.getMaxRange();
                                int maxTurret = pps.getMaxTurrets();
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perPlayerInfo"), playerName, Integer.valueOf(maxTurret), Integer.valueOf(maxRange))));
                            } else {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerNotSet"), playerName)));
                            }
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (!console && command.equals("skreload")) {
                    if (this.plugin.hasPermission(player, "skullturret.admin") || this.plugin.hasPermission(player, "skullturret.reload")) {
                        if (cp.command.equals("RELOAD") && (args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("no")))) {
                            cp.clearPlayer();
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadCancel"), SkullTurret.pluginName)));
                        } else if (cp.command.equals("RELOAD") && args.length > 0 && args[0].equalsIgnoreCase("yes")) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloading"), SkullTurret.pluginName)));
                            SkullTurret.RELOAD = true;
                            this.ds.reInit();
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("finishedReloading"), SkullTurret.pluginName)));
                            cp.clearPlayer();
                        } else {
                            cp.command = "RELOAD";
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadCaution"), SkullTurret.pluginName)));
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadConfirm"), SkullTurret.pluginName, "/skull reload yes", "/skull reload no")));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
                    }
                    return true;
                }
                if (console && command.equals("skreload")) {
                    if (SkullTurret.RELOAD_QUESTION && (args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("no")))) {
                        SkullTurret.RELOAD_QUESTION = false;
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadCancel"), SkullTurret.pluginName)));
                    } else if (SkullTurret.RELOAD_QUESTION && args.length > 0 && args[0].equalsIgnoreCase("yes")) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloading"), SkullTurret.pluginName)));
                        SkullTurret.RELOAD = true;
                        this.ds.reInit();
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("finishedReloading"), SkullTurret.pluginName)));
                        SkullTurret.RELOAD_QUESTION = false;
                    } else {
                        SkullTurret.RELOAD_QUESTION = true;
                        sender.sendMessage(ChatColor.YELLOW + SkullTurret.pluginName + ": -----");
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadCaution"), SkullTurret.pluginName)));
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("reloadConfirm"), SkullTurret.pluginName, "/skull reload yes", "/skull reload no")));
                    }
                    return true;
                }
            }
        }
        if (console) {
            simpleInConsoleHelp(sender);
        } else {
            simpleInGameHelp(sender);
        }
        return true;
    }

    private void doTransaction(Player player, int amount, String name) {
        if (amount > 64) {
            player.sendMessage(Utils.parseText(Utils.getLocalization("tooManyItems")));
            return;
        }
        String type = "";
        Economy eco = this.plugin.econ;
        double balance = eco.getBalance(player);
        double price = 0.0D;
        ItemStack is = this.plugin.recipes.getCrazedSkull(amount);
        if (name.equalsIgnoreCase("bow") || name.equalsIgnoreCase("b")) {
            type = "Skull Bow";
            price = SkullTurret.ECON_BOW_COST;
            is = this.plugin.recipes.getSkullBow(amount);
        } else if (name.equalsIgnoreCase("crazed") || name.equalsIgnoreCase("c")) {
            type = "Crazed Skull";
            price = SkullTurret.ECON_CRAZED_COST;
            is = this.plugin.recipes.getCrazedSkull(amount);
        } else if (name.equalsIgnoreCase("devious") || name.equalsIgnoreCase("d")) {
            type = "Devious Skull";
            price = SkullTurret.ECON_DEVIOUS_COST;
            is = this.plugin.recipes.getDeviousSkull(amount);
        } else if (name.equalsIgnoreCase("master") || name.equalsIgnoreCase("m")) {
            type = "Master Skull";
            price = SkullTurret.ECON_MASTER_COST;
            is = this.plugin.recipes.getMasterSkull(amount);
        } else if (name.equalsIgnoreCase("wizard") || name.equalsIgnoreCase("w")) {
            type = "Wizard Skull";
            price = SkullTurret.ECON_WIZARD_COST;
            is = this.plugin.recipes.getWizardSkull(amount);
        }
        double cost = amount * price;
        if (balance <= 0.0D && cost > 0.0D) {
            player.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughFunds")));
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("balanceInfo"), String.format("%s", this.plugin.econ.format(this.plugin.econ.getBalance(player))))));
            return;
        }
        if (balance < cost) {
            int trueAmount = (int) Math.floor(balance / price);
            player.sendMessage(Utils.parseText(Utils.getLocalization("notEnoughFunds")));
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("askedFor"), Integer.valueOf(amount), type, String.format("%s", this.plugin.econ.format(cost)))));
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("balanceInfo"), String.format("%s", this.plugin.econ.format(this.plugin.econ.getBalance(player))))));
            if (trueAmount > 0) {
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("canAfford"), trueAmount + " " + type)));
            } else {
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("cantAfford"), type)));
            }
            return;
        }
        HashMap<Integer, ItemStack> couldNotFit = player.getInventory().addItem(is);
        int notFitAmount = 0;
        for (Map.Entry<Integer, ItemStack> items : couldNotFit.entrySet()) {
            ItemStack item = items.getValue();
            notFitAmount += item.getAmount();
        }
        if (notFitAmount > 0)
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("inventorySpaceErr"), notFitAmount + " " + type)));
        amount -= notFitAmount;
        double trueCost = amount * price;
        eco.withdrawPlayer(player, trueCost);
        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("purchased"), amount + " " + type, String.format("%s", this.plugin.econ.format(trueCost)))));
        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("balanceInfo"), String.format("%s", this.plugin.econ.format(this.plugin.econ.getBalance(player))))));
    }

    private boolean isNumber(String toTest) {
        try {
            Integer.parseInt(toTest);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void initOwnedSkulls(UUID uuid) {
        for (PlacedSkull skull : this.plugin.skullMap.values()) {
            if (skull.getSkullCreator().equals(uuid))
                skull.reInitSkull();
        }
    }

    private void updateAllOwnedSkulls(final CommandSender sender, final String command, final String[] args) {
        if (!(sender instanceof Player))
            return;
        final Player player = (Player) sender;
        final UUID senderUUid = player.getUniqueId();
        if (this.plugin.playersSkullNumber.get(senderUUid) == null || this.plugin.playersSkullNumber.get(senderUUid).getActiveSkulls() == 0) {
            sender.sendMessage(Utils.parseText(Utils.getLocalization("noOwnedSkulls")));
            return;
        }
        if (permissionsOK(sender, command, args))
            this.plugin.scheduler.runTaskAsynchronously(this.plugin, new Runnable() {
                public void run() {
                    boolean failed = false;
                    Map<Location, PlacedSkull> skulls = CommandHandler.this.plugin.skullMap;
                    int count = 0;
                    UUID playerUUid = null;
                    CustomPlayer cp = CustomPlayer.getSettings(player);
                    cp.updateAll = true;
                    boolean displayedInfo = false;
                    for (PlacedSkull ps : skulls.values()) {
                        if (!ps.getSkullCreator().equals(senderUUid) || ps instanceof MobileSkull)
                            continue;
                        if (command.equals("skskin") && ps.getIntelligence().canSkinChange()) {
                            boolean successful = false;
                            if (args.length == 0) {
                                successful = ps.setSkin("");
                                if (!successful) {
                                    failed = true;
                                    break;
                                }
                            } else {
                                successful = ps.setSkin(args[0]);
                                if (!successful) {
                                    failed = true;
                                    break;
                                }
                            }
                        } else if (command.equals("skaddfriend")) {
                            if (ps.getIntelligence() == SkullIntelligence.CRAZED || ps.getIntelligence() == SkullIntelligence.DEVIOUS)
                                continue;
                            if (!args[0].toLowerCase().contains("player") && CommandHandler.this.isValidMob(args.length, args[0])) {
                                failed = CommandHandler.this.addMobFriend(ps, args, sender, true);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriends"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                failed = CommandHandler.this.addMobFriend(ps, args, sender, true);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriends"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else {
                                playerUUid = CommandHandler.this.addPlayerFriend(ps, args, sender, true, playerUUid);
                            }
                            if (playerUUid == null || failed)
                                failed = true;
                        } else if (command.equals("skremfriend")) {
                            if (ps.getIntelligence() == SkullIntelligence.CRAZED || ps.getIntelligence() == SkullIntelligence.DEVIOUS)
                                continue;
                            if (!args[0].toLowerCase().contains("player") && CommandHandler.this.isValidMob(args.length, args[0])) {
                                if (CommandHandler.this.plugin.customNames.containsKey(args[0].toUpperCase())) {
                                    Map<EntityType, EntityType> types = CommandHandler.this.customTypes(args[0]);
                                    if (types != null) {
                                        String[] typeString = {""};
                                        for (EntityType t : types.values()) {
                                            typeString[0] = t.toString();
                                            CommandHandler.this.remMobFriend(ps, typeString, sender, true, false);
                                        }
                                        failed = false;
                                    } else {
                                        failed = true;
                                    }
                                } else {
                                    failed = CommandHandler.this.remMobFriend(ps, args, sender, true, false);
                                }
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriends"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                failed = CommandHandler.this.remMobFriend(ps, args, sender, true, false);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriends"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else {
                                playerUUid = CommandHandler.this.remPlayerFriend(ps, args, sender, true, playerUUid);
                            }
                            if (playerUUid == null || failed)
                                failed = true;
                        } else if (command.equals("skaddenemy")) {
                            if (ps.getIntelligence() == SkullIntelligence.CRAZED || ps.getIntelligence() == SkullIntelligence.DEVIOUS)
                                continue;
                            if (!args[0].toLowerCase().contains("player") && CommandHandler.this.isValidMob(args.length, args[0])) {
                                failed = CommandHandler.this.addMobEnemy(ps, args, sender, true);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemies"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                failed = CommandHandler.this.addMobEnemy(ps, args, sender, true);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemies"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else {
                                playerUUid = CommandHandler.this.addPlayerEnemy(ps, args, sender, true, playerUUid);
                            }
                            if (playerUUid == null || failed)
                                failed = true;
                        } else if (command.equals("skremenemy")) {
                            if (ps.getIntelligence() == SkullIntelligence.CRAZED || ps.getIntelligence() == SkullIntelligence.DEVIOUS)
                                continue;
                            if (!args[0].toLowerCase().contains("player") && CommandHandler.this.isValidMob(args.length, args[0])) {
                                if (CommandHandler.this.plugin.customNames.containsKey(args[0].toUpperCase().trim())) {
                                    Map<EntityType, EntityType> types = CommandHandler.this.customTypes(args[0]);
                                    if (types != null) {
                                        String[] typeString = {""};
                                        for (EntityType t : types.values()) {
                                            typeString[0] = t.toString();
                                            CommandHandler.this.remMobEnemy(ps, typeString, sender, true, false);
                                        }
                                        failed = false;
                                    } else {
                                        failed = true;
                                    }
                                } else {
                                    failed = CommandHandler.this.remMobEnemy(ps, args, sender, true, false);
                                }
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemies"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else if (args[0].toLowerCase().contains("player") && args.length == 1) {
                                failed = CommandHandler.this.remMobEnemy(ps, args, sender, true, false);
                                if (!failed && !displayedInfo) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemies"), new Object[]{this.val$args[0]})));
                                    displayedInfo = true;
                                }
                            } else {
                                playerUUid = CommandHandler.this.remPlayerEnemy(ps, args, sender, true, playerUUid);
                            }
                            if (playerUUid == null || failed)
                                failed = true;
                        } else if (command.equals("skpatrol")) {
                            ps.setPatrol(!ps.doPatrol());
                        } else if (command.equals("skredstone")) {
                            ps.setRedstone(!ps.isRedstone());
                        } else if (command.equals("skammo") &&
                                ps.getIntelligence() != SkullIntelligence.WIZARD) {
                            EntityType ammo = ps.getAmmoTypeFromString(args[0]);
                            if (ammo != null) {
                                ps.setAmmoType(ammo);
                            } else {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("invalidAmmo"), "arrow, firearrow, firecharge, snowball")));
                                failed = true;
                                break;
                            }
                        }
                        count++;
                    }
                    CommandHandler.this.sendFinishedMessage(sender, command, args, count, failed);
                    cp.updateAll = false;
                    CommandHandler.this.ds.saveDatabase(false);
                }
            });
    }

    private boolean permissionsOK(CommandSender sender, String command, String[] args) {
        if (command.equals("skskin") && this.plugin.hasPermission((Player) sender, "skullturret.skin"))
            return true;
        if (command.equals("skaddfriend") || command.equals("skaddenemy")) {
            if (SkullTurret.ONLY_BOW) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                return false;
            }
            if (!this.plugin.hasPermission((Player) sender, "skullturret.target." + args[0].toLowerCase())) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("noAddTargetPermission")));
                return false;
            }
            return true;
        }
        if (command.equals("skremfriend") || command.equals("skremenemy")) {
            if (SkullTurret.ONLY_BOW) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("skullBowTargetOnly")));
                return false;
            }
            if (!this.plugin.hasPermission((Player) sender, "skullturret.target." + args[0].toLowerCase())) {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("noRemTargetPermission")));
                return false;
            }
            return true;
        }
        if (command.equals("skpatrol") || command.equals("skredstone")) {
            if (this.plugin.hasPermission((Player) sender, "skullturret.edit"))
                return true;
            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
            return false;
        }
        if (command.equals("skammo")) {
            if (this.plugin.hasPermission((Player) sender, "skullturret.changeammo"))
                return true;
            sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
            return false;
        }
        sender.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionError")));
        return false;
    }

    private void sendFinishedMessage(CommandSender sender, String command, String[] args, int count, boolean failed) {
        if (command.equals("skskin")) {
            if (args.length == 0) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinsReset"), Integer.valueOf(count))));
            } else if (!failed) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinsSetTo"), Integer.valueOf(count), args[0])));
            } else {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skinSetErr"), args[0])));
            }
        } else if ((command.equals("skaddfriend") || command.equals("skaddenemy") || command.equals("skremfriend") || command.equals("skremenemy")) && !failed) {
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("targetsUpdated"), Integer.valueOf(count))));
        } else if (command.equals("skpatrol")) {
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("patrolInvert"), Integer.valueOf(count))));
        } else if (command.equals("skredstone")) {
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("redstoneInvert"), Integer.valueOf(count))));
        } else if (command.equals("skammo") && !failed) {
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("multiAmmoChanged"), Integer.valueOf(count), args[0].toUpperCase())));
        }
    }

    private boolean simpleInGameHelp(CommandSender sender) {
        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alias"), "/sk")));
        sender.sendMessage(ChatColor.GOLD + "/skull [?|help] (command)");
        sender.sendMessage(ChatColor.GOLD + "/skull [edit]");
        sender.sendMessage(ChatColor.GOLD + "/skull [skin] (PlayerName)");
        sender.sendMessage(ChatColor.GOLD + "/skull [add | rem] <EntityType>(PlayerName)");
        sender.sendMessage(ChatColor.GOLD + "/skull [add] [player] <PlayerName> <maxTurrets> <maxRange>");
        sender.sendMessage(ChatColor.GOLD + "/skull [rem] [player] <PlayerName>");
        sender.sendMessage(ChatColor.GOLD + "/skull [give] <item> (playerName) (amount)");
        sender.sendMessage(ChatColor.GOLD + "/skull [buy] <item> (amount)");
        sender.sendMessage(ChatColor.GOLD + "/skull [costs]");
        sender.sendMessage(ChatColor.GOLD + "/skull [ammo] <ammoType>");
        sender.sendMessage(ChatColor.GOLD + "/skull [destruct] <PlayerName>");
        sender.sendMessage(ChatColor.GOLD + "/skull [patrol | redstone | rotate | list | listall | reload | done]");
        sender.sendMessage(ChatColor.GOLD + "/skull [default] <reset (w/m)>");
        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("useB"), "/skull ? (command)")));
        sender.sendMessage(Utils.parseText(Utils.getLocalization("additionalHelp")));
        sender.sendMessage(ChatColor.YELLOW + "http://dev.bukkit.org/bukkit-plugins/skull-turret/pages/command-info/");
        return true;
    }

    private boolean simpleInConsoleHelp(CommandSender sender) {
        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alias"), "/sk")));
        sender.sendMessage(ChatColor.GOLD + "/skull [?|help] (command)");
        sender.sendMessage(ChatColor.GOLD + "/skull [add] [player] <PlayerName> <maxTurrets> <maxRange>");
        sender.sendMessage(ChatColor.GOLD + "/skull [rem] [player] <PlayerName>");
        sender.sendMessage(ChatColor.GOLD + "/skull [give] <item> (playerName) (amount)");
        sender.sendMessage(ChatColor.GOLD + "/skull [destruct] <PlayerName>");
        sender.sendMessage(ChatColor.GOLD + "/skull [listall | reload]");
        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("useB"), "/skull ? (command)")));
        sender.sendMessage(Utils.parseText(Utils.getLocalization("additionalHelp")));
        sender.sendMessage(ChatColor.YELLOW + "http://dev.bukkit.org/bukkit-plugins/skull-turret/pages/command-info/");
        return true;
    }

    private boolean inGameHelp(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
            simpleInGameHelp(sender);
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("add") && this.plugin.hasPermission(player, "skullturret.addplayer")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend|enemy> (EntityType) (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurrets> <maxRange>")));
            } else if (args[1].equalsIgnoreCase("rem") && this.plugin.hasPermission(player, "skullturret.remplayer")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend|enemy> (EntityType) (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <player> <playerName>")));
            } else if (args[1].equalsIgnoreCase("done") && this.plugin.hasPermission(player, "skullturret.edit")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull done")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpCancelEdit")));
            } else if (args[1].equalsIgnoreCase("buy") && (this.plugin.hasPermission(player, "skullturret.buy.crazed") || this.plugin.hasPermission(player, "skullturret.buy.devious") || this.plugin.hasPermission(player, "skullturret.buy.master") || this.plugin.hasPermission(player, "skullturret.buy.wizard") || this.plugin.hasPermission(player, "skullturret.buy.bow"))) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull buy <itemToBuy><amountToBuy")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("helpValidItems"), "crazed, devious, master, wizard, bow (c,d,m,w,b)")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpPurchase")));
            } else if (args[1].equalsIgnoreCase("costs") && (this.plugin.hasPermission(player, "skullturret.buy.crazed") || this.plugin.hasPermission(player, "skullturret.buy.devious") || this.plugin.hasPermission(player, "skullturret.buy.master") || this.plugin.hasPermission(player, "skullturret.buy.wizard") || this.plugin.hasPermission(player, "skullturret.buy.bow"))) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull costs")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpCosts")));
            } else if (args[1].equalsIgnoreCase("rotate") && this.plugin.hasPermission(player, "skullturret.edit")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rotate")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRotateA")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRotateB")));
            } else if (args[1].equalsIgnoreCase("redstone") && this.plugin.hasPermission(player, "skullturret.edit")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull redstone")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRedstone")));
            } else if (args[1].equalsIgnoreCase("destruct") && this.plugin.hasPermission(player, "skullturret.destruct")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull destruct <playerName>")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpDestruct")));
            } else if (args[1].equalsIgnoreCase("patrol") && this.plugin.hasPermission(player, "skullturret.edit")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull patrol")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpPatrol")));
            } else if (args[1].equalsIgnoreCase("ammo") && this.plugin.hasPermission(player, "skullturret.changeammo")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull ammo <ammoType>")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("helpValidAmmo"), "(arrow, firearrow, snowball, firecharge)")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAmmo")));
            } else if (args[1].equalsIgnoreCase("skin") && this.plugin.hasPermission(player, "skullturret.skin")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull skin (username)")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpSkinA")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpSkinB")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpSkinC")));
            } else if (args[1].equalsIgnoreCase("reload") && this.plugin.hasPermission(player, "skullturret.reload")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull reload (yes/no)")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpReload")));
            } else if (args[1].equalsIgnoreCase("give") && this.plugin.hasPermission(player, "skullturret.give")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> (playerName) (amount) (ammo)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("helpValidItems"), "(crazed(c), devious(d), master(m), wizard(w), bow(b), tempmaster(tm), tempdevious(td))")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpGive")));
            } else if (args[1].equalsIgnoreCase("list") && this.plugin.hasPermission(player, "skullturret.listplayer")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull list")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpPerUserA")));
            } else if (args[1].equalsIgnoreCase("listall") && this.plugin.hasPermission(player, "skullturret.listallplayer")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull listall")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpPerUserB")));
            } else if (args[1].equalsIgnoreCase("edit") && this.plugin.hasPermission(player, "skullturret.edit")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull edit")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpEdit")));
            } else if (args[1].equalsIgnoreCase("default") && this.plugin.hasPermission(player, "skullturret.default")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull default <reset (m/w)>")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpDefaultA")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpDefaultB")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpDefaultC")));
            } else {
                simpleInGameHelp(sender);
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("add")) {
                if (args[2].equalsIgnoreCase("friend") && this.plugin.hasPermission(player, "skullturret.edit")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> (EntityType) (playerName)")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddFriendA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddB")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddFriendB")));
                } else if (args[2].equalsIgnoreCase("enemy") && this.plugin.hasPermission(player, "skullturret.edit")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> (EntityType) (playerName)")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddEnemyA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddB")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddEnemyB")));
                } else if (args[2].equalsIgnoreCase("player") && this.plugin.hasPermission(player, "skullturret.addplayer")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurrets> <maxRange>")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerB")));
                } else {
                    simpleInGameHelp(sender);
                }
            } else if (args[1].equalsIgnoreCase("rem")) {
                if (args[2].equalsIgnoreCase("friend") && this.plugin.hasPermission(player, "skullturret.edit")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> (EntityType) (playerName)")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRemFriend")));
                } else if (args[2].equalsIgnoreCase("enemy") && this.plugin.hasPermission(player, "skullturret.edit")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> (EntityType) (playerName)")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRemEnemy")));
                } else if (args[2].equalsIgnoreCase("player") && this.plugin.hasPermission(player, "skullturret.remplayer")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <player> <playerName>")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRemPlayer")));
                } else {
                    simpleInGameHelp(sender);
                }
            } else {
                simpleInGameHelp(sender);
            }
        } else {
            simpleInGameHelp(sender);
        }
        return true;
    }

    private boolean inConsoleHelp(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
            simpleInConsoleHelp(sender);
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("add")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurrets> <maxRange>")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerA")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerB")));
            } else if (args[1].equalsIgnoreCase("rem")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <player> <playerName>")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRemPlayer")));
            } else if (args[1].equalsIgnoreCase("listall")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull listall")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpPerUserB")));
            } else if (args[1].equalsIgnoreCase("reload")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull reload (yes/no)")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpReload")));
            } else if (args[1].equalsIgnoreCase("give")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull give <item> <playerName> <amount> (ammo)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("helpValidItems"), "(crazed(c), devious(d), master(m), wizard(w), bow(b), tempmaster(tm), tempdevious(td))")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpGive")));
            } else if (args[1].equalsIgnoreCase("destruct")) {
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull destruct <playerName>")));
                sender.sendMessage(Utils.parseText(Utils.getLocalization("helpDestruct")));
            } else {
                simpleInConsoleHelp(sender);
            }
        } else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("add")) {
                if (args[2].equalsIgnoreCase("player")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <player> <playerName> <maxTurrets> <maxRange>")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerA")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpAddPlayerB")));
                } else {
                    simpleInConsoleHelp(sender);
                }
            } else if (args[1].equalsIgnoreCase("rem")) {
                if (args[2].equalsIgnoreCase("player")) {
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <player> <playerName>")));
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("helpRemPlayer")));
                } else {
                    simpleInConsoleHelp(sender);
                }
            } else {
                simpleInConsoleHelp(sender);
            }
        } else {
            simpleInConsoleHelp(sender);
        }
        return true;
    }

    private String getName(String enumName) {
        String stripDash = enumName.replace("_", " ");
        return stripDash.substring(0, 1) + stripDash.substring(1).toLowerCase();
    }

    private Map<EntityType, EntityType> customTypes(String typeName) {
        String tn = typeName.toUpperCase().trim();
        Map<EntityType, EntityType> types = new HashMap<EntityType, EntityType>();
        if (this.plugin.customNames.containsKey(tn))
            for (String s : this.plugin.customNames.get(tn)) {
                try {
                    EntityType ent = EntityType.valueOf(s.toUpperCase().trim());
                    types.put(ent, ent);
                } catch (Exception exception) {
                }
            }
        return types;
    }

    private boolean isValidMob(int length, String mobType) {
        if (this.plugin.customNames.containsKey(mobType.toUpperCase()))
            return true;
        try {
            EntityType type = EntityType.valueOf(mobType.toUpperCase());
            if (!type.isAlive())
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean addMobEnemy(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull) {
        if (args.length == 1)
            if (this.plugin.customNames.containsKey(args[0].toUpperCase().trim())) {
                Map<EntityType, EntityType> ents = customTypes(args[0]);
                if (ents != null) {
                    ps.enemies.putAll(customTypes(args[0]));
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemiesB"), args[0], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add enemy")));
                  return multiSkull;
                }
            } else {
                try {
                    EntityType type = EntityType.valueOf(args[0].toUpperCase());
                    if (!multiSkull && !permissionsOK(sender, "skaddenemy", args))
                        return true;
                    ps.enemies.put(type, type);
                    String typeName = getName(type.name());
                    if (typeName == null)
                        typeName = type.name();
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemiesB"), args[0], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } catch (Exception e) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add enemy")));
                    if (multiSkull)
                        return true;
                }
            }
        return false;
    }

    private UUID addPlayerEnemy(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, UUID playerUUid) {
        try {
            EntityType type = EntityType.valueOf(args[0].toUpperCase());
            String playerName = args[1];
            if (type == EntityType.PLAYER) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    CustomPlayer cp = CustomPlayer.getSettings(player);
                    if (!cp.UUIDLookup && playerUUid == null) {
                        cp.UUIDLookup = true;
                        boolean fail = false;
                        try {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), playerName)));
                            UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), playerName, uuid.toString())));
                            playerUUid = uuid;
                            if (multiSkull && ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAlreadyInEnemyList"), playerName)));
                                cp.UUIDLookup = false;
                                return null;
                            }
                            if (ps.getSkullCreator().equals(uuid) || (ps.playerFrenemies.containsKey(uuid) && ps.playerFrenemies.get(uuid).getFriendOrEnemy().equals("ENEMY"))) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("playerAlreadyInEnemyList"), playerName)));
                            } else if (!ps.playerFrenemies.containsKey(uuid)) {
                                ps.playerFrenemies.put(uuid, new PlayerNamesFoF(playerName, "ENEMY"));
                                String typeName = getName(type.name());
                                if (typeName == null)
                                    typeName = type.name();
                                if (!multiSkull)
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemiesC"), typeName, playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            } else if (!multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyMulti"), args[1], ps.playerFrenemies.get(args[1]), ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            }
                        } catch (Exception e) {
                            fail = true;
                        }
                        if (fail)
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                        cp.UUIDLookup = false;
                    } else if (playerUUid != null) {
                        if (ps.getSkullCreator().equals(playerUUid) && !multiSkull)
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("cantBeEnemy")));
                        if (ps.playerFrenemies.containsKey(playerUUid) && ps.playerFrenemies.get(playerUUid).getFriendOrEnemy().equals("ENEMY") && !multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyEnemy"), playerName)));
                        } else if (!ps.playerFrenemies.containsKey(playerUUid)) {
                            ps.playerFrenemies.put(playerUUid, new PlayerNamesFoF(playerName, "ENEMY"));
                            String typeName = getName(type.name());
                            if (typeName == null)
                                typeName = type.name();
                            if (!multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToEnemiesC"), typeName, playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                                this.ds.saveDatabase(false);
                            }
                        } else if (!multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyMulti"), args[1], ps.playerFrenemies.get(args[1]), ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidSearchInProgress")));
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("notConsoleCommand")));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> <entityType> (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add enemy")));
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <enemy> <entityType> (playerName)")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add enemy")));
            return null;
        }
        return playerUUid;
    }

    private boolean remMobEnemy(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, boolean recur) {
        if (args.length == 1) {
            String entityName = args[0].toUpperCase().trim();
            if (this.plugin.customNames.containsKey(entityName) && !recur) {
                Map<EntityType, EntityType> types = customTypes(entityName);
                if (types != null) {
                    String[] typeString = {""};
                    for (EntityType t : types.values()) {
                        typeString[0] = t.toString();
                        remMobEnemy(ps, typeString, sender, true, true);
                    }
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemiesB"), args[0], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem enemy")));
                    return true;
                }
            } else {
                try {
                    EntityType type = EntityType.valueOf(entityName);
                    if (ps.enemies.containsKey(type)) {
                        ps.enemies.remove(type);
                        String typeName = getName(type.name());
                        if (typeName == null)
                            typeName = type.name();
                        if (!multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemiesB"), typeName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            this.ds.saveDatabase(false);
                        }
                    } else if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("notInEnemyList")));
                    }
                } catch (Exception e) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem enemy")));
                    return true;
                }
            }
        }
        return false;
    }

    private UUID remPlayerFriend(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, UUID playerUUid) {
        try {
            EntityType type = EntityType.valueOf(args[0].toUpperCase());
            String playerName = args[1];
            if (type == EntityType.PLAYER) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    CustomPlayer cp = CustomPlayer.getSettings(player);
                    if (!cp.UUIDLookup && playerUUid == null) {
                        cp.UUIDLookup = true;
                        boolean fail = false;
                        try {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), playerName)));
                            UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                            playerUUid = uuid;
                            sender.sendMessage(playerName + " found with UUID: " + uuid.toString());
                            if (multiSkull && ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("cantRemFriendSelf")));
                                cp.UUIDLookup = false;
                                return null;
                            }
                            if (ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("cantRemFriendSelf")));
                            } else if (ps.playerFrenemies.containsKey(uuid) && ps.playerFrenemies.get(uuid).getFriendOrEnemy().equals("FRIEND")) {
                                if (!multiSkull)
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriendsC"), type.name(), playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                                ps.playerFrenemies.remove(uuid);
                            } else if (!ps.playerFrenemies.containsKey(uuid) &&
                                    !multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("notFriend"), args[1], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            }
                        } catch (Exception e) {
                            fail = true;
                        }
                        if (fail)
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                        cp.UUIDLookup = false;
                    } else if (playerUUid != null) {
                        if (ps.getSkullCreator().equals(playerUUid) && !multiSkull) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("cantRemFriendSelf")));
                        } else if (ps.playerFrenemies.containsKey(playerUUid) && ps.playerFrenemies.get(playerUUid).getFriendOrEnemy().equals("FRIEND")) {
                            if (!multiSkull)
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriendsC"), type.name(), playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            ps.playerFrenemies.remove(playerUUid);
                        } else if (!ps.playerFrenemies.containsKey(playerUUid) &&
                                !multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("notFriend"), args[1], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidSearchInProgress")));
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("notConsoleCommand")));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> <entityType> (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem friend")));
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> <entityType> (playerName)")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem friend")));
            return null;
        }
        return playerUUid;
    }

    private boolean addMobFriend(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull) {
        if (args.length == 1)
            if (this.plugin.customNames.containsKey(args[0].toUpperCase().trim())) {
                Map<EntityType, EntityType> ents = customTypes(args[0]);
                if (ents != null) {
                    ps.friends.putAll(customTypes(args[0]));
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriendsB"), args[0], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add friend")));
                  return multiSkull;
                }
            } else {
                try {
                    EntityType type = EntityType.valueOf(args[0].toUpperCase());
                    if (!multiSkull && !permissionsOK(sender, "skaddfriend", args))
                        return true;
                    ps.friends.put(type, type);
                    String typeName = getName(type.name());
                    if (typeName == null)
                        typeName = type.name();
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriendsB"), typeName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } catch (Exception e) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add friend")));
                    if (multiSkull)
                        return true;
                }
            }
        return false;
    }

    private UUID addPlayerFriend(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, UUID playerUUid) {
        try {
            EntityType type = EntityType.valueOf(args[0].toUpperCase());
            String playerName = args[1];
            if (type == EntityType.PLAYER) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    CustomPlayer cp = CustomPlayer.getSettings(player);
                    if (!cp.UUIDLookup && playerUUid == null) {
                        cp.UUIDLookup = true;
                        boolean fail = false;
                        try {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), playerName)));
                            UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), playerName, uuid.toString())));
                            playerUUid = uuid;
                            if (multiSkull && ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyFriend"), playerName)));
                                cp.UUIDLookup = false;
                                return null;
                            }
                            if (ps.getSkullCreator().equals(uuid) || (ps.playerFrenemies.containsKey(uuid) && ps.playerFrenemies.get(uuid).getFriendOrEnemy().equals("FRIEND"))) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyFriend"), playerName)));
                            } else if (!ps.playerFrenemies.containsKey(uuid)) {
                                ps.playerFrenemies.put(uuid, new PlayerNamesFoF(playerName, "FRIEND"));
                                String typeName = getName(type.name());
                                if (typeName == null)
                                    typeName = type.name();
                                if (!multiSkull) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriendsC"), typeName, playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                                    this.ds.saveDatabase(false);
                                }
                            } else if (!multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyMulti"), args[1], ps.playerFrenemies.get(args[1]), ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            }
                        } catch (Exception e) {
                            fail = true;
                        }
                        if (fail)
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                        cp.UUIDLookup = false;
                    } else if (playerUUid != null) {
                        if (ps.getSkullCreator().equals(playerUUid) || (ps.playerFrenemies.containsKey(playerUUid) && ps.playerFrenemies.get(playerUUid).getFriendOrEnemy().equals("FRIEND"))) {
                            if (!multiSkull)
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyFriend"), playerName)));
                        } else if (!ps.playerFrenemies.containsKey(playerUUid)) {
                            ps.playerFrenemies.put(playerUUid, new PlayerNamesFoF(playerName, "FRIEND"));
                            String typeName = getName(type.name());
                            if (typeName == null)
                                typeName = type.name();
                            if (!multiSkull)
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("addToFriendsC"), typeName, playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        } else if (!multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("alreadyMulti"), args[1], ps.playerFrenemies.get(args[1]), ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidSearchInProgress")));
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("notConsoleCommand")));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> <entityType> (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add friend")));
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull add <friend> <entityType> (playerName)")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? add friend")));
            return null;
        }
        return playerUUid;
    }

    private boolean remMobFriend(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, boolean recur) {
        if (args.length == 1) {
            String entityName = args[0].toUpperCase().trim();
            if (this.plugin.customNames.containsKey(entityName) && !recur) {
                Map<EntityType, EntityType> types = customTypes(entityName);
                if (types != null) {
                    String[] typeString = {""};
                    for (EntityType t : types.values()) {
                        typeString[0] = t.toString();
                        remMobFriend(ps, typeString, sender, true, true);
                    }
                    if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriendsB"), entityName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        this.ds.saveDatabase(false);
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem friend")));
                    return true;
                }
            } else {
                try {
                    EntityType type = EntityType.valueOf(entityName);
                    if (ps.friends.containsKey(type)) {
                        ps.friends.remove(type);
                        String typeName = getName(type.name());
                        if (typeName == null)
                            typeName = type.name();
                        if (!multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromFriendsB"), typeName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            this.ds.saveDatabase(false);
                        }
                    } else if (!multiSkull) {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("entityNotFriend")));
                    }
                } catch (Exception e) {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <friend> <entityType> (playerName)")));
                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem friend")));
                    return true;
                }
            }
        }
        return false;
    }

    private UUID remPlayerEnemy(PlacedSkull ps, String[] args, CommandSender sender, boolean multiSkull, UUID playerUUid) {
        try {
            EntityType type = EntityType.valueOf(args[0].toUpperCase());
            String playerName = args[1];
            if (type == EntityType.PLAYER) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    CustomPlayer cp = CustomPlayer.getSettings(player);
                    if (!cp.UUIDLookup && playerUUid == null) {
                        cp.UUIDLookup = true;
                        boolean fail = false;
                        try {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookup"), playerName)));
                            UUID uuid = FindUUID.getUUIDFromPlayerName(playerName);
                            playerUUid = uuid;
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("uuidLookupSuccess"), playerName, uuid.toString())));
                            if (multiSkull && ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("cantBeEnemy")));
                                cp.UUIDLookup = false;
                                return null;
                            }
                            if (ps.getSkullCreator().equals(uuid)) {
                                sender.sendMessage(Utils.parseText(Utils.getLocalization("cantBeEnemy")));
                            } else if (ps.playerFrenemies.containsKey(uuid) && ps.playerFrenemies.get(uuid).getFriendOrEnemy().equals("ENEMY")) {
                                if (!multiSkull) {
                                    sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemiesC"), type.name(), playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                                    this.ds.saveDatabase(false);
                                }
                                ps.playerFrenemies.remove(uuid);
                            } else if (!ps.playerFrenemies.containsKey(uuid) &&
                                    !multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("notEnemy"), args[1], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                            }
                        } catch (Exception e) {
                            fail = true;
                        }
                        if (fail)
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidNotFound")));
                        cp.UUIDLookup = false;
                    } else if (playerUUid != null) {
                        if (ps.getSkullCreator().equals(playerUUid) && !multiSkull) {
                            sender.sendMessage(Utils.parseText(Utils.getLocalization("cantBeEnemy")));
                        } else if (ps.playerFrenemies.containsKey(playerUUid) && ps.playerFrenemies.get(playerUUid).getFriendOrEnemy().equals("ENEMY")) {
                            if (!multiSkull) {
                                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("remFromEnemiesC"), type.name(), playerName, ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                                this.ds.saveDatabase(false);
                            }
                            ps.playerFrenemies.remove(playerUUid);
                        } else if (!ps.playerFrenemies.containsKey(playerUUid) &&
                                !multiSkull) {
                            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("notEnemy"), args[1], ps.getIntelligence().getNormalName(), ps.getStringLocation())));
                        }
                    } else {
                        sender.sendMessage(Utils.parseText(Utils.getLocalization("uuidSearchInProgress")));
                    }
                } else {
                    sender.sendMessage(Utils.parseText(Utils.getLocalization("notConsoleCommand")));
                }
            } else {
                sender.sendMessage(Utils.parseText(Utils.getLocalization("wrongCommandArgs")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> <entityType> (playerName)")));
                sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem enemy")));
                return null;
            }
        } catch (Exception e) {
            sender.sendMessage(Utils.parseText(Utils.getLocalization("invalidEntity")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("usage"), "/skull rem <enemy> <entityType> (playerName)")));
            sender.sendMessage(Utils.parseText(String.format(Utils.getLocalization("use"), "/skull ? rem enemy")));
            return null;
        }
        return playerUUid;
    }
}
