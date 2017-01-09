package fr.leaxs.AutoMailbox.AdvancedPrinter;

import fr.leaxs.AutoMailbox.GUIUtils.GUITab;
import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;

public class InksGauge extends GUITab
{

	public InksGauge(String name, int x, int y, int w, int h) 
	{
		super(name, x, y, w, h);
	}

	@Override
	public void drawBackground(GUIWithTabs gui, int x, int y) {}

	@Override
	public void drawForeground(GUIWithTabs gui, int x, int y) {}
	
	@Override
	public void drawString(GUIWithTabs gui, int mouseX, int mouseY, String str) 
	{
		int colorID = 0;
		if(getName().equals("yellow"))
			colorID = 1;
		else if(getName().equals("maganta"))
			colorID = 2;
		else if(getName().equals("cyan"))
			colorID = 3;
		super.drawString(gui, mouseX, mouseY, getName()+" : "+((GUI_AdvancedPrinter)gui).getTEAdvPrinter().getInkLevel()[colorID]+"/64");
	}

}
