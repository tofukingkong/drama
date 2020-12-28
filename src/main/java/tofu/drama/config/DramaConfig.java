package tofu.drama.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;

import tofu.drama.Drama;

@Mod.EventBusSubscriber(modid = Drama.MOD_ID, bus = Bus.MOD)
public class DramaConfig {
    public static class Common{
        
		public final IntValue afkDelay;

        public Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Drama Mod Configurations").push("drama");
			
			afkDelay = builder
				.comment("Duration (seconds) with no movement before a player is considered AFK")
				.translation("drama.configgui.afk_delay")
				.worldRestart()
				.defineInRange("afk_delay", 10, 1, 3600);

			builder.pop();
        }
    }

	public static final ForgeConfigSpec COMMON_SPEC;
	public static final Common COMMON;
	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}

    //@SubscribeEvent
	//public static void onLoad(final ModConfig.Loading event)
	//{

	//}
	
	// @SubscribeEvent
	// public static void onFileChange(final ModConfig.ConfigReloading event) {
		
	// }
}
