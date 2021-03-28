package com.github.novisoftware.noiseRemove;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.github.novisoftware.noiseRemove.Main.FoundNoise;

/**
*
*
* @author Shiro SADO (sh.sado@gmail.com)
*
*/
public class DebuggingWindow extends JFrame implements ComponentListener {
	int WINDOW_WIDTH = 1000;
	int WINDOW_HEIGHT = 500;

	final Main mainObj;

	int start;
	/**
	 * 描画時のスキップ間隔
	 */
	int skip = 1;

	// 微分・2階微分の表示
	int diff_start;
	int diff_width;

	int diff[][];
	int diff2[][];
	// 自己相関
	double autoCorrelation[][];
	int ac_n_data;
	int ac_shift;

	// noiseList

	DebuggingWindow(Main n, String title) {
		this.mainObj = n;
		this.start = 433780;
		this.setTitle("[data debug] " + title);

		DisplayPanel panel = new DisplayPanel(this);
		panel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		this.add(panel);
		this.addComponentListener(this);
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

	@Override
	public void componentResized(ComponentEvent e) {
		Rectangle r = e.getComponent().getBounds();
		this.WINDOW_HEIGHT = r.height;
		this.WINDOW_WIDTH = r.width;
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}


	class DisplayPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
		DebuggingWindow frame;
		final Font font = new Font(Font.SANS_SERIF, Font.BOLD, 30);
		final int N_CHANNELS = 2;

		DisplayPanel(DebuggingWindow frame) {
			this.frame = frame;
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
			this.frame.addKeyListener(this);
		}

		private final Color DIFF_COLOR = new Color(0.4f, 0.4f, 1f);
		private final int diffToY(int diff, int window_height) {
			return (int)Math.round(
					diff * 0.005  + window_height / 2
					);
		}

		private final Color DIFF2_COLOR = new Color(1f, 0.4f, 0.4f);
		private final int diff2ToY(int diff, int window_height) {
			return (int)Math.round(
					diff * 0.001  + window_height / 2
					);
		}


		private final Color AC_COLOR = new Color(0.3f, 0.8f, 0.3f);
		private final int AcToY(double ac, int window_height) {
			return (int)Math.round(
					- ac * ( window_height * 20 )  + window_height / 2
					);
		}

		final double square(double x) {
			return x * x;
		}

		final int CH_DRAW_OFFSET = 100;

		@Override
		public void paint(Graphics g) {
			int start = frame.start;
			int skip = frame.skip;
			int window_width =  frame.WINDOW_WIDTH;
			int window_height = frame.WINDOW_HEIGHT;
			ByteData b_new = frame.mainObj.byteData;

			// オリジナルの波形
			ByteData b;
			if (mainObj.debug_orgByteData != null) {
				b = mainObj.debug_orgByteData;
			}
			else {
				b = frame.mainObj.byteData;
			}

			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, window_width, window_height);

			// 横軸
			g2.setColor(Color.GRAY);
			for (int ch = 0; ch < N_CHANNELS; ch++) {

				g2.fillRect(0, ch * CH_DRAW_OFFSET + window_height /2 - 1, window_width, 3);
			}

			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			double secs = 1.0 * start / mainObj.nSamplesPerSec;
			int mm = (int) Math.floor( secs / 60 );
			double ss = secs - 60 * mm;
			String printMMSS = String.format("%02d:%06.3f", mm, ss);
			g2.setFont(font);
			g2.setColor(Color.GRAY);
			g2.drawString(printMMSS + " offset = " + start, 10, 40);

			// 修正後の波形を描画
			if (mainObj.debug_orgByteData != null) {
				g2.setColor(Color.RED);
			}
			else {
				g2.setColor(Color.BLACK);
			}
			// 修正後の波形を描画
			for (int ch = 0; ch < N_CHANNELS; ch++) {
				int old_y = 0;
				for (int i=0 ; i < window_width; i++) {
					int rawValue = 0;
					try {
						rawValue = - b_new.getValue(ch, i * skip  + start);

					} catch(ArrayIndexOutOfBoundsException e) {
					}
					int y = (int)Math.round(
							rawValue * 1.0 / (0x1000000 /  window_height) * 4.5 + window_height / 2
							);
					if (i > 0) {
						g2.drawLine(i - 1, ch * CH_DRAW_OFFSET + old_y, i, ch * CH_DRAW_OFFSET + y);
					}

					old_y = y;
				}
			}

			// 修正前の波形を描画
			if (mainObj.debug_orgByteData != null) {
				ByteData b_org = mainObj.debug_orgByteData;

				g2.setColor(Color.BLACK);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					int old_y = 0;
					for (int i=0 ; i < window_width; i++) {
						int rawValue = 0;
						try {
							rawValue = - b_org.getValue(ch, i * skip  + start);

						} catch(ArrayIndexOutOfBoundsException e) {
						}
						int y = (int)Math.round(
								rawValue * 1.0 / (0x1000000 /  window_height) * 4.5 + window_height / 2
								);
						if (i > 0) {
							g2.drawLine(i - 1, ch * CH_DRAW_OFFSET + old_y, i, ch * CH_DRAW_OFFSET + y);
						}

						old_y = y;
					}
				}
			}


