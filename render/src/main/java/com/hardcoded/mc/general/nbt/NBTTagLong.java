package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagLong extends NBTBase {
	private long value;
	
	public NBTTagLong() {
		
	}
	
	public NBTTagLong(long value) {
		this.value = value;
	}
	
	public long getValue() {
		return value;
	}
	
	public void setValue(long value) {
		this.value = value;
	}
	
	@Override
	protected int getId() {
		return TAG_LONG;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeLong(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readLong();
	}
	
	@Override
	public String toString() {
		return Long.toString(value);
	}
}
