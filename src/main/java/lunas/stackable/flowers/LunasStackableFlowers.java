package lunas.stackable.flowers;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LunasStackableFlowers implements ModInitializer {
	public static final String MOD_ID = "lunas-stackable-flowers";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Lunas Stackable Flowers world!");
	}
}