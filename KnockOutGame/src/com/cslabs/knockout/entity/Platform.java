package com.cslabs.knockout.entity;

import org.andengine.entity.primitive.DrawMode;
import org.andengine.entity.primitive.Mesh;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Platform extends Mesh implements CollidableEntity {
	
	private Body body;
	
	private float[] platform_cords;
	private Vector2[] pVertices;
	private int vertexCount;

	public static final Type TYPE = Type.PLATFORM;

	public Platform(float pX, float pY, float[] pBufferData, int pVertexCount,
			DrawMode pDrawMode,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pBufferData, pVertexCount, pDrawMode, pVertexBufferObjectManager);
		this.setColor(Color.GREEN);
		platform_cords = pBufferData;
		vertexCount = pVertexCount;
		pVertices = Utils
				.arrayToVector2withMeterConv(platform_cords);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setBody(Body body) {
		this.body = body;

	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public Type getType() {
		return TYPE;
	}

	public float[] getPlatform_cords() {
		return platform_cords;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public Vector2[] getpVertices() {
		return pVertices;
	}

	public void setpVertices(Vector2[] pVertices) {
		this.pVertices = pVertices;
	}

}
