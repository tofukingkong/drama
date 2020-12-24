package tofu.drama;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("drama")
public class Drama {
    public static final String MOD_ID = "drama";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public Drama() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    

    // public void onEvent(NameFormat event) {
    //     // use command /afk to toggle afk on and off in username?
    //     // event.displayname = event.username + " [AFK]";
    // }

    @SubscribeEvent
    public void onEvent(ServerChatEvent event) {
        String message = event.getUsername() + ":" + event.getMessage();
        
    }

    // public void onEvent(EnteringChunk event) {
    //     // maybe not for players?
    // }

    // public void onEvent(LivingDeathEvent event) {
    //     // creeper explosions
    // }

    // public void onEvent(EntityInteractEvent event) {
    //     // chests and barrels and furnaces?
    // }

    // public void onEvent(EntityItemPickupEvent event) {
    //     // take stuff?
    // }

    // public void onEvent(PlayerDestroyItemEvent event) {
    //     // ??
    // }

    // public void onEvent(PlayerInteractEvent event) {
    //     //RIGHT_CLICK_BLOCK
    // }

    // public void onEvent(PlayerOpenContainerEvent event) {

    // }

    // public void onEvent(PlayerSleepInBedEvent event) {

    // }

    // public void onEvent(ExplosionEvent.Detonate event) {

    // }

    // // FMLCommonHandler.instance().bus // cpw.mods.fml.common.gameevent.PlayerEvent
    // public void onEvent(PlayerEvent event) {

    // }

    // public void onEvent(PlayerLoggedInEvent event) {

    // }
    
    // public void onEvent(PlayerLoggedOutEvent event) {

    // }
}
