package graphics;

import utility.Color;
import utility.Vec2;
import utility.maths;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Fluid extends JPanel {
	private static final long serialVersionUID = 1L;

	JFrame jf = new JFrame();
	static Color bg = Color.black, fg = Color.white;
	boolean fullscreen = false;
	BufferedImage img;
	public static ImageObserver iObs;

	int size = 72;
	static final int crop = 1;
	int drawSize = 10;
	int upResFac = 1;
	float halfSize = drawSize / 2f;
	static final int iter = 4;
	static final int steps = 1;
	float ms = 8;

	Vec2 mOld = Vec2.inv;
	Vec2 mNew = Vec2.inv;
	float vAddScale = 7;

	float visc = 0;
	float diff = 1;

	boolean running = true;

	int drawW = size * drawSize;
	int drawH = size * drawSize;

	float tps = 30;
	float fade = 0;
	float addIntensity;

	Color dyeCol = Color.white;

	boolean vectorView = false;
	boolean changeCol = false;

	Random r = new Random(System.currentTimeMillis());

	Vec2[][] v;
	Color[][] s;

	Vec2[][] newV;
	Color[][] newS;

	public Fluid(int sz, int drawS, float visc, float diff, float ticksPerSec, float fadeVal, boolean fullscreen) {
		this.size = sz;
		this.drawSize = drawS;
		this.visc = visc;
		this.diff = diff;
		this.fullscreen = fullscreen;
		this.halfSize = drawSize / 2f;
		this.tps = ticksPerSec;
		this.fade = fadeVal;
		iObs = new ImageObserver() {
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
				return false;
			}
		};
		addIntensity = 600f;
		drawW = drawH = (size - 2 * crop) * drawS;
		img = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_RGB);
		v = new Vec2[size][size];
		s = new Color[size][size];
		newV = new Vec2[size][size];
		newS = new Color[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
//				v[i][j] = new Vec2((float) Math.random() * drawSize - halfSize,
//						(float) Math.random() * drawSize - halfSize);
				v[i][j] = Vec2.zero;
				newV[i][j] = v[i][j];
//				if (i < j)
//					s[i][j] = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
//				else
				s[i][j] = Color.black;
				newS[i][j] = s[i][j];
			}
		}
		setBackground(bg.toAwt());
		setForeground(fg.toAwt());
		jf.setBackground(bg.toAwt());
		jf.add(this);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if (fullscreen)
			jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
		else
			jf.setSize(drawW, drawH);
