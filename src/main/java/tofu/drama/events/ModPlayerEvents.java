package tofu.drama.events;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import tofu.drama.Drama;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.FORGE)
public class ModPlayerEvents {

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

    public static void onEvent(PlayerTickEvent event) {
        if (!event.player.isServerWorld())
        {
            return;
        }

        Drama.LOGGER.info("zzz PlayerTick : " + event.getClass() + ":: " + event.player + "::" + event.side);
    }

    @SubscribeEvent
    public static void onEvent(PlayerSleepInBedEvent event)
    {
        if (!event.getPlayer().isServerWorld())
        {
            return;
        }

        if (event.hasResult()) {
            Drama.LOGGER.info("zzz SLEEP EVENT: " + event.getResultStatus().name());
        }
        Drama._tracker.onSleep((ServerPlayerEntity)event.getPlayer());
    }

}
