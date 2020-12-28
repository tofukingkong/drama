package tofu.drama;

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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
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

    public static PlayerTracker _tracker = null;
    
    public Drama() {
      ModLoadingContext.get().registerConfig(Type.SERVER, DramaConfig.COMMON_SPEC, "drama-server.toml");
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::config);
        
      //MinecraftForge.EVENT_BUS.register(ModPlayerEvents.class);        
      MinecraftForge.EVENT_BUS.register(ModServerEvents.class);
      MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event)
    {
        LOGGER.info("Drama.SETUP");

        String chatFile = ".\\logs\\chat.txt";
        String posFile = ".\\logs\\pos.txt";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyMMdd.HHmmss");
        String formattedTime = LocalDateTime.now().format(format);
        File file = new File(chatFile);
        if(file.exists()) {
            boolean result = file.renameTo(new File(MessageFormat.format(".\\logs\\chat.{0}.txt", formattedTime)));
            if (!result){
                LOGGER.warn("Couldn't rename chat.txt");
            }
        }

        file = new File(posFile);
        if(file.exists()) {
            boolean result = file.renameTo(new File(MessageFormat.format(".\\logs\\pos.{0}.txt", formattedTime)));
            LOGGER.warn("Couldn't rename pos.txt");
        }

        try {
            CHATLOG = new BufferedWriter(new FileWriter(chatFile, true));
            POSITIONLOG = new BufferedWriter(new FileWriter(posFile, true));
        }catch(Exception e) {
            LOGGER.error("Failed to set up chatlog or positionlog", e);
        }
    }

    @SubscribeEvent
    public void config(ModConfigEvent event) 
    { 
      LOGGER.info("Drama.CONFIG");
    }

    @SubscribeEvent
    public void stopping(FMLServerStoppingEvent event)
    {
      LOGGER.info("Drama.STOPPING");
      try
      {
        if (POSITIONLOG != null) {POSITIONLOG.flush();POSITIONLOG.close();}
        if (CHATLOG != null) {CHATLOG.flush();CHATLOG.close();}
      }catch(IOException e) {
        Drama.LOGGER.error("CHAT OR POSITION LOG failure to flush or close", e);
      }
    }

    @SubscribeEvent
    public void starting(FMLServerStartedEvent event) {
        LOGGER.info("Drama.STARTED");
        _tracker = new PlayerTracker(event.getServer(), DramaConfig.COMMON.afkDelay.get());
    }
}
