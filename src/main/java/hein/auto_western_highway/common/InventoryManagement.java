package hein.auto_western_highway.common;

import baritone.api.BaritoneAPI;
import hein.auto_western_highway.common.types.ResourceLoadout;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static hein.auto_western_highway.common.AutoHighwaySchematic.CLEAR_PLAYER_SPACE;
import static hein.auto_western_highway.common.AutoWesternHighway.stopAutoWesternHighway;
import static hein.auto_western_highway.common.Baritone.build;
import static hein.auto_western_highway.common.Baritone.resetSettings;
import static hein.auto_western_highway.common.Blocks.*;
import static hein.auto_western_highway.common.Globals.globalClient;
import static hein.auto_western_highway.common.Globals.globalPlayer;
import static hein.auto_western_highway.common.InvokeVersionSpecific.invokeVersionSpecific;
import static hein.auto_western_highway.common.Utils.*;
import static hein.auto_western_highway.common.types.InventoryLoadout.*;
import static net.minecraft.screen.slot.SlotActionType.*;
import static net.minecraft.util.Hand.MAIN_HAND;

public class InventoryManagement {

    public static void replenishItemsIfNeeded() {
        if (globalPlayer.getPos().getY() % 1 != 0) {
            return; // surely this will never cause us to run out of materials
        }
        resetInventoryLoadout();

        ArrayList<ItemStack> items = new ArrayList<>(globalPlayer.getInventory().main);
        countMaterials(items);
        if (getLowMaterials().isEmpty()) {
            return;
        }
        while (true) {
            items = new ArrayList<>(globalPlayer.getInventory().main);
            ArrayList<ItemStack> shulkers = new ArrayList<>(items.stream().filter(item -> (item.getItem() instanceof BlockItem) && getBlockId(item.getItem()).equals("shulker_box")).toList());
            shulkers.sort(Comparator.comparingInt(shulker -> getRelevantItemCount((ItemStack) shulker)).reversed());
            // if we have no shulkers with needed items, we're done replenishing
            if (getRelevantItemCount(shulkers.get(0)) == 0) {
                return;
            }
            // if all materials are above the replenish target threshold, we've successfully replenished
            if (getUnfilledMaterials().isEmpty()) {
                return;
            }
            placeShulker(shulkers.get(0), items);
            extractItems();
            globalClient.execute(() -> {
                globalClient.setScreen(null);
                globalPlayer.closeHandledScreen();
            });
            // wait for shulker to fully close
            waitUntilTrue(() -> getPlayerFeetBlock().getY() == globalPlayer.getPos().getY());

            int shulkerCount = getShulkerCount();
            breakBlock(getStandingBlock(), "shulker_box");
            waitUntilTrue(() -> getShulkerCount() > shulkerCount);
        }
    }

    private static int getShulkerCount() {
        return globalPlayer.getInventory().main
                .stream().filter(item ->
                        item.getItem() instanceof BlockItem &&
                                getBlockId(item.getItem()).equals("shulker_box")).toList().size();
    }

    public static void setHotbarToInventoryLoadout() {
        inventoryLoadout.forEach(resource -> globalClient.execute(() -> {
            PlayerInventory inventory = globalPlayer.getInventory();
            ItemStack itemInHotbarAtPreferredSlot = inventory.getStack(resource.preferredSlotId);
            ItemStack itemStackWithTargetItem = inventory.main.stream().filter(item ->
                            item.getItem() instanceof BlockItem &&
                                    getBlockId(item.getItem()).equals(resource.block)) // get all ItemStacks that contain the target item
                    .max(Comparator.comparingInt(ItemStack::getCount)) // get the stack that has the most items in it
                    .orElse(null);
            // if we don't have any ItemStacks of the target item in our inventory, we're done because we would have restocked from shulkers before this
            if (itemStackWithTargetItem == null) {
                stopAutoWesternHighway();
                return;
            }
            assert itemStackWithTargetItem.getItem() instanceof BlockItem; // we only deal with blocks when we set our hotbar, so the target item will always be instanceof BlockItem

            if (!(itemInHotbarAtPreferredSlot.getItem() instanceof BlockItem) || // the item is not a block (it def can't be the correct item)
                    !getBlockId((itemInHotbarAtPreferredSlot.getItem())).equals(getBlockId((itemStackWithTargetItem.getItem()))) || // OR it is a block that is not the correct item
                    getBlockId(itemInHotbarAtPreferredSlot.getItem()).equals(getBlockId((itemStackWithTargetItem.getItem()))) && // OR ( it is the correct item,
                            itemInHotbarAtPreferredSlot.getCount() < itemStackWithTargetItem.getCount() // AND has a lower count than another stack in our inv)
            ) {
                int inventorySlotWithTargetItem = inventory.main.indexOf(itemStackWithTargetItem);
                swapItem(itemStackWithTargetItem, inventorySlotWithTargetItem, resource.preferredSlotId);
            }

        }));
    }

    private static void countMaterials(ArrayList<ItemStack> items) {
        items.forEach(item -> {
            if (item.getItem() instanceof BlockItem) {
                String blockId = getBlockId(item.getItem());
                if (getInventoryLoadoutBlocks().contains(blockId)) {
                    addToInventoryLoadout(blockId, item.getCount());
                }
            }
        });
    }

