package com.hardcoded.mc.general.nbt;

import com.hardcoded.mc.general.ByteBuf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NBTTagList<T extends NBTBase> extends NBTBase implements Iterable<T> {
	private List<T> list;

	public NBTTagList() {
		list = new ArrayList<>();
	}

	public void add(int index, T element) {
		list.add(index, element);
	}

	public void add(T element) {
		list.add(element);
	}

	public void clear() {
		list.clear();
	}

	public void remove(int index) {
		list.remove(index);
	}

	public T get(int index) {
		return list.get(index);
	}

	public void remove(T element) {
		list.remove(element);
	}

	/**
	 * @return the size of this {@code NBTTagList}
	 */
	public int size() {
		return list.size();
	}

	@Override
	protected int getId() {
		return TAG_LIST;
	}

	@Override
	public void write(ByteBuf writer, int depth) {
		if(size() == 0) {
			writer.writeByte(0);
			writer.writeInt(0);
		} else {
			NBTBase base = this.list.get(0);
			writer.writeByte(base.getId());
			writer.writeInt(size());

			for(NBTBase nbt : this.list) {
				nbt.write(writer, depth + 1);
			}
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int index;

			@Override
			public boolean hasNext() {
				return index < size();
			}

			@Override
			public T next() {
				return get(index++);
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(ByteBuf reader, int depth) {
		int type = reader.readByte();

		if(type == 0) {
			reader.readInt();
			return;
		} else {
			int length = reader.readInt();

			for(int i = 0; i < length; i++) {
				NBTBase element = NBTBase.createFromId(type);
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
}
