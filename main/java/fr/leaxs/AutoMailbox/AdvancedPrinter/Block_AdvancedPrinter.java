package fr.leaxs.AutoMailbox.AdvancedPrinter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.leaxs.AutoMailbox.AutoMailbox;

/**
 * This block is an amelioration of the CC printer, it allows to print a page with more as one color
 * 
 *	@author leaxs
 */
public class Block_AdvancedPrinter extends BlockContainer
{
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public Block_AdvancedPrinter(Material material) 
	{
		super(material);
		setBlockName("AdvancedPrinter");
		setHardness(5.0f);
		setResistance(1.0f);
		setCreativeTab(CreativeTabs.tabRedstone);
	}
	//Display function
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int metadata)
	{
		if(side <= 1)
			return icons[2];
		if((metadata == 2 && side == 2)||(metadata == 3 && side == 5)||(metadata == 0 && side == 3)||(metadata == 1 && side == 4))
			return this.icons[0];
		else 
			return this.icons[1];
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister icRegis) 
	{
		icons = new IIcon[3];
		icons[0] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "ADVPrinter_front");
		icons[1] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "ADVPrinter_face");
		icons[2] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "ADVPrinter_top");
		System.out.println(icons[0].getIconHeight());
	}

	@Override
	public void breakBlock(World world, int x, int y,int z, Block block, int meta)	
	{
		final TileEntity te = world.getTileEntity(x, y, z);
		if(te != null && te instanceof IInventory)
		{
			final IInventory inventory = (IInventory)te;
			for(int i = 0; i < inventory.getSizeInventory(); i++)
			{
				final ItemStack stack = inventory.getStackInSlot(i);
				if(stack != null)
				{
					final float spawnX = x + world.rand.nextFloat();
					final float spawnY = y + world.rand.nextFloat();
					final float spawnZ = z + world.rand.nextFloat();

					final EntityItem itemDropped = new EntityItem(world, spawnX, spawnY, spawnZ, stack);

					itemDropped.motionX = (-0.5F + world.rand.nextFloat())*0.05F;
					itemDropped.motionY = (4 + world.rand.nextFloat())*0.05F;
					itemDropped.motionZ = (-0.5F + world.rand.nextFloat())*0.05F;

					world.spawnEntityInWorld(itemDropped);
				}
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack item)
	{
        int direction = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 2.5D) & 3;
        world.setBlockMetadataWithNotify(x, y, z, direction, 2);
	}

	@Override
	public boolean onBlockActivated(World world, int x,int y, int z, EntityPlayer player, int face, float par7, float par8,float par9) 
	{
		if(player instanceof FakePlayer)
			return true;
		if(!world.isRemote)
		{
			FMLNetworkHandler.openGui(player, AutoMailbox.instance, 1, world, x, y, z);
			return true;
		}
		return true;
	}
	@Override
	public TileEntity createNewTileEntity(World world, int id) 
	{
		return new TileEntity_AdvancedPrinter();
	}

}
