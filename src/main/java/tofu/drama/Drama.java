package tofu.drama;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tofu.drama.config.DramaConfig;
import tofu.drama.events.ModServerEvents;

@Mod("drama")
public class Drama {
    public static final String MOD_ID = "drama";

    public static final DateTimeFormatter DATEFORMAT = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static BufferedWriter CHATLOG;
    public static BufferedWriter POSITIONLOG;

    private MinecraftServer _server = null;
    public static PlayerTracker _tracker = null;
    
    public Drama() throws IOException {
      ModLoadingContext.get().registerConfig(Type.SERVER, DramaConfig.COMMON_SPEC, "drama-server.toml");
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::config);
        
      //MinecraftForge.EVENT_BUS.register(ModPlayerEvents.class);        
      MinecraftForge.EVENT_BUS.register(ModServerEvents.class);
      MinecraftForge.EVENT_BUS.register(this);
      
      CHATLOG = new BufferedWriter(new FileWriter("c:\\mc\\chat.txt", true));
      POSITIONLOG = new BufferedWriter(new FileWriter("c:\\mc\\pos.txt", true));
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) 
    {    
      LOGGER.info("Drama.SETUP");
    }

    @SubscribeEvent
    public void config(ModConfigEvent event) 
    { 
      LOGGER.info("Drama.CONFIG");
      ModConfig config = event.getConfig();
      LOGGER.info("zzz CONFIG IN : " + config.getFullPath());
    }

    @SubscribeEvent
    public void stopping(FMLServerStoppingEvent event)
    {
      LOGGER.info("Drama.STOPPING");
      try
      {
        if (POSITIONLOG != null) POSITIONLOG.flush();
        if (CHATLOG != null) CHATLOG.flush();
      }catch(IOException e)
      {
        Drama.LOGGER.error("CHAT OR POSITION LOG failure to flush : %1s", e);
      }
    }

    @SubscribeEvent
    public void starting(FMLServerStartedEvent event)
    {
      LOGGER.info("Drama.STARTED " + event.getPhase());
      _server = event.getServer();
      _tracker = new PlayerTracker(_server);
      _tracker.start();
    }
}
