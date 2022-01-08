package plugin.arcwolf.skullturrets.listener;

import plugin.arcwolf.skullturrets.SkullTurret;

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
    private boolean masterDefaults;
    private boolean wizardDefaults;
    private boolean pps;

    public PerPlayerSettings(final UUID playerUUID, final int maxTurrets, final int maxRange) {
        this.masterDefaults = false;
        this.wizardDefaults = false;
        this.pps = false;
        this.playerUUID = playerUUID;
        this.maxTurrets = maxTurrets;
        this.maxRange = maxRange;
        this.pps = true;
    }

    public PerPlayerSettings(final UUID playerUUID, final String skinName, final String ammoTypeName, final boolean redstone, final boolean patrol) {
        this.masterDefaults = false;
        this.wizardDefaults = false;
        this.pps = false;
        this.playerUUID = playerUUID;
        this.masterSkinName = skinName;
        this.ammoTypeName = ammoTypeName;
        this.masterRedstone = redstone;
        this.masterPatrol = patrol;
        this.masterDefaults = true;
    }

    public PerPlayerSettings(final UUID playerUUID, final String skinName, final boolean redstone, final boolean patrol) {
        this.masterDefaults = false;
        this.wizardDefaults = false;
        this.pps = false;
        this.playerUUID = playerUUID;
        this.wizardSkinName = skinName;
        this.wizardRedstone = redstone;
        this.wizardPatrol = patrol;
        this.wizardDefaults = true;
    }

    public PerPlayerSettings(final UUID playerUUID) {
        this.masterDefaults = false;
        this.wizardDefaults = false;
        this.pps = false;
        this.playerUUID = playerUUID;
    }

    public void reloadDefaults(final PerPlayerSettings oldSettings) {
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
        if (!this.isPps() && !this.isMasterDefaults() && !this.isWizardDefaults()) {
            SkullTurret.plugin.perPlayerSettings.remove(this.playerUUID);
        }
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public void setPlayerUUID(final UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getAmmoTypeName() {
        return this.ammoTypeName;
    }

    public void setAmmoTypeName(final String ammoTypeName) {
        this.ammoTypeName = ammoTypeName;
    }

    public int getMaxTurrets() {
        return this.maxTurrets;
    }

    public void setMaxTurrets(final int maxTurrets) {
        this.maxTurrets = maxTurrets;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public void setMaxRange(final int maxRange) {
        this.maxRange = maxRange;
    }

    public String getLastKnownPlayerName() {
        return this.playerName;
    }

    public void setLastKnownPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public boolean isPps() {
        return this.pps;
    }

    public void setPps(final boolean pps) {
        this.pps = pps;
    }

    public String getMasterSkinName() {
        return this.masterSkinName;
    }

    public void setMasterSkinName(final String masterSkinName) {
        this.masterSkinName = masterSkinName;
    }

    public String getWizardSkinName() {
        return this.wizardSkinName;
    }

    public void setWizardSkinName(final String wizardSkinName) {
        this.wizardSkinName = wizardSkinName;
    }

    public boolean isMasterRedstone() {
        return this.masterRedstone;
    }

    public void setMasterRedstone(final boolean masterRedstone) {
        this.masterRedstone = masterRedstone;
    }

    public boolean isMasterPatrol() {
        return this.masterPatrol;
    }

    public void setMasterPatrol(final boolean masterPatrol) {
        this.masterPatrol = masterPatrol;
    }

    public boolean isWizardRedstone() {
        return this.wizardRedstone;
    }

    public void setWizardRedstone(final boolean wizardRedstone) {
        this.wizardRedstone = wizardRedstone;
    }

    public boolean isWizardPatrol() {
        return this.wizardPatrol;
    }

    public void setWizardPatrol(final boolean wizardPatrol) {
        this.wizardPatrol = wizardPatrol;
    }

    public boolean isMasterDefaults() {
        return this.masterDefaults;
    }

    public void setMasterDefaults(final boolean masterDefaults) {
        this.masterDefaults = masterDefaults;
    }

    public boolean isWizardDefaults() {
        return this.wizardDefaults;
    }

    public void setWizardDefaults(final boolean wizardDefaults) {
        this.wizardDefaults = wizardDefaults;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.playerUUID == null) ? 0 : this.playerUUID.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final PerPlayerSettings other = (PerPlayerSettings) obj;
        if (this.playerUUID == null) {
            return other.playerUUID == null;
        } else return this.playerUUID.equals(other.playerUUID);
    }
}
