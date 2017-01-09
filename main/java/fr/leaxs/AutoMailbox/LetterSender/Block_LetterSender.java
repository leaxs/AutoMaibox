package fr.leaxs.AutoMailbox.LetterSender;

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
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.leaxs.AutoMailbox.AutoMailbox;

/**
 * The new name for the autoMailbox. It's sent letter with a command from a computer (CC).
 * All the function to process the letter is in "xxx.package".
 * 
 * @author leaxs
 */
public class Block_LetterSender extends BlockContainer
{
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public Block_LetterSender(Material material) 
	{
		super(material);
		setBlockName("LetterSender");
		setHardness(5.0f);
		setResistance(1.0f);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	//Display function
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int meta)
	{
		if(side == 0)
			return icons[0];
		else if (side == 1)
			return icons[1];
		else
			return icons[2];
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister icRegis) 
	{
		icons = new IIcon[3];
		icons[0] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "AMB_bottom");
		icons[1] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "LS_top");
		icons[2] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "LS_face");
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
	
	@Override
	public void onBlockPlacedBy(World world, int x,int y, int z, EntityLivingBase entity,ItemStack item)
	{
		TileEntity tels = world.getTileEntity(x, y, z);
		if(tels instanceof TileEntity_LetterSender)
		{
			if(entity instanceof EntityPlayer && !(entity instanceof FakePlayer))
				((TileEntity_LetterSender)tels).setName("{AMB}"+((EntityPlayer)entity).getDisplayName());
		}
		super.onBlockPlacedBy(world, x, y, z,entity, item);
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int face, final float par7, final float par8, final float par9)
	{
		if(player instanceof FakePlayer)
			return true;
		if(!world.isRemote)
		{
			FMLNetworkHandler.openGui(player, AutoMailbox.instance, 0, world, x, y, z);
			return true;
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int id) 
	{
		return new TileEntity_LetterSender();
	}
}
