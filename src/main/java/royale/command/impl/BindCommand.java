package royale.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import royale.Initialization;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.command.impl.HelpCommand;
import royale.modules.module.ModuleRepository;
import royale.modules.module.ModuleStructure;
import royale.util.config.ConfigSystem;
import royale.util.config.impl.bind.BindConfig;
import royale.util.string.KeyHelper;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u0438\u043d\u0434\u0430\u043c\u0438 \u043c\u043e\u0434\u0443\u043b\u0435\u0439", "b");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        ModuleRepository repository = this.getModuleRepository();
        if (repository == null) {
            this.logDirect("\u0420\u0435\u043f\u043e\u0437\u0438\u0442\u043e\u0440\u0438\u0439 \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!", Formatting.RED);
            return;
        }
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 3) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: bind add <module> <key>", Formatting.RED);
                    return;
                }
                String moduleName = args[1];
                String keyName = args[2];
                ModuleStructure module2 = this.findModule(repository, moduleName);
                if (module2 == null) {
                    this.logDirect(String.format("\u041c\u043e\u0434\u0443\u043b\u044c %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!", moduleName), Formatting.RED);
                    return;
                }
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043b\u0430\u0432\u0438\u0448\u0430: %s", keyName), Formatting.RED);
                    return;
                }
                module2.setKey(key);
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("\u00a7a\u041c\u043e\u0434\u0443\u043b\u044c \u00a7f%s \u00a7a\u043f\u0440\u0438\u0432\u044f\u0437\u0430\u043d \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435 \u00a7f%s", module2.getName(), KeyHelper.getKeyName(key).toLowerCase()), Formatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: bind remove <module>", Formatting.RED);
                    return;
                }
                String moduleName = args[1];
                ModuleStructure module3 = this.findModule(repository, moduleName);
                if (module3 == null) {
                    this.logDirect(String.format("\u041c\u043e\u0434\u0443\u043b\u044c %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!", moduleName), Formatting.RED);
                    return;
                }
                module3.setKey(-1);
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("\u0411\u0438\u043d\u0434 \u0434\u043b\u044f \u043c\u043e\u0434\u0443\u043b\u044f %s \u0443\u0434\u0430\u043b\u0435\u043d!", module3.getName()), Formatting.GREEN);
                break;
            }
            case "clear": {
                int count = 0;
                for (ModuleStructure module4 : repository.modules()) {
                    if (module4.getKey() == -1) continue;
                    module4.setKey(-1);
                    ++count;
                }
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("\u0412\u0441\u0435 \u0431\u0438\u043d\u0434\u044b \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u0443\u0434\u0430\u043b\u0435\u043d\u044b! \u0423\u0434\u0430\u043b\u0435\u043d\u043e: %d", count), Formatting.GREEN);
                break;
            }
            case "set": {
                if (args.length < 3) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: bind set <target> <key>", Formatting.RED);
                    this.logDirect("\u0414\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0435 \u0446\u0435\u043b\u0438: Bind", Formatting.RED);
                    return;
                }
                String target = args[1].toLowerCase(Locale.US);
                String keyName = args[2];
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043b\u0430\u0432\u0438\u0448\u0430: %s", keyName), Formatting.RED);
                    return;
                }
                if (target.equals("Bind")) {
                    BindConfig.getInstance().setKeyAndSave(key);
                    this.logDirect(String.format("\u00a7a\u041a\u043b\u0430\u0432\u0438\u0448\u0430 \u0434\u043b\u044f Bind \u0438\u0437\u043c\u0435\u043d\u0435\u043d\u0430 \u043d\u0430: \u00a7f%s", KeyHelper.getKeyName(key).toLowerCase()), Formatting.GREEN);
                    break;
                }
                this.logDirect(String.format("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u0446\u0435\u043b\u044c: %s", target), Formatting.RED);
                break;
            }
            case "list": {
                List boundModules;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException keyName) {
                        // empty catch block
                    }
                }
                if ((boundModules = repository.modules().stream().filter(m -> m.getKey() != -1 && m.getKey() != -1).collect(Collectors.toList())).isEmpty()) {
                    this.logDirect("\u041d\u0435\u0442 \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u0441 \u0431\u0438\u043d\u0434\u0430\u043c\u0438!", Formatting.RED);
                    return;
                }
                Paginator<ModuleStructure> paginator = new Paginator<ModuleStructure>(boundModules);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7l\u0421\u041f\u0418\u0421\u041e\u041a \u0411\u0418\u041d\u0414\u041e\u0412 \u00a77(" + boundModules.size() + ")");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, module -> {
                    String name = module.getName();
                    String keyName = KeyHelper.getKeyName(module.getKey()).toLowerCase();
                    MutableText component = Text.literal((String)("  \u00a7b\u25cf \u00a7f" + name)).append((Text)Text.literal((String)(" \u00a78[\u00a77" + keyName + "\u00a78]")));
                    MutableText hoverText = Text.literal((String)("\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0447\u0442\u043e\u0431\u044b \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0431\u0438\u043d\u0434 \u0434\u043b\u044f \u00a7f" + name));
                    String removeCommand = manager.getPrefix() + "bind remove " + name;
                    component.setStyle(component.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0423\u041f\u0420\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0411\u0418\u041d\u0414\u0410\u041c\u0418");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> bind add <module> <key> \u00a78- \u00a7f\u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435");
                this.logDirect("\u00a77> bind remove <module> \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0431\u0438\u043d\u0434 \u043c\u043e\u0434\u0443\u043b\u044f");
                this.logDirect("\u00a77> bind list \u00a78- \u00a7f\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0431\u0438\u043d\u0434\u043e\u0432");
                this.logDirect("\u00a77> bind clear \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0431\u0438\u043d\u0434\u044b");
                this.logDirect("\u00a77> bind set Bind <key> \u00a78- \u00a7f\u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043a\u043b\u0430\u0432\u0438\u0448\u0443 Bind");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        ModuleRepository repository = this.getModuleRepository();
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear", "set").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2) {
            action = args[0].toLowerCase();
            if (action.equals("add")) {
                return new TabCompleteHelper().append(this.getModuleNames(repository)).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(this.getBoundModuleNames(repository)).filterPrefix(args[1]).stream();
            }
            if (action.equals("set")) {
                return new TabCompleteHelper().append("Bind").filterPrefix(args[1]).stream();
            }
        }
        if (args.length == 3 && ((action = args[0].toLowerCase()).equals("add") || action.equals("set"))) {
            return new TabCompleteHelper().append(KeyHelper.getAllKeyNames()).filterPrefix(args[2]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0431\u0438\u043d\u0434\u0430\u043c\u0438 \u043c\u043e\u0434\u0443\u043b\u0435\u0439";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u0431\u0438\u043d\u0434\u0430\u043c\u0438 \u043c\u043e\u0434\u0443\u043b\u0435\u0439 \u0438 GUI", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> bind add <module> <key> - \u041f\u0440\u0438\u0432\u044f\u0437\u0430\u0442\u044c \u043c\u043e\u0434\u0443\u043b\u044c \u043a \u043a\u043b\u0430\u0432\u0438\u0448\u0435", "> bind remove <module> - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0431\u0438\u043d\u0434 \u043c\u043e\u0434\u0443\u043b\u044f", "> bind list - \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0431\u0438\u043d\u0434\u043e\u0432", "> bind clear - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0431\u0438\u043d\u0434\u044b", "> bind set Bind <key> - \u0418\u0437\u043c\u0435\u043d\u0438\u0442\u044c \u043a\u043b\u0430\u0432\u0438\u0448\u0443 Bind");
    }

    private ModuleRepository getModuleRepository() {
        Initialization instance = Initialization.getInstance();
        if (instance != null && instance.getManager() != null) {
            return instance.getManager().getModuleRepository();
        }
        return null;
    }

    private ModuleStructure findModule(ModuleRepository repository, String name) {
        return repository.modules().stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private String[] getModuleNames(ModuleRepository repository) {
        if (repository == null) {
            return new String[0];
        }
        return (String[])repository.modules().stream().map(ModuleStructure::getName).toArray(String[]::new);
    }

    private String[] getBoundModuleNames(ModuleRepository repository) {
        if (repository == null) {
            return new String[0];
        }
        return (String[])repository.modules().stream().filter(m -> m.getKey() != -1 && m.getKey() != -1).map(ModuleStructure::getName).toArray(String[]::new);
    }
}

