package tofu.drama;


import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
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
        Drama.LOGGER.info(MessageFormat.format("PlayerTracker: Adding player {0}",  player.getName().getString()));
        _playerData.put(player.getUniqueID(), new DramaPlayerData(player.getUniqueID(), player.getName().getString(), player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ));
    }

    public void onLeave(ServerPlayerEntity player)
    {
        Drama.LOGGER.info(MessageFormat.format("PlayerTracker: Removing player {0}",player.getName().getString()));
        _playerData.remove(player.getUniqueID());
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
                    Drama.LOGGER.info(MessageFormat.format("PlayerTracker.CHECKAFK: Afk disabled for {0}", data.name));
                    data.isAfk = false;
                }
            }
            else if (!data.isAfk && Duration.between(data.lastMove, now).getSeconds() > _afkDelay)
            {
                data.isAfk = true;
                Drama.LOGGER.info(MessageFormat.format("PlayerTracker.CHECKAFK: Akf enabled for {0}", data.name));
            }
        }
    }

    public void trackPlayer(ServerPlayerEntity player)
    {
        LocalDateTime now = LocalDateTime.now();
        String name = player.getName().getString();

        ResourceLocation location = player.world.getDimensionKey().getLocation();
        String message = MessageFormat.format("{0}\t{1}\t{2}\t{3}\n", now.format(Drama.DATEFORMAT), name, location.toString(), new BlockPos(player.getPosition()));
        try{
            Drama.POSITIONLOG.write(message);
        }catch(Exception e){
            Drama.LOGGER.error("Position File Error: " + e.toString());
        }
    }
}
