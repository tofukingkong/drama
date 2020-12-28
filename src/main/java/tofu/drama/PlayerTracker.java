package tofu.drama;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;

public class PlayerTracker {
    // concurrentmap?  easier to just skip ifRunning
    private final ConcurrentHashMap<UUID,DramaPlayerData> _playerData = new ConcurrentHashMap<UUID,DramaPlayerData>();
    private final Timer _timer;
    private MinecraftServer _server;

    public PlayerTracker(MinecraftServer server) {
        _server = server;
        _timer = new Timer();
    }

    public void start()
    {
        _timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                
                List<ServerPlayerEntity> playerList = _server.getPlayerList().getPlayers();
                for (ServerPlayerEntity player : playerList)
                {
                    processPlayer(player, now);
                }

                try{Drama.POSITIONLOG.flush();Drama.CHATLOG.flush();}catch(IOException e){Drama.LOGGER.error("POSITIONLOG failure to flush : %1s", e);}
            }
        }, 
        30*1000,
        15*1000 
        );
    }

    // TODO - split this out since if someone leaves or changes dimension we might need to sleep also.  Just catch those in the timer expiry for simplicity
    public void onSleep(ServerPlayerEntity player)
    {
        // check to see they are in the overworld, and if everyone in the overworld is asleep or afk
        ResourceLocation location = getLocation(player);
        
        if (!location.toString().contains("overworld"))
        {
            Drama.LOGGER.info("PlayerTracker.SLEEP.CHECK: " + player.getScoreboardName() + " not in overworld");
            return;
        }

        if (!player.world.isNightTime())
        {
            Drama.LOGGER.info("zzz SleepNotNight");
            return;
        }

        for (PlayerEntity other : player.world.getPlayers())
        {
            String name = other.getScoreboardName();

            if (other.isSleeping())
            {
                Drama.LOGGER.info("PlayerTracker.SLEEP: " + name + " Asleep");
                continue;
            }

            DramaPlayerData data = _playerData.get(other.getUniqueID());
            if (data == null) {
                Drama.LOGGER.info("PlayerTracker.SLEEP: " + name + " NotKnown");
                return;
            }
            if (!data.isAfk)
            {
                Drama.LOGGER.info("PlayerTracker.SLEEP: " + name + " NotAFK");
                return;
            }

            Drama.LOGGER.info("PlayerTracker.SLEEP: " + name + " AFK");
        }

        // func_241114_a_ setDayTime()
        player.getServerWorld().func_241114_a_(24000);
    }

    public void onJoin(ServerPlayerEntity player)
    {
        Drama.LOGGER.info("PlayerTracker: Adding player [" + player.getScoreboardName() + "] ");        
        _playerData.put(player.getUniqueID(), new DramaPlayerData(player.getUniqueID(), player.getScoreboardName(), player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ));
    }

    public void onLeave(ServerPlayerEntity player)
    {
        Drama.LOGGER.info("PlayerTracker: Removing player [" + player.getScoreboardName() + "]");
        _playerData.remove(player.getUniqueID());
    }

    private void processPlayer(ServerPlayerEntity player, LocalDateTime now){
        String name = player.getScoreboardName();
        UUID uuid = player.getUniqueID();
        ResourceLocation location = getLocation(player);
        
        String message = String.format("%1$s\t[%2$s]\t%3$s\t%4$s\n", now.format(Drama.DATEFORMAT), name, location.toString(), getPosition(player));
        try{
            Drama.POSITIONLOG.write(message);
        }catch(Exception e){Drama.LOGGER.error("Position File Error: " + e.toString());};

        DramaPlayerData data = _playerData.get(uuid);

        if (data == null) {
            Drama.LOGGER.warn("PlayerTracker.PROCESS: No player [" + uuid + "] (" + name + ")");
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
                    Drama.LOGGER.info("PlayerTracker.PROCESS: Afk disabled for " + data.name);
                    data.isAfk = false;
                }
            }
            else if (Duration.between(data.lastMove, now).getSeconds() > 10)
            {
                data.isAfk = true;                
                Drama.LOGGER.info("PlayerTracker.PROCESS: Akf enabled for " + data.name);
            }
        }
    }


    // func_233580_cy_ getPosition
    // Biome biome = player.world.getBiome(new BlockPos(player.func_233580_cy_()));

    private BlockPos getPosition(ServerPlayerEntity player)
    {        
        // func_233580_cy_ getPosition
        return new BlockPos(player.func_233580_cy_());
    }

    private ResourceLocation getLocation(ServerPlayerEntity player)
    {
        // func_230315_m_ getDimensionType
        DimensionType type = player.world.func_230315_m_();
        // func_242725_p getEffects
        ResourceLocation location = type.func_242725_p();

        return location;
    }
}
