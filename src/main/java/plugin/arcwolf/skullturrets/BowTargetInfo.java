package plugin.arcwolf.skullturrets;

import java.util.UUID;

public class BowTargetInfo {
    public UUID playerUUID;

    public long timer;

    public BowTargetInfo(UUID playerUUID, long timer) {
        this.playerUUID = playerUUID;
        this.timer = timer;
    }
}
