package com.hardcoded.mc.general.world;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockDataManager {
	static final Map<Integer, BlockData> id_states = new LinkedHashMap<>();
	private static final Map<Integer, BlockData> states = new LinkedHashMap<>();

	// Temporary
	public static IBlockData getState(String name) {
		return states.get(name.hashCode());
	}

	public static IBlockData getState(String name, Map<String, String> states) {
		IBlockData data = getState(name);
		if(data == null) return null;
		return data.getFromStates(states);
	}

	public static IBlockData getStateFromInternalId(int id) {
		return id_states.get(id);
	}

	public static IBlockData getState(String name, boolean occluding, int rgb, List<IBlockState> list) {
		int hash = name.hashCode();
		BlockData state = states.get(hash);
		if(state == null) {
			state = new BlockData(name, list)
				.setColor(rgb)
				.setOccluding(occluding);

			states.put(hash, state);
		}

		return state;
	}

	public static final Set<IBlockData> getStates() {
		return states.values().stream().map(i -> (IBlockData)i).collect(Collectors.toUnmodifiableSet());
	}
}
