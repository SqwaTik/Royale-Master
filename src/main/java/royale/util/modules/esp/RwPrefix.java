package royale.util.modules.esp;
public class RwPrefix
{
public static String getIconLabel(char c) {
switch (c) { case 'ꔀ': case 'ꔄ': case 'ꔈ': case 'ꔒ': case 'ꔖ': case 'ꔠ': case 'ꔤ': case 'ꔨ': case 'ꔲ': case 'ꕒ': case 'ꔶ': case 'ꕄ': case 'ꕖ': case 'ꕀ': case 'ꕈ': case 'ꔁ': case 'ꔅ': case 'ꕠ': case 'ꔉ': case 'ꔓ': case 'ꔗ': case 'ꔡ': case 'ꔥ': case 'ꔩ': case 'ꔳ': case 'ꔷ': case 'ꕅ': case 'ꕉ':  }  return 
null;
}
public static boolean isIcon(char c) {
if (getIconLabel(c) != null)
return true; 
return ((c >= 'ꀀ' && c <= '꿿') || (c >= '' && c <= '') || (c >= '␀' && c <= '␿') || (c >= '─' && c <= '╿'));
}
public static String stripFormatting(String text) {
if (text == null) return ""; 
StringBuilder result = new StringBuilder();
for (int i = 0; i < text.length(); i++) {
char c = text.charAt(i);
if (c == '§' && i + 1 < text.length()) {
char next = text.charAt(i + 1);
if (next == '#' && i + 7 < text.length()) {
i += 7;
}
else if ((next == 'x' || next == 'X') && i + 13 < text.length()) {
i += 13;
} else {
i++;
} 
} else {
result.append(c);
} 
}  return result.toString();
}
public static ParsedName parseDisplayName(String displayName) {
if (displayName == null || displayName.isEmpty()) {
return new ParsedName("", "", "");
}
String clean = stripFormatting(displayName);
StringBuilder prefix = new StringBuilder();
StringBuilder name = new StringBuilder();
StringBuilder clan = new StringBuilder();
boolean foundName = false;
boolean inClan = false;
int clanBracketCount = 0;
for (int i = 0; i < clean.length(); i++) {
char c = clean.charAt(i);
if (isIcon(c)) {
String label = getIconLabel(c);
if (label != null) {
if (prefix.length() > 0) prefix.append(" "); 
prefix.append(label);
}
}
else if (foundName || (c != ' ' && c != '[' && c != ']')) {
if ((!foundName && Character.isLetterOrDigit(c)) || c == '_') {
foundName = true;
}
if (foundName) {
if (c == '[') {
inClan = true;
clanBracketCount++;
clan.append(c);
}
else if (c == ']' && inClan) {
clan.append(c);
clanBracketCount--;
if (clanBracketCount <= 0) {
inClan = false;
}
}
else if (inClan) {
clan.append(c);
} else if ((c != ' ' || name.length() > 0) && (
c != ' ' || i + 1 >= clean.length() || clean.charAt(i + 1) != '[')) {
if (!inClan && clan.length() == 0) {
name.append(c);
}
} 
}
} 
} 
String nameStr = name.toString().trim();
if (nameStr.contains(" ")) {
int spaceIdx = nameStr.indexOf(' ');
String possibleClan = nameStr.substring(spaceIdx).trim();
if (possibleClan.startsWith("[") && possibleClan.endsWith("]")) {
clan = new StringBuilder(possibleClan);
nameStr = nameStr.substring(0, spaceIdx);
} 
} 
return new ParsedName(prefix.toString().trim(), nameStr.trim(), clan.toString().trim());
}
public static class ParsedName {
public final String prefix;
public final String name;
public final String clan;
public ParsedName(String prefix, String name, String clan) {
this.prefix = prefix;
this.name = name;
this.clan = clan;
}
public String getFullText() {
StringBuilder sb = new StringBuilder();
if (!this.prefix.isEmpty()) {
sb.append(this.prefix).append(" ");
}
sb.append(this.name);
if (!this.clan.isEmpty()) {
sb.append(" ").append(this.clan);
}
return sb.toString();
}
}
}


