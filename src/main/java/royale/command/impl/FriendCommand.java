package royale.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.client.network.PlayerListEntry;
import royale.command.Command;
import royale.command.CommandManager;
import royale.command.helpers.Paginator;
import royale.command.helpers.TabCompleteHelper;
import royale.command.impl.HelpCommand;
import royale.util.repository.friend.FriendUtils;

public class FriendCommand
extends Command {
    public FriendCommand() {
        super("friend", "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0434\u0440\u0443\u0437\u0435\u0439", "f", "friends");
    }

    @Override
    public void execute(String label, String[] args) {
        String arg;
        CommandManager manager = CommandManager.getInstance();
        switch (arg = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: friend add <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (FriendUtils.isFriend(name)) {
                    this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0443\u0436\u0435 \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0434\u0440\u0443\u0437\u0435\u0439!", name), Formatting.RED);
                    return;
                }
                FriendUtils.addFriendAndSave(name);
                this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0434\u0440\u0443\u0437\u044c\u044f!", name), Formatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435: friend remove <name>", Formatting.RED);
                    return;
                }
                String name = args[1];
                if (!FriendUtils.isFriend(name)) {
                    this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043a\u0435 \u0434\u0440\u0443\u0437\u0435\u0439!", name), Formatting.RED);
                    return;
                }
                FriendUtils.removeFriendAndSave(name);
                this.logDirect(String.format("\u0418\u0433\u0440\u043e\u043a %s \u0443\u0434\u0430\u043b\u0435\u043d \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439!", name), Formatting.GREEN);
                break;
            }
            case "clear": {
                int count = FriendUtils.size();
                FriendUtils.clearAndSave();
                this.logDirect(String.format("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439 \u043e\u0447\u0438\u0449\u0435\u043d! \u0423\u0434\u0430\u043b\u0435\u043d\u043e: %d", count), Formatting.GREEN);
                break;
            }
            case "list": {
                List<String> friends;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if ((friends = FriendUtils.getFriendNames()).isEmpty()) {
                    this.logDirect("\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439 \u043f\u0443\u0441\u0442!", Formatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(friends);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7l\u0421\u041f\u0418\u0421\u041e\u041a \u0414\u0420\u0423\u0417\u0415\u0419 \u00a77(" + friends.size() + ")");
                    this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                }, friend -> {
                    MutableText nameComponent = Text.literal((String)("  \u00a7a\u25cf \u00a7f" + friend));
                    MutableText hoverText = Text.literal((String)("\u00a77\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0447\u0442\u043e\u0431\u044b \u0443\u0434\u0430\u043b\u0438\u0442\u044c \u00a7f" + friend + " \u00a77\u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439"));
                    String removeCommand = manager.getPrefix() + "friend remove " + friend;
                    nameComponent.setStyle(nameComponent.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)hoverText)).withClickEvent((ClickEvent)new ClickEvent.RunCommand(removeCommand)));
                    return nameComponent;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7l\u0423\u041f\u0420\u0410\u0412\u041b\u0415\u041d\u0418\u0415 \u0414\u0420\u0423\u0417\u042c\u042f\u041c\u0418");
                this.logDirectRaw(Text.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> friend add <name> \u00a78- \u00a7f\u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0434\u0440\u0443\u0437\u044c\u044f");
                this.logDirect("\u00a77> friend remove <name> \u00a78- \u00a7f\u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439");
                this.logDirect("\u00a77> friend list \u00a78- \u00a7f\u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439");
                this.logDirect("\u00a77> friend clear \u00a78- \u00a7f\u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439");
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
                return new TabCompleteHelper().append(this.getOnlinePlayers().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(FriendUtils.getFriendNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "\u0423\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0434\u0440\u0443\u0437\u0435\u0439";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 \u0434\u043b\u044f \u0443\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u0438\u044f \u0441\u043f\u0438\u0441\u043a\u043e\u043c \u0434\u0440\u0443\u0437\u0435\u0439", "\u0418\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u043d\u0438\u0435:", "> friend add <name> - \u0414\u043e\u0431\u0430\u0432\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0432 \u0434\u0440\u0443\u0437\u044c\u044f", "> friend remove <name> - \u0423\u0434\u0430\u043b\u0438\u0442\u044c \u0438\u0433\u0440\u043e\u043a\u0430 \u0438\u0437 \u0434\u0440\u0443\u0437\u0435\u0439", "> friend list - \u041f\u043e\u043a\u0430\u0437\u0430\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439", "> friend clear - \u041e\u0447\u0438\u0441\u0442\u0438\u0442\u044c \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439");
    }

    private List<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<String>();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getNetworkHandler() != null) {
            for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
                String name = entry.getProfile().name();
                if (FriendUtils.isFriend(name)) continue;
                players.add(name);
            }
        }
        return players;
    }
}

