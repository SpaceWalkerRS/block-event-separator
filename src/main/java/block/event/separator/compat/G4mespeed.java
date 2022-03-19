package block.event.separator.compat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.fabricmc.loader.api.FabricLoader;

public class G4mespeed {

	private static final String MOD_ID = "g4mespeed";

	private static Object blockEventDistance_object;
	private static Method getValue_method;

	public static void init() {
		if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
			return;
		}

		try {
			Class<?> GSServerController = Class.forName("com.g4mesoft.core.server.GSServerController");
			Class<?> GSTpsModule = Class.forName("com.g4mesoft.module.tps.GSTpsModule");
			Class<?> GSIntegerSetting = Class.forName("com.g4mesoft.setting.types.GSIntegerSetting");

			Method getInstance = GSServerController.getMethod("getInstance");
			Method getTpsModule = GSServerController.getMethod("getTpsModule");
			Method getValue = GSIntegerSetting.getMethod("getValue");

			Field sBlockEventDistance = GSTpsModule.getField("sBlockEventDistance");

			Object server = getInstance.invoke(null);
			Object tpsModule = getTpsModule.invoke(server);
			Object blockEventDistance = sBlockEventDistance.get(tpsModule);

			blockEventDistance_object = blockEventDistance;
			getValue_method = getValue;
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {

		}
	}

	public static float getBlockEventDistance(float orDefault) {
		if (getValue_method != null) {
			try {
				int chunkDistance = (int)getValue_method.invoke(blockEventDistance_object);
				return chunkDistance * 16;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {

			}
		}

		return orDefault;
	}
}
