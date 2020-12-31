package tofukingkong.drama.events;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.List;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tofukingkong.drama.Drama;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.FORGE)
public class ModServerEvents {

    @SubscribeEvent
    public static void onEvent(ServerChatEvent event) {
        Drama.CHATLOG.write("{0}\t{1}", event.getUsername(), event.getMessage());
    }


// HURT AND DEATH
    // @SubscribeEvent
    // public static void onEvent(LivingHurtEvent event) {
    //     Drama.LOGGER.info("yyy HurtEvent " + event.getEntity() + ":"+event.getEntityLiving() + " for amount: " + event.getAmount() + 
    //     " at X " + event.getEntity().lastTickPosX);
    // }

    // @SubscribeEvent
    // public static void onEvent(LivingDeathEvent event) {
    //     Drama.LOGGER.info("yyy DeathEvent for: " + event.getEntityLiving().getEntityString());
    // }

    // public static void onEvent(EntityInteractEvent event) {
    //     // chests and barrels and furnaces?
    // }

    // public static void onEvent(EntityItemPickupEvent event) {
    //     // take stuff?
    // }

    // public static void onEvent(ExplosionEvent.Detonate event) {

    // }


    @SubscribeEvent
    public static void onEvent(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        _tickCount++;

        if (_tickCount % _sleepTick == 0) {
            Drama._tracker.trySleepOverride();
        }

        if (_tickCount % _chatFlushTick == 0) {
            Drama.CHATLOG.flush();
        }
    }

    private static long _tickCount = 0;
    private static final long _sleepTick = 20 * 10; // 10sec
    private static final long _chatFlushTick = 20 * 10; // 10sec
}
