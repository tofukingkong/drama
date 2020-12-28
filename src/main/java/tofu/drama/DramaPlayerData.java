package tofu.drama;

import java.time.LocalDateTime;
import java.util.UUID;

public class DramaPlayerData {
    public UUID uuid;
    public String name;
    public double lastX;
    public double lastY;
    public double lastZ;
    public LocalDateTime lastMove;
    public LocalDateTime lastSeen;
    public boolean isAfk;
    public FileManager.ManagedFile tracker;


    public DramaPlayerData(UUID id, String name, double x, double y, double z) {
        uuid = id;
        this.name = name;
        lastX = x;
        lastY = y;
        lastZ = z;
        lastMove = LocalDateTime.now();
        lastSeen = lastMove;
        isAfk = false;
        tracker = null;
    }

    public void reset() {
        lastMove = LocalDateTime.now();
        lastSeen = lastMove;
        isAfk = false;
        tracker = null;
    }
}
