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

import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;

public class KeyBindings {

	private static final String FILE_NAME = "hotkeys.txt";

	private static final Set<String> CATEGORIES;
	private static final Map<String, KeyBinding> BINDINGS;

	public static final String MAIN;

	public static final KeyBinding TOGGLE_ANIMATION_MODE;

	private static String registerCategory(String category) {
		if (!CATEGORIES.add(category)) {
			throw new IllegalArgumentException("Cannot register multiple key binding categories with the same name!");
		}

		return category;
	}

	private static KeyBinding registerBinding(KeyBinding binding) {
		if (BINDINGS.putIfAbsent(binding.getKeyDescription(), binding) != null) {
			throw new IllegalArgumentException("Cannot register multiple key bindings with the same name!");
		}

		return binding;
	}

	public static Collection<String> getCategories() {
		return Collections.unmodifiableSet(CATEGORIES);
	}

	public static Collection<KeyBinding> getKeyBindings() {
		return Collections.unmodifiableCollection(BINDINGS.values());
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

				String bindingName = args[0];
				String keyName = args[1];

				KeyBinding binding = BINDINGS.get(bindingName);
				int key = Integer.parseInt(keyName);

				if (binding != null) {
					binding.setKeyCode(key);
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
			for (KeyBinding binding : BINDINGS.values()) {
				String bindingName = binding.getKeyDescription();
				int key = binding.getKeyCode();

				bw.write(bindingName + "=" + key);
				bw.newLine();
			}
		} catch (IOException e) {

		}
	}

	static {

		CATEGORIES = new LinkedHashSet<>();
		BINDINGS = new LinkedHashMap<>();

		MAIN = registerCategory(BlockEventSeparatorMod.MOD_NAME);

		TOGGLE_ANIMATION_MODE = registerBinding(new KeyBinding("Toggle Animation Mode", Keyboard.KEY_NONE, MAIN));

	}
}
