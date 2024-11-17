package hein.auto_western_highway.common;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.common.types.StepFunctionWithCount;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import static hein.auto_western_highway.common.Baritone.resetSettings;
import static hein.auto_western_highway.common.BlockRenderer.blockRendererBlocks;
import static hein.auto_western_highway.common.FuturePath.renderFuturePath;
import static hein.auto_western_highway.common.Globals.*;
import static hein.auto_western_highway.common.Movement.adjustStandingBlock;
import static hein.auto_western_highway.common.Movement.getStepFunction;
import static hein.auto_western_highway.common.Utils.getStandingBlock;
import static hein.auto_western_highway.common.Utils.sendStatusMessage;


public class AutoWesternHighway implements ModInitializer {
    public static boolean running = false;
    public static boolean displayFuturePath = true;
    private static Thread scriptThread;
    private static Thread futurePathThread;
    private static boolean displayStatus = true;

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
        scriptThread = new Thread(() -> {
            resetSettings();
            running = true;
            BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess().resume();
            mainLoop();
        });
        scriptThread.start();

        futurePathThread = new Thread(() -> {
            try {
                renderFuturePath();
            } catch (NoSuchMethodException e) {
                blockRendererBlocks = null;
                e.printStackTrace();
            }
        });
        futurePathThread.start();
    }

    public static void stopAutoWesternHighway() {
        if (scriptThread != null) {
            blockRendererBlocks = null;
            scriptThread.interrupt();
            futurePathThread.interrupt();
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            sendStatusMessage("Stopping autoWesternHighway...");
            running = false;
        }
    }

    private static void mainLoop() {
        BlockPos standingBlock = getStandingBlock();
        standingBlock = new BlockPos(standingBlock.getX(), standingBlock.getY(), 0);
        while (running) {
            StepFunctionWithCount stepFunction = getStepFunction(standingBlock);
            if (stepFunction == null) {
                stopAutoWesternHighway();
                throw new RuntimeException("No step function found");
            }
            try {
                if (stepFunction.scaffoldFunction != null) {
                    stepFunction.scaffoldFunction.invoke(null, stepFunction.stepHeight, standingBlock);
                }
                stepFunction.stepFunction.invoke(null, standingBlock, stepFunction.stepHeight.count);
            } catch (Exception e) {
                stopAutoWesternHighway();
                throw new RuntimeException(e);
            }
            standingBlock = adjustStandingBlock(standingBlock, stepFunction);
        }
    }


    @Override
    public void onInitialize() {
        WorldRenderEvents.AFTER_ENTITIES.register(BlockRenderer::blockRendererBlock);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // start main script
            dispatcher.register(ClientCommandManager.literal("autoWesternHighway").executes(context -> {
                setGlobals();
                autoWesternHighway();
                return 1;
            }));

            // stop main script
            dispatcher.register(ClientCommandManager.literal("stopAutoWesternHighway").executes(context -> {
                if (running) {
                    stopAutoWesternHighway();
                    return 1;
                }
                return 0;
            }));

            // toggle HUD status texts
            dispatcher.register(ClientCommandManager.literal("toggleAutoWesternHighwayStatusDisplay").executes(context -> {
                if (running) {
                    displayStatus = !displayStatus;
                    sendStatusMessage("Showing AutoWesternHighway status: " + displayStatus);
                    return 1;
                }
                return 0;
            }));

            // toggle future path wireframe blocks
            dispatcher.register(ClientCommandManager.literal("toggleAutoWesternHighwayWireframes").executes(context -> {
                if (running) {
                    displayFuturePath = !displayFuturePath;
                    sendStatusMessage("Showing AutoWesternHighway wireframes: " + displayFuturePath);
                    if (!displayFuturePath) {
                        blockRendererBlocks = null;
                    }
                    return 1;
                }
                return 0;
            }));
        });
    }
}