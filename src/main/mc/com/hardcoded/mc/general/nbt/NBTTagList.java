package com.hardcoded.mc.general.nbt;

import java.util.ArrayList;
import java.util.List;

import com.hardcoded.mc.general.PacketIO;

public class NBTTagList<T extends NBTBase> extends NBTBase {
	private List<T> list;
	
	public NBTTagList(String name) {
		super(name, TAG_LIST);
		this.list = new ArrayList<T>();
	}
	
	public void add(int index, T element) {
		this.list.add(index, element);
	}
	
	public void add(T element) {
		this.list.add(element);
	}
	
	public void clear() {
		this.list.clear();
	}
	
	public void remove(int index) {
		this.list.remove(index);
	}
	
	public NBTBase get(int index) {
		return this.list.get(index);
	}
	
	public void remove(T element) {
		this.list.remove(element);
	}
	
	public int size() {
		return this.list.size();
	}
	
	public void write(PacketIO writer, int depth) {
		if(size() == 0) {
			writer.writeByte(0);
			writer.writeInt(0);
		} else {
			NBTBase base = this.list.get(0);
			writer.writeByte(base.getId());
			writer.writeInt(size());
			
			for(NBTBase nbt : this.list) {
				// System.out.println(StringUtils.printNBT(nbt, depth + 1));
				nbt.write(writer, depth + 1);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void read(PacketIO reader, int depth) {
		int type = reader.readByte();
		
		if(type == 0) {
			reader.readInt();
			return;
		} else {
			int length = reader.readInt();
			
			for(int i = 0; i < length; i++) {
				NBTBase element = NBTBase.createFromId(null, type);
				element.read(reader, depth + 1);
				add((T)element);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(T element : list) {
			sb.append(element.toString()).append(", ");
		}
		
		if(list.size() == 0) {
			return sb.append("}").toString();
		} else {
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			return sb.append("}").toString();
		}
	}
	
	@Override
	public Object getObjectValue() {
		return list;
	}
}
