package plugin.arcwolf.skullturrets;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.Potion;
import org.bukkit.util.Vector;

import java.util.*;

public class STListeners implements Listener {
    private final SkullTurret plugin;

    public STListeners(SkullTurret plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Material ph = Material.LEGACY_PISTON_EXTENSION;
        Block block = event.getBlock();
        Material blockMeta = block.getType();
        if (blockMeta == ph && isMobileTurret(block))
            event.setCancelled(true);
    }

    private boolean isMobileTurret(Block block) {
        Block skullBlock = block.getRelative(BlockFace.UP);
        if (skullBlock.hasMetadata("SkullTurretPlace"))
            return true;
        Location location = skullBlock.getLocation();
        return this.plugin.skullMap.containsKey(location);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (event.isCancelled())
            return;
        Entity ent = event.getEntity();
        if (ent instanceof LivingEntity && ent.getType() == EntityType.ENDERMAN) {
            LivingEntity le = (LivingEntity) ent;
            if (!le.isDead()) {
                List<MetadataValue> targetedMeta = le.getMetadata("SkullTurretEnder");
                if (targetedMeta.size() > 0) {
                    Object obj = targetedMeta.get(0).value();
                    if (obj instanceof Potion) {
                        Potion potion = (Potion) obj;
                        le.damage(potion.getLevel() * 6.0D);
                        le.getWorld().playEffect(event.getFrom(), Effect.POTION_BREAK, potion);
                        le.removeMetadata("SkullTurretEnder", this.plugin);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExposionPrime(ExplosionPrimeEvent event) {
        if (event.isCancelled())
            return;
        if (SkullTurret.DEBUG == 10 &&
                !SkullTurret.MOB_LOOT) {
            Entity expEnt = event.getEntity();
            if (expEnt.getType() == EntityType.WITHER_SKULL && expEnt.hasMetadata("SkullTurretsSMART")) {
                List<Entity> entities = expEnt.getNearbyEntities(3.0D, 3.0D, 3.0D);
                for (Entity noDrop : entities) {
                    if (noDrop.getType() != EntityType.PLAYER && !noDrop.hasMetadata("SkullTurretsNODROP"))
                        noDrop.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled())
            return;
        if (event.getDamager() != null) {
            Entity firedRound = event.getDamager();
            Entity damagee = event.getEntity();
            if (damagee.getType() == EntityType.FIREBALL && SkullTurret.DEBUG == 99) {
                List<MetadataValue> targetedMeta = firedRound.getMetadata("SkullTurretantiFireball");
                if (targetedMeta.size() > 0) {
                    Fireball fb = (Fireball) damagee;
                    Vector oldV = fb.getDirection();
                    Vector newV = new Vector(-oldV.getX(), -oldV.getY(), -oldV.getZ());
                    fb.setDirection(newV);
                    System.out.println("Hit fireball");
                    return;
                }
            }
            if (damagee instanceof LivingEntity && !damagee.isDead()) {
                List<MetadataValue> targetedMeta = firedRound.getMetadata("SkullTurretsArrow");
                if (targetedMeta.size() > 0) {
                    event.setDamage(0.0D);
                    UUID playerUUID = UUID.fromString(targetedMeta.get(0).asString());
                    if (event.getEntity().getType() == EntityType.PLAYER) {
                        Player player = (Player) damagee;
                        if (player.getUniqueId().equals(playerUUID))
                            return;
                        if (this.plugin.hasPermission(player, "skullturret.ignoreme"))
                            return;
                    }
                    damagee.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(playerUUID, System.currentTimeMillis())));
                    Player shooter = Utils.getPlayerFromUUID(playerUUID);
                    if (SkullTurret.SKULLSFX && shooter != null && shooter.isOnline())
                        shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, 0.1F);
                }
                targetedMeta = firedRound.getMetadata("SkullTurretWizard");
                if (targetedMeta.size() > 0) {
                    Object obj = targetedMeta.get(0).value();
                    if (obj instanceof Potion) {
                        Potion potion = (Potion) obj;
                        if (potion != null) {
                            event.setCancelled(true);
                            LivingEntity le = (LivingEntity) damagee;
                            potion.apply(le);
                            damagee.getWorld().playEffect(damagee.getLocation(), Effect.POTION_BREAK, potion);
                            targetedMeta = firedRound.getMetadata("SkullTurretsSMART");
                            if (targetedMeta.size() > 0 &&
                                    firedRound instanceof org.bukkit.entity.EnderPearl)
                                firedRound.setMetadata("SkullTurretsWizardHitSuccess", new FixedMetadataValue(SkullTurret.plugin, le));
                            firedRound.remove();
                            if (!SkullTurret.MOB_LOOT && damagee.getType() != EntityType.PLAYER && !damagee.hasMetadata("SkullTurretsNODROP"))
                                damagee.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                            return;
                        }
                    }
                }
                targetedMeta = firedRound.getMetadata("SkullTurretDAMAGE");
                if (targetedMeta.size() > 0) {
                    Object obj = targetedMeta.get(0).value();
                    if (obj instanceof PlacedSkull) {
                        PlacedSkull pc = (PlacedSkull) obj;
                        if (firedRound.getType() == EntityType.ARROW)
                            event.setDamage(pc.getIntelligence().getDamageMod());
                    }
                }
                targetedMeta = firedRound.getMetadata("SkullTurretsNODROP");
                if (targetedMeta.size() > 0) {
                    Object obj = targetedMeta.get(0).value();
                    if (obj instanceof PlacedSkull &&
                            !SkullTurret.MOB_LOOT && damagee.getType() != EntityType.PLAYER && !damagee.hasMetadata("SkullTurretsNODROP"))
                        damagee.setMetadata("SkullTurretsNODROP", new FixedMetadataValue(SkullTurret.plugin, ""));
                }
            }
        }
    }

    @EventHandler
    public void onEntityShotBow(EntityShootBowEvent event) {
        if (event.isCancelled())
            return;
        if (event.getEntity() != null && event.getEntity().getType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            ItemStack bow = event.getBow();
            if (bow.hasItemMeta()) {
                ItemMeta meta = bow.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore.size() != 0 && lore.get(0).contains("Skull Target Bow")) {
                        if (!this.plugin.hasPermission(player, "skullturret.use.bow")) {
                            event.setCancelled(true);
                            player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionSkullBow")));
                        }
                        Entity arrow = event.getProjectile();
                        arrow.setMetadata("SkullTurretsArrow", new FixedMetadataValue(SkullTurret.plugin, player.getUniqueId().toString()));
                    }
                }
            } else if (SkullTurret.ALLOW_ARROW_DAMAGE && this.plugin.hasPermission(player, "skullturret.attack.bow")) {
                Entity arrow = event.getProjectile();
                arrow.setMetadata("SkullTurretsArrowForce", new FixedMetadataValue(SkullTurret.plugin, Float.valueOf(event.getForce())));
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled())
            return;
        Location loc = event.getToBlock().getLocation();
        if (this.plugin.skullMap.containsKey(loc))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.isCancelled())
            return;
        ItemStack item = event.getCursor();
        if (event.getInventory() != null && event.getInventory().getType() == InventoryType.MERCHANT) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK && event.getRawSlot() < 3 && event.getRawSlot() >= 0) {
                if (!item.hasItemMeta())
                    return;
                BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book"))
                        event.setCancelled(true);
                }
            }
        } else if (event.getInventory() != null && event.getInventory().getType() == InventoryType.CRAFTING) {
            final ItemStack result = event.getInventory().getItem(0);
            if (isDeviousSkullTurret(result) || isMasterSkullTurret(result)) {
                final Player player = (Player) event.getWhoClicked();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                if (event.getRawSlot() != 0)
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            CraftingInventory ci = (CraftingInventory) event.getInventory();
                            cp.ammoAmount = STListeners.this.countAmmo(ci);
                            STListeners.this.updateMobileTurretAmmoAmount(result, cp.ammoAmount);
                            player.updateInventory();
                        }
                    } 1L)
              if (event.getRawSlot() == 0 && player.getItemOnCursor() != null && player.getItemOnCursor().getType().equals(Material.AIR))
                    cp.clearArrows = true;
            }
        } else if (event.getInventory() != null && event.getInventory().getType() == InventoryType.WORKBENCH) {
            final ItemStack result = event.getInventory().getItem(0);
            if (isDeviousSkullTurret(result) || isMasterSkullTurret(result)) {
                final Player player = (Player) event.getWhoClicked();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                if (isDeviousSkullTurret(result) && !this.plugin.hasPermission(player, "skullturret.create.tempdevious")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    cp.clearArrows = false;
                    cp.ammoAmount = 0;
                    return;
                }
                if (isMasterSkullTurret(result) && !this.plugin.hasPermission(player, "skullturret.create.tempmaster")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    cp.clearArrows = false;
                    cp.ammoAmount = 0;
                    return;
                }
                if (event.getRawSlot() != 0)
                    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                        public void run() {
                            CraftingInventory ci = (CraftingInventory) event.getInventory();
                            cp.ammoAmount = STListeners.this.countAmmo(ci);
                            STListeners.this.updateMobileTurretAmmoAmount(result, cp.ammoAmount);
                            player.updateInventory();
                        }
                    } 1L)
              if (event.getRawSlot() == 0 && player.getItemOnCursor() != null && player.getItemOnCursor().getType().equals(Material.AIR))
                    cp.clearArrows = true;
            }
        }
    }

    private int countAmmo(CraftingInventory ci) {
        int ammoCount = 0;
        for (int i = 0; i < (ci.getMatrix()).length; i++) {
            ItemStack item = ci.getMatrix()[i];
            if (item != null && item.getType().equals(Material.ARROW))
                ammoCount += item.getAmount();
        }
        return ammoCount;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled())
            return;
        ItemStack item = event.getOldCursor();
        if (event.getInventory() != null && event.getInventory().getType() == InventoryType.MERCHANT) {
            boolean trade = false;
            for (Integer i : event.getRawSlots()) {
                if (i.intValue() < 3 && i.intValue() >= 0) {
                    trade = true;
                    break;
                }
            }
            if (item != null && item.getType() == Material.WRITTEN_BOOK && trade) {
                if (!item.hasItemMeta())
                    return;
                BookMeta meta = (BookMeta) item.getItemMeta();
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book"))
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.clearPlayer();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PerPlayerSettings pps = this.plugin.perPlayerSettings.get(uuid);
        if (pps != null && pps.isPps()) {
            PerPlayerGroups ppg = this.plugin.getPlayerGroup(uuid);
            if (ppg != null && !player.isOp()) {
                pps.setPps(false);
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": detected, " + player.getName() + " is in both a player group and has player settings.");
                SkullTurret.LOGGER.info(SkullTurret.pluginName + ": player settings for maxrange and skullnumber removed.");
                for (PlacedSkull skull : this.plugin.skullMap.values()) {
                    if (skull.getSkullCreator().equals(uuid)) {
                        int maxRange = ppg.getMaxRange();
                        double fireRange = ppg.getMaxRange() * skull.getIntelligence().getFireRangeMultiplier();
                        if (maxRange != skull.getMaxRange() || skull.getFireRange() != fireRange)
                            skull.reInitSkull();
                    }
                }
                (new DataStore(this.plugin)).savePerPlayerSettings();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.clearPlayer();
        List<MetadataValue> targetedMeta = event.getEntity().getMetadata("SkullTurretsTarget");
        if (targetedMeta.size() > 0)
            event.getEntity().removeMetadata("SkullTurretsTarget", this.plugin);
        List<ItemStack> drops = event.getDrops();
        if (SkullTurret.DROP_BOOKS_ON_DEATH)
            for (ItemStack stack : drops) {
                if (isSkullBook(stack))
                    stack.setDurability((short) 10);
            }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        ItemStack item = event.getEntity().getItemStack();
        if (isSkullBook(item)) {
            if (item.getDurability() == 10) {
                item.setDurability((short) 0);
                return;
            }
            Location loc = event.getLocation();
            World world = loc.getWorld();
            world.playEffect(loc, Effect.ENDER_SIGNAL, 0);
            world.playEffect(loc, Effect.EXTINGUISH, 0);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled())
            return;
        Block b = event.getBlock();
        if (!testLocationForRemoval(b.getLocation()))
            testLocationForRemoval(b.getRelative(BlockFace.UP).getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        for (Block b : event.blockList()) {
            if (!testLocationForRemoval(b.getLocation()))
                testLocationForRemoval(b.getRelative(BlockFace.UP).getLocation());
        }
    }

    private boolean testLocationForRemoval(Location loc) {
        if (this.plugin.skullMap.containsKey(loc)) {
            Utils.clearBlock(loc.getBlock());
            loc.getWorld().dropItemNaturally(loc, this.plugin.skullMap.get(loc).getIntelligence().getSkullItem());
            UUID playerUUID = this.plugin.skullMap.get(loc).getSkullCreator();
            subPlayerSkullCount(playerUUID);
            this.plugin.skullMap.remove(loc);
            return true;
        }
        return false;
    }

    private void subPlayerSkullCount(UUID playerUUID) {
        if (this.plugin.playersSkullNumber.containsKey(playerUUID)) {
            SkullCounts sc = this.plugin.playersSkullNumber.get(playerUUID);
            int numSkulls = sc.getActiveSkulls();
            if (numSkulls > 1) {
                numSkulls--;
                sc.setActiveSkulls(numSkulls);
            } else {
                this.plugin.playersSkullNumber.remove(playerUUID);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        if (event.isSticky()) {
            Location upFromFence = event.getRetractLocation().getBlock().getRelative(BlockFace.UP).getLocation();
            if (this.plugin.skullMap.containsKey(upFromFence))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        BlockFace face = event.getDirection();
        List<Block> blocks = event.getBlocks();
        int size = blocks.size();
        if (size == 0) {
            Location skullTest = event.getBlock().getRelative(face).getLocation();
            if (this.plugin.skullMap.containsKey(skullTest))
                event.setCancelled(true);
        } else {
            Block lastBlock = blocks.get(size - 1);
            Location skullTest = lastBlock.getRelative(face).getLocation();
            if (this.plugin.skullMap.containsKey(skullTest))
                event.setCancelled(true);
            for (Block b : event.getBlocks()) {
                Location upFromFence = b.getRelative(BlockFace.UP).getLocation();
                if (this.plugin.skullMap.containsKey(upFromFence)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof LivingEntity && livingEntity.getType() != EntityType.PLAYER) {
            if (livingEntity.hasMetadata("SkullTurretsNODROP") &&
                    !SkullTurret.MOB_LOOT) {
                event.getDrops().clear();
                event.setDroppedExp(0);
                return;
            }
            if (!livingEntity.hasMetadata("SkullTurretsHit")) {
                if (SkullTurret.MOB_DROPS &&
                        livingEntity.getType() == EntityType.SKELETON) {
                    if (SkullTurret.NO_DROP_WORLDS.contains(livingEntity.getWorld().getName()))
                        return;
                    int random = 1;
                    try {
                        random = (int) Math.ceil(Math.abs(SkullTurret.DROP_CHANCE) / 2.0D);
                    } catch (Exception exception) {
                    }
                    int chance = (new Random()).nextInt(SkullTurret.DROP_CHANCE) + 1;
                    if (random == chance)
                        event.getDrops().add(new ItemStack(Material.SKELETON_SKULL, 1));
                }
                return;
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        final projectile e = event.getEntity();
        List<MetadataValue> meta = projectile.getMetadata("SkullTurretsSMART");
        List<MetadataValue> wizMeta = projectile.getMetadata("SkullTurretsWizardHitSuccess");
        List<MetadataValue> forceMeta = projectile.getMetadata("SkullTurretsArrowForce");
        if (projectile instanceof Arrow &&
                forceMeta.size() > 0) {
            final float force = ((Float) forceMeta.get(0).value()).floatValue();
            this.plugin.scheduler.runTaskLater((Plugin) this.plugin, new Runnable() {
                public void run() {
                    Arrow a = (Arrow) e;
                    if (a.isOnGround() &&
                            a.getShooter() != null && a.getShooter() instanceof Player) {
                        Location loc = e.getLocation().getBlock().getLocation();
                        long cTime = System.currentTimeMillis();
                        PlacedSkull pc = STListeners.this.plugin.skullMap.get(loc);
                        if (pc == null)
                            return;
                        double skullHealth = pc.getHealth();
                        Player shooter = (Player) a.getShooter();
                        if (shooter.getGameMode() == GameMode.CREATIVE)
                            return;
                        double damage = (force * 9.0F);
                        UUID createrUUID = pc.getSkullCreator();
                        if (skullHealth > damage) {
                            pc.setHealth(skullHealth - damage);
                            if (SkullTurret.SKULLSFX) {
                                shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1F, 0.1F);
                            } else {
                                shooter.sendMessage(ChatColor.RED + "A hit!");
                            }
                            pc.setRecoveryTimer(cTime);
                            if (SkullTurret.SKULLS_RETALIATE)
                                shooter.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                            if (SkullTurret.SKULLSFX)
                                shooter.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_HURT, 0.5F, 0.4F);
                            if (SkullTurret.DEBUG == 5)
                                shooter.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.RED + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.RED + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                        } else if (!pc.isDying()) {
                            pc.setHealth(0.0D);
                            pc.setDestructTimer(cTime);
                            pc.setDying(true);
                            pc.doDeathRattle(cTime, cTime, SkullTurret.DESTRUCT_TIMER);
                            if (SkullTurret.SKULLSFX)
                                shooter.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_DEATH, 0.5F, 0.4F);
                            if (SkullTurret.SKULLS_RETALIATE)
                                shooter.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                        }
                        if (SkullTurret.DEBUG == 5)
                            System.out.println(shooter.getName() + " shot with a force of " + force + " damage of " + (force * 9.0F));
                    }
                }
            } 2L)
        }
        if (meta.size() > 0) {
            final Object obj = meta.get(0).value();
            if (obj instanceof PlacedSkull) {
                PlacedSkull pc = (PlacedSkull) obj;
                if (wizMeta.size() > 0) {
                    Object objw = wizMeta.get(0).value();
                    if (projectile instanceof org.bukkit.entity.EnderPearl && objw instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) objw;
                        le.removeMetadata("SkullTurretsSMART", this.plugin);
                        Projectile.removeMetadata("SkullTurretsWizardHitSuccess", (Plugin) this.plugin);
                    }
                } else if (projectile instanceof org.bukkit.entity.EnderPearl) {
                    LivingEntity livingEntity = pc.getTarget();
                    if (livingEntity != null) {
                        Integer hash = Integer.valueOf(pc.hashCode());
                        livingEntity.setMetadata(hash.toString(), new FixedMetadataValue(SkullTurret.plugin, Long.valueOf(System.currentTimeMillis())));
                        livingEntity.removeMetadata("SkullTurretsSMART", this.plugin);
                        pc.setTarget(null);
                    }
                }
            }
        }
        if (meta.size() > 0) {
            final Object obj = meta.get(0).value();
            this.plugin.scheduler.runTaskLater((Plugin) this.plugin, new Runnable() {
                public void run() {
                    boolean onGround = false;
                    if (e instanceof Arrow) {
                        Arrow a = (Arrow) e;
                        onGround = a.isOnGround();
                    }
                    if (obj instanceof PlacedSkull)
                        if (onGround) {
                            PlacedSkull pc = (PlacedSkull) obj;
                            LivingEntity livingEntity = pc.getTarget();
                            if (livingEntity != null) {
                                Integer hash = Integer.valueOf(pc.hashCode());
                                livingEntity.setMetadata(hash.toString(), new FixedMetadataValue(SkullTurret.plugin, Long.valueOf(System.currentTimeMillis())));
                                livingEntity.removeMetadata("SkullTurretsSMART", STListeners.this.plugin);
                            }
                            pc.setTarget(null);
                        } else if (SkullTurret.MOB_DROPS) {
                            PlacedSkull pc = (PlacedSkull) obj;
                            LivingEntity livingEntity = pc.getTarget();
                            if (livingEntity != null)
                                livingEntity.setMetadata("SkullTurretsHit", new FixedMetadataValue(SkullTurret.plugin, Long.valueOf(System.currentTimeMillis())));
                        }
                }
            } 2L)
        }
    }

    private void damageSkull(Player player, long cTime, CustomPlayer cp) {
        if (cp.skull_to_damage != null && (!cp.skull_to_damage.getSkullCreator().equals(player.getUniqueId()) || SkullTurret.DEBUG == 5)) {
            cp.attackTimer = cTime;
            PlacedSkull pc = cp.skull_to_damage;
            double skullHealth = pc.getHealth();
            ItemStack weapon = player.getItemInHand();
            if (weapon != null) {
                Material weapType = weapon.getType();
                if (this.plugin.weapons.containsKey(weapType) && this.plugin.hasPermission(player, "skullturret.attack.weapon")) {
                    double damage = this.plugin.weapons.get(weapType).doubleValue();
                    UUID createrUUID = pc.getSkullCreator();
                    if (skullHealth > damage) {
                        pc.setHealth(skullHealth - damage);
                        pc.setRecoveryTimer(cTime);
                        if (SkullTurret.SKULLS_RETALIATE)
                            player.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                        if (SkullTurret.SKULLSFX)
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_HURT, 0.5F, 0.4F);
                        if (SkullTurret.DEBUG == 5)
                            player.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.RED + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.RED + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                    } else if (!pc.isDying()) {
                        pc.setHealth(0.0D);
                        pc.setDestructTimer(cTime);
                        pc.setDying(true);
                        pc.doDeathRattle(cTime, cTime, SkullTurret.DESTRUCT_TIMER);
                        if (SkullTurret.SKULLSFX)
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_SILVERFISH_DEATH, 0.5F, 0.4F);
                        if (SkullTurret.SKULLS_RETALIATE)
                            player.setMetadata("SkullTurretsTarget", new FixedMetadataValue(SkullTurret.plugin, new BowTargetInfo(createrUUID, System.currentTimeMillis())));
                    }
                } else if (pc.getIntelligence().getRepair_item().equals(weapType) && this.plugin.hasPermission(player, "skullturret.repair")) {
                    double repairAmt = SkullTurret.SKULL_REPAIR_AMOUNT;
                    if (pc.getHealth() < pc.getIntelligence().getHealth()) {
                        pc.setHealth(skullHealth + repairAmt);
                        if (pc.getHealth() >= pc.getIntelligence().getHealth()) {
                            pc.setDying(false);
                            pc.setHealth(pc.getIntelligence().getHealth());
                            removeRepairItem(weapon, player);
                        }
                        if (SkullTurret.SKULLSFX)
                            player.getWorld().playSound(pc.getCenterPoint(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 0.5F);
                        if (SkullTurret.DEBUG == 5)
                            player.sendMessage(ChatColor.AQUA + pc.getIntelligence().getNormalName() + ChatColor.GREEN + " Skull's Health is " + ChatColor.GOLD + pc.getHealth() + ChatColor.GREEN + " of " + ChatColor.GOLD + pc.getIntelligence().getHealth());
                    } else {
                        pc.setDying(false);
                        pc.setHealth(pc.getIntelligence().getHealth());
                        int amount = weapon.getAmount();
                        if (amount > 1) {
                            removeRepairItem(weapon, player);
                        } else {
                            removeRepairItem(weapon, player);
                        }
                    }
                }
            }
        }
    }

    private void removeRepairItem(ItemStack weapon, Player player) {
        int amount = weapon.getAmount();
        if (amount > 1) {
            weapon.setAmount(amount - 1);
            player.updateInventory();
        } else {
            player.setItemInHand(new ItemStack(Material.AIR));
            player.updateInventory();
        }
    }

    @EventHandler
    public void onBlockDamageEvent(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        CustomPlayer cp = CustomPlayer.getSettings(player);
        if (SkullTurret.ALLOW_SKULL_DAMAGE) {
            long cTime = System.currentTimeMillis();
            PlacedSkull pc = this.plugin.skullMap.get(loc);
            if (pc == null) {
                cp.skull_to_damage = null;
                return;
            }
            if (SkullTurret.OFFLINE_PLAYERS) {
                Player playerOwner = Utils.getPlayerFromUUID(pc.getSkullCreator());
                if (playerOwner == null) {
                    cp.skull_to_damage = null;
                    return;
                }
            }
            if (!pc.getSkullCreator().equals(player.getUniqueId()) || SkullTurret.DEBUG == 5) {
                ItemStack weapon = player.getItemInHand();
                Material weapType = weapon.getType();
                if (this.plugin.weapons.containsKey(weapType) || pc.getIntelligence().getRepair_item().equals(weapType)) {
                    event.setCancelled(true);
                    cp.skull_to_damage = pc;
                    if (cTime - cp.attackTimer > 1700L) {
                        damageSkull(player, cTime, cp);
                        cp.attackTimer = cTime;
                    }
                }
            } else {
                cp.skull_to_damage = null;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled())
            return;
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        CustomPlayer cp = CustomPlayer.getSettings(player);
        if (cp.skull_to_damage != null && !this.plugin.skullMap.containsKey(block.getLocation())) {
            cp.skull_to_damage = null;
            cp.attackTimer = 0L;
        }
        if (cp.command.equals("skedit") && block != null && this.plugin.skullMap.containsKey(block.getLocation()) && block.getType().equals(Material.SKELETON_SKULL) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (this.plugin.hasPermission(player, "skullturret.edit")) {
                PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
                if (pc.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin")) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullSelected")));
                    cp.pc = pc;
                } else {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullOwner"), pc.getIntelligence().getNormalName(), pc.getSkullCreatorLastKnowName().isEmpty() ? pc.getSkullCreator() : pc.getSkullCreatorLastKnowName())));
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullSelectFail")));
                    return;
                }
            } else {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noSelectPermission")));
            }
        } else if (cp.command.equals("skrotate")) {
            if (block.getType().equals(Material.SKELETON_SKULL) && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
                if (pc == null)
                    return;
                if (pc.doPatrol()) {
                    player.sendMessage(Utils.parseText(Utils.getLocalization("rotateFail")));
                    return;
                }
                if (pc.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin")) {
                    Skull skull = pc.getSkull();
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        pc.setSkullRotation(clockwiseRotation(skull));
                    } else {
                        pc.setSkullRotation(counterClockwiseRotation(skull));
                    }
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("rotateSuccess"), skull.getRotation().name())));
                } else {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullOwner"), pc.getIntelligence().getNormalName(), pc.getSkullCreatorLastKnowName().isEmpty() ? pc.getSkullCreator() : pc.getSkullCreatorLastKnowName())));
                    player.sendMessage(Utils.parseText(Utils.getLocalization("rotateFailB")));
                    return;
                }
            }
        } else if (this.plugin.skullMap.containsKey(block.getLocation()) && block.getType().equals(Material.SKELETON_SKULL)) {
            ItemStack item = event.getItem();
            PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
            if (pc == null)
                return;
            if (pc instanceof MobileSkull && player.getItemInHand().getType().equals(Material.ARROW)) {
                MobileSkull ms = (MobileSkull) pc;
                int ammoAmount = ms.getAmmoAmount();
                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("arrowsRemain"), Integer.valueOf(ms.getAmmoAmount()))));
                    event.setCancelled(true);
                    return;
                }
                if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (!SkullTurret.ALLOW_TEMPTURRET_REARM) {
                        player.sendMessage(Utils.parseText(Utils.getLocalization("rearmFail")));
                        return;
                    }
                    int clickedAmmo = player.getItemInHand().getAmount();
                    if (ammoAmount + clickedAmmo <= 448) {
                        ms.setAmmoAmount(ammoAmount + clickedAmmo);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.setItemInHand(new ItemStack(Material.AIR));
                            updateInventory(player);
                        }
                    } else {
                        int subAmmo = 448 - ammoAmount;
                        int inhand = player.getItemInHand().getAmount();
                        ms.setAmmoAmount(448);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            player.getItemInHand().setAmount(inhand - subAmmo);
                            updateInventory(player);
                        }
                    }
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("arrowsRemain"), Integer.valueOf(ms.getAmmoAmount()))));
                    event.setCancelled(true);
                    return;
                }
            }
            if (cp.command.equalsIgnoreCase("default") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (pc.getSkullCreator().equals(player.getUniqueId())) {
                    SkullIntelligence skintel = pc.getIntelligence();
                    if (skintel != SkullIntelligence.MASTER && skintel != SkullIntelligence.WIZARD) {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultErr"), pc.getIntelligence().getNormalName())));
                        return;
                    }
                    PerPlayerSettings ppsDefaults = this.plugin.perPlayerSettings.get(player.getUniqueId());
                    if (ppsDefaults != null) {
                        if (skintel == SkullIntelligence.MASTER && isMasterSkull(player.getItemInHand())) {
                            ppsDefaults.setAmmoTypeName(pc.getAmmoType().name());
                            ppsDefaults.setMasterSkinName(pc.getSkinData());
                            ppsDefaults.setMasterPatrol(pc.doPatrol());
                            ppsDefaults.setMasterRedstone(pc.isRedstone());
                            if (ppsDefaults.isMasterDefaults()) {
                                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultOverwrite"), pc.getIntelligence().getNormalName())));
                            } else {
                                ppsDefaults.setMasterDefaults(true);
                            }
                            (new DataStore(this.plugin)).savePerPlayerSettings();
                        } else if (skintel == SkullIntelligence.WIZARD && isWizardSkull(player.getItemInHand())) {
                            ppsDefaults.setWizardSkinName(pc.getSkinData());
                            ppsDefaults.setWizardPatrol(pc.doPatrol());
                            ppsDefaults.setWizardRedstone(pc.isRedstone());
                            if (ppsDefaults.isWizardDefaults()) {
                                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultOverwrite"), pc.getIntelligence().getNormalName())));
                            } else {
                                ppsDefaults.setWizardDefaults(true);
                            }
                            (new DataStore(this.plugin)).savePerPlayerSettings();
                        }
                    } else if (skintel == SkullIntelligence.MASTER && isMasterSkull(player.getItemInHand())) {
                        ppsDefaults = new PerPlayerSettings(player.getUniqueId(), pc.getSkinData(), pc.getAmmoType().name(), pc.isRedstone(), pc.doPatrol());
                        this.plugin.perPlayerSettings.put(player.getUniqueId(), ppsDefaults);
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultSuccess"), pc.getIntelligence().getNormalName())));
                    } else if (skintel == SkullIntelligence.WIZARD && isWizardSkull(player.getItemInHand())) {
                        ppsDefaults = new PerPlayerSettings(player.getUniqueId(), pc.getSkinData(), pc.isRedstone(), pc.doPatrol());
                        this.plugin.perPlayerSettings.put(player.getUniqueId(), ppsDefaults);
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultSuccess"), pc.getIntelligence().getNormalName())));
                    }
                } else {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultErr"), pc.getIntelligence().getNormalName())));
                    return;
                }
                return;
            }
            if (cp.command.equalsIgnoreCase("resetDefault") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                PerPlayerSettings ppsDefaults = this.plugin.perPlayerSettings.get(player.getUniqueId());
                if (ppsDefaults != null) {
                    SkullIntelligence skintel = pc.getIntelligence();
                    if (skintel == SkullIntelligence.MASTER && isMasterSkull(player.getItemInHand())) {
                        if (ppsDefaults.isMasterDefaults()) {
                            ppsDefaults.setMasterDefaults(false);
                            ppsDefaults.cleanUpPPS();
                            cp.clearPlayer();
                            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), pc.getIntelligence().getNormalName())));
                        }
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                    } else if (skintel == SkullIntelligence.WIZARD && isWizardSkull(player.getItemInHand())) {
                        if (ppsDefaults.isWizardDefaults()) {
                            ppsDefaults.setWizardDefaults(false);
                            ppsDefaults.cleanUpPPS();
                            cp.clearPlayer();
                            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("defaultsReset"), pc.getIntelligence().getNormalName())));
                        }
                        (new DataStore(this.plugin)).savePerPlayerSettings();
                    }
                }
                return;
            }
            if (!this.plugin.hasPermission(player, "skullturret.use")) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                event.setCancelled(true);
                return;
            }
            if (item != null && item.getType() == Material.WRITTEN_BOOK && (pc.getIntelligence() == SkullIntelligence.MASTER || pc.getIntelligence() == SkullIntelligence.WIZARD)) {
                pc.parseBook(item, player);
                (new DataStore(this.plugin)).saveDatabase(false);
            } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && player.getItemInHand().getType() == Material.AIR) {
                if ((pc.getIntelligence() == SkullIntelligence.MASTER || pc.getIntelligence() == SkullIntelligence.WIZARD) && (pc.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin"))) {
                    PlayerInventory playerInventory = player.getInventory();
                    ItemStack book = pc.getInfoBook(false, player);
                    if (alreadyHasBook(pc, player)) {
                        player.sendMessage(Utils.parseText(Utils.getLocalization("skullBookInvError")));
                        return;
                    }
                    playerInventory.addItem(book);
                    player.updateInventory();
                } else {
                    if ((pc.getIntelligence() == SkullIntelligence.DEVIOUS || pc.getIntelligence() == SkullIntelligence.CRAZED) && (pc.getSkullCreator().equals(player.getUniqueId()) || this.plugin.hasPermission(player, "skullturret.admin"))) {
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("skullBookNoBookErr"), pc.getIntelligence().getNormalName())));
                        return;
                    }
                    player.sendMessage(Utils.parseText(Utils.getLocalization("skullBookNotOwner")));
                }
            }
        }
    }

    private boolean alreadyHasBook(PlacedSkull clickedSkull, Player player) {
        Iterator<ItemStack> iter = player.getInventory().iterator();
        while (iter.hasNext()) {
            ItemStack item = iter.next();
            if (item == null || item.getType() != Material.WRITTEN_BOOK ||
                    !item.hasItemMeta())
                continue;
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (meta.hasLore() &&
                    meta.getLore().size() >= 2 && meta.getLore().get(1).equals("Skull Knowledge Book")) {
                PlacedSkull pc = null;
                try {
                    String page1 = meta.getPage(1);
                    if (page1.contains("BROKE"))
                        continue;
                    String[] split = page1.split(":");
                    String world = split[2].split("\n")[0].trim();
                    String[] coords = split[3].split("\n")[0].trim().split(",");
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    int z = Integer.parseInt(coords[2].trim());
                    World skullWorld = SkullTurret.plugin.getServer().getWorld(world);
                    if (skullWorld != null) {
                        Location skullLocation = new Location(skullWorld, x, y, z);
                        pc = SkullTurret.plugin.skullMap.get(skullLocation);
                        if (pc == clickedSkull)
                            return true;
                    }
                } catch (Exception exception) {
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onCraftItemEvent(CraftItemEvent event) {
        if (event.getRecipe() instanceof ShapelessRecipe) {
            ItemStack mds = this.plugin.recipes.mobileDeviousSkullItem;
            ItemStack mms = this.plugin.recipes.mobileMasterSkullItem;
            ShapelessRecipe eventRecipe = (ShapelessRecipe) event.getRecipe();
            ItemStack result = eventRecipe.getResult();
            if (result.equals(mds) || result.equals(mms)) {
                Player player = (Player) event.getView().getPlayer();
                final CustomPlayer cp = CustomPlayer.getSettings(player);
                final CraftingInventory ci = event.getInventory();
                if (cp.clearArrows)
                    this.plugin.getServer().getScheduler().runTaskLater((Plugin) this.plugin, new Runnable() {
                        public void run() {
                            int maxIndex = 0;
                            if (ci.getType() == InventoryType.CRAFTING) {
                                maxIndex = 4;
                            } else if (ci.getType() == InventoryType.WORKBENCH) {
                                maxIndex = 9;
                            }
                            for (int i = 1; i <= maxIndex; i++) {
                                if (ci.getItem(i) != null && ci.getItem(i).getType().equals(Material.ARROW))
                                    ci.setItem(i, new ItemStack(Material.AIR));
                            }
                            cp.clearArrows = false;
                        }
                    } 1L)
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        Player player = (Player) event.getView().getPlayer();
        String itemName = "";
        if (event.getRecipe() instanceof ShapedRecipe) {
            ShapedRecipe eventRecipe = (ShapedRecipe) event.getRecipe();
            if (eventRecipe.getResult().equals(this.plugin.recipes.crazedSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.crazed")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Crazed Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!isRawSkull(event.getInventory().getMatrix()[4]))
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.deviousSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.devious")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Devious Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!isCrazedSkull(event.getInventory().getMatrix()[4]))
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.masterSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.master")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Master Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!isDeviousSkull(event.getInventory().getMatrix()[4]))
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.wizardSkullItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.wizard")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Wizard Skull";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!isMasterSkull(event.getInventory().getMatrix()[4])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                } else if (!isGrayDye(event.getInventory().getMatrix()[0])) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                }
                updateResultQuantity(player, event.getInventory().getContents()[0]);
            } else if (eventRecipe.getResult().equals(this.plugin.recipes.bowTargetItem)) {
                if (!this.plugin.hasPermission(player, "skullturret.create.bow")) {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    itemName = "Skull Bow";
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("noCreatePermission"), itemName)));
                    return;
                }
                if (!isRawBow(event.getInventory().getMatrix()[4]))
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                updateResultQuantity(player, event.getInventory().getContents()[0]);
            }
        } else if (event.getRecipe() instanceof ShapelessRecipe) {
            ShapelessRecipe eventRecipe = (ShapelessRecipe) event.getRecipe();
            CustomPlayer cp = CustomPlayer.getSettings(player);
            ItemStack mds = this.plugin.recipes.mobileDeviousSkullItem;
            ItemStack mms = this.plugin.recipes.mobileMasterSkullItem;
            ItemStack result = eventRecipe.getResult();
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
                CraftingInventory ci = event.getInventory();
                int ammoCount = 0;
                for (int i = 0; i < (ci.getMatrix()).length; i++) {
                    ItemStack item = ci.getMatrix()[i];
                    if (item != null) {
                        if (isDeviousSkull(item)) {
                            foundDevious = true;
                        } else if (isMasterSkull(item)) {
                            foundMaster = true;
                        } else if (isOnWoodFence(item.getType())) {
                            foundFence = true;
                        } else if (item.getType().equals(Material.CRIMSON_FENCE)) {
                            foundNetherFence = true;
                        }
                        if (item != null && item.getType().equals(Material.ARROW))
                            ammoCount += item.getAmount();
                    }
                }
                if ((foundDevious && foundFence) || (foundMaster && foundNetherFence)) {
                    if (ammoCount != cp.ammoAmount)
                        cp.ammoAmount = ammoCount;
                    if (ammoCount == 0)
                        cp.clearArrows = false;
                    if (cp.clearArrows) {
                        int maxIndex = 0;
                        if (ci.getType() == InventoryType.CRAFTING) {
                            maxIndex = 4;
                        } else if (ci.getType() == InventoryType.WORKBENCH) {
                            maxIndex = 9;
                        }
                        for (int j = 1; j <= maxIndex; j++) {
                            if (ci.getItem(j) != null && ci.getItem(j).getType().equals(Material.ARROW))
                                ci.setItem(j, new ItemStack(Material.AIR));
                        }
                        cp.clearArrows = false;
                    }
                    updateMobileTurretAmmoAmount(ci.getResult(), cp.ammoAmount);
                    updateInventory(player);
                } else {
                    event.getInventory().setItem(0, new ItemStack(Material.AIR));
                    updateInventory(player);
                }
            }
        }
    }

    private void updateResultQuantity(Player player, ItemStack item) {
        if (item == null)
            return;
        if (isCrazedSkull(item)) {
            item.setAmount(SkullTurret.CRAZED_STACK_SIZE);
        } else if (isDeviousSkull(item)) {
            item.setAmount(SkullTurret.DEVIOUS_STACK_SIZE);
        } else if (isMasterSkull(item)) {
            item.setAmount(SkullTurret.MASTER_STACK_SIZE);
        } else if (isWizardSkull(item)) {
            item.setAmount(SkullTurret.WIZARD_STACK_SIZE);
        } else if (isSkullBow(item)) {
            item.setAmount(SkullTurret.BOW_STACK_SIZE);
        }
        updateInventory(player);
    }

    private void updateInventory(final Player player) {
        final CustomPlayer cp = CustomPlayer.getSettings(player);
        if (!cp.invUpdate) {
            cp.invUpdate = true;
            this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
                public void run() {
                    player.updateInventory();
                    cp.invUpdate = false;
                }
            }, 1L);
        }
    }

    private boolean updateMobileTurretAmmoAmount(ItemStack item, int ammoCount) {
        if (item == null || item.getType().equals(Material.AIR))
            return false;
        String loreName = "";
        if (isDeviousSkullTurret(item)) {
            loreName = this.plugin.recipes.mobileDeviousLoreName;
        } else if (isMasterSkullTurret(item)) {
            loreName = this.plugin.recipes.mobileMasterLoreName;
        } else {
            return false;
        }
        ItemMeta itmeta = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(loreName);
        lore.add("Ammo=" + ammoCount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return true;
    }

    private boolean onFence(Block against) {
        return !(against.getType() != Material.OAK_FENCE && against.getType() != Material.SPRUCE_FENCE && against.getType() != Material.ACACIA_FENCE &&
                against.getType() != Material.BIRCH_FENCE && against.getType() != Material.JUNGLE_FENCE && against.getType() != Material.DARK_OAK_FENCE &&
                against.getType() != Material.CRIMSON_FENCE);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        final Block block = event.getBlock();
        final Block against = event.getBlockAgainst();
        final ItemStack skull = event.getItemInHand();
        final Player player = event.getPlayer();
        CustomPlayer cp = CustomPlayer.getSettings(player);
        cp.skull_to_damage = null;
        cp.attackTimer = 0L;
        if (isDeviousSkullTurret(skull) || isMasterSkullTurret(skull)) {
            if (!this.plugin.hasPermission(player, "skullturret.use.tempturret")) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                event.setCancelled(true);
                return;
            }
            if (!isOnGround(block, event.getBlockAgainst())) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("placeOnGroundErr")));
                event.setCancelled(true);
                return;
            }
            final Block skullBlock = placeSkullTurret(cp, player, block);
            if (skullBlock == null) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("invalidPosition")));
                event.setCancelled(true);
                return;
            }
            final UUID playerUUID = player.getUniqueId();
            this.plugin.scheduler.runTaskLater((Plugin) this.plugin, new Runnable() {
                public void run() {
                    if (skullBlock.getType().equals(Material.SKELETON_SKULL)) {
                        if (STListeners.this.skullLimit(playerUUID)) {
                            Block block = event.getBlock();
                            Utils.clearBlock(block);
                            Utils.clearBlock(skullBlock);
                            skullBlock.removeMetadata("SkullTurretPlace", STListeners.this.plugin);
                            SkullIntelligence skullIntelligence = SkullIntelligence.CRAZED;
                            if (STListeners.this.isDeviousSkullTurret(skull)) {
                                skullIntelligence = SkullIntelligence.DEVIOUS;
                            } else if (STListeners.this.isMasterSkullTurret(skull)) {
                                skullIntelligence = SkullIntelligence.MASTER;
                            } else {
                                skullIntelligence = SkullIntelligence.DEVIOUS;
                            }
                            ItemStack skullItem = skullIntelligence.getMobileSkullItem();
                            STListeners.this.updateMobileTurretAmmoAmount(skullItem, STListeners.this.getMobileSkullAmmo(skull));
                            if (player.getGameMode() != GameMode.CREATIVE)
                                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), skullItem);
                            return;
                        }
                        SkullIntelligence skullType = SkullIntelligence.CRAZED;
                        if (STListeners.this.isDeviousSkullTurret(skull)) {
                            skullType = SkullIntelligence.DEVIOUS;
                        } else if (STListeners.this.isMasterSkullTurret(skull)) {
                            skullType = SkullIntelligence.MASTER;
                        }
                        int maxRange = SkullTurret.MAX_RANGE;
                        PerPlayerGroups ppg = STListeners.this.plugin.getPlayerGroup(playerUUID);
                        PerPlayerSettings pps = STListeners.this.plugin.perPlayerSettings.get(playerUUID);
                        if (ppg != null && !player.isOp()) {
                            maxRange = ppg.getMaxRange();
                        } else if (pps != null && pps.isPps()) {
                            maxRange = STListeners.this.plugin.perPlayerSettings.get(playerUUID).getMaxRange();
                        } else {
                            maxRange = SkullTurret.MAX_RANGE;
                        }
                        int ammoCount = STListeners.this.getMobileSkullAmmo(skull);
                        MobileSkull ms = new MobileSkull(skullBlock, event.getPlayer().getUniqueId(), maxRange, skullType, ammoCount);
                        ms.setSkullCreatorLastKnowName(player.getName());
                        STListeners.this.plugin.skullMap.put(skullBlock.getLocation(), ms);
                        player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("tempSkullAdded"), "Temporary " + skullType.getNormalName() + " skull")));
                        (new DataStore(STListeners.this.plugin)).saveDatabase(true);
                        if (STListeners.this.plugin.playersSkullNumber.containsKey(playerUUID)) {
                            SkullCounts sc = STListeners.this.plugin.playersSkullNumber.get(playerUUID);
                            int numSkulls = sc.getActiveSkulls();
                            numSkulls++;
                            sc.setActiveSkulls(numSkulls);
                        } else {
                            STListeners.this.plugin.playersSkullNumber.put(playerUUID, new SkullCounts(1, 0));
                        }
                        skullBlock.removeMetadata("SkullTurretPlace", STListeners.this.plugin);
                    }
                }
            } 10L)
          return;
        }
        if (block.getType().equals(Material.SKELETON_SKULL) && onFence(against)) {
            final UUID playerUUID = player.getUniqueId();
            if (!isCrazedSkull(skull) && !isDeviousSkull(skull) && !isMasterSkull(skull) && !isWizardSkull(skull))
                return;
            if (!isValidSkull(block)) {
                event.setCancelled(true);
                player.sendMessage(Utils.parseText(Utils.getLocalization("placeError")));
                return;
            }
            if (!this.plugin.hasPermission(player, "skullturret.use")) {
                player.sendMessage(Utils.parseText(Utils.getLocalization("noPermissionErrorB")));
                event.setCancelled(true);
                return;
            }
            if (skullLimit(playerUUID)) {
                event.setCancelled(true);
                return;
            }
            String str1 = "";
            String str2 = "";
            if ((isMasterSkull(skull) || isWizardSkull(skull)) && !block.getRelative(BlockFace.DOWN).getType().equals(Material.CRIMSON_FENCE)) {
                str1 = "Nether";
                str2 = isMasterSkull(skull) ? "Master Skulls" : "Wizard Skulls";
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), str2, str1)));
                event.setCancelled(true);
                return;
            }
            if ((isDeviousSkull(skull) || isCrazedSkull(skull)) && !isOnWoodFence(block.getRelative(BlockFace.DOWN).getType())) {
                str1 = "Wooden";
                str2 = isDeviousSkull(skull) ? "Devious Skulls" : "Crazed Skulls";
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), str2, str1)));
                event.setCancelled(true);
                return;
            }
            this.plugin.scheduler.runTaskLater((Plugin) this.plugin, new Runnable() {
                public void run() {
                    if (block.getType().equals(Material.SKELETON_SKULL) && STListeners.this.onFence(against)) {
                        if (STListeners.this.skullLimit(playerUUID)) {
                            Block block = event.getBlock();
                            Utils.clearBlock(block);
                            SkullIntelligence skullIntelligence = SkullIntelligence.CRAZED;
                            if (STListeners.this.isDeviousSkull(skull)) {
                                skullIntelligence = SkullIntelligence.DEVIOUS;
                            } else if (STListeners.this.isMasterSkull(skull)) {
                                skullIntelligence = SkullIntelligence.MASTER;
                            }
                            if (player.getGameMode() != GameMode.CREATIVE)
                                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), skullIntelligence.getSkullItem());
                            return;
                        }
                        SkullIntelligence skullType = SkullIntelligence.CRAZED;
                        if (STListeners.this.isDeviousSkull(skull)) {
                            skullType = SkullIntelligence.DEVIOUS;
                        } else if (STListeners.this.isMasterSkull(skull)) {
                            skullType = SkullIntelligence.MASTER;
                        } else if (STListeners.this.isWizardSkull(skull)) {
                            skullType = SkullIntelligence.WIZARD;
                        }
                        int maxRange = SkullTurret.MAX_RANGE;
                        PerPlayerGroups ppg = STListeners.this.plugin.getPlayerGroup(playerUUID);
                        PerPlayerSettings pps = STListeners.this.plugin.perPlayerSettings.get(playerUUID);
                        if (ppg != null && !player.isOp()) {
                            maxRange = ppg.getMaxRange();
                        } else if (pps != null && pps.isPps()) {
                            maxRange = STListeners.this.plugin.perPlayerSettings.get(playerUUID).getMaxRange();
                        } else {
                            maxRange = SkullTurret.MAX_RANGE;
                        }
                        PlacedSkull pc = new PlacedSkull(block, event.getPlayer().getUniqueId(), maxRange, skullType);
                        pc.setSkullCreatorLastKnowName(player.getName());
                        pc.setType(((Skull) block.getState()).getSkullType());
                        if (pc.getType().equals(SkullType.PLAYER))
                            pc.setSkin(((Skull) block.getState()).getOwner());
                        STListeners.this.plugin.skullMap.put(block.getLocation(), pc);
                        event.getPlayer().sendMessage(ChatColor.GREEN + skullType.getNormalName() + " skull added.");
                        (new DataStore(STListeners.this.plugin)).saveDatabase(true);
                        if (STListeners.this.plugin.playersSkullNumber.containsKey(playerUUID)) {
                            SkullCounts sc = STListeners.this.plugin.playersSkullNumber.get(playerUUID);
                            int numSkulls = sc.getActiveSkulls();
                            numSkulls++;
                            sc.setActiveSkulls(numSkulls);
                        } else {
                            STListeners.this.plugin.playersSkullNumber.put(playerUUID, new SkullCounts(1, 0));
                        }
                    }
                }
            } 10L)
        }
        if ((SkullTurret.fd.hasFactions() && FactionsDetector.wrongFaction(player, block.getLocation())) || (this.plugin.hasTowny() && wrongTown(player, block.getLocation()))) {
            event.setCancelled(true);
            player.sendMessage(Utils.parseText(Utils.getLocalization("placeError")));
            return;
        }
        String postType = "";
        String skullType = "";
        if ((isMasterSkull(skull) || isWizardSkull(skull)) && (!against.getType().equals(Material.CRIMSON_FENCE) || !isValidSkull(block))) {
            postType = "Nether";
            skullType = isMasterSkull(skull) ? "Master Skulls" : "Wizard Skulls";
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType, postType)));
            event.setCancelled(true);
            return;
        }
        if ((isDeviousSkull(skull) || isCrazedSkull(skull)) && (!isOnWoodFence(against.getType()) || !isValidSkull(block))) {
            postType = "Wooden";
            skullType = isDeviousSkull(skull) ? "Devious Skulls" : "Crazed Skulls";
            player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("placeErrorB"), skullType, postType)));
            event.setCancelled(true);
            return;
        }
    }

    private boolean isOnWoodFence(Material type) {
        return !(type != Material.OAK_FENCE && type != Material.SPRUCE_FENCE && type != Material.ACACIA_FENCE &&
                type != Material.BIRCH_FENCE && type != Material.JUNGLE_FENCE && type != Material.DARK_OAK_FENCE);
    }

    private boolean isOnGround(Block skullBlock, Block againstBlock) {
        return againstBlock.getRelative(BlockFace.UP).equals(skullBlock);
    }

    private boolean isValidSkull(Block skull) {
        return (skull.getType().equals(Material.SKELETON_SKULL) && skull.getData() == 1);
    }

    private Block placeSkullTurret(CustomPlayer cp, Player player, Block block) {
        Location skullBlock = block.getLocation();
        World world = block.getWorld();
        int blockY = skullBlock.getBlockY();
        int maxWorldHeight = world.getMaxHeight();
        if (blockY + 2 >= maxWorldHeight)
            return null;
        Location abovePlacedSkull = block.getRelative(BlockFace.UP).getLocation();
        if (!abovePlacedSkull.getBlock().isEmpty())
            return null;
        Block newSkullBlock = abovePlacedSkull.getBlock();
        newSkullBlock.setMetadata("SkullTurretPlace", new FixedMetadataValue(SkullTurret.plugin, ""));
        MaterialData skullMD = block.getState().getData();
        newSkullBlock.setType(block.getType());
        BlockFace skullFacing = ((Skull) block.getState()).getRotation();
        BlockState topState = newSkullBlock.getState();
        topState.setData(skullMD);
        ((Skull) topState).setRotation(skullFacing);
        topState.update();
        block.setType(Material.PISTON_EXTENSION);
        BlockState bottomState = block.getState();
        PistonExtensionMaterial pem = (PistonExtensionMaterial) bottomState.getData();
        pem.setFacingDirection(BlockFace.DOWN);
        bottomState.setData(pem);
        bottomState.update();
        return newSkullBlock;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Block block = event.getBlock();
        Block upBlock = event.getBlock().getRelative(BlockFace.UP);
        boolean fenceBreak = false;
        if (isOnWoodFence(block.getType()) || block.getType().equals(Material.CRIMSON_FENCE) || block.getType().equals(Material.PISTON_EXTENSION)) {
            block = upBlock;
            fenceBreak = true;
        }
        if (block.getType().equals(Material.SKELETON_SKULL)) {
            PlacedSkull pc = this.plugin.skullMap.get(block.getLocation());
            Player player = event.getPlayer();
            if (pc != null) {
                CustomPlayer cp = CustomPlayer.getSettings(player);
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
                if (SkullTurret.DEBUG > 0)
                    pc.clearDebug();
                UUID playerUUID = event.getPlayer().getUniqueId();
                if (cp.pc != null && cp.pc.equals(pc))
                    cp.pc = null;
                World world = block.getWorld();
                if (!player.getGameMode().equals(GameMode.CREATIVE) && SkullTurret.DROP)
                    if (pc instanceof MobileSkull) {
                        MobileSkull ms = (MobileSkull) pc;
                        ItemStack skullItem = pc.getIntelligence().getMobileSkullItem();
                        updateMobileTurretAmmoAmount(skullItem, ms.getAmmoAmount());
                        world.dropItemNaturally(pc.getLocation(), skullItem);
                    } else {
                        if (!fenceBreak) {
                            event.setCancelled(true);
                            Utils.clearBlock(block);
                        }
                        world.dropItemNaturally(pc.getLocation(), pc.getIntelligence().getSkullItem());
                        if (SkullTurret.DROP_BOOK_ON_BREAK && SkullTurret.DROP) {
                            ItemStack book = pc.getInfoBook(true, player);
                            if (book != null) {
                                book.setDurability((short) 10);
                                world.dropItemNaturally(pc.getLocation(), book);
                            }
                        }
                    }
                this.plugin.skullMap.remove(block.getLocation());
                if (fenceBreak)
                    Utils.clearBlock(block);
                if (pc instanceof MobileSkull) {
                    Utils.clearBlock(block);
                    Utils.clearBlock(block.getRelative(BlockFace.DOWN));
                }
                player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("tempSkullRemoved"), ((pc instanceof MobileSkull) ? "Temporary " : "") + pc.getIntelligence().getNormalName() + " skull")));
                (new DataStore(this.plugin)).saveDatabase(true);
                subPlayerSkullCount(playerUUID);
            }
        }
    }

    private BlockFace clockwiseRotation(Skull skull) {
        BlockFace face = skull.getRotation();
        switch (face) {
            case NORTH:
                return BlockFace.NORTH_NORTH_EAST;
            case NORTH_NORTH_EAST:
                return BlockFace.NORTH_EAST;
            case NORTH_EAST:
                return BlockFace.EAST_NORTH_EAST;
            case EAST_NORTH_EAST:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.EAST_SOUTH_EAST;
            case EAST_SOUTH_EAST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.SOUTH_SOUTH_WEST;
            case SOUTH_SOUTH_WEST:
                return BlockFace.SOUTH_WEST;
            case SOUTH_WEST:
                return BlockFace.WEST_SOUTH_WEST;
            case WEST_SOUTH_WEST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.WEST_NORTH_WEST;
            case WEST_NORTH_WEST:
                return BlockFace.NORTH_WEST;
            case NORTH_WEST:
                return BlockFace.NORTH_NORTH_WEST;
            case NORTH_NORTH_WEST:
                return BlockFace.NORTH;
        }
        return BlockFace.NORTH;
    }

    private BlockFace counterClockwiseRotation(Skull skull) {
        BlockFace face = skull.getRotation();
        switch (face) {
            case NORTH:
                return BlockFace.NORTH_NORTH_WEST;
            case NORTH_NORTH_WEST:
                return BlockFace.NORTH_WEST;
            case NORTH_WEST:
                return BlockFace.WEST_NORTH_WEST;
            case WEST_NORTH_WEST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.WEST_SOUTH_WEST;
            case WEST_SOUTH_WEST:
                return BlockFace.SOUTH_WEST;
            case SOUTH_WEST:
                return BlockFace.SOUTH_SOUTH_WEST;
            case SOUTH_SOUTH_WEST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_EAST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.EAST_SOUTH_EAST;
            case EAST_SOUTH_EAST:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.EAST_NORTH_EAST;
            case EAST_NORTH_EAST:
                return BlockFace.NORTH_EAST;
            case NORTH_EAST:
                return BlockFace.NORTH_NORTH_EAST;
            case NORTH_NORTH_EAST:
                return BlockFace.NORTH;
        }
        return BlockFace.NORTH;
    }

    private boolean isGrayDye(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        short data = itemStack.getDurability();
        Material type = itemStack.getType();
      return type == Material.INK_SACK && data == 8;
    }

    private boolean isSkullBook(ItemStack item) {
        if (item == null)
            return false;
        if (item.getType() == Material.WRITTEN_BOOK) {
            if (!item.hasItemMeta())
                return false;
            BookMeta meta = (BookMeta) item.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
              return lore.size() == 2 && lore.get(0).contains("Skull Turret") && lore.get(1).equals("Skull Knowledge Book");
            }
        }
        return false;
    }

    private boolean skullLimit(UUID uUID) {
        Player player = Utils.getPlayerFromUUID(uUID);
        if (player == null)
            return false;
        if (this.plugin.hasPermission(player, "skullturret.admin"))
            return false;
        if (SkullTurret.DEBUG != 1 && this.plugin.playersSkullNumber.containsKey(uUID)) {
            int skullCount = this.plugin.playersSkullNumber.get(uUID).getActiveSkulls();
            int maxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;
            PerPlayerGroups ppg = this.plugin.getPlayerGroup(player);
            PerPlayerSettings pps = this.plugin.perPlayerSettings.get(uUID);
            if (ppg != null && !player.isOp()) {
                maxTurrets = ppg.getMaxTurrets();
            } else if (pps != null && pps.isPps()) {
                maxTurrets = pps.getMaxTurrets();
            } else if (SkullTurret.fd.hasFactions() && SkullTurret.FACT_USE_FACTION_POWER) {
                int maxFactionTurrets = this.plugin.getSkullLimit(uUID);
                int hardMaxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;
                if (hardMaxTurrets <= maxFactionTurrets) {
                    maxTurrets = hardMaxTurrets;
                } else {
                    maxTurrets = maxFactionTurrets;
                }
            }
            if (skullCount >= maxTurrets) {
                if (SkullTurret.fd.hasFactions() && SkullTurret.FACT_USE_FACTION_POWER) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("maxSkullErr"), Integer.valueOf(maxTurrets))));
                    player.sendMessage(Utils.parseText(Utils.getLocalization("factionPowerErr")));
                } else if (ppg != null) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perGroupErr"), ppg.getGroupName(), Integer.valueOf(maxTurrets))));
                } else if (pps != null) {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("perPlayerErr"), Integer.valueOf(maxTurrets))));
                } else {
                    player.sendMessage(Utils.parseText(String.format(Utils.getLocalization("maxSkullErr"), Integer.valueOf(maxTurrets))));
                }
                return true;
            }
        }
        return false;
    }

    private boolean isRawSkull(ItemStack skull) {
        return (skull != null && skull.getType().equals(Material.SKELETON_SKULL_ITEM) && !skull.getItemMeta().hasLore() && !skull.getItemMeta().hasDisplayName());
    }

    private boolean isRawBow(ItemStack bow) {
        return (bow != null && bow.getType().equals(Material.BOW) && !bow.getItemMeta().hasLore() && !bow.getItemMeta().hasDisplayName());
    }

    private boolean isSkullBow(ItemStack bow) {
        if (isValidSkullTurretItem(bow) && bow.getType() == Material.BOW) {
            List<String> lore = bow.getItemMeta().getLore();
            String name = bow.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.bowLoreName) && name.equals(this.plugin.recipes.bowName));
        }
        return false;
    }

    private boolean isCrazedSkull(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.crazedLoreName) && name.equals(this.plugin.recipes.crazedName));
        }
        return false;
    }

    private boolean isDeviousSkull(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.deviousLoreName) && name.equals(this.plugin.recipes.deviousName));
        }
        return false;
    }

    private boolean isMasterSkull(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.masterLoreName) && name.equals(this.plugin.recipes.masterName));
        }
        return false;
    }

    private boolean isWizardSkull(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.wizardLoreName) && name.equals(this.plugin.recipes.wizardName));
        }
        return false;
    }

    private boolean isDeviousSkullTurret(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.mobileDeviousLoreName) && name.equals(this.plugin.recipes.mobileDeviousName));
        }
        return false;
    }

    private boolean isMasterSkullTurret(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String name = skull.getItemMeta().getDisplayName();
            return (lore.contains(this.plugin.recipes.mobileMasterLoreName) && name.equals(this.plugin.recipes.mobileMasterName));
        }
        return false;
    }

    private int getMobileSkullAmmo(ItemStack skull) {
        if (isValidSkullTurretItem(skull) && skull.getType() == Material.SKELETON_SKULL_ITEM) {
            List<String> lore = skull.getItemMeta().getLore();
            String[] ammoAmount = lore.get(1).split("=");
            int amt = Integer.parseInt(ammoAmount[1]);
            return amt;
        }
        return 0;
    }

    private boolean isValidSkullTurretItem(ItemStack turretItem) {
        if (turretItem == null)
            return false;
        if (!turretItem.hasItemMeta())
            return false;
        if (!turretItem.getItemMeta().hasLore())
            return false;
      return turretItem.getItemMeta().hasDisplayName();
    }
}
