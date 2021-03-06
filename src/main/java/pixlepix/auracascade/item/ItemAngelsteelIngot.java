package pixlepix.auracascade.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.network.PacketBurst;
import pixlepix.auracascade.registry.ISpecialCreativeSort;
import pixlepix.auracascade.registry.ITTinkererItem;
import pixlepix.auracascade.registry.ThaumicTinkererRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pixlepix on 12/21/14.
 */
public class ItemAngelsteelIngot extends Item implements ITTinkererItem, ISpecialCreativeSort {
    public static final String name = "ingotAngelSteel";

    @Override
    public ArrayList<Object> getSpecialParameters() {
        return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return super.getItemStackDisplayName(stack).replace("%n", I18n.translateToLocal(stack.getItemDamage() + ".aurasteel.name"));
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (entityItem.world.getTotalWorldTime() % 4 == 0 && entityItem.onGround && !entityItem.world.isRemote && entityItem.getEntityItem().getItemDamage() < AngelsteelToolHelper.MAX_DEGREE - 1) {
            EntityItem[] targetStacks = new EntityItem[3];
            targetStacks[0] = entityItem;
            int i = 1;

            if (entityItem.getEntityItem().getCount() == 2) {
                targetStacks[1] = entityItem;
                i = 2;
            } else if (entityItem.getEntityItem().getCount() >= 3) {
                targetStacks[1] = entityItem;
                targetStacks[2] = entityItem;
                i = 3;
            }

            int degree = entityItem.getEntityItem().getItemDamage();

            if (i != 3) {
                AxisAlignedBB range = new AxisAlignedBB(entityItem.posX - 3, entityItem.posY - 3, entityItem.posZ - 3, entityItem.posX + 3, entityItem.posY + 3, entityItem.posZ + 3);
                List<EntityItem> entityItems = entityItem.world.getEntitiesWithinAABB(EntityItem.class, range);
                for (EntityItem nearbyItem : entityItems) {
                    ItemStack nearbyStack = nearbyItem.getEntityItem();
                    if (nearbyItem != entityItem && nearbyStack.getItem() == this && nearbyStack.getItemDamage() == degree) {

                        targetStacks[i] = nearbyItem;
                        i += 1;

                        if (nearbyStack.getCount() >= 2 && i < 3) {
                            targetStacks[i] = nearbyItem;
                            i += 1;
                        }

                        if (i == 3) {
                            break;

                        }

                    }
                }
            }

            if (i == 3) {
                for (EntityItem item : targetStacks) {
                    item.getEntityItem().shrink(1);
                }
                EntityItem item = new EntityItem(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, new ItemStack(this, 1, degree + 1));
                entityItem.world.spawnEntity(item);
                AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(1, item.posX, item.posY, item.posZ), new NetworkRegistry.TargetPoint(item.world.provider.getDimension(), item.posX, item.posY, item.posZ, 32));
            }
        }
        return false;
    }

    @Override
    public String getItemName() {
        return name;
    }

    @Override
    public boolean shouldRegister() {
        return true;
    }

    @Override
    public boolean shouldDisplayInTab() {
        return true;
    }

    @Override
    public ThaumicTinkererRecipe getRecipeItem() {
        return null;
    }

    @Override
    public int getCreativeTabPriority() {
        return -5;
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        for (int i = 0; i < AngelsteelToolHelper.MAX_DEGREE; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public int compare(ItemStack stack, ItemStack otherStack) {
        if (otherStack.getItem() instanceof ItemAngelsteelIngot) {
            return (stack.getItemDamage() - otherStack.getItemDamage());
        }
        return stack.getDisplayName().compareToIgnoreCase(otherStack.getDisplayName());
    }
}
