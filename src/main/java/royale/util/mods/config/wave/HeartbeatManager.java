package royale.util.mods.config.wave;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class HeartbeatManager
{
private static ScheduledExecutorService scheduler;
private static String systemHwid;
private static String profileHwid;
private static String currentUsername;
private static String currentUid;
private static String g1() {
char[] k = { 'h', 't', 't', 'p', ':', '/', '/', '8', '7', '.', '1', '2', '0', '.', '1', '8', '6', '.', '1', '8', '6', ':', '3', '0', '0', '0' };
StringBuilder sb = new StringBuilder();
for (char c : k) sb.append(c); 
return sb.toString();
}
private static String g2() {
char[] k = { 'V', 'M', '$', 'U', 'v', 'w', '9', 'u', '6', 'W', 'C', 'U', '6', '5', '9', '0', 'w', 'q', '6', 'u', 'j', 't', 'e', 'g', 's', 'a' };
StringBuilder sb = new StringBuilder();
for (char c : k) sb.append(c); 
return sb.toString();
}
private static String g3() {
char[] k = { '/', 'a', 'p', 'i', '/', 'r', 'e', 'g', 'i', 's', 't', 'e', 'r' };
StringBuilder sb = new StringBuilder();
for (char c : k) sb.append(c); 
return sb.toString();
}
private static String g4() {
char[] k = { '/', 'a', 'p', 'i', '/', 'h', 'e', 'a', 'r', 't', 'b', 'e', 'a', 't' };
StringBuilder sb = new StringBuilder();
for (char c : k) sb.append(c); 
return sb.toString();
}
private static String g5() {
char[] k = { '/', 'a', 'p', 'i', '/', 'o', 'f', 'f', 'l', 'i', 'n', 'e' };
StringBuilder sb = new StringBuilder();
for (char c : k) sb.append(c); 
return sb.toString();
}
public static void start(String sysHwid, String profHwid, String username, String uid) {
systemHwid = sysHwid;
profileHwid = profHwid;
currentUsername = username;
currentUid = uid;
(new Thread(() -> {
register();
scheduler = Executors.newSingleThreadScheduledExecutor();
scheduler.scheduleAtFixedRate(HeartbeatManager::heartbeat, 0L, 10L, TimeUnit.SECONDS);
})).start();
Runtime.getRuntime().addShutdownHook(new Thread(HeartbeatManager::offline));
}
private static void register() {
try {
String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\",\"username\":\"%s\",\"uid\":\"%s\"}", new Object[] {
g2(), 
escape(systemHwid), 
escape((profileHwid != null) ? profileHwid : ""), 
escape(currentUsername), 
escape(currentUid)
});
sendPost(g1() + g1(), json);
} catch (Exception exception) {}
}
private static void heartbeat() {
try {
String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", new Object[] {
g2(), 
escape(systemHwid), 
escape((profileHwid != null) ? profileHwid : "")
});
String response = sendPost(g1() + g1(), json);
if (response != null && (
response.contains("\"kill\":true") || response.contains("\"banned\":true"))) {
shutdown();
}
}
catch (Exception exception) {}
}
private static void offline() {
try {
String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", new Object[] {
g2(), 
escape(systemHwid), 
escape((profileHwid != null) ? profileHwid : "")
});
sendPost(g1() + g1(), json);
} catch (Exception exception) {}
}
private static String sendPost(String urlStr, String json) {
try {
URL url = new URL(urlStr);
HttpURLConnection conn = (HttpURLConnection)url.openConnection();
conn.setRequestMethod(d("UE9TVA=="));
conn.setRequestProperty(d("Q29udGVudC1UeXBl"), d("YXBwbGljYXRpb24vanNvbg=="));
conn.setRequestProperty(d("VXNlci1BZ2VudA=="), d("UmljaENsaWVudC8yLjA="));
conn.setDoOutput(true);
conn.setConnectTimeout(5000);
conn.setReadTimeout(5000);
OutputStream os = conn.getOutputStream(); 
try { os.write(json.getBytes(StandardCharsets.UTF_8));
if (os != null) os.close();  } catch (Throwable throwable) { if (os != null)
try { os.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }
int code = conn.getResponseCode();
if (code == 200) {
BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)); 
try { StringBuilder response = new StringBuilder();
String line;
while ((line = br.readLine()) != null) {
response.append(line);
}
String str1 = response.toString();
br.close(); return str1; } catch (Throwable throwable) { try { br.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; } 
} 
} catch (Exception exception) {}
return null;
}
private static String escape(String s) {
if (s == null) return ""; 
return s.replace("\\", "\\\\").replace("\"", "\\\"");
}
private static void shutdown() {
try {
Runtime.getRuntime().halt(0);
} catch (Throwable t) {
System.exit(0);
} 
}
private static String d(String b) {
try {
return new String(Base64.getDecoder().decode(b), StandardCharsets.UTF_8);
} catch (Exception e) {
return "";
} 
}
}


