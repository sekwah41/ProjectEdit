package com.hardcoded.mc.general.world;

import com.hardcoded.mc.general.ByteBuf;
import com.hardcoded.mc.general.files.Blocks;
import com.hardcoded.mc.general.nbt.*;

import java.util.HashMap;
import java.util.Map;

public class RegionChunk {
	private NBTTagCompound nbt;
	public SubChunk[] sections = new SubChunk[16];
	private int dataVersion;

	public RegionChunk(ByteBuf buf) {
		this.nbt = NBTBase.readNBTTagCompound(buf);
		read();
	}

	@SuppressWarnings("unchecked")
	public void read() {
		NBTTagCompound level = (NBTTagCompound)nbt.get("Level");
		NBTTagList<NBTTagCompound> sections = (NBTTagList<NBTTagCompound>)level.get("Sections");
		if(sections == null) {

			// Ooof
			return;
		}

		dataVersion = ((NBTTagInt)nbt.get("DataVersion")).getValue();

		NBTTagCompound[] nbt_sections = new NBTTagCompound[16];
		for(NBTTagCompound entry : sections) {
			int y = ((NBTTagByte)entry.get("Y")).getValue();

			if(y < 0 || y > 15) continue;
			nbt_sections[y] = entry;
			this.sections[y] = new SubChunk(entry, y);
		}

		// Clear nbt
		// Release memory
		nbt = null;
	}

	public SubChunk getSubChunk(int y) {
		return sections[y];
	}

	private static int ceillog2(int i) {
		return i < 1 ? 0:(32 - Integer.numberOfLeadingZeros(i - 1));
	}

	public class SubChunk {
		public IBlockData[] blocks = new IBlockData[4096];
		public NBTTagCompound nbt;
		public int y;

		public SubChunk(NBTTagCompound nbt, int y) {
			this.nbt = nbt;
			this.y = y;
			load();
		}

		@SuppressWarnings("unchecked")
		public void load() {
			NBTTagList<NBTTagCompound> palette = (NBTTagList<NBTTagCompound>)nbt.get("Palette");
			if(palette == null) return;
			final int len = palette.size();

			NBTTagLongArray blockStates = (NBTTagLongArray)nbt.get("BlockStates");
			int bits_per_block = Math.max(4, ceillog2(len));
			Map<String, String>[] states_map = new Map[len];

			long[] array = blockStates.getArray();
			String[] palette_test = new String[palette.size()];
			for(int i = 0; i < len; i++) {
				NBTTagCompound entry = palette.get(i);
				String name = ((NBTTagString)entry.get("Name")).getValue();
				palette_test[i] = name;

				NBTTagCompound props = (NBTTagCompound)entry.get("Properties");
				if(props == null) {
					states_map[i] = Map.of();
				} else {
					Map<String, String> map = new HashMap<>();
					states_map[i] = map;

					for(String key : props.keySet()) {
						map.put(key, ((NBTTagString)props.get(key)).getValue());
					}
				}
			}

			IBlockData[] block_palette = new IBlockData[len];
			for(int i = 0; i < len; i++) {
				block_palette[i] = BlockDataManager.getState(palette_test[i], states_map[i]);
			}

			// 2527
			// 2230
			if(dataVersion < 2527) {//1976) {
				getBlockArrayFast(array, block_palette, bits_per_block);
			} else {
				compute_blocks(array, block_palette, bits_per_block);
			}

			if(wrongMethod) {
				System.out.println("WrongMethod for: " + dataVersion);
			}
		}

		private boolean wrongMethod = false;

		private void compute_blocks(long[] data, IBlockData[] block_palette, long bits) {
			final IBlockData[] array = blocks;

			final long mask = (1L << bits) - 1;
			final long gt = 64 - bits;
			for(int i = 0, index = 0; i < 4096; index += bits, i++) {
				final long offset = (index & 63);
				if(index >> 6 >= data.length) break;

				int value = (int)((data[index >> 6] >>> offset) & mask);
				if(value >= block_palette.length) {
					array[i] = Blocks.AIR;
					wrongMethod = true;
				} else {
					array[i] = block_palette[value];
				}

				final long next_offset = (index + bits) & 63;
				index += (next_offset > gt) ? (64 - next_offset):0;
			}
		}

		private void getBlockArrayFast(long[] data, IBlockData[] block_palette, int bits) {
			final IBlockData[] array = blocks;

			long mask = (1 << (bits + 0L)) - 1;
			long offset = 0;
			int position = 0;
			int amount = 4096;

			for(int i = 0; i < data.length; i++) {
				long value = data[i] >>> offset;

				for(int j = 0; j <= 63 / bits; j++) {
					long bitPos = j * bits;

					long id = value & mask;
					value >>>= bits;

					if(bitPos + offset + bits > 64) {
						long pos = ((bitPos + bits + offset) & 63);
						long next = 0;
						if(i + 1 < data.length) {
							next = (data[i + 1] & ((1 << pos) - 1)) << (bits - pos);
						}

						id |= next;
						if(position < amount) {
							array[position++] = block_palette[(int)id];
						}

						offset = pos;
						break;
					}

					if(position < amount) {
						array[position++] = block_palette[(int)id];
					}
				}
			}
		}

//		private void compute_blocks(long[] data, String[] palette, long bits) {
//			final long mask = (1L << bits) - 1;
//			final long gt = 64 - bits;
//			for(int i = 0, index = 0; i < 4096; index += bits, i++) {
//				final long offset = (index & 63);
//				int value = (int)((data[index >> 6] >>> offset) & mask);
//				blocks[i] = BlockDataManager.getState(palette[value]);
//
//				final long next_offset = (index + bits) & 63;
//				index += (next_offset > gt) ? (64 - next_offset):0;
//			}
//		}
	}

	public NBTTagCompound getNBT() {
		return nbt;
	}
}
