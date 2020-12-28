package tofu.drama.events;

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
import tofu.drama.Drama;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.FORGE)
public class ModServerEvents {

    @SubscribeEvent
    public static void onEvent(ServerChatEvent event) {
        LocalDateTime now = LocalDateTime.now();
        String message = MessageFormat.format("{0}\t{1}\t{2}\n", now.format(Drama.DATEFORMAT), event.getUsername(), event.getMessage());
try{
        Drama.CHATLOG.write(message);
}catch(Exception e){Drama.LOGGER.error(MessageFormat.format("Chatlog File Error: {0}", e.toString()));}
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

        if (_tickCount % _flushTick == 0) {
            try {
                Drama.CHATLOG.flush();
                Drama.POSITIONLOG.flush();
            } catch (Exception e) {
                Drama.LOGGER.error(MessageFormat.format("Failed to flush chat or position log : {0}", e.toString()));
            }
        }
    }

    private static long _tickCount = 0;
    private static final long _sleepTick = 20 * 10; // 10sec
    private static final long _flushTick = 20 * 10; // 10sec
}