//		System.out.println(jf.getSize());
		jf.setUndecorated(true);

		jf.setBackground(getBackground());
		jf.setVisible(true);

		jf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent k) {
				if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
					running = false;
				}
				if (k.getKeyCode() == KeyEvent.VK_SPACE) {
					vectorView = !vectorView;
				}
				if (k.getKeyCode() == KeyEvent.VK_C) {
					changeCol = true;
				}
			}
		});

		jf.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent m) {
				Point loc = m.getPoint();
				mNew = new Vec2(loc.x * 1f / drawSize + crop, loc.y * 1f / drawSize + crop);
				float yDiff = mNew.y - mOld.y, xDiff = mNew.x - mOld.x, slope = yDiff / xDiff, slopeRec = xDiff / yDiff;
				boolean[][] through = new boolean[(int) Math.abs(yDiff) + 2][(int) Math.abs(xDiff) + 2];
				int yMin = Math.min((int) mOld.y, (int) mNew.y), xMin = Math.min((int) mOld.x, (int) mNew.x),
						xMax = Math.max((int) mOld.x, (int) mNew.x), yMax = Math.max((int) mOld.y, (int) mNew.y);
				Vec2 vel = new Vec2(xDiff, yDiff).multiply(vAddScale);
				if (!mOld.equals(Vec2.inv) && (xMin > 1) && (yMin > 1) && (yMax < size - 1) && (xMax < size - 1)) {
					for (int i = yMin; i < yMax; i++) {
						int x = (int) (slopeRec * (i - mOld.y) + mOld.x);
						for (int k = i - yMin; k <= i + 2 - yMin; k++) {
							for (int l = x - xMin; l <= x + 2 - xMin; l++) {
								if (k > 0 && k < through.length && l > 0 && l < through[0].length)
									through[k][l] = true;
							}
						}
					}
					for (int j = xMin; j < xMax; j++) {
						int y = (int) (slope * (j - mOld.x) + mOld.y);
						for (int k = (j - xMin); k <= j + 1 - xMin; k++)
							for (int l = y - yMin; l <= y + 1 - yMin; l++)
								if (k > 0 && k < through.length && l > 0 && l < through[0].length)
									through[k][l] = true;
					}
					for (int i = -1; i < through.length - 1; i++) {
						for (int j = -1; j < through[0].length - 1; j++) {
							if (through[i + 1][j + 1]) {
								addDye((int) maths.clamp(1, xMin + j, size - 2),
										(int) maths.clamp(1, yMin + i, size - 2), dyeCol.multiply(addIntensity));
								addVel((int) maths.clamp(1, xMin + j, size - 2),
										(int) maths.clamp(1, yMin + i, size - 2), vel);
							}
						}
					}

					s = diffCol(s, s);
				}
				mOld = mNew;
			}

		});

		jf.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent m) {
				mOld = Vec2.inv;
			}

		});
	}

	void addDye(int x, int y, Color col) {
		s[y][x] = s[y][x].add(col);
	}

	void addVel(int x, int y, Vec2 vel) {
		v[y][x] = v[y][x].add(vel);
	}

	void fade() {
		for (int i = 0; i < (size); i++) {
			for (int j = 0; j < (size); j++) {
				float cFade = -fade / tps * ms, vFade = fade / tps * ms * drawSize * vAddScale / addIntensity;
				float vX = v[i][j].x, vY = v[i][j].y;
				Color c = s[i][j];
				if (vX != 0) {
					if (Math.abs(vX) > vFade)
						vX = vX > 0 ? vX - vFade : vX + vFade;
					else
						vX = 0;
					v[i][j].x = vX;
				}
				if (vY != 0) {
					if (Math.abs(vY) > vFade)
						vY = vY > 0 ? vY - vFade : vY + vFade;
					else
						vY = 0;
					v[i][j].y = vY;
				}
				if (!c.equals(Color.black)) {
					c = c.add(new Color(cFade, cFade, cFade));
					c = Color.clamp(0, c, 1);
					s[i][j] = c;
				}
			}
		}
	}

	float[][] setBnd(float[][] fl) {
		for (int i = 1; i < size - 1; i++) {
			fl[i][0] = fl[i][1];
			fl[i][size - 1] = fl[i][size - 2];
		}
		for (int j = 1; j < size - 1; j++) {
			fl[0][j] = fl[1][j];
			fl[size - 1][j] = fl[size - 2][j];
		}

		fl[0][0] = 0.5f * (fl[0][1] + fl[1][0]);
		fl[0][size - 1] = 0.5f * (fl[0][size - 2] + fl[1][size - 1]);
		fl[size - 1][0] = 0.5f * (fl[size - 2][0] + fl[size - 1][1]);
		fl[size - 1][size - 1] = 0.5f * (fl[size - 2][size - 1] + fl[size - 1][size - 2]);
		return fl;
	}

	Color[][] setBndCol(Color[][] col) {
		Color x, y;

		for (int i = 1; i < size - 1; i++) {
			col[i][0] = col[i][1];
			col[i][size - 1] = col[i][size - 2];
		}
		for (int j = 1; j < size - 1; j++) {
			col[0][j] = col[1][j];
			col[size - 1][j] = col[size - 2][j];
		}

		col[0][0] = col[0][1].avg(col[1][0]);
		col[0][size - 1] = col[0][size - 2].avg(col[1][size - 1]);
		col[size - 1][0] = col[size - 2][0].avg(col[size - 1][1]);
		col[size - 1][size - 1] = col[size - 2][size - 1].avg(col[size - 1][size - 2]);

//		for (int i = 1; i < size - 1; i++) {
//			x = col[i][1].avg(col[i][size - 2]);
//			col[i][0] = x;
//			col[i][size - 1] = x;
//		}
//		for (int j = 1; j < size - 1; j++) {
//			y = col[1][j].avg(col[size - 2][j]);
//			col[size - 2][j] = y;
//			col[1][j] = y;
//		}

		return col;
	}

	Vec2[][] setBndVec(Vec2[][] vec) {
		Vec2 y, x;
		for (int i = 0; i < size; i++) {
			vec[i][0] = Vec2.zero;
			vec[i][size - 1] = Vec2.zero;
		}
		for (int j = 0; j < size; j++) {
			vec[0][j] = Vec2.zero;
			vec[size - 1][j] = Vec2.zero;
		}
		for (int i = 1; i < size - 1; i++) {
			x = vec[i][1].avg(vec[i][size - 2]);
			vec[i][1] = x;
			vec[i][size - 2] = x;
		}
		for (int j = 0; j < size; j++) {
			y = vec[1][j].avg(vec[size - 2][j]);
			vec[1][j] = y;
			vec[size - 2][j] = y;
		}
		return vec;
	}

	Color[][] linSolveCol(float a, float c, Color[][] newCol, Color[][] oldCol) {
		float cRec = 1f / c;
		for (int k = 0; k < iter; k++) {
			for (int i = 1; i < (size - 1); i++) {
				for (int j = 1; j < (size - 1); j++) {
					Color sum = newCol[i + 1][j].add(newCol[i - 1][j]).add(newCol[i][j - 1]).add(newCol[i][j + 1]);
					newCol[i][j] = (oldCol[i][j].add(sum.multiply(a))).multiply(cRec);
				}
			}
		}
		return newCol;
	}

	float[][] linSolve(float a, float c, float[][] newFloat, float[][] oldFloat) {
		float cRec = 1f / c;
		for (int k = 0; k < iter; k++) {
			for (int i = 1; i < (size - 1); i++) {
				for (int j = 1; j < (size - 1); j++) {
					float sum = newFloat[i + 1][j] + (newFloat[i - 1][j]) + (newFloat[i][j - 1]) + (newFloat[i][j + 1]);
					newFloat[i][j] = (oldFloat[i][j] + (a * sum)) * cRec;
				}
			}
		}
		return newFloat;
	}

	Color[][] diffCol(Color[][] newCol, Color[][] oldCol) {
		float a = ms * diff * (size - 2) * (size - 2);
		float c = (1 + 4 * a);
		newCol = linSolveCol(a, c, newCol, oldCol);
		newCol = setBndCol(newCol);
		return newCol;
	}

	Vec2[][] linSolveVec(float a, float c, Vec2[][] newVec, Vec2[][] oldVec) {
		float cRec = 1f / c;
		for (int k = 0; k < iter; k++) {
			for (int i = 1; i < (size - 1); i++) {
				for (int j = 1; j < (size - 1); j++) {
					Vec2 sum = newVec[i + 1][j].add(newVec[i - 1][j]).add(newVec[i][j + 1]).add(newVec[i][j - 1]);
					newVec[i][j] = (oldVec[i][j].add(sum.multiply(a))).multiply(cRec);
				}
			}
		}
		return newVec;
	}

	Vec2[][] diffVec(Vec2[][] newVec, Vec2[][] oldVec) {
		float a = ms * diff * (size - 2) * (size - 2);
		float c = (1 + 4 * a);
		newVec = linSolveVec(a, c, newVec, oldVec);
//		newVec = setBndVec(newVec);
		return newVec;

	}

	Vec2[][] project(Vec2[][] vec) {
		float[][] p = new float[size][size], div = new float[size][size];
		for (int i = 1; i < size - 1; i++) {
			for (int j = 1; j < size - 1; j++) {
				div[i][j] = -0.5f * (vec[i + 1][j].y - vec[i - 1][j].y + vec[i][j + 1].x - vec[i][j - 1].x) / size;
				p[i][j] = 0;
			}
		}
		p = setBnd(p);
		div = setBnd(div);
		p = linSolve(1, 6, p, div);

		for (int i = 1; i < size - 1; i++) {
			for (int j = 1; j < size - 1; j++) {
				vec[i][j] = vec[i][j]
						.sub(new Vec2(p[i][j + 1] - p[i][j - 1], p[i + 1][j] - p[i - 1][j]).multiply(0.5f * size));
			}
		}
//		setBndVec(vec);
		return vec;
	}

	Vec2[][] advectVec(Vec2[][] oldVec, Vec2[][] newVec) {
		Vec2 pos;
		int x0, y0, x1, y1;
		float xFromRight, xFromLeft, yFromTop, yFromBottom;// from top and from bottom misnomer because y axis go down
		for (int i = 1; i < size - 1; i++) {
			for (int j = 1; j < size - 1; j++) {
				pos = new Vec2(j, i).sub((newVec[i][j]));
				pos = Vec2.clamp(0.1f, pos, size - 1.1f);
				x0 = (int) pos.x;
				y0 = (int) pos.y;
				x1 = x0 + 1;
				y1 = y0 + 1;
				xFromLeft = pos.x - x0;
				xFromRight = 1f - xFromLeft;
				yFromBottom = pos.y - y0;
				yFromTop = 1f - yFromBottom;
//				float xVel = xFromRight * (yFromTop * newVec[y0][x0].x + yFromBottom * newVec[y1][x0].x)
//						+ xFromLeft * (yFromTop * newVec[y0][x1].x + yFromBottom * newVec[y1][x1].x);
//				float yVel = xFromRight* (yFromTop * newVec[y0][x0].y + yFromBottom * newVec[y1][x0].y)
//						+ xFromLeft* (yFromTop * newVec[y0][x1].y + yFromBottom * newVec[y1][x1].y);
				Vec2 velocity = (newVec[y0][x0].multiply(yFromTop).add(newVec[y1][x0].multiply(yFromBottom)))
						.multiply(xFromRight)
						.add((newVec[y0][x1].multiply(yFromTop).add(newVec[y1][x1].multiply(yFromBottom)))
								.multiply(xFromLeft));
				oldVec[i][j] = velocity;
			}
		}
//		setBndVec(oldVec);
		return oldVec;
	}

	Color[][] advectCol(Vec2[][] vec, Color[][] oldCol, Color[][] newCol) {
		Vec2 pos;
		int x0, y0, x1, y1;
		float xFromRight, xFromLeft, yFromTop, yFromBottom;// from top and from bottom misnomer because y axis go down
		for (int i = 1; i < size - 1; i++) {
			for (int j = 1; j < size - 1; j++) {
				pos = new Vec2(j, i).sub((vec[i][j]));
				pos = Vec2.clamp(0.1f, pos, size - 1.1f);
				x0 = (int) pos.x;
				y0 = (int) pos.y;
				x1 = x0 + 1;
				y1 = y0 + 1;
				xFromLeft = pos.x - x0;
				xFromRight = 1f - xFromLeft;
				yFromBottom = pos.y - y0;
				yFromTop = 1f - yFromBottom;
//				float rNew = xFromLeft * (yFromBottom * newCol[y0][x0].r + yFromTop * newCol[y1][x0].r)
//						+ xFromRight * (yFromBottom * newCol[y0][x1].r + yFromTop * newCol[y1][x1].r);
//				float gNew = xFromLeft * (yFromBottom * newCol[y0][x0].g + yFromTop * newCol[y1][x0].g)
//						+ xFromRight * (yFromBottom * newCol[y0][x1].g + yFromTop * newCol[y1][x1].g);
//				float bNew = xFromLeft * (yFromBottom * newCol[y0][x0].b + yFromTop * newCol[y1][x0].b)
//						+ xFromRight * (yFromBottom * newCol[y0][x1].b + yFromTop * newCol[y1][x1].b);
				// this is wrong don't try using

				Color color = (newCol[y0][x0].multiply(yFromTop).add(newCol[y1][x0].multiply(yFromBottom)))
						.multiply(xFromRight)
						.add((newCol[y0][x1].multiply(yFromTop).add(newCol[y1][x1].multiply(yFromBottom)))
								.multiply(xFromLeft));
				oldCol[i][j] = color;
			}
		}
		setBndCol(oldCol);
		return oldCol;
	}

	BufferedImage createImg(BufferedImage im) {
		BufferedImage i = new BufferedImage(im.getWidth(), im.getHeight(), im.getType());
		for (int y = 0; y < drawW; y++) {
			for (int x = 0; x < drawH; x++) {
				i.setRGB(x, y, s[(int) (y / drawSize) + crop][(int) (x / drawSize) + crop].clampToInt());
			}
		}
		return i;
	}

	void tick() {
		newV = diffVec(newV, v);
		newV = project(newV);
		v = advectVec(v, newV);
		v = project(v);
//		setBndVec(newV);
//		setBndVec(v);
		newS = diffCol(newS, s);
		s = advectCol(v, s, newS);
		fade();
	}

	void loop(float tps) {

		long oldTime = System.currentTimeMillis();
		ms = 1000f / tps / steps;
		float delta = 0, paintDelta = 0;
		int frame = 0;
		float rTP = 4;
		float gTP = 4;
		float bTP = 4;
		float rPhase = rTP;
		float gPhase = gTP;
		float bPhase = bTP;
		float rDenom = tps / steps / rTP;
		float gDenom = tps / steps / gTP;
		float bDenom = tps / steps / bTP;
		while (running) {
			long now = System.currentTimeMillis();
			delta += (now - oldTime) / ms;
			oldTime = now;
			try {
				Thread.sleep((int) (ms / 4));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (delta >= 1) {
				if (changeCol) {
					System.out.println("Color changing");
					rTP = 4 * r.nextFloat();
					gTP = 4 * r.nextFloat();
					bTP = 4 * r.nextFloat();
					rPhase = rTP * r.nextFloat();
					gPhase = gTP * r.nextFloat();
					bPhase = bTP * r.nextFloat();
					rDenom = tps / steps / rTP;
					gDenom = tps / steps / gTP;
					bDenom = tps / steps / bTP;
					changeCol = false;
				}
				delta--;
				// CODE AFTER THIS
				tick();
				frame++;
				paintDelta++;
				dyeCol = new Color((float) Math.sin(Math.PI * (frame / rDenom + rPhase)),
						(float) Math.sin(Math.PI * (frame / gDenom + gPhase)),
						(float) Math.sin(Math.PI * (frame / bDenom + bPhase)));
				dyeCol = Color.clamp(0.004f, dyeCol, 4);
				if (paintDelta >= steps) {
					img = createImg(img);
					this.repaint();
					paintDelta = 0;
				}
				// CODE BEFORE THIS
			}
		}
	}

	public void paintComponent(Graphics g) {
		g.drawImage(img, 0, 0, iObs);
		if (vectorView) {
			for (int i = crop; i < (s.length - crop); i++) {
				for (int j = crop; j < (s[1].length - crop); j++) {

					float vX = maths.clamp(-drawSize, v[i][j].x * drawSize, drawSize),
							vY = maths.clamp(-drawSize, v[i][j].y * drawSize, drawSize);
					g.setColor(Color.clamp(0,
							Color.mix(Color.red, Color.green, (float) Math.sqrt(vX * vX + vY * vY) / halfSize), 1)
							.toAwt());
					float yCent = (i - crop) * drawSize + halfSize, xCent = (j - crop) * drawSize + halfSize;
					g.drawLine((int) (xCent - vX), (int) (yCent - vY), (int) (xCent + vX), (int) (yCent + vY));

				}
			}
		}

	}

	public static void main(String[] args) {
		Fluid fl = new Fluid(148, 5, 1f / 1e9f, 1f / 5e8f, 30, 1f / 1000f, false);
		fl.loop(fl.tps);
		System.exit(0);
	}
}
