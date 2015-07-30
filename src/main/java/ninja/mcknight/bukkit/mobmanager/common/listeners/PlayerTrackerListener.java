package ninja.mcknight.bukkit.mobmanager.common.listeners;

import ninja.mcknight.bukkit.mobmanager.P;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player online status in a thread safe Map
 */
public class PlayerTrackerListener implements Listener {
    private ConcurrentHashMap<String, Player> players;
    private Map<String, Player> safeMap;

    public PlayerTrackerListener() {
        players = new ConcurrentHashMap<>();
        safeMap = Collections.unmodifiableMap(players);

        for (Player p : P.p().getServer().getOnlinePlayers()) {
            players.put(p.getName().toLowerCase(), p);
        }
    }

    public Map<String, Player> getOnlinePlayerPlayers() {
        return safeMap;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        players.put(player.getName().toLowerCase(), player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        players.remove(player.getName().toLowerCase());
    }
}
