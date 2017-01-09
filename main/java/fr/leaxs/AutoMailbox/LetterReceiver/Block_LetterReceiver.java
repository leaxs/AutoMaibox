package fr.leaxs.AutoMailbox.LetterReceiver;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.leaxs.AutoMailbox.AutoMailbox;
import fr.leaxs.AutoMailbox.LetterSender.Block_LetterSender;

/**
 * The opposite of the {@link Block_LetterSender.java letterSender}, it can process incoming letter for
 * the owner without he have to do something.
 *	@author leaxs
 */

public class Block_LetterReceiver extends BlockContainer
{
	private IIcon[] icons;

	public Block_LetterReceiver(Material material) 
	{
		super(material);
		setBlockName("LetterReceiver");
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
		icons[1] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "LR_top");
		icons[2] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "LR_face");
		System.out.println(icons[0].getIconHeight());
	}
	
	@Override
	public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int face, final float par7, final float par8, final float par9)
	{
		if(player instanceof FakePlayer)
			return true;
		if(!world.isRemote)
		{
			FMLNetworkHandler.openGui(player, AutoMailbox.instance, 2, world, x, y, z);
			return true;
		}
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x,int y, int z, EntityLivingBase entity,ItemStack item)
	{
		TileEntity tels = world.getTileEntity(x, y, z);
		if(tels instanceof TileEntity_LetterReceiver)
		{
			if(entity instanceof EntityPlayer && !(entity instanceof FakePlayer))
				((TileEntity_LetterReceiver)tels).setOwner(((EntityPlayer)entity).getGameProfile());
		}
		super.onBlockPlacedBy(world, x, y, z,entity, item);
	}

	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) 
	{
		return new TileEntity_LetterReceiver();
	}
	
}
