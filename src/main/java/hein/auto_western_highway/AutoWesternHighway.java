package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.types.StepHeight;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.Baritone.resetSettings;
import static hein.auto_western_highway.Blocks.copyBlock;
import static hein.auto_western_highway.Down.*;
import static hein.auto_western_highway.Globals.*;
import static hein.auto_western_highway.Step.step;
import static hein.auto_western_highway.Up.*;
import static hein.auto_western_highway.Utils.getStandingBlock;
import static hein.auto_western_highway.Utils.sendStatusMessage;


public class AutoWesternHighway implements ModInitializer {
    private static boolean running = false;
    private static Thread runningThread;
    private static boolean displayStatus = true;

    public static int stopAutoWesternHighway() {
        if (runningThread != null) {
            runningThread.interrupt();
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            sendStatusMessage("Stopping autoWesternHighway...");
            running = false;
            return 1;
        }
        return 0;
    }

    private static void setGlobals() {
        globalClient = MinecraftClient.getInstance();
        globalPlayer = globalClient.player;
        globalHudRenderer = new HudRender();
        HudRenderCallback.EVENT.register((drawContext, delta) -> {
            if (running && displayStatus) {
                globalHudRenderer.render(drawContext);
            }
        });
    }

    private static void autoWesternHighway() {
        if (running) {
            sendStatusMessage("Script is already running, stop it with /stopAutoWesternHighway");
            return;
        }
        // start the script in a separate thread - this prevents freezing the game whenever we sleep the thread
        runningThread = new Thread(() -> {
            resetSettings();
            running = true;
            mainLoop();
        });
        runningThread.start();
    }

    private static void mainLoop() {
        BlockPos standingBlock = getStandingBlock();
        standingBlock = new BlockPos(standingBlock.getX(), standingBlock.getY(), 0);
        while (true) {
            // check if/how much we should step up
            StepHeight stepUpHeight = getStepUpHeight(standingBlock);
            if (stepUpHeight.height > 0) {
                int futureStepDownLength = getFutureStepDownLength(standingBlock, stepUpHeight.height);
                if (futureStepDownLength > 0) {
                    standingBlock = step(standingBlock, futureStepDownLength);
                    continue;
                }
                upwardScaffold(stepUpHeight, copyBlock(standingBlock));
                standingBlock = stepUp(stepUpHeight.height, standingBlock);
                continue;
            }
            // check if/how much we should step down
            StepHeight stepDownHeight = getStepDownHeight(standingBlock);
            if (stepDownHeight.height > 0) {
                int futureStepUpLength = getFutureStepUpLength(standingBlock, stepUpHeight.height);
                if (futureStepUpLength > 0) {
                    standingBlock = step(standingBlock, futureStepUpLength);
                    continue;
                }
                downwardScaffold(stepDownHeight, copyBlock(standingBlock));
                standingBlock = stepDown(stepDownHeight.height, standingBlock);
                continue;
            }
            // just step forward if neither
            standingBlock = step(standingBlock, 1);
        }
    }

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autoWesternHighway").executes(context -> {
                setGlobals();
                autoWesternHighway();
                return 1;
            }));

            dispatcher.register(ClientCommandManager.literal("toggleStatusDisplay").executes(context -> {
                if (running) {
                    displayStatus = !displayStatus;
                    sendStatusMessage("Showing AutoWesternHighway status: " + displayStatus);
                    return 1;
                }
                return 0;
            }));

            dispatcher.register(ClientCommandManager.literal("stopAutoWesternHighway").executes(context ->
                    stopAutoWesternHighway())
            );
        });
    }
}