package hein.auto_western_highway._1_21;

import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;

import static hein.auto_western_highway.common.utils.Blocks.getBlockId;

@SuppressWarnings("unused")
public class GetItemIdsInShulker {
    public static List<String> getItemIdsInShulker(ItemStack shulker) {
        ContainerComponent items = (ContainerComponent) shulker.getComponents().get(Registries.DATA_COMPONENT_TYPE.get(Identifier.of("minecraft:container")));
        assert items != null;
        return items.stream().filter(item -> item.getItem() instanceof BlockItem).map(item -> getBlockId(item.getItem())).toList();
    }
}
