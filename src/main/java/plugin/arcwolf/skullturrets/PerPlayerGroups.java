package plugin.arcwolf.skullturrets;

public class PerPlayerGroups {
    private int maxTurrets = SkullTurret.MAX_SKULL_PER_PLAYER;

    private int maxRange = SkullTurret.MAX_RANGE;

    private String groupName;

    public PerPlayerGroups(String groupName, int maxTurrets, int maxRange) {
        this.groupName = "";
        this.groupName = groupName;
        this.maxTurrets = maxTurrets;
        this.maxRange = maxRange;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public int getMaxTurrets() {
        return this.maxTurrets;
    }

    public int getMaxRange() {
        return this.maxRange;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setMaxTurrets(int maxTurrets) {
        this.maxTurrets = maxTurrets;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + ((this.groupName == null) ? 0 : this.groupName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PerPlayerGroups other = (PerPlayerGroups) obj;
        if (this.groupName == null) {
            return other.groupName == null;
        } else return this.groupName.equals(other.groupName);
    }
}
