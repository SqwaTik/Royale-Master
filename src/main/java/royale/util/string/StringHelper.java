package royale.util.string;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
public final class StringHelper
{
private StringHelper() {
throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
} public static String randomString(int length) {
return IntStream.range(0, length)
.<CharSequence>mapToObj(operand -> String.valueOf((char)(new Random()).nextInt(97, 123)))
.collect(Collectors.joining());
}
public static String getBindName(int key) {
if (key < 0) return "N/A"; 
return PlayerInteractionHelper.getKeyType(key).createFromCode(key).getTranslationKey().replace("key.keyboard.", "")
.replace("key.mouse.", "mouse ").replace(".", " ").toUpperCase();
}
public static String getUserRole() {
switch ("DEVELOPER") { case "Разработчик": case "Администратор":  }  return 
"User";
}
public static String getDuration(int time) {
int mins = time / 60;
String sec = String.format("%02d", new Object[] { Integer.valueOf(time % 60) });
return "" + mins + ":" + mins;
}
}


