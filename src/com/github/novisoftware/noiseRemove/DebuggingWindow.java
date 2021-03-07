package com.github.novisoftware.noiseRemove;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
*
*
* @author Shiro SADO (sh.sado@gmail.com)
*
*/
public class DebuggingWindow extends JFrame {
	static final int WINDOW_WIDTH = 1000;
	static final int WINDOW_HEIGHT = 500;

	ByteData byteData;
	int start;
	int skip = 1;

	DebuggingWindow(ByteData byteData) {
		this.byteData = byteData;

		this.start = 433780;
		this.setTitle("data debug");

		DisplayPanel panel = new DisplayPanel(this);
		panel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.add(panel);
		this.setSize(WINDOW_WIDTH + 20, WINDOW_HEIGHT + 20);
		this.setVisible(true);
		this.repaint();
	}

	void setStartPosition(int start) {
		this.start = start;
	}

	int getStartPosition() {
		return this.start;
	}

	class DisplayPanel extends JPanel implements MouseListener, MouseMotionListener {
		DebuggingWindow frame;

		DisplayPanel(DebuggingWindow frame) {
			this.frame = frame;
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, DebuggingWindow.WINDOW_WIDTH, DebuggingWindow.WINDOW_HEIGHT);

			g2.setColor(Color.GRAY);
			g2.fillRect(0, DebuggingWindow.WINDOW_HEIGHT /2 - 1, DebuggingWindow.WINDOW_WIDTH, 3);


			g2.setColor(Color.BLACK);

			int start = frame.start;
			int skip = frame.skip;

			for (int ch = 0; ch < 2; ch++) {
				int old_y = 0;
				for (int i=0 ; i < DebuggingWindow.WINDOW_WIDTH; i++) {
					int rawValue = 0;
					try {
						rawValue = - frame.byteData.getValue(ch, i * skip  + start);

					} catch(ArrayIndexOutOfBoundsException e) {
					}
					int y = (int)Math.round(
							rawValue / (0x1000000 /  DebuggingWindow.WINDOW_HEIGHT) * 4.5 + DebuggingWindow.WINDOW_HEIGHT / 2
							);
					if (i > 0) {
						g2.drawLine(i - 1, old_y, i, y);
					}

					old_y = y;
				}
			}
		}

		int mouseX_old;
		int mouseY_old;
		boolean isDragEnable = false;

		@Override
		public void mouseDragged(MouseEvent e) {
			System.out.print("x");
			if (isDragEnable) {
				System.out.print(".");
				int x = e.getX();
				int y = e.getY();
				int diffX = mouseX_old - x;
				int diffY = mouseY_old - y;
				mouseX_old = x;
				mouseY_old = y;

				frame.start += diffX;
				if (frame.start < 0) {
					frame.start = 0;
				}
				this.repaint();
			}
			System.out.print("-"+e.getButton());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				mouseX_old = e.getX();
				mouseY_old = e.getY();
				isDragEnable = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				isDragEnable = false;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}
	}

}
