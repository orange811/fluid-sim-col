package utility;

public class Color {

	public float r, g, b;

	public static final Color black = new Color(0, 0, 0);
	public static final Color white = new Color(1, 1, 1);
	public static final Color red = new Color(1, 0, 0);
	public static final Color green = new Color(0, 1, 0);
	public static final Color blue = new Color(0, 0, 1);
	public static final Color yellow = new Color(1, 1, 0);
	public static final Color cyan = new Color(0, 1, 1);
	public static final Color purple = new Color(1, 0, 1);
	public static final Color grey = new Color(0.5f, 0.5f, 0.5f);
	public static final Color sky = new Color(0.427f, 0.729f, 0.850f);
	public static final Color sun = toColor(0xfeebc5);
	public static final Color yellowish = toColor(0xFDB813);
	public static final Color unhit = new Color(-1, -1, -1);
	public static final int maxInt = 0xFFFFFF;
	
	@Override
	public String toString() {
		return "Color[r:"+r+",g:"+g+",b:"+b+"]";
	}

	public static Color mapClamp(final Color col, float minFin, float maxFin, float minInit, float maxInit, float minClamp,
			float maxClamp) {
		Color c = new Color(col);
		c = new Color(minFin, minFin, minFin).add(
				(c.add(new Color(-minInit, -minInit, -minInit))).multiply((maxFin - minFin) / (maxInit - minInit)));
		c = clamp(minClamp, c, maxClamp);
		return c;
	}

	public static Color clamp(float min, Color col, float max) {
		float r = col.r, g = col.g, b = col.b;
		if (col.r < min)
			r = min;
		if (col.g < min)
			g = min;
		if (col.b < min)
			b = min;
		if (col.r > max)
			r = max;
		if (col.g > max)
			g = max;
		if (col.b > max)
			b = max;
		return new Color(r, g, b);

	}

	public static Color random() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	public Color() {
		r = 0;
		g = 0;
		b = 0;
	}

	public Color(float r1, float g1, float b1) {
		r = r1;
		g = g1;
		b = b1;
	}

	public Color(Color c) {
		r = c.r;
		g = c.g;
		b = c.b;
	}

	public Color add(Color c) {
		float r1 = r + c.r;
		float g1 = c.g + g;
		float b1 = c.b + b;
		return new Color(r1, g1, b1);
	}

	public Color divide(float scalar) {
		return new Color(r / scalar, g / scalar, b / scalar);
	}

	public static Color toColor(int rgb) {
		float r = ((rgb >> 16) & 0xFF) / 256.0f;
		float g = ((rgb >> 8) & 0xFF) / 256.0f;
		float b = (rgb & 0xFF) / 256.0f;
		return new Color(r, g, b);
	}

	public float magnitude() {
		return (float) (Math.sqrt(r * r + g * g + b * b));
	}

	public Color normalized() {
		float mag = magnitude();
		return new Color(r / mag, b / mag, g / mag);
	}

	public Color multiply(float scalar) {
		return new Color(r * (float) scalar, g * (float) scalar, b * (float) scalar);
	}

	public Color multiply(Color c) {
		return new Color(r * c.r, g * c.g, b * c.b);
	}

	public int toInt() {
		return (int) (r * 255) << 16 | (int) (g * 255) << 8 | (int) (b * 255);
	}

	public int clampToInt() {
		Color c = clamp(0, this, 1);
		return (int) (c.r * 255) << 16 | (int) (c.g * 255) << 8 | (int) (c.b * 255);
	}

	public java.awt.Color toAwt() {
		Color c = clamp(0, this, 1);
//		Color c = normalized();
		return new java.awt.Color(c.r, c.g, c.b);
	}

	public Color avg(Color c) {
		return new Color((r + c.r) / 2f, (g + c.g) / 2f, (b + c.b) / 2f);
	}

	public static Color mix(Color a, Color b, float fac) {
		float r = (float) (a.r * fac + b.r * (1 - fac));
		float g = (float) (a.g * fac + b.g * (1 - fac));
		float bl = (float) (a.b * fac + b.b * (1 - fac));
		return new Color(r, g, bl);
	}

}
