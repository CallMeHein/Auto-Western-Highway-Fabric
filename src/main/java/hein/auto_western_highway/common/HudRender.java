package hein.auto_western_highway.common;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class HudRender {
    public String message;
    public TextRenderer textRenderer;

    public void render(DrawContext drawContext) {
        if (this.textRenderer == null) {
            this.textRenderer = MinecraftClient.getInstance().textRenderer;
        }
        drawContext.drawText(textRenderer, "AWH: " + this.message, 10, 10, 0xFFFFFF, false);
    }
}
