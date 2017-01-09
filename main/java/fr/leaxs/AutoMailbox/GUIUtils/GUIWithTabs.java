package fr.leaxs.AutoMailbox.GUIUtils;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

public abstract class GUIWithTabs extends GuiContainer
{
	protected int activeTabId = 0;
	protected GUITab[] tabs;

	public GUIWithTabs(Container container) 
	{
		super(container);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float t,int x, int y) 
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if(tabs != null)
			tabs[activeTabId].drawBackground(this, x, y);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int x,int y) 
	{
		if(tabs != null)
			tabs[activeTabId].drawForeground(this, x, x);
	}
	
	// Inform active tab of mouse event
	@Override
	protected void mouseClicked(final int x, final int y, final int button)
	{
		if(tabs != null)
			tabs[activeTabId].mouseClick(this, x, y, button);
		super.mouseClicked(x, y, button);
	}
	
	@Override
	protected void mouseClickMove(final int x, final int y, final int button, final long timeSinceClicked)
	{
		if(tabs != null)
			tabs[activeTabId].mouseMoveClick(this, x, y, button,timeSinceClicked);
		super.mouseClickMove(x, y, button, timeSinceClicked);
	}
	
	// public access to the display function
	public int getLeft()
	{
		return guiLeft;
	}
	
	public int getTop()
	{
		return guiTop;
	}

	protected void drawHoverString(final List<String> lst, final int x, final int y)
	{
		drawHoveringText(lst, x, y, fontRendererObj);
	}
	
	public void drawString(String str, int x, int y, int color) 
	{
		super.drawString(fontRendererObj, str, x, y, color);
	}
	
	public void drawSplitString(final String string, final int x, final int y, final int weight, final int color)
	{
		fontRendererObj.drawSplitString(string, x, y, weight, color);
	}

	public void drawItemStack(final ItemStack item, final int x, final int y, final String text)
	{
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		final RenderItem itemRender = new RenderItem();
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		itemRender.zLevel = 200.0F;
		FontRenderer font = null;
		if (item != null) font = item.getItem().getFontRenderer(item);
		if (font == null) font = fontRendererObj;
		itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), item, x, y);
		itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), item, x, y, text);
		itemRender.zLevel = 0.0F;
		RenderHelper.disableStandardItemLighting();
		GL11.glColor4f(1,1,1,1);
	}

}
