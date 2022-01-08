package plugin.arcwolf.skullturrets.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import plugin.arcwolf.skullturrets.SkullTurret;
import plugin.arcwolf.skullturrets.utils.Utils;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Callable;

public class FindUUID implements Callable<UUID> {
    private final String name;

    public FindUUID(final String name) {
        this.name = name;
    }

    public static UUID getUUIDFromPlayerName(final String name) throws Exception {
        final Player player = Utils.getPlayer(name);
        if (SkullTurret.ONLINE_UUID_CHECK) {
            return (player != null) ? player.getUniqueId() : new FindUUID(name).call();
        }
        return (player != null) ? player.getUniqueId() : Bukkit.getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public UUID call() throws Exception {
        final JSONParser jsonParser = new JSONParser();
        final URL url = new URL(SkullTurret.PROFILE_URL);
        final HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setUseCaches(false);
        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);
        final String jsonString = "[\"" + this.name + "\"]";
        final OutputStream outputStream = httpConnection.getOutputStream();
        outputStream.write(jsonString.getBytes());
        outputStream.flush();
        outputStream.close();
        final JSONArray jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(httpConnection.getInputStream()));
        final JSONObject userProfile = (JSONObject) jsonArray.get(0);
        final String id = (String) userProfile.get("id");
        final UUID uuid = UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
        Thread.sleep(100L);
        return uuid;
    }
}
