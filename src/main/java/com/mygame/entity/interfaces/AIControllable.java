/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.interfaces;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.settings.GeneralConstants;
import com.mygame.settings.Managers;
import java.util.List;

/**
 *
 * @author Arash
 */
public interface AIControllable extends Actor {

    // void initNavMesh();
    NavMeshPathfinder getPathfinder();

    void setPathfinder(NavMeshPathfinder pathfinder);

    Actor getTarget();

    void setTarget(Actor target);

    Vector3f getCurrentNavigationPosition();

    @Override
    default void updateActorState() {
        if (this.getTarget() != null && this.canAttack()) {
            this.getControl().setWalkDirection(Vector3f.ZERO);
            this.setState(EnumActorState.ATTACKING);
        } else if (this.getPathfinder().getNextWaypoint() != null) {
            this.setState(EnumActorState.WALKING);
        } else {
            this.setState(EnumActorState.STAND_STILL);
        }
    }

    /**
     * ********************************Attack*****************************************
     */
    float getMaxAttackDistance();

    default boolean canAttack() {
        float distanceToTarget = this.getPosition().distance(this.getTarget().getPosition());
        return distanceToTarget <= this.getMaxAttackDistance();
    }

    void attack();

    boolean canMove();

    /**
     * ********************************Navigation*****************************************
     */
    default void initNavMesh() {
        Node node = (Node) Managers.getInstance().getAsseManager().loadModel(Managers.getInstance().getCurrentlyLoadedLevel().getPathToScene());
        Geometry navMeshGeom = (Geometry) node.getChild(GeneralConstants.NAV_MESH_NAME);
        NavMesh navMesh = new NavMesh(navMeshGeom.getMesh());
        this.setPathfinder(new NavMeshPathfinder(navMesh));
    }

    default void navigateTo(Vector3f position) {
        if (this.getTarget() != null) {
            if (this.canMove()) {
                if (this.getPathfinder() == null) {
                    System.out.println("This Actor Have No Pathfinder assigned.");
                    return;
                }
                if (this.getTarget() == null) {
                    System.out.println("This Actor Have No Target assigned.");
                    return;
                }

                this.getPathfinder().setPosition(this.getPosition());
                if (!this.getCurrentNavigationPosition().equals(position)) {
                    this.getCurrentNavigationPosition().set(position);
                    // this.getPathfinder().clearPath();
                    this.getPathfinder().computePath(this.getCurrentNavigationPosition());

                }

                this.getControl().setWalkDirection(Vector3f.ZERO);

                Waypoint waypoint = this.getPathfinder().getNextWaypoint();
                if (waypoint == null) {
                    return;
                }

                Vector3f waypointDirection = waypoint.getPosition().subtract(this.getControl().getPhysicsLocation());
                this.getControl().setWalkDirection(waypointDirection.normalize().divide(11));

                if (waypoint.getPosition().distance(this.getPosition()) < this.getMaxAttackDistance() && !this.getPathfinder().isAtGoalWaypoint()) {
                    this.getPathfinder().goToNextWaypoint();
                }

                if (this.getPathfinder().isAtGoalWaypoint()) {
                    this.getPathfinder().clearPath();
                    this.getControl().setWalkDirection(Vector3f.ZERO);
                }
            } else {
                System.out.println("Cant Move");
                this.getControl().setWalkDirection(Vector3f.ZERO);
                this.getPathfinder().computePath(this.getPosition());
            }
        } else {
            System.out.println("This Actor Have No TargetPosition.Setting Target Position to currentPosition");
        }

    }

    /**
     * ********************************Detection*****************************************
     */
    default void updateDetection(float tpf) {
        // System.out.println("amount : " + this.getDetectionAmount());
        if (this.getDetectionAmount() < 1) {
            if (this.isTargetVisible()) {
                if (this.getDetectionAmount() < 1) {
                    this.setDetectionAmount(this.getDetectionAmount() + tpf / 4);
                }
            } else {
                if (this.getDetectionAmount() > 0) {
                    this.setDetectionAmount(this.getDetectionAmount() - tpf / 4);
                }
            }

            this.losetarget();
        } else {
            this.setIsFoundTarget(true);
            if (this.getPosition().distance(this.getLastTargetPosition()) <= this.getMaxAttackDistance()) {
                this.setDetectionAmount(this.getDetectionAmount() - tpf / 6);
            }
        }
    }

    default boolean isTargetVisible() {
        if (this.getTarget() != null) {
            return this.isTargetAtFront() && isTargetCanBeSeen();
        } else {
            return false;
        }
    }

    default boolean isTargetAtFront() {
        Vector3f targetDir = this.getPosition().subtract(this.getTarget().getPosition()).normalize();
        // float degree = this.getControl().getViewDirection().normalize().angleBetween(targetDir);
        float dot = this.getControl().getViewDirection().normalize().dot(targetDir);
        return dot >= 0.4f && dot <= 1;
    }

    default boolean isTargetCanBeSeen() {
        List<PhysicsRayTestResult> results = this.physicsTayTo(this.getPosition(), this.getTarget().getPosition());
        if (!results.isEmpty()) {
            return results.get(0).getCollisionObject().getUserObject().equals(this.getTarget());
        } else {
            return false;
        }
    }

    default void lookAtTarget(Actor target) {
        Vector3f dir = this.getPosition().subtract(target.getPosition());
        this.getControl().setViewDirection(dir);

    }

    default void losetarget() {
        if (this.isFoundTarget() && this.getDetectionAmount() < 0.4f) {
            this.setIsFoundTarget(false);
        }
    }

    Vector3f getLastTargetPosition();

    void setDetectionAmount(float amount);

    float getDetectionAmount();

    boolean isFoundTarget();

    void setIsFoundTarget(boolean found);

}
