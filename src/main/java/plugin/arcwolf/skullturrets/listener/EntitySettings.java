package plugin.arcwolf.skullturrets.listener;

public class EntitySettings {
    private int rating;
    private boolean canPoison;
    private boolean mustHeal;

    public EntitySettings(final int rating, final boolean canPoison, final boolean mustHeal) {
        this.rating = 1;
        this.canPoison = true;
        this.mustHeal = false;
        this.rating = rating;
        this.canPoison = canPoison;
        this.mustHeal = mustHeal;
    }

    public int getRating() {
        return this.rating;
    }

    public boolean canPoison() {
        return this.canPoison;
    }

    public boolean mustHeal() {
        return this.mustHeal;
    }
}
