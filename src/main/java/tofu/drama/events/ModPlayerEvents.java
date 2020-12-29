package tofu.drama.events;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import org.spongepowered.asm.mixin.MixinEnvironment;
import tofu.drama.Drama;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.FORGE)
public class ModPlayerEvents {

    private static final HashMap<UUID, Long> _ticks = new HashMap<>();

    public void onEvent(PlayerEvent.NameFormat event) {
        // use command /afk to toggle afk on and off in username?
        // event.displayname = event.username + " [AFK]";
    }

    @SubscribeEvent
    public static void onEvent(PlayerLoggedInEvent event) {
        if (!event.getPlayer().isServerWorld())
        {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Drama.LOGGER.info("Drama.LogIn : " + player.getScoreboardName());
        Drama._tracker.onJoin(player);
    }

    @SubscribeEvent
    public static void onEvent(PlayerLoggedOutEvent event) {
        if (!event.getPlayer().isServerWorld())
        {
            return;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
        Drama.LOGGER.info("Drama.LogOut : " + player.getScoreboardName());
        Drama._tracker.onLeave(player);
    }

    public static void onEvent(PlayerEvent event) {
        if (!event.getPlayer().isServerWorld())
        {
            return;
        }
        
        Drama.LOGGER.info("zzz PlayerEvent : " + event.getClass() + "::  " + event.getPlayer() + ":" + event.getPhase() + ":" + (event.hasResult() ? event.getResult() : "NO RESULT"));
    }

    @SubscribeEvent
    public static void onEvent(PlayerContainerEvent event) {
        if (!event.getPlayer().isServerWorld())
        {
            return;
        }

        Drama.LOGGER.info("zzz PlayerContainerEvent : " + event.getClass() + "::  " + event.getContainer().getType() + "::" + event.getPlayer());
    }

    @SubscribeEvent
    public static void onEvent(PlayerTickEvent event) {
        if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.START)
        {
            return;
        }

        int tickCount = event.player.getServer().getTickCounter();

        if (tickCount % _trackingTick == 0) {
            Drama._tracker.trackPlayer((ServerPlayerEntity)event.player, tickCount % _trackingFlush == 0);
        }

        if (tickCount % _afkTick == 0) {
            Drama._tracker.updatePlayerAfk((ServerPlayerEntity)event.player);
        }
    }

    private static final long _trackingTick = 20 * 20; // 20sec
    private static final long _trackingFlush = 20 * 60; // 60sec
    private static final long _afkTick = 20 * 10; // 10sec
}
