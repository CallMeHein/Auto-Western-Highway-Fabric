package hein.auto_western_highway.common.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static hein.auto_western_highway.common.AutoWesternHighway.nightLogout;

public class HudRenderer {
    public TextRenderer textRenderer;

    public String buildMessage;
    public String inventoryManagementMessage;

    public void render(DrawContext drawContext) {
        if (this.textRenderer == null) {
            this.textRenderer = MinecraftClient.getInstance().textRenderer;
        }
        String settingsMessage = String.format("nightLogout: %s", nightLogout);

        List<String> messages = Stream.of(buildMessage, inventoryManagementMessage, settingsMessage).filter(Objects::nonNull).toList();

        for (int i = 0; i < messages.size(); i++) {
            drawContext.drawText(textRenderer, "AWH: " + messages.get(i), 10, (i + 1) * 10, 0xFFFFFF, false);
        }
    }
}
