package hein.auto_western_highway.common.building;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.IBuilderProcess;
import baritone.api.utils.BetterBlockPos;
import hein.auto_western_highway.common.types.ResourceLoadout;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.ShulkerBoxSlot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static hein.auto_western_highway.common.AutoWesternHighway.stopAutoWesternHighway;
import static hein.auto_western_highway.common.Globals.*;
import static hein.auto_western_highway.common.building.Baritone.build;
import static hein.auto_western_highway.common.building.Baritone.resetSettings;
import static hein.auto_western_highway.common.types.AutoHighwaySchematic.CLEAR_PLAYER_SPACE;
import static hein.auto_western_highway.common.types.InventoryLoadout.*;
import static hein.auto_western_highway.common.utils.Blocks.*;
import static hein.auto_western_highway.common.utils.Reflections.invokeVersionSpecific;
import static hein.auto_western_highway.common.utils.Wait.*;
import static net.minecraft.screen.slot.SlotActionType.*;
import static net.minecraft.util.Hand.MAIN_HAND;

public class InventoryManagement {

    public static void replenishItemsIfNeeded() {
        ClientPlayerEntity player = globalPlayerNonNull.get();
        if (player.getPos().getY() % 1 != 0) {
            return; // surely this will never cause us to run out of materials
        }
        resetInventoryLoadout();

        ArrayList<ItemStack> items = new ArrayList<>(player.getInventory().main);
        countMaterials(items);
        if (getLowMaterials().isEmpty()) {
            return;
        }
        while (true) {
            items = new ArrayList<>(player.getInventory().main);

            ArrayList<ItemStack> shulkers = new ArrayList<>(items.stream()
                    .filter(item ->
                            (item.getItem() instanceof BlockItem) &&
                                    getBlockId(item.getItem()).equals("shulker_box") &&
                                    getRelevantItemStacksCount(item) > 0
                    ).toList());

            shulkers.sort(Comparator.comparingInt(InventoryManagement::getRelevantItemStacksCount));

            // if we have no shulkers with needed items, we're done replenishing
            if (shulkers.isEmpty()) {
                break;
            }
            // if all materials are above the replenish target threshold, we've successfully replenished
            if (getUnfilledMaterials().isEmpty()) {
                break;
            }
            BlockPos shulkerPos = getStandingBlock(player);
            placeShulker(shulkerPos, player, shulkers.get(0), items);
            extractItems(player);
            globalClient.get().execute(() -> {
                globalClient.get().setScreen(null);
                player.closeHandledScreen();
            });
            // wait for shulker to fully close
            waitUntilTrue(() -> getPlayerFeetBlock(player).getY() == player.getPos().getY());

            int shulkerCount = getShulkerCount(player);
            breakShulker(new BetterBlockPos(offsetBlock(shulkerPos, 0, 1, 0)));
            BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(offsetBlock(shulkerPos, 0, 1, 0)));
            boolean immediatelyPickedUp = waitUntilTrueWithTimeout(() -> getShulkerCount(player) != shulkerCount, 250, 1500); // await potential immediate pickup
            if (!immediatelyPickedUp) {
                pickupShulker(shulkerCount);
            }
            sleep(200);
        }
        globalHudRenderer.setInventoryManagementMessage(null);
    }

    private static int getShulkerCount(ClientPlayerEntity player) {
        return player.getInventory().main
                .stream().filter(item ->
                        item.getItem() instanceof BlockItem &&
                                getBlockId(item.getItem()).equals("shulker_box")).toList().size();
    }

    public static void setHotbarToInventoryLoadout() {
        inventoryLoadout.forEach(resource -> globalClient.get().execute(() -> {
            PlayerInventory inventory = globalPlayerNonNull.get().getInventory();
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

            if (!(itemInHotbarAtPreferredSlot.getItem() instanceof BlockItem) || // the item in our hotbar is not a block (it def can't be the correct item)
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

    private static void extractItems(ClientPlayerEntity player) {
        globalHudRenderer.setInventoryManagementMessage("Extracting items from shulker");
        waitUntilTrue(() -> player.currentScreenHandler instanceof ShulkerBoxScreenHandler);
        ScreenHandler screen = player.currentScreenHandler;
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
                    sleep(100); // allow inventory to update
                    break;
                }
            }
        }
        globalHudRenderer.setInventoryManagementMessage("Extracting items from shulker DONE");
    }

    private static void placeShulker(BlockPos position, ClientPlayerEntity player, ItemStack shulker, ArrayList<ItemStack> items) {
        BaritoneAPI.getSettings().buildIgnoreBlocks.value = new ArrayList<>();
        BaritoneAPI.getSettings().layerOrder.value = false;
        build(CLEAR_PLAYER_SPACE, offsetBlock(getPlayerFeetBlock(player), -1, 1, -1));
        resetSettings();
        int shulkerSlot = items.indexOf(shulker);
        // slot index does not match slot id when the inventory is open, so we add the needed amount to the id
        moveItem(shulker, shulkerSlot <= 8 ? shulkerSlot + 36 : shulkerSlot, 8 + 36, true);
        player.getInventory().selectedSlot = 8;
        waitUntilTrue(() -> player.getInventory().getMainHandStack().getItem() instanceof BlockItem && ((BlockItem)player.getInventory().getMainHandStack().getItem()).getBlock() instanceof ShulkerBoxBlock);

        globalHudRenderer.setInventoryManagementMessage("Placing shulker");
        player.jump();
        while (!getBlocksNameFromBlockPositions(List.of(copyBlock(position, 0, 1, 0))).get(0).equals("shulker_box")) {
            rightClick(position, player); // place
            sleep(50);
        }
        globalHudRenderer.setInventoryManagementMessage("Placing shulker DONE");
        globalHudRenderer.setInventoryManagementMessage("Opening shulker");
        position = position.offset(Direction.Axis.Y, 1);
        rightClick(position, player); // open
        waitUntilTrue(() -> globalClient.get().currentScreen instanceof ShulkerBoxScreen);
        globalHudRenderer.setInventoryManagementMessage("Opening shulker DONE");
    }

    private static void rightClick(BlockPos position, ClientPlayerEntity player) {
        ClientPlayerInteractionManager interactionManager = globalClient.get().interactionManager;
        assert interactionManager != null;
        globalClient.get().execute(() ->
                interactionManager.interactBlock(player, MAIN_HAND, new BlockHitResult(
                        Vec3d.ofCenter(position),
                        Direction.UP,
                        position,
                        false
                )));
    }

    private static void breakShulker(BetterBlockPos position) {
        globalHudRenderer.setInventoryManagementMessage("Breaking shulker");
        try {
            IBuilderProcess builderProcess = BaritoneAPI.getProvider().getPrimaryBaritone().getBuilderProcess();
            builderProcess.clearArea(position, position);
            waitUntilTrue(() -> !builderProcess.isActive());
            globalHudRenderer.setInventoryManagementMessage("Breaking shulker DONE");
        } catch (
                IllegalArgumentException ignored) { // baritone sometimes conflicts between BetterBlockPos and BlockPos, does not actually affect the script
        }
    }

    private static void pickupShulker(int startingShulkerCount) {
        globalHudRenderer.setInventoryManagementMessage("Picking up shulker");
        MinecraftClient client = globalClient.get();
        assert client != null;
        assert client.world != null;
        assert client.interactionManager != null;
        ClientPlayerEntity player = globalPlayerNonNull.get();

        Vec3d playerPos = player.getPos();
        Box searchBox = new Box(playerPos.subtract(10, 10, 10), playerPos.add(10, 10, 10));

        for (Entity entity : client.world.getEntitiesByClass(ItemEntity.class, searchBox, e -> true)) {
            if (entity instanceof ItemEntity itemEntity && itemEntity.toString().contains("Shulker Box")) {
                Vec3d prevXYZ = null;
                ItemEntity targetEntity;
                // wait for shulker item to stop moving
                while (true) {
                    targetEntity = (ItemEntity) client.world.getEntityById(itemEntity.getId());
                    assert targetEntity != null;
                    if (prevXYZ != null && prevXYZ.equals(new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()))) {
                        break;
                    }
                    prevXYZ = new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                    sleep(500);
                }
                BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(itemEntity.getBlockPos()));
                waitUntilTrue(() -> getShulkerCount(player) > startingShulkerCount); // Baritone's API is async, so we manually wait until it is picked up
                globalHudRenderer.setInventoryManagementMessage("Picking up shulker DONE");
            }
        }
    }

    private static int getRelevantItemStacksCount(ItemStack shulker) {
        List<String> shulkerItems = invokeVersionSpecific("GetItemIdsInShulker", "getItemIdsInShulker", shulker);
        int relevantCount = 0;
        for (String itemId : shulkerItems) {
            for (ResourceLoadout resource : getLowMaterials()) {
                if (resource.block.equals(itemId)) {
                    relevantCount += 1;
                }
            }
        }
        return relevantCount;
    }

    public static void swapItem(ItemStack item, int sourceSlot, int targetSlot) {
        ScreenHandler screenHandler = globalPlayerNonNull.get().currentScreenHandler;
        globalClient.get().execute(() -> {
            ClickSlotC2SPacket pickupPacket = new ClickSlotC2SPacket(
                    screenHandler.syncId,
                    screenHandler.getRevision(),
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
        ClientPlayNetworkHandler networkHandler = globalClient.get().getNetworkHandler();
        assert networkHandler != null;
        networkHandler.sendPacket(packet);
    }

    public static void moveItem(ItemStack item, int sourceSlot, int targetSlot, boolean openInventory) {
        if (openInventory) {
            globalClient.get().execute(() -> globalClient.get().setScreen(new InventoryScreen(globalPlayerNonNull.get())));
        }
        clickSlot(item, targetSlot, QUICK_MOVE); // quick move whatever is in the target slot away
        // "PICKUP" is just a badly-named regular click, we use it to both pick up and put down
        clickSlot(item, sourceSlot, PICKUP); // pick up
        clickSlot(item, targetSlot, PICKUP); // put down
        if (openInventory) {
            globalClient.get().execute(() -> globalClient.get().setScreen(null));
        }
    }

    private static void clickSlot(ItemStack shulker, int slotId, SlotActionType action) {
        ScreenHandler screenHandler = globalPlayerNonNull.get().currentScreenHandler;
        ClickSlotC2SPacket pickupPacket = new ClickSlotC2SPacket(
                screenHandler.syncId,
                screenHandler.getRevision(),
                slotId,
                0,
                action,
                shulker,
                new Int2ObjectArrayMap<>()
        );
        sendPacket(pickupPacket);
    }
}
