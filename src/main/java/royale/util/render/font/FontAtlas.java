package royale.util.render.font;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class FontAtlas
{
private static final Logger LOGGER = LoggerFactory.getLogger("royale/Font");
private static final long RETRY_DELAY_MS = 500L;
private final Identifier jsonId;
private final Identifier textureId;
private final Map<Integer, Glyph> glyphs;
private float atlasWidth = 512.0F;
private float atlasHeight = 512.0F;
private float fontSize = 32.0F;
private float lineHeight = 40.0F;
private float distanceRange = 4.0F;
private boolean yOriginBottom = false;
private final AtomicBoolean loaded = new AtomicBoolean(false);
private long lastFailedLoadAttempt = -1L;
private boolean missingResourceWarningShown = false;
public FontAtlas(Identifier jsonId, Identifier textureId) {
this.jsonId = jsonId;
this.textureId = textureId;
this.glyphs = new HashMap<>();
}
public void forceLoad() {
if (this.loaded.get())
return;  synchronized (this) {
if (this.loaded.get())
return;  if (!canAttemptLoad())
return;  doLoad();
} 
}
public void ensureLoaded() {
if (this.loaded.get())
return;  synchronized (this) {
if (this.loaded.get())
return;  if (!canAttemptLoad())
return;  doLoad();
} 
}
private void doLoad() {
try { Optional<Resource> resourceOpt = MinecraftClient.getInstance().getResourceManager().getResource(this.jsonId);
if (resourceOpt.isEmpty()) {
if (!this.missingResourceWarningShown) {
LOGGER.warn("Font JSON not found yet: {}. Will retry.", this.jsonId);
this.missingResourceWarningShown = true;
} 
markFailedLoad();
return;
} 
InputStream is = ((Resource)resourceOpt.get()).getInputStream(); 
try { InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
try { JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
parseJson(root);
this.loaded.set(true);
this.lastFailedLoadAttempt = -1L;
this.missingResourceWarningShown = false;
LOGGER.info("Loaded font: {} with {} glyphs", this.jsonId, Integer.valueOf(this.glyphs.size()));
reader.close(); } catch (Throwable throwable) { try { reader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  if (is != null) is.close();  } catch (Throwable throwable) { if (is != null)
try { is.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (Exception e)
{ LOGGER.error("Failed to load font: {}", this.jsonId, e);
markFailedLoad(); }
}
private boolean canAttemptLoad() {
if (this.lastFailedLoadAttempt < 0L) {
return true;
}
return (System.currentTimeMillis() - this.lastFailedLoadAttempt >= 500L);
}
private void markFailedLoad() {
this.lastFailedLoadAttempt = System.currentTimeMillis();
}
private void parseJson(JsonObject root) {
float emSize = 1.0F;
if (root.has("atlas")) {
JsonObject atlas = root.getAsJsonObject("atlas");
this.atlasWidth = getFloat(atlas, "width", 512.0F);
this.atlasHeight = getFloat(atlas, "height", 512.0F);
this.fontSize = getFloat(atlas, "size", 32.0F);
this.distanceRange = getFloat(atlas, "distanceRange", 4.0F);
if (atlas.has("yOrigin")) {
String origin = atlas.get("yOrigin").getAsString();
this.yOriginBottom = origin.equalsIgnoreCase("bottom");
} 
LOGGER.info("Atlas: {}x{}, size={}, distanceRange={}, yOrigin={}", new Object[] {
Float.valueOf(this.atlasWidth), Float.valueOf(this.atlasHeight), Float.valueOf(this.fontSize), Float.valueOf(this.distanceRange), this.yOriginBottom ? "bottom" : "top"
});
} 
if (root.has("metrics")) {
JsonObject metrics = root.getAsJsonObject("metrics");
emSize = getFloat(metrics, "emSize", 1.0F);
float normalizedLineHeight = getFloat(metrics, "lineHeight", 1.2F);
this.lineHeight = normalizedLineHeight * this.fontSize;
} 
if (root.has("glyphs")) {
JsonArray glyphsArray = root.getAsJsonArray("glyphs");
for (JsonElement elem : glyphsArray) {
JsonObject g = elem.getAsJsonObject();
parseMsdfGlyph(g, emSize);
} 
} 
}
private void parseMsdfGlyph(JsonObject g, float emSize) {
int unicode = -1;
if (g.has("unicode")) {
unicode = g.get("unicode").getAsInt();
} else if (g.has("char")) {
String charStr = g.get("char").getAsString();
if (!charStr.isEmpty()) {
unicode = charStr.codePointAt(0);
}
} else if (g.has("id")) {
unicode = g.get("id").getAsInt();
} 
if (unicode < 0)
return; 
float advance = getFloat(g, "advance", 0.0F) * this.fontSize;
if (advance == 0.0F) {
advance = getFloat(g, "xadvance", 0.0F);
}
float x = 0.0F, y = 0.0F, w = 0.0F, h = 0.0F;
float xOffset = 0.0F, yOffset = 0.0F;
if (g.has("atlasBounds")) {
JsonObject bounds = g.getAsJsonObject("atlasBounds");
float left = getFloat(bounds, "left", 0.0F);
float bottom = getFloat(bounds, "bottom", 0.0F);
float right = getFloat(bounds, "right", 0.0F);
float top = getFloat(bounds, "top", 0.0F);
x = left;
w = right - left;
h = top - bottom;
if (this.yOriginBottom) {
y = this.atlasHeight - top;
} else {
y = bottom;
} 
} else if (g.has("x") && g.has("y") && g.has("width") && g.has("height")) {
x = getFloat(g, "x", 0.0F);
y = getFloat(g, "y", 0.0F);
w = getFloat(g, "width", 0.0F);
h = getFloat(g, "height", 0.0F);
} 
if (g.has("planeBounds")) {
JsonObject plane = g.getAsJsonObject("planeBounds");
float pLeft = getFloat(plane, "left", 0.0F);
float pBottom = getFloat(plane, "bottom", 0.0F);
float pRight = getFloat(plane, "right", 0.0F);
float pTop = getFloat(plane, "top", 0.0F);
xOffset = pLeft * this.fontSize;
float ascender = 0.95F;
yOffset = (ascender - pTop) * this.fontSize;
} else if (g.has("xoffset") && g.has("yoffset")) {
xOffset = getFloat(g, "xoffset", 0.0F);
yOffset = getFloat(g, "yoffset", 0.0F);
} 
this.glyphs.put(Integer.valueOf(unicode), new Glyph(unicode, x, y, w, h, xOffset, yOffset, advance, this.atlasWidth, this.atlasHeight));
}
private float getFloat(JsonObject obj, String key, float def) {
return obj.has(key) ? obj.get(key).getAsFloat() : def;
}
public Glyph getGlyph(int codePoint) {
return this.glyphs.get(Integer.valueOf(codePoint));
}
public boolean hasGlyph(int codePoint) {
return this.glyphs.containsKey(Integer.valueOf(codePoint));
}
public Identifier getTextureId() {
return this.textureId;
}
public float getFontSize() {
return this.fontSize;
}
public float getLineHeight() {
return this.lineHeight;
}
public float getAtlasWidth() {
return this.atlasWidth;
}
public float getAtlasHeight() {
return this.atlasHeight;
}
public float getDistanceRange() {
return this.distanceRange;
}
public boolean isLoaded() {
return this.loaded.get();
}
public int getGlyphCount() {
return this.glyphs.size();
}
}


