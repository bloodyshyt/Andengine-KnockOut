package com.cslabs.knockout.factory;

import java.util.ArrayList;

import org.andengine.entity.primitive.DrawMode;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.cslabs.knockout.ResourceManager;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.Utils;
import com.cslabs.knockout.entity.VirtualPlatform;

public class PlatformFactory {
	private static PlatformFactory INSTANCE = new PlatformFactory();
	private VertexBufferObjectManager vbom;
	private PhysicsWorld physicsWorld;
	public static final FixtureDef PLATFORM_FIXTURE = PhysicsFactory
			.createFixtureDef(0, 0, 0);

	private PlatformFactory() {
	}

	public static PlatformFactory getInstance() {
		return INSTANCE;
	}

	public void create(PhysicsWorld physicsWorld, VertexBufferObjectManager vbom) {
		this.physicsWorld = physicsWorld;
		this.vbom = vbom;
	}

	public Platform createPlatform(float[] platform_cords2) {

		Platform platform = new Platform(0, 0, platform_cords2,
				platform_cords2.length / 3, DrawMode.LINE_LOOP, vbom);
		Vector2[] pVertices = platform.getpVertices();

		PLATFORM_FIXTURE.isSensor = true;
		Body platformBody = PhysicsFactory.createPolygonBody(physicsWorld,
				platform, pVertices, BodyType.StaticBody, PLATFORM_FIXTURE,
				PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
		platformBody.setUserData(platform);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(platform,
				platformBody));

		platform.setBody(platformBody);
		return platform;
	}
	
	public static VirtualPlatform clonePlatformIntoPhysicsWorld(FixedStepPhysicsWorld world, Platform p)
	{
		Body body = p.getBody();
		final BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.StaticBody;
		boxBodyDef.position.x = body.getPosition().x;
		boxBodyDef.position.y = body.getPosition().y;
		
		final FixtureDef pFixtureDef = PhysicsFactory
				.createFixtureDef(0, 0, 0);
		pFixtureDef.isSensor = true;

		final Body boxBody = world.createBody(boxBodyDef);

		final PolygonShape boxPoly = new PolygonShape();

		boxPoly.set(p.getpVertices());
		pFixtureDef.shape = boxPoly;

		boxBody.createFixture(pFixtureDef);

		boxPoly.dispose();
		
		VirtualPlatform vp = new VirtualPlatform(p);
		vp = vp.clone();
		vp.setBody(boxBody);
		boxBody.setUserData(vp);
		return vp;
	}

	public static VirtualPlatform clonePlatformIntoPhysicsWorld(FixedStepPhysicsWorld world, VirtualPlatform p)
	{
		Body body = p.getBody();
		final BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = BodyType.StaticBody;
		boxBodyDef.position.x = body.getPosition().x;
		boxBodyDef.position.y = body.getPosition().y;
		
		final FixtureDef pFixtureDef = PhysicsFactory
				.createFixtureDef(0, 0, 0);
		pFixtureDef.isSensor = true;

		final Body boxBody = world.createBody(boxBodyDef);

		final PolygonShape boxPoly = new PolygonShape();

		boxPoly.set(p.getpVertices());
		pFixtureDef.shape = boxPoly;

		boxBody.createFixture(pFixtureDef);

		boxPoly.dispose();
		
		VirtualPlatform vp = p.clone();
		vp.setBody(boxBody);
		boxBody.setUserData(vp);
		return vp;
	}
	
	public void copyPlatformIntoPhysicsWorld(float[] coords, Platform p,
			FixedStepPhysicsWorld world) {

		PLATFORM_FIXTURE.isSensor = true;

		Body platformBody = PhysicsFactory
				.createPolygonBody(world, p,
						Utils.arrayToVector2withMeterConv(coords),
						BodyType.StaticBody, PLATFORM_FIXTURE,
						PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
		platformBody.setUserData(p);
		p.setBody(platformBody);

	}
}
