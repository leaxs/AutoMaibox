package fr.leaxs.AutoMailbox.Utils;

public enum ColorUtils
{
	BLACK(0,		new int[]{1,1,1},false),
	BLUE(4,			new int[]{0,1,1},false),
	BROWN(3,		new int[]{1,1,0},false),
	CYAN(6,			new int[]{0,0,1},true),
	GRAY(8,			new int[]{1,1,1},false),
	GREEN(2,		new int[]{1,0,1},false),
	LIGHT_BLUE(12,	new int[]{0,0,1},false),
	LIGHT_GRAY(7,	new int[]{1,1,1},false),
	LIME(10,		new int[]{0,1,1},false),
	MAGANTA(13,		new int[]{0,1,0},true),
	NOTDEF(-1, null,false),
	ORANGE(14,		new int[]{1,1,0},false),
	PINK(9,			new int[]{1,1,0},false),
	PURPLE(5,		new int[]{1,0,1},false),
	RED(1,			new int[]{1,1,0},false),
	YELLOW(11,		new int[]{1,0,0},true);

	public static ColorUtils getByID(final int id)
	{
		if(id<0||id>14)
			return null;
		switch(id)
		{
		case 0:
			return ColorUtils.BLACK;
		case 1:
			return ColorUtils.RED;
		case 2:
			return ColorUtils.GREEN;
		case 3:
			return ColorUtils.BROWN;
		case 4:
			return ColorUtils.BLUE;
		case 5:
			return ColorUtils.PURPLE;
		case 6:
			return ColorUtils.CYAN;
		case 7:
			return ColorUtils.LIGHT_GRAY;
		case 8:
			return ColorUtils.GRAY;
		case 9:
			return ColorUtils.PINK;
		case 10:
			return ColorUtils.LIME;
		case 11:
			return ColorUtils.YELLOW;
		case 12:
			return ColorUtils.LIGHT_BLUE;
		case 13:
			return ColorUtils.MAGANTA;
		case 14:
			return ColorUtils.ORANGE;
		}
		return null;
	}
	public static ColorUtils getByName(final String color)
	{
		for(final ColorUtils c : ColorUtils.values())
		{
			if(c.toString().equalsIgnoreCase(color))
				return c;
		}
		return null;
	}
	public static boolean isValid(final int color)
	{
		if(getByID(color)!=null)
			return true;
		else
			return false;
	}

	public static boolean isValid(final String color)
	{
		for(final ColorUtils c : ColorUtils.values())
		{
			if(c.toString().equalsIgnoreCase(color))
				return true;
		}
		return false;
	}

	private int[] combinaison = {0,0,0};

	private int id = 0;

	private boolean secondary;

	ColorUtils(int identifier,int[] combinaison, boolean isSecondary)
	{
		id = identifier;
		this.combinaison = combinaison;
		secondary = isSecondary;
	}

	public boolean canBeCreate(final boolean[] secondary)
	{
		for(int i=0;i<3;i++)
		{
			if(secondary[i] && combinaison[i] == 1)
			{
				return false;
			}
		}
		return true;
	}

	public int getCode()
	{
		return id;
	}

	public int[] getMixing()
	{
		return combinaison;
	}
	
	public String[] getMixingName()
	{
		return new String[]{"yellow","magenta","cyan"};
	}

	public boolean isSecondary()
	{
		return secondary;
	}
}

