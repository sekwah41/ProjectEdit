package com.hardcoded.mc.general.world;

import com.hardcoded.mc.general.world.IBlockState.IBlockStateList;

import java.util.Map;
import java.util.Set;

public interface IBlockData {

	int getBlockId();

	String getName();

	boolean isAir();

	/**
	 * The value returned by this function could change between sessions.
	 */
	int getInternalId();

	int getMapColor();

	Set<IBlockState> getStates();

	IBlockData getFromStates(Map<String, String> states);

	IBlockStateList getStateList();

	IBlockData getDefaultState();

	boolean isOpaque();
}
