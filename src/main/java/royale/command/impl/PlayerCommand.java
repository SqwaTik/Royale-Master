package royale.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.scoreboard.Team;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.command.impl.HelpCommand;
import royale.util.repository.staff.StaffUtils;

public class PlayerCommand
extends Command {
    public PlayerCommand() {
        super("players", "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0434\u043b\u044f HUD Players", "staff");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: players add <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (StaffUtils.isStaff(name)) {
                    this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0443\u0436\u0435 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 Players!", name), Formatting.RED);
                    return;
                }
                StaffUtils.addStaffAndSave(name);
                this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 Players.", name), Formatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: players remove <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (!StaffUtils.isStaff(name)) {
                    this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 Players.", name), Formatting.RED);
                    return;
                }
                StaffUtils.removeStaffAndSave(name);
                this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 Players.", name), Formatting.GREEN);
                break;
            }
            case "clear": {
                int count = StaffUtils.size();
                StaffUtils.clearAndSave();
                this.logDirect(String.format("\u0421\u043f\u0438\u0441\u043e\u043a Players \u043e\u0447\u0438\u0449\u0435\u043d. \u0423\u0434\u0430\u043b\u0435\u043d\u043e: %d", count), Formatting.GREEN);
                break;
            }
            case "list": {
                List<String> players;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if ((players = StaffUtils.getStaffNames()).isEmpty()) {
                    this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a Players \u043f\u0443\u0441\u0442.", Formatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(players);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7l\u0421\u041f\u0418\u0421\u041e\u041a PLAYERS \u00a77(" + players.size() + ")");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, playerName -> {
                    MutableText nameComponent = Text.literal((String)("  \u00a7c\u25cf \u00a7f" + playerName));
                    MutableText hoverText = Text.literal((String)("\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435, \u0447\u0442\u043e\u0431\u044b \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u00a7f" + playerName + " \u00a77\u0438\u0437 Players"));
                    String removeCommand = manager.getPrefix() + "players remove " + playerName;
                    nameComponent.setStyle(nameComponent.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(removeCommand)));
                    return nameComponent;
                }, manager.getPrefix() + "players list");
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0423\u041f\u0420\u0410\u0412\u041b\u0415\u041d\u0418\u0415 PLAYERS");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> players add <name> \u00a78- \u00a7f\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0441\u043f\u0438\u0441\u043e\u043a Players");
                this.logDirect("\u00a77> players remove <name> \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430 Players");
                this.logDirect("\u00a77> players list \u00a78- \u00a7f\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a Players");
                this.logDirect("\u00a77> players clear \u00a78- \u00a7f\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a Players");
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
            String action = args[0].toLowerCase(Locale.US);
            if (action.equals("add")) {
                return new TabCompleteHelper().append(this.getOnlinePlayers().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(StaffUtils.getStaffNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u0434\u043b\u044f HUD Players";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0438\u0433\u0440\u043e\u043a\u043e\u0432, \u043a\u043e\u0442\u043e\u0440\u044b\u0439 \u043e\u0442\u043e\u0431\u0440\u0430\u0436\u0430\u0435\u0442\u0441\u044f \u0432 HUD Players.", "\u0412 HUD \u043f\u043e\u043f\u0430\u0434\u0430\u044e\u0442 \u0442\u043e\u043b\u044c\u043a\u043e \u0438\u0433\u0440\u043e\u043a\u0438 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430, \u043a\u043e\u0442\u043e\u0440\u044b\u0435 \u043e\u043d\u043b\u0430\u0439\u043d, \u043d\u0435 \u0432 GM3 \u0438 \u043d\u0435 \u0432 vanish.", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> players add <name> - \u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0441\u043f\u0438\u0441\u043e\u043a", "> players remove <name> - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430", "> players list - \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a", "> players clear - \u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a");
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        String stripped = Formatting.strip((String)text);
        return stripped == null ? text : stripped;
    }

    private boolean isVanished(PlayerListEntry entry, String name) {
        String display = entry.getDisplayName() != null ? this.normalize(entry.getDisplayName().getString()) : name;
        String lowerDisplay = display.toLowerCase(Locale.ROOT);
        if (lowerDisplay.contains("vanish") || lowerDisplay.contains("vanished") || lowerDisplay.contains("\u043d\u0435\u0432\u0438\u0434")) {
            return true;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) {
            return false;
        }
        Team team = mc.world.getScoreboard().getScoreHolderTeam(name);
        if (team == null) {
            return false;
        }
        String teamText = (this.normalize(team.getPrefix().getString()) + " " + this.normalize(team.getSuffix().getString()) + " " + team.getName()).toLowerCase(Locale.ROOT);
        return teamText.contains("vanish") || teamText.contains("\u043d\u0435\u0432\u0438\u0434");
    }

    private List<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<String>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() == null) {
            return players;
        }
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name;
            if (entry.getProfile() == null || entry.getProfile().name() == null || StaffUtils.isStaff(name = entry.getProfile().name()) || entry.getGameMode() == GameMode.SPECTATOR || this.isVanished(entry, name)) continue;
            players.add(name);
        }
        return players;
    }
}

