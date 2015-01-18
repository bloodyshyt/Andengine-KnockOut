package com.cslabs.knockout.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.andengine.engine.camera.ZoomCamera;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.cslabs.knockout.AI.Shot;

/**
 * @author The NG Family
 *
 */
public class Utils {

	// Constants
	public static final int MAX_POWER = 50;
	public static final int FIRING_RADIUS = 100;
	public static final int MAX_SKILL_LEVEL = 5;

	// static variables
	static Random randomGenerator = new Random();

	// converts array to vector and dividing PIXEL_TO_METER_RATIO_DEFAULT
	public static Vector2[] arrayToVector2withMeterConv(float[] pBufferData) {
		int numOfVec = pBufferData.length / 3;
		Vector2 vec[] = new Vector2[numOfVec];
		int counter = 0;
		for (int i = 0; i < numOfVec; i++) {
			vec[i] = new Vector2(pBufferData[counter++]
					/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
					pBufferData[counter++]
							/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
			counter++; // every third element is useless
		}
		return vec;

	}

	public static float calculateDistance(float x1, float y1, float x2, float y2) {
		return Math.min(
				(float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)),
				FIRING_RADIUS);
	}

	public static Vector2 generateVelocity(double distance, double angle) {

		final int Vx = (int) (MAX_POWER * Math.cos(angle));
		final int Vy = (int) (MAX_POWER * Math.sin(angle));

		final Vector2 velocity = new Vector2(Vx, Vy)
				.mul((float) (distance / FIRING_RADIUS));
		return velocity;
	}

	public static Vector2 generateVelocity(int power, double angle) {
		int newPower = Math.min(power, MAX_POWER);

		final int Vx = (int) (newPower * Math.cos(angle));
		final int Vy = (int) (newPower * Math.sin(angle));

		final Vector2 velocity = new Vector2(Vx, Vy);

		return velocity;
	}

	public static boolean isFingerAtEdgeofScreen(
			final TouchEvent pSceneTouchEvent, final ZoomCamera camera) {

		// Constants
		final float edge = 0.08f; // percent of the screen that is considered
									// near the edge

		// Variables
		float currentZoom, camCenterX, camCenterY;
		float X1, Y1, X, Y, x, y;

		currentZoom = camera.getZoomFactor();
		camCenterX = camera.getCenterX();
		camCenterY = camera.getCenterY();
		X = camera.getWidth();
		Y = camera.getHeight();
		x = pSceneTouchEvent.getX();
		y = pSceneTouchEvent.getY();
		X1 = camCenterX - X / 2;
		Y1 = camCenterY - Y / 2;

		if ((x > X1 && x < X1 + edge * X)
				|| (x > X1 + (1 - edge) * X && x < X1 + X)
				|| (y > Y1 && y < Y1 + edge * Y)
				|| (y > Y1 + (1 - edge) * Y && y < Y1 + Y)) {
			return true;
		}
		return false;
	}

	public static void perturbShot(Shot shot, int pAccuracy) {
		float Vx = shot.getVelocity().x;
		float Vy = shot.getVelocity().y;
		double perturbFactor = randomGenerator.nextGaussian();
		float nVx = (float) (perturbFactor * Vx
				* (0.1 * (MAX_SKILL_LEVEL - pAccuracy)) + Vx);
		float nVy = (float) (perturbFactor * Vy
				* (0.1 * (MAX_SKILL_LEVEL - pAccuracy)) + Vy);
		shot.getVelocity().set(nVx, nVy);
		Debug.i("Before: X:" + Vx + " Y:" + Vy + " After: X:" + nVx + " Y: "
				+ nVy);
	}

	public static <T> List<T> copyIterator(Iterator<T> iter) {
		List<T> copy = new ArrayList<T>();
		while (iter.hasNext())
			copy.add(iter.next());
		return copy;
	}

	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

}
