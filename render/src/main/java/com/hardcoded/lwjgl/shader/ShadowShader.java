package com.hardcoded.lwjgl.shader;

import org.joml.Matrix4f;

/**
 * A shadow shader
 * 
 * @author HardCoded
 */
public class ShadowShader extends Shader {
	protected int load_mvpMatrix;
	
	public ShadowShader() {
		super(
			"/shaders/shadow/shadow_vertex.vs",
			"/shaders/shadow/shadow_fragment.fs"
		);
	}
	
	@Override
	protected void loadBinds() {
		bindAttrib(0, "in_Position");
	}
	
	@Override
	protected void loadUniforms() {
		load_mvpMatrix = getUniformLocation("mvpMatrix");
	}
	
	public void setMvpMatrix(Matrix4f mvpMatrix) {
		setMatrix4f(load_mvpMatrix, mvpMatrix);
	}
}
