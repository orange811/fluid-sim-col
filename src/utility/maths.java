package utility;

public class maths {
	public static float clamp(float min, float val, float max) {
		if (val < min)
			return min;
		else if (val > max)
			return max;
		return val;
	}

	public static float map(float val, float minFin, float maxFin, float minInit, float maxInit) {
		val = minFin + ((val - minInit) * (maxFin - minFin) / (maxInit - minInit));
		val = clamp(minFin, val, maxFin);
		return val;
	}

	public static float mapClamp(float val, float minFin, float maxFin, float minInit, float maxInit, float minClamp,
			float maxClamp) {
		val = (val - minInit) * (maxFin - minFin) / (maxInit - minInit);
		val = clamp(minClamp, val, maxClamp);
		return val;
	}
}
