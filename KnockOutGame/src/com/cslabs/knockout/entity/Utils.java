package com.cslabs.knockout.entity;

import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;

/**
 * @author The NG Family
 *
 */
public class Utils {

	// Constants
	public static final int MAX_POWER = 50;
	public static final int FIRING_RADIUS = 100;

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

		final Vector2 velocity = new Vector2(Vx, Vy).mul(
				(float) (distance / FIRING_RADIUS));
		return velocity;
	}
	
	public static Vector2 generateVelocity(int power, double angle) {
		int newPower =  Math.min(power, MAX_POWER);

		final int Vx = (int) (newPower * Math.cos(angle));
		final int Vy = (int) (newPower * Math.sin(angle));

		final Vector2 velocity = new Vector2(Vx, Vy);

		return velocity;
	}

}
