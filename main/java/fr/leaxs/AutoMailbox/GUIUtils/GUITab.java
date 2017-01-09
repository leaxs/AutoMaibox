package fr.leaxs.AutoMailbox.GUIUtils;

public abstract class GUITab extends GuiRectangle 
{
	private final String name;

	public GUITab(final String name, final int x, final int y, final int w, final int h)
	{
		super(x, y, w, h);
		this.name = name;
	}
	public abstract void drawBackground(GUIWithTabs gui, int x, int y);
	public abstract void drawForeground(GUIWithTabs gui, int x, int y);
	public void mouseClick(final GUIWithTabs gui, final int x, final int y, final int button) {}
	public void mouseMoveClick(final GUIWithTabs gui, final int x, final int y, final int button, final long timeSinceClicked) {}
	public void mouseReleased(final GUIWithTabs gui, final int x, final int y, final int button) {}
	
	public String getName()
	{
		return name;
	}
}
