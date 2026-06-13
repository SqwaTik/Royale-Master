package royale.util.network;

import io.netty.channel.Channel;
import net.minecraft.client.network.ServerInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ViaFabricPlusBridge {
    private static final String IMPL_CLASS = "com.viaversion.viafabricplus.ViaFabricPlusImpl";
    private static final String PROTOCOL_TRANSLATOR_CLASS = "com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator";
    private static final String PROTOCOL_VERSION_CLASS = "com.viaversion.viaversion.api.protocol.version.ProtocolVersion";

    private ViaFabricPlusBridge() {
    }

    public static boolean isAvailable() {
        return loadClass(IMPL_CLASS) != null
                && loadClass(PROTOCOL_VERSION_CLASS) != null
                && loadClass(PROTOCOL_TRANSLATOR_CLASS) != null;
    }

    public static boolean setTargetVersion(String versionText, boolean savePrevious) {
        Object protocolVersion = resolveProtocolVersion(versionText);
        if (protocolVersion == null) {
            return false;
        }

        return setTargetVersion(protocolVersion, savePrevious);
    }

    public static boolean setTargetVersionForServer(ServerInfo serverInfo, boolean savePrevious) {
        if (serverInfo == null) {
            return enableAutoDetectTargetVersion(savePrevious);
        }

        Object protocolVersion = resolveServerProtocolVersion(serverInfo);
        if (protocolVersion != null) {
            return setTargetVersion(protocolVersion, savePrevious);
        }

        if (serverInfo.version != null) {
            String versionText = serverInfo.version.getString();
            if (versionText != null && !versionText.isBlank() && setTargetVersion(versionText, savePrevious)) {
                return true;
            }
        }

        return enableAutoDetectTargetVersion(savePrevious);
    }

    public static boolean enableAutoDetectTargetVersion(boolean savePrevious) {
        Object autoDetect = getStaticField(PROTOCOL_TRANSLATOR_CLASS, "AUTO_DETECT_PROTOCOL");
        if (autoDetect == null) {
            return false;
        }

        return setTargetVersion(autoDetect, savePrevious);
    }

    public static void resetTargetVersion() {
        Object nativeVersion = getStaticField(PROTOCOL_TRANSLATOR_CLASS, "NATIVE_VERSION");
        if (nativeVersion != null) {
            setTargetVersion(nativeVersion, false);
        }
    }

    public static void injectPreviousVersionReset(Channel channel) {
        if (channel == null) {
            return;
        }

        try {
            Class<?> translatorClass = loadClass(PROTOCOL_TRANSLATOR_CLASS);
            if (translatorClass == null) {
                return;
            }

            Method method = translatorClass.getMethod("injectPreviousVersionReset", Channel.class);
            method.invoke(null, channel);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean setTargetVersion(Object protocolVersion, boolean savePrevious) {
        try {
            Class<?> implClass = loadClass(IMPL_CLASS);
            Class<?> protocolVersionClass = loadClass(PROTOCOL_VERSION_CLASS);
            if (implClass == null || protocolVersionClass == null) {
                return false;
            }

            Field instanceField = implClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            Method method = implClass.getMethod("setTargetVersion", protocolVersionClass, boolean.class);
            method.invoke(instance, protocolVersion, savePrevious);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static Object resolveServerProtocolVersion(ServerInfo serverInfo) {
        try {
            Class<?> implClass = loadClass(IMPL_CLASS);
            if (implClass == null) {
                return null;
            }

            Field instanceField = implClass.getField("INSTANCE");
            Object instance = instanceField.get(null);
            Method method = implClass.getMethod("getServerVersion", ServerInfo.class);
            Object protocolVersion = method.invoke(instance, serverInfo);
            if (protocolVersion != null) {
                return protocolVersion;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }

    private static Object resolveProtocolVersion(String versionText) {
        try {
            Class<?> protocolVersionClass = loadClass(PROTOCOL_VERSION_CLASS);
            if (protocolVersionClass == null) {
                return null;
            }

            String canonical = ProtocolVersionResolver.canonicalizeVersion(versionText);
            Method getClosest = protocolVersionClass.getMethod("getClosest", String.class);
            Object protocolVersion = getClosest.invoke(null, canonical);
            if (protocolVersion == null) {
                return null;
            }

            Object unknown = protocolVersionClass.getField("unknown").get(null);
            if (protocolVersion.equals(unknown)) {
                return null;
            }

            return protocolVersion;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object getStaticField(String className, String fieldName) {
        try {
            Class<?> type = loadClass(className);
            if (type == null) {
                return null;
            }

            Field field = type.getField(fieldName);
            return field.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, ViaFabricPlusBridge.class.getClassLoader());
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
