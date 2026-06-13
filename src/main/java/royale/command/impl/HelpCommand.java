package royale.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.util.math.OptionValueUtil;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", "\u041f\u043e\u043a\u0430\u0437\u044b\u0432\u0430\u0435\u0442 \u0441\u043f\u0438\u0441\u043e\u043a \u0432\u0441\u0435\u0445 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0445 \u043a\u043e\u043c\u0430\u043d\u0434", new String[0]);
    }

    @Override
    public void execute(String label, String[] args) {
        CommandManager manager = CommandManager.getInstance();
        if (args.length == 0 || this.isInteger(args[0])) {
            int page = 1;
            if (args.length > 0 && this.isInteger(args[0])) {
                page = Integer.parseInt(args[0]);
            }
            List commands = manager.getCommands().stream().filter(cmd -> !cmd.hiddenFromHelp()).collect(Collectors.toList());
            Paginator<Command> paginator = new Paginator<Command>(commands);
            paginator.setPage(page);
            paginator.display(() -> {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0414\u041e\u0421\u0422\u0423\u041f\u041d\u042b\u0415 \u041a\u041e\u041c\u0410\u041d\u0414\u042b");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            }, command -> {
                String name = command.getName();
                String fullName = manager.getPrefix() + name;
                MutableText shortDescComponent = Text.literal((String)(" \u00a78- \u00a77" + command.getShortDesc()));
                MutableText hoverComponent = Text.literal((String)"");
                hoverComponent.setStyle(hoverComponent.getStyle().withColor(Formatting.GRAY));
                hoverComponent.append((Text)Text.literal((String)fullName).formatted(Formatting.WHITE));
                hoverComponent.append("\n\u00a77" + command.getShortDesc());
                hoverComponent.append("\n\n\u00a78\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u043f\u043e\u043b\u043d\u0443\u044e \u0441\u043f\u0440\u0430\u0432\u043a\u0443 \u043e \u043a\u043e\u043c\u0430\u043d\u0434\u0435");
                String clickCommand = manager.getPrefix() + String.format("%s %s", label, name);
                MutableText component = Text.literal((String)("\u00a7f" + fullName));
                component.append((Text)shortDescComponent);
                component.setStyle(component.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverComponent)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(clickCommand)));
                return component;
            }, manager.getPrefix() + label);
        } else {
            String commandName = args[0].toLowerCase();
            Command command2 = manager.getCommand(commandName);
            if (command2 == null) {
                this.logDirect("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 '" + commandName + "' \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430!", Formatting.RED);
                return;
            }
            this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            this.logDirect("\u00a7f\u00a7l" + command2.getName().toUpperCase());
            this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            List<String> desc = command2.getLongDesc();
            boolean firstLine = true;
            for (String line : desc) {
                if (line.isEmpty()) continue;
                this.logDirect("\u00a77" + line);
                if (!firstLine) continue;
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                firstLine = false;
            }
            this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().filterPrefix(args[0]).addCommands(CommandManager.getInstance()).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u041f\u0440\u043e\u0441\u043c\u043e\u0442\u0440 \u0432\u0441\u0435\u0445 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0445 \u043a\u043e\u043c\u0430\u043d\u0434";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u0421 \u043f\u043e\u043c\u043e\u0449\u044c\u044e \u044d\u0442\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u043c\u043e\u0436\u043d\u043e \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0435\u0442\u044c \u043f\u043e\u0434\u0440\u043e\u0431\u043d\u0443\u044e \u0441\u043f\u0440\u0430\u0432\u043e\u0447\u043d\u0443\u044e \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044e \u043e \u0442\u043e\u043c, \u043a\u0430\u043a \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u043d\u044b\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> help - \u041f\u0435\u0440\u0435\u0447\u0438\u0441\u043b\u044f\u0435\u0442 \u0432\u0441\u0435 \u043a\u043e\u043c\u0430\u043d\u0434\u044b \u0438 \u0438\u0445 \u043a\u0440\u0430\u0442\u043a\u0438\u0435 \u043e\u043f\u0438\u0441\u0430\u043d\u0438\u044f.", "> help <command> - \u041e\u0442\u043e\u0431\u0440\u0430\u0436\u0435\u043d\u0438\u0435 \u0441\u043f\u0440\u0430\u0432\u043e\u0447\u043d\u043e\u0439 \u0438\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u0438 \u043f\u043e \u043a\u043e\u043d\u043a\u0440\u0435\u0442\u043d\u043e\u0439 \u043a\u043e\u043c\u0430\u043d\u0434\u0435.");
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getLine() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) {
            return "\u00a78\u00a7m                    ";
        }
        int chatWidth = OptionValueUtil.toInt(mc.options.getChatWidth().getValue(), 1);
        int scaledWidth = chatWidth * 280 + 40;
        int dashWidth = mc.textRenderer.getWidth("-");
        if (dashWidth <= 0) {
            dashWidth = 4;
        }
        int dashCount = scaledWidth / dashWidth - 2;
        dashCount = Math.max(10, Math.min(dashCount, 80));
        StringBuilder sb = new StringBuilder("\u00a78\u00a7m");
        for (int i = 0; i < dashCount; ++i) {
            sb.append("-");
        }
        return sb.toString();
    }
}

