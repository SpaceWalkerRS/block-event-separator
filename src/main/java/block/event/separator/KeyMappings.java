package block.event.separator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.KeyMapping;

public class KeyMappings {

	private static final String FILE_NAME = "hotkeys.txt";

	private static final Set<String> CATEGORIES;
	private static final Map<String, KeyMapping> MAPPINGS;

	public static final String MAIN;

	public static final KeyMapping TOGGLE_ANIMATION_MODE;

	private static String registerCategory(String category) {
		if (!CATEGORIES.add(category)) {
			throw new IllegalArgumentException("Cannot register multiple key mapping categories with the same name!");
		}

		return category;
	}

	private static KeyMapping registerMapping(KeyMapping mapping) {
		if (MAPPINGS.putIfAbsent(mapping.getName(), mapping) != null) {
			throw new IllegalArgumentException("Cannot register multiple key mappings with the same name!");
		}

		return mapping;
	}

	public static Collection<String> getCategories() {
		return Collections.unmodifiableSet(CATEGORIES);
	}

	public static Collection<KeyMapping> getKeyMappings() {
		return Collections.unmodifiableCollection(MAPPINGS.values());
	}

	public static void load(File folder) {
		File file = new File(folder, FILE_NAME);

		if (!file.exists()) {
			save(folder);
			return;
		}

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = br.readLine()) != null) {
				String[] args = line.split("=", 2);

				if (args.length < 2) {
					continue;
				}

				String mappingName = args[0];
				String keyName = args[1];

				KeyMapping mapping = MAPPINGS.get(mappingName);
				Key key = InputConstants.getKey(keyName);

				if (mapping != null && key != null) {
					mapping.setKey(key);
				}
			}
		} catch (IOException e) {

		}
	}

	public static void save(File folder) {
		if (!folder.exists()) {
			folder.mkdirs();
		}

		File file = new File(folder, FILE_NAME);

		try {
			file.createNewFile();
		} catch (IOException e) {

		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			for (KeyMapping mapping : MAPPINGS.values()) {
				String mappingName = mapping.getName();
				String keyName = mapping.saveString();

				bw.write(mappingName + "=" + keyName);
				bw.newLine();
			}
		} catch (IOException e) {

		}
	}

	static {

		CATEGORIES = new LinkedHashSet<>();
		MAPPINGS = new LinkedHashMap<>();

		MAIN = registerCategory(BlockEventSeparatorMod.MOD_NAME);

		TOGGLE_ANIMATION_MODE = registerMapping(new KeyMapping("Toggle Animation Mode", GLFW.GLFW_KEY_UNKNOWN, MAIN));

	}
}
