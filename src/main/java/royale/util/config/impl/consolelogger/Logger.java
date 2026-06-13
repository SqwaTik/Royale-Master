package royale.util.config.impl.consolelogger;
public class Logger
{
private static final String RESET = "\033[0m";
private static final String GREEN_BG = "\033[42m";
private static final String RED_BG = "\033[41m";
private static final String BLACK = "\033[30m";
private static final String WHITE = "\033[97m";
private static final String BOLD = "\033[1m";
public static void success(String message) {
System.out.println("\033[42m\033[30m\033[1m " + message + " \033[0m");
}
public static void error(String message) {
System.out.println("\033[41m\033[97m\033[1m " + message + " \033[0m");
}
public static void info(String message) {
System.out.println("\033[44m\033[97m\033[1m " + message + " \033[0m");
}
}


