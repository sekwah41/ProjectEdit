package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagInt extends NBTBase {
	private int value;
	
	public NBTTagInt() {
		
	}
	
	public NBTTagInt(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
	
	@Override
	protected int getId() {
		return TAG_INT;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeInt(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readInt();
	}
	
	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
