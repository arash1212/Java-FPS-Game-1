/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.interfaces;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.settings.GeneralConstants;
import com.mygame.settings.Managers;
import java.util.ArrayList;
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

    void setCurrentNavigationPosition(Vector3f position);

    @Override
    default void updateActorState() {
        if (this.getTarget() != null && this.canAttack()) {
            this.getControl().setWalkDirection(Vector3f.ZERO);
            this.setState(EnumActorState.ATTACKING);
        } else if (this.getPathfinder().getNextWaypoint() != null) {
            if (this.getTarget() != null && this.isFoundTarget()) {
                this.setState(EnumActorState.RUNNING);
            } else {
                this.setState(EnumActorState.WALKING);
            }
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
    float getSpeed();

    Geometry getNavMeshGeom();

    void setNavMeshGeom(Geometry navMeshGeom);

    default void initNavMesh() {
        Node node = (Node) Managers.getInstance().getAsseManager().loadModel(Managers.getInstance().getCurrentlyLoadedLevel().getPathToScene());
        this.setNavMeshGeom((Geometry) node.getChild(GeneralConstants.NAV_MESH_NAME));
        NavMesh navMesh = new NavMesh(this.getNavMeshGeom().getMesh());
        this.setPathfinder(new NavMeshPathfinder(navMesh));
    }

    default boolean navigateTo(Vector3f position) {
        if (this.getTarget() != null) {
            if (this.canMove()) {
                if (this.getPathfinder() == null) {
                    System.out.println("This Actor Have No Pathfinder assigned.");
                    return false;
                }
                if (this.getTarget() == null) {
                    System.out.println("This Actor Have No Target assigned.");
                    return false;
                }

                this.getPathfinder().setPosition(this.getPosition());

                if (!(this.getCurrentNavigationPosition().isSimilar(position, 2))) {
                    this.setCurrentNavigationPosition(position);
                    //this.getPathfinder().clearPath();
                    this.getPathfinder().computePath(this.getCurrentNavigationPosition());
//                    System.out.println("computing new path ?");
                }

                this.getControl().setWalkDirection(Vector3f.ZERO);

                Waypoint waypoint = this.getPathfinder().getNextWaypoint();
                if (waypoint == null) {
                    System.out.println("no waypoint");
                    return false;
                }

                Vector3f waypointDirection = waypoint.getPosition().subtract(this.getControl().getPhysicsLocation());
                this.getControl().setWalkDirection(waypointDirection.normalize().divide(this.getState().equals(EnumActorState.WALKING) ? this.getSpeed() : this.getSpeed() / 2));

                if (waypoint.getPosition().distance(this.getPosition()) < this.getMaxAttackDistance() && !this.getPathfinder().isAtGoalWaypoint()) {
                    this.getPathfinder().goToNextWaypoint();
                }

                if (this.getPathfinder().isAtGoalWaypoint()) {
                    this.getPathfinder().clearPath();
                    this.getControl().setWalkDirection(Vector3f.ZERO);
                }
            } else {
                // System.out.println("cant walks");
                this.getControl().setWalkDirection(Vector3f.ZERO);
                this.getPathfinder().computePath(this.getPosition());
            }
            return true;
        } else {
            System.out.println("This Actor Have No TargetPosition.Setting Target Position to currentPosition");
            return false;
        }
    }

    /**
     * ********************************Detection*****************************************
     */
    default float getDetectionSpeed(float tpf) {
        float distanceToTarget = this.getPosition().distance(this.getTarget().getPosition());
        if (distanceToTarget < 10) {
            return tpf * 2;
        } else if (distanceToTarget < 20) {
            return tpf;
        } else {
            return tpf / 2;
        }
    }

    default void updateDetection(float tpf) {
        // System.out.println("detetcion amount : " + this.getDetectionAmount());
        if (this.getDetectionAmount() < 1) {
            if (this.isTargetVisible()) {
                this.setDetectionAmount(this.getDetectionAmount() + this.getDetectionSpeed(tpf));
            } else {
                if (this.getDetectionAmount() > 0) {
                    this.setDetectionAmount(this.getDetectionAmount() - tpf / 2);
                }
            }

            this.losetarget();
        } else {
            this.setIsFoundTarget(true);
            if (this.getPosition().distance(this.getLastTargetPosition()) <= this.getMaxAttackDistance()) {
                if (this.getDetectionAmount() > 0) {
                    this.setDetectionAmount(this.getDetectionAmount() - tpf / 2);
                }
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

    default void lookAtTarget(Vector3f position) {
        Vector3f dir = this.getPosition().subtract(position);
        dir.y = 0;
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

    /**
     * ********************************Patrol*****************************************
     */
    float getMaxPatrolDistance();

    Vector3f getPatrolPoint();

    Vector3f getInitiaPos();

    float getTimeBetweenChaningPatrolPoint();

    void setTimeBetweenChaningPatrolPoint(float newTime);

    default List<Triangle> getTriangles(float distance) {
        List<Triangle> triangles = new ArrayList();
        if (this.getNavMeshGeom() != null) {
            int[] indices = new int[3];
            for (int i = 0; i < this.getNavMeshGeom().getMesh().getTriangleCount(); i++) {
                Triangle tri = new Triangle();
                this.getNavMeshGeom().getMesh().getTriangle(i, tri);
                if (tri.getCenter().distance(this.getInitiaPos()) <= distance) {
                    triangles.add(tri);
                }
            }
        }
        return triangles;
    }

    default void findRandomPoint(List<Triangle> tris) {
        if (this.getPatrolPoint().equals(Vector3f.ZERO) || this.getPosition().distance(this.getPatrolPoint()) <= 1) {
            if (Managers.getInstance().getTimer().getTimeInSeconds() > this.getTimeBetweenChaningPatrolPoint()) {
                int randomTriangle = (int) (Math.random() * tris.size());
                int randomGet = (int) (Math.random() * 3);
                switch (randomGet) {
                    case 1:
                        this.getPatrolPoint().set(tris.get(randomTriangle).get1());
                        break;
                    case 2:
                        this.getPatrolPoint().set(tris.get(randomTriangle).get2());
                        break;
                    case 3:
                        this.getPatrolPoint().set(tris.get(randomTriangle).get3());
                        break;
                    default:
                        break;
                }
            }
        } else if (this.getPosition().distance(this.getPatrolPoint()) > 1) {
            this.setTimeBetweenChaningPatrolPoint(Managers.getInstance().getTimer().getTimeInSeconds() + 5.f);
        }
    }

    default void randomPatrol() {
        this.findRandomPoint(this.getTriangles(this.getMaxPatrolDistance()));

        lookAtTarget(this.getPatrolPoint());
        if (!this.navigateTo(this.getPatrolPoint())) {
            this.getPatrolPoint().set(new Vector3f(0, 0, 0));
        }
    }

    default void updateLastTargetPosition() {
        if (this.isTargetVisible()) {
            this.getLastTargetPosition().set(this.getTarget().getPosition());
        }
    }

}
