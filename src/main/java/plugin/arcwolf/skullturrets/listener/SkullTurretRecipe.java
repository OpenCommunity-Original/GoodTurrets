package plugin.arcwolf.skullturrets.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.arcwolf.skullturrets.SkullTurret;

import java.util.ArrayList;
import java.util.List;

public class SkullTurretRecipe {
    private final ItemStack skull;
    private final ItemStack wizSkull;
    private final ItemStack bow;
    private final SkullTurret plugin;
    public ItemStack crazedSkullItem;
    public ItemStack deviousSkullItem;
    public ItemStack masterSkullItem;
    public ItemStack wizardSkullItem;
    public ItemStack bowTargetItem;
    public String crazedName;
    public String deviousName;
    public String masterName;
    public String wizardName;
    public String crazedLoreName;
    public String deviousLoreName;
    public String masterLoreName;
    public String wizardLoreName;
    public String bowName;
    public String bowLoreName;
    public ShapelessRecipe[] mobileDeviousSkull;
    public ShapelessRecipe[] mobileMasterSkull;
    public ItemStack mobileDeviousSkullItem;
    public ItemStack mobileMasterSkullItem;
    public String mobileDeviousName;
    public String mobileMasterName;
    public String mobileCrazedLoreName;
    public String mobileDeviousLoreName;
    public String mobileMasterLoreName;

    public SkullTurretRecipe(final SkullTurret plugin) {
        new ItemStack(Material.BOW);
        this.skull = new ItemStack(Material.SKELETON_SKULL);
        this.wizSkull = this.getWizSkull();
        this.bow = new ItemStack(Material.BOW);
        this.crazedName = "Crazed Skull";
        this.deviousName = "Devious Skull";
        this.masterName = "Master Skull";
        this.wizardName = "Wizard Skull";
        this.crazedLoreName = "A Crazed Skull.";
        this.deviousLoreName = "A Devious Skull.";
        this.masterLoreName = "A Master Skull.";
        this.wizardLoreName = "A Wizard Skull.";
        this.bowName = "Skull Bow";
        this.bowLoreName = "Skull Target Bow";
        this.mobileDeviousSkull = new ShapelessRecipe[7];
        this.mobileMasterSkull = new ShapelessRecipe[7];
        this.mobileDeviousName = "Devious Turret";
        this.mobileMasterName = "Master Turret";
        this.mobileCrazedLoreName = "A Crazed Turret";
        this.mobileDeviousLoreName = "A Devious Turret";
        this.mobileMasterLoreName = "A Master Turret";
        this.plugin = plugin;
        this.crazedSkullItem = this.getCrazedSkull();
        this.deviousSkullItem = this.getDeviousSkull();
        this.masterSkullItem = this.getMasterSkull();
        this.wizardSkullItem = this.getWizardSkull();
        this.bowTargetItem = this.getSkullBow();
        if (SkullTurret.ALLOW_TEMP_TURRETS) {
            this.loadRecipies();
        }
    }

    public ItemStack getSkullBow() {
        return this.getSkullBow(1);
    }

    public ItemStack getCrazedSkull() {
        return this.getCrazedSkull(1);
    }

    public ItemStack getDeviousSkull() {
        return this.getDeviousSkull(1);
    }

    public ItemStack getMasterSkull() {
        return this.getMasterSkull(1);
    }

    public ItemStack getWizardSkull() {
        return this.getWizardSkull(1);
    }

    protected ItemStack getMobileDeviousSkull() {
        return this.getMobileDeviousSkull(1, 1);
    }

    protected ItemStack getMobileMasterSkull() {
        return this.getMobileMasterSkull(1, 1);
    }

    private ItemStack getWizSkull() {
        ItemStack wsItem = new ItemStack(Material.WITHER_SKELETON_WALL_SKULL);
        ItemMeta itemMeta = wsItem.getItemMeta();
        if (itemMeta instanceof Damageable) {
            ((Damageable) itemMeta).setDamage(1);
        }
        return wsItem;
    }

    protected ItemStack getSkullBow(final int amount) {
        final ItemStack item = this.bow;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.bowName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.bowLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        return item;
    }

    protected ItemStack getCrazedSkull(final int amount) {
        final ItemStack item = this.skull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.crazedName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.crazedLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getDeviousSkull(final int amount) {
        final ItemStack item = this.skull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.deviousName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.deviousLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMasterSkull(final int amount) {
        final ItemStack item = this.skull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.masterName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.masterLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getWizardSkull(final int amount) {
        final ItemStack item = this.wizSkull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.wizardName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.wizardLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMobileDeviousSkull(final int amount, final int ammoAmount) {
        final ItemStack item = this.skull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.mobileDeviousName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.mobileDeviousLoreName);
        lore.add("Ammo=" + ammoAmount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMobileMasterSkull(final int amount, final int ammoAmount) {
        final ItemStack item = this.skull;
        final ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.mobileMasterName);
        final List<String> lore = new ArrayList<>();
        lore.add(this.mobileMasterLoreName);
        lore.add("Ammo=" + ammoAmount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    private void loadRecipies() {
        final Material arrow = Material.ARROW;
        final Material fence = Material.OAK_FENCE;
        final Material netherFence = Material.NETHER_BRICK_FENCE;
        final Material skull = Material.SKELETON_SKULL;
        this.mobileDeviousSkullItem = this.getMobileDeviousSkull();
        this.mobileMasterSkullItem = this.getMobileMasterSkull();
        int count = 1;
        for (int i = 0; i < 7; ++i) {
            this.mobileDeviousSkull[i] = new ShapelessRecipe(/*plugin.key,*/ this.mobileDeviousSkullItem);
            for (int amt = 0; amt < count; ++amt) {
                this.mobileDeviousSkull[i].addIngredient(arrow);
            }
            ++count;
        }
        count = 1;
        for (int i = 0; i < 7; ++i) {
            this.mobileMasterSkull[i] = new ShapelessRecipe(/*plugin.key,*/ this.mobileMasterSkullItem);
            for (int amt = 0; amt < count; ++amt) {
                this.mobileMasterSkull[i].addIngredient(arrow);
            }
            ++count;
        }
        ShapelessRecipe[] mobileDeviousSkull;
        for (int length = (mobileDeviousSkull = this.mobileDeviousSkull).length, j = 0; j < length; ++j) {
            final ShapelessRecipe sr = mobileDeviousSkull[j];
            sr.addIngredient(skull);
            sr.addIngredient(fence);
            this.plugin.getServer().addRecipe(sr);
        }
        ShapelessRecipe[] mobileMasterSkull;
        for (int length2 = (mobileMasterSkull = this.mobileMasterSkull).length, k = 0; k < length2; ++k) {
            final ShapelessRecipe sr = mobileMasterSkull[k];
            sr.addIngredient(skull);
            sr.addIngredient(netherFence);
            this.plugin.getServer().addRecipe(sr);
        }
    }
}
