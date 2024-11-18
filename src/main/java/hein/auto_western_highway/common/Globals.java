package hein.auto_western_highway.common;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class Globals {
    public static final Supplier<MinecraftClient> globalClient = MinecraftClient::getInstance;
    public static final Supplier<@Nullable ClientPlayerEntity> globalPlayer = () -> globalClient.get().player;
    public static final Supplier<ClientPlayerEntity> globalPlayerNonNull = () -> globalClient.get().player;
    public static HudRender globalHudRenderer;
}
