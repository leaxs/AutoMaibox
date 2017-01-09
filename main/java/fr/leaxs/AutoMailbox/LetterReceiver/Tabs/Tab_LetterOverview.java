package fr.leaxs.AutoMailbox.LetterReceiver.Tabs;

import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;

public class Tab_LetterOverview extends GUITab
{

	public Tab_LetterOverview(int x, int y, int w, int h) 
	{
		super("Overview", x, y, w, h);
	}

	@Override
	public void drawBackground(GUIWithTabs gui, int x, int y) 
	{
		for(int i=0;i<6;i++)
		{
			gui.drawTexturedModalRect(gui.getLeft()+5, gui.getTop()+23+18*i, 5, 5, 216, 18);
		}
		
		gui.drawTexturedModalRect(gui.getLeft()+164, gui.getTop()+131, 262, 131, 2, 1);
		gui.drawTexturedModalRect(gui.getLeft()+164, gui.getTop()+132, 262, 131, 1, 1);
		gui.drawTexturedModalRect(gui.getLeft()+221, gui.getTop()+110, 221, 89, 2, 21);
	}

	@Override
	public void drawForeground(GUIWithTabs gui, int x, int y) {}

}
