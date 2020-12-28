package tofu.drama;


import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.org.apache.xpath.internal.functions.FuncSubstringAfter;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PlayerTracker {
    private final ConcurrentHashMap<UUID,DramaPlayerData> _playerData = new ConcurrentHashMap<>();
    private final MinecraftServer _server;
    private final int _afkDelay;

    public PlayerTracker(MinecraftServer server, int afkDelay) {
        _server = server;
        _afkDelay = afkDelay;

        Drama.LOGGER.info(MessageFormat.format("PlayerTracker: AfkDelay[{0}]", afkDelay));
    }

    public void onJoin(ServerPlayerEntity player)
    {
        String name = player.getName().getString();
        Drama.LOGGER.info(MessageFormat.format("PlayerTracker: Adding player {0}", name));
        UUID id = player.getUniqueID();

        DramaPlayerData data = _playerData.get(id);
        if (data != null) {
            data.reset();
        } else {
            data = new DramaPlayerData(id, name, player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ);
        }

        data.tracker = Drama.FILE_MANAGER.manageFile("logs", "u_" + name);
        _playerData.put(id, data);

       data.tracker.write("JOIN");
    }

    public void onLeave(ServerPlayerEntity player)
    {
        DramaPlayerData data = _playerData.get(player.getUniqueID());
        if (data != null) {
            data.tracker.write("LEFT");
        }
    }

    public void trySleepOverride()
    {
        int sleeping = 0;
        int afk = 0;
        ServerWorld overWorld = _server.getWorld(World.OVERWORLD);
        if (overWorld == null || !overWorld.isNightTime())
        {
            return;
        }

        List<ServerPlayerEntity> players = overWorld.getPlayers();
        for (ServerPlayerEntity player : players) {
            if (player.isSleeping()) {
                sleeping++;
            } else {
                DramaPlayerData data = _playerData.get(player.getUniqueID());
                if (data != null && data.isAfk) {
                    afk++;
                }
            }
        }

        if (players.size() > 0 &&
                afk > 0 &&
                (sleeping + afk == players.size())) {
            overWorld.setDayTime(24000);
        }
    }

    public void updatePlayerAfk(ServerPlayerEntity player)
    {
        LocalDateTime now = LocalDateTime.now();
        UUID uuid = player.getUniqueID();
        String name = player.getName().getString();
        DramaPlayerData data = _playerData.get(uuid);

        if (data == null) {
            Drama.LOGGER.warn(MessageFormat.format("PlayerTracker.PROCESS: No player [{0}] ({1})", uuid, name));
        }
        else {
            data.lastSeen = now;
            if (data.lastX != player.lastTickPosX ||
                    data.lastY != player.lastTickPosY ||
                    data.lastZ != player.lastTickPosZ)
            {
                data.lastMove = now;
                data.lastX = player.lastTickPosX;
                data.lastY = player.lastTickPosY;
                data.lastZ = player.lastTickPosZ;

                if (data.isAfk)
                {
                    data.isAfk = false;
                    data.tracker.write("AFK:OFF");
                    Drama.LOGGER.info(MessageFormat.format("PlayerTracker.CHECKAFK: Afk disabled for {0}", data.name));
                }
            }
            else if (!data.isAfk && Duration.between(data.lastMove, now).getSeconds() > _afkDelay)
            {
                data.isAfk = true;
                data.tracker.write("AFK:ON");
                Drama.LOGGER.info(MessageFormat.format("PlayerTracker.CHECKAFK: Akf enabled for {0}", data.name));
            }
        }
    }

    public void trackPlayer(ServerPlayerEntity player, boolean forceFlush)
    {
        BlockPos position = player.getPosition();

        String location = player.world.getDimensionKey().getLocation().toString();
        int pivot = location.indexOf(":");
        int second = location.indexOf('_', pivot);
        pivot = second > 0 ? second : pivot;
        location = location.substring(pivot+1, pivot+4);

        DramaPlayerData data = _playerData.get(player.getUniqueID());
        if (data != null) {
            data.tracker.write("LOC:{0}({1},{2},{3})", location, position.getX(), position.getY(), position.getZ());
            if (forceFlush) {
                data.tracker.flush();
            }
        }
    }
}
