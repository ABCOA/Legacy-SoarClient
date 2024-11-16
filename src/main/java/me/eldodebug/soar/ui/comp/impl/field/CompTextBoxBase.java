package me.eldodebug.soar.ui.comp.impl.field;

import org.lwjgl.input.Keyboard;

import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.IOUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;

public class CompTextBoxBase extends Comp {

	private float width, height;
	
	private String text;
	private boolean focused;
	private int cursorPosition;
	private int selectionEnd;
	
	private int maxStringLength;
	
	public CompTextBoxBase(float x, float y, float width, float height) {
		super(x, y);
		this.width = width;
		this.height = height;
		this.focused = false;
		this.text = "";
		this.maxStringLength = 256;
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		boolean flag = MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		
		this.setFocused(flag);
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(!focused) {
			return;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_C) {
			IOUtils.copyStringToClipboard(this.getSelectedText());
            return;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_V) {
			writeText(IOUtils.getStringFromClipboard());
            return;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_X) {
			IOUtils.copyStringToClipboard(this.getSelectedText());
			this.writeText("");
            return;
		}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && keyCode == Keyboard.KEY_A) {
            this.setCursorPosition(this.text.length());
            this.setSelectionPos(0);
            return;
		}else {
			
			switch(keyCode) {
				case Keyboard.KEY_BACK:
					if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
						this.deleteWords(-1);
					}else {
						this.deleteFromCursor(-1);
					}
					return;
				case Keyboard.KEY_HOME:
					if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						this.setSelectionPos(0);
					}else {
						this.setCursorPosition(0);
					}
					return;
				case Keyboard.KEY_LEFT:
					if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
							this.setSelectionPos(this.getNthWordFromPos(-1, this.selectionEnd));
						}else {
							this.setSelectionPos(this.selectionEnd - 1);
						}
					}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
						this.setCursorPosition(this.getNthWordFromCursor(-1));
					}else {
						this.moveCursorBy(-1);
					}
					return;
				case Keyboard.KEY_RIGHT:
					if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
							this.setSelectionPos(this.getNthWordFromPos(1, this.selectionEnd));
						}else {
							this.setSelectionPos(this.selectionEnd + 1);
						}
					}else if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
						this.setCursorPosition(this.getNthWordFromCursor(1));
					}else {
						this.moveCursorBy(1);
					}
					return;
				case Keyboard.KEY_END:
					if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
						this.setSelectionPos(this.text.length());
					}else {
						this.setCursorPosition(this.text.length());
					}
					return;
				case Keyboard.KEY_DELETE:
					if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
						this.deleteWords(1);
					}else {
						this.deleteFromCursor(1);
					}
					return;
				default:
					if(ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
						this.writeText(Character.toString(typedChar));
					}
					return;
			}
		}
	}
	
	private void writeText(String text) {
		
		String s = "";
		String s1 = ChatAllowedCharacters.filterAllowedCharacters(text);
		int min = Math.min(this.cursorPosition, this.selectionEnd);
		int max = Math.max(this.cursorPosition, this.selectionEnd);
		int len = this.maxStringLength - this.text.length() - (min - max);
		int l;
		
		if(this.text.length() > 0) {
			s = s + this.text.substring(0, min);
		}
		
		if(len < s1.length()) {
			s = s + s1.substring(0, len);
			l = len;
		} else {
			s = s + s1;
			l = s1.length();
		}
		
		if(this.text.length() > 0 && max < this.text.length()) {
			s = s + this.text.substring(max);
		}
		
		this.text = s;
		this.moveCursorBy(min - this.selectionEnd + l);
	}
	
	private void deleteWords(int num) {
		if(this.text.length() != 0) {
			if(this.selectionEnd != this.cursorPosition) {
				this.writeText("");
			}else {
				this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
			}
		}
	}
	
	private String getSelectedText() {
		
		int min = Math.min(this.cursorPosition, this.selectionEnd);
		int max = Math.max(this.cursorPosition, this.selectionEnd);
		
		return this.text.substring(min, max);
	}
	
	private void deleteFromCursor(int num) {
		
		if(this.text.length() != 0) {
			
			if(this.selectionEnd != this.cursorPosition) {
				this.writeText("");
			}else {
				
				boolean negative = num < 0;
				int i = negative ? this.cursorPosition + num : this.cursorPosition;
				int j = negative ? this.cursorPosition : this.cursorPosition + num;
				String s = "";
				
				if(i > 0) {
					s = this.text.substring(0, i);
				}
				
				if(j < this.text.length()) {
					s = s + this.text.substring(j);
				}
				
				this.text = s;
				
				if(negative) {
					this.moveCursorBy(num);
				}
			}
		}
	}
	
	private int getNthWordFromCursor(int num) {
		return getNthWordFromPos(num, this.cursorPosition);
	}
	
	private int getNthWordFromPos(int num, int pos) {
		
        int i = pos;
        boolean negative = num < 0;
        int j = Math.abs(num);

        for (int k = 0; k < j; ++k) {
            if (!negative) {
                int l = this.text.length();
                i = this.text.indexOf(32, i);

                if (i == -1) {
                    i = l;
                } else {
                    while (i < l && this.text.charAt(i) == 32) {
                        ++i;
                    }
                }
            } else {
                while (i > 0 && this.text.charAt(i - 1) == 32) {
                    --i;
                }

                while (i > 0 && this.text.charAt(i - 1) != 32) {
                    --i;
                }
            }
        }

        return i;
	}
	
	private void moveCursorBy(int i) {
		this.setCursorPosition(this.selectionEnd + i);
	}
	
	private void setCursorPosition(int i) {
		
		this.cursorPosition = i;
		
		int len = this.text.length();
		
		this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, len);
		this.setSelectionPos(this.cursorPosition);
	}
	
	private void setSelectionPos(int selectionPos) {
		
		int len = this.text.length();
		
		if(selectionPos > len) {
			selectionPos = len;
		}
		
		if(selectionPos < 0) {
			selectionPos = 0;
		}
		
		this.selectionEnd = selectionPos;
	}
	
	public void setPosition(float x, float y, float width, float height) {
		this.setX(x);
		this.setY(y);
		this.width = width;
		this.height = height;
	}
	
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.setCursorPosition(this.getText().length());
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public int getSelectionEnd() {
		return selectionEnd;
	}

	public void setSelectionEnd(int selectionEnd) {
		this.selectionEnd = selectionEnd;
	}

	public int getMaxStringLength() {
		return maxStringLength;
	}

	public void setMaxStringLength(int maxStringLength) {
		this.maxStringLength = maxStringLength;
	}

	public int getCursorPosition() {
		return cursorPosition;
	}
}
