package plugin.arcwolf.skullturrets.listener;

public class PlayerNamesFoF {
    private String playerName;
    private String friendOrEnemy;

    public PlayerNamesFoF(final String playerName, final String friendOrEnemy) {
        this.playerName = "";
        this.friendOrEnemy = "";
        this.setPlayerName(playerName);
        this.setFriendOrEnemy(friendOrEnemy);
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public String getFriendOrEnemy() {
        return this.friendOrEnemy;
    }

    public void setFriendOrEnemy(final String friendOrEnemy) {
        this.friendOrEnemy = friendOrEnemy;
    }
}
