package hein.auto_western_highway.common;

import net.minecraft.block.Block;

import java.util.List;

import static net.minecraft.block.Blocks.*;


public class Constants {
    public static final int MAX_RAY_STEPS = 100;
    public static final int FUTURE_STEPS = 3;

    public static final List<Block> buildIgnoreBlocks = List.of(
            ALLIUM,
            AZURE_BLUET,
            BLUE_ORCHID,
            BROWN_MUSHROOM,
            CACTUS,
            CARROTS,
            CORNFLOWER,
            DANDELION,
            FERN,
            FIRE,
            LILAC,
            LILY_OF_THE_VALLEY,
            MANGROVE_LOG,
            ORANGE_TULIP,
            OXEYE_DAISY,
            PEONY,
            PINK_TULIP,
            POPPY,
            POTATOES,
            RED_MUSHROOM,
            RED_TULIP,
            ROSE_BUSH,
            SHORT_GRASS,
            SOUL_FIRE,
            SUGAR_CANE,
            SUNFLOWER,
            TORCH,
            TORCHFLOWER,
            WALL_TORCH,
            WATER,
            WHEAT,
            WHITE_TULIP,
            WITHER_ROSE
    );
}
