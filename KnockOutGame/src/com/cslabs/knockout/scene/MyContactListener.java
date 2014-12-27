package com.cslabs.knockout.scene;

import java.util.LinkedList;

import org.andengine.util.debug.Debug;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cslabs.knockout.entity.CollidableEntity;
import com.cslabs.knockout.entity.Platform;
import com.cslabs.knockout.entity.Checker;
import com.cslabs.knockout.entity.PlayerNo;

public class MyContactListener implements ContactListener {

	private LinkedList<Checker> pieces;
	private Platform platform;

	public MyContactListener(LinkedList<Checker> pieces, Platform platform) {
		this.pieces = pieces;
		this.platform = platform;
	} 

	@Override
	public void beginContact(Contact contact) {
	}

	@Override
	public void endContact(Contact contact) {
		// Iterate through all of the pieces and see if any fell out of the
		// platform
		for (Checker p : pieces) {
			if (contact.getFixtureA().getBody().getUserData() instanceof Checker) {
				Checker ceA = (Checker) contact.getFixtureA()
						.getBody().getUserData();
				if (ceA.getID() == p.getID()) {
					//Debug.i("Piece " + p.getID() + " is fucking dead");
				}
			} else if (contact.getFixtureB().getBody().getUserData() instanceof Checker) {
				Checker ceB = (Checker) contact.getFixtureB()
						.getBody().getUserData();
				if (ceB.getID() == p.getID()) {
					p.die();
				}
			}
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub

	}

	private boolean checkContact(Contact contact, String typeA, String typeB) {
		if (contact.getFixtureA().getBody().getUserData() instanceof CollidableEntity
				&& contact.getFixtureB().getBody().getUserData() instanceof CollidableEntity) {
			CollidableEntity ceA = (CollidableEntity) contact.getFixtureA()
					.getBody().getUserData();
			CollidableEntity ceB = (CollidableEntity) contact.getFixtureB()
					.getBody().getUserData();

			if (typeA.equals(ceA.getType()) && typeB.equals(ceB.getType())
					|| typeA.equals(ceB.getType())
					&& typeB.equals(ceA.getType())) {
				return true;

			}
		}
		return false;
	}

	public boolean areBodiesContacted(Body pBody1, Body pBody2, Contact pContact) {
		if (pContact.getFixtureA().getBody().equals(pBody1)
				|| pContact.getFixtureB().getBody().equals(pBody1))
			if (pContact.getFixtureA().getBody().equals(pBody2)
					|| pContact.getFixtureB().getBody().equals(pBody2))
				return true;
		return false;
	}

}
