package com.hardcoded.mc.general.world;

import com.hardcoded.mc.general.ByteBuf;
import com.hardcoded.mc.utils.StreamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * This class handles region files and can both read and write to them.
 *
 * <p>The structure of the region file is as follows:
 * <pre>
 * RegionFile:
 *     int[1024]: offsets
 *     int[1024]: last_modified
 *     byte[....]: sectors
 * </pre>
 *
 * The value of {@code offsets} points towards individual chunks inside the file.
 * Calling {@link #getChunkBuffer(int, int)} will give you a {@code ByteBuf} stream
 * that contains the raw chunk nbt data.
 *
 * Minecraft currently uses two compression systems inside the region files and
 * those are {@code GZIP} and {@code DEFLATE}.
 *
 * @author HardCoded
 *
 */
public class RegionFile {
	private static final Logger LOGGER = LogManager.getLogger(RegionFile.class);

	private static final int SECTOR_BYTES = 4096;
	private static final int SECTOR_INTS = SECTOR_BYTES / 4;

	private static final int VERSION_GZIP = 1;
	private static final int VERSION_DEFLATE = 2;

	final File file;
	private final int[] offsets;
	private final int[] timestamps;
	private final ByteBuf buf;

	public RegionFile(File file) throws IOException {
		this.file = file;
		this.offsets = new int[SECTOR_INTS];
		this.timestamps = new int[SECTOR_INTS];

		byte[] read = Files.readAllBytes(Path.of(file.toURI()));
		byte[] bytes = new byte[read.length + 4095 - ((read.length - 1) & 4095)];
		System.arraycopy(read, 0, bytes, 0, read.length);


		int sectors = bytes.length / SECTOR_BYTES;
		boolean[] has_sector = new boolean[sectors];
		Arrays.fill(has_sector, true);

		has_sector[0] = false; // chunk offset
		has_sector[1] = false; // timestamp

		this.buf = ByteBuf.direct(bytes);
		for(int i = 0; i < SECTOR_INTS; i++) {
			int offset = buf.readInt();
			offsets[i] = offset;

			if(offset != 0 && ((offset >> 8) + (offset & 0xff)) < sectors) {
				final int len = offset & 0xff;
				for(int j = 0; j < len; j++) {
					has_sector[(offset >> 8) + j] = false;
				}
			}
		}

		for(int i = 0; i < SECTOR_INTS; i++) {
			timestamps[i] = buf.readInt();
		}
	}

	public ByteBuf getChunkBuffer(int x, int z) {
		if(!isInsideBounds(x, z)) {
			return null;
		}

		int offset = getOffset(x, z);
		if(offset == 0) {
			return null;
		}

		int sector = offset >> 8;

		int sector_index = sector * SECTOR_BYTES;
		int length = buf.readInt(sector_index);
		int version = buf.readUnsignedByte(sector_index + 4);

		if(version == VERSION_GZIP) {
			byte[] bytes = StreamUtils.decompress_gzip(buf.readBytes(sector_index + 5, length));
			if(bytes != null) {
				return ByteBuf.direct(bytes);
			}

			LOGGER.error("Failed to decompress gzip");
		} else if(version == VERSION_DEFLATE) {
			byte[] bytes = StreamUtils.decompress_deflate(buf.readBytes(sector_index + 5, length));
			if(bytes != null) {
				return ByteBuf.direct(bytes);
			}

			LOGGER.error("Failed to decompress deflate");
		} else {
			LOGGER.error("Unknown RegionFileChunk version {}", version);
		}

		return null;
	}

	protected boolean isInsideBounds(int x, int z) {
		return x >= 0 && x < 32 && z >= 0 && z < 32;
	}

	protected int getOffset(int x, int z) {
		return offsets[x + z * 32];
	}

	public boolean hasChunk(int x, int z) {
		return getOffset(x, z) != 0;
	}
}
