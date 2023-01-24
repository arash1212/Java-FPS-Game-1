/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.ai.zombie;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.Armature;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.animation.DacConfiguration;
import com.jme3.bullet.animation.DynamicAnimControl;
import com.jme3.bullet.animation.RangeOfMotion;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Triangle;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.Timer;
import com.mygame.entity.interfaces.AIControllable;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.interfaces.EnumActorState;
import com.mygame.settings.GeneralConstants;
import com.mygame.settings.GeneralUtils;
import com.mygame.settings.Managers;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arash
 */
public class ZombieNormal extends Node implements AIControllable {

    //constants
    private static final float HEIGHT = 2.7f;
    private static final String PATH_TO_MODEL = "Models/zombies/zombieNormal/ZombieNormal.j3o";
    private static final float MAX_ATTACK_DISTANCE = 4.f;
    private static final float SPEED = 12;
    private static final float MAX_PATROL_DISTANCE = 25.f;

    //anim constants
    private static final String ANIM_ACTION_IDLE = "Idle";
    private static final String ANIM_ACTION_WALK = "Walk";
    private static final String ANIM_ACTION_RUN = "Run";
    //
    private static final String ANIM_ACTION_ATTACK1 = "Attack1";
    private static final String ANIM_ACTION_ATTACK2 = "Attack2";
    private static final String ANIM_ACTION_ATTACK3 = "Attack3";
    private static final String ANIM_ACTION_ATTACK4 = "Attack4";
    //
    private static final String ANIM_ACTION_REACT_TO_HIT1 = "ReactToHit1";
    private static final String ANIM_ACTION_REACT_TO_HIT2 = "ReactToHit2";
    private static final String ANIM_ACTION_REACT_TO_HIT3 = "ReactToHit3";
    private static final String ANIM_ACTION_REACT_TO_HIT_ONCE = "ReactToHitOnce";

    //Health
    private float health = 100;
    private Actor attacker;

    //Managers
    private final AssetManager assetManager;
    private final BulletAppState bullAppState;
    private final Node shootables;
    private final Timer timer;
    private final List<Actor> actors;

    //Navigation
    private CharacterControl control;
    private NavMeshPathfinder pathfinder;
    private Actor target;
    private final Vector3f currentNavigationPosition = new Vector3f(0, 0, 0);
    private final Vector3f lastTargetPosition = new Vector3f(0, 0, 0);
    private Geometry navMeshGeom;
    private List<Triangle> triangles;
    private boolean isAttacking = false;

    //Patrol
    private Vector3f patrolPoint = new Vector3f();
    private Vector3f initialPos;
    private float timeBetweenChangingPoint = 2.f;

    //detection
    private float detectionAmount = 0.0f;
    private boolean isFoundTarget = false;

    //Animatiom
    private AnimComposer animComposer;
    private EnumActorState currentState = EnumActorState.STAND_STILL;
    private Action reactToHit;

    private EnumActorState state = EnumActorState.STAND_STILL;

    //Attack
    private float timeBetweenAttacks = 1.f;
    private boolean isAlreadyAttacked = false;
    private float currentTime;

    //Dead
    private float timeToRemoveActor = 2.f;
    private boolean shouldRemoveActor = false;

    //tests
    private Spatial model;
    DynamicAnimControl ragdoll;

    Geometry testPatrolBox;

    public ZombieNormal() {
        this.assetManager = Managers.getInstance().getAsseManager();
        this.bullAppState = Managers.getInstance().getBulletAppState();
        this.shootables = Managers.getInstance().getShooteables();
        this.timer = Managers.getInstance().getTimer();
        this.actors = Managers.getInstance().getActors();
    }

    private void init() {
        model = this.assetManager.loadModel(PATH_TO_MODEL);
        this.animComposer = ((Node) model).getChild(0).getControl(AnimComposer.class);
        initTweens(this.getState());

        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.3f, HEIGHT, 1);
        this.control = new CharacterControl(capsule, 1.001f);
        model.addControl(control);
        this.control.setSpatial(this);
        model.setLocalRotation(new Quaternion().fromAngles(0, 110, 0));

        this.bullAppState.getPhysicsSpace().add(control);
        this.shootables.attachChild(this);
        this.attachChild(model);
        this.addControl(control);

        this.initialPos = new Vector3f(this.getPosition());
        this.animComposer.setCurrentAction("Idle");

