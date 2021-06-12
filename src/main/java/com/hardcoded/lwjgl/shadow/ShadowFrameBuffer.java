package com.hardcoded.lwjgl.shadow;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.*;

import com.hardcoded.lwjgl.LwjglRender;

/**
 * The frame buffer for the shadow pass. This class sets up the depth texture
 * which can be rendered to during the shadow render pass, producing a shadow
 * map.
 * 
 * @author Karl <ThinMatrix> <https://www.youtube.com/channel/UCUkRj4qoT1bsWpE_C8lZYoQ>
 * @since v0.2
 */
public class ShadowFrameBuffer {
	private final int WIDTH;
	private final int HEIGHT;
	private int fbo;
	private int shadowMap;

	/**
	 * Initialises the frame buffer and shadow map of a certain size.
	 * 
	 * @param width
	 *            - the width of the shadow map in pixels.
	 * @param height
	 *            - the height of the shadow map in pixels.
	 */
	public ShadowFrameBuffer(int width, int height) {
		this.WIDTH = width;
		this.HEIGHT = height;
		initialiseFrameBuffer();
	}

	/**
	 * Deletes the frame buffer and shadow map texture when the game closes.
	 */
	protected void cleanUp() {
		GL30.glDeleteFramebuffers(fbo);
		GL11.glDeleteTextures(shadowMap);
	}

	/**
	 * Binds the frame buffer, setting it as the current render target.
	 */
	public void bindFrameBuffer() {
		bindFrameBuffer(fbo, WIDTH, HEIGHT);
	}

	/**
	 * Unbinds the frame buffer, setting the default frame buffer as the current
	 * render target.
	 */
	public void unbindFrameBuffer() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, LwjglRender.width, LwjglRender.height);
	}

	/**
	 * @return The ID of the shadow map texture.
	 */
	public int getShadowMap() {
		return shadowMap;
	}

	/**
	 * Creates the frame buffer and adds its depth attachment texture.
	 */
	private void initialiseFrameBuffer() {
		fbo = createFrameBuffer();
		shadowMap = createDepthBufferAttachment(WIDTH, HEIGHT);
		unbindFrameBuffer();
	}

	/**
	 * Binds the frame buffer as the current render target.
	 * 
	 * @param frameBuffer
	 *            - the frame buffer.
	 * @param width
	 *            - the width of the frame buffer.
	 * @param height
	 *            - the height of the frame buffer.
	 */
	private static void bindFrameBuffer(int frameBuffer, int width, int height) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, width, height);
	}

	/**
	 * Creates a frame buffer and binds it so that attachments can be added to
	 * it. The draw buffer is set to none, indicating that there's no colour
	 * buffer to be rendered to.
	 * 
	 * @return The newly created frame buffer's ID.
	 */
	private static int createFrameBuffer() {
		int frameBuffer = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glDrawBuffer(GL11.GL_NONE);
		GL11.glReadBuffer(GL11.GL_NONE);
		return frameBuffer;
	}

	/**
	 * Creates a depth buffer texture attachment.
	 * 
	 * @param width
	 *            - the width of the texture.
	 * @param height
	 *            - the height of the texture.
	 * @return The ID of the depth texture.
	 */
	private static int createDepthBufferAttachment(int width, int height) {
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL31.GL_CLAMP_TO_BORDER);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL31.GL_CLAMP_TO_BORDER);
		GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL31.GL_TEXTURE_BORDER_COLOR, new float[] { 1, 1, 1, 1 });
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture, 0);
		return texture;
	}
}
