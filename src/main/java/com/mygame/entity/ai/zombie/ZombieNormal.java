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
import com.jme3.bullet.animation.DacConfiguration;
import com.jme3.bullet.animation.DynamicAnimControl;
import com.jme3.bullet.animation.RangeOfMotion;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.Timer;
import com.mygame.entity.interfaces.AIControllable;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.interfaces.EnumActorGroup;
import com.mygame.entity.interfaces.EnumActorState;
import com.mygame.levels.Level1;
import com.mygame.settings.GeneralUtils;
import com.mygame.settings.Managers;
import java.util.List;

/**
 *
 * @author Arash
 */
public class ZombieNormal extends Node implements AIControllable {

    //Constants
    private static final float HEIGHT = 2.3f;
    private static final float TIME_TO_REMOVE_DEAD_BODY = 2.f;
    private static final float SPEED = 14;
    private static final float MAX_PATROL_DISTANCE = 25.f;
    private static final float DAMAGE_TO_PLAYER_RECOIL_AMOUNT = 0.3f;
    private static final EnumActorGroup GROUP = EnumActorGroup.ZOMBIE;
    private static final String PATH_TO_MODEL = "Models/zombies/zombieNormal/ZombieNormal.j3o";

    //attack constants
    private static final float ATTACK_JUMP_DURATION = 2;
    private static final float ATTACK_BITE_DURATION = 0.5f;
    private static final float MAX_ATTACK_DISTANCE = 4.f;
    private static final float TIME_BETWEEN_BITE_ATTACKS = 5.0f;

    //anim constants
    private static final String ANIM_ACTION_IDLE = "Idle";
    private static final String ANIM_ACTION_WALK = "Walk";
    private static final String ANIM_ACTION_RUN = "Run";
    //
    private static final String ANIM_ACTION_ATTACK1 = "Attack1";
    private static final String ANIM_ACTION_ATTACK2 = "Attack2";
    private static final String ANIM_ACTION_ATTACK3 = "Attack3";
    private static final String ANIM_ACTION_ATTACK4 = "Attack4";
    private static final String ANIM_ACTION_ATTACK_BITE = "Bite";
    private static final String ANIM_ACTION_JUMP_ATTACK_ONCE = "JumpAttackOnce";
    //
    private static final String ANIM_ACTION_REACT_TO_HIT1 = "ReactToHit1";
    private static final String ANIM_ACTION_REACT_TO_HIT2 = "ReactToHit2";
    private static final String ANIM_ACTION_REACT_TO_HIT3 = "ReactToHit3";
    private static final String ANIM_ACTION_REACT_TO_HIT_ONCE = "ReactToHitOnce";

    //Managers
    private final AssetManager assetManager;
    private final BulletAppState bullAppState;
    private final Node shootables;
    private final Timer timer;
    private final List<Actor> actors;

    private float currentTime = 0.f;
    private EnumActorState state = EnumActorState.STAND_STILL;

    //Health
    private float health = 100;
    private Actor attacker;
    private Actor grabber;

    //Navigation
    private CharacterControl control;
    private NavMeshPathfinder pathfinder;
    private NavMesh navMesh;
    private Actor target;
    private final Vector3f currentNavigationPosition = new Vector3f(0, 0, 0);
    private final Vector3f lastTargetPosition = new Vector3f(0, 0, 0);
    private Geometry navMeshGeom;

    //Patrol    
    private Vector3f initialPos;
    private final Vector3f patrolPoint = new Vector3f();
    private float timeBetweenChangingPoint = 2.f;

    //Detection
    private float detectionAmount = 0.0f;
    private boolean isFoundTarget = false;

    //Animatiom
    private AnimComposer animComposer;
    private EnumActorState currentState = EnumActorState.STAND_STILL;
    private Action actionReactToHit;
    private Action actionJumpAttackOnce;

    //Attack
    private float timeBetweenAttacks = 1.f;
    private float lastBiteAttackTime = 0;
    private boolean isBiting = false;
    private boolean isAttacking = false;
    private boolean isGrabbed = false;

    //Dead
    private float deadTime = 0;
    private boolean isDead = false;

    //Testing
    private Spatial model;
    DynamicAnimControl ragdoll;

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
        initReactToHitTweens(this.getState());

        CapsuleCollisionShape capsule = new CapsuleCollisionShape(0.6f, HEIGHT, 1);
        this.control = new CharacterControl(capsule, 0.301f);
        model.addControl(control);
        this.control.setSpatial(this);
        model.setLocalRotation(new Quaternion().fromAngles(0, 110, 0));

