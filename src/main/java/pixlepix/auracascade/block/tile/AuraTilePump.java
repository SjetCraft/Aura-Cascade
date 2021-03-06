package pixlepix.auracascade.block.tile;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.main.AuraUtil;
import pixlepix.auracascade.main.Config;
import pixlepix.auracascade.network.PacketBurst;

import java.util.List;

/**
 * Created by pixlepix on 11/29/14.
 */
public class AuraTilePump extends AuraTilePumpBase {


    @Override
    public void update() {
        super.update();
        if (world.getTotalWorldTime() % 2400 == 0) {
            AuraUtil.keepAlive(this, 3);
        }
        if (!world.isRemote && world.getTotalWorldTime() % 20 == 2 && world.isBlockIndirectlyGettingPowered(getPos()) == 0) {
            if (pumpPower <= 0) {
                int range = 3;
                List<EntityItem> nearbyItems = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range, range, range)));
                for (EntityItem entityItem : nearbyItems) {
                    ItemStack stack = entityItem.getEntityItem();
                    if (!entityItem.isDead && TileEntityFurnace.getItemBurnTime(stack) != 0) {
                        //Worth noting that the burn time should be 2* longer than a furnace

                        addFuel(Config.pumpCoalDuration * TileEntityFurnace.getItemBurnTime(stack) / 5, Config.pumpCoalSpeed);

                        //Kill the stack
                        if (stack.getCount() == 1) {
                            entityItem.setDead();
                        } else {
                            stack.shrink(1);
                        }
                        AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(1, entityItem.posX, entityItem.posY, entityItem.posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));
                        break;
                    }
                }
            }
        }
    }


}