			// ドラッグ中以外
			if (! isDragEnable) {
				if (frame.diff_start != start || frame.diff_width != window_width) {
					frame.diff_start = start;
					frame.diff_width = window_width;

					diff = new int[N_CHANNELS][window_width * skip];
					diff2 = new int[N_CHANNELS][window_width * skip];

					for (int ch = 0; ch < N_CHANNELS; ch++) {
						int old_y = 0;
						int old_yy = 0;
						for (int i=0 ; i < window_width * skip + 2; i++) {
							int rawValue = 0;
							try {
								rawValue = - b.getValue(ch, i * skip  + start);

							} catch(ArrayIndexOutOfBoundsException e) {
							}
							if (i > 1 && i < window_width * skip + 1) {
								diff[ch][i - 1] = rawValue - old_y;
							}
							if (i > 2) {
								diff2[ch][i - 2] = rawValue - old_y - old_yy;
							}

							old_yy = rawValue - old_y;
							old_y = rawValue;
						}
					}

					int window = 5;
					ac_n_data = window_width * skip;
					ac_shift = ac_n_data / window;
					autoCorrelation = new double[N_CHANNELS][ac_n_data - ac_shift];

					// 微分フィルタ通過後の値に対する自己相関
					for (int ch = 0; ch < N_CHANNELS; ch++) {
						for (int shift=0 ; shift < ac_shift ; shift++) {
							double sum0 = 0;
							double sum1 = 0;
							double sum2 = 0;

							for (int i=0 ; i < ac_n_data - ac_shift ; i++) {
								sum0 += 1.0 * diff[ch][i] * diff[ch][i + shift];
								sum1 += square(diff[ch][i]);
								sum2 += square(diff[ch][i+shift]);
							}
							double divisor = Math.sqrt(sum1) * Math.sqrt(sum2);
							if (divisor == 0) {
								// 全て0の場合。
								autoCorrelation[ch][shift] = 0;
								System.out.println( " zero: " + shift);
							}
							else {
								autoCorrelation[ch][shift] = (1.0 / ac_shift) * sum0 / divisor;
							}
						}
					}
				}

				g2.setColor(DIFF2_COLOR);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					try {
						g2.drawString("ch" + ch + ": ddx/ddt = " + diff[ch][this.moveMouseX], 500, 40 + ch * 40);
					} catch(ArrayIndexOutOfBoundsException e) {
					}
				}

				g2.setColor(Color.GRAY);
				g2.drawLine(this.moveMouseX, 0, this.moveMouseX, window_height-1);

