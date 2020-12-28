package tofu.drama.events;

import java.time.LocalDateTime;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import tofu.drama.Drama;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.FORGE)
public class ModServerEvents {
    // public void onEvent(NameFormat event) {
    //     // use command /afk to toggle afk on and off in username?
    //     // event.displayname = event.username + " [AFK]";
    // }

    @SubscribeEvent
    public static void onEvent(ServerChatEvent event) {
        LocalDateTime now = LocalDateTime.now();
        String message = String.format("%1$s\t[%2$s]\t%3$s\n", now.format(Drama.DATEFORMAT), event.getUsername(), event.getMessage());
try{
        Drama.CHATLOG.write(message);
}catch(Exception e){Drama.LOGGER.error("Chatlog File Error: " + e.toString());};
    }

    // public static void onEvent(EnteringChunk event) {
    //     // maybe not for players?
    // }

    // @SubscribeEvent
    // public static void onEvent(LivingAttackEvent event) {
    //     Drama.LOGGER.info("zzz AttackEvent from src: " + event.getSource());
    // }


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

    // // FMLCommonHandler.instance().bus // cpw.mods.fml.common.gameevent.PlayerEvent
    // public static void onEvent(PlayerEvent event) {

    // }

    // public static void onEvent(PlayerLoggedInEvent event) {

    // }
    
    // public void onEvent(PlayerLoggedOutEvent event) {

    // }
}
