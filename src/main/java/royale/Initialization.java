package royale;
import net.fabricmc.api.ClientModInitializer;
import royale.manager.Manager;
public class Initialization
implements ClientModInitializer {
private static Initialization instance;
private Manager manager;
public static Initialization getInstance() {
return instance;
}
public Manager getManager() {
return this.manager;
}
public void onInitializeClient() {}
public void init() {
instance = this;
this.manager = new Manager();
this.manager.init();
}
}