				// 最大
				int[] maxAc = new int[N_CHANNELS];
				double[] maxAcVal = new double[N_CHANNELS];
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					int processPhase = 0;
					double max = 0;
					for (int i=0 ; i < ac_n_data - ac_shift ; i++) {
						if (processPhase == 0) {
							if (autoCorrelation[ch][i] < 0) {
								processPhase = 1;
							}
						} else if (processPhase == 1) {
							if (autoCorrelation[ch][i] > 0) {
								processPhase = 2;
								max = autoCorrelation[ch][i];
							}
						} else if (processPhase == 2) {
							if (autoCorrelation[ch][i] > max) {
								max = autoCorrelation[ch][i];
								maxAcVal[ch] = autoCorrelation[ch][i];
								maxAc[ch] = i;
							}
							else if (autoCorrelation[ch][i] < max) {
								break;
							}
						}
					}
				}

				g2.setColor(AC_COLOR);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					g2.drawString("ch" + ch + ": estimated wavelength = " + maxAc[ch], 10, 70 + ch * 40);
				}

				g2.setColor(DIFF_COLOR);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					for (int i=1 ; i < window_width * skip; i++) {
						g2.drawLine(
								i-1, ch * CH_DRAW_OFFSET + diffToY(diff[ch][i*skip-1], window_height),
								i, ch * CH_DRAW_OFFSET + diffToY(diff[ch][i*skip], window_height)
								);
					}
				}
				g2.setColor(DIFF2_COLOR);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					for (int i=1 ; i < window_width * skip; i++) {
						g2.drawLine(
								i-1, ch * CH_DRAW_OFFSET + diff2ToY(diff2[ch][i*skip-1], window_height),
								i, ch * CH_DRAW_OFFSET + diff2ToY(diff2[ch][i*skip], window_height)
								);
					}
				}

				g2.setColor(AC_COLOR);
				for (int ch = 0; ch < N_CHANNELS; ch++) {
					int i_adder = ac_shift / 2;
					for (int i=1 ; i < ac_n_data - ac_shift; i++) {
						g2.drawLine(
								i_adder + i-1, ch * CH_DRAW_OFFSET + AcToY(autoCorrelation[ch][i-1], window_height),
								i_adder + i, ch * CH_DRAW_OFFSET + AcToY(autoCorrelation[ch][i], window_height));
					}
				}

				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				Stroke s = new BasicStroke( 2 );
				g2.setStroke(s);

				// 検出されたノイズ
				ArrayList<FoundNoise> noiseList = frame.mainObj.noiseList;
				if (noiseList != null) {
					for(FoundNoise n : noiseList) {
						if (  (start < n.start &&  n.start < start + window_width)
								|| (start < n.start + n.length && n.start + n.length < start + window_width))  {
							if (n.ch == 0) {
								g2.setColor(Color.RED);
							}
							else {
								g2.setColor(Color.BLUE);
							}
							final int RECT_HEIGHT = 160;
							final int RECT_ROUND = 30;
							g2.drawRoundRect(n.start - start,
									n.ch * CH_DRAW_OFFSET + window_height / 2 - RECT_HEIGHT,
									n.length, RECT_HEIGHT*2, RECT_ROUND, RECT_ROUND);
						}
					}
				}
			}
		}

		int moveMouseX;
		int moveMouseY;

		int mouseX_old;
		int mouseY_old;
		boolean isDragEnable = false;

		@Override
		public void mouseDragged(MouseEvent e) {
			if (isDragEnable) {
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
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			if (! isDragEnable) {
				moveMouseX = e.getX();
				moveMouseY = e.getY();

				this.repaint();
			}
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
				this.repaint();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			// none
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// none
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// none
		}

		@Override
		public void keyPressed(KeyEvent e) {

			System.out.println("ev caught");

			int vk = e.getKeyCode();

			if (vk == KeyEvent.VK_RIGHT) {
				frame.start += frame.WINDOW_WIDTH;
				this.repaint();
			}
			else if(vk == KeyEvent.VK_LEFT) {
				frame.start -= frame.WINDOW_WIDTH;
				this.repaint();
			}
			// none
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// none
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// none
		}
	}
}
