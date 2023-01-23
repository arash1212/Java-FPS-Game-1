/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.interfaces;

import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.mygame.settings.Managers;
import java.util.List;

/**
 *
 * @author Arash
 */
public interface Actor {

    void spawn(Vector3f spawnPoint);

    void update(float tpf);

    default boolean canJump() {
        return this.getState() != EnumActorState.IN_AIR;
    }

    EnumActorState getState();

    void setState(EnumActorState state);

    CharacterControl getControl();

    boolean isRunning();

    default void updateActorState() {
        if (this.getControl().onGround()) {
            if (!this.getControl().getWalkDirection().equals(Vector3f.ZERO)) {
                if (!this.isRunning()) {
                    this.setState(EnumActorState.WALKING);
                } else {
                    this.setState(EnumActorState.RUNNING);
                }
            } else if (this.getControl().getWalkDirection().equals(Vector3f.ZERO)) {
                this.setState(EnumActorState.STAND_STILL);
            }
        } else {
            this.setState(EnumActorState.IN_AIR);
        }
    }

    default Vector3f getPosition() {
        return this.getControl().getPhysicsLocation();
    }

    void takeDamage(float damage, Actor attacker);

    void die();

    float getHealth();

    default CollisionResults rayTo(Vector3f origin, Vector3f direction, Node node) {
        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        node.collideWith(ray, results);
        return results;
    }

    default List<PhysicsRayTestResult> physicsTayTo(Vector3f from, Vector3f to) {
        return Managers.getInstance().getBulletAppState().getPhysicsSpace().rayTest(from, to);
    }
}
