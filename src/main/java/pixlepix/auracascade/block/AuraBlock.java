package pixlepix.auracascade.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pixlepix.auracascade.AuraCascade;
import pixlepix.auracascade.block.tile.AuraTile;
import pixlepix.auracascade.block.tile.AuraTileBlack;
import pixlepix.auracascade.block.tile.AuraTileCapacitor;
import pixlepix.auracascade.block.tile.AuraTileConserve;
import pixlepix.auracascade.block.tile.AuraTileOrange;
import pixlepix.auracascade.block.tile.AuraTilePedestal;
import pixlepix.auracascade.block.tile.AuraTilePump;
import pixlepix.auracascade.block.tile.AuraTilePumpAlt;
import pixlepix.auracascade.block.tile.AuraTilePumpBase;
import pixlepix.auracascade.block.tile.AuraTilePumpCreative;
import pixlepix.auracascade.block.tile.AuraTilePumpFall;
import pixlepix.auracascade.block.tile.AuraTilePumpFallAlt;
import pixlepix.auracascade.block.tile.AuraTilePumpLight;
import pixlepix.auracascade.block.tile.AuraTilePumpLightAlt;
import pixlepix.auracascade.block.tile.AuraTilePumpProjectile;
import pixlepix.auracascade.block.tile.AuraTilePumpProjectileAlt;
import pixlepix.auracascade.block.tile.AuraTilePumpRedstone;
import pixlepix.auracascade.block.tile.AuraTilePumpRedstoneAlt;
import pixlepix.auracascade.block.tile.AuraTileRF;
import pixlepix.auracascade.block.tile.CraftingCenterTile;
import pixlepix.auracascade.data.AuraQuantity;
import pixlepix.auracascade.data.EnumAura;
import pixlepix.auracascade.data.IToolTip;
import pixlepix.auracascade.item.ItemAuraCrystal;
import pixlepix.auracascade.item.ItemMaterial;
import pixlepix.auracascade.main.AuraUtil;
import pixlepix.auracascade.main.Config;
import pixlepix.auracascade.main.EnumColor;
import pixlepix.auracascade.network.PacketBurst;
import pixlepix.auracascade.registry.BlockRegistry;
import pixlepix.auracascade.registry.CraftingBenchRecipe;
import pixlepix.auracascade.registry.ITTinkererBlock;
import pixlepix.auracascade.registry.ThaumicTinkererRecipe;

public class AuraBlock extends Block implements IToolTip, ITTinkererBlock, ITileEntityProvider {

    public static String name = "auraNode";
    //"" is default
    //"pump" is AuraTilePump\
    //"black" is AuraTileBlack etc
    String type;
    private static AxisAlignedBB AABB = new AxisAlignedBB(.25F, .25F, .25F, .75F, .75F, .75F);
    public AuraBlock(String type) {
        super(Material.glass);
        this.type = type;
        setLightOpacity(0);
        setHardness(2F);
    }

    public AuraBlock() {
        this("");
        setHardness(2F);
    }
    public static AuraBlock getBlockFromName(String name) {
        List<Block> blockList = BlockRegistry.getBlockFromClass(AuraBlock.class);
        if ("capacitor".equals(name)) {
            blockList = BlockRegistry.getBlockFromClass(AuraBlockCapacitor.class);
        }
        for (Block b : blockList) {
            if (((AuraBlock) b).type.equals(name)) {
                return (AuraBlock) b;
            }
        }
        return null;
    }

    public static ItemStack getAuraNodeItemstack() {
        List<Block> blockList = BlockRegistry.getBlockFromClass(AuraBlock.class);
        for (Block b : blockList) {
            if (((AuraBlock) b).type.equals("")) {
                return new ItemStack(b);
            }
        }
        AuraCascade.log.warn("Failed to find aura node itemstack. Something has gone horribly wrong");
        return null;
    }

    public static ItemStack getAuraNodePumpItemstack() {
        List<Block> blockList = BlockRegistry.getBlockFromClass(AuraBlock.class);
        for (Block b : blockList) {
            if (((AuraBlock) b).type.equals("pump")) {
                return new ItemStack(b);
            }
        }
        AuraCascade.log.warn("Failed to find aura node pump itemstack. Something has gone horribly wrong");
        return null;
    }

