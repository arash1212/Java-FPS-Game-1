/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.ai.zombie;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.entity.interfaces.AIControllable;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.interfaces.EnumActorState;
import com.mygame.settings.GeneralConstants;
import com.mygame.settings.GeneralUtils;
import com.mygame.settings.Managers;
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
    private float currentTime = 0;

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

    //Navigation
    private CharacterControl control;
    private NavMeshPathfinder pathfinder;
    private Actor target;
    private final Vector3f currentNavigationPosition = new Vector3f(0, 0, 0);
    private final Vector3f lastTargetPosition = new Vector3f(0, 0, 0);

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

    public ZombieNormal() {
        this.assetManager = Managers.getInstance().getAsseManager();
        this.bullAppState = Managers.getInstance().getBulletAppState();
        this.shootables = Managers.getInstance().getShooteables();
    }

    private void init() {
        Spatial model = this.assetManager.loadModel(PATH_TO_MODEL);
        this.animComposer = ((Node) model).getChild(0).getControl(AnimComposer.class);

        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.3f, HEIGHT, 1);
        this.control = new CharacterControl(capsule, 1.001f);
        model.addControl(control);
        this.control.setSpatial(this);
        model.setLocalRotation(new Quaternion().fromAngles(0, 110, 0));

        this.bullAppState.getPhysicsSpace().add(control);
        this.shootables.attachChild(this);
        this.attachChild(model);
        this.addControl(control);

        this.animComposer.setCurrentAction("Idle");

    }

    @Override
    public void spawn(Vector3f spawnPoint) {
        this.init();
        this.initNavMesh();

        this.control.setPhysicsLocation(spawnPoint);
    }

    @Override
    public void update(float tpf) {
        this.currentTime += tpf;

        // if (this.pathfinder == null && Managers.getInstance().getCurrentlyLoadedLevel() != null) {
        //     // this.target = Managers.getInstance().getPlayer();
        // }
        this.updateActorState();

        this.updateAnimations();

        //Test
        if (this.isFoundTarget) {
            lookAtTarget(this.getTarget());
            this.navigateTo(this.lastTargetPosition);
        }

        this.die();

        if (this.isTargetVisible()) {
            this.lastTargetPosition.set(this.getTarget().getPosition());
        }

        this.updateDetection(tpf);

        this.attack();
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
        if (state == EnumActorState.WALKING && this.currentState != EnumActorState.WALKING) {
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

        initTweens(this.getState());

        if (target == null) {
            this.setTarget(attacker);
        }
        if (this.detectionAmount < 1) {
            this.detectionAmount = 1;
        }
        this.lookAtTarget(attacker);
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
            this.bullAppState.getPhysicsSpace().remove(this.control);
            this.shootables.detachChild(this);
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
            if (!this.isAlreadyAttacked && this.canAttack() && currentTime > timeBetweenAttacks) {
                int randomNum = GeneralUtils.randomInt(1, 2);
                System.out.println("random : " + randomNum);
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
                timeBetweenAttacks = currentTime + (randomNum == 1 ? 1.46f : 1.18f);
                isAlreadyAttacked = true;
            }

            if (this.isAlreadyAttacked && this.canAttack() && currentTime > timeBetweenAttacks) {
                this.target.takeDamage(25, this);
                isAlreadyAttacked = false;
            }
        }

        if (!this.canAttack() && isAlreadyAttacked) {
            this.isAlreadyAttacked = false;
        }
    }

    @Override
    public boolean canMove() {
        return this.animComposer.getCurrentAction() != this.animComposer.action(ANIM_ACTION_ATTACK1)
                || this.animComposer.getCurrentAction() != this.animComposer.action(ANIM_ACTION_ATTACK2)
                || this.animComposer.getCurrentAction() != this.animComposer.action(ANIM_ACTION_ATTACK3)
                || this.animComposer.getCurrentAction() != this.animComposer.action(ANIM_ACTION_ATTACK4)
                || this.animComposer.getCurrentAction() != this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE);

    }

}
