package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagDouble extends NBTBase {
	private double value;
	
	public NBTTagDouble() {
		
	}
	
	public NBTTagDouble(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	protected int getId() {
		return TAG_DOUBLE;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeDouble(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readDouble();
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}
}
