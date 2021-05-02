package plugin.arcwolf.skullturrets;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public enum SkullIntelligence {
    CRAZED(

            0.5D, 15, 6.0D, null, "Crazed", SkullTurret.plugin.recipes.crazedSkullItem, null, Sound.ENTITY_BLAZE_AMBIENT, 1.05F, true, 1000, 30.0D, Material.STICK),
    DEVIOUS(0.75D, 10, 8.5D, Effect.SMOKE, "Devious", SkullTurret.plugin.recipes.deviousSkullItem, SkullTurret.plugin.recipes.mobileDeviousSkullItem, Sound.ENTITY_BLAZE_AMBIENT, 1.0F, true, 1300, 40.0D, Material.BONE),
    MASTER(0.9D, 0, 9.5D, Effect.MOBSPAWNER_FLAMES, "Master", SkullTurret.plugin.recipes.masterSkullItem, SkullTurret.plugin.recipes.mobileMasterSkullItem, Sound.ENTITY_BLAZE_AMBIENT, 0.95F, true, 1000, 60.0D, Material.DIAMOND),
    WIZARD(0.85D, 5, 9.0D, Effect.MOBSPAWNER_FLAMES, "Wizard", SkullTurret.plugin.recipes.wizardSkullItem, null, Sound.ENTITY_BLAZE_AMBIENT, 0.95F, false, 2000, 50.0D, Material.GOLD_INGOT);

    private int spread;

    private double fireRangeMultiplier;

    private double damageMod;

    private final Effect effect;

    private final String normalName;

    private final ItemStack skullType;

    private final ItemStack mobileSkullType;

    private final Sound sound;

    private final float pitch;

    private final boolean skinChange;

    private int cooldown;

    private double health;

    private Material repair_item;

    SkullIntelligence(double fireRangeMultiplier, int spread, double damageMod, Effect effect, String normalName, ItemStack skullType, ItemStack mobileSkullType, Sound sound, float pitch, boolean skinChange, int cooldown, double health, Material repair_item) {
        this.fireRangeMultiplier = fireRangeMultiplier;
        this.spread = spread;
        this.damageMod = damageMod;
        this.effect = effect;
        this.normalName = normalName;
        this.skullType = skullType;
        this.mobileSkullType = mobileSkullType;
        this.sound = sound;
        this.pitch = pitch;
        this.skinChange = skinChange;
        this.cooldown = cooldown;
        this.health = health;
        this.repair_item = repair_item;
    }

    public int getSpread() {
        return this.spread;
    }

    public double getFireRangeMultiplier() {
        return this.fireRangeMultiplier;
    }

    public Effect getEffect() {
        return this.effect;
    }

    public String getNormalName() {
        return this.normalName;
    }

    public ItemStack getSkullItem() {
        return this.skullType;
    }

    public ItemStack getMobileSkullItem() {
        return this.mobileSkullType;
    }

    public Sound getSound() {
        return this.sound;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean canSkinChange() {
        return this.skinChange;
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public double getDamageMod() {
        return this.damageMod;
    }

    public void setFireRangeMultiplier(double fireRangeMultiplier) {
        this.fireRangeMultiplier = fireRangeMultiplier;
    }

    public void setDamageMod(double damageMod) {
        this.damageMod = damageMod;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setSpread(int spread) {
        this.spread = spread;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double getHealth() {
        return this.health;
    }

    public Material getRepair_item() {
        return this.repair_item;
    }

    public void setRepair_item(Material repair_item) {
        this.repair_item = repair_item;
    }
}
