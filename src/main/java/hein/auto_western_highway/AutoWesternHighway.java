package hein.auto_western_highway;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.types.StepHeight;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.Baritone.resetSettings;
import static hein.auto_western_highway.Blocks.copyBlock;
import static hein.auto_western_highway.Down.*;
import static hein.auto_western_highway.Step.step;
import static hein.auto_western_highway.Up.*;
import static hein.auto_western_highway.Utils.getStandingBlock;
import static hein.auto_western_highway.Utils.sendStatusMessage;


public class AutoWesternHighway implements ModInitializer {
    private static boolean running = false;
    private static Thread runningThread;

    @Override
    public void onInitialize() {
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
                                MinecraftClient client = MinecraftClient.getInstance();
                                ClientPlayerEntity player = client.player;
                                assert player != null;
                                if (runningThread != null) {
                                    runningThread.interrupt();
                                    BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                                    sendStatusMessage(player, "Stopping autoWesternHighway...");
                                    return 1;
                                }
                                sendStatusMessage(player, "autoWesternHighway is not running");
                                return 0;
                            })
            );
        });
    }

    private void autoWesternHighway() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        assert player != null;
        if (running) {
            sendStatusMessage(player, "Script is already running, stop it with /stopAutoWesternHighway");
            return;
        }
        // start the script in a separate thread - this prevents blocking the mod and allows execution of other commands like /stopAutoWesternHighway
        runningThread = new Thread(() -> {
            resetSettings();
            mainLoop(player);
            running = false;
            sendStatusMessage(player, "Stopped autoWesternHighway");
        });
        runningThread.start();
    }

    private void mainLoop(ClientPlayerEntity player) {
        BlockPos standingBlock = getStandingBlock(player);
        standingBlock = new BlockPos(standingBlock.getX(), standingBlock.getY(), 0);
        while (true) {
            StepHeight stepUpHeight = getStepUpHeight(player, standingBlock);
            if (stepUpHeight.height > 0) {
                int futureStepDownLength = getFutureStepDownLength(player, standingBlock, stepUpHeight.height);
                if (futureStepDownLength > 0) {
                    standingBlock = step(standingBlock, futureStepDownLength);
                    continue;
                }
                upwardScaffold(stepUpHeight, copyBlock(standingBlock));
                standingBlock = stepUp(stepUpHeight.height, standingBlock);
                continue;
            }

            StepHeight stepDownHeight = getStepDownHeight(player, standingBlock);
            if (stepDownHeight.height > 0) {
                int futureStepUpLength = getFutureStepUpLength(player, standingBlock, stepUpHeight.height);
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