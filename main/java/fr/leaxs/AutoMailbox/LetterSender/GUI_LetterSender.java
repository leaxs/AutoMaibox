package fr.leaxs.AutoMailbox.LetterSender;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;
import fr.leaxs.AutoMailbox.Packet.PacketHandler;
import fr.leaxs.AutoMailbox.Packet.PacketLetterSender;

@SideOnly(Side.CLIENT)
public class GUI_LetterSender extends GUIWithTabs
{
	protected TileEntity_LetterSender tels;
	private class GUITabLS extends GUITab
	{
		public GUITabLS(String name, int x, int y, int w, int h) 
		{
			super(name, x, y, w, h);
		}
		
		@Override
		public void drawBackground(final GUIWithTabs gui, final int x, final int y)
		{			
			for(int Y = 0; Y < 2; Y++)
			{
				for(int X = 0; X < 9; X++)
				{
					int i = X + Y*9;
					if(tels.getStackInSlot(i+12) == null && tels.getLockSlotItem()[i] != null)
					{
						drawItemStack(tels.getLockSlotItem()[i],gui.getLeft() + 8 + 18*X, gui.getTop()+73+18*Y,"0");
					}
				}
			}
			
		}

		@Override
		public void drawForeground(GUIWithTabs gui, int x, int y) {}
		
	}
	
	private final ResourceLocation texture = new ResourceLocation("automailbox", "textures/gui/LetterSender.png");
	
	public GUI_LetterSender(InventoryPlayer inventoryPlayer, TileEntity_LetterSender tels) 
	{
		super(new Container_LetterSender(inventoryPlayer, tels));
		xSize = 176;
		ySize = 212;
		this.tels = tels;
		tabs = new GUITab[]
		{
			new GUITabLS("Lock item", 158, 60, 7, 8),
			new GUITabLS("Unlock item", 158, 60, 7, 8)
			{
				@Override
				public void draw(GUIWithTabs gui, int srcX, int srcY) {}
			}
		};
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float t, int x, int y) 
	{
		GL11.glColor4f(1,1,1,1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		if(!tabs[activeTabId].inRect(this, x, y))
			tabs[activeTabId].draw(this, 176, 0);
		else
		{
			tabs[(activeTabId == 0 ? 1 : 0)].draw(this, 176, 0);
			drawTexturedModalRect(guiLeft+158,guiTop+60, 176, 8, 7, 8);
		}
		
		super.drawGuiContainerBackgroundLayer(t, x, y);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{
		drawString(fontRendererObj, "AMB : "+tels.getName().substring(5, tels.getName().length()), 8, 8, 0xacc5ff);
		if(tabs[activeTabId].inRect(this, x, y))
			tabs[(activeTabId == 0 ? 1 : 0)].drawString(this, x, y, tabs[(activeTabId == 0 ? 1 : 0)].getName());
		super.drawGuiContainerForegroundLayer(x, y);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int button) 
	{
		if(tabs[activeTabId].inRect(this, x, y))
		{
			activeTabId = (activeTabId == 0 ? 1 : 0);
			PacketHandler.INSTANCE.sendToServer(new PacketLetterSender(tels));
		}
		super.mouseClicked(x, y, button);
	}
}
