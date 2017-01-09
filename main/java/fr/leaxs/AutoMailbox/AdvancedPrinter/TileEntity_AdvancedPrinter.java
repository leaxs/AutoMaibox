package fr.leaxs.AutoMailbox.AdvancedPrinter;

import java.util.ArrayList;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.shared.media.items.ItemPrintout;
import dan200.computercraft.shared.peripheral.PeripheralType;
import dan200.computercraft.shared.peripheral.common.IPeripheralTile;
import fr.leaxs.AutoMailbox.Utils.ColorUtils;
import fr.leaxs.AutoMailbox.Utils.InkType;
import fr.leaxs.AutoMailbox.Utils.TileEntityContainer;

public class TileEntity_AdvancedPrinter extends TileEntityContainer implements IPeripheral, IPeripheralTile
{
	private String pageTitle = "";
	private Terminal page;
	private int lastColor = 0;
	private int[] inkLevel = new int[]{0,0,0,0};
	private int[] inkLevelRemaning = new int[]{0,0,0,0};
	private ArrayList<ColorUtils> colorUsed = new ArrayList<ColorUtils>();
	private boolean m_printing;

	public TileEntity_AdvancedPrinter() 
	{
		super("AdvancedPrinter", 16, 64);
		page = new Terminal(25, 21);

	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack items) 
	{
		if(slotID<16)
		{
			int i = slotID<6? 0 : slotID<10? 1 : 2;
			return Slot_AdvancedPrinter.isItemValidForSlot(items,slotID,i);
		}
		return super.isItemValidForSlot(slotID, items);
	}
	
	@Override
	public void updateEntity() 
	{
		boolean hasChange = false;
		for(int i=0;i<4;i++)
		{
			while(inkLevel[i]<64 && getStackInSlot(i+6) != null)
			{
				if(m_printing)
					inkLevelRemaning[i]++;
				inkLevel[i]++;
				decrStackSize(i+6, 1);
				hasChange = true;
			}
		}
		if(hasChange)
			this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		super.updateEntity();
	}

	public int[] getInkLevel()
	{
		return inkLevel;
	}

	private boolean checkInkLevel()
	{
		synchronized (this.items)
		{
			for(int i=0;i<4;i++)
			{
				if(inkLevel[i]<=0)
					return true;
			}
			return false;
		}
	}

	private String getMissingElement()
	{
		String error = "";
		if(getPaperLevel()<=0)
			error +="No paper | ";
		for(int i=0;i<4;i++)
		{
			if(inkLevel[i]<=0)
				error+= InkType.order[i].name()+" empty |";
			else if(inkLevel[i]<=8)
				error+= InkType.order[i].name()+" low |";
		}
		return error;
	}

	@Override
	public int getDirection() 
	{
		return 0;
	}

	@Override
	public void setDirection(int arg0) {}

	@Override
	public String getLabel() 
	{
		return "Advanced printer";
	}

	@Override
	public IPeripheral getPeripheral(int arg0) 
	{
		return this;
	}

	@Override
	public PeripheralType getPeripheralType() 
	{
		return PeripheralType.Printer;
	}

	@Override
	public String getType() 
	{
		return "Advanced_printer";
	}

