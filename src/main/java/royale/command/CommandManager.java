package royale.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import royale.command.Command;
import royale.command.impl.BindCommand;
import royale.command.impl.ConfigCommand;
import royale.command.impl.FriendCommand;
import royale.command.impl.HelpCommand;
import royale.command.impl.MacroCommand;
import royale.command.impl.PrefixCommand;
import royale.command.impl.PlayerCommand;
import royale.command.impl.WayCommand;
import royale.events.api.EventHandler;
import royale.events.api.EventManager;
import royale.events.impl.ChatEvent;
import royale.events.impl.TabCompleteEvent;
import royale.util.config.impl.prefix.PrefixConfig;
import royale.util.string.chat.ChatMessage;

public class CommandManager {
    private static CommandManager instance;
    private final List<Command> commands;
    private String prefix;

    public CommandManager() {
        instance = this;
        this.commands = new CopyOnWriteArrayList<Command>();
        this.prefix = PrefixConfig.getInstance().getPrefix();
    }

    public static CommandManager getInstance() {
        return instance;
    }

    public void init() {
        this.registerCommand(new HelpCommand());
        this.registerCommand(new ConfigCommand());
        this.registerCommand(new FriendCommand());
        this.registerCommand(new MacroCommand());
        this.registerCommand(new BindCommand());
        this.registerCommand(new PrefixCommand());
        this.registerCommand(new WayCommand());
        this.registerCommand(new PlayerCommand());
        EventManager.register(this);
    }

    public void registerCommand(Command command) {
        this.commands.add(command);
    }

    public void unregisterCommand(Command command) {
        this.commands.remove(command);
    }

    public Command getCommand(String name) {
        return this.commands.stream().filter(cmd -> cmd.matches(name)).findFirst().orElse(null);
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(this.commands);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String msg = event.getMessage();
        if (msg.startsWith(this.prefix)) {
            event.cancel();
            String commandStr = msg.substring(this.prefix.length());
            if (commandStr.trim().isEmpty()) {
                this.execute("help");
                return;
            }
            if (!this.execute(commandStr)) {
                this.sendError("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043e\u043c\u0430\u043d\u0434\u0430. \u0418\u0441\u043f\u043e\u043b\u044c\u0437\u0443\u0439\u0442\u0435 " + this.prefix + "help \u0434\u043b\u044f \u0441\u043f\u0438\u0441\u043a\u0430 \u043a\u043e\u043c\u0430\u043d\u0434.");
            }
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String eventPrefix = event.prefix;
        if (!eventPrefix.startsWith(this.prefix)) {
            return;
        }
        String msg = eventPrefix.substring(this.prefix.length());
        Stream<String> stream = this.tabComplete(msg);
        String[] parts = msg.split(" ", -1);
        if (parts.length <= 1) {
            stream = stream.map(x -> this.prefix + x);
        }
        event.completions = (String[])stream.toArray(String[]::new);
    }

    public boolean execute(String input) {
        if (input == null || input.trim().isEmpty()) {
            return this.execute("help");
        }
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[]{};
        Command command = this.getCommand(commandName);
        if (command != null) {
            try {
                command.execute(commandName, args);
                return true;
            }
            catch (Exception e) {
                this.sendError("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u0432\u044b\u043f\u043e\u043b\u043d\u0435\u043d\u0438\u0438 \u043a\u043e\u043c\u0430\u043d\u0434\u044b: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public Stream<String> tabComplete(String input) {
        String[] args;
        if (input == null) {
            input = "";
        }
        if ((args = input.split("\\s+", -1)).length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            return this.getCommandSuggestions(partial);
        }
        String commandName = args[0];
        Command command = this.getCommand(commandName);
        if (command != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return command.tabComplete(commandName, subArgs);
        }
        return Stream.empty();
    }

    private Stream<String> getCommandSuggestions(String partial) {
        if (partial.isEmpty()) {
            return this.commands.stream().map(Command::getName).sorted();
        }
        LinkedHashSet<String> suggestions = new LinkedHashSet<String>();
        block0: for (Command cmd : this.commands) {
            String mainName = cmd.getName();
            if (mainName.toLowerCase().startsWith(partial)) {
                suggestions.add(mainName);
                continue;
            }
            for (String alias : cmd.getAliases()) {
                if (!alias.toLowerCase().startsWith(partial)) continue;
                suggestions.add(alias);
                continue block0;
            }
        }
        return suggestions.stream().sorted();
    }

    public void sendMessage(String message) {
        ChatMessage.brandmessage(message);
    }

    public void sendSuccess(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MutableText prefixText = ChatMessage.brandmessage();
            MutableText formattedMessage = prefixText.copy().append((Text)Text.literal((String)" -> ").formatted(Formatting.DARK_GRAY)).append((Text)Text.literal((String)message).formatted(Formatting.GREEN));
            MinecraftClient.getInstance().player.sendMessage((Text)formattedMessage, false);
        }
    }

    public void sendError(String message) {
        if (MinecraftClient.getInstance().player != null) {
            MutableText prefixText = ChatMessage.brandmessage();
            MutableText formattedMessage = prefixText.copy().append((Text)Text.literal((String)" -> ").formatted(Formatting.DARK_GRAY)).append((Text)Text.literal((String)message).formatted(Formatting.RED));
            MinecraftClient.getInstance().player.sendMessage((Text)formattedMessage, false);
        }
    }

    public void sendRaw(Text text) {
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player.sendMessage(text, false);
        }
    }
}