    //Prevents being moved by RIM
    @SuppressWarnings({})
    public static boolean _Immovable() {
        return true;
    }

    @Override
    public String getHarvestTool(IBlockState state) {
        return "pickaxe";
    }
    @Override
    public boolean isFullyOpaque(IBlockState state)
    {
        return false;
    }
    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return false;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof AuraTile) {
            int aura = ((AuraTile) tileEntity).storage.getTotalAura();
            return (int) Math.floor(Math.log10(aura));
        } else {
            return super.getComparatorInputOverride(blockState, worldIn, pos);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && world.getTileEntity(pos) instanceof AuraTile) {

            if (world.getTileEntity(pos) instanceof AuraTileCapacitor && player.isSneaking()) {
                AuraTileCapacitor capacitor = (AuraTileCapacitor) world.getTileEntity(pos);
                capacitor.storageValueIndex = (capacitor.storageValueIndex + 1) % capacitor.storageValues.length;
                player.addChatComponentMessage(new TextComponentString("Max Storage: " + capacitor.storageValues[capacitor.storageValueIndex]));
                world.markBlocksDirtyVertical(pos.getX(), pos.getZ(), pos.getX(), pos.getZ());
                return true;
            } else if (world.getTileEntity(pos) instanceof AuraTilePedestal && !player.isSneaking()) {
                AuraTilePedestal pedestal = (AuraTilePedestal) world.getTileEntity(pos);

                //Remove current itemstack from pedestal
                if (pedestal.itemStack != null) {
                    EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, pedestal.itemStack);
                    world.spawnEntityInWorld(item);
                }

                pedestal.itemStack = player.inventory.getCurrentItem() != null ? player.inventory.decrStackSize(player.inventory.currentItem, 1) : null;
                world.markBlocksDirtyVertical(pos.getX(), pos.getZ(), pos.getX(), pos.getZ());
                world.notifyBlockOfStateChange(pos, this);
                return true;

            } else if (world.getTileEntity(pos) instanceof AuraTile && player.inventory.getCurrentItem() == null) {
                player.addChatComponentMessage(new TextComponentString("Aura:"));
                for (EnumAura aura : EnumAura.values()) {
                    if (((AuraTile) world.getTileEntity(pos)).storage.get(aura) != 0) {
                        player.addChatComponentMessage(new TextComponentString(aura.name + " Aura: " + ((AuraTile) world.getTileEntity(pos)).storage.get(aura)));
                    }
                }

                if (world.getTileEntity(pos) instanceof AuraTilePumpBase) {

                    player.addChatComponentMessage(new TextComponentString("Power: " + ((AuraTilePumpBase) world.getTileEntity(pos)).pumpPower));
                }
                return true;
            }
        } else if (!world.isRemote && world.getTileEntity(pos) instanceof CraftingCenterTile && player.inventory.getCurrentItem() == null) {
            CraftingCenterTile tile = (CraftingCenterTile) world.getTileEntity(pos);
            if (tile.getRecipe() != null) {
                player.addChatComponentMessage(new TextComponentString(EnumColor.DARK_BLUE + "Making: " + tile.getRecipe().result.getDisplayName()));
                for (EnumFacing direction : CraftingCenterTile.pedestalRelativeLocations) {
                    AuraTilePedestal pedestal = ((AuraTilePedestal) world.getTileEntity(pos.offset(direction)));
                    if (tile.getRecipe() != null && tile.getRecipe().getAuraFromItem(pedestal.itemStack) != null) {
                        player.addChatComponentMessage(new TextComponentString("" + EnumColor.AQUA + pedestal.powerReceived + "/" + tile.getRecipe().getAuraFromItem(pedestal.itemStack).getNum() + " (" + tile.getRecipe().getAuraFromItem(pedestal.itemStack).getType().name + ")"));
                    } else {
                        AuraCascade.log.warn("Invalid recipe when checking crafting center");
                    }
                }
            } else {
            	player.addChatComponentMessage(new TextComponentString("No Recipe Selected"));
            }
            return true;
        }
        return true;
    }

    public boolean isCollidable()
    {
        return true;
    }
    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState blockState, Entity entity) {
        super.onEntityCollidedWithBlock(world, pos, this.getDefaultState(), entity);

        TileEntity te = world.getTileEntity(pos);
        if (entity instanceof EntityItem && !world.isRemote) {
            ItemStack stack = ((EntityItem) entity).getEntityItem();
            if (stack.getItem() instanceof ItemAuraCrystal) {
                if (te instanceof AuraTile) {
                    ((AuraTile) te).storage.add(new AuraQuantity(EnumAura.values()[stack.getItemDamage()], 1000 * stack.stackSize));
                    world.markBlocksDirtyVertical(pos.getX(), pos.getZ(), pos.getX(), pos.getZ());
                    world.notifyNeighborsOfStateChange(pos, this);
                    AuraCascade.proxy.networkWrapper.sendToAllAround(new PacketBurst(1, entity.posX, entity.posY, entity.posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 32));

                    entity.setDead();
                }

            }
        }
        if (!world.isRemote && te instanceof AuraTilePumpProjectile) {
            ((AuraTilePumpProjectile) te).onEntityCollidedWithBlock(entity);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        AuraUtil.updateMonitor(world, pos);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IInventory) {
            IInventory inv = (IInventory) te;
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                if (inv.getStackInSlot(i) != null) {
                    double d0 = AuraUtil.getDropOffset(world);
                    double d1 = AuraUtil.getDropOffset(world);
                    double d2 = AuraUtil.getDropOffset(world);
                    EntityItem entityitem = new EntityItem(world, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, inv.getStackInSlot(i));
                    AuraUtil.setItemDelay(entityitem, 10);
                    world.spawnEntityInWorld(entityitem);
                }
            }
        }
        super.breakBlock(world, pos, state);
        AuraUtil.updateMonitor(world, pos);
    }

    @Override
    public ThaumicTinkererRecipe getRecipeItem() {
        if (type.equals("pump")) {
            return new CraftingBenchRecipe(new ItemStack(this), "ILI", "INI", "ILI", 'I', new ItemStack(Items.iron_ingot), 'L', new ItemStack(Items.dye, 1, 4), 'N', getAuraNodeItemstack());
        }
        if (type.equals("black")) {
            return new CraftingBenchRecipe(new ItemStack(this), " I ", " N ", " I ", 'I', ItemMaterial.getIngot(EnumAura.BLACK_AURA), 'N', getAuraNodeItemstack());
        }
        if (type.equals("conserve")) {
            return new CraftingBenchRecipe(new ItemStack(this), "B", "N", "B", 'B', ItemMaterial.getIngot(EnumAura.BLUE_AURA), 'N', getAuraNodeItemstack());
        }
        if (type.equals("capacitor")) {
            return new CraftingBenchRecipe(new ItemStack(this), "Y", "N", "G", 'Y', ItemMaterial.getIngot(EnumAura.YELLOW_AURA), 'G', ItemMaterial.getIngot(EnumAura.GREEN_AURA), 'N', getAuraNodeItemstack());
        }

        if (type.equals("craftingPedestal")) {
            return new CraftingBenchRecipe(new ItemStack(this), "BBB", "BNB", "BBB", 'B', new ItemStack(Items.dye, 1, 4), 'N', getAuraNodeItemstack());
        }

        if (type.equals("craftingCenter")) {
            return new CraftingBenchRecipe(new ItemStack(this), "GGG", "RDR", "RRR", 'G', new ItemStack(Items.gold_ingot), 'R', ItemMaterial.getIngot(EnumAura.RED_AURA), 'D', new ItemStack(Items.diamond));
        }


        if (type.equals("orange")) {
            return new CraftingBenchRecipe(new ItemStack(this), " O ", " N ", " O ", 'O', ItemMaterial.getIngot(EnumAura.ORANGE_AURA), 'N', getAuraNodeItemstack());
        }

        if (type.equals("pumpProjectile")) {
            return new CraftingBenchRecipe(new ItemStack(this), "XXX", "GPG", 'X', new ItemStack(Items.arrow), 'G', ItemMaterial.getIngot(EnumAura.VIOLET_AURA), 'P', getAuraNodePumpItemstack());
        }
        if (type.equals("pumpFall")) {
            return new CraftingBenchRecipe(new ItemStack(this), "XXX", "GPG", 'X', new ItemStack(Items.water_bucket), 'G', ItemMaterial.getIngot(EnumAura.BLUE_AURA), 'P', getAuraNodePumpItemstack());
        }
        if (type.equals("pumpLight")) {
            return new CraftingBenchRecipe(new ItemStack(this), "XXX", "GPG", "G G", 'X', new ItemStack(Blocks.glowstone), 'G', ItemMaterial.getIngot(EnumAura.YELLOW_AURA), 'P', getAuraNodePumpItemstack());
        }
        if (type.equals("pumpRedstone")) {
            return new CraftingBenchRecipe(new ItemStack(this), "XXX", "GPG", "G G", 'X', new ItemStack(Blocks.redstone_block), 'G', ItemMaterial.getIngot(EnumAura.RED_AURA), 'P', getAuraNodePumpItemstack());
        }
        if (type.equals("pumpAlt")) {
            return new CraftingBenchRecipe(new ItemStack(this), " E ", "EPE", " E ", 'P', new ItemStack(getBlockFromName("pump")), 'E', Items.redstone);
        }
        if (type.equals("pumpRedstoneAlt")) {
            return new CraftingBenchRecipe(new ItemStack(this), " E ", "EPE", " E ", 'P', new ItemStack(getBlockFromName("pumpRedstone")), 'E', Items.redstone);
        }
        if (type.equals("pumpLightAlt")) {
            return new CraftingBenchRecipe(new ItemStack(this), " E ", "EPE", " E ", 'P', new ItemStack(getBlockFromName("pumpLight")), 'E', Items.redstone);
        }
        if (type.equals("pumpFallAlt")) {
            return new CraftingBenchRecipe(new ItemStack(this), " E ", "EPE", " E ", 'P', new ItemStack(getBlockFromName("pumpFall")), 'E', Items.redstone);
        }
        if (type.equals("pumpProjectileAlt")) {
            return new CraftingBenchRecipe(new ItemStack(this), " E ", "EPE", " E ", 'P', new ItemStack(getBlockFromName("pumpProjectile")), 'E', Items.redstone);
        }
        if (type.equals("flux")) {
            return new CraftingBenchRecipe(new ItemStack(this), "RNR", 'R', ItemMaterial.getIngot(EnumAura.RED_AURA), 'N', getAuraNodeItemstack());
        }
        if (type.equals("pumpCreative")) {
            return null;

        }
        return new CraftingBenchRecipe(new ItemStack(this, 4), "PPP", "PRP", "PPP", 'P', new ItemStack(Items.gold_nugget), 'R', new ItemStack(Items.redstone));
    }

    @Override
    public int getCreativeTabPriority() {
        if (type.equals("pump") || type.equals("") || type.equals("flux")) {
            return 100;
        }
        if (type.contains("Alt")) {
            return -25;
        }

        if (type.contains("pump")) {
            return 75;
        }
        return 50;
    }

    @Override
    public ArrayList<Object> getSpecialParameters() {
    	//This was typed to Object from no-type.
        ArrayList<Object> result = new ArrayList<Object>();
        result.add("pump");
        result.add("black");
        result.add("conserve");

        result.add("craftingCenter");

        result.add("craftingPedestal");
        result.add("orange");

        result.add("pumpAlt");

        result.add("pumpProjectile");
        result.add("pumpFall");
        result.add("pumpLight");
        result.add("pumpRedstone");
        result.add("pumpProjectileAlt");
        result.add("pumpFallAlt");
        result.add("pumpCreative");
        result.add("pumpLightAlt");
        result.add("pumpRedstoneAlt");


        if (ModAPIManager.INSTANCE.hasAPI("CoFHAPI|energy")) {
            result.add("flux");
        }
        return result;
    }

    @Override
    public String getBlockName() {
        return name + type;
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
    public Class<? extends ItemBlock> getItemBlock() {
        return null;
    }


    @Override
    public Class<? extends TileEntity> getTileEntity() {

        if (type.equals("pump")) {
            return AuraTilePump.class;
        }
        if (type.equals("black")) {
            return AuraTileBlack.class;
        }
        if (type.equals("conserve")) {
            return AuraTileConserve.class;
        }
        if (type.equals("capacitor")) {
            return AuraTileCapacitor.class;
        }

        if (type.equals("craftingPedestal")) {
            return AuraTilePedestal.class;
        }

        if (type.equals("craftingCenter")) {
            return CraftingCenterTile.class;
        }
        if (type.equals("flux")) {
            return AuraTileRF.class;

        }
        if (type.equals("pumpCreative")) {
            return AuraTilePumpCreative.class;
        }


        if (type.equals("orange")) {
            return AuraTileOrange.class;
        }
        if (type.equals("pumpProjectile")) {
            return AuraTilePumpProjectile.class;
        }
        if (type.equals("pumpFall")) {
            return AuraTilePumpFall.class;
        }
        if (type.equals("pumpCreative")) {
            return AuraTilePumpFall.class;
        }
        if (type.equals("pumpLight")) {
            return AuraTilePumpLight.class;
        }
        if (type.equals("pumpRedstone")) {
            return AuraTilePumpRedstone.class;
        }
        if (type.equals("pumpAlt")) {
            return AuraTilePumpAlt.class;
        }
        if (type.equals("pumpProjectileAlt")) {
            return AuraTilePumpProjectileAlt.class;
        }
        if (type.equals("pumpFallAlt")) {
            return AuraTilePumpFallAlt.class;
        }
        if (type.equals("pumpLightAlt")) {
            return AuraTilePumpLightAlt.class;
        }
        if (type.equals("pumpRedstoneAlt")) {
            return AuraTilePumpRedstoneAlt.class;
        }
        return AuraTile.class;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        try {
            return getTileEntity().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public List<String> getTooltipData(World world, EntityPlayer player, BlockPos pos) {
        List<String> result = new ArrayList<String>();
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof AuraTile) {

            if (tileEntity instanceof AuraTileCapacitor) {
                AuraTileCapacitor capacitor = (AuraTileCapacitor) tileEntity;
                result.add("Max Storage: " + capacitor.storageValues[capacitor.storageValueIndex]);

            }
            if (((AuraTile) tileEntity).storage.getTotalAura() > 0) {
                result.add("Aura Stored: ");
                for (EnumAura aura : EnumAura.values()) {
                    if (((AuraTile) tileEntity).storage.get(aura) != 0) {
                        result.add("    " + aura.name + " Aura: " + ((AuraTile) tileEntity).storage.get(aura));
                    }
                }
            } else {
                result.add("No Aura");
            }
            if (tileEntity instanceof AuraTileCapacitor) {
                if (((AuraTileCapacitor) tileEntity).ticksDisabled > 0) {
                    result.add("Time until functional: " + ((AuraTileCapacitor) tileEntity).ticksDisabled / 20);
                }
            }
            if (tileEntity instanceof AuraTilePumpBase) {
                result.add("Time left: " + ((AuraTilePumpBase) tileEntity).pumpPower + " seconds");
                result.add("Power: " + ((AuraTilePumpBase) tileEntity).pumpSpeed + " power per second");
                if (((AuraTilePumpBase) tileEntity).isAlternator()) {
                    AuraTilePumpBase altPump = (AuraTilePumpBase) tileEntity;
                    int power = (int) (altPump.pumpSpeed * altPump.getAlternatingFactor());
                    result.add("Phase Power: " + power);
                }
            }

            if (ModAPIManager.INSTANCE.hasAPI("CoFHAPI|energy")) {
                if (tileEntity instanceof AuraTileRF) {
                    AuraTileRF auraTileRF = (AuraTileRF) tileEntity;
                    result.add("RF/t Output: " + auraTileRF.lastPower * Config.powerFactor);
                }

            }

        } else if (tileEntity instanceof CraftingCenterTile) {
            CraftingCenterTile tile = (CraftingCenterTile) tileEntity;
            if (tile.getRecipe() != null) {
                result.add("Making: " + tile.getRecipe().result.getDisplayName());
                for (EnumFacing direction : CraftingCenterTile.pedestalRelativeLocations) {
                    AuraTilePedestal pedestal = (AuraTilePedestal) world.getTileEntity(pos.offset(direction));
                    if (tile.getRecipe() != null && tile.getRecipe().getAuraFromItem(pedestal.itemStack) != null) {
                        result.add("    Power received:" + pedestal.powerReceived + "/" + tile.getRecipe().getAuraFromItem(pedestal.itemStack).getNum() + " (" + tile.getRecipe().getAuraFromItem(pedestal.itemStack).getType().name + ")");
                    } else {
                        AuraCascade.log.warn("Invalid recipe when checking crafting center");
                    }
                }
            } else {
                result.add("No Valid Recipe Selected");
            }

        }
        return result;
    }
    @Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB;
	}
}

