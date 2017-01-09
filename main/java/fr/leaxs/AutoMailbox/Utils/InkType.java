package fr.leaxs.AutoMailbox.Utils;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum InkType
{
	black(0,0xFF000000),cyan(6,0xFF00FFFF),maganta(13,0xFFFF00FF),yellow(11,0xFFFFFF00);

	public static final InkType order[] =
	{
		InkType.black,
		InkType.yellow,
		InkType.maganta,
		InkType.cyan
	};


	public static boolean isInkValid(final ItemStack item,final InkType dye)
	{
		return item.isItemEqual(new ItemStack(Items.dye, 1 , dye.getCode()));
	}

	public static boolean isPrinterInk(final ItemStack item)
	{
		for(final InkType ink : InkType.values())
		{
			if(item.isItemEqual(new ItemStack(Items.dye, 1 , ink.getCode())))
				return true;
		}
		return false;
	}

	private int meta = 0;
	private int color = 0;

	InkType(int metadata, int color)
	{
		meta = metadata;
		this.color = color;
	}

	public int getCode()
	{
		return meta;
	}
	
	public int getColor()
	{
		return color;
	}
}