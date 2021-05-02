package plugin.arcwolf.skullturrets;

public class PlayerNamesFoF {
    private String playerName = "";

    private String friendOrEnemy = "";

    public PlayerNamesFoF(String playerName, String friendOrEnemy) {
        setPlayerName(playerName);
        setFriendOrEnemy(friendOrEnemy);
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public void setFriendOrEnemy(String friendOrEnemy) {
        this.friendOrEnemy = friendOrEnemy;
    }

    public String getFriendOrEnemy() {
        return this.friendOrEnemy;
    }
}
