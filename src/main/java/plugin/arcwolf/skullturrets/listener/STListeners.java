package plugin.arcwolf.skullturrets.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.Potion;
import org.bukkit.util.Vector;
import plugin.arcwolf.skullturrets.SkullTurret;
import plugin.arcwolf.skullturrets.data.DataStore;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class STListeners implements Listener {
    private final SkullTurret plugin;

    public STListeners(final SkullTurret plugin) {
        this.plugin = plugin;
    }

    // ЧТО ЭТО?!
    @EventHandler
    public void onBlockPhysics(final BlockPhysicsEvent event) {
        final Material ph = Material.LEGACY_PISTON_EXTENSION;
        final Block block = event.getBlock();
        final Material blockMeta = block.getType();
        if (blockMeta == ph && this.isMobileTurret(block)) {
            event.setCancelled(true);
        }
    }

    private boolean isMobileTurret(final Block block) {
        final Block skullBlock = block.getRelative(BlockFace.UP);
        if (skullBlock.hasMetadata("SkullTurretPlace")) {
            return true;
        }
        final Location location = skullBlock.getLocation();
        return this.plugin.skullMap.containsKey(location);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(final EntityTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Entity ent = event.getEntity();
        if (ent instanceof LivingEntity && ent.getType() == EntityType.ENDERMAN) {
            final LivingEntity le = (LivingEntity) ent;
            if (!le.isDead()) {
                final List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretEnder");
                if (targetedMeta.size() > 0) {
                    final Object obj = targetedMeta.get(0).value();
                    if (obj instanceof Potion) {
                        final Potion potion = (Potion) obj;
                        le.damage(potion.getLevel() * 6.0);
                        le.getWorld().playEffect(event.getFrom(), Effect.POTION_BREAK, (Object) potion);
                        le.removeMetadata("SkullTurretEnder", this.plugin);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExposionPrime(final ExplosionPrimeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (SkullTurret.DEBUG == 10 && !SkullTurret.MOB_LOOT) {
            final Entity expEnt = event.getEntity();
            if (expEnt.getType() == EntityType.WITHER_SKULL && expEnt.hasMetadata("SkullTurretsSMART")) {
                final List<Entity> entities = expEnt.getNearbyEntities(3.0, 3.0, 3.0);
                for (final Entity noDrop : entities) {
                    if (noDrop.getType() != EntityType.PLAYER && !noDrop.hasMetadata("SkullTurretsNODROP")) {
                        noDrop.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getDamager() != null) {
            final Entity firedRound = event.getDamager();
            final Entity damagee = event.getEntity();
            if (damagee.getType() == EntityType.FIREBALL && SkullTurret.DEBUG == 99) {
                final List<MetadataValue> targetedMeta = firedRound.getMetadata("SkullTurretantiFireball");
                if (targetedMeta.size() > 0) {
                    final Fireball fb = (Fireball) damagee;
                    final Vector oldV = fb.getDirection();
                    final Vector newV = new Vector(-oldV.getX(), -oldV.getY(), -oldV.getZ());
                    fb.setDirection(newV);
                    System.out.println("Hit fireball");
                    return;
                }
            }
            if (damagee instanceof LivingEntity && !damagee.isDead()) {
                List<MetadataValue> targetedMeta = firedRound.getMetadata("SkullTurretsArrow");
                if (targetedMeta.size() > 0) {
                    event.setDamage(0.0);
                    final UUID playerUUID = UUID.fromString(targetedMeta.get(0).asString());
                    if (event.getEntity().getType() == EntityType.PLAYER) {
                        final Player player = (Player) damagee;
                        if (player.getUniqueId().equals(playerUUID)) {
                            return;
                        }
                        if (this.plugin.hasPermission(player, "skullturret.ignoreme")) {
                            return;
                        }
                    }
                    damagee.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(playerUUID, System.currentTimeMillis())));
                    final Player shooter = Utils.getPlayerFromUUID(playerUUID);
                    if (SkullTurret.SKULLSFX && shooter != null && shooter.isOnline()) {
                        shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.1f);
                    }
                }
                targetedMeta = firedRound.getMetadata("SkullTurretWizard");
                if (targetedMeta.size() > 0) {
                    final Object obj = targetedMeta.get(0).value();
                    if (obj instanceof Potion) {
                        final Potion potion = (Potion) obj;
                        if (potion != null) {
                            event.setCancelled(true);
                            final LivingEntity le = (LivingEntity) damagee;
                            potion.apply(le);
                            damagee.getWorld().playEffect(damagee.getLocation(), Effect.POTION_BREAK, (Object) potion);
                            targetedMeta = firedRound.getMetadata("SkullTurretsSMART");
                            if (targetedMeta.size() > 0 && firedRound instanceof EnderPearl) {
                                firedRound.setMetadata("SkullTurretsWizardHitSuccess", new FixedMetadataValue(SkullTurret.plugin, le));
                            }
                            firedRound.remove();
                            if (!SkullTurret.MOB_LOOT && damagee.getType() != EntityType.PLAYER && !damagee.hasMetadata("SkullTurretsNODROP")) {
                                damagee.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                            }
                            return;
                        }
                    }
                }
                targetedMeta = firedRound.getMetadata("SkullTurretDAMAGE");
                if (targetedMeta.size() > 0) {
                    final Object obj = targetedMeta.get(0).value();
                    if (obj instanceof PlacedSkull) {
                        final PlacedSkull pc = (PlacedSkull) obj;
                        if (firedRound.getType() == EntityType.ARROW) {
                            event.setDamage(pc.getIntelligence().getDamageMod());
                        }
                    }
                }
                targetedMeta = firedRound.getMetadata("SkullTurretsNODROP");
                if (targetedMeta.size() > 0) {
                    final Object obj = targetedMeta.get(0).value();
                    if (obj instanceof PlacedSkull && !SkullTurret.MOB_LOOT && damagee.getType() != EntityType.PLAYER && !damagee.hasMetadata("SkullTurretsNODROP")) {
                        damagee.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityShotBow(final EntityShootBowEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getEntity() != null && event.getEntity().getType() == EntityType.PLAYER) {
            final Player player = (Player) event.getEntity();
            final ItemStack bow = event.getBow();
            if (bow.hasItemMeta()) {
                final ItemMeta meta = bow.getItemMeta();
                if (meta.hasLore()) {
                    final List<String> lore = meta.getLore();
                    if (lore.size() != 0 && lore.get(0).contains("Skull Target Bow")) {
                        if (!this.plugin.hasPermission(player, "skullturret.use.bow")) {
                            event.setCancelled(true);
                            player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionSkullBow")));
                        }
                        final Entity arrow = event.getProjectile();
                        arrow.setMetadata("SkullTurretsArrow", new FixedMetadataValue(SkullTurret.plugin, player.getUniqueId().toString()));
                    }
                }
            } else if (SkullTurret.ALLOW_ARROW_DAMAGE && this.plugin.hasPermission(player, "skullturret.attack.bow")) {
                final Entity arrow2 = event.getProjectile();
                arrow2.setMetadata("SkullTurretsArrowForce", new FixedMetadataValue(SkullTurret.plugin, event.getForce()));
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(final BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Location loc = event.getToBlock().getLocation();
        if (this.plugin.skullMap.containsKey(loc)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ItemStack item = event.getCursor();
        if (event.getInventory() != null && event.getInventory().getType() == InventoryType.MERCHANT) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK && event.getRawSlot() < 3 && event.getRawSlot() >= 0) {
                if (!item.hasItemMeta()) {
                    return;
                }
                final BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta.hasLore()) {
                    final List<String> lore = meta.getLore();
                    if (lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book")) {
                        event.setCancelled(true);
                    }
                }
            }
        } else if (event.getInventory() != null && event.getInventory().getType() == InventoryType.CRAFTING) {
            final ItemStack result = event.getInventory().getItem(0);
            if (this.isDeviousSkullTurret(result) || this.isMasterSkullTurret(result)) {
                final Player player = (Player) event.getWhoClicked();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                if (event.getRawSlot() != 0) {
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        @Override
                        public void run() {
                            final CraftingInventory ci = (CraftingInventory) event.getInventory();
                            cp.ammoAmount = STListeners.this.countAmmo(ci);
                            STListeners.this.updateMobileTurretAmmoAmount(result, cp.ammoAmount);
                            player.updateInventory();
                        }
                    }, 1L);
                }
                if (event.getRawSlot() == 0 && player.getItemOnCursor() != null && player.getItemOnCursor().getType().equals(Material.AIR)) {
                    cp.clearArrows = true;
                }
            }
        } else if (event.getInventory() != null && event.getInventory().getType() == InventoryType.WORKBENCH) {
            final ItemStack result = event.getInventory().getItem(0);
            if (this.isDeviousSkullTurret(result) || this.isMasterSkullTurret(result)) {
                final Player player = (Player) event.getWhoClicked();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                if (this.isDeviousSkullTurret(result) && !this.plugin.hasPermission(player, "skullturret.create.tempdevious")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    cp.clearArrows = false;
                    cp.ammoAmount = 0;
                    return;
                }
                if (this.isMasterSkullTurret(result) && !this.plugin.hasPermission(player, "skullturret.create.tempmaster")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    cp.clearArrows = false;
                    cp.ammoAmount = 0;
                    return;
                }
                if (event.getRawSlot() != 0) {
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        @Override
                        public void run() {
                            final CraftingInventory ci = (CraftingInventory) event.getInventory();
                            cp.ammoAmount = STListeners.this.countAmmo(ci);
                            STListeners.this.updateMobileTurretAmmoAmount(result, cp.ammoAmount);
                            player.updateInventory();
                        }
                    }, 1L);
                }
                if (event.getRawSlot() == 0 && player.getItemOnCursor() != null && player.getItemOnCursor().getType().equals(Material.AIR)) {
                    cp.clearArrows = true;
                }
            }
        }
    }

    private int countAmmo(final CraftingInventory ci) {
        int ammoCount = 0;
        for (int i = 0; i < ci.getMatrix().length; ++i) {
            final ItemStack item = ci.getMatrix()[i];
            if (item != null && item.getType().equals(Material.ARROW)) {
                ammoCount += item.getAmount();
            }
        }
        return ammoCount;
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final ItemStack item = event.getOldCursor();
        if (event.getInventory() != null && event.getInventory().getType() == InventoryType.MERCHANT) {
            boolean trade = false;
            for (final Integer i : event.getRawSlots()) {
                if (i < 3 && i >= 0) {
                    trade = true;
                    break;
                }
            }
            if (item != null && item.getType() == Material.WRITTEN_BOOK && trade) {
                if (!item.hasItemMeta()) {
                    return;
                }
                final BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta.hasLore()) {
                    final List<String> lore = meta.getLore();
                    if (lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.clearPlayer();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        final PerPlayerSettings pps = this.plugin.perPlayerSettings.get(uuid);
        if (pps != null && pps.isPps()) {
            final PerPlayerGroups ppg = this.plugin.getPlayerGroup(uuid);
            if (ppg != null && !player.isOp()) {
                pps.setPps(false);
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": detected, " + player.getName() + " is in both a player group and has player settings.");
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": player settings for maxrange and skullnumber removed.");
                for (final PlacedSkull skull : this.plugin.skullMap.values()) {
                    if (skull.getSkullCreator().equals(uuid)) {
                        final int maxRange = ppg.getMaxRange();
                        final double fireRange = ppg.getMaxRange() * skull.getIntelligence().getFireRangeMultiplier();
                        if (maxRange == skull.getMaxRange() && skull.getFireRange() == fireRange) {
                            continue;
                        }
                        skull.reInitSkull();
                    }
                }
                new DataStore(this.plugin).savePerPlayerSettings();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.clearPlayer();
        final List<MetadataValue> targetedMeta = event.getEntity().getMetadata("SkullTurretsTarget");
        if (targetedMeta.size() > 0) {
            event.getEntity().removeMetadata("SkullTurretsTarget", this.plugin);
        }
        final List<ItemStack> drops = event.getDrops();
        if (SkullTurret.DROP_BOOKS_ON_DEATH) {
            for (final ItemStack stack : drops) {
                if (this.isSkullBook(stack)) {
                    stack.setDurability((short) 10);
                }
            }
        }
    }

    @EventHandler
    public void onItemSpawn(final ItemSpawnEvent event) {
        final ItemStack item = event.getEntity().getItemStack();
        if (this.isSkullBook(item)) {
            if (item.getDurability() == 10) {
                item.setDurability((short) 0);
                return;
            }
            final Location loc = event.getLocation();
            final World world = loc.getWorld();
            world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
            world.playEffect(loc, Effect.EXTINGUISH, 0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(final BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block b = event.getBlock();
        if (!this.testLocationForRemoval(b.getLocation())) {
            this.testLocationForRemoval(b.getRelative(BlockFace.UP).getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        for (final Block b : event.blockList()) {
            if (!this.testLocationForRemoval(b.getLocation())) {
                this.testLocationForRemoval(b.getRelative(BlockFace.UP).getLocation());
            }
        }
    }

    private boolean testLocationForRemoval(final Location loc) {
        if (this.plugin.skullMap.containsKey(loc)) {
            Utils.clearBlock(loc.getBlock());
            loc.getWorld().dropItemNaturally(loc, this.plugin.skullMap.get(loc).getIntelligence().getSkullItem());
            final UUID playerUUID = this.plugin.skullMap.get(loc).getSkullCreator();
            this.subPlayerSkullCount(playerUUID);
            this.plugin.skullMap.remove(loc);
            return true;
        }
        return false;
    }

    private void subPlayerSkullCount(final UUID playerUUID) {
        if (this.plugin.playersSkullNumber.containsKey(playerUUID)) {
            final SkullCounts sc = this.plugin.playersSkullNumber.get(playerUUID);
            int numSkulls = sc.getActiveSkulls();
            if (numSkulls > 1) {
                --numSkulls;
                sc.setActiveSkulls(numSkulls);
            } else {
                this.plugin.playersSkullNumber.remove(playerUUID);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.isSticky()) {
            final Location upFromFence = event.getRetractLocation().getBlock().getRelative(BlockFace.UP).getLocation();
            if (this.plugin.skullMap.containsKey(upFromFence)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final BlockFace face = event.getDirection();
        final List<Block> blocks = event.getBlocks();
        final int size = blocks.size();
        if (size == 0) {
            final Location skullTest = event.getBlock().getRelative(face).getLocation();
            if (this.plugin.skullMap.containsKey(skullTest)) {
                event.setCancelled(true);
            }
        } else {
            final Block lastBlock = blocks.get(size - 1);
            final Location skullTest2 = lastBlock.getRelative(face).getLocation();
            if (this.plugin.skullMap.containsKey(skullTest2)) {
                event.setCancelled(true);
            }
            for (final Block b : event.getBlocks()) {
                final Location upFromFence = b.getRelative(BlockFace.UP).getLocation();
                if (this.plugin.skullMap.containsKey(upFromFence)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(final EntityDeathEvent event) {
        final Entity ent = event.getEntity();
        if (ent instanceof LivingEntity && ent.getType() != EntityType.PLAYER) {
            if (ent.hasMetadata("SkullTurretsNODROP") && !SkullTurret.MOB_LOOT) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                return;
            }
            if (!ent.hasMetadata("SkullTurretsHit")) {
                if (SkullTurret.MOB_DROPS && ent.getType() == EntityType.SKELETON) {
                    if (SkullTurret.NO_DROP_WORLDS.contains(ent.getWorld().getName())) {
                        return;
                    }
                    int random = 1;
                    try {
                        random = (int) Math.ceil(Math.abs(SkullTurret.DROP_CHANCE) / 2.0);
                    } catch (Exception ex) {
                    }
                    final int chance = new Random().nextInt(SkullTurret.DROP_CHANCE) + 1;
                    if (random == chance) {
                        event.getDrops().add(new ItemStack(Material.SKELETON_SKULL, 1));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Entity e = event.getEntity();
        final List<MetadataValue> meta = e.getMetadata("SkullTurretsSMART");
        final List<MetadataValue> wizMeta = e.getMetadata("SkullTurretsWizardHitSuccess");
        final List<MetadataValue> forceMeta = e.getMetadata("SkullTurretsArrowForce");
        if (e instanceof Arrow && forceMeta.size() > 0) {
            final float force = (float) forceMeta.get(0).value();
            this.plugin.scheduler.runTaskLater(this.plugin, new Runnable() {
                @Override
                public void run() {
                    final Arrow a = (Arrow) e;
                    if (a.isOnGround() && a.getShooter() != null && a.getShooter() instanceof Player) {
                        final Location loc = e.getLocation().getBlock().getLocation();
                        final long cTime = System.currentTimeMillis();
                        final PlacedSkull pc = STListeners.this.plugin.skullMap.get(loc);
                        if (pc == null) {
                            return;
                        }
                        final double skullHealth = pc.getHealth();
                        final Player shooter = (Player) a.getShooter();
                        if (shooter.getGameMode() == GameMode.CREATIVE) {
                            return;
                        }
                        final double damage = force * 9.0f;
                        final UUID createrUUID = pc.getSkullCreator();
                        if (skullHealth > damage) {
                            pc.setHealth(skullHealth - damage);
                            if (SkullTurret.SKULLSFX) {
                                shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f, 0.1f);
                            } else {
                                shooter.sendMessage(ChatColor.RED + "A hit!");
                            }
                            pc.setRecoveryTimer(cTime);
                            if (SkullTurret.SKULLS_RETALIATE) {
                                shooter.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                            }
                            if (SkullTurret.SKULLSFX) {
                                shooter.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_HURT, 0.5f, 0.4f);
                            }
                            if (SkullTurret.DEBUG == 5) {
                                shooter.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.RED + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.RED + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                            }
                        } else if (!pc.isDying()) {
                            pc.setHealth(0.0);
                            pc.setDestructTimer(cTime);
                            pc.setDying(true);
                            pc.doDeathRattle(cTime, cTime, SkullTurret.DESTRUCT_TIMER);
                            if (SkullTurret.SKULLSFX) {
                                shooter.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_DEATH, 0.5f, 0.4f);
                            }
                            if (SkullTurret.SKULLS_RETALIATE) {
                                shooter.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                            }
                        }
                        if (SkullTurret.DEBUG == 5) {
                            System.out.println(shooter.getName() + " shot with a force of " + force + " damage of " + force * 9.0f);
                        }
                    }
                }
            }, 2L);
        }
        if (meta.size() > 0) {
            final Object obj = meta.get(0).value();
            if (obj instanceof PlacedSkull) {
                final PlacedSkull pc = (PlacedSkull) obj;
                if (wizMeta.size() > 0) {
                    final Object objw = wizMeta.get(0).value();
                    if (e instanceof EnderPearl && objw instanceof LivingEntity) {
                        final LivingEntity le = (LivingEntity) objw;
                        le.removeMetadata("SkullTurretsSMART", this.plugin);
                        e.removeMetadata("SkullTurretsWizardHitSuccess", this.plugin);
                    }
                } else if (e instanceof EnderPearl) {
                    final Entity target = pc.getTarget();
                    if (target != null) {
                        final Integer hash = pc.hashCode();
                        target.setMetadata(hash.toString(), new FixedMetadataValue(SkullTurret.plugin, System.currentTimeMillis()));
                        target.removeMetadata("SkullTurretsSMART", this.plugin);
                        pc.setTarget(null);
                    }
                }
            }
        }
        if (meta.size() > 0) {
            final Object obj = meta.get(0).value();
            this.plugin.scheduler.runTaskLater(this.plugin, new Runnable() {
                @Override
                public void run() {
                    boolean onGround = false;
                    if (e instanceof Arrow) {
                        final Arrow a = (Arrow) e;
                        onGround = a.isOnGround();
                    }
                    if (obj instanceof PlacedSkull) {
                        if (onGround) {
                            final PlacedSkull pc = (PlacedSkull) obj;
                            final Entity target = pc.getTarget();
                            if (target != null) {
                                final Integer hash = pc.hashCode();
                                target.setMetadata(hash.toString(), new FixedMetadataValue(SkullTurret.plugin, System.currentTimeMillis()));
                                target.removeMetadata("SkullTurretsSMART", STListeners.this.plugin);
                            }
                            pc.setTarget(null);
                        } else if (SkullTurret.MOB_DROPS) {
                            final PlacedSkull pc = (PlacedSkull) obj;
                            final Entity target = pc.getTarget();
                            if (target != null) {
                                target.setMetadata("SkullTurretsHit", new FixedMetadataValue(SkullTurret.plugin, System.currentTimeMillis()));
                            }
                        }
                    }
                }
            }, 2L);
        }
    }

    private void damageSkull(final Player player, final long cTime, final CustomPlayer cp) {
        if (cp.skull_to_damage != null && (!cp.skull_to_damage.getSkullCreator().equals(player.getUniqueId()) || SkullTurret.DEBUG == 5)) {
            cp.attackTimer = cTime;
            final PlacedSkull pc = cp.skull_to_damage;
            final double skullHealth = pc.getHealth();
            final ItemStack weapon = player.getItemInHand();
            if (weapon != null) {
                final Material weapType = weapon.getType();
                if (this.plugin.weapons.containsKey(weapType) && this.plugin.hasPermission(player, "skullturret.attack.weapon")) {
                    final double damage = this.plugin.weapons.get(weapType);
                    final UUID createrUUID = pc.getSkullCreator();
                    if (skullHealth > damage) {
                        pc.setHealth(skullHealth - damage);
                        pc.setRecoveryTimer(cTime);
                        if (SkullTurret.SKULLS_RETALIATE) {
                            player.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                        }
                        if (SkullTurret.SKULLSFX) {
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_HURT, 0.5f, 0.4f);
                        }
                        if (SkullTurret.DEBUG == 5) {
                            player.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.RED + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.RED + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                        }
                    } else if (!pc.isDying()) {
                        pc.setHealth(0.0);
                        pc.setDestructTimer(cTime);
                        pc.setDying(true);
                        pc.doDeathRattle(cTime, cTime, SkullTurret.DESTRUCT_TIMER);
                        if (SkullTurret.SKULLSFX) {
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_DEATH, 0.5f, 0.4f);
                        }
                        if (SkullTurret.SKULLS_RETALIATE) {
                            player.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                        }
                    }
                } else if (pc.getIntelligence().getRepair_item().equals(weapType) && this.plugin.hasPermission(player, "skullturret.repair")) {
                    final double repairAmt = SkullTurret.SKULL_REPAIR_AMOUNT;
                    if (pc.getHealth() < pc.getIntelligence().getHealth()) {
                        pc.setHealth(skullHealth + repairAmt);
                        if (pc.getHealth() >= pc.getIntelligence().getHealth()) {
                            pc.setDying(false);
                            pc.setHealth(pc.getIntelligence().getHealth());
                            this.removeRepairItem(weapon, player);
                        }
                        if (SkullTurret.SKULLSFX) {
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
                        }
                        if (SkullTurret.DEBUG == 5) {
                            player.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.GREEN + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.GREEN + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                        }
                    } else {
                        pc.setDying(false);
                        pc.setHealth(pc.getIntelligence().getHealth());
                        final int amount = weapon.getAmount();
                        if (amount > 1) {
                            this.removeRepairItem(weapon, player);
                        } else {
                            this.removeRepairItem(weapon, player);
                        }
                    }
                }
            }
        }
    }

    private void removeRepairItem(final ItemStack weapon, final Player player) {
        final int amount = weapon.getAmount();
        if (amount > 1) {
            weapon.setAmount(amount - 1);
            player.updateInventory();
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockDamageEvent(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        final Location loc = event.getBlock().getLocation();
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        if (SkullTurret.ALLOW_SKULL_DAMAGE) {
            final long cTime = System.currentTimeMillis();
            final PlacedSkull pc = this.plugin.skullMap.get(loc);
            if (pc == null) {
                cp.skull_to_damage = null;
                return;
            }
            if (SkullTurret.OFFLINE_PLAYERS) {
                final Player playerOwner = Utils.getPlayerFromUUID(pc.getSkullCreator());
                if (playerOwner == null) {
                    cp.skull_to_damage = null;
                    return;
                }
            }
            if (!pc.getSkullCreator().equals(player.getUniqueId()) || SkullTurret.DEBUG == 5) {
                final ItemStack weapon = player.getItemInHand();
                final Material weapType = weapon.getType();
                if (this.plugin.weapons.containsKey(weapType) || pc.getIntelligence().getRepair_item().equals(weapType)) {
                    event.setCancelled(true);
                    cp.skull_to_damage = pc;
                    if (cTime - cp.attackTimer > 1700L) {
                        this.damageSkull(player, cTime, cp);
                        cp.attackTimer = cTime;
                    }
                }
            } else {
                cp.skull_to_damage = null;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block block = event.getClickedBlock();
        final Player player = event.getPlayer();
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        if (cp.skull_to_damage != null && !this.plugin.skullMap.containsKey(block.getLocation())) {
            cp.skull_to_damage = null;
            cp.attackTimer = 0L;
        }
        if (cp.command.equals("skedit") && block != null && this.plugin.skullMap.containsKey(block.getLocation()) && block.getType().equals(Material.SKELETON_SKULL) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (this.plugin.hasPermission(player, "skullturret.edit")) {
                final PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
                if (!pc.getSkullCreator().equals(player.getUniqueId()) && !this.plugin.hasPermission(player, "skullturret.admin")) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullOwner"), pc.getIntelligence().getNormalName(), pc.getSkullCreatorLastKnowName().isEmpty() ? pc.getSkullCreator() : pc.getSkullCreatorLastKnowName())));
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullSelectFail")));
                    return;
                }
                player.sendMessage(Utils.parseText(Utils.getLocalization("skullSelected")));
                cp.pc = pc;
            } else {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noSelectPermission")));
            }
        } else if (cp.command.equals("skrotate")) {
            if (block.getType().equals(Material.SKELETON_SKULL) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                final PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
                if (pc == null) {
                    return;
                }
                if (pc.doPatrol()) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("rotateFail")));
                    return;
                }
                if (!pc.getSkullCreator().equals(player.getUniqueId()) && !this.plugin.hasPermission(player, "skullturret.admin")) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullOwner"), pc.getIntelligence().getNormalName(), pc.getSkullCreatorLastKnowName().isEmpty() ? pc.getSkullCreator() : pc.getSkullCreatorLastKnowName())));
                    player.sendMessage(Utils.parseText(Utils.getLocalization("rotateFailB")));
                    return;
                }
                final Skull skull = pc.getSkull();
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    pc.setSkullRotation(this.clockwiseRotation(skull));
                } else {
                    pc.setSkullRotation(this.counterClockwiseRotation(skull));
                }
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("rotateSuccess"), skull.getRotation().name())));
            }
        } else if (this.plugin.skullMap.containsKey(block.getLocation()) && block.getType().equals(Material.SKELETON_SKULL)) {
            final ItemStack item = event.getItem();
            final PlacedSkull pc2 = this.plugin.skullMap.get(block.getLocation());
            if (pc2 == null) {
                return;
            }
            if (pc2 instanceof MobileSkull && player.getItemInHand().getType().equals(Material.ARROW)) {
                final MobileSkull ms = (MobileSkull) pc2;
                final int ammoAmount = ms.getAmmoAmount();
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("arrowsRemain"), ms.getAmmoAmount())));
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!SkullTurret.ALLOW_TEMPTURRET_REARM) {
                        player.sendMessage(Utils.parseText(Utils.getLocalization("rearmFail")));
                        return;
                    }
                    final int clickedAmmo = player.getItemInHand().getAmount();
                    if (ammoAmount + clickedAmmo <= 448) {
                        ms.setAmmoAmount(ammoAmount + clickedAmmo);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setItemInHand(new ItemStack(Material.AIR));
                            this.updateInventory(player);
                        }
                    } else {
                        final int subAmmo = 448 - ammoAmount;
                        final int inhand = player.getItemInHand().getAmount();
                        ms.setAmmoAmount(448);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.getItemInHand().setAmount(inhand - subAmmo);
                            this.updateInventory(player);
                        }
                    }
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("arrowsRemain"), ms.getAmmoAmount())));
                    event.setCancelled(true);
                    return;
                }
            }
            if (cp.command.equalsIgnoreCase("default") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!pc2.getSkullCreator().equals(player.getUniqueId())) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultErr"), pc2.getIntelligence().getNormalName())));
                    return;
                }
                final SkullIntelligence skintel = pc2.getIntelligence();
                if (skintel != SkullIntelligence.MASTER && skintel != SkullIntelligence.WIZARD) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultErr"), pc2.getIntelligence().getNormalName())));
                    return;
                }
                PerPlayerSettings ppsDefaults = this.plugin.perPlayerSettings.get(player.getUniqueId());
                if (ppsDefaults != null) {
                    if (skintel == SkullIntelligence.MASTER && this.isMasterSkull(player.getItemInHand())) {
                        ppsDefaults.setAmmoTypeName(pc2.getAmmoType().name());
                        ppsDefaults.setMasterSkinName(pc2.getSkinData());
                        ppsDefaults.setMasterPatrol(pc2.doPatrol());
                        ppsDefaults.setMasterRedstone(pc2.isRedstone());
                        if (ppsDefaults.isMasterDefaults()) {
                            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultOverwrite"), pc2.getIntelligence().getNormalName())));
                        } else {
                            ppsDefaults.setMasterDefaults(true);
                        }
                        new DataStore(this.plugin).savePerPlayerSettings();
                    } else if (skintel == SkullIntelligence.WIZARD && this.isWizardSkull(player.getItemInHand())) {
                        ppsDefaults.setWizardSkinName(pc2.getSkinData());
                        ppsDefaults.setWizardPatrol(pc2.doPatrol());
                        ppsDefaults.setWizardRedstone(pc2.isRedstone());
                        if (ppsDefaults.isWizardDefaults()) {
                            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultOverwrite"), pc2.getIntelligence().getNormalName())));
                        } else {
                            ppsDefaults.setWizardDefaults(true);
                        }
                        new DataStore(this.plugin).savePerPlayerSettings();
                    }
                } else if (skintel == SkullIntelligence.MASTER && this.isMasterSkull(player.getItemInHand())) {
                    ppsDefaults = new PerPlayerSettings(player.getUniqueId(), pc2.getSkinData(), pc2.getAmmoType().name(), pc2.isRedstone(), pc2.doPatrol());
                    this.plugin.perPlayerSettings.put(player.getUniqueId(), ppsDefaults);
                    new DataStore(this.plugin).savePerPlayerSettings();
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultSuccess"), pc2.getIntelligence().getNormalName())));
                } else if (skintel == SkullIntelligence.WIZARD && this.isWizardSkull(player.getItemInHand())) {
                    ppsDefaults = new PerPlayerSettings(player.getUniqueId(), pc2.getSkinData(), pc2.isRedstone(), pc2.doPatrol());
                    this.plugin.perPlayerSettings.put(player.getUniqueId(), ppsDefaults);
                    new DataStore(this.plugin).savePerPlayerSettings();
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultSuccess"), pc2.getIntelligence().getNormalName())));
                }
            } else {
                if (cp.command.equalsIgnoreCase("resetDefault") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final PerPlayerSettings ppsDefaults2 = this.plugin.perPlayerSettings.get(player.getUniqueId());
                    if (ppsDefaults2 != null) {
                        final SkullIntelligence skintel2 = pc2.getIntelligence();
                        if (skintel2 == SkullIntelligence.MASTER && this.isMasterSkull(player.getItemInHand())) {
                            if (ppsDefaults2.isMasterDefaults()) {
                                ppsDefaults2.setMasterDefaults(false);
                                ppsDefaults2.cleanUpPPS();
                                cp.clearPlayer();
                                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), pc2.getIntelligence().getNormalName())));
                            }
                            new DataStore(this.plugin).savePerPlayerSettings();
                        } else if (skintel2 == SkullIntelligence.WIZARD && this.isWizardSkull(player.getItemInHand())) {
                            if (ppsDefaults2.isWizardDefaults()) {
                                ppsDefaults2.setWizardDefaults(false);
                                ppsDefaults2.cleanUpPPS();
                                cp.clearPlayer();
                                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), pc2.getIntelligence().getNormalName())));
                            }
                            new DataStore(this.plugin).savePerPlayerSettings();
                        }
                    }
                    return;
                }
                if (!this.plugin.hasPermission(player, "skullturret.use")) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                    event.setCancelled(true);
                    return;
                }
                if (item != null && item.getType() == Material.WRITTEN_BOOK && (pc2.getIntelligence() == SkullIntelligence.MASTER || pc2.getIntelligence() == SkullIntelligence.WIZARD)) {
                    pc2.parseBook(item, player);
                    new DataStore(this.plugin).saveDatabase(false);
                } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getType() == Material.AIR) {
                    if ((pc2.getIntelligence() == SkullIntelligence.MASTER || pc2.getIntelligence() == SkullIntelligence.WIZARD) && (pc2.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin"))) {
                        final Inventory playerInv = player.getInventory();
                        final ItemStack book = pc2.getInfoBook(false, player);
                        if (this.alreadyHasBook(pc2, player)) {
                            player.sendMessage(Utils.parseText(Utils.getLocalization("skullBookInvError")));
                            return;
                        }
                        playerInv.addItem(book);
                        player.updateInventory();
                    } else {
                        if ((pc2.getIntelligence() == SkullIntelligence.DEVIOUS || pc2.getIntelligence() == SkullIntelligence.CRAZED) && (pc2.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin"))) {
                            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullBookNoBookErr"), pc2.getIntelligence().getNormalName())));
                            return;
                        }
                        player.sendMessage(Utils.parseText(Utils.getLocalization("skullBookNotOwner")));
                    }
                }
            }
        }
    }

    private boolean alreadyHasBook(final PlacedSkull clickedSkull, final Player player) {
        for (final ItemStack item : player.getInventory()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                if (!item.hasItemMeta()) {
                    continue;
                }
                final BookMeta meta = (BookMeta) item.getItemMeta();
                if (!meta.hasLore()) {
                    continue;
                }
                if (meta.getLore().size() < 2) {
                    continue;
                }
                if (!meta.getLore().get(1).equals("Skull Knowledge Book")) {
                    continue;
                }
                PlacedSkull pc = null;
                try {
                    final String page1 = meta.getPage(1);
                    if (page1.contains("BROKE")) {
                        continue;
                    }
                    final String[] split = page1.split(":");
                    final String world = split[2].split("\n")[0].trim();
                    final String[] coords = split[3].split("\n")[0].trim().split(",");
                    final int x = Integer.parseInt(coords[0].trim());
                    final int y = Integer.parseInt(coords[1].trim());
                    final int z = Integer.parseInt(coords[2].trim());
                    final World skullWorld = SkullTurret.plugin.getServer().getWorld(world);
                    if (skullWorld == null) {
                        continue;
                    }
                    final Location skullLocation = new Location(skullWorld, x, y, z);
                    pc = SkullTurret.plugin.skullMap.get(skullLocation);
                    if (pc == clickedSkull) {
                        return true;
                    }
                    continue;
                } catch (Exception ex) {
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onCraftItemEvent(final CraftItemEvent event) {
        if (event.getRecipe() instanceof ShapelessRecipe) {
            final ItemStack mds = this.plugin.recipes.mobileDeviousSkullItem;
            final ItemStack mms = this.plugin.recipes.mobileMasterSkullItem;
            final ShapelessRecipe eventRecipe = (ShapelessRecipe) event.getRecipe();
            final ItemStack result = eventRecipe.getResult();
            if (result.equals(mds) || result.equals(mms)) {
                final Player player = (Player) event.getView().getPlayer();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                final CraftingInventory ci = event.getInventory();
                if (cp.clearArrows) {
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, new Runnable() {
                        @Override
                        public void run() {
                            int maxIndex = 0;
                            if (ci.getType() == InventoryType.CRAFTING) {
                                maxIndex = 4;
                            } else if (ci.getType() == InventoryType.WORKBENCH) {
                                maxIndex = 9;
                            }
                            for (int i = 1; i <= maxIndex; ++i) {
                                if (ci.getItem(i) != null && ci.getItem(i).getType().equals(Material.ARROW)) {
                                    ci.setItem(i, new ItemStack(Material.AIR));
                                }
                            }
                            cp.clearArrows = false;
                        }
                    }, 1L);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemCraft(final PrepareItemCraftEvent event) {
        final Player player = (Player) event.getView().getPlayer();
        String itemName = "";
        if (event.getRecipe() instanceof ShapedRecipe) {
            final ShapedRecipe eventRecipe = (ShapedRecipe) event.getRecipe();
            if (eventRecipe.getResult().equals(this.plugin.recipes.crazedSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.crazed")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Crazed Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!this.isRawSkull(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                this.updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.deviousSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.devious")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Devious Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!this.isCrazedSkull(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                this.updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.masterSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.master")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Master Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!this.isDeviousSkull(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                this.updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.wizardSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.wizard")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Wizard Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!this.isMasterSkull(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                } else if (!this.isGrayDye(event.getInventory().getMatrix()[0])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                this.updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.bowTargetItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.bow")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Skull Bow";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!this.isRawBow(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                this.updateResultQuantity(player, event.getInventory().getContents()[0]);
            }
        } else if (event.getRecipe() instanceof ShapelessRecipe) {
            final ShapelessRecipe eventRecipe2 = (ShapelessRecipe) event.getRecipe();
            final CustomPlayer cp = CustomPlayer.getSettings(player);
            final ItemStack mds = this.plugin.recipes.mobileDeviousSkullItem;
            final ItemStack mms = this.plugin.recipes.mobileMasterSkullItem;
            final ItemStack result = eventRecipe2.getResult();
            itemName = "Temporary Skull Turret";
            if (result.equals(mds) && !this.plugin.hasPermission(player, "skullturret.create.tempdevious")) {
                event.getInventory().setItem(0, new ItemStack(Material.AIR));
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                cp.clearArrows = false;
                cp.ammoAmount = 0;
                return;
            }
            if (result.equals(mms) && !this.plugin.hasPermission(player, "skullturret.create.tempmaster")) {
                event.getInventory().setItem(0, new ItemStack(Material.AIR));
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                cp.clearArrows = false;
                cp.ammoAmount = 0;
                return;
            }
            if (result.equals(mds) || result.equals(mms)) {
                boolean foundDevious = false;
                boolean foundFence = false;
                boolean foundMaster = false;
                boolean foundNetherFence = false;
                final CraftingInventory ci = event.getInventory();
                int ammoCount = 0;
                for (int i = 0; i < ci.getMatrix().length; ++i) {
                    final ItemStack item = ci.getMatrix()[i];
                    if (item != null) {
                        if (this.isDeviousSkull(item)) {
                            foundDevious = true;
                        } else if (this.isMasterSkull(item)) {
                            foundMaster = true;
                        } else if (this.isOnWoodFence(item.getType())) {
                            foundFence = true;
                        } else if (item.getType().equals(Material.NETHER_BRICK_FENCE)) {
                            foundNetherFence = true;
                        }
                        if (item != null && item.getType().equals(Material.ARROW)) {
                            ammoCount += item.getAmount();
                        }
                    }
                }
                if ((foundDevious && foundFence) || (foundMaster && foundNetherFence)) {
                    if (ammoCount != cp.ammoAmount) {
                        cp.ammoAmount = ammoCount;
                    }
                    if (ammoCount == 0) {
                        cp.clearArrows = false;
                    }
                    if (cp.clearArrows) {
                        int maxIndex = 0;
                        if (ci.getType() == InventoryType.CRAFTING) {
                            maxIndex = 4;
                        } else if (ci.getType() == InventoryType.WORKBENCH) {
                            maxIndex = 9;
                        }
                        for (int j = 1; j <= maxIndex; ++j) {
                            if (ci.getItem(j) != null && ci.getItem(j).getType().equals(Material.ARROW)) {
                                ci.setItem(j, new ItemStack(Material.AIR));
                            }
                        }
                        cp.clearArrows = false;
                    }
                    this.updateMobileTurretAmmoAmount(ci.getResult(), cp.ammoAmount);
                    this.updateInventory(player);
                } else {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    this.updateInventory(player);
                }
            }
        }
    }

    private void updateResultQuantity(final Player player, final ItemStack item) {
        if (item == null) {
            return;
        }
        if (this.isCrazedSkull(item)) {
            item.setAmount(SkullTurret.CRAZED_STACK_SIZE);
        } else if (this.isDeviousSkull(item)) {
            item.setAmount(SkullTurret.DEVIOUS_STACK_SIZE);
        } else if (this.isMasterSkull(item)) {
            item.setAmount(SkullTurret.MASTER_STACK_SIZE);
        } else if (this.isWizardSkull(item)) {
            item.setAmount(SkullTurret.WIZARD_STACK_SIZE);
        } else if (this.isSkullBow(item)) {
            item.setAmount(SkullTurret.BOW_STACK_SIZE);
        }
        this.updateInventory(player);
    }

    private void updateInventory(final Player player) {
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        if (!cp.invUpdate) {
            cp.invUpdate = true;
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                @Override
                public void run() {
                    player.updateInventory();
                    cp.invUpdate = false;
                }
            }, 1L);
        }
    }

    private boolean updateMobileTurretAmmoAmount(final ItemStack item, final int ammoCount) {
        if (item == null || item.getType().equals(Material.AIR)) {
            return false;
        }
        String loreName = "";
        if (this.isDeviousSkullTurret(item)) {
            loreName = this.plugin.recipes.mobileDeviousLoreName;
        } else {
            if (!this.isMasterSkullTurret(item)) {
                return false;
            }
            loreName = this.plugin.recipes.mobileMasterLoreName;
        }
        final ItemMeta itmeta = item.getItemMeta();
        final List<String> lore = new ArrayList<>();
        lore.add(loreName);
        lore.add("Ammo=" + ammoCount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return true;
    }

    private boolean onFence(final Block against) {
        return against.getType() == Material.OAK_FENCE || against.getType() == Material.SPRUCE_FENCE || against.getType() == Material.ACACIA_FENCE || against.getType() == Material.BIRCH_FENCE || against.getType() == Material.JUNGLE_FENCE || against.getType() == Material.DARK_OAK_FENCE || against.getType() == Material.NETHER_BRICK_FENCE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        final Block block = event.getBlock();
        final Block against = event.getBlockAgainst();
        final ItemStack skull = event.getItemInHand();
        final Player player = event.getPlayer();
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.skull_to_damage = null;
        cp.attackTimer = 0L;
        if (this.isDeviousSkullTurret(skull) || this.isMasterSkullTurret(skull)) {
            if (!this.plugin.hasPermission(player, "skullturret.use.tempturret")) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                event.setCancelled(true);
                return;
            }
            if (!this.isOnGround(block, event.getBlockAgainst())) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("placeOnGroundErr")));
                event.setCancelled(true);
                return;
            }
            final Block skullBlock = this.placeSkullTurret(cp, player, block);
            if (skullBlock == null) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("invalidPosition")));
                event.setCancelled(true);
                return;
            }
            final UUID playerUUID = player.getUniqueId();
            this.plugin.scheduler.runTaskLater(this.plugin, new Runnable() {
                @Override
                public void run() {
                    if (skullBlock.getType().equals(Material.SKELETON_SKULL)) {
                        if (STListeners.this.skullLimit(playerUUID)) {
                            final Block block = event.getBlock();
                            Utils.clearBlock(block);
                            Utils.clearBlock(skullBlock);
                            skullBlock.removeMetadata("SkullTurretPlace", STListeners.this.plugin);
                            SkullIntelligence skullType = SkullIntelligence.CRAZED;
                            if (STListeners.this.isDeviousSkullTurret(skull)) {
                                skullType = SkullIntelligence.DEVIOUS;
                            } else if (STListeners.this.isMasterSkullTurret(skull)) {
                                skullType = SkullIntelligence.MASTER;
                            } else {
                                skullType = SkullIntelligence.DEVIOUS;
                            }
                            final ItemStack skullItem = skullType.getMobileSkullItem();
                            STListeners.this.updateMobileTurretAmmoAmount(skullItem, STListeners.this.getMobileSkullAmmo(skull));
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), skullItem);
                            }
                            return;
                        }
                        SkullIntelligence skullType2 = SkullIntelligence.CRAZED;
                        if (STListeners.this.isDeviousSkullTurret(skull)) {
                            skullType2 = SkullIntelligence.DEVIOUS;
                        } else if (STListeners.this.isMasterSkullTurret(skull)) {
                            skullType2 = SkullIntelligence.MASTER;
                        }
                        int maxRange = SkullTurret.MAX_RANGE;
                        final PerPlayerGroups ppg = STListeners.this.plugin.getPlayerGroup(playerUUID);
                        final PerPlayerSettings pps = STListeners.this.plugin.perPlayerSettings.get(playerUUID);
                        if (ppg != null && !player.isOp()) {
                            maxRange = ppg.getMaxRange();
                        } else if (pps != null && pps.isPps()) {
                            maxRange = STListeners.this.plugin.perPlayerSettings.get(playerUUID).getMaxRange();
                        } else {
                            maxRange = SkullTurret.MAX_RANGE;
                        }
                        final int ammoCount = STListeners.this.getMobileSkullAmmo(skull);
                        final MobileSkull ms = new MobileSkull(skullBlock, event.getPlayer().getUniqueId(), maxRange, skullType2, ammoCount);
                        ms.setSkullCreatorLastKnowName(player.getName());
                        STListeners.this.plugin.skullMap.put(skullBlock.getLocation(), ms);
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("tempSkullAdded"), "Temporary " + skullType2.getNormalName() + " skull")));
                        new DataStore(STListeners.this.plugin).saveDatabase(true);
                        if (STListeners.this.plugin.playersSkullNumber.containsKey(playerUUID)) {
                            final SkullCounts sc = STListeners.this.plugin.playersSkullNumber.get(playerUUID);
                            int numSkulls = sc.getActiveSkulls();
                            ++numSkulls;
                            sc.setActiveSkulls(numSkulls);
                        } else {
                            STListeners.this.plugin.playersSkullNumber.put(playerUUID, new SkullCounts(1, 0));
                        }
                        skullBlock.removeMetadata("SkullTurretPlace", STListeners.this.plugin);
                    }
                }
            }, 10L);
        } else {
            if (block.getType().equals(Material.SKELETON_SKULL) && this.onFence(against)) {
                final UUID playerUUID2 = player.getUniqueId();
                if (!this.isCrazedSkull(skull) && !this.isDeviousSkull(skull) && !this.isMasterSkull(skull) && !this.isWizardSkull(skull)) {
                    return;
                }
                if (!this.isValidSkull(block)) {
                    event.setCancelled(true);
                    player.sendMessage(Utils.parseText(Utils.getLocalization("placeError")));
                    return;
                }
                if (!this.plugin.hasPermission(player, "skullturret.use")) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                    event.setCancelled(true);
                    return;
                }
                if (this.skullLimit(playerUUID2)) {
                    event.setCancelled(true);
                    return;
                }
                String postType = "";
                String skullType = "";
                if ((this.isMasterSkull(skull) || this.isWizardSkull(skull)) && !block.getRelative(BlockFace.DOWN).getType().equals(Material.NETHER_BRICK_FENCE)) {
                    postType = "Nether brick";
                    skullType = (this.isMasterSkull(skull) ? "Master Skulls" : "Wizard Skulls");
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType, postType)));
                    event.setCancelled(true);
                    return;
                }
                if ((this.isDeviousSkull(skull) || this.isCrazedSkull(skull)) && !this.isOnWoodFence(block.getRelative(BlockFace.DOWN).getType())) {
                    postType = "Oak";
                    skullType = (this.isDeviousSkull(skull) ? "Devious Skulls" : "Crazed Skulls");
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType, postType)));
                    event.setCancelled(true);
                    return;
                }
                this.plugin.scheduler.runTaskLater(this.plugin, new Runnable() {
                    @Override
                    public void run() {
                        if (block.getType().equals(Material.SKELETON_SKULL) && STListeners.this.onFence(against)) {
                            if (STListeners.this.skullLimit(playerUUID2)) {
                                final Block block = event.getBlock();
                                Utils.clearBlock(block);
                                SkullIntelligence skullType = SkullIntelligence.CRAZED;
                                if (STListeners.this.isDeviousSkull(skull)) {
                                    skullType = SkullIntelligence.DEVIOUS;
                                } else if (STListeners.this.isMasterSkull(skull)) {
                                    skullType = SkullIntelligence.MASTER;
                                }
                                if (player.getGameMode() != GameMode.CREATIVE) {
                                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), skullType.getSkullItem());
                                }
                                return;
                            }
                            SkullIntelligence skullType2 = SkullIntelligence.CRAZED;
                            if (STListeners.this.isDeviousSkull(skull)) {
                                skullType2 = SkullIntelligence.DEVIOUS;
                            } else if (STListeners.this.isMasterSkull(skull)) {
                                skullType2 = SkullIntelligence.MASTER;
                            } else if (STListeners.this.isWizardSkull(skull)) {
                                skullType2 = SkullIntelligence.WIZARD;
                            }
                            int maxRange = SkullTurret.MAX_RANGE;
                            final PerPlayerGroups ppg = STListeners.this.plugin.getPlayerGroup(playerUUID2);
                            final PerPlayerSettings pps = STListeners.this.plugin.perPlayerSettings.get(playerUUID2);
                            if (ppg != null && !player.isOp()) {
                                maxRange = ppg.getMaxRange();
                            } else if (pps != null && pps.isPps()) {
                                maxRange = STListeners.this.plugin.perPlayerSettings.get(playerUUID2).getMaxRange();
                            } else {
                                maxRange = SkullTurret.MAX_RANGE;
                            }
                            final PlacedSkull pc = new PlacedSkull(block, event.getPlayer().getUniqueId(), maxRange, skullType2);
                            pc.setSkullCreatorLastKnowName(player.getName());
                            pc.setType(((Skull) block.getState()).getSkullType());
                            if (pc.getType().equals(SkullType.PLAYER)) {
                                pc.setSkin(((Skull) block.getState()).getOwner());
                            }
                            STListeners.this.plugin.skullMap.put(block.getLocation(), pc);
                            event.getPlayer().sendMessage(ChatColor.GREEN + skullType2.getNormalName() + " skull added.");
                            new DataStore(STListeners.this.plugin).saveDatabase(true);
                            if (STListeners.this.plugin.playersSkullNumber.containsKey(playerUUID2)) {
                                final SkullCounts sc = STListeners.this.plugin.playersSkullNumber.get(playerUUID2);
                                int numSkulls = sc.getActiveSkulls();
                                ++numSkulls;
                                sc.setActiveSkulls(numSkulls);
                            } else {
                                STListeners.this.plugin.playersSkullNumber.put(playerUUID2, new SkullCounts(1, 0));
                            }
                        }
                    }
                }, 10L);
            }
            String postType2 = "";
            String skullType2 = "";
            if ((this.isMasterSkull(skull) || this.isWizardSkull(skull)) && (!against.getType().equals(Material.NETHER_BRICK_FENCE) || !this.isValidSkull(block))) {
                postType2 = "Nether";
                skullType2 = (this.isMasterSkull(skull) ? "Master Skulls" : "Wizard Skulls");
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType2, postType2)));
                event.setCancelled(true);
                return;
            }
            if ((this.isDeviousSkull(skull) || this.isCrazedSkull(skull)) && (!this.isOnWoodFence(against.getType()) || !this.isValidSkull(block))) {
                postType2 = "Wooden";
                skullType2 = (this.isDeviousSkull(skull) ? "Devious Skulls" : "Crazed Skulls");
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType2, postType2)));
                event.setCancelled(true);
            }
        }
    }

    private boolean isOnWoodFence(final Material type) {
        return type == Material.OAK_FENCE || type == Material.SPRUCE_FENCE || type == Material.ACACIA_FENCE || type == Material.BIRCH_FENCE || type == Material.JUNGLE_FENCE || type == Material.DARK_OAK_FENCE;
    }

    private boolean isOnGround(final Block skullBlock, final Block againstBlock) {
        return againstBlock.getRelative(BlockFace.UP).equals(skullBlock);
    }

    private boolean isValidSkull(final Block skull) {
        return skull.getType().equals(Material.SKELETON_SKULL) /*&& skull.getData() == 1*/;
    }

    private Block placeSkullTurret(final CustomPlayer cp, final Player player, final Block block) {
        final Location skullBlock = block.getLocation();
        final World world = block.getWorld();
        final int blockY = skullBlock.getBlockY();
        final int maxWorldHeight = world.getMaxHeight();
        if (blockY + 2 >= maxWorldHeight) {
            return null;
        }
        final Location abovePlacedSkull = block.getRelative(BlockFace.UP).getLocation();
        if (!abovePlacedSkull.getBlock().isEmpty()) {
            return null;
        }
        final Block newSkullBlock = abovePlacedSkull.getBlock();
        newSkullBlock.setMetadata("SkullTurretPlace", new FixedMetadataValue(SkullTurret.plugin, ""));
        final MaterialData skullMD = block.getState().getData();
        newSkullBlock.setType(block.getType());
        final BlockFace skullFacing = ((Skull) block.getState()).getRotation();
        final BlockState topState = newSkullBlock.getState();
        topState.setData(skullMD);
        ((Skull) topState).setRotation(skullFacing);
        topState.update();
        block.setType(Material.LEGACY_PISTON_EXTENSION);
        final BlockState bottomState = block.getState();
        final PistonExtensionMaterial pem = (PistonExtensionMaterial) bottomState.getData();
        pem.setFacingDirection(BlockFace.DOWN);
        bottomState.setData(pem);
        bottomState.update();
        return newSkullBlock;
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        final Block upBlock = event.getBlock().getRelative(BlockFace.UP);
        boolean fenceBreak = false;
        if (this.isOnWoodFence(block.getType()) || block.getType().equals(Material.NETHER_BRICK_FENCE) || block.getType().equals(Material.LEGACY_PISTON_EXTENSION)) {
            block = upBlock;
            fenceBreak = true;
        }
        if (block.getType().equals(Material.SKELETON_SKULL)) {
            final PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
            final Player player = event.getPlayer();
            if (pc != null) {
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                if (SkullTurret.DEBUG == 5 && pc.getSkullCreator().equals(player.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
                if (!this.plugin.hasPermission(player, "skullturret.use")) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                    event.setCancelled(true);
                    return;
                }
                if (!pc.getSkullCreator().equals(player.getUniqueId()) && !this.plugin.hasPermission(player, "skullturret.admin")) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("notOwnedSkull")));
                    event.setCancelled(true);
                    return;
                }
                if (SkullTurret.DEBUG > 0) {
                    pc.clearDebug();
                }
                final UUID playerUUID = event.getPlayer().getUniqueId();
                if (cp.pc != null && cp.pc.equals(pc)) {
                    cp.pc = null;
                }
                final World world = block.getWorld();
                if (!player.getGameMode().equals(GameMode.CREATIVE) && SkullTurret.DROP) {
                    if (pc instanceof MobileSkull) {
                        final MobileSkull ms = (MobileSkull) pc;
                        final ItemStack skullItem = pc.getIntelligence().getMobileSkullItem();
                        this.updateMobileTurretAmmoAmount(skullItem, ms.getAmmoAmount());
                        world.dropItemNaturally(pc.getLocation(), skullItem);
                    } else {
                        if (!fenceBreak) {
                            event.setCancelled(true);
                            Utils.clearBlock(block);
                        }
                        world.dropItemNaturally(pc.getLocation(), pc.getIntelligence().getSkullItem());
                        if (SkullTurret.DROP_BOOK_ON_BREAK && SkullTurret.DROP) {
                            final ItemStack book = pc.getInfoBook(true, player);
                            if (book != null) {
                                book.setDurability((short) 10);
                                world.dropItemNaturally(pc.getLocation(), book);
                            }
                        }
                    }
                }
                this.plugin.skullMap.remove(block.getLocation());
                if (fenceBreak) {
                    Utils.clearBlock(block);
                }
                if (pc instanceof MobileSkull) {
                    Utils.clearBlock(block);
                    Utils.clearBlock(block.getRelative(BlockFace.DOWN));
                }
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("tempSkullRemoved"), ((pc instanceof MobileSkull) ? "Temporary " : "") + pc.getIntelligence().getNormalName() + " skull")));
                new DataStore(this.plugin).saveDatabase(true);
                this.subPlayerSkullCount(playerUUID);
            }
        }
    }

    private BlockFace clockwiseRotation(final Skull skull) {
        final BlockFace face = skull.getRotation();
        switch (face) {
            case NORTH: {
                return BlockFace.NORTH_NORTH_EAST;
            }
            case NORTH_NORTH_EAST: {
                return BlockFace.NORTH_EAST;
            }
            case NORTH_EAST: {
                return BlockFace.EAST_NORTH_EAST;
            }
            case EAST_NORTH_EAST: {
                return BlockFace.EAST;
            }
            case EAST: {
                return BlockFace.EAST_SOUTH_EAST;
            }
            case EAST_SOUTH_EAST: {
                return BlockFace.SOUTH_EAST;
            }
            case SOUTH_EAST: {
                return BlockFace.SOUTH_SOUTH_EAST;
            }
            case SOUTH_SOUTH_EAST: {
                return BlockFace.SOUTH;
            }
            case SOUTH: {
                return BlockFace.SOUTH_SOUTH_WEST;
            }
            case SOUTH_SOUTH_WEST: {
                return BlockFace.SOUTH_WEST;
            }
            case SOUTH_WEST: {
                return BlockFace.WEST_SOUTH_WEST;
            }
            case WEST_SOUTH_WEST: {
                return BlockFace.WEST;
            }
            case WEST: {
                return BlockFace.WEST_NORTH_WEST;
            }
            case WEST_NORTH_WEST: {
                return BlockFace.NORTH_WEST;
            }
            case NORTH_WEST: {
                return BlockFace.NORTH_NORTH_WEST;
            }
            case NORTH_NORTH_WEST: {
                return BlockFace.NORTH;
            }
            default: {
                return BlockFace.NORTH;
            }
        }
    }

    private BlockFace counterClockwiseRotation(final Skull skull) {
        final BlockFace face = skull.getRotation();
        switch (face) {
            case NORTH: {
                return BlockFace.NORTH_NORTH_WEST;
            }
            case NORTH_NORTH_WEST: {
                return BlockFace.NORTH_WEST;
            }
            case NORTH_WEST: {
                return BlockFace.WEST_NORTH_WEST;
            }
            case WEST_NORTH_WEST: {
                return BlockFace.WEST;
            }
            case WEST: {
                return BlockFace.WEST_SOUTH_WEST;
            }
            case WEST_SOUTH_WEST: {
                return BlockFace.SOUTH_WEST;
            }
            case SOUTH_WEST: {
                return BlockFace.SOUTH_SOUTH_WEST;
            }
            case SOUTH_SOUTH_WEST: {
                return BlockFace.SOUTH;
            }
            case SOUTH: {
                return BlockFace.SOUTH_SOUTH_EAST;
            }
            case SOUTH_SOUTH_EAST: {
                return BlockFace.SOUTH_EAST;
            }
            case SOUTH_EAST: {
                return BlockFace.EAST_SOUTH_EAST;
            }
            case EAST_SOUTH_EAST: {
                return BlockFace.EAST;
            }
            case EAST: {
                return BlockFace.EAST_NORTH_EAST;
            }
            case EAST_NORTH_EAST: {
                return BlockFace.NORTH_EAST;
            }
            case NORTH_EAST: {
                return BlockFace.NORTH_NORTH_EAST;
            }
            case NORTH_NORTH_EAST: {
                return BlockFace.NORTH;
            }
            default: {
                return BlockFace.NORTH;
            }
        }
    }

    private boolean isGrayDye(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        final short data = itemStack.getDurability();
        final Material type = itemStack.getType();
        return type == Material.INK_SAC && data == 8;
    }

    private boolean isSkullBook(final ItemStack item) {
        if (item == null) {
            return false;
        }
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (!item.hasItemMeta()) {
                return false;
            }
            final BookMeta meta = (BookMeta) item.getItemMeta();
            if (meta.hasLore()) {
                final List<String> lore = meta.getLore();
                return lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book");
            }
        }
        return false;
    }

    private boolean skullLimit(final UUID UUID) {
        final Player player = Utils.getPlayerFromUUID(UUID);
        if (player == null) {
            return false;
        }
        if (this.plugin.hasPermission(player, "skullturret.admin")) {
            return false;
        }
        if (SkullTurret.DEBUG != 1 && this.plugin.playersSkullNumber.containsKey(UUID)) {
            final int skullCount = this.plugin.playersSkullNumber.get(UUID).getActiveSkulls();
            int maxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;
            final PerPlayerGroups ppg = this.plugin.getPlayerGroup(player);
            final PerPlayerSettings pps = this.plugin.perPlayerSettings.get(UUID);
            if (ppg != null && !player.isOp()) {
                maxTurrets = ppg.getMaxTurrets();
            } else if (pps != null && pps.isPps()) {
                maxTurrets = pps.getMaxTurrets();
            }
            if (skullCount >= maxTurrets) {
                if (ppg != null) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perGroupErr"), ppg.getGroupName(), maxTurrets)));
                } else if (pps != null) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perPlayerErr"), maxTurrets)));
                } else {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("maxSkullErr"), maxTurrets)));
                }
                return true;
            }
        }
        return false;
    }

    private boolean isRawSkull(final ItemStack skull) {
        return skull != null && skull.getType().equals(Material.SKELETON_SKULL) && !skull.getItemMeta().hasLore() && !skull.getItemMeta().hasDisplayName();
    }

    private boolean isRawBow(final ItemStack bow) {
        return bow != null && bow.getType().equals(Material.BOW) && !bow.getItemMeta().hasLore() && !bow.getItemMeta().hasDisplayName();
    }

    private boolean isSkullBow(final ItemStack bow) {
        if (this.isValidSkullTurretItem(bow) && bow.getType() == Material.BOW) {
            final List<String> lore = bow.getItemMeta().getLore();
            final String name = bow.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.bowLoreName) && name.equals(this.plugin.recipes.bowName);
        }
        return false;
    }

    private boolean isCrazedSkull(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.crazedLoreName) && name.equals(this.plugin.recipes.crazedName);
        }
        return false;
    }

    private boolean isDeviousSkull(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.deviousLoreName) && name.equals(this.plugin.recipes.deviousName);
        }
        return false;
    }

    private boolean isMasterSkull(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.masterLoreName) && name.equals(this.plugin.recipes.masterName);
        }
        return false;
    }

    private boolean isWizardSkull(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.wizardLoreName) && name.equals(this.plugin.recipes.wizardName);
        }
        return false;
    }

    private boolean isDeviousSkullTurret(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.mobileDeviousLoreName) && name.equals(this.plugin.recipes.mobileDeviousName);
        }
        return false;
    }

    private boolean isMasterSkullTurret(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String name = skull.getItemMeta().getDisplayName();
            return lore.contains(this.plugin.recipes.mobileMasterLoreName) && name.equals(this.plugin.recipes.mobileMasterName);
        }
        return false;
    }

    private int getMobileSkullAmmo(final ItemStack skull) {
        if (this.isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL) {
            final List<String> lore = skull.getItemMeta().getLore();
            final String[] ammoAmount = lore.get(1).split("=");
            final int amt = Integer.parseInt(ammoAmount[1]);
            return amt;
        }
        return 0;
    }

    private boolean isValidSkullTurretItem(final ItemStack turretItem) {
        return turretItem != null && turretItem.hasItemMeta() && turretItem.getItemMeta().hasLore() && turretItem.getItemMeta().hasDisplayName();
    }
}
