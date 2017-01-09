package fr.leaxs.AutoMailbox.LetterReceiver;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;
import fr.leaxs.AutoMailbox.LetterReceiver.Tabs.Tab_LetterOverview;
import fr.leaxs.AutoMailbox.LetterReceiver.Tabs.Tab_LetterViewer;
import fr.leaxs.AutoMailbox.LetterReceiver.Tabs.Tab_letterHistoy;
import fr.leaxs.AutoMailbox.Packet.PacketHandler;
import fr.leaxs.AutoMailbox.Packet.PacketLetterReceiver;

public class GUI_LetterReceiver extends GUIWithTabs
{
	private final ResourceLocation texture = new ResourceLocation("automailbox", "textures/gui/LetterReceiver.png");
	private TileEntity_LetterReceiver telr;
	
	public GUI_LetterReceiver(InventoryPlayer inventoryPlayer, TileEntity_LetterReceiver telr) 
	{
		//Never active, always active, active with redstone
		super(new Container_LetterReceiver(inventoryPlayer, telr));
		xSize = 226;
		ySize = 231;
		this.telr = telr;
		PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,1,0));
		tabs = new GUITab[] 
		{
			new Tab_LetterViewer(167, 132, 15, 13, telr),
			new Tab_LetterOverview(187, 132, 15, 13),
			new Tab_letterHistoy(207, 132, 15, 13, telr)
		};
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float t, int x, int y) 
	{
		GL11.glColor4f(1,1,1,1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		for(int i=0;i<tabs.length;i++)
		{
			if(i==activeTabId)
				continue;
			if(tabs[i].inRect(this, x, y))
			{
				drawRect(guiLeft+167+20*i, guiTop+132, guiLeft+182+20*i, guiTop+145, 0xFF6A90E2);
				GL11.glColor4f(1,1,1,1);
				break;
			}
		}
		for(int i=0;i<tabs.length;i++)
		{
			if(i == activeTabId)
			{
				drawTexturedModalRect(guiLeft+165+20*i, guiTop+129, 165, 132, 19, 4);
				drawRect(guiLeft+167+20*i, guiTop+129, guiLeft+182+20*i, guiTop+145, 0xFF476099);
				GL11.glColor4f(1,1,1,1);
				drawTexturedModalRect(guiLeft+166+20*i, guiTop+131, 238, 20+12*i, 16, 12);
			}
			else
				drawTexturedModalRect(guiLeft+166+20*i, guiTop+133, 238, 20+12*i, 16, 12);
		}
		super.drawGuiContainerBackgroundLayer(t, x, y);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
	{
		super.drawGuiContainerForegroundLayer(x, y);
		for(GUITab tab : tabs)
			tab.drawString(this, x, y, tab.getName());
		
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button) 
	{
		for(int i=0;i<tabs.length;i++)
		{
			if(tabs[i].inRect(this, x, y))
			{
				activeTabId = i;
				((Container_LetterReceiver) inventorySlots).changeTab(activeTabId);
				PacketHandler.INSTANCE.sendToServer(new PacketLetterReceiver(telr,0,activeTabId));
				break;
			}
		}
		super.mouseClicked(x, y, button);
	}
	
	@Override
	public void onGuiClosed() 
	{
		telr.changeViewedLetter(-1);
		super.onGuiClosed();
	}
}
