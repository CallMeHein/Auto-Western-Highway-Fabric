package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.types.StepHeight;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.Baritone.resetSettings;
import static hein.auto_western_highway.Blocks.copyBlock;
import static hein.auto_western_highway.Down.*;
import static hein.auto_western_highway.Globals.globalPlayer;
import static hein.auto_western_highway.Step.step;
import static hein.auto_western_highway.Up.*;
import static hein.auto_western_highway.Utils.getStandingBlock;
import static hein.auto_western_highway.Utils.sendStatusMessage;


public class AutoWesternHighway implements ModInitializer {
    private static boolean running = false;
    private static Thread runningThread;

    @Override
    public void onInitialize() {
        MinecraftClient client = MinecraftClient.getInstance();
        globalPlayer = client.player;
        assert globalPlayer != null;
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                    ClientCommandManager.literal("autoWesternHighway")
                            .executes(context -> {
                                autoWesternHighway();
                                return 1;
                            })
            );

            dispatcher.register(
                    ClientCommandManager.literal("stopAutoWesternHighway")
                            .executes(context -> {
                                if (runningThread != null) {
                                    runningThread.interrupt();
                                    BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                                    sendStatusMessage("Stopping autoWesternHighway...");
                                    return 1;
                                }
                                sendStatusMessage("autoWesternHighway is not running");
                                return 0;
                            })
            );
        });
    }

    private void autoWesternHighway() {
        if (running) {
            sendStatusMessage("Script is already running, stop it with /stopAutoWesternHighway");
            return;
        }
        // start the script in a separate thread - this prevents blocking the mod and allows execution of other commands like /stopAutoWesternHighway
        runningThread = new Thread(() -> {
            resetSettings();
            mainLoop();
            running = false;
            sendStatusMessage("Stopped autoWesternHighway");
        });
        runningThread.start();
    }

    private void mainLoop() {
        BlockPos standingBlock = getStandingBlock();
        standingBlock = new BlockPos(standingBlock.getX(), standingBlock.getY(), 0);
        while (true) {
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
            standingBlock = step(standingBlock, 1);
        }
    }
}