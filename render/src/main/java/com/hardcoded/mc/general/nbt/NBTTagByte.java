package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagByte extends NBTBase {
	private byte value;
	
	public NBTTagByte() {
		
	}
	
	public NBTTagByte(int value) {
		this.value = (byte)value;
	}
	
	public byte getValue() {
		return (byte)value;
	}
	
	public void setValue(int value) {
		this.value = (byte)value;
	}
	
	@Override
	public int getId() {
		return TAG_BYTE;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeByte(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readByte();
	}
	
	@Override
	public String toString() {
		return Byte.toString(value);
	}
}
