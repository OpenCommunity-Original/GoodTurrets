package plugin.arcwolf.skullturrets.listener;

import java.util.UUID;

public class BowTargetInfo {
    public UUID playerUUID;
    public long timer;

    public BowTargetInfo(final UUID playerUUID, final long timer) {
        this.playerUUID = playerUUID;
        this.timer = timer;
    }
}
