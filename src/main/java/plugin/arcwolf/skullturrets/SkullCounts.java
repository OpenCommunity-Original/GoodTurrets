package plugin.arcwolf.skullturrets;

public class SkullCounts {
    private int activeSkulls = 0;

    private int disabledSkulls = 0;

    public SkullCounts(int activeSkulls, int disabledSkulls) {
        setActiveSkulls(activeSkulls);
        setDisabledSkulls(disabledSkulls);
    }

    public void setActiveSkulls(int activeSkulls) {
        this.activeSkulls = activeSkulls;
    }

    public int getActiveSkulls() {
        return this.activeSkulls;
    }

    public void setDisabledSkulls(int disabledSkulls) {
        this.disabledSkulls = disabledSkulls;
    }

    public int getDisabledSkulls() {
        return this.disabledSkulls;
    }

    public int getTotalSkulls() {
        return this.activeSkulls + this.disabledSkulls;
    }

    public void disableSkull(PlacedSkull pc) {
        pc.setDisabled(true);
        this.activeSkulls--;
        this.disabledSkulls++;
    }

    public void enableSkull(PlacedSkull pc) {
        pc.setDisabled(false);
        this.activeSkulls++;
        this.disabledSkulls--;
    }
}
