package royale.util.proxy;

import com.google.gson.annotations.SerializedName;

public class Proxy {
    @SerializedName(value="IP:PORT")
    public String ipPort = "";
    public ProxyType type = ProxyType.SOCKS5;
    public String username = "";
    public String password = "";

    public Proxy() {
    }

    public Proxy(boolean isSocks4, String ipPort, String username, String password) {
        this.type = isSocks4 ? ProxyType.SOCKS4 : ProxyType.SOCKS5;
        this.ipPort = ipPort;
        this.username = username;
        this.password = password;
    }

    public Proxy(Proxy other) {
        this.ipPort = other.ipPort;
        this.type = other.type;
        this.username = other.username;
        this.password = other.password;
    }

    public int getPort() {
        if (this.ipPort == null || this.ipPort.isEmpty() || !this.ipPort.contains(":")) {
            return 0;
        }
        try {
            return Integer.parseInt(this.ipPort.split(":")[1]);
        }
        catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return 0;
        }
    }

    public String getIp() {
        if (this.ipPort == null || this.ipPort.isEmpty() || !this.ipPort.contains(":")) {
            return "";
        }
        return this.ipPort.split(":")[0];
    }

    public boolean isEmpty() {
        return this.ipPort == null || this.ipPort.isEmpty();
    }

    public static enum ProxyType {
        SOCKS4,
        SOCKS5;

    }
}