        this.bullAppState.getPhysicsSpace().add(control);
        this.shootables.attachChild(this);
        this.attachChild(model);
        this.addControl(control);

        this.initJumpAttackTween();

        this.animComposer.setCurrentAction("Idle");
        this.initRagdol();
    }

    @Override
    public void spawn(Vector3f spawnPoint) {
        this.init();
        this.initNavMesh();

        this.control.setPhysicsLocation(spawnPoint);
        this.initialPos = new Vector3f(this.getPosition());
    }

    @Override
    public void update(float tpf) {
        this.currentTime = this.timer.getTimeInSeconds();

        if (this.health > 0 || this.getPosition().y < -20) {
            this.setTarget(this.chooseTarget());

            this.updateActorState();

            this.updateAnimations();

            if (!isGrabbed) {
                //Patrol / follow Target
                if (!this.isFoundTarget) {
                    this.randomPatrol();
                } else if (this.isFoundTarget) {
                    this.navigateTo(this.getLastTargetPosition());
                }
                this.attack(tpf);
            }

            this.updateLastTargetPosition();

            this.updateDetection(tpf);

            this.updateLookAtPosition();
            //  this.lookAtTargetCloseDistance();
        }
        this.die();

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
        if (this.canMove() && this.getControl().onGround()) {
            if (state == EnumActorState.WALKING && this.currentState != EnumActorState.WALKING && !this.isAttacking) {
                this.animComposer.setCurrentAction(ANIM_ACTION_WALK);
                this.currentState = EnumActorState.WALKING;
            } else if (state == EnumActorState.RUNNING && this.currentState != EnumActorState.RUNNING) {
                System.out.println("run anim ?");
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
    public void applyDamage(float damage, Actor attacker) {
        this.health -= damage;

        initReactToHitTweens(this.getState());

//        this.setTarget(attacker);
        this.detectionAmount = 1;

        this.lookAtTarget(attacker.getPosition());
        this.getLastTargetPosition().set(attacker.getPosition());

        if (!this.isBiting) {
            this.animComposer.setCurrentAction(ANIM_ACTION_REACT_TO_HIT_ONCE);
        }
    }

    private void initJumpAttackTween() {
        Action jumpAtack = this.animComposer.action("JumpAttack");
        Tween doneTween = Tweens.callMethod(animComposer, "setCurrentAction", ANIM_ACTION_RUN);
        actionJumpAttackOnce = this.animComposer.actionSequence(ANIM_ACTION_JUMP_ATTACK_ONCE, jumpAtack, doneTween);
        actionJumpAttackOnce.setSpeed(1.5f);
    }

    private void initReactToHitTweens(EnumActorState state) {
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
        actionReactToHit = this.animComposer.actionSequence(ANIM_ACTION_REACT_TO_HIT_ONCE, reactToHitAction, doneTween);
        actionReactToHit.setSpeed(2.0f);
    }

    @Override
    public void die() {
        if (this.health <= 0 || this.getPosition().y < -20) {
            if (!isDead) {
                this.deadTime = this.currentTime;
                this.isDead = true;
                this.model.removeControl(control);
                this.bullAppState.getPhysicsSpace().remove(this.control);
                this.animComposer.reset();
                this.bullAppState.getPhysicsSpace().add(ragdoll);
                ragdoll.setRagdollMode();
                if (this.isBiting) {
                    this.getTarget().setGrabber(null);
                    this.target.setIsGrabbed(false);
                }
            } else {
                this.removeActor();
            }
        }
    }

    private void removeActor() {
        if (this.currentTime >= this.deadTime + TIME_TO_REMOVE_DEAD_BODY) {
            this.bullAppState.getPhysicsSpace().remove(ragdoll);
            this.shootables.detachChild(this);
            this.actors.remove(this);

            //Test
            ((Level1) Managers.getInstance().getCurrentlyLoadedLevel()).spawnZombies(this.initialPos);
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
     * ********************************AttackConditions*****************************************
     */
    private boolean canBiteAttack() {
        return this.target != null
                && this.isTargetInAttackRange()
                && !this.isAttacking
                && !this.target.isGrabbed()
                && !this.isBiting
                && this.isFoundTarget()
                && this.isTargetAtFront(this.getTarget())
                && this.isTargetCanBeSeen(this.getTarget())
                && this.currentTime > this.timeBetweenAttacks + ATTACK_BITE_DURATION
                && this.currentTime > lastBiteAttackTime + TIME_BETWEEN_BITE_ATTACKS
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE));
    }

    private boolean canJumpAttack() {
        return this.target != null
                && this.isFoundTarget()
                && this.isTargetAtFront(this.getTarget())
                && this.isTargetCanBeSeen(this.getTarget())
                && this.actionJumpAttackOnce != null
                && this.control.onGround()
                && !this.isAttacking
                && this.getPosition().distance(this.getTarget().getPosition()) > 7
                && this.getPosition().distance(this.getTarget().getPosition()) < 16
                && this.currentTime >= this.timeBetweenAttacks + ATTACK_JUMP_DURATION
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_ATTACK1))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_ATTACK2))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_ATTACK3))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_ATTACK4));
    }

    /**
     * ********************************AttackTypes*****************************************
     */
    private void biteAttack() {
        this.animComposer.setCurrentAction(ANIM_ACTION_ATTACK_BITE);
        timeBetweenAttacks = this.currentTime + 3.1f;
        this.lastBiteAttackTime = this.currentTime;
        this.currentState = EnumActorState.ATTACKING;
        this.isAttacking = true;
        this.isBiting = true;
        this.getTarget().getControl().setWalkDirection(new Vector3f(0, 0, 0));
        this.getTarget().setGrabber(this);
    }

    private void jumpAttack() {
        this.animComposer.setCurrentAction(ANIM_ACTION_JUMP_ATTACK_ONCE);
        this.jumpTowards(target.getPosition(), 15.f, 4.9f);
        this.isAttacking = true;
        timeBetweenAttacks = this.currentTime + 1.2f;
    }

    private void normalAttack() {
        this.animComposer.setCurrentAction(this.selectAttackAnimation());
        isAttacking = true;
        this.currentState = EnumActorState.ATTACKING;
    }

    /**
     * ********************************Attack*****************************************
     */
    @Override
    public float getMaxAttackDistance() {
        return MAX_ATTACK_DISTANCE;
    }

    private void lockTargetInPosition() {
        if (this.getTarget() == null) {
            return;
        }

        this.target.setIsGrabbed(true);
    }

    private boolean shouldLockTargetInPosition() {
        return this.isAttacking && currentTime < timeBetweenAttacks && this.health > 0 && this.isBiting;
    }

    private String selectAttackAnimation() {
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

        timeBetweenAttacks = this.currentTime + (randomNum == 1 ? 1.46f : (randomNum == 2 ? 1.18f : 1.54f));
        return currentAttackAnimation;
    }

    @Override
    public void attack(float tpf) {

        if (this.canBiteAttack()) {
            this.biteAttack();
        }

        if (this.shouldLockTargetInPosition()) {
            this.lockTargetInPosition();
        }

        if (this.canJumpAttack()) {
            this.jumpAttack();
        }

        if (this.isFoundTarget && this.control.onGround()) {
            if (!this.isAttacking && !this.isBiting && this.isTargetInAttackRange() && currentTime > timeBetweenAttacks) {
                normalAttack();
            }
        }

        this.updateAfterAttack();
    }

    private void updateAfterAttack() {
        if (this.isAttacking && currentTime > timeBetweenAttacks) {
            if (this.isTargetInAttackRange()) {
                this.target.applyDamage(25, this);
            }
            this.isAttacking = false;
            if (this.getTarget() != null && this.isBiting) {
                this.target.setIsGrabbed(false);
                this.getTarget().setGrabber(null);
            }
            this.isBiting = false;
        }
    }

    @Override
    public boolean canMove() {
        return this.health > 0
                && !this.isAttacking
                && !this.isGrabbed
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_JUMP_ATTACK_ONCE));
    }

    private void initRagdol() {
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

    @Override
    public void setPosition(Vector3f position) {
        this.control.setPhysicsLocation(position);
    }

    @Override
    public boolean isGrabbed() {
        return this.isGrabbed;
    }

    @Override
    public void setIsGrabbed(boolean grabbed) {
        this.isGrabbed = grabbed;
    }

    @Override
    public boolean isDeath() {
        return this.isDead;
    }

    @Override
    public float getDamageRecoilAmount() {
        return DAMAGE_TO_PLAYER_RECOIL_AMOUNT;
    }

    @Override
    public EnumActorGroup getGroup() {
        return GROUP;
    }

    @Override
    public void setGrabber(Actor grabber) {
        this.grabber = grabber;
    }

    @Override
    public Actor getGrabber() {
        return this.grabber;
    }

    @Override
    public NavMesh getNamMesh() {
        return this.navMesh;
    }

    @Override
    public void setNavMesh(NavMesh navmesh) {
        this.navMesh = navmesh;
    }

}
