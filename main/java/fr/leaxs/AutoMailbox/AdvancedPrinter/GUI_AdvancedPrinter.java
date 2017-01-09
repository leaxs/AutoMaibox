package fr.leaxs.AutoMailbox.AdvancedPrinter;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import fr.leaxs.AutoMailbox.GUIUtils.GUIWithTabs;
import fr.leaxs.AutoMailbox.Utils.InkType;

public class GUI_AdvancedPrinter extends GUIWithTabs
{
	private final ResourceLocation texture = new ResourceLocation("automailbox", "textures/gui/AdvancedPrinter.png");
	private TileEntity_AdvancedPrinter teAdvPrinter;
	private InksGauge inks[] = {
			new InksGauge("black",15, 24, 2, 37),
			new InksGauge("yellow",24, 24, 2, 37),
			new InksGauge("maganta",31, 24, 2, 37),
			new InksGauge("cyan",38, 24, 2, 37)
	};


	public GUI_AdvancedPrinter(InventoryPlayer inventoryPlayer, TileEntity_AdvancedPrinter teAdvPrinter) 
	{
		super(new Container_AdvancedPrinter(inventoryPlayer,teAdvPrinter));
		xSize = 175;
		ySize = 192;
		this.teAdvPrinter = teAdvPrinter;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float t,int x, int y) 
	{
		GL11.glColor4f(1,1,1,1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		//Draw filling bar
		for(int b=0; b<4; b++)
		{
			int fillBar = (int)((teAdvPrinter.getInkLevel()[b]/64F) * 37);
			if(fillBar > 0)
			{
				if(b==0)
					drawRect(guiLeft+15+3*b, 61+guiTop-fillBar, guiLeft+17+3*b, 61+guiTop, 0xFF000000);
				else
					drawRect(guiLeft+17+7*b, 61+guiTop-fillBar, guiLeft+19+7*b, 61+guiTop, InkType.order[b].getColor());
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) 
	{
		for(InksGauge ink : inks)
		{
			ink.drawString(this, x, y, "");
		}
	}
	
	public TileEntity_AdvancedPrinter getTEAdvPrinter()
	{
		return teAdvPrinter;
	}

}
