package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagShort extends NBTBase {
	private short value;
	
	public NBTTagShort() {
		
	}
	
	public NBTTagShort(int value) {
		this.value = (short)value;
	}
	
	public short getValue() {
		return (short)value;
	}
	
	public void setValue(int value) {
		this.value = (short)value;
	}
	
	@Override
	protected int getId() {
		return TAG_SHORT;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeShort(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readShort();
	}
	
	@Override
	public String toString() {
		return Short.toString(value);
	}
}
