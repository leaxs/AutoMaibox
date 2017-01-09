package fr.leaxs.AutoMailbox.GUIUtils;

import java.util.Arrays;


public class GuiRectangle 
{
	private final int h;
	private final int w;
	private int x;
	private int y;

	public GuiRectangle(final int x, final int y, final int w, final int h) 
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void draw(final GUIWithTabs gui, final int srcX, final int srcY) 
	{
		gui.drawTexturedModalRect(gui.getLeft() + x, gui.getTop() + y, srcX, srcY, w, h);
	}

	public void drawString(final GUIWithTabs gui, final int mouseX, final int mouseY, final String str)
	{
		if (inRect(gui, mouseX, mouseY))
			gui.drawHoverString(Arrays.asList(str.split("\n")), mouseX - gui.getLeft(), mouseY - gui.getTop());
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}
	
	public int getWidth()
	{
		return w;
	}

	public int getHeight()
	{
		return h;
	}
	
	public boolean inRect(final GUIWithTabs gui, int mouseX, int mouseY) 
	{
		mouseX -= gui.getLeft();
		mouseY -= gui.getTop();

		return x <= mouseX && mouseX < x + w && y <= mouseY && mouseY < y + h;
	}

	public void setX(final int x) 
	{
		this.x = x;
	}

	public void setY(final int y) 
	{
		this.y = y;
	}
}
