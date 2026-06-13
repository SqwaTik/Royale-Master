package royale.command.helpers;

import java.util.List;
import java.util.function.Function;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import royale.command.CommandManager;
import royale.command.impl.HelpCommand;

public class Paginator<T> {
    private final List<T> items;
    private final int itemsPerPage;
    private int currentPage;

    public Paginator(List<T> items) {
        this(items, 8);
    }

    public Paginator(List<T> items, int itemsPerPage) {
        this.items = items;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
    }

    public int getTotalPages() {
        return Math.max(1, (int)Math.ceil((double)this.items.size() / (double)this.itemsPerPage));
    }

    public List<T> getCurrentPageItems() {
        int start = (this.currentPage - 1) * this.itemsPerPage;
        int end = Math.min(start + this.itemsPerPage, this.items.size());
        return this.items.subList(start, end);
    }

    public void setPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, this.getTotalPages()));
    }

    public void display(Runnable header, Function<T, MutableText> itemFormatter, String commandPrefix) {
        CommandManager manager = CommandManager.getInstance();
        if (header != null) {
            header.run();
        }
        for (T item : this.getCurrentPageItems()) {
            MutableText formatted = itemFormatter.apply(item);
            manager.sendRaw((Text)formatted);
        }
        if (this.getTotalPages() > 1) {
            this.displayNavigation(manager, commandPrefix);
        } else {
            manager.sendRaw((Text)Text.literal((String)HelpCommand.getLine()));
        }
    }

    private void displayNavigation(CommandManager manager, String commandPrefix) {
        manager.sendRaw((Text)Text.literal((String)HelpCommand.getLine()));
        MutableText navigation = Text.literal((String)"");
        if (this.currentPage > 1) {
            MutableText prevButton = Text.literal((String)"\u00a78[\u00a7b\u25c4 \u041d\u0430\u0437\u0430\u0434\u00a78]");
            String prevCommand = commandPrefix + " " + (this.currentPage - 1);
            prevButton.setStyle(prevButton.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)Text.literal((String)("\u00a77\u0421\u0442\u0440\u0430\u043d\u0438\u0446\u0430 " + (this.currentPage - 1))))).withClickEvent((ClickEvent)new ClickEvent.RunCommand(prevCommand)));
            navigation.append((Text)prevButton);
        } else {
            navigation.append((Text)Text.literal((String)"\u00a78[\u00a77\u25c4 \u041d\u0430\u0437\u0430\u0434\u00a78]"));
        }
        navigation.append((Text)Text.literal((String)(" \u00a77\u0421\u0442\u0440\u0430\u043d\u0438\u0446\u0430 \u00a7b" + this.currentPage + "\u00a77/\u00a7b" + this.getTotalPages() + " ")));
        if (this.currentPage < this.getTotalPages()) {
            MutableText nextButton = Text.literal((String)"\u00a78[\u00a7b\u0412\u043f\u0435\u0440\u0451\u0434 \u25ba\u00a78]");
            String nextCommand = commandPrefix + " " + (this.currentPage + 1);
            nextButton.setStyle(nextButton.getStyle().withHoverEvent((HoverEvent)new HoverEvent.ShowText((Text)Text.literal((String)("\u00a77\u0421\u0442\u0440\u0430\u043d\u0438\u0446\u0430 " + (this.currentPage + 1))))).withClickEvent((ClickEvent)new ClickEvent.RunCommand(nextCommand)));
            navigation.append((Text)nextButton);
        } else {
            navigation.append((Text)Text.literal((String)"\u00a78[\u00a77\u0412\u043f\u0435\u0440\u0451\u0434 \u25ba\u00a78]"));
        }
        manager.sendRaw((Text)navigation);
    }

    public static <T> void paginate(String[] args, Paginator<T> paginator, Runnable header, Function<T, MutableText> itemFormatter, String commandPrefix) {
        if (args.length > 0) {
            try {
                int page = Integer.parseInt(args[0]);
                paginator.setPage(page);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        paginator.display(header, itemFormatter, commandPrefix);
    }
}