        //Ragdoll (Test)
        ragdoll = new DynamicAnimControl();
        ragdoll.setMass(DacConfiguration.torsoName, 1f);
        ragdoll.link("mixamorig:Spine", 0.1f, new RangeOfMotion(5.1f, -5.1f, 5.1f, -5.1f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:Spine1", 0.1f, new RangeOfMotion(6.1f, -5.1f, 5.1f, -5.1f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:Spine2", 1.1f, new RangeOfMotion(6.4f, -5.4f, 5.8f, -5.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:Neck", 1.1f, new RangeOfMotion(0.4f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:Head", 0.1f, new RangeOfMotion(0.4f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:LeftShoulder", 0.1f, new RangeOfMotion(0.4f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:RightShoulder", 0.1f, new RangeOfMotion(0.4f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:RightForeArm", 0.1f, new RangeOfMotion(0.4f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:LeftForeArm", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:LeftUpLeg", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:RightUpLeg", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:RightLeg", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:LeftLeg", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:RightFoot", 0.1f, new RangeOfMotion(1f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.link("mixamorig:LeftFoot", 0.1f, new RangeOfMotion(2f, -0.4f, 0.8f, -0.8f, 0.4f, -0.4f));
        ragdoll.setEnabled(true);
        ((Node) this.model).getChild(0).addControl(ragdoll);
        ragdoll.physicsTick(this.bullAppState.getPhysicsSpace(), 1);
    }

    @Override
    public void spawn(Vector3f spawnPoint) {
        this.init();
        this.initNavMesh();

        this.control.setPhysicsLocation(spawnPoint);
    }

    @Override
    public void update(float tpf) {
        if (this.health > 0) {
            this.updateActorState();

            this.updateAnimations();

            //Patrol / follow Target
            if (!this.isFoundTarget) {
                this.randomPatrol();
            } else if (this.isFoundTarget) {
                lookAtTarget(this.getLastTargetPosition());
                this.navigateTo(this.getLastTargetPosition());
            }

            this.updateLastTargetPosition();

            this.updateDetection(tpf);

            this.attack();
        } else {
            this.die();
        }
    }

    @Override
    public EnumActorState getState() {
        return this.state;
    }

    @Override
    public void setState(EnumActorState state) {
        this.state = state;
    }

    @Override
    public CharacterControl getControl() {
        return this.control;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    /**
     * ********************************Animation*****************************************
     */
    public void updateAnimations() {
        if (this.canMove() && this.health > 0) {
            if (state == EnumActorState.WALKING && this.currentState != EnumActorState.WALKING && !this.isAttacking) {
                this.animComposer.setCurrentAction(ANIM_ACTION_WALK);
                this.currentState = EnumActorState.WALKING;
            } else if (state == EnumActorState.RUNNING && this.currentState != EnumActorState.RUNNING) {
                this.animComposer.setCurrentAction(ANIM_ACTION_RUN);
                this.currentState = EnumActorState.RUNNING;
            } else if ((state == EnumActorState.STAND_STILL || state == EnumActorState.IN_AIR) && this.currentState != EnumActorState.STAND_STILL) {
                this.animComposer.setCurrentAction(ANIM_ACTION_IDLE);
                this.currentState = EnumActorState.STAND_STILL;
            }
        }
    }

    @Override
    public NavMeshPathfinder getPathfinder() {
        return this.pathfinder;
    }

    @Override
    public Actor getTarget() {
        return this.target;
    }

    @Override
    public void setTarget(Actor target) {
        this.target = target;
    }

    @Override
    public void takeDamage(float damage, Actor attacker) {
        this.health -= damage;

        this.isAlreadyAttacked = false;

        initTweens(this.getState());

        if (target == null) {
            this.setTarget(attacker);
        }
        if (this.detectionAmount < 1) {
            this.detectionAmount = 1;
        }
        this.lookAtTarget(attacker.getPosition());
        this.animComposer.setCurrentAction(ANIM_ACTION_REACT_TO_HIT_ONCE);
    }

    private void initTweens(EnumActorState state) {
        int randomNum = GeneralUtils.randomInt(1, 3);
        String currentAttackAnimation = "";
        switch (randomNum) {
            case 1:
                currentAttackAnimation = ANIM_ACTION_REACT_TO_HIT1;
                break;
            case 2:
                currentAttackAnimation = ANIM_ACTION_REACT_TO_HIT2;
                break;
            case 3:
                currentAttackAnimation = ANIM_ACTION_REACT_TO_HIT3;
                break;
            default:
                break;
        }
        Action reactToHitAction = this.animComposer.action(currentAttackAnimation);
        Tween doneTween = doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_IDLE);
        if (state == EnumActorState.WALKING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_WALK);
        } else if (state == EnumActorState.RUNNING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_RUN);
        }
        reactToHit = this.animComposer.actionSequence(ANIM_ACTION_REACT_TO_HIT_ONCE, reactToHitAction, doneTween);
        reactToHit.setSpeed(2.0f);
    }

    @Override
    public void die() {
        if (this.health <= 0) {
            this.model.removeControl(control);
            this.bullAppState.getPhysicsSpace().remove(this.control);
            this.animComposer.reset();
            this.bullAppState.getPhysicsSpace().add(ragdoll);
            ragdoll.setRagdollMode();

            this.removeActor();
        }
    }

    private void removeActor() {
        if (!this.shouldRemoveActor) {
            this.shouldRemoveActor = true;
            this.timeToRemoveActor = this.timer.getTimeInSeconds() + this.timeToRemoveActor;
        }
        if (this.shouldRemoveActor && this.timer.getTimeInSeconds() >= this.timeToRemoveActor) {
            this.bullAppState.getPhysicsSpace().remove(ragdoll);
            this.shootables.detachChild(this);
            this.actors.remove(this);
        }
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    @Override
    public Vector3f getCurrentNavigationPosition() {
        return this.currentNavigationPosition;
    }

    @Override
    public Vector3f getLastTargetPosition() {
        return this.lastTargetPosition;
    }

    @Override
    public void setPathfinder(NavMeshPathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public void setDetectionAmount(float amount) {
        this.detectionAmount = amount;
    }

    @Override
    public float getDetectionAmount() {
        return this.detectionAmount;
    }

    @Override
    public boolean isFoundTarget() {
        return this.isFoundTarget;
    }

    @Override
    public void setIsFoundTarget(boolean found) {
        this.isFoundTarget = found;
    }

    /**
     * ********************************Attack*****************************************
     */
    @Override
    public float getMaxAttackDistance() {
        return MAX_ATTACK_DISTANCE;
    }

    @Override
    public void attack() {
        if (this.isFoundTarget) {
            this.currentTime = this.timer.getTimeInSeconds();

            if (!this.isAlreadyAttacked && this.canAttack() && currentTime > timeBetweenAttacks) {
                isAttacking = true;
                int randomNum = GeneralUtils.randomInt(1, 2);
                String currentAttackAnimation = "";
                switch (randomNum) {
                    case 1:
                        currentAttackAnimation = ANIM_ACTION_ATTACK1;
                        break;
                    case 2:
                        currentAttackAnimation = ANIM_ACTION_ATTACK2;
                        break;
                    case 3:
                        currentAttackAnimation = ANIM_ACTION_ATTACK3;
                        break;
                    case 4:
                        currentAttackAnimation = ANIM_ACTION_ATTACK4;
                        break;
                    default:
                        break;
                }

                this.animComposer.setCurrentAction(currentAttackAnimation);
                timeBetweenAttacks = this.timer.getTimeInSeconds() + (randomNum == 1 ? 1.46f : 1.18f);
                isAlreadyAttacked = true;
                this.currentState = EnumActorState.ATTACKING;
            }

            //apply damage
            if (this.isAlreadyAttacked && this.canAttack() && currentTime > timeBetweenAttacks) {
                this.target.takeDamage(25, this);
                this.isAlreadyAttacked = false;
            }
            if (this.isAttacking && currentTime > timeBetweenAttacks + 0.4f) {
                isAttacking = false;
            }
        }

        if (!this.canAttack() && isAlreadyAttacked) {
            this.isAlreadyAttacked = false;
        }
    }

    @Override
    public boolean canMove() {
        return !this.isAttacking
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE));

    }

    @Override
    public Geometry getNavMeshGeom() {
        return this.navMeshGeom;
    }

    @Override
    public void setNavMeshGeom(Geometry navMeshGeom) {
        this.navMeshGeom = navMeshGeom;
    }

    @Override
    public float getSpeed() {
        return SPEED;
    }

    @Override
    public Vector3f getPatrolPoint() {
        return this.patrolPoint;
    }

    @Override
    public Vector3f getInitiaPos() {
        return this.initialPos;
    }

    @Override
    public float getTimeBetweenChaningPatrolPoint() {
        return this.timeBetweenChangingPoint;
    }

    @Override
    public void setTimeBetweenChaningPatrolPoint(float newTime) {
        this.timeBetweenChangingPoint = newTime;
    }

    @Override
    public float getMaxPatrolDistance() {
        return MAX_PATROL_DISTANCE;
    }

    @Override
    public void setCurrentNavigationPosition(Vector3f position) {
        this.currentNavigationPosition.set(position);
    }

}
