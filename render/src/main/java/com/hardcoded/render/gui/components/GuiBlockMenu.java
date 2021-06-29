package com.hardcoded.render.gui.components;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.hardcoded.lwjgl.LwjglWindow;
import com.hardcoded.lwjgl.input.InputMask;
import com.hardcoded.mc.general.world.BlockDataManager;
import com.hardcoded.mc.general.world.IBlockData;
import com.hardcoded.render.gui.GuiComponent;
import com.hardcoded.render.gui.GuiListener;
import com.hardcoded.render.gui.GuiListener.GuiEvent.*;
import com.hardcoded.render.gui.GuiRender;

public class GuiBlockMenu extends GuiComponent implements GuiListener {
	private List<GuiBlockListItem> menuItem;
	private GuiRender gui;
	
	public GuiBlockMenu(GuiRender gui) {
		this.gui = gui;
		
		List<IBlockData> blocks = new ArrayList<>(BlockDataManager.getStates());
		blocks.sort((s1, s2) -> {
			return s1.getName().compareTo(s2.getName());
		});
		
		menuItem = new ArrayList<>();
		for(int i = 0, len = blocks.size(); i < len; i++) {
			GuiBlockListItem comp = new GuiBlockListItem().setBlock(blocks.get(i));
			menuItem.add(comp);
		}
		
		setSize(256, 64 * 10);
	}
	
	private float scroll_amount;
	private boolean has_scroll_mouse;
	private boolean has_resize_mouse;
	private boolean mouse_hover_scroll;
	
	public int getBlockScreenWidth() {
		return getWidth() - 48;
	}
	
	public int getBlockScreenHeight() {
		return getHeight() - 56;
	}
	
	private int getBlockMenuHeight() {
		int xb = getBlockScreenWidth() / 64;
		int rows = (menuItem.size() + xb - 1) / xb;
		return rows;
	}
	
	private float getScroll() {
		return scroll_amount / (getHeight() - 48.0f);
	}
	
	@Override
	public void onMouseEvent(GuiMouseEvent event) {
		int width = getWidth();
		int height = getHeight();
		int x = getX();
		int y = getY();
		
		if(event.isInside(this)) {
			event.consume();
			
			if(event instanceof GuiMouseScroll) {
				scroll_amount -= event.getScrollAmount() * 4;
			}
		} else {
			
		}
		
		if(event.isInside(x + width - 32, y + 8 + scroll_amount, 24, 32)) {
			mouse_hover_scroll = true;
		} else {
			mouse_hover_scroll = false;
		}
		
		if(event.isMouseDown(GLFW.GLFW_MOUSE_BUTTON_1)) {
			if(has_resize_mouse || has_scroll_mouse) {
				
			} else {
				if(mouse_hover_scroll) {
					has_scroll_mouse = true;
				}
				
				if(event.isInside(x + width - 8, y + height - 8, 8, 8)) {
					has_resize_mouse = true;
				}
			}
		} else {
			has_scroll_mouse = false;
			has_resize_mouse = false;
		}
		
		if(has_resize_mouse) {
			float mx = event.getX() - x + 4;
			float my = event.getY() - y + 4;
			
			width = (int)mx;
			height = (int)my;
			if(width < 112) {
				width = 112;
			}
			
			if(height < 120) {
				height = 120;
			}
			
			setSize(width, height);
		} else if(has_scroll_mouse) {
			scroll_amount = event.getY() - 24 - y;
		}
		
		if(scroll_amount > height - 48) scroll_amount = height - 48;
		if(scroll_amount < 0) scroll_amount = 0;

		boolean mouse_inside_block_screen = event.isInside(getX() + 8, getY() + 48, getBlockScreenWidth(), getBlockScreenHeight());
		
		if(mouse_inside_block_screen) {
			if(!(has_resize_mouse || has_scroll_mouse)) {
				if(event instanceof GuiMouseMove
				|| event instanceof GuiMouseScroll) {
					if(mouse_inside_block_screen) {
						callOnScreenItems((item, idx, c_xp, c_yp) -> {
							item.hover = false;
							if(event.isInside(c_xp, c_yp, 64, 64)) {
								item.hover = true;
							}
						});
					}
				}
				
				if(event instanceof GuiMousePress
				&& event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1
				&& event.getAction() == GLFW.GLFW_PRESS) {
					for(GuiBlockListItem item : menuItem) {
						item.selected = false;
						item.hover = false;
					}
					
					callOnScreenItems((item, idx, c_xp, c_yp) -> {
						item.selected = false;
						if(event.isInside(c_xp, c_yp, 64, 64)) {
							item.selected = true;
							gui.selectedBlock = item.getBlock();
						}
					});
				}
			}
		}
	}

