package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public abstract class NBTBase {
	public static final int TAG_END = 0,
							TAG_BYTE = 1,
							TAG_SHORT = 2,
							TAG_INT = 3,
							TAG_LONG = 4,
							TAG_FLOAT = 5,
							TAG_DOUBLE = 6,
							TAG_BYTE_ARRAY = 7,
							TAG_STRING = 8,
							TAG_LIST = 9,
							TAG_COMPOUND = 10,
							TAG_INT_ARRAY = 11,
							TAG_LONG_ARRAY = 12;
	
	protected NBTBase() {
		
	}
	
	protected abstract int getId();
	public abstract void write(ByteBuf writer, int depth);
	public abstract void read(ByteBuf reader, int depth);
	
	public static NBTTagCompound readNBTTagCompound(ByteBuf reader) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.read(reader, 0);
		
		if(tag.size() < 1) {
			return null;
		} else {
			return (NBTTagCompound)tag.get("");
		}
	}
	
	public static void writeNBTTagCompound(ByteBuf writer, NBTBase base) {
		if(base == null) {
			writer.writeByte(TAG_END);
			return;
		}
		
		base.write(writer, 0);
	}
	
	protected static NBTBase createFromId(int id) {
		switch(id) {
			case TAG_BYTE: return new NBTTagByte();
			case TAG_SHORT: return new NBTTagShort();
			case TAG_INT: return new NBTTagInt();
			case TAG_LONG: return new NBTTagLong();
			case TAG_FLOAT: return new NBTTagFloat();
			case TAG_DOUBLE: return new NBTTagDouble();
			case TAG_BYTE_ARRAY: return new NBTTagByteArray();
			case TAG_STRING: return new NBTTagString();
			case TAG_LIST: return new NBTTagList<>();
			case TAG_COMPOUND: return new NBTTagCompound();
			case TAG_INT_ARRAY: return new NBTTagIntArray();
			case TAG_LONG_ARRAY: return new NBTTagLongArray();
			case TAG_END:
			default:
				return null;
		}
	}
	
	public static String getTypeIdName(int id) {
		switch(id) {
			case TAG_BYTE: return "TAG_BYTE";
			case TAG_SHORT: return "TAG_SHORT";
			case TAG_INT: return "TAG_INT";
			case TAG_LONG: return "TAG_LONG";
			case TAG_FLOAT: return "TAG_FLOAT";
			case TAG_DOUBLE: return "TAG_DOUBLE";
			case TAG_BYTE_ARRAY: return "TAG_BYTE_ARRAY";
			case TAG_STRING: return "TAG_STRING";
			case TAG_LIST: return "TAG_LIST";
			case TAG_COMPOUND: return "TAG_COMPOUND";
			case TAG_INT_ARRAY: return "TAG_INT_ARRAY";
			case TAG_LONG_ARRAY: return "TAG_LONG_ARRAY";
			case TAG_END: return "TAG_END";
		}
		
		return null;
	}
}
