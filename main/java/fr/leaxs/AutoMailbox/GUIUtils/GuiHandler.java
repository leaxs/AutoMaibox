package fr.leaxs.AutoMailbox.GUIUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import fr.leaxs.AutoMailbox.AutoMailbox;
import fr.leaxs.AutoMailbox.AdvancedPrinter.Container_AdvancedPrinter;
import fr.leaxs.AutoMailbox.AdvancedPrinter.GUI_AdvancedPrinter;
import fr.leaxs.AutoMailbox.AdvancedPrinter.TileEntity_AdvancedPrinter;
import fr.leaxs.AutoMailbox.LetterReceiver.Container_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.GUI_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.TileEntity_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterSender.Container_LetterSender;
import fr.leaxs.AutoMailbox.LetterSender.GUI_LetterSender;
import fr.leaxs.AutoMailbox.LetterSender.TileEntity_LetterSender;

public class GuiHandler implements IGuiHandler 
{
	public GuiHandler()
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(AutoMailbox.instance, this);
	}

	@Override
	public Object getClientGuiElement(final int ID, final EntityPlayer player, final World world,	final int x, final int y, final int z)
	{
		final TileEntity te = world.getTileEntity(x,y,z);
		switch(ID)
		{
		case 0:
			if(te != null && te instanceof TileEntity_LetterSender)
				return new GUI_LetterSender(player.inventory,(TileEntity_LetterSender)te);
			break;
		case 1:
			if(te != null && te instanceof TileEntity_AdvancedPrinter)
				return new GUI_AdvancedPrinter(player.inventory,(TileEntity_AdvancedPrinter)te);
			break;
		case 2:
			if(te != null && te instanceof TileEntity_LetterReceiver)
				return new GUI_LetterReceiver(player.inventory,(TileEntity_LetterReceiver)te);
			break;
		}
		return null;
	}

	@Override
	public Object getServerGuiElement(final int ID, final EntityPlayer player, final World world,final int x, final int y, final int z)
	{
		final TileEntity te = world.getTileEntity(x,y,z);
		switch(ID)
		{
		case 0:
			if(te != null && te instanceof TileEntity_LetterSender)
				return new Container_LetterSender(player.inventory,(TileEntity_LetterSender)te);
			break;
		case 1:
			if(te != null && te instanceof TileEntity_AdvancedPrinter)
				return new Container_AdvancedPrinter(player.inventory,(TileEntity_AdvancedPrinter)te);
			break;
		case 2:
			if(te != null && te instanceof TileEntity_LetterReceiver)
				return new Container_LetterReceiver(player.inventory,(TileEntity_LetterReceiver)te);
			break;
		}
		return null;
	}

}
