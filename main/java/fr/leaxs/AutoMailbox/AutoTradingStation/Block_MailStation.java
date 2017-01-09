package fr.leaxs.AutoMailbox.AutoTradingStation;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.leaxs.AutoMailbox.AutoMailbox;
import fr.leaxs.AutoMailbox.LetterSender.Block_LetterSender;

/**
 * This block combines the advanced Printer, {@link Block_LetterSender.java letterSender} and the {@link Block_LetterSender.java Auto-Receiver}.
 * It's controlled also by a computer from CC.
 *	@author leaxs
 */
public class Block_MailStation extends BlockContainer
{
	private IIcon[] icons;

	public Block_MailStation(Material material) 
	{
		super(material);
		setBlockName("MailStation");
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
		icons[1] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "AMB_bottom");
		icons[2] = 	icRegis.registerIcon(AutoMailbox.MODID.toLowerCase() + ":" + "LS_face");
		System.out.println(icons[0].getIconHeight());
	}
	
	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) 
	{
		return new TileEntity_MailStation();
	}
}
