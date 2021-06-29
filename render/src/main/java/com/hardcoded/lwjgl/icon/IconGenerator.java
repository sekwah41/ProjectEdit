package com.hardcoded.lwjgl.icon;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.joml.Matrix4f;
import org.lwjgl.opengl.*;

import com.hardcoded.api.IResource;
import com.hardcoded.lwjgl.LwjglWindow;
import com.hardcoded.lwjgl.data.TextureAtlas;
import com.hardcoded.mc.general.world.BlockDataManager;
import com.hardcoded.mc.general.world.IBlockData;
import com.hardcoded.mc.utils.MeshBuilder;
import com.hardcoded.mc.utils.FastModelRenderer;
import com.hardcoded.mc.utils.MathUtils;

public class IconGenerator extends IResource {
	public static final int ICON_WIDTH = 64;
	public static final int ICON_HEIGHT = 64;

	private final TextureManager manager;
	private TextureAtlas atlas;
	private int frameBuffer;
	private int textureBuffer;
	private int depthBuffer;

	public IconGenerator(TextureManager manager) {
		this.manager = manager;
		atlas = new TextureAtlas(0, Math.max(ICON_WIDTH, ICON_HEIGHT), 2048, 2048, false);
	}

	public int getBlockId(IBlockData data) {
		return atlas.getImageId(data.getDefaultState().getName());
	}

	public TextureAtlas getTextureAtlas() {
		return atlas;
	}

	@Override
	public void init() {
		// Create FrameBuffer
		frameBuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);

		// Create Texture
		textureBuffer = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBuffer);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, ICON_WIDTH, ICON_HEIGHT, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL32.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textureBuffer, 0);

		// Add Depth Buffer
		depthBuffer = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthBuffer);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, ICON_WIDTH, ICON_HEIGHT, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer)null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, depthBuffer, 0);

		// Unbind
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}

	private int generateIcon(IBlockData data) {
		if(atlas.hasImage(data.getName())) {
			return atlas.getImageId(data.getName());
		}

		int[] pixels = new int[ICON_WIDTH * ICON_HEIGHT];
		int[] read_pixels = drawGetPixels(data);

		for(int i = 0, len = read_pixels.length; i < len; i++) {
			int rgba = read_pixels[i];

			int r = (rgba >>   0) & 0xff;
			int g = (rgba >>   8) & 0xff;
			int b = (rgba >>  16) & 0xff;
			int a = (rgba >>> 24) & 0xff;

			int x = i % ICON_WIDTH;
			int y = i / ICON_HEIGHT;

			int idx = x + (ICON_HEIGHT - y - 1) * ICON_WIDTH;
			if(a != 0) {
				pixels[idx] = (r << 16) | (g << 8) | (b) | (a << 24);
			}
		}

		BufferedImage bi = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		bi.setRGB(0, 0, ICON_WIDTH, ICON_HEIGHT, pixels, 0, ICON_WIDTH);

		int id = atlas.addTexture(data.getName(), bi);
		return id;
	}

	private int[] drawGetPixels(IBlockData data) {
		// Set States
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// Bind FrameBuffer
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, ICON_WIDTH, ICON_HEIGHT);

		// Clear FrameBuffer
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		drawBlock(data);

		int[] pixels = new int[ICON_WIDTH * ICON_HEIGHT];
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBuffer);
		GL46.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		// Unbind
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

		// Disable States
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glViewport(0, 0, LwjglWindow.getWidth(), LwjglWindow.getHeight());

		return pixels;
	}

	public void loadIcons() {
		// Clear the current atlas
		atlas.unload();

		for(IBlockData data : BlockDataManager.getStates()) {
			generateIcon(data);
		}

		atlas.reload();
	}

	private List<IBlockData> loadingBlocks;
	private int loadingIndex;
	public boolean loadIconsPartwise(int max_loading, BiConsumer<Integer, Integer> callback) {
		if(loadingBlocks == null) {
			atlas.unload();
			loadingBlocks = new ArrayList<>(BlockDataManager.getStates());
		}

		for(int i = 0; i < max_loading; i++) {
			if(loadingIndex < loadingBlocks.size()) {
				IBlockData data = loadingBlocks.get(loadingIndex++);
				generateIcon(data);
			} else {
				callback.accept(loadingIndex, loadingBlocks.size());
				atlas.reload();
				loadingBlocks = null;
				loadingIndex = 0;
				return true;
			}
		}

		callback.accept(loadingIndex, loadingBlocks.size());
		return false;
	}

	public void drawBlock(IBlockData data) {
		float side = 0.25f;
		Matrix4f projMat = new Matrix4f()
			.setOrtho(-side, side, -side, side, -10, 10);

		float t = 0.5f / 16.0f;
		Matrix4f viewMat = new Matrix4f()
			.translateLocal(-t, -t, -t)
			.rotateLocalY(-MathUtils.toRadians(45))
			.rotateLocalX(-MathUtils.toRadians(-30))
			.scaleLocal(4)
		;

		Matrix4f projView = projMat.mul(viewMat, new Matrix4f());

		GL11.glPushMatrix();
		GL11.glLoadMatrixf(projView.get(new float[16]));

		MeshBuilder buffer = new MeshBuilder();
		FastModelRenderer.renderModel(data, 0, 0, 0, buffer, -1);

		float[] color = buffer.colors.toArray();
		float[] verts = buffer.verts.toArray();
		float[] uv = buffer.uvs.toArray();

		manager.getBlockAtlas().bind();
		GL11.glBegin(GL11.GL_TRIANGLES);
		for(int vi = 0, ui = 0, len = buffer.verts.size(); vi < len; vi += 3, ui += 2) {
			GL11.glTexCoord2f(uv[ui], uv[ui + 1]);
			GL11.glColor3f(color[vi], color[vi + 1], color[vi + 2]);
			GL11.glVertex3f(verts[vi] / 16.0f, verts[vi + 1] / 16.0f, verts[vi + 2] / 16.0f);
		}
		GL11.glEnd();
		manager.getBlockAtlas().unbind();

		GL11.glPopMatrix();
	}

	@Override
	public void unload() {
		atlas.unload();
	}

	@Override
	protected void cleanup() {
		atlas.unload();

		// Delete gl objects
		GL11.glDeleteTextures(textureBuffer);
		GL11.glDeleteTextures(depthBuffer);
		GL30.glDeleteFramebuffers(frameBuffer);
	}
}
