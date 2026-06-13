package royale.util.string.chat;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import royale.modules.impl.misc.Client;
import royale.util.string.chat.helper.TextHelper;
public class ChatMessage {
private static MutableText accentPrefix(String text) {
return Text.literal(text)
.setStyle(Style.EMPTY.withColor(Client.getResolvedClientColor()).withBold(Boolean.valueOf(true)));
}
private static Text prefixedArrow(String text) {
return (Text)accentPrefix(text).copy().append((Text)Text.literal(" -> ").formatted(Formatting.DARK_GRAY));
}
public static MutableText brandmessage() {
return Text.literal("Royale Client")
.setStyle(Style.EMPTY.withColor(Client.getResolvedClientColor()).withBold(Boolean.valueOf(true)));
}
public static void brandmessage(String message) {
if ((MinecraftClient.getInstance()).player != null) {
MutableText class_52501 = brandmessage().copy().append((Text)Text.literal(" -> ").formatted(Formatting.DARK_GRAY));
MutableText class_52502 = class_52501.copy().append((Text)Text.literal(message));
(MinecraftClient.getInstance()).player.sendMessage((Text)class_52502, false);
} 
}
public static void ancientmessage(String message) {
if ((MinecraftClient.getInstance()).player != null) {
Text prefix = prefixedArrow("Ancient Xray");
MutableText MutableText = prefix.copy().append((Text)Text.literal(message));
(MinecraftClient.getInstance()).player.sendMessage((Text)MutableText, false);
} 
}
public static void helpmessage(String message) {
if ((MinecraftClient.getInstance()).player != null) {
MutableText class_52501 = brandmessage().copy().append((Text)Text.literal(" -> ").formatted(Formatting.DARK_GRAY));
MutableText class_52502 = class_52501.copy().append((Text)Text.literal(message));
(MinecraftClient.getInstance()).player.sendMessage((Text)class_52502, false);
} 
}
public static void swapmessage(String message) {
if ((MinecraftClient.getInstance()).player != null) {
Text prefix = prefixedArrow("AutoSwap");
MutableText MutableText = prefix.copy().append((Text)Text.literal(message));
(MinecraftClient.getInstance()).player.sendMessage((Text)MutableText, false);
} 
}
public static void ircmessage(String message) {
if ((MinecraftClient.getInstance()).player != null) {
MutableText class_52501 = accentPrefix("[IRC]").copy().append((Text)Text.literal(" ").formatted(Formatting.DARK_GRAY));
MutableText class_52502 = class_52501.copy().append((Text)Text.literal(message));
(MinecraftClient.getInstance()).player.sendMessage((Text)class_52502, false);
} 
}
public static void ircmessageWithGreen(String message) {
if ((MinecraftClient.getInstance()).player != null) {
MutableText class_52501 = accentPrefix("[IRC]").copy().append((Text)Text.literal(" ").formatted(Formatting.DARK_GRAY));
MutableText class_52502 = class_52501.copy().append((Text)Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.GREEN)));
(MinecraftClient.getInstance()).player.sendMessage((Text)class_52502, false);
} 
}
public static void ircmessageWithRed(String message) {
if ((MinecraftClient.getInstance()).player != null) {
MutableText class_52501 = accentPrefix("[IRC]").copy().append((Text)Text.literal(" ").formatted(Formatting.DARK_GRAY));
MutableText class_52502 = class_52501.copy().append((Text)Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED)));
(MinecraftClient.getInstance()).player.sendMessage((Text)class_52502, false);
} 
}
public static Text ircprefixDeveloper(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Developer ", "dark_red_bright_red", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixCurator(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Куратор ", "dark_red", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixYouTube(String message) {
Text prefix = TextHelper.applyPredefinedGradient("YouTube ", "red_white", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixPikmi(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Пикми ", "purple_bright_pink", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixLabuba(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Лабуба ", "pink_dark_pink", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixZapen(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Запен ", "bright_red", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixBoost(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Буст ", "dark_green_bright_green", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixRich(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Рич ", "red_orange", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixPanda(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Панда ", "white_black", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixSmiley(String message) {
Text prefix = TextHelper.applyPredefinedGradient("(●'◡'●) ", "turquoise_blue", true);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixBibi(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Биби...! ", "cyan_orange_fade", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixBenena(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Бэнена ", "yellow_cyan", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
public static Text ircprefixBlyabuba(String message) {
Text prefix = TextHelper.applyPredefinedGradient("Блябуба ", "purple_red_fade", false);
return (Text)prefix.copy().append((Text)Text.literal(message));
}
}


