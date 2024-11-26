package hein.auto_western_highway._1_20_4;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.List;

@SuppressWarnings("unused")
public class GetItemIdsInShulker {
    public static List<String> getItemIdsInShulker(ItemStack shulker) {
        if (shulker.getNbt() == null) {
            return List.of();
        }
        NbtList shulkerItems = shulker.getNbt().getCompound("BlockEntityTag").getList("Items", 10); // type 10 == NbtCompound
        return shulkerItems.stream().map(x -> ((NbtCompound) x).getString("id").substring(10)).toList();
    }
}
