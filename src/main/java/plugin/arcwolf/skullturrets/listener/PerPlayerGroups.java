package plugin.arcwolf.skullturrets.listener;

import plugin.arcwolf.skullturrets.SkullTurret;

public class PerPlayerGroups {
    private int maxTurrets;
    private int maxRange;
    private String groupName;

    public PerPlayerGroups(final String groupName, final int maxTurrets, final int maxRange) {
        this.maxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;
        this.maxRange = SkullTurret.MAX_RANGE;
        this.groupName = "";
        this.groupName = groupName;
        this.maxTurrets = maxTurrets;
        this.maxRange = maxRange;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(final String groupName) {
        this.groupName = groupName;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.groupName == null) ? 0 : this.groupName.hashCode());
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
        final PerPlayerGroups other = (PerPlayerGroups) obj;
        if (this.groupName == null) {
            return other.groupName == null;
        } else return this.groupName.equals(other.groupName);
    }
}
