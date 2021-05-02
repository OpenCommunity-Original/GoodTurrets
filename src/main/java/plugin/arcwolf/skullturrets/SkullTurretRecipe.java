package plugin.arcwolf.skullturrets;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class SkullTurretRecipe {
    private final MaterialData skull = new MaterialData(Material.SKELETON_SKULL);

    private final MaterialData wizSkull = getWizSkull();

    private final MaterialData bow = new MaterialData(Material.BOW);

    public ItemStack crazedSkullItem;

    public ItemStack deviousSkullItem;

    public ItemStack masterSkullItem;

    public ItemStack wizardSkullItem;

    public ItemStack bowTargetItem;

    public String crazedName = "Crazed Skull";

    public String deviousName = "Devious Skull";

    public String masterName = "Master Skull";

    public String wizardName = "Wizard Skull";

    public String crazedLoreName = "A Crazed Skull.";

    public String deviousLoreName = "A Devious Skull.";

    public String masterLoreName = "A Master Skull.";

    public String wizardLoreName = "A Wizard Skull.";

    public String bowName = "Skull Bow";

    public String bowLoreName = "Skull Target Bow";

    public ShapelessRecipe[] mobileDeviousSkull = new ShapelessRecipe[7];

    public ShapelessRecipe[] mobileMasterSkull = new ShapelessRecipe[7];

    public ItemStack mobileDeviousSkullItem;

    public ItemStack mobileMasterSkullItem;

    public String mobileDeviousName = "Devious Turret";

    public String mobileMasterName = "Master Turret";

    public String mobileCrazedLoreName = "A Crazed Turret";

    public String mobileDeviousLoreName = "A Devious Turret";

    public String mobileMasterLoreName = "A Master Turret";

    private final SkullTurret plugin;

    public SkullTurretRecipe(SkullTurret plugin) {
        this.plugin = plugin;
        this.crazedSkullItem = getCrazedSkull();
        this.deviousSkullItem = getDeviousSkull();
        this.masterSkullItem = getMasterSkull();
        this.wizardSkullItem = getWizardSkull();
        this.bowTargetItem = getSkullBow();
        if (SkullTurret.ALLOW_TEMP_TURRETS)
            loadRecipies();
    }

    protected ItemStack getSkullBow() {
        return getSkullBow(1);
    }

    protected ItemStack getCrazedSkull() {
        return getCrazedSkull(1);
    }

    protected ItemStack getDeviousSkull() {
        return getDeviousSkull(1);
    }

    protected ItemStack getMasterSkull() {
        return getMasterSkull(1);
    }

    protected ItemStack getWizardSkull() {
        return getWizardSkull(1);
    }

    protected ItemStack getMobileDeviousSkull() {
        return getMobileDeviousSkull(1, 1);
    }

    protected ItemStack getMobileMasterSkull() {
        return getMobileMasterSkull(1, 1);
    }

    private MaterialData getWizSkull() {
        ItemStack wsItem = new ItemStack(Material.SKELETON_SKULL);
        wsItem.setDurability((short) 1);
        return wsItem.getData();
    }

    protected ItemStack getSkullBow(int amount) {
        ItemStack item = this.bow.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.bowName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.bowLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        item.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        return item;
    }

    protected ItemStack getCrazedSkull(int amount) {
        ItemStack item = this.skull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.crazedName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.crazedLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getDeviousSkull(int amount) {
        ItemStack item = this.skull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.deviousName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.deviousLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMasterSkull(int amount) {
        ItemStack item = this.skull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.masterName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.masterLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getWizardSkull(int amount) {
        ItemStack item = this.wizSkull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.wizardName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.wizardLoreName);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMobileDeviousSkull(int amount, int ammoAmount) {
        ItemStack item = this.skull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.mobileDeviousName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.mobileDeviousLoreName);
        lore.add("Ammo=" + ammoAmount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    protected ItemStack getMobileMasterSkull(int amount, int ammoAmount) {
        ItemStack item = this.skull.toItemStack(amount);
        ItemMeta itmeta = item.getItemMeta();
        itmeta.setDisplayName(this.mobileMasterName);
        List<String> lore = new ArrayList<String>();
        lore.add(this.mobileMasterLoreName);
        lore.add("Ammo=" + ammoAmount);
        itmeta.setLore(lore);
        item.setItemMeta(itmeta);
        return item;
    }

    private void loadRecipies() {
        Material arrow = Material.ARROW;
        Material fence = Material.OAK_FENCE;
        Material netherFence = Material.CRIMSON_FENCE;
        Material skull = Material.SKELETON_SKULL;
        this.mobileDeviousSkullItem = getMobileDeviousSkull();
        this.mobileMasterSkullItem = getMobileMasterSkull();
        int count = 1;
        int i;
        for (i = 0; i < 7; i++) {
            this.mobileDeviousSkull[i] = new ShapelessRecipe(this.mobileDeviousSkullItem);
            for (int amt = 0; amt < count; amt++)
                this.mobileDeviousSkull[i].addIngredient(arrow);
            count++;
        }
        count = 1;
        for (i = 0; i < 7; i++) {
            this.mobileMasterSkull[i] = new ShapelessRecipe(this.mobileMasterSkullItem);
            for (int amt = 0; amt < count; amt++)
                this.mobileMasterSkull[i].addIngredient(arrow);
            count++;
        }
        byte b;
        int j;
        ShapelessRecipe[] arrayOfShapelessRecipe;
        for (j = (arrayOfShapelessRecipe = this.mobileDeviousSkull).length, b = 0; b < j; ) {
            ShapelessRecipe sr = arrayOfShapelessRecipe[b];
            sr.addIngredient(skull);
            sr.addIngredient(fence);
            this.plugin.getServer().addRecipe(sr);
            b++;
        }
        for (j = (arrayOfShapelessRecipe = this.mobileMasterSkull).length, b = 0; b < j; ) {
            ShapelessRecipe sr = arrayOfShapelessRecipe[b];
            sr.addIngredient(skull);
            sr.addIngredient(netherFence);
            this.plugin.getServer().addRecipe(sr);
            b++;
        }
    }
}