	@Override
	public void onKeyEvent(GuiKeyEvent event) {
		
	}
	
	@FunctionalInterface
	private interface IItemConsumer {
		void apply(GuiBlockListItem item, int idx, float x, float y);
	}
	
	private void callOnScreenItems(IItemConsumer consumer) {
		int height = getHeight();
		int x = getX();
		int y = getY();
		
		int menuItems = menuItem.size();
		
		int block_screen_width = getBlockScreenWidth();
		int block_screen_height = getBlockScreenHeight();
		int xt = Math.max(1, block_screen_width / 64);
		int yt = Math.max(1, block_screen_height / 64);
		int rows = getBlockMenuHeight();
		
		float scroll = getScroll();
		float ys = scroll * rows - yt;
		
		int is = Math.max((int)ys - 1, 0);
		
		int pixel_height = rows * 64 - block_screen_height;
		float scroll_px = scroll * pixel_height;
		
		for(int i = is;; i++) {
			float p_yp = y + 48 + i * 64 - scroll_px;
			if(p_yp > y + height - 8) break;
			if(p_yp < y - 24) continue;
			
			for(int j = 0; j < xt; j++) {
				float p_xp = x + 8 + j * 64;
				
				int idx = i * xt + j;
				if(idx >= menuItems) return;
				
				consumer.apply(menuItem.get(idx), idx, p_xp, p_yp);
			}
		}
	}
	
	@Override
	public void renderComponent() {
		int width = getWidth();
		int height = getHeight();
		int x = getX();
		int y = getY();
		
		if(!LwjglWindow.isMouseCaptured()) {
			InputMask.addEventMask(x, y, width, height, this);
		}
		
		int block_screen_width = getBlockScreenWidth();
		int block_screen_height = getBlockScreenHeight();
		int xt = Math.max(1, block_screen_width / 64);
		
		GL11.glColor4f(0.1f, 0.1f, 0.1f, 1);
		renderBox(x, y, width, height);
		
		GL11.glColor4f(0.3f, 0.3f, 0.3f, 1);
		renderBox(x + 8, y + 8, width - 48, 32);
		renderBox(x + width - 32, y + 8, 24, height - 16);
		
		GL11.glColor4f(0.6f, 0.6f, 0.6f, 1);
		if(has_scroll_mouse) {
			GL11.glColor4f(0.8f, 0.8f, 0.8f, 1);
		} else if(mouse_hover_scroll) {
			GL11.glColor4f(0.7f, 0.7f, 0.7f, 1);
		}
		
		renderBox(x + width - 32, y + 8 + scroll_amount, 24, 32);
		
		if(has_resize_mouse) {
			GL11.glColor4f(0.9f, 0.9f, 0.9f, 1);
		} else {
			GL11.glColor4f(0.7f, 0.7f, 0.7f, 1);
		}
		renderBox(x + width - 8, y + height - 8, 8, 8);
		
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
		GL11.glEnable(GL11.GL_STENCIL_TEST);
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		GL11.glStencilMask(0xFF);
		GL11.glColor4f(0.7f, 0.7f, 0.7f, 1);
		renderBox(x + 8, y + 48, block_screen_width, block_screen_height);
		
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		GL11.glStencilMask(0xFF);
		
		callOnScreenItems((comp, idx, c_xp, c_yp) -> {
			{
				if((xt & 1) == 0) {
					idx += (idx / xt);
				}
				
				if((idx & 1) == 0) {
					GL11.glColor4f(0.9f, 0.9f, 0.9f, 1);
				} else {
					GL11.glColor4f(0.7f, 0.7f, 0.7f, 1);
				}
				renderBox((int)c_xp, (int)c_yp, 64, 64);
			}
			
			comp.setLocation((int)c_xp, (int)c_yp);
			comp.renderComponent();
		});
		
		GL11.glDisable(GL11.GL_STENCIL_TEST);
	}
	
	@Override
	public GuiBlockMenu setSize(int width, int height) {
		super.setSize(width, height);
		return this;
	}
}
