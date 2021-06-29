package com.hardcoded.api;

import java.io.File;

import com.hardcoded.render.utils.Nonnull;
import com.hardcoded.render.utils.Nullable;

/**
 * Interface describing the ProjectEdit api
 *
 * @author HardCoded
 */
public interface IProjectEdit {

	/**
	 * Returns the current camera used by the editor
	 */
	@Nonnull
	ICamera getCamera();

	/**
	 * Returns the current world loaded by the editor.
	 * If no world is loaded {@code null} will be returned
	 */
	@Nullable
	IWorld getWorld();

	/**
	 * Load a world from a world folder
	 *
	 * @param file the folder of the world
	 * @return the loaded world
	 */
	IWorld loadWorld(File file);
}
