package royale.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import royale.IMinecraft;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.command.impl.HelpCommand;
import royale.util.config.impl.way.WayConfig;
import royale.util.repository.way.Way;
import royale.util.repository.way.WayRepository;

public class WayCommand
extends Command
implements IMinecraft {
    public WayCommand() {
        super("way", "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0442\u043e\u0447\u043a\u0430\u043c\u0438 \u043d\u0430 \u043a\u0430\u0440\u0442\u0435", "waypoint", "wp");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        WayRepository repository = WayRepository.getInstance();
        if (WayCommand.mc.player == null) {
            this.logDirect("\u0412\u044b \u0434\u043e\u043b\u0436\u043d\u044b \u0431\u044b\u0442\u044c \u0432 \u0438\u0433\u0440\u0435!", Formatting.RED);
            return;
        }
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                BlockPos pos;
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: way add <name> [x] [y] [z]", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (args.length >= 5) {
                    try {
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        pos = new BlockPos(x, y, z);
                    }
                    catch (NumberFormatException e) {
                        this.logDirect("\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0435 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b!", Formatting.RED);
                        return;
                    }
                } else {
                    pos = WayCommand.mc.player.getBlockPos();
                }
                String server = repository.getCurrentServer();
                if (server.isEmpty()) {
                    this.logDirect("\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u0442\u044c \u0441\u0435\u0440\u0432\u0435\u0440!", Formatting.RED);
                    return;
                }
                if (repository.hasWay(name)) {
                    this.logDirect(String.format("\u0422\u043e\u0447\u043a\u0430 \u0441 \u0438\u043c\u0435\u043d\u0435\u043c %s \u0443\u0436\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442!", name), Formatting.RED);
                    return;
                }
                repository.addWayAndSave(name, pos, server);
                this.logDirect(String.format("\u00a7a\u0422\u043e\u0447\u043a\u0430 \u00a7f%s \u00a7a\u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d\u0430 \u043d\u0430 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u0430\u0445 \u00a7f%d %d %d", name, pos.getX(), pos.getY(), pos.getZ()), Formatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: way remove <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (!repository.hasWay(name)) {
                    this.logDirect(String.format("\u0422\u043e\u0447\u043a\u0430 %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430!", name), Formatting.RED);
                    return;
                }
                repository.deleteWayAndSave(name);
                this.logDirect(String.format("\u0422\u043e\u0447\u043a\u0430 %s \u0443\u0434\u0430\u043b\u0435\u043d\u0430!", name), Formatting.GREEN);
                break;
            }
            case "clear": {
                String server = repository.getCurrentServer();
                int count = 0;
                List<Way> toRemove = repository.getWayList().stream().filter(way -> way.server().equalsIgnoreCase(server)).toList();
                for (Way way2 : toRemove) {
                    repository.deleteWay(way2.name());
                    ++count;
                }
                if (count > 0) {
                    WayConfig.getInstance().save();
                }
                this.logDirect(String.format("\u0423\u0434\u0430\u043b\u0435\u043d\u043e \u0442\u043e\u0447\u0435\u043a \u0434\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430: %d", count), Formatting.GREEN);
                break;
            }
            case "clearall": {
                int count = repository.size();
                repository.clearListAndSave();
                this.logDirect(String.format("\u0412\u0441\u0435 \u0442\u043e\u0447\u043a\u0438 \u0443\u0434\u0430\u043b\u0435\u043d\u044b! \u0423\u0434\u0430\u043b\u0435\u043d\u043e: %d", count), Formatting.GREEN);
                break;
            }
            case "list": {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException count) {
                        // empty catch block
                    }
                }
                String server = repository.getCurrentServer();
                List<Way> serverWays = repository.getWayList().stream().filter(way -> way.server().equalsIgnoreCase(server)).toList();
                if (serverWays.isEmpty()) {
                    this.logDirect("\u041d\u0435\u0442 \u0442\u043e\u0447\u0435\u043a \u0434\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430!", Formatting.RED);
                    return;
                }
                Paginator<Way> paginator = new Paginator<Way>(serverWays);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7l\u0422\u041e\u0427\u041a\u0418 \u00a77(" + serverWays.size() + ")");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, way -> {
                    String wayName = way.name();
                    BlockPos pos = way.pos();
                    double distance = WayCommand.mc.player.getEntityPos().distanceTo(pos.toCenterPos());
                    MutableText component = Text.literal((String)("  \u00a7d\u25cf \u00a7f" + wayName)).append((Text)Text.literal((String)String.format(" \u00a78[\u00a77%d %d %d\u00a78]", pos.getX(), pos.getY(), pos.getZ()))).append((Text)Text.literal((String)String.format(" \u00a78(\u00a77%.1fm\u00a78)", distance)));
                    MutableText hoverText = Text.literal((String)"\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0447\u0442\u043e\u0431\u044b \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443");
                    String removeCommand = manager.getPrefix() + "way remove " + wayName;
                    component.setStyle(component.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0423\u041f\u0420\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0422\u041e\u0427\u041a\u0410\u041c\u0418");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> way add <name> [x y z] \u00a78- \u00a7f\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443");
                this.logDirect("\u00a77> way remove <name> \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443");
                this.logDirect("\u00a77> way list \u00a78- \u00a7f\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0442\u043e\u0447\u0435\u043a");
                this.logDirect("\u00a77> way clear \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0438 \u0434\u043b\u044f \u044d\u0442\u043e\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430");
                this.logDirect("\u00a77> way clearall \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0442\u043e\u0447\u043a\u0438");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        WayRepository repository = WayRepository.getInstance();
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear", "clearall").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2 && ((action = args[0].toLowerCase()).equals("remove") || action.equals("del") || action.equals("delete"))) {
            String server = repository.getCurrentServer();
            return new TabCompleteHelper().append(repository.getWayNamesForServer(server).toArray(new String[0])).filterPrefix(args[1]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0442\u043e\u0447\u043a\u0430\u043c\u0438 \u043d\u0430 \u043a\u0430\u0440\u0442\u0435";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f waypoints (\u0442\u043e\u0447\u043a\u0430\u043c\u0438 \u043d\u0430 \u043a\u0430\u0440\u0442\u0435)", "\u0422\u043e\u0447\u043a\u0438 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u044e\u0442\u0441\u044f \u043d\u0430 \u044d\u043a\u0440\u0430\u043d\u0435 \u0441 \u0440\u0430\u0441\u0441\u0442\u043e\u044f\u043d\u0438\u0435\u043c \u0434\u043e \u043d\u0438\u0445", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> way add <name> [x y z] - \u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443 (\u0431\u0435\u0437 \u043a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442 - \u0442\u0435\u043a\u0443\u0449\u0430\u044f \u043f\u043e\u0437\u0438\u0446\u0438\u044f)", "> way remove <name> - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0442\u043e\u0447\u043a\u0443", "> way list - \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0442\u043e\u0447\u0435\u043a \u0434\u043b\u044f \u0442\u0435\u043a\u0443\u0449\u0435\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430", "> way clear - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0442\u043e\u0447\u043a\u0438 \u0434\u043b\u044f \u0442\u0435\u043a\u0443\u0449\u0435\u0433\u043e \u0441\u0435\u0440\u0432\u0435\u0440\u0430", "> way clearall - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0432\u0441\u0435 \u0442\u043e\u0447\u043a\u0438");
    }
}

