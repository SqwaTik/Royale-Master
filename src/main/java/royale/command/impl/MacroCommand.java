package royale.command.impl;

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
import royale.util.repository.macro.Macro;
import royale.util.repository.macro.MacroRepository;
import royale.util.string.KeyHelper;

public class MacroCommand
extends Command {
    public MacroCommand() {
        super("macro", "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u0430\u043c\u0438", "macros");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        MacroRepository macroRepository = MacroRepository.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 4) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: macro add <key> <name> <message>", Formatting.RED);
                    return;
                }
                String keyName = args[1];
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("\u041d\u0435\u0438\u0437\u0432\u0435\u0441\u0442\u043d\u0430\u044f \u043a\u043b\u0430\u0432\u0438\u0448\u0430: %s", keyName), Formatting.RED);
                    return;
                }
                String name = args[2];
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 3; i < args.length; ++i) {
                    if (i > 3) {
                        messageBuilder.append(" ");
                    }
                    messageBuilder.append(args[i]);
                }
                String message = messageBuilder.toString();
                if (macroRepository.hasMacro(name)) {
                    this.logDirect(String.format("\u041c\u0430\u043a\u0440\u043e\u0441 \u0441 \u0438\u043c\u0435\u043d\u0435\u043c %s \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!", name), Formatting.RED);
                    return;
                }
                macroRepository.addMacroAndSave(name, message, key);
                this.logDirect(String.format("\u00a7a\u0414\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u043c\u0430\u043a\u0440\u043e\u0441 \u00a7f%s \u00a7a\u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0448\u0443 \u00a7f%s \u00a7a\u0441 \u043a\u043e\u043c\u0430\u043d\u0434\u043e\u0439 \u00a7f%s", name, KeyHelper.getKeyName(key).toLowerCase(), message), Formatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: macro remove <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (!macroRepository.hasMacro(name)) {
                    this.logDirect(String.format("\u041c\u0430\u043a\u0440\u043e\u0441 %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d!", name), Formatting.RED);
                    return;
                }
                macroRepository.deleteMacroAndSave(name);
                this.logDirect(String.format("\u041c\u0430\u043a\u0440\u043e\u0441 %s \u0443\u0434\u0430\u043b\u0435\u043d!", name), Formatting.GREEN);
                break;
            }
            case "clear": {
                int count = macroRepository.size();
                macroRepository.clearListAndSave();
                this.logDirect(String.format("\u0412\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b \u0443\u0434\u0430\u043b\u0435\u043d\u044b! \u0423\u0434\u0430\u043b\u0435\u043d\u043e: %d", count), Formatting.GREEN);
                break;
            }
            case "list": {
                List<Macro> macros;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException key) {
                        // empty catch block
                    }
                }
                if ((macros = macroRepository.getMacroList()).isEmpty()) {
                    this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432 \u043f\u0443\u0441\u0442!", Formatting.RED);
                    return;
                }
                Paginator<Macro> paginator = new Paginator<Macro>(macros);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7l\u0421\u041f\u0418\u0421\u041e\u041a \u041c\u0410\u041a\u0420\u041e\u0421\u041e\u0412 \u00a77(" + macros.size() + ")");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, macro -> {
                    String macroName = macro.name();
                    String keyName = KeyHelper.getKeyName(macro.key()).toLowerCase();
                    String message = macro.message();
                    MutableText component = Text.literal((String)("  \u00a7e\u25cf \u00a7f" + macroName)).append((Text)Text.literal((String)(" \u00a78[\u00a77" + keyName + "\u00a78]"))).append((Text)Text.literal((String)(" \u00a78-> \u00a77" + message)));
                    MutableText hoverText = Text.literal((String)("\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0447\u0442\u043e\u0431\u044b \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441 \u00a7f" + macroName));
                    String removeCommand = manager.getPrefix() + "macro remove " + macroName;
                    component.setStyle(component.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0423\u041f\u0420\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u041c\u0410\u041a\u0420\u041e\u0421\u0410\u041c\u0418");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> macro add <key> <name> <message> \u00a78- \u00a7f\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441");
                this.logDirect("\u00a77> macro remove <name> \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441");
                this.logDirect("\u00a77> macro list \u00a78- \u00a7f\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432");
                this.logDirect("\u00a77> macro clear \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("add")) {
                return new TabCompleteHelper().append(KeyHelper.getAllKeyNames()).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(MacroRepository.getInstance().getMacroNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u0430\u043c\u0438";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u043c\u0430\u043a\u0440\u043e\u0441\u0430\u043c\u0438", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> macro add <key> <name> <message> - \u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441", "> macro remove <name> - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u043c\u0430\u043a\u0440\u043e\u0441", "> macro list - \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u043c\u0430\u043a\u0440\u043e\u0441\u043e\u0432", "> macro clear - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u043c\u0430\u043a\u0440\u043e\u0441\u044b");
    }
}

