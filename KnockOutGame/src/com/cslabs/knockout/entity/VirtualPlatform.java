package com.cslabs.knockout.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class VirtualPlatform {
	private static final String TAG = "VirtualPlatform";
	private Body body;

	private float[] platform_cords;
	private Vector2[] pVertices;

	public VirtualPlatform() {

	}

	public VirtualPlatform(Platform p) {
		platform_cords = p.getPlatform_cords();
		pVertices = p.getpVertices();
	}
	
	public VirtualPlatform clone() {
		VirtualPlatform copy = new VirtualPlatform();
		copy.platform_cords = this.getPlatform_cords();
		copy.pVertices = this.getpVertices();
		return copy;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public float[] getPlatform_cords() {
		return platform_cords;
	}

	public Vector2[] getpVertices() {
		return pVertices;
	}
}
