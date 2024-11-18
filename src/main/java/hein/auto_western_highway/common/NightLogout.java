package hein.auto_western_highway.common;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBuilderProcess;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.AutoWesternHighway.nightLogout;
import static hein.auto_western_highway.common.Globals.*;
import static hein.auto_western_highway.common.Utils.*;

public class NightLogout {
    // not the actual values; rounded and padded to allow for some wiggle room without letting mobs spawn
    private static final int FIRST_MOB_SPAWN_TICK = 12850; // ~7:30PM
    private static final int LAST_MOB_SPAWN_TICK = 23050; // ~5AM

    public static void nightLogout() {
        if (!nightLogout) {
            return;
        }
        ClientPlayerEntity player = globalPlayer.get();
        assert player != null;
        long dayTimeTicks = player.clientWorld.getTimeOfDay() % 24_000;
        if (FIRST_MOB_SPAWN_TICK < dayTimeTicks && dayTimeTicks < LAST_MOB_SPAWN_TICK) {
            IBuilderProcess builderProcess = BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess();
            builderProcess.pause();
            waitUntilTrue(builderProcess::isPaused);

            BlockPos logoutPos = getPlayerFeetBlock(globalPlayerNonNull.get());

            DisconnectedScreen screen = new DisconnectedScreen(null, Text.literal("AWH: Disconnected to avoid the night"), Text.literal("Waiting for Meteor to Auto-Reconnect"));
            globalClient.get().execute(() -> globalClient.get().disconnect(screen));
            sleep(3000);
            waitUntilTrue(() -> {
                ClientPlayerEntity _player = globalPlayer.get();
                return _player != null && getPlayerFeetBlock(player).isWithinDistance(logoutPos, 20);
            });
            sleep(3000);
            builderProcess.resume();
        }
    }
}