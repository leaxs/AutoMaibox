package fr.leaxs.AutoMailbox.LetterReceiver.Tabs;

import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;
import fr.leaxs.AutoMailbox.GUIUtils.GuiRectangle;
import fr.leaxs.AutoMailbox.LetterReceiver.TileEntity_LetterReceiver;
import fr.leaxs.AutoMailbox.Packet.PacketHandler;
import fr.leaxs.AutoMailbox.Packet.PacketLetterReceiver;

public class Tab_letterHistoy extends GUITab
{
	private int index = 0;
	private TileEntity_LetterReceiver telr;
	private GuiRectangle[] buttons;

	public Tab_letterHistoy(int x, int y, int w, int h, TileEntity_LetterReceiver telr) 
	{
		super("History", x, y, w, h);
		this.telr = telr;
		buttons = new GuiRectangle[]
				{
				new GuiRectangle(6, 121, 10, 10),
				new GuiRectangle(17, 121, 10, 10),
				new GuiRectangle(28, 121, 10, 10)
				};
	}
	@Override
	public void drawBackground(GUIWithTabs gui, int x, int y) 
	{
		Gui.drawRect(gui.getLeft()+5, gui.getTop()+5, gui.getLeft()+221, gui.getTop()+23, 0xFF476099);
		GL11.glColor4f(1,1,1,1);
		//From textField
		gui.drawTexturedModalRect(gui.getLeft()+41, gui.getTop()+9, 5, 26, 100, 13);
		gui.drawTexturedModalRect(gui.getLeft()+42, gui.getTop()+10, 43, 107, 99, 12);

		//Letter number textField
		gui.drawTexturedModalRect(gui.getLeft()+187, gui.getTop()+5, 5, 26, 34, 12);
		gui.drawTexturedModalRect(gui.getLeft()+188, gui.getTop()+6, 109, 108, 33, 13);

		Gui.drawRect(gui.getLeft()+204, gui.getTop()+110, gui.getLeft()+222, gui.getTop()+128, 0xFF476099);
		GL11.glColor4f(1,1,1,1);

		if(telr.getLetterHistoryViewed(index)!=null)
		{
			for(int i=0;i<4;i++)
			{
				for(int j=0;j<4;j++)
				{
					ItemStack item = telr.getLetterHistoryViewed(index).getAttachments()[j+i*4];
					if(item != null)
						gui.drawItemStack(item,gui.getLeft()+150+18*i,gui.getTop()+30+18*j,""+item.stackSize);
				}	
			}
			ItemStack item = telr.getLetterHistoryViewed(index).getAttachments()[16];
			if(item != null)
				gui.drawItemStack(item,gui.getLeft()+150,gui.getTop()+102,""+item.stackSize);
			item = telr.getLetterHistoryViewed(index).getAttachments()[17];
			if(item != null)
				gui.drawItemStack(item,gui.getLeft()+168,gui.getTop()+102,""+item.stackSize);
		}
		for(int i=0;i<buttons.length;i++)
		{
			if((i==0 && index == 0)||(i==1 && telr.getHistorySize()==0)||(i==2 && index >= telr.getHistorySize()))
				continue;
			if(buttons[i].inRect(gui, x, y))
			{
				int xPos = gui.getLeft()+buttons[i].getX();
				int yPos = gui.getTop()+buttons[i].getY();
				Gui.drawRect(xPos, yPos, xPos+buttons[i].getWidth(),yPos+buttons[i].getHeight(), 0x33FFFFFF);
				return;
			}
		}
	}

	@Override
	public void drawForeground(GUIWithTabs gui, int x, int y) 
	{
		String sender = "";
		String text = "";
		if(telr.getLetterHistoryViewed(index)!=null)
		{
			sender = telr.getLetterHistoryViewed(index).getSender().getName();
			text = telr.getLetterHistoryViewed(index).getText();
		}
		gui.drawString("From : "+sender, 9, 11, 0xFFFFFFFF);
		gui.drawSplitString(text, 8, 29, 133, 0xFFFFFFFF);
		int showIndex = telr.getHistorySize()>0?1+index:index;
		gui.drawString((showIndex)+"/"+telr.getHistorySize(), 189, 7, 0xFFFFFFFF);
	}

	@Override
	public void mouseClick(GUIWithTabs gui, int x, int y, int button) 
	{
		for(int i=0;i<buttons.length;i++)
		{
			if((i==0 && index <= 0)||(i==2 && index+1 >= telr.getHistorySize()))
				continue;
			if(buttons[i].inRect(gui, x, y))
			{
				switch(i)
				{
				case 0:
					if(index>=1)
						index--;
					return;
				case 1:
					PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,5,index));
					telr.removeHistoryIndex(index);
					if(index>=1)
						index--;
					return;
				case 2:
					if(index<telr.getHistorySize())
						index++;
					return;
				}
			}
		}
		super.mouseClick(gui, x, y, button);
	}

}
