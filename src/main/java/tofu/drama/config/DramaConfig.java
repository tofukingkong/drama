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
        
		public final IntValue some_int;

        public Common(ForgeConfigSpec.Builder builder) {
			builder.comment("Drama Mod Configurations").push("drama");
			
			some_int = builder
				.comment("Just a test")
				.translation("drama.configgui.some_int")
				.worldRestart()
				.defineInRange("some_int", 3, 3, 5);

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

    @SubscribeEvent
	public static void onLoad(final ModConfig.Loading event) {
		Drama.LOGGER.info("zzz CONFIG ONLOAD");
	}
	
	// @SubscribeEvent
	// public static void onFileChange(final ModConfig.ConfigReloading event) {
		
	// }
}
