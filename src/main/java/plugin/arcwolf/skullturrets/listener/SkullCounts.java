package plugin.arcwolf.skullturrets.listener;

public class SkullCounts {
    private int activeSkulls;
    private int disabledSkulls;

    public SkullCounts(final int activeSkulls, final int disabledSkulls) {
        this.activeSkulls = 0;
        this.disabledSkulls = 0;
        this.setActiveSkulls(activeSkulls);
        this.setDisabledSkulls(disabledSkulls);
    }

    public int getActiveSkulls() {
        return this.activeSkulls;
    }

    public void setActiveSkulls(final int activeSkulls) {
        this.activeSkulls = activeSkulls;
    }

    public int getDisabledSkulls() {
        return this.disabledSkulls;
    }

    public void setDisabledSkulls(final int disabledSkulls) {
        this.disabledSkulls = disabledSkulls;
    }

    public int getTotalSkulls() {
        return this.activeSkulls + this.disabledSkulls;
    }

    public void disableSkull(final PlacedSkull pc) {
        pc.setDisabled(true);
        --this.activeSkulls;
        ++this.disabledSkulls;
    }

    public void enableSkull(final PlacedSkull pc) {
        pc.setDisabled(false);
        ++this.activeSkulls;
        --this.disabledSkulls;
    }
}
