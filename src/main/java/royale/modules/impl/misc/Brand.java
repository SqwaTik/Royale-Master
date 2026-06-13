package royale.modules.impl.misc;

import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import royale.events.api.EventHandler;
import royale.events.impl.GameLeftEvent;
import royale.events.impl.PacketEvent;
import royale.events.impl.WorldChangeEvent;
import royale.modules.module.ModuleStructure;
import royale.modules.module.category.ModuleCategory;
import royale.modules.module.setting.Setting;
import royale.modules.module.setting.implement.SelectSetting;
import royale.modules.module.setting.implement.TextSetting;
import royale.util.Instance;

import java.util.Locale;

public class Brand extends ModuleStructure {
    private static final String CLIENT_FABRIC = "Fabric";
    private static final String CLIENT_FORGE = "Forge";
    private static final String CLIENT_NEOFORGE = "NeoForge";
    private static final String CLIENT_QUILT = "Quilt";
    private static final String CLIENT_CUSTOM = "Custom";

    private boolean brandSpoofPending;

    public final SelectSetting clientType = (new SelectSetting(
            "Клиент",
            "Что увидит сервер в brand"
    )).value(CLIENT_FABRIC, CLIENT_FORGE, CLIENT_NEOFORGE, CLIENT_QUILT, CLIENT_CUSTOM).selected(CLIENT_FABRIC);

    public final TextSetting customClient = (new TextSetting(
            "Кастомный бренд",
            "Свое значение brand"
    )).setText("")
            .setMin(0)
            .setMax(64)
            .setPlaceholder("fabric, forge, lunar, custom...")
            .setNormalizer(value -> value == null ? "" : value.trim())
            .visible(() -> this.clientType.isSelected(CLIENT_CUSTOM));

    public Brand() {
        super("Brand", "Меняет brand при подключении", ModuleCategory.MISC);
        settings(new Setting[]{this.clientType, this.customClient});
    }

    public static Brand getInstance() {
        return Instance.get(Brand.class);
    }

    @Override
    public void activate() {
        this.brandSpoofPending = false;
    }

    @Override
    public void deactivate() {
        this.brandSpoofPending = false;
    }

    public void prepareConnection() {
        this.brandSpoofPending = isState();
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!isState() || event.getType() != PacketEvent.Type.SEND) {
            return;
        }

        if (event.getPacket() instanceof CustomPayloadC2SPacket payloadPacket
                && payloadPacket.payload() instanceof BrandCustomPayload
                && this.brandSpoofPending) {
            this.brandSpoofPending = false;
            event.setPacket(new CustomPayloadC2SPacket(new BrandCustomPayload(resolveClientName())));
        }
    }

    @EventHandler
    public void onWorldChange(WorldChangeEvent event) {
        this.brandSpoofPending = false;
    }

    @EventHandler
    public void onGameLeft(GameLeftEvent event) {
        this.brandSpoofPending = false;
    }

    private String resolveClientName() {
        String selected = this.clientType.getSelected();
        if (CLIENT_CUSTOM.equals(selected)) {
            String customValue = this.customClient.getText();
            if (customValue != null && !customValue.isBlank()) {
                return customValue.trim();
            }
            return "custom";
        }

        return switch (selected) {
            case CLIENT_FORGE -> "forge";
            case CLIENT_NEOFORGE -> "neoforge";
            case CLIENT_QUILT -> "quilt";
            case CLIENT_FABRIC -> "fabric";
            default -> selected == null ? "custom" : selected.toLowerCase(Locale.ROOT);
        };
    }
}
