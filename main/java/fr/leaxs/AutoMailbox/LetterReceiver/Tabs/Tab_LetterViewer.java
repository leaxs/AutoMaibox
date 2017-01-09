package fr.leaxs.AutoMailbox.LetterReceiver.Tabs;

import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;
import fr.leaxs.AutoMailbox.GUIUtils.GuiRectangle;
import fr.leaxs.AutoMailbox.LetterReceiver.Container_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.GUI_LetterReceiver;
import fr.leaxs.AutoMailbox.LetterReceiver.TileEntity_LetterReceiver;
import fr.leaxs.AutoMailbox.Packet.PacketHandler;
import fr.leaxs.AutoMailbox.Packet.PacketLetterReceiver;

public class Tab_LetterViewer extends GUITab
{
	private TileEntity_LetterReceiver telr;
	private int index = 0;
	private GuiRectangle[] buttons;

	public Tab_LetterViewer(int x, int y, int w, int h, TileEntity_LetterReceiver telr) 
	{
		super("Letter viewer", x, y, w, h);
		this.telr = telr;
		buttons = new GuiRectangle[]
				{
				new GuiRectangle(6, 121, 10, 10),
				new GuiRectangle(17, 121, 10, 10),
				new GuiRectangle(28, 121, 10, 10),
				new GuiRectangle(204,110,18,18)
				};
	}

	@Override
	public void drawBackground(GUIWithTabs gui, int x, int y) 
	{
		for(int i = 0;i<=((int)index/12);i++)
			Gui.drawRect(gui.getLeft()+3, gui.getTop()+6+i*2,gui.getLeft()+5, gui.getTop()+7+i*2, 0xFF000000);
		GL11.glColor4f(1,1,1,1);
		gui.drawTexturedModalRect(gui.getLeft()+4+18*(index%12), gui.getTop()+4, 226, 0, 20, 20);
		gui.drawTexturedModalRect(gui.getLeft()+207, gui.getTop()+113, 226, 20+12*telr.getMode(), 12, 12);
		for(int i=0;i<buttons.length;i++)
		{
			if((i==0 && index == 0)||(i==2 && index == telr.getMailBoxInventory().getSizeInventory()))
				continue;
			if(buttons[i].inRect(gui, x, y))
			{
				int xPos = gui.getLeft()+buttons[i].getX();
				int yPos = gui.getTop()+buttons[i].getY();
				if(i == 3)
				{
					switch(telr.getMode())
					{
					case 0:
						buttons[3].drawString(gui,x, y, "Never Process");
						break;
					case 1:
						buttons[3].drawString(gui, x, y, "Always Process");
						break;
					case 2:
						buttons[3].drawString(gui, x, y, "Active with redstone signal");
						break;
					}
				}
				Gui.drawRect(xPos, yPos, xPos+buttons[i].getWidth(),yPos+buttons[i].getHeight(), 0x33FFFFFF);
				return;
			}
		}

	}

	@Override
	public void drawForeground(GUIWithTabs gui, int x, int y) 
	{
		gui.drawSplitString(telr.getMessage(), 8, 29, 133, 0xFFFFFFFF);		
		if(buttons[3].inRect(gui, x, y))
		{
			switch(telr.getMode())
			{
			case 0:
				buttons[3].drawString(gui, x, y, "Never Process");
				break;
			case 1:
				buttons[3].drawString(gui, x, y, "Always Process");
				break;
			case 2:
				buttons[3].drawString(gui, x, y, "Active with redstone signal");
				break;
			}
		}
	}

	@Override
	public void mouseClick(GUIWithTabs gui, int x, int y, int button) 
	{
		for(int i=0;i<buttons.length;i++)
		{
			if((i==0 && index <= 0)||(i==2 && index+1 >= telr.getMailBoxInventory().getSizeInventory()))
				continue;
			if(buttons[i].inRect(gui, x, y))
			{
				switch(i)
				{
				case 0:
					index--;
					((Container_LetterReceiver)((GUI_LetterReceiver)gui).inventorySlots).updateLetterUpBar(index/12);
					PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,1,index));
					telr.changeViewedLetter(index);
					return;
				case 1:
					PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,2,0));
					((Container_LetterReceiver)((GUI_LetterReceiver)gui).inventorySlots).emptyingLetter();
					return;
				case 2:
					index++;
					((Container_LetterReceiver)((GUI_LetterReceiver)gui).inventorySlots).updateLetterUpBar(index/12);
					PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,1,index));
					telr.changeViewedLetter(index);
					return;
				case 3:
					telr.changeMode();
					PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,3,0));
					return;

				}
			}
		}

		super.mouseClick(gui, x, y, button);
	}

}