    private static void extractItems() {
        waitUntilTrue(() -> globalPlayer.currentScreenHandler instanceof ShulkerBoxScreenHandler);
        ScreenHandler screen = globalPlayer.currentScreenHandler;
        Inventory shulkerInventory = screen.slots.stream().filter(slot -> slot instanceof ShulkerBoxSlot).findFirst().orElseThrow().inventory;
        for (int shulkerSlotId = 0; shulkerSlotId < shulkerInventory.size(); shulkerSlotId++) {
            ItemStack item = shulkerInventory.getStack(shulkerSlotId);
            if (!(item.getItem() instanceof BlockItem)) {
                continue;
            }
            String itemId = getBlockId((item.getItem()));
            for (ResourceLoadout resource : getUnfilledMaterials()) {
                if (resource.block.equals(itemId)) {
                    clickSlot(item, shulkerSlotId, QUICK_MOVE);
                    addToInventoryLoadout(itemId, item.getCount());
                    break;
                }
            }
        }
    }

    private static void placeShulker(ItemStack shulker, ArrayList<ItemStack> items) {
        BaritoneAPI.getSettings().buildIgnoreBlocks.value = new ArrayList<>();
        BaritoneAPI.getSettings().layerOrder.value = false;
        build(CLEAR_PLAYER_SPACE, offsetBlock(getPlayerFeetBlock(), -1, 1, -1));
        resetSettings();
        int shulkerSlot = items.indexOf(shulker);
        // slot index does not match slot id when the inventory is open, so we add the needed amount to the id
        moveItem(shulker, shulkerSlot <= 8 ? shulkerSlot + 36 : shulkerSlot, 8 + 36, true);
        globalPlayer.getInventory().selectedSlot = 8;

        BlockPos position = getStandingBlock();
        globalPlayer.jump();
        while (!getBlocksNameFromBlockPositions(List.of(getStandingBlock())).get(0).equals("shulker_box")) {
            rightClick(position); // place
            sleep(50);
        }
        position = position.offset(Direction.Axis.Y, 1);
        rightClick(position); // open
        waitUntilTrue(() -> globalClient.currentScreen instanceof ShulkerBoxScreen);
    }

    private static void rightClick(BlockPos position) {
        assert globalClient.interactionManager != null;
        globalClient.execute(() ->
                globalClient.interactionManager.interactBlock(globalPlayer, MAIN_HAND, new BlockHitResult(
                        Vec3d.ofCenter(position),
                        Direction.UP,
                        position,
                        false
                )));
    }

    private static void breakBlock(BlockPos position, String blockId) {
        assert globalClient.interactionManager != null;
        while (getBlocksNameFromBlockPositions(List.of(position)).stream().findFirst().orElseThrow().equals(blockId)) {
            CountDownLatch latch = new CountDownLatch(1);
            globalClient.execute(() -> {
                globalClient.interactionManager.attackBlock(position, Direction.UP);
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, position, Direction.UP));
                latch.countDown();
            });
            try {
                latch.await(); // we don't want to sleep within client.execute() because it freezes the entire game, so we do the workaround with Latch
                sleep(1000);
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, position, Direction.UP));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static int getRelevantItemCount(ItemStack shulker) {
        List<String> shulkerItems = invokeVersionSpecific("GetItemIdsInShulker", "getItemIdsInShulker",shulker);
        List<String> foundRelevantItems = new ArrayList<>();
        shulkerItems.forEach(itemId -> {
            for (ResourceLoadout resource : getLowMaterials()) {
                if (resource.block.equals(itemId) && !foundRelevantItems.contains(resource.block)) {
                    foundRelevantItems.add(resource.block);
                }
            }
        });
        return foundRelevantItems.size();
    }

    public static void swapItem(ItemStack item, int sourceSlot, int targetSlot) {
        globalClient.execute(() -> {
            ClickSlotC2SPacket pickupPacket = new ClickSlotC2SPacket(
                    globalPlayer.currentScreenHandler.syncId,
                    globalPlayer.currentScreenHandler.getRevision(),
                    sourceSlot,
                    targetSlot,
                    SWAP,
                    item,
                    new Int2ObjectArrayMap<>()
            );
            sendPacket(pickupPacket);
        });
    }

    private static void sendPacket(Packet<ServerPlayPacketListener> packet) {
        assert globalClient.getNetworkHandler() != null;
        globalClient.getNetworkHandler().sendPacket(packet);
    }

    public static void moveItem(ItemStack item, int sourceSlot, int targetSlot, boolean openInventory) {
        if (openInventory) {
            globalClient.execute(() -> globalClient.setScreen(new InventoryScreen(globalPlayer)));
        }
        clickSlot(item, targetSlot, QUICK_MOVE); // quick move whatever is in the target slot away
        // "PICKUP" is just a badly-named regular click, we use it to both pick up and put down
        clickSlot(item, sourceSlot, PICKUP); // pick up
        clickSlot(item, targetSlot, PICKUP); // put down
        if (openInventory) {
            globalClient.execute(() -> globalClient.setScreen(null));
        }
    }

    private static void clickSlot(ItemStack shulker, int slotId, SlotActionType action) {
        ClickSlotC2SPacket pickupPacket = new ClickSlotC2SPacket(
                globalPlayer.currentScreenHandler.syncId,
                globalPlayer.currentScreenHandler.getRevision(),
                slotId,
                0,
                action,
                shulker,
                new Int2ObjectArrayMap<>()
        );
        sendPacket(pickupPacket);
    }
}