	@Override
	public String[] getMethodNames() 
	{
		return new String[] { "write", "setCursorPos", "getCursorPos", "getPageSize", "newPage", "endPage", "getInkLevel", "setPageTitle", "getPaperLevel" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context,
			int method, Object[] args) throws InterruptedException,
			LuaException 
	{
		switch(method)
		{
		case 0:	//write(String txt [,int/String color])
			if (page == null)
				throw new LuaException("Page not started");
			if(args.length == 0)
				throw new LuaException("You must specify at least a text");
			else if(args[0] instanceof String)
			{
				int colorID = lastColor;
				if(args.length > 1)
				{
					if(args[1] instanceof Number)
						colorID = ((Number)args[1]).intValue();
					else if(args[1] instanceof String)
						colorID = ColorUtils.getByName((String)args[1]).getCode();
				}
				ColorUtils color = ColorUtils.getByID(colorID);
				if(!colorUsed.contains(color))
				{
					System.out.println(color);
					System.out.println(inkLevelRemaning[0]);
					colorUsed.add(color);
					if(color == ColorUtils.BLACK && inkLevelRemaning[0] != 0)
						inkLevelRemaning[0]--;
					else
					{
						for(int i=1;i<4;i++)
						{
							if(inkLevelRemaning[i]-color.getMixing()[i-1]<0)
								throw new LuaException("insufficient ink : "+color.getMixingName()[i]);
							inkLevelRemaning[i]-=color.getMixing()[i-1];
						}
					}
					lastColor = colorID;
					System.out.println(inkLevelRemaning[0]);
				}
				page.setTextColour(15 - lastColor);
				page.write((String)args[0]);
				page.setCursorPos(page.getCursorX() + ((String)args[0]).length(), page.getCursorY());
			}
			else
				throw new LuaException("Wrong argument type, must be String");
			return null;
		case 1:	//setCursorPos(int x, int y)
			if (page == null)
				throw new LuaException("Page not started");
			if ((args.length != 2) || 
					(args[0] == null) || (!(args[0] instanceof Number)) ||
					(args[1] == null) || (!(args[1] instanceof Number))) 
				throw new LuaException("Expected number, number");

			int x = ((Number)args[0]).intValue() - 1;
			int y = ((Number)args[1]).intValue() - 1;
			page.setCursorPos(x, y);
			return null;
		case 2:	//getCursorPos()
			if (page == null)
				throw new LuaException("Page not started");
			return new Object[] { 
					Integer.valueOf(page.getCursorX() + 1),
					Integer.valueOf(page.getCursorY() + 1)
			};
		case 3:	//getPageSize()
			if (page == null)
				throw new LuaException("Page not started");
			return new Object[] { 
					Integer.valueOf(page.getWidth()),
					Integer.valueOf(page.getHeight())
			};
		case 4: //newPage()
			return new Object[] { startNewPage() };
		case 5:	//endPage()
			if (page == null)
				throw new LuaException("Page not started");
			return new Object[] { endCurrentPage() };
		case 6:	//getInkLevel()
			return new Object[] { inkLevel[0],inkLevel[1],inkLevel[2],inkLevel[3] };
		case 7:	//setTitlePage(String title)
			if (page == null)
				throw new LuaException("Page not started");
			else if(args.length != 0 && args[0] instanceof String)
				pageTitle = (String)args[0];
			else
				throw new LuaException("Expected string");
			return null;
		case 8: //getPaperLevel()
			return new Object[] { Integer.valueOf(getPaperLevel()) };
		}
		return null;
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) 
	{
		return false;
	}


	private int getPaperLevel() 
	{
		int count = 0;
		synchronized (this.items)
		{
			for (int i = 0; i < 6; i++)
			{
				ItemStack paperStack = this.items[i];
				if ((paperStack != null) && (isPaper(paperStack))) {
					count += paperStack.stackSize;
				}
			}
		}
		return count;
	}

	private boolean isPaper(ItemStack item) 
	{
		return item.isItemEqual(new ItemStack(Items.paper))||
				(item.getItem() instanceof ItemPrintout && ItemPrintout.getType(item) == ItemPrintout.Type.Single);

	}

	public String startNewPage()
	{
		synchronized (this.items)
		{
			if (canInputPage())
			{
				if ((this.m_printing) && (!outputPage())) {
					return "Cannnot start a new page";
				}
				if (inputPage()) {
					return "Page Started";
				}
			}
			return getMissingElement();
		}
	}

	public boolean endCurrentPage()
	{
		synchronized (this.items)
		{
			if ((this.m_printing) && (outputPage())) {
				return true;
			}
		}
		return false;
	}

	private boolean canInputPage()
	{
		synchronized (this.items)
		{
			if(checkInkLevel())
				return false;
			if (getPaperLevel() > 0)
				return true;
			return false;
		}
	}

	private boolean inputPage()
	{
		synchronized (this.items)
		{
			for (int i = 0; i < 6; i++)
			{
				ItemStack paperStack = this.items[i];
				if ((paperStack != null) && (isPaper(paperStack)))
				{
					paperStack.stackSize -= 1;
					if (paperStack.stackSize <= 0)
					{
						this.items[i] = null;
					}
					this.page.clear();
					if ((paperStack.getItem() instanceof ItemPrintout))
					{
						this.pageTitle = ItemPrintout.getTitle(paperStack);
						String[] text = ItemPrintout.getText(paperStack);
						String[] textColour = ItemPrintout.getColours(paperStack);
						for (int y = 0; y < this.page.getHeight(); y++) {
							this.page.setLine(y, text[y], textColour[y], "");
						}
					}
					else
						this.pageTitle = "";
					this.page.setCursorPos(0, 0);

					markDirty();
					this.m_printing = true;
					inkLevelRemaning = inkLevel.clone();
					colorUsed.clear();
					return true;
				}
			}
			return false;
		}
	}

	private boolean outputPage()
	{
		synchronized (this.page)
		{
			int height = this.page.getHeight();
			String[] lines = new String[height];
			String[] colours = new String[height];
			for (int i = 0; i < height; i++)
			{
				lines[i] = this.page.getLine(i).toString();
				colours[i] = this.page.getTextColourLine(i).toString();
			}
			ItemStack stack = ItemPrintout.createSingleFromTitleAndText(this.pageTitle, lines, colours);
			synchronized (this.items)
			{
				ItemStack remainder = stack.copy();
				for(int x=10;x<16;x++)
				{
					if(getStackInSlot(x) == null)
						setInventorySlotContents(x, remainder.splitStack(Math.min(getInventoryStackLimit(), remainder.stackSize)));
					if(remainder.stackSize<=0)
					{
						remainder = null;
						break;
					}
				}
				if (remainder == null)
				{
					this.m_printing = false;
					colorUsed.clear();
					inkLevel = inkLevelRemaning.clone();
					lastColor = 0;
					this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound cmpd) 
	{
		inkLevel = cmpd.hasKey("InksLevel") ? cmpd.getIntArray("InksLevel") : inkLevel;
		super.readFromNBT(cmpd);
	}

	@Override
	public void writeToNBT(NBTTagCompound cmpd) 
	{
		cmpd.setIntArray("InksLevel", inkLevel);
		super.writeToNBT(cmpd);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) 
	{
		readFromNBT(pkt.func_148857_g());
		super.onDataPacket(net, pkt);
	}

	@Override
	public Packet getDescriptionPacket() 
	{
		NBTTagCompound NTBTag = new NBTTagCompound();
		writeToNBT(NTBTag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, NTBTag);
	}
}
