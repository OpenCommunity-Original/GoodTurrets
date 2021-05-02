package plugin.arcwolf.skullturrets;

public class EntitySettings {
    private int rating = 1;

    private boolean canPoison = true;

    private boolean mustHeal = false;

    public EntitySettings(int rating, boolean canPoison, boolean mustHeal) {
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
