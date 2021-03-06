package pixlepix.auracascade.block.tile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.main.Config;

/**
 * Created by pixlepix on 12/25/14.
 */
public class AuraTilePumpRedstone extends AuraTilePumpBase {


    @Override
    public void update() {
        super.update();
        if (pumpPower == 0) {
            for (EnumFacing direction : EnumFacing.VALUES) {
                for (int i = 1; i < 16; i++) {
                    BlockPos pos = getPos().offset(direction, i);
                    Block block = world.getBlockState(pos).getBlock();
                    if (block instanceof BlockRedstoneWire && world.getBlockState(pos).getValue(BlockRedstoneWire.POWER) > 0) {
                        addFuel((int) (Config.pumpRedstoneDuration * Math.pow(1.4, i)), Config.pumpRedstoneSpeed);
                        if (!world.isRemote) {
                            for (int j = 0; j < 5; j++) {
                                AuraCascade.proxy.addBlockDestroyEffects(pos);
                            }
                        }

                        world.setBlockToAir(pos);
                    } else {
                        break;
                    }
                }
            }

        }
    }
}
