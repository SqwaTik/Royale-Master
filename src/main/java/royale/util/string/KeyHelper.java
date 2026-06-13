package royale.util.string;
import java.util.HashMap;
import java.util.Map;
public class KeyHelper
{
private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
private static final Map<String, Integer> NAME_TO_KEY = new HashMap<>();
static {
KEY_NAMES.put(Integer.valueOf(32), "Space");
KEY_NAMES.put(Integer.valueOf(39), "'");
KEY_NAMES.put(Integer.valueOf(44), ",");
KEY_NAMES.put(Integer.valueOf(45), "-");
KEY_NAMES.put(Integer.valueOf(46), ".");
KEY_NAMES.put(Integer.valueOf(47), "/");
KEY_NAMES.put(Integer.valueOf(48), "0");
KEY_NAMES.put(Integer.valueOf(49), "1");
KEY_NAMES.put(Integer.valueOf(50), "2");
KEY_NAMES.put(Integer.valueOf(51), "3");
KEY_NAMES.put(Integer.valueOf(52), "4");
KEY_NAMES.put(Integer.valueOf(53), "5");
KEY_NAMES.put(Integer.valueOf(54), "6");
KEY_NAMES.put(Integer.valueOf(55), "7");
KEY_NAMES.put(Integer.valueOf(56), "8");
KEY_NAMES.put(Integer.valueOf(57), "9");
KEY_NAMES.put(Integer.valueOf(59), ";");
KEY_NAMES.put(Integer.valueOf(61), "=");
KEY_NAMES.put(Integer.valueOf(65), "A");
KEY_NAMES.put(Integer.valueOf(66), "B");
KEY_NAMES.put(Integer.valueOf(67), "C");
KEY_NAMES.put(Integer.valueOf(68), "D");
KEY_NAMES.put(Integer.valueOf(69), "E");
KEY_NAMES.put(Integer.valueOf(70), "F");
KEY_NAMES.put(Integer.valueOf(71), "G");
KEY_NAMES.put(Integer.valueOf(72), "H");
KEY_NAMES.put(Integer.valueOf(73), "I");
KEY_NAMES.put(Integer.valueOf(74), "J");
KEY_NAMES.put(Integer.valueOf(75), "K");
KEY_NAMES.put(Integer.valueOf(76), "L");
KEY_NAMES.put(Integer.valueOf(77), "M");
KEY_NAMES.put(Integer.valueOf(78), "N");
KEY_NAMES.put(Integer.valueOf(79), "O");
KEY_NAMES.put(Integer.valueOf(80), "P");
KEY_NAMES.put(Integer.valueOf(81), "Q");
KEY_NAMES.put(Integer.valueOf(82), "R");
KEY_NAMES.put(Integer.valueOf(83), "S");
KEY_NAMES.put(Integer.valueOf(84), "T");
KEY_NAMES.put(Integer.valueOf(85), "U");
KEY_NAMES.put(Integer.valueOf(86), "V");
KEY_NAMES.put(Integer.valueOf(87), "W");
KEY_NAMES.put(Integer.valueOf(88), "X");
KEY_NAMES.put(Integer.valueOf(89), "Y");
KEY_NAMES.put(Integer.valueOf(90), "Z");
KEY_NAMES.put(Integer.valueOf(91), "[");
KEY_NAMES.put(Integer.valueOf(92), "\\");
KEY_NAMES.put(Integer.valueOf(93), "]");
KEY_NAMES.put(Integer.valueOf(96), "`");
KEY_NAMES.put(Integer.valueOf(256), "Escape");
KEY_NAMES.put(Integer.valueOf(257), "Enter");
KEY_NAMES.put(Integer.valueOf(258), "Tab");
KEY_NAMES.put(Integer.valueOf(259), "Backspace");
KEY_NAMES.put(Integer.valueOf(260), "Insert");
KEY_NAMES.put(Integer.valueOf(261), "Delete");
KEY_NAMES.put(Integer.valueOf(262), "Right");
KEY_NAMES.put(Integer.valueOf(263), "Left");
KEY_NAMES.put(Integer.valueOf(264), "Down");
KEY_NAMES.put(Integer.valueOf(265), "Up");
KEY_NAMES.put(Integer.valueOf(266), "PageUp");
KEY_NAMES.put(Integer.valueOf(267), "PageDown");
KEY_NAMES.put(Integer.valueOf(268), "Home");
KEY_NAMES.put(Integer.valueOf(269), "End");
KEY_NAMES.put(Integer.valueOf(280), "CapsLock");
KEY_NAMES.put(Integer.valueOf(281), "ScrollLock");
KEY_NAMES.put(Integer.valueOf(282), "NumLock");
KEY_NAMES.put(Integer.valueOf(283), "PrintScreen");
KEY_NAMES.put(Integer.valueOf(284), "Pause");
KEY_NAMES.put(Integer.valueOf(290), "F1");
KEY_NAMES.put(Integer.valueOf(291), "F2");
KEY_NAMES.put(Integer.valueOf(292), "F3");
KEY_NAMES.put(Integer.valueOf(293), "F4");
KEY_NAMES.put(Integer.valueOf(294), "F5");
KEY_NAMES.put(Integer.valueOf(295), "F6");
KEY_NAMES.put(Integer.valueOf(296), "F7");
KEY_NAMES.put(Integer.valueOf(297), "F8");
KEY_NAMES.put(Integer.valueOf(298), "F9");
KEY_NAMES.put(Integer.valueOf(299), "F10");
KEY_NAMES.put(Integer.valueOf(300), "F11");
KEY_NAMES.put(Integer.valueOf(301), "F12");
KEY_NAMES.put(Integer.valueOf(320), "Numpad0");
KEY_NAMES.put(Integer.valueOf(321), "Numpad1");
KEY_NAMES.put(Integer.valueOf(322), "Numpad2");
KEY_NAMES.put(Integer.valueOf(323), "Numpad3");
KEY_NAMES.put(Integer.valueOf(324), "Numpad4");
KEY_NAMES.put(Integer.valueOf(325), "Numpad5");
KEY_NAMES.put(Integer.valueOf(326), "Numpad6");
KEY_NAMES.put(Integer.valueOf(327), "Numpad7");
KEY_NAMES.put(Integer.valueOf(328), "Numpad8");
KEY_NAMES.put(Integer.valueOf(329), "Numpad9");
KEY_NAMES.put(Integer.valueOf(330), "NumpadDecimal");
KEY_NAMES.put(Integer.valueOf(331), "NumpadDivide");
KEY_NAMES.put(Integer.valueOf(332), "NumpadMultiply");
KEY_NAMES.put(Integer.valueOf(333), "NumpadSubtract");
KEY_NAMES.put(Integer.valueOf(334), "NumpadAdd");
KEY_NAMES.put(Integer.valueOf(335), "NumpadEnter");
KEY_NAMES.put(Integer.valueOf(340), "LShift");
KEY_NAMES.put(Integer.valueOf(341), "LCtrl");
KEY_NAMES.put(Integer.valueOf(342), "LAlt");
KEY_NAMES.put(Integer.valueOf(344), "RShift");
KEY_NAMES.put(Integer.valueOf(345), "RCtrl");
KEY_NAMES.put(Integer.valueOf(346), "RAlt");
KEY_NAMES.put(Integer.valueOf(348), "Menu");
for (Map.Entry<Integer, String> entry : KEY_NAMES.entrySet()) {
NAME_TO_KEY.put(((String)entry.getValue()).toLowerCase(), entry.getKey());
}
}
public static String getKeyName(int keyCode) {
return KEY_NAMES.getOrDefault(Integer.valueOf(keyCode), "Unknown(" + keyCode + ")");
}
public static int getKeyCode(String name) {
return ((Integer)NAME_TO_KEY.getOrDefault(name.toLowerCase(), Integer.valueOf(-1))).intValue();
}
public static boolean isValidKey(String name) {
return NAME_TO_KEY.containsKey(name.toLowerCase());
}
public static String[] getAllKeyNames() {
return (String[])KEY_NAMES.values().toArray((Object[])new String[0]);
}
}


