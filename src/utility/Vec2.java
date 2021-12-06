package utility;

public class Vec2 {

	public float x, y;

	public static final Vec2 zero = new Vec2(0, 0);
	public static final Vec2 inv = new Vec2(-1, -1);

	public String toString() {
		return "Vec2[" + x + "," + y + "]";
	}

	public Vec2(float x1, float y1) {
		x = x1;
		y = y1;
	}

	public Vec2() {
		x = 0;
		y = 0;
	}

	public Vec2(Vec2 vec2) {
		x = vec2.x;
		y = vec2.y;
	}

	public static Vec2 clamp(float min, Vec2 vec, float max) {
		if (vec.x > max)
			vec.x = max;
		if (vec.x < min)
			vec.x = min;
		if (vec.y > max)
			vec.y = max;
		if (vec.y < min)
			vec.y = min;
		return vec;
	}

	public Vec2 add(Vec2 b) {// a+b
		return new Vec2(x + b.x, y + b.y);
	}

	public Vec2 sub(Vec2 b) {// a-b
		return new Vec2(x - b.x, y - b.y);
	}

	public Vec2 multiply(float scalar) {
		return new Vec2(x * scalar, y * scalar);
	}

	public float dot(Vec2 a, Vec2 b) {
		return (x * b.x + y * b.y);
	}

	public float mag() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public Vec2 normalized() {
		float mag = mag();
		return new Vec2(x / mag, y / mag);
	}

	public Vec2 avg(Vec2 v) {
		return new Vec2((x + v.x) * 0.5f, (y + v.y) * 0.5f);
	}

}
