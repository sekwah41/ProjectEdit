package com.hardcoded.render.gui;

import org.lwjgl.glfw.GLFW;

import com.hardcoded.lwjgl.input.Input;
import com.hardcoded.render.gui.GuiListener.GuiEvent.GuiKeyEvent;
import com.hardcoded.render.gui.GuiListener.GuiEvent.GuiMouseEvent;

/**
 * If the class extends this it will expect user unput
 */
public interface GuiListener {
	void onMouseEvent(GuiMouseEvent event);
	void onKeyEvent(GuiKeyEvent event);
	
	
	public static interface GuiEvent {
		abstract class GuiMouseEvent implements GuiEvent {
			private final int button;
			private final int action;
			private final int modifiers;
			private final float scrollAmount;
			private final float x;
			private final float y;
			private final Object customData;
			private boolean consumed;
			
			public GuiMouseEvent(float x, float y, float scrollAmount, int button, int action, int modifiers, Object customData) {
				this.x = x;
				this.y = y;
				this.button = button;
				this.scrollAmount = scrollAmount;
				this.action = action;
				this.modifiers = modifiers;
				this.customData = customData;
			}
			
			public float getX() {
				return x;
			}
			
			public float getY() {
				return y;
			}
			
			public float getScrollAmount() {
				return scrollAmount;
			}
			
			public int getModifiers() {
				return modifiers;
			}
			
			public int getButton() {
				return button;
			}
			
			public int getAction() {
				return action;
			}
			
			public boolean isMouseDown(int key) {
				return Input.isMouseDown(key);
			}
			
			@Override
			public void consume() {
				consumed = true;
			}
			
			@Override
			public boolean isConsumed() {
				return consumed;
			}
			
			@Override
			public Object getCustomData() {
				return customData;
			}
			
			public boolean isInside(GuiComponent comp) {
				return isInside(comp.getX(), comp.getY(), comp.getWidth(), comp.getHeight());
			}
			
			public boolean isInside(float x, float y, float w, float h) {
				return !(this.x <= x || this.y <= y || this.x > x + w || this.y > y + h);
			}
			
			@Override
			public String toString() {
				return String.format("MouseEvent[x=%.4f,y=%.4f,scrollAmount=%.4f,button=%d,action=%d,modifiers=%d,consumed=%s]", x, y, scrollAmount, button, action, modifiers, consumed);
			}
		}
		
		class GuiMouseScroll extends GuiMouseEvent {
			public GuiMouseScroll(float x, float y, float scrollAmount, int modifiers, Object customData) {
				super(x, y, scrollAmount, 0, 0, modifiers, customData);
			}
		}
		
		class GuiMouseMove extends GuiMouseEvent {
			public GuiMouseMove(float x, float y, int modifiers, Object customData) {
				super(x, y, 0, 0, 0, modifiers, customData);
			}
		}
		
		class GuiMouseDrag extends GuiMouseEvent {
			public GuiMouseDrag(float x, float y, int buttons, int modifiers, Object customData) {
				super(x, y, 0, buttons, GLFW.GLFW_REPEAT, modifiers, customData);
			}
		}
		
		class GuiMousePress extends GuiMouseEvent {
			public GuiMousePress(float x, float y, int button, int action, int modifiers, Object customData) {
				super(x, y, 0, button, action, modifiers, customData);
			}
		}
		
		class GuiKeyEvent implements GuiEvent {
			private final int action;
			private final int keyCode;
			private final int modifiers;
			private boolean consumed;
			
			public GuiKeyEvent(int keyCode, int action, int modifiers) {
				this.keyCode = keyCode;
				this.action = action;
				this.modifiers = modifiers;
			}
			
			public int getKeyCode() {
				return keyCode;
			}
			
			public int getAction() {
				return action;
			}
			
			@Override
			public int getModifiers() {
				return modifiers;
			}
			
			@Override
			public Object getCustomData() {
				return null;
			}
			
			@Override
			public void consume() {
				consumed = true;
			}
			
			@Override
			public boolean isConsumed() {
				return consumed;
			}
			
			@Override
			public String toString() {
				return String.format("KeyEvent[keyCode=%d,action=%d,modifiers=%d,consumed=%s]", keyCode, action, modifiers, consumed);
			}
		}
		
		void consume();
		
		/**
		 * Returns {@code true} if this event has been consumed
		 * @return
		 */
		boolean isConsumed();
		
		/**
		 * Returns the current key modifiers.
		 * @see GLFW#GLFW_MOD_SHIFT
		 */
		int getModifiers();
		
		/**
		 * Custom data
		 */
		Object getCustomData();
	}
}
