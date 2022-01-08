package plugin.arcwolf.skullturrets.listener;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import plugin.arcwolf.skullturrets.SkullTurret;

public enum SkullIntelligence {
    CRAZED("CRAZED", 0, 0.5, 15, 6.0, null, "Crazed", SkullTurret.plugin.recipes.crazedSkullItem, null, Sound.ENTITY_BLAZE_DEATH, 1.05f, true, 1000, 30.0, Material.STICK),
    DEVIOUS("DEVIOUS", 1, 0.75, 10, 8.5, Effect.SMOKE, "Devious", SkullTurret.plugin.recipes.deviousSkullItem, SkullTurret.plugin.recipes.mobileDeviousSkullItem, Sound.ENTITY_BLAZE_DEATH, 1.0f, true, 1300, 40.0, Material.BONE),
    MASTER("MASTER", 2, 0.9, 0, 9.5, Effect.MOBSPAWNER_FLAMES, "Master", SkullTurret.plugin.recipes.masterSkullItem, SkullTurret.plugin.recipes.mobileMasterSkullItem, Sound.ENTITY_BLAZE_DEATH, 0.95f, true, 1000, 60.0, Material.DIAMOND),
    WIZARD("WIZARD", 3, 0.85, 5, 9.0, Effect.MOBSPAWNER_FLAMES, "Wizard", SkullTurret.plugin.recipes.wizardSkullItem, null, Sound.ENTITY_BLAZE_DEATH, 0.95f, false, 2000, 50.0, Material.GOLD_INGOT);

    private final Effect effect;
    private final String normalName;
    private final ItemStack skullType;
    private final ItemStack mobileSkullType;
    private final Sound sound;
    private final float pitch;
    private final boolean skinChange;
    private int spread;
    private double fireRangeMultiplier;
    private double damageMod;
    private int cooldown;
    private double health;
    private Material repair_item;

    SkullIntelligence(final String name, final int ordinal, final double fireRangeMultiplier, final int spread, final double damageMod, final Effect effect, final String normalName, final ItemStack skullType, final ItemStack mobileSkullType, final Sound sound, final float pitch, final boolean skinChange, final int cooldown, final double health, final Material repair_item) {
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

    public void setSpread(final int spread) {
        this.spread = spread;
    }

    public double getFireRangeMultiplier() {
        return this.fireRangeMultiplier;
    }

    public void setFireRangeMultiplier(final double fireRangeMultiplier) {
        this.fireRangeMultiplier = fireRangeMultiplier;
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

    public void setCooldown(final int cooldown) {
        this.cooldown = cooldown;
    }

    public double getDamageMod() {
        return this.damageMod;
    }

    public void setDamageMod(final double damageMod) {
        this.damageMod = damageMod;
    }

    public double getHealth() {
        return this.health;
    }

    public void setHealth(final double health) {
        this.health = health;
    }

    public Material getRepair_item() {
        return this.repair_item;
    }

    public void setRepair_item(final Material repair_item) {
        this.repair_item = repair_item;
    }
}
