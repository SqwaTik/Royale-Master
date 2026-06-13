package royale.command.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.command.impl.HelpCommand;
import royale.util.config.ConfigSystem;
import royale.util.config.impl.ConfigPath;

public class ConfigCommand
extends Command {
    private static final String CONFIG_EXTENSION = ".cfg";

    public ConfigCommand() {
        super("config", "Manage client configs", "cfg");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "load": {
                Path legacyConfig;
                if (args.length < 2) {
                    this.logDirect("Usage: config load <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                Path configDir = ConfigPath.getConfigDirectory();
                Path configFile = configDir.resolve(name + CONFIG_EXTENSION);
                if (!Files.exists(configFile, new LinkOption[0]) && Files.exists(legacyConfig = configDir.resolve(name + ".json"), new LinkOption[0])) {
                    configFile = legacyConfig;
                }
                if (!Files.exists(configFile, new LinkOption[0])) {
                    this.logDirect("Config not found: " + name, Formatting.RED);
                    return;
                }
                try {
                    Files.copy(configFile, ConfigPath.getConfigFile(), StandardCopyOption.REPLACE_EXISTING);
                    ConfigSystem.getInstance().load();
                    this.logDirect("Config loaded: " + name);
                }
                catch (Exception e) {
                    this.logDirect("Load failed: " + e.getMessage(), Formatting.RED);
                }
                break;
            }
            case "save": {
                if (args.length < 2) {
                    ConfigSystem.getInstance().save();
                    this.logDirect("Current config saved");
                    return;
                }
                String name = args[1];
                try {
                    Path configDir = ConfigPath.getConfigDirectory();
                    Files.createDirectories(configDir, new FileAttribute[0]);
                    Path targetConfig = configDir.resolve(name + CONFIG_EXTENSION);
                    ConfigSystem.getInstance().save();
                    Files.copy(ConfigPath.getConfigFile(), targetConfig, StandardCopyOption.REPLACE_EXISTING);
                    this.logDirect("Config saved: " + name);
                }
                catch (Exception e) {
                    this.logDirect("Save failed: " + e.getMessage(), Formatting.RED);
                }
                break;
            }
            case "list": {
                List<String> configs;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException e) {
                        // empty catch block
                    }
                }
                if ((configs = this.getConfigs()).isEmpty()) {
                    this.logDirect("No configs found", Formatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(configs);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("CONFIGS");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, config -> {
                    MutableText namesComponent = Text.literal((String)("  \u00a7b\u2022 \u00a7f" + config));
                    MutableText hoverText = Text.literal((String)("\u00a77Click to load \u00a7f" + config));
                    String loadCommand = manager.getPrefix() + "config load " + config;
                    namesComponent.setStyle(namesComponent.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(loadCommand)));
                    return namesComponent;
                }, manager.getPrefix() + label + " list");
                break;
            }
            case "dir": {
                try {
                    Path configDir = ConfigPath.getConfigDirectory();
                    String os = System.getProperty("os.name").toLowerCase(Locale.US);
                    ProcessBuilder pb = os.contains("win") ? new ProcessBuilder("explorer", configDir.toAbsolutePath().toString()) : (os.contains("mac") ? new ProcessBuilder("open", configDir.toAbsolutePath().toString()) : new ProcessBuilder("xdg-open", configDir.toAbsolutePath().toString()));
                    pb.start();
                    this.logDirect("Opened config folder");
                }
                catch (IOException e) {
                    this.logDirect("Cannot open folder: " + e.getMessage(), Formatting.RED);
                }
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("USAGE");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("config load <name> - load config");
                this.logDirect("config save [name] - save current or save as named config");
                this.logDirect("config list [page] - show configs");
                this.logDirect("config dir - open config folder");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        if (args.length == 1) {
            return new TabCompleteHelper().append("load", "save", "list", "dir").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2 && ((action = args[0].toLowerCase(Locale.US)).equals("load") || action.equals("save"))) {
            return new TabCompleteHelper().append(this.getConfigs().toArray(new String[0])).filterPrefix(args[1]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Load, save and list client configs";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Manage client configurations.", "Usage:", "> config load <name>", "> config save [name]", "> config list [page]", "> config dir");
    }

    public List<String> getConfigs() {
        ArrayList<String> configs = new ArrayList<String>();
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            if (Files.exists(configDir, new LinkOption[0])) {
                Files.list(configDir).filter(path -> path.toString().endsWith(CONFIG_EXTENSION) || path.toString().endsWith(".json")).forEach(path -> {
                    String configName;
                    String name = path.getFileName().toString();
                    int dot = name.lastIndexOf(46);
                    String string = configName = dot > 0 ? name.substring(0, dot) : name;
                    if (!configName.equalsIgnoreCase("autoconfig") && configs.stream().noneMatch(existing -> existing.equalsIgnoreCase(configName))) {
                        configs.add(configName);
                    }
                });
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return configs;
    }
}

