package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

public class NBTTagFloat extends NBTBase {
	private float value;
	
	public NBTTagFloat() {
		
	}
	
	public NBTTagFloat(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	@Override
	protected int getId() {
		return TAG_FLOAT;
	}
	
	@Override
	public void write(ByteBuf writer, int depth) {
		writer.writeFloat(value);
	}
	
	@Override
	public void read(ByteBuf reader, int depth) {
		this.value = reader.readFloat();
	}
	
	@Override
	public String toString() {
		return Float.toString(value);
	}
}
