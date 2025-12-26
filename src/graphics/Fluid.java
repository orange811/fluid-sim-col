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

	int size = 102;
	static final int crop = 1;
	int drawSize = 8;
	int upResFac = 1;
	float halfSize = drawSize / 2f;
	static final int iter = 4;
	static final int brushSize = 5;
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
	    // Initialize parameters based on input arguments
	    this.size = sz;
	    this.drawSize = drawS;
	    this.visc = visc;
	    this.diff = diff;
	    this.fullscreen = fullscreen;
	    this.halfSize = drawSize / 2f;
	    this.tps = ticksPerSec;
	    this.fade = fadeVal;

	    // Create an ImageObserver to be used for images (not used in this code)
	    iObs = new ImageObserver() {
	        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
	            return false;
	        }
	    };

	    // Initialize addIntensity
	    addIntensity = 50000f;

	    // Calculate the dimensions of the drawing area
	    drawW = drawH = (size - 1 * crop) * drawS;

	    // Create a BufferedImage to store the rendered image
	    img = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_RGB);

	    // Initialize velocity and color arrays
	    v = new Vec2[size][size];
	    s = new Color[size][size];
	    newV = new Vec2[size][size];
	    newS = new Color[size][size];
	    for (int i = 0; i < size; i++) {
	        for (int j = 0; j < size; j++) {
	            v[i][j] = Vec2.zero;
	            newV[i][j] = v[i][j];
	            s[i][j] = Color.black;
	            newS[i][j] = s[i][j];
	        }
	    }

	    // Set background and foreground colors for the JPanel
	    setBackground(bg.toAwt());
	    setForeground(fg.toAwt());

	    // Create the JFrame for displaying the simulation
	    jf.setBackground(bg.toAwt());
	    jf.add(this);
	    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    
	    // Set JFrame size based on fullscreen preference
	    if (fullscreen)
	        jf.setExtendedState(JFrame.MAXIMIZED_BOTH);
	    else
	        jf.setSize(drawW, drawH);

	    // Set JFrame properties
	    jf.setUndecorated(true);
	    jf.setBackground(getBackground());
	    jf.setVisible(true);

	    // Add keyboard listener for user interactions
	    jf.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent k) {
	            // Exit the simulation on ESC key
	            if (k.getKeyCode() == KeyEvent.VK_ESCAPE) {
	                running = false;
	            }
	            // Toggle vector view on SPACE key
	            if (k.getKeyCode() == KeyEvent.VK_SPACE) {
	                vectorView = !vectorView;
	            }
	            // Trigger color change on C key
	            if (k.getKeyCode() == KeyEvent.VK_C) {
	                changeCol = true;
	            }
	        }
	    });

		jf.addMouseMotionListener(new MouseMotionAdapter() {
			//LINE BASED IMPLEMENTATION
			public void mouseDragged(MouseEvent m) {
			    // Retrieve current mouse position and convert to array coordinates
			    Point loc = m.getPoint();
			    Vec2 mNew = new Vec2(loc.x * 1f / drawSize + crop, loc.y * 1f / drawSize + crop);
			    Vec2 diff = mNew.sub(mOld);
			    
			    // Extract integer coordinates of old and new positions
			    int x0 = (int) mOld.x;
			    int y0 = (int) mOld.y;
			    int x1 = (int) mNew.x;
			    int y1 = (int) mNew.y;

			    // Calculate absolute differences in x and y coordinates
			    int dx = Math.abs(x1 - x0);
			    int dy = Math.abs(y1 - y0);

			    // Determine direction of the line (left or right, up or down)
			    int sx = x0 < x1 ? 1 : -1;
			    int sy = y0 < y1 ? 1 : -1;

			    // Initialize error term
			    int err = dx - dy;

			    // Iterate over the points along the line
			    while (x0 >= 0 && x0 < size && y0 >= 0 && y0 < size) {
			        // Add dye and velocity to the current position in the arrays
			        addDye(x0, y0, dyeCol.multiply(addIntensity));
			        addVel(x0, y0, diff.multiply(vAddScale));

			        // Check if the current position matches the destination position
			        if (x0 == x1 && y0 == y1) {
			            break;
			        }

			        // Update error term based on Bresenham's algorithm
			        int e2 = 2 * err;
			        if (e2 > -dy) {
			            err -= dy;
			            x0 += sx;
			        }
			        if (e2 < dx) {
			            err += dx;
			            y0 += sy;
			        }
			    }

			    // Update the color array based on the difference calculation
			    s = diffCol(s, s);
			    
			    // Update the previous mouse position for the next iteration
			    mOld = mNew;
			}
		});
		
		// Add mouse listener to reset mouse position
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

	// Fading method to simulate decay and energy loss
	void fade() {
	    // Iterate over each grid cell
	    for (int i = 0; i < (size); i++) {
	        for (int j = 0; j < (size); j++) {
	            // Calculate fading and damping factors
	            float cFade = -fade / tps * ms;
	            float vFade = fade / tps * ms * drawSize * vAddScale / addIntensity;

	            // Extract current velocity components
	            float vX = v[i][j].x, vY = v[i][j].y;

	            // Extract current color
	            Color c = s[i][j];

	            // Apply damping to velocity components
	            if (vX != 0) {
	                if (Math.abs(vX) > vFade)
	                    vX -= Math.signum(vX) * vFade;
	                else
	                    vX = 0;
	                v[i][j].x = vX;
	            }
	            if (vY != 0) {
	                if (Math.abs(vY) > vFade)
	                    vY -= Math.signum(vY) * vFade;
	                else
	                    vY = 0;
	                v[i][j].y = vY;
	            }

	            // Apply fading to color
	            if (!c.equals(Color.black)) {
	                c = c.add(new Color(cFade, cFade, cFade));
	                c = Color.clamp(0, c, 1);
	                s[i][j] = c;
	            }
	        }
	    }
	}

	// Method to set boundary conditions for a scalar field
	float[][] setBnd(float[][] fl) {
	    // Set boundary values for the left and right edges
	    for (int i = 1; i < size - 1; i++) {
	        fl[i][0] = fl[i][1];
	        fl[i][size - 1] = fl[i][size - 2];
	    }
	    
	    // Set boundary values for the top and bottom edges
	    for (int j = 1; j < size - 1; j++) {
	        fl[0][j] = fl[1][j];
	        fl[size - 1][j] = fl[size - 2][j];
	    }

	    // Set corner values by averaging adjacent values
	    fl[0][0] = 0.5f * (fl[0][1] + fl[1][0]);
	    fl[0][size - 1] = 0.5f * (fl[0][size - 2] + fl[1][size - 1]);
	    fl[size - 1][0] = 0.5f * (fl[size - 2][0] + fl[size - 1][1]);
	    fl[size - 1][size - 1] = 0.5f * (fl[size - 2][size - 1] + fl[size - 1][size - 2]);
	    
	    return fl;
	}

	Color[][] setBndCol(Color[][] col) {
//	    Color x, y;

	    // Set boundary values for top and bottom rows (excluding corners)
	    for (int i = 1; i < size - 1; i++) {
	        col[i][0] = col[i][1];
	        col[i][size - 1] = col[i][size - 2];
	    }
	    
	    // Set boundary values for left and right columns (excluding corners)
	    for (int j = 1; j < size - 1; j++) {
	        col[0][j] = col[1][j];
	        col[size - 1][j] = col[size - 2][j];
	    }

	    // Set boundary values for the corners
	    col[0][0] = col[0][1].avg(col[1][0]);
	    col[0][size - 1] = col[0][size - 2].avg(col[1][size - 1]);
	    col[size - 1][0] = col[size - 2][0].avg(col[size - 1][1]);
	    col[size - 1][size - 1] = col[size - 2][size - 1].avg(col[size - 1][size - 2]);

	    return col;
	}

	Vec2[][] setBndVec(Vec2[][] vec) {
	    // Set boundary values for top and bottom rows
	    for (int i = 0; i < size; i++) {
	        vec[i][0] = vec[i][1].multiply(-1);
	        vec[i][size - 1] = vec[i][size - 2].multiply(-1);
	    }
	    
	    // Set boundary values for left and right columns (excluding corners)
	    for (int j = 0; j < size; j++) {
	        vec[0][j] = vec[1][j].multiply(-1);
	        vec[size - 1][j] = vec[size - 2][j].multiply(-1);
	    }
	    
	    // Set boundary values for the corners
	    vec[0][0] = vec[1][1].multiply(-1);
	    vec[0][size - 1] = vec[1][size - 2].multiply(-1);
	    vec[size - 1][0] = vec[size - 2][1].multiply(-1);
	    vec[size - 1][size - 1] = vec[size - 2][size - 2].multiply(-1);
	    
	    return vec;
	}

	// Method to solve linear equations for color fields
	Color[][] linSolveCol(float a, float c, Color[][] newCol, Color[][] oldCol) {
	    // Calculate the reciprocal of coefficient c
	    float cRec = 1f / c;
	    
	    // Iterate over the field multiple times for convergence
	    for (int k = 0; k < iter; k++) {
	        for (int i = 1; i < (size - 1); i++) {
	            for (int j = 1; j < (size - 1); j++) {
	                // Calculate the sum of neighboring colors
	                Color sum = newCol[i + 1][j].add(newCol[i - 1][j]).add(newCol[i][j - 1]).add(newCol[i][j + 1]);
	                
	                // Calculate the new color value based on the old value and the sum
	                newCol[i][j] = (oldCol[i][j].add(sum.multiply(a))).multiply(cRec);
	            }
	        }
	    }
	    
	    // Apply boundary conditions to the new color field
	    newCol = setBndCol(newCol);
	    
	    return newCol;
	}

	// Method to solve linear equations for scalar fields
	float[][] linSolve(float a, float c, float[][] newFloat, float[][] oldFloat) {
	    // Calculate the reciprocal of coefficient c
	    float cRec = 1f / c;
	    
	    // Iterate over the field multiple times for convergence
	    for (int k = 0; k < iter; k++) {
	        for (int i = 1; i < (size - 1); i++) {
	            for (int j = 1; j < (size - 1); j++) {
	                // Calculate the sum of neighboring values
	                float sum = newFloat[i + 1][j] + (newFloat[i - 1][j]) + (newFloat[i][j - 1]) + (newFloat[i][j + 1]);
	                
	                // Calculate the new value based on the old value, the sum, and coefficient a
	                newFloat[i][j] = (oldFloat[i][j] + (a * sum)) * cRec;
	            }
	        }
	    }
	    
	    // Apply boundary conditions to the new scalar field
	    newFloat = setBnd(newFloat);
	    
	    return newFloat;
	}

	// Method to diffuse color fields (simulating diffusion of color)
	Color[][] diffCol(Color[][] newCol, Color[][] oldCol) {
	    // Calculate the diffusion coefficient 'a' based on time step and grid size
	    float a = ms * diff * (size - 2) * (size - 2);
	    
	    // Calculate the coefficient 'c' used in linear solving
	    float c = (1 + 4 * a);
	    
	    // Solve linear equations for diffusion multiple times for convergence
	    newCol = linSolveCol(a, c, newCol, oldCol);
	    
	    // Apply boundary conditions to the new color field
	    newCol = setBndCol(newCol);
	    
	    return newCol;
	}


	// Method to solve linear equations for vector fields
	Vec2[][] linSolveVec(float a, float c, Vec2[][] newVec, Vec2[][] oldVec) {
	    // Calculate the reciprocal of coefficient c
	    float cRec = 1f / c;
	    
	    // Iterate over the field multiple times for convergence
	    for (int k = 0; k < iter; k++) {
	        for (int i = 1; i < (size - 1); i++) {
	            for (int j = 1; j < (size - 1); j++) {
	                // Calculate the sum of neighboring vectors
	                Vec2 sum = newVec[i + 1][j].add(newVec[i - 1][j]).add(newVec[i][j + 1]).add(newVec[i][j - 1]);
	                
	                // Calculate the new vector value based on the old value, the sum, and coefficient a
	                newVec[i][j] = (oldVec[i][j].add(sum.multiply(a))).multiply(cRec);
	            }
	        }
	    }
	    
	    // Apply boundary conditions to the new vector field
	    newVec = setBndVec(newVec);
	    
	    return newVec;
	}

	// Method to diffuse vector fields (simulating diffusion of velocity)
	Vec2[][] diffVec(Vec2[][] newVec, Vec2[][] oldVec) {
	    // Calculate the diffusion coefficient 'a' based on time step and grid size
	    float a = ms * diff * (size - 2) * (size - 2);
	    
	    // Calculate the coefficient 'c' used in linear solving
	    float c = (1 + 4 * a);
	    
	    // Solve linear equations for diffusion multiple times for convergence
	    newVec = linSolveVec(a, c, newVec, oldVec);
	    
	    // Apply boundary conditions to the new vector field
	    newVec = setBndVec(newVec);
	    
	    return newVec;
	}

	// Method to project the velocity field to ensure incompressibility
	Vec2[][] project(Vec2[][] vec) {
	    // Initialize arrays for pressure and divergence
	    float[][] p = new float[size][size];
	    float[][] div = new float[size][size];
	    
	    // Calculate divergence of the velocity field
	    for (int i = 1; i < size - 1; i++) {
	        for (int j = 1; j < size - 1; j++) {
	            // Calculate the divergence using central differences
	            div[i][j] = -0.5f * (vec[i + 1][j].y - vec[i - 1][j].y + vec[i][j + 1].x - vec[i][j - 1].x) / size;
	            p[i][j] = 0; // Initialize pressure to zero
	        }
	    }
	    
	    // Apply boundary conditions to the divergence field
	    div = setBnd(div);
	    
	    // Solve the pressure field to ensure incompressibility
	    p = linSolve(1, 6, p, div);
	    
	    // Update the velocity field based on the pressure field
	    for (int i = 1; i < size - 1; i++) {
	        for (int j = 1; j < size - 1; j++) {
	            // Update the velocity by subtracting the gradient of pressure
	            vec[i][j] = vec[i][j]
	                    .sub(new Vec2(p[i][j + 1] - p[i][j - 1], p[i + 1][j] - p[i - 1][j]).multiply(0.5f * size));
	        }
	    }
	    
	    // Apply boundary conditions to the updated velocity field
	    vec = setBndVec(vec);
	    
	    return vec;
	}

	// Method to advect vector fields based on velocity
	Vec2[][] advectVec(Vec2[][] oldVec, Vec2[][] newVec) {
	    Vec2 pos;
	    int x0, y0, x1, y1;
	    float xFromRight, xFromLeft, yFromTop, yFromBottom;
	    
	    // Iterate over the grid to advect vector fields
	    for (int i = 1; i < size - 1; i++) {
	        for (int j = 1; j < size - 1; j++) {
	            // Calculate the position based on the velocity
	            pos = new Vec2(j, i).sub((newVec[i][j]));
	            pos = Vec2.clamp(0.1f, pos, size - 1.1f);
	            
	            // Calculate integer coordinates and factors for interpolation
	            x0 = (int) pos.x;
	            y0 = (int) pos.y;
	            x1 = x0 + 1;
	            y1 = y0 + 1;
	            xFromLeft = pos.x - x0;
	            xFromRight = 1f - xFromLeft;
	            yFromBottom = pos.y - y0;
	            yFromTop = 1f - yFromBottom;

	            // Perform bilinear interpolation to advect vector
	            Vec2 velocity = (newVec[y0][x0].multiply(yFromTop).add(newVec[y1][x0].multiply(yFromBottom)))
	                    .multiply(xFromRight)
	                    .add((newVec[y0][x1].multiply(yFromTop).add(newVec[y1][x1].multiply(yFromBottom)))
	                            .multiply(xFromLeft));
	            
	            // Update the vector field with the advected vector
	            oldVec[i][j] = velocity;
	        }
	    }
	    
	    // Apply boundary conditions to the advected vector field
	    oldVec = setBndVec(oldVec);
	    
	    return oldVec;
	}


	// Method to advect color fields based on velocity
	Color[][] advectCol(Vec2[][] vec, Color[][] oldCol, Color[][] newCol) {
	    Vec2 pos;
	    int x0, y0, x1, y1;
	    float xFromRight, xFromLeft, yFromTop, yFromBottom;
	    
	    // Iterate over the grid to advect color fields
	    for (int i = 1; i < size - 1; i++) {
	        for (int j = 1; j < size - 1; j++) {
	            // Calculate the position based on the velocity
	            pos = new Vec2(j, i).sub((vec[i][j]));
	            pos = Vec2.clamp(0.1f, pos, size - 1.1f);
	            
	            // Calculate integer coordinates and factors for interpolation
	            x0 = (int) pos.x;
	            y0 = (int) pos.y;
	            x1 = x0 + 1;
	            y1 = y0 + 1;
	            xFromLeft = pos.x - x0;
	            xFromRight = 1f - xFromLeft;
	            yFromBottom = pos.y - y0;
	            yFromTop = 1f - yFromBottom;

	            // Perform bilinear interpolation to advect color
	            Color color = (newCol[y0][x0].multiply(yFromTop).add(newCol[y1][x0].multiply(yFromBottom)))
	                    .multiply(xFromRight)
	                    .add((newCol[y0][x1].multiply(yFromTop).add(newCol[y1][x1].multiply(yFromBottom)))
	                            .multiply(xFromLeft));
	            
	            // Update the color field with the advected color
	            oldCol[i][j] = color;
	        }
	    }
	    
	    // Apply boundary conditions to the advected color field
	    oldCol = setBndCol(oldCol);
	    
	    return oldCol;
	}

	BufferedImage createImg(BufferedImage im) {
		//Creating Image variable
		BufferedImage i = new BufferedImage(im.getWidth(), im.getHeight(), im.getType());
		
		//Scaling pixels and adding colors to image
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
		newS = diffCol(newS, s);
		s = advectCol(v, s, newS);
		fade();
	}

	// Main loop of the fluid simulation
	void loop(float tps) {
	    long oldTime = System.currentTimeMillis();
	    ms = 1000f / tps;
	    float delta = 0, paintDelta = 0;
	    int frame = 0;
	    float rTP = 4;
	    float gTP = 4;
	    float bTP = 4;
	    float rPhase = 0;
	    float gPhase = 1.333f;
	    float bPhase = 2.666f;
	    float rDenom = tps / rTP;
	    float gDenom = tps / gTP;
	    float bDenom = tps / bTP;
	    
	    // Main simulation loop
	    while (running) {
	        long now = System.currentTimeMillis();
	        delta += (now - oldTime) / ms;
	        oldTime = now;
	        
	        // Sleep for a short period to control simulation speed
	        try {
	            Thread.sleep((int) (ms / 4));
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        
	        if (delta >= 1) {
	            if (changeCol) {
	                System.out.println("Color changing");
	                // Generate new color phase parameters
	                rTP = 4 * r.nextFloat();
	                gTP = 4 * r.nextFloat();
	                bTP = 4 * r.nextFloat();
	                rPhase = rTP * r.nextFloat();
	                gPhase = gTP * r.nextFloat();
	                bPhase = bTP * r.nextFloat();
	                rDenom = tps / rTP;
	                gDenom = tps / gTP;
	                bDenom = tps / bTP;
	                changeCol = false;
	            }
	            delta--;
	            
	            // Simulate a time step
	            tick();
	            frame++;
	            paintDelta++;
	            
	            // Update the dye color based on a sinusoidal function
	            dyeCol = new Color((float) (Math.sin(Math.PI * (frame / rDenom + rPhase)) + .5f),
	                    (float) (Math.sin(Math.PI * (frame / gDenom + gPhase)) + .5f),
	                    (float) (Math.sin(Math.PI * (frame / bDenom + bPhase)) + .5f));
	            dyeCol = Color.clamp(0, dyeCol, 4);
	            
	            if (paintDelta >= 1) {
	                // Update the displayed image
	                img = createImg(img);
	                this.repaint();
	                paintDelta = 0;
	            }
	        }
	    }
	}

	public void paintComponent(Graphics g) {
		//Draw main color grid
		g.drawImage(img, 0, 0, iObs);
		
		//Draw velocity vectors
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
		Fluid fl = new Fluid(148, 5, 1f / 1e9f, 1f / 5e8f, 30, 4f / 1000f, false);
		fl.loop(fl.tps);
		System.exit(0);
	}
}
