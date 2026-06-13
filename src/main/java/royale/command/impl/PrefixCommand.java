package royale.command.impl;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import royale.command.Command;
import royale.command.CommandManager;
import royale.util.config.impl.prefix.PrefixConfig;
public class PrefixCommand
extends Command
{
public PrefixCommand() {
super("prefix", "Изменение префикса команд", new String[0]);
}
public void execute(String label, String[] args) {
CommandManager manager = CommandManager.getInstance();
if (args.length == 0) {
logDirectRaw(Text.literal(HelpCommand.getLine()));
logDirect("§f§lПРЕФИКС КОМАНД");
logDirectRaw(Text.literal(HelpCommand.getLine()));
logDirect("§7Текущий префикс: §f" + manager.getPrefix());
logDirect("§7> prefix set <symbol> §8- §fУстановить новый префикс");
logDirectRaw(Text.literal(HelpCommand.getLine()));
return;
} 
String action = args[0].toLowerCase();
if (action.equals("set")) {
if (args.length < 2) {
logDirect("Использование: prefix set <symbol>", Formatting.RED);
return;
} 
String newPrefix = args[1];
if (newPrefix.length() > 3) {
logDirect("Префикс не может быть длиннее 3 символов!", Formatting.RED);
return;
} 
if (newPrefix.contains(" ")) {
logDirect("Префикс не может содержать пробелы!", Formatting.RED);
return;
} 
PrefixConfig.getInstance().setPrefixAndSave(newPrefix);
logDirect(String.format("§aПрефикс изменен на: §f%s", new Object[] { newPrefix }), Formatting.GREEN);
logDirect(String.format("§7Теперь команды вводятся как: §f%shelp", new Object[] { newPrefix }), Formatting.GREEN);
} else {
logDirect("Использование: prefix set <symbol>", Formatting.RED);
} 
}
public Stream<String> tabComplete(String label, String[] args) {
if (args.length == 1) {
return Stream.<String>of("set").filter(s -> s.startsWith(args[0].toLowerCase()));
}
if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
return Stream.<String>of(new String[] { ".", "!", "$", "#", "-", "/" }).filter(s -> s.startsWith(args[1]));
}
return Stream.empty();
}
public String getShortDesc() {
return "Изменение префикса команд";
}
public List<String> getLongDesc() {
return Arrays.asList(new String[] { "Команда для изменения префикса команд в чите", "Использование:", "> prefix - Показать текущий префикс", "> prefix set <symbol> - Установить новый префикс" });
}
}


