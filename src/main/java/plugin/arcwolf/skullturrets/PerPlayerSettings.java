package plugin.arcwolf.skullturrets;

import java.util.UUID;

public class PerPlayerSettings {
    private UUID playerUUID;

    private String playerName;

    private String masterSkinName;

    private String wizardSkinName;

    private String ammoTypeName;

    private int maxTurrets;

    private int maxRange;

    private boolean masterRedstone;

    private boolean masterPatrol;

    private boolean wizardRedstone;

    private boolean wizardPatrol;

    private boolean masterDefaults = false;

    private boolean wizardDefaults = false;

    private boolean pps = false;

    public PerPlayerSettings(UUID playerUUID, int maxTurrets, int maxRange) {
        this.playerUUID = playerUUID;
        this.maxTurrets = maxTurrets;
        this.maxRange = maxRange;
        this.pps = true;
    }

    public PerPlayerSettings(UUID playerUUID, String skinName, String ammoTypeName, boolean redstone, boolean patrol) {
        this.playerUUID = playerUUID;
        this.masterSkinName = skinName;
        this.ammoTypeName = ammoTypeName;
        this.masterRedstone = redstone;
        this.masterPatrol = patrol;
        this.masterDefaults = true;
    }

    public PerPlayerSettings(UUID playerUUID, String skinName, boolean redstone, boolean patrol) {
        this.playerUUID = playerUUID;
        this.wizardSkinName = skinName;
        this.wizardRedstone = redstone;
        this.wizardPatrol = patrol;
        this.wizardDefaults = true;
    }

    public void reloadDefaults(PerPlayerSettings oldSettings) {
        if (oldSettings.masterDefaults) {
            this.masterRedstone = oldSettings.isMasterRedstone();
            this.masterPatrol = oldSettings.isMasterPatrol();
            this.masterSkinName = oldSettings.getMasterSkinName();
            this.ammoTypeName = oldSettings.getAmmoTypeName();
            this.masterDefaults = true;
        }
        if (oldSettings.wizardDefaults) {
            this.wizardRedstone = oldSettings.isWizardRedstone();
            this.wizardPatrol = oldSettings.isWizardPatrol();
            this.wizardSkinName = oldSettings.getWizardSkinName();
            this.wizardDefaults = true;
        }
    }

    public void cleanUpPPS() {
        if (!isPps() && !isMasterDefaults() && !isWizardDefaults())
            SkullTurret.plugin.perPlayerSettings.remove(this.playerUUID);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getAmmoTypeName() {
        return this.ammoTypeName;
    }

    public int getMaxTurrets() {
        return this.maxTurrets;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public String getLastKnownPlayerName() {
        return this.playerName;
    }

    public void setLastKnownPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPps(boolean pps) {
        this.pps = pps;
    }

    public boolean isPps() {
        return this.pps;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void setAmmoTypeName(String ammoTypeName) {
        this.ammoTypeName = ammoTypeName;
    }

    public void setMaxTurrets(int maxTurrets) {
        this.maxTurrets = maxTurrets;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public String getMasterSkinName() {
        return this.masterSkinName;
    }

    public String getWizardSkinName() {
        return this.wizardSkinName;
    }

    public boolean isMasterRedstone() {
        return this.masterRedstone;
    }

    public boolean isMasterPatrol() {
        return this.masterPatrol;
    }

    public boolean isWizardRedstone() {
        return this.wizardRedstone;
    }

    public boolean isWizardPatrol() {
        return this.wizardPatrol;
    }

    public boolean isMasterDefaults() {
        return this.masterDefaults;
    }

    public boolean isWizardDefaults() {
        return this.wizardDefaults;
    }

    public void setMasterSkinName(String masterSkinName) {
        this.masterSkinName = masterSkinName;
    }

    public void setWizardSkinName(String wizardSkinName) {
        this.wizardSkinName = wizardSkinName;
    }

    public void setMasterRedstone(boolean masterRedstone) {
        this.masterRedstone = masterRedstone;
    }

    public void setMasterPatrol(boolean masterPatrol) {
        this.masterPatrol = masterPatrol;
    }

    public void setWizardRedstone(boolean wizardRedstone) {
        this.wizardRedstone = wizardRedstone;
    }

    public void setWizardPatrol(boolean wizardPatrol) {
        this.wizardPatrol = wizardPatrol;
    }

    public void setMasterDefaults(boolean masterDefaults) {
        this.masterDefaults = masterDefaults;
    }

    public void setWizardDefaults(boolean wizardDefaults) {
        this.wizardDefaults = wizardDefaults;
    }

    public PerPlayerSettings(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + ((this.playerUUID == null) ? 0 : this.playerUUID.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PerPlayerSettings other = (PerPlayerSettings) obj;
        if (this.playerUUID == null) {
            return other.playerUUID == null;
        } else return this.playerUUID.equals(other.playerUUID);
    }
}
