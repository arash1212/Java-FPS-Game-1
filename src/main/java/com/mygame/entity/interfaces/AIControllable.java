/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.interfaces;

import com.jme3.ai.navmesh.Cell;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.Path.Waypoint;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.math.FastMath;
import com.jme3.math.Triangle;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.mygame.settings.GeneralConstants;
import com.mygame.settings.Managers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Arash
 */
public interface AIControllable extends Actor {
    
    float MAX_IGNORED_ATTACK_DISTANCE = 0.55f;
    float MIN_DISTANCE_TO_WAYPOINTS = 0.25f;

    // void initNavMesh();
    NavMeshPathfinder getPathfinder();
    
    void setPathfinder(NavMeshPathfinder pathfinder);
    
    Actor getTarget();
    
    void setTarget(Actor target);
    
    Vector3f getCurrentNavigationPosition();
    
    void setCurrentNavigationPosition(Vector3f position);
    
    NavMesh getNamMesh();
    
    void setNavMesh(NavMesh navmesh);
    
    @Override
    default void updateActorState() {
        if (!this.getControl().onGround()) {
            this.setState(EnumActorState.IN_AIR);
        } //        else if (this.getTarget() != null && this.isTargetInAttackRange() && this.isTargetVisible(this.getTarget())) {
        ////            this.getControl().setWalkDirection(new Vector3f(0, 0, 0));
        //            this.setState(EnumActorState.ATTACKING);
        //        } 
        else if (this.getPathfinder().getNextWaypoint() != null && !this.isGrabbed()) {
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
    
    default boolean isTargetInAttackRange() {
        if (this.getTarget() == null) {
            return false;
        }
        
        float distanceToTarget = this.getPosition().distance(this.getTarget().getPosition());
        return distanceToTarget <= this.getMaxAttackDistance() + MAX_IGNORED_ATTACK_DISTANCE;
    }
    
    default void lookAtTargetCloseDistance() {
        if (this.getTarget() == null) {
            return;
        }
        if (!this.isFoundTarget() || this.getPosition().distance(this.getTarget().getPosition()) > 15) {
            return;
        }
        
        this.lookAtTarget(this.getTarget().getPosition());
    }
    
    void attack(float tpf);

    /**
     * ********************************Navigation*****************************************
     */
    float getSpeed();
    
    Geometry getNavMeshGeom();
    
    void setNavMeshGeom(Geometry navMeshGeom);
    
    default void initNavMesh() {
        Node node = (Node) Managers.getInstance().getAsseManager().loadModel(Managers.getInstance().getCurrentlyLoadedLevel().getPathToScene());
        this.setNavMeshGeom((Geometry) node.getChild(GeneralConstants.NAV_MESH_NAME));
        this.setNavMesh(new NavMesh(this.getNavMeshGeom().getMesh()));
        this.setPathfinder(new NavMeshPathfinder(this.getNamMesh()));
    }
    
    default boolean navigateTo(Vector3f position) {
        if (this.canMove()) {
            if (this.getPathfinder() == null) {
                System.out.println("This Actor Have No Pathfinder assigned.");
                return false;
            }
            
            this.getPathfinder().setPosition(this.getPosition());
            
            if (!(this.getCurrentNavigationPosition().isSimilar(position, MAX_IGNORED_ATTACK_DISTANCE))) {
                this.setCurrentNavigationPosition(position);
                // this.getPathfinder().clearPath();
                this.getPathfinder().computePath(this.getCurrentNavigationPosition());
                //System.out.println("computing new path ?");
            }
            
            this.getControl().setWalkDirection(new Vector3f(0, 0, 0));
            
            Waypoint waypoint = this.getPathfinder().getNextWaypoint();
            if (waypoint == null) {
                //System.out.println("no waypoint");
                return false;
            }
            
            Vector3f waypointDirection = waypoint.getPosition().subtract(this.getControl().getPhysicsLocation());
            this.getControl().setWalkDirection(waypointDirection.normalize().divide(this.getState().equals(EnumActorState.WALKING) ? this.getSpeed() : this.getSpeed() / 3.5f));
            
            Vector2f currentPosition = new Vector2f(this.getPosition().x, this.getPosition().z);
            Vector2f waypoint2D = new Vector2f(waypoint.getPosition().x, waypoint.getPosition().z);
            float distance = currentPosition.distance(waypoint2D);
            if (distance < MIN_DISTANCE_TO_WAYPOINTS && this.getPathfinder().getNextWaypoint() != null && !this.getPathfinder().isAtGoalWaypoint()) {
                this.getPathfinder().goToNextWaypoint();
            }
            
            if (this.getPathfinder().isAtGoalWaypoint()) {
                this.getPathfinder().clearPath();
                this.getControl().setWalkDirection(new Vector3f(0, 0, 0));
                // System.out.println("at goal point");
            }
        } else {
            //  System.out.println("cant walk ?");
            if (!this.getState().equals(EnumActorState.IN_AIR)) {
                this.getControl().setWalkDirection(new Vector3f(0, 0, 0));
            }
            this.getPathfinder().computePath(this.getPosition());
            this.setCurrentNavigationPosition(this.getPosition());
            return false;
        }
        
        return true;
    }
    
    default void updateLookAtPosition() {
        if (this.getControl().onGround() && !this.isGrabbed()) {
            if (this.getTarget() != null && this.isFoundTarget()) {
                if (this.isTargetCanBeSeen(this.getTarget()) && this.distanceToTarget(this.getTarget()) < this.getMaxAttackDistance()) {
                    lookAtTarget(this.getLastTargetPosition());
                } else {
                    if (this.getPathfinder().getNextWaypoint() != null) {
                        lookAtTarget(this.getPathfinder().getNextWaypoint().getPosition());
                    } else {
                        lookAtTarget(this.getLastTargetPosition());
                    }
                }
            } else {
                if (this.getPathfinder().getNextWaypoint() != null) {
                    lookAtTarget(this.getPathfinder().getNextWaypoint().getPosition());
                } else {
                    lookAtTarget(this.getPatrolPoint());
                }
            }
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
        if (this.getTarget() == null) {
            return;
        }
        
        if (this.getDetectionAmount() < 1) {
            this.increaseDetectionAmount(tpf);
            
            if (this.isFoundTarget() && this.getDetectionAmount() < 0.2f) {
                this.losetarget();
            }
        } else {
            this.setIsFoundTarget(true);
            this.decreaseDetectionAmount(tpf);
        }
    }
    
    default void increaseDetectionAmount(float tpf) {
        if (this.isTargetVisible(this.getTarget())) {
            this.setDetectionAmount(FastMath.clamp(this.getDetectionAmount() + this.getDetectionSpeed(tpf), 0, 1));
        } else {
            this.setDetectionAmount(FastMath.clamp(this.getDetectionAmount() - tpf / 4, 0, 1));
        }
    }
    
    default void decreaseDetectionAmount(float tpf) {
        float distanceToTarget = this.getPosition().distance(this.getLastTargetPosition());
        if (distanceToTarget > 2 && distanceToTarget <= MIN_DISTANCE_TO_WAYPOINTS + 1 && !this.isTargetCanBeSeen(this.getTarget())) {
            this.setDetectionAmount(FastMath.clamp(this.getDetectionAmount() - tpf, 0, 1));
        }
    }
    
    default boolean isTargetVisible(Actor target) {
        return this.isTargetAtFront(target) && isTargetCanBeSeen(target);
    }
    
    default boolean isTargetAtFront(Actor target) {
        Vector3f targetDir = this.getPosition().subtract(target.getPosition()).normalize();
        float dot = this.getControl().getViewDirection().normalize().dot(targetDir);
        return dot >= 0.4f && dot <= 1;
    }
    
    default boolean isTargetCanBeSeen(Actor target) {
        List<PhysicsRayTestResult> results = this.physicsRayTo(this.getPosition(), target.getPosition());
        if (!results.isEmpty()) {
            return results.get(0).getCollisionObject().getUserObject().equals(target);
        } else {
            return false;
        }
    }
    
    default boolean isTargetCanBeSeenFrom(Vector3f from, Actor target) {
        List<PhysicsRayTestResult> results = this.physicsRayTo(from, target.getPosition());
        if (!results.isEmpty()) {
            return results.get(0).getCollisionObject().getUserObject().equals(target);
        } else {
            return false;
        }
    }
    
    default void losetarget() {
        this.setIsFoundTarget(false);
        this.setTarget(null);
    }
    
    default float distanceToTarget(Actor target) {
        return this.getPosition().distance(target.getPosition());
    }
    
    Vector3f getLastTargetPosition();
    
    void setDetectionAmount(float amount);
    
    float getDetectionAmount();
    
    boolean isFoundTarget();
    
    void setIsFoundTarget(boolean found);

    /**
     * ********************************PatrolOld*****************************************
     */
    float getMaxPatrolDistance();
    
    Vector3f getPatrolPoint();
    
    Vector3f getInitiaPos();
    
    float getTimeBetweenChaningPatrolPoint();
    
    void setTimeBetweenChaningPatrolPoint(float newTime);
    
    @Deprecated
    default List<Triangle> getTriangles(Vector3f from, float distance) {
        List<Triangle> triangles = new ArrayList();
        if (this.getNavMeshGeom() != null) {
            int[] indices = new int[3];
            for (int i = 0; i < this.getNavMeshGeom().getMesh().getTriangleCount(); i++) {
                Triangle tri = new Triangle();
                this.getNavMeshGeom().getMesh().getTriangle(i, tri);
                if (tri.getCenter().distance(from) <= distance) {
                    triangles.add(tri);
                }
            }
        }
        return triangles;
    }
    
    @Deprecated
    default Vector3f findRandomPoint(List<Triangle> tris) {
        try {
            //find new random patrol point
            int randomTriangle = (int) (Math.random() * tris.size());
            int randomGet = (int) (Math.random() * 4);
            switch (randomGet) {
                case 1:
                    return this.getPatrolPoint().set(tris.get(randomTriangle).get1());
                case 2:
                    return this.getPatrolPoint().set(tris.get(randomTriangle).get2());
                case 3:
                    return this.getPatrolPoint().set(tris.get(randomTriangle).get3());
                case 4:
                    return this.getPatrolPoint().set(tris.get(randomTriangle).getCenter());
                default:
                    break;
            }
        } catch (Exception e) {
            return this.getPatrolPoint();
        }
        return this.getPatrolPoint();
    }

//    @Deprecated
    default void randomPatrol() {
        if (this.getPatrolPoint().equals(Vector3f.ZERO) || this.getPosition().distance(this.getPatrolPoint()) <= MIN_DISTANCE_TO_WAYPOINTS) {
            if (Managers.getInstance().getTimer().getTimeInSeconds() > this.getTimeBetweenChaningPatrolPoint()) {
//                this.findRandomPoint(this.getTriangles(this.getInitiaPos(), this.getMaxPatrolDistance()));
//                System.out.println("reached ? ");
                this.getPatrolPoint().set(this.getRandomPointOnNavMesh(this.getInitiaPos(), this.getMaxPatrolDistance()));
            }
        }

        //update TimeBetweenChaningPatrolPoint time if actor is not close to his patrol point
        if (!this.getPatrolPoint().equals(Vector3f.ZERO) && this.getPosition().distance(this.getPatrolPoint()) > MIN_DISTANCE_TO_WAYPOINTS + 2) {
            this.setTimeBetweenChaningPatrolPoint(Managers.getInstance().getTimer().getTimeInSeconds() + 5.f);
//            System.out.println("Not reached ? ");
        }
        
        if (!this.navigateTo(this.getPatrolPoint())) {
            //    if (Managers.getInstance().getTimer().getTimeInSeconds() > this.getTimeBetweenChaningPatrolPoint()) {
            // this.randomPatrol();
            this.getPatrolPoint().set(this.getRandomPointOnNavMesh(this.getInitiaPos(), this.getMaxPatrolDistance()));
            // System.out.println("new point ?");
            //     }
            // System.out.println("patrol point not reachable");
        }
    }
    
    default void updateLastTargetPosition() {
        if (this.getTarget() == null) {
            return;
        }
        
        if (this.isTargetVisible(this.getTarget())) {
            this.getLastTargetPosition().set(this.getTarget().getPosition());
        }
    }

    /**
     * ********************************PatrolNew*****************************************
     */
    default List<Cell> getCells(float distance) {
        List<Cell> cells = new ArrayList();
        NavMesh navMesh = this.getNamMesh();
        for (int i = 0; i < navMesh.getNumCells(); i++) {
            Cell cell = navMesh.getCell(i);
            if (cell.getCenter().distance(this.getPosition()) <= distance || cell.contains(this.getPosition())) {
                cells.add(cell);
            }
        }
        return cells;
    }
    
    default Vector3f getRandomPointOnNavMesh(Vector3f around, float distance) {
        try {
            List<Cell> cells = this.getCells(distance);
            System.out.println("Cell size : " + cells.size());
            int randomCellNumber = (int) (Math.random() * cells.size());
            Cell cell = cells.get(randomCellNumber);
            
            float f1 = FastMath.nextRandomFloat();
            float f2 = FastMath.nextRandomFloat();
            return this.getNamMesh().snapPointToCell(cell,
                    cell.getTriangle()[0].mult(1 - FastMath.sqrt(f1))
                            .add(cell.getTriangle()[1].mult(FastMath.sqrt(f1) * (1 - f2)))
                            .add(cell.getTriangle()[2].mult(FastMath.sqrt(f1 * f2))
                            )
            );
        } catch (Exception e) {
            return this.getPosition();
        }
    }

    /**
     * ********************************targetSelectors*****************************************
     */
    default List<Actor> getHostileActors() {
        return Managers.getInstance().getActors()
                .stream().filter(x -> !x.getGroup().equals(this.getGroup()))
                .sorted(Comparator.comparing(x -> this.getDistanceToTarget(x))).collect(Collectors.toList());
    }
    
    default Actor chooseTarget() {
        List<Actor> hostiles = this.getHostileActors();
        Iterator<Actor> iterator = hostiles.iterator();
        
        while (iterator.hasNext()) {
            Actor actor = iterator.next();
            float distanceToActor = this.getDistanceToTarget(actor);
            if (this.getTarget() == null || this.getTarget().isDeath() || distanceToActor < 5 && distanceToActor < this.getDistanceToTarget(this.getTarget())) {
                if (this.isTargetVisible(actor)) {
                    return actor;
                }
            }
        }
        
        return this.getTarget();
    }
    
    default void checkIfShouldSetTargetToAttacker(Actor attacker) {
        if (this.getTarget() == null || this.distanceToTarget(attacker) < this.distanceToTarget(this.getTarget()) || this.getTarget().isDeath()) {
            this.setTarget(attacker);
        }
    }
}
