/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.ai.soldiers;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.anim.AnimComposer;
import com.jme3.anim.Joint;
import com.jme3.anim.SkinningControl;
import com.jme3.anim.tween.Tween;
import com.jme3.anim.tween.Tweens;
import com.jme3.anim.tween.action.Action;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData.DataType;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.animation.DacConfiguration;
import com.jme3.bullet.animation.DynamicAnimControl;
import com.jme3.bullet.animation.RangeOfMotion;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResults;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.Timer;
import com.mygame.entity.ai.zombie.ZombieNormal;
import com.mygame.entity.interfaces.AIControllable;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.interfaces.EnumActorGroup;
import com.mygame.entity.interfaces.EnumActorState;
import com.mygame.entity.player.Player;
import com.mygame.levels.Level1;
import com.mygame.settings.GeneralUtils;
import com.mygame.settings.Managers;
import java.util.List;

/**
 *
 * @author Arash
 */
public class SoldierAiNormal extends Node implements AIControllable {
    //Constants

    private static final float HEIGHT = 2.8f;
    private static final float TIME_TO_REMOVE_DEAD_BODY = 2.f;
    private static final float MAX_ATTACK_DISTANCE = 60.f;
    private static final float MAX_KICK_DISTANCE = 5.f;
    private static final float SPEED = 17;
    private static final float MAX_PATROL_DISTANCE = 30.f;
    private static final float DAMAGE_TO_PLAYER_RECOIL_AMOUNT = 0.00001f;
    private static final EnumActorGroup GROUP = EnumActorGroup.GROUP1;
    private static final String PATH_TO_MODEL = "Models/soldiers/soldierNormal/SoldierNormal.j3o";

    //
    private static final String PATH_TO_SHOOT_SOUND = "Sounds/Weapons/AK_Shoot_Sound.wav";

    //attack constants
    //anim constants
    private static final String ANIM_ACTION_IDLE = "Idle";
    private static final String ANIM_ACTION_WALK = "Walk";
    private static final String ANIM_ACTION_WALK_AIM = "WalkAiming";
    private static final String ANIM_ACTION_WALK_FIRE = "WalkFiring";
    private static final String ANIM_ACTION_RUN = "Run";
    private static final String ANIM_ACTION_AIM_IDLE = "IdleAim";
    //
    private static final String ANIM_ACTION_FIRE = "Fire";
    private static final String ANIM_ACTION_FIRE2 = "Fire2";
    private static final String ANIM_ACTION_ATTACK2 = "Attack2";
    private static final String ANIM_ACTION_KICK = "Kick";
    private static final String ANIM_ACTION_BLOCK = "Block";
    private static final String ANIM_ACTION_FIRE_ONCE = "FireOnce";
    private static final String ANIM_ACTION_KICK_ONCE = "KickOnce";
    //
    private static final String ANIM_ACTION_REACT_TO_HIT1 = "ReactToHit1";
    private static final String ANIM_ACTION_REACT_TO_HIT2 = "ReactToHit2";
    private static final String ANIM_ACTION_REACT_TO_HIT3 = "ReactToHit3";
    private static final String ANIM_ACTION_REACT_TO_HIT_ONCE = "ReactToHitOnce";

    //sound constants
    private static final String PATH_TO_FOUND_ZOMBIE_SOUND_1 = "Sounds/EnemySoldierSounds/EnemyFoundZombie.wav";
    private static final String PATH_TO_FOUND_ZOMBIE_SOUND_2 = "Sounds/EnemySoldierSounds/EnemyFoundZombie2.wav";
    private static final String PATH_TO_FOUND_ZOMBIE_SOUND_3 = "Sounds/EnemySoldierSounds/EnemySoldierFoundZombie3.wav";
    private static final String PATH_TO_FOUND_ZOMBIE_SOUND_4 = "Sounds/EnemySoldierSounds/EnemySoldierFoundZombie4.wav";

    private static final String PATH_TO_FOUND_PLAYER_SOUND_1 = "Sounds/EnemySoldierSounds/EnemySoldierFoundPlayer.wav";
    private static final String PATH_TO_FOUND_PLAYER_SOUND_2 = "Sounds/EnemySoldierSounds/EnemySoldierFoundPlayer_2.wav";
    private static final String PATH_TO_FOUND_PLAYER_SOUND_3 = "Sounds/EnemySoldierSounds/EnemySoldierFoundPlayer_3.wav";

    private static final String PATH_TO_DEAD_SOUND_1 = "Sounds/EnemySoldierSounds/EnemySoldierDeadSound_1.wav";
    private static final String PATH_TO_DEAD_SOUND_2 = "Sounds/EnemySoldierSounds/EnemySoldierDeadSound_2.wav";
    private static final String PATH_TO_DEAD_SOUND_3 = "Sounds/EnemySoldierSounds/EnemySoldierDeadSound_3.wav";
    private static final String PATH_TO_DEAD_SOUND_4 = "Sounds/EnemySoldierSounds/EnemySoldierDeadSound_4.wav";
    private static final String PATH_TO_DEAD_SOUND_5 = "Sounds/EnemySoldierSounds/EnemySoldierDeadSound_5.wav";

    private static final String PATH_TO_GRABBED_SOUND_1 = "Sounds/EnemySoldierSounds/EnemySoldierGrabbedSound_1.wav";
    private static final String PATH_TO_GRABBED_SOUND_2 = "Sounds/EnemySoldierSounds/EnemySoldierGrabbedSound_2.wav";
    private static final String PATH_TO_GRABBED_SOUND_3 = "Sounds/EnemySoldierSounds/EnemySoldierGrabbedSound_3.wav";

    //Managers
    private final AssetManager assetManager;
    private final BulletAppState bullAppState;
    private final Node shootables;
    private final Timer timer;
    private final List<Actor> actors;

    private float currentTime = 0.f;
    private EnumActorState state = EnumActorState.STAND_STILL;

    //Sounds
    private AudioNode shootSound;
    private AudioNode detectionSound;
    private AudioNode deadSound;
    private AudioNode grabbedSound;

    private boolean isDeadSoundPlayed = false;
    private boolean isGrabbedSoundPlayed = false;
    private boolean isDetectionSoundPlayed = false;

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
    private Action actionFireOnce;
    private Action actionKickOnce;

    //Attack
    private float timeBetweenShots = 1.f;
    private float timeBetweenKicks = 1.f;
    private int shootCount = 0;
    private boolean isGrabbed = false;
    private boolean isKicked = false;
    private boolean shouldChangePosition = false;
    private final Vector3f positionToMoveToAfterAttack = new Vector3f(1, 0, 1);

    //Dead
    private float deadTime = 0;
    private boolean isDead = false;

    //Testing
    private Spatial model;
    private DynamicAnimControl ragdoll;
    private Spatial navigationObject;

    //MuzzleFlash
    private SkinningControl skinningControl;
    private Joint rightHandJoint;
    private Spatial muzzleFlash;

    public SoldierAiNormal() {
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

        //navigationObject
//        Box box = new Box(1, 1, 1);
//        navigationObject = new Geometry("test", box);
//        Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        navigationObject.setMaterial(mat);
//        Managers.getInstance().getRootNode().attachChild(navigationObject);
        CapsuleCollisionShape capsule = new CapsuleCollisionShape(0.6f, HEIGHT, 1);
        this.control = new CharacterControl(capsule, 0.301f);
        model.addControl(control);
        this.control.setSpatial(this);
        model.setLocalRotation(new Quaternion().fromAngles(0, 109.86f, 0));

        this.bullAppState.getPhysicsSpace().add(control);
        this.shootables.attachChild(this);
        this.attachChild(model);
        this.addControl(control);

        this.animComposer.setCurrentAction("Idle");
        this.initRagdol();

        initKickTween();
        initShootTween();

        this.shootSound = new AudioNode(this.assetManager, PATH_TO_SHOOT_SOUND, DataType.Buffer);
        this.shootSound.setVolume(5);

        initMuzzleFlash();
        this.hideMuzzleFlash();
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

        if (this.health > 0) {
            this.setTarget(this.chooseTarget());

            this.updateActorState();

            this.updateAnimations();

            if (!isGrabbed) {
                //Patrol / follow Target
                if (!this.isFoundTarget) {
                    this.randomPatrol();
                    this.isDetectionSoundPlayed = false;
                    this.shouldChangePosition = false;
                    this.shootCount = 0;
                } else if (this.isFoundTarget) {
                    if (this.getDistanceToTarget(target) >= MAX_ATTACK_DISTANCE || !this.isTargetVisible(target)) {
                        this.navigateTo(this.getLastTargetPosition());
                        this.shouldChangePosition = false;
                    } else if (!this.shouldChangePosition()) {
                        this.control.setWalkDirection(new Vector3f(0, 0, 0));
                        this.state = EnumActorState.STAND_STILL;
                    }

                    if (!this.isDetectionSoundPlayed) {
                        //this.playDetectionSound();
                    }
                    this.attack(tpf);
                }
                this.isGrabbedSoundPlayed = false;
            } else {
                //grabbedd ?
                this.playGrabbedSound();
            }

            this.updateLastTargetPosition();

            //this.lookAtTargetCloseDistance();
            this.updateDetection(tpf);

            if (!this.isGrabbed) {
                this.updateLookAtPosition();
            }

            this.updateAfterAttack();

        }
        this.die();

        //testing
//        this.navigationObject.setLocalTranslation(patrolPoint);
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
        if (isGrabbed && this.currentState != EnumActorState.GRABBED) {
            this.animComposer.setCurrentAction(ANIM_ACTION_BLOCK);
            if (this.grabber != null) {
                this.lookAtTarget(this.grabber.getPosition());
            }
            this.currentState = EnumActorState.GRABBED;
        }

        if (!isGrabbed && this.canMove()) {
            if (state == EnumActorState.WALKING && this.currentState != EnumActorState.WALKING) {
                this.animComposer.setCurrentAction(ANIM_ACTION_WALK);
                this.currentState = EnumActorState.WALKING;

            } else if (state == EnumActorState.RUNNING) {
                if (this.distanceToTarget(target) >= MAX_ATTACK_DISTANCE && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_RUN))) {
                    this.animComposer.setCurrentAction(ANIM_ACTION_RUN);
                } else if (this.distanceToTarget(target) <= MAX_ATTACK_DISTANCE && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_WALK_AIM))) {
                    this.animComposer.setCurrentAction(ANIM_ACTION_WALK_AIM);
                }
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
        this.checkIfShouldSetTargetToAttacker(attacker);
        if (!isGrabbed) {
            //   this.lookAtTarget(attacker.getPosition());
        }
        this.getLastTargetPosition().set(attacker.getPosition());

        this.shootCount = 0;
        this.detectionAmount = 1;

        this.attacker = attacker;

        // if (this.animComposer != null && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))) {
        this.animComposer.setCurrentAction(ANIM_ACTION_REACT_TO_HIT_ONCE);
        // }

    }

    /**
     * ********************************Tweens*****************************************
     */
    private void initKickTween() {
        Action kickAtack = this.animComposer.action(ANIM_ACTION_KICK);
        Tween doneTween = Tweens.callMethod(animComposer, "setCurrentAction", ANIM_ACTION_IDLE);
        if (state == EnumActorState.WALKING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_WALK);
        } else if (state == EnumActorState.RUNNING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_RUN);
        }
        actionKickOnce = this.animComposer.actionSequence(ANIM_ACTION_KICK_ONCE, kickAtack, doneTween);
        actionKickOnce.setSpeed(1.5f);
    }

    private void initShootTween() {
        Action shootAttack = this.animComposer.action(ANIM_ACTION_FIRE2);
        Tween doneTween = Tweens.callMethod(animComposer, "setCurrentAction", ANIM_ACTION_IDLE);
        if (state == EnumActorState.WALKING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_WALK);
        } else if (state == EnumActorState.RUNNING) {
            doneTween = Tweens.callMethod(this.animComposer, "setCurrentAction", ANIM_ACTION_RUN);
        }
        actionFireOnce = this.animComposer.actionSequence(ANIM_ACTION_FIRE_ONCE, shootAttack, doneTween);
        actionFireOnce.setSpeed(1.5f);
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
                this.playDeadSound();
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
            ((Level1) Managers.getInstance().getCurrentlyLoadedLevel()).spawnEnemySoldier(this.initialPos);
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
    private boolean canShoot() {
        return this.isTargetInAttackRange()
                && this.getPosition().distance(this.target.getPosition()) > MAX_KICK_DISTANCE
                && this.isTargetVisible(this.getTarget())
                && this.currentTime >= this.timeBetweenShots
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_KICK_ONCE))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE));
    }

    private boolean canKick() {
        return this.isTargetInAttackRange()
                && this.currentTime >= this.timeBetweenKicks
                && this.isTargetVisible(this.getTarget())
                && this.getPosition().distance(this.getTarget().getPosition()) <= MAX_KICK_DISTANCE
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE));
    }

    private boolean shouldChangePosition() {
        return this.shouldChangePosition || this.getTarget() != null && this.target.getPosition().distance(this.getPosition()) < 8;
    }

    /**
     * ********************************AttackTypes*****************************************
     */
    private void applyDamageToTarget() {
        Vector3f pos = this.getTarget().getPosition();
        pos.x += Math.random() * 2.f;
        pos.y += Math.random() * 2.f;
        pos.z += Math.random() * 2.f;

        List<PhysicsRayTestResult> results = this.physicsRayTo(this.getPosition(), pos);
        if (!results.isEmpty() && results.get(0).getCollisionObject().getUserObject() == this.getTarget()) {
            this.target.applyDamage(5, this);
        }
    }

    private void shoot() {
        if (this.shootCount < 8) {
            this.showMuzzleFlash();
            this.state = EnumActorState.SHOOTING;
            if (!this.isWalking()) {
                this.animComposer.setCurrentAction(ANIM_ACTION_FIRE_ONCE);
            }
            this.timeBetweenShots = this.currentTime + 0.08f;
            this.shootSound.playInstance();
            this.shootCount++;
            //  this.shouldChangePosition = false;
            applyDamageToTarget();

        } else {
            this.shootCount = 0;
            timeBetweenShots = this.currentTime + 2.1f;
            this.shouldChangePosition = true;

            if (this.distanceToTarget(target) < 10) {
                positionToMoveToAfterAttack.set(this.findRandomPoint(target.getPosition()));
            } else {
                positionToMoveToAfterAttack.set(this.findRandomPoint(this.getPosition()));
            }
            this.hideMuzzleFlash();
        }
    }

    private void kick(float tpf) {
        if (!this.isKicked && this.currentTime >= this.timeBetweenKicks) {
            this.animComposer.setCurrentAction(ANIM_ACTION_KICK_ONCE);
            this.timeBetweenKicks = this.currentTime + 0.3f;
            this.isKicked = true;
        }

        if (this.isKicked && this.currentTime >= this.timeBetweenKicks) {
            //push
            this.timeBetweenKicks = this.currentTime + 1.f;
            Vector3f pushDirection = this.getPosition().subtract(target.getPosition());
            if (this.getTarget().getControl().onGround()) {
                this.getTarget().getControl().setWalkDirection(pushDirection.normalize().negate().multLocal(3));
            }

            this.target.applyDamage(25, this);
            this.isKicked = false;
            this.shouldChangePosition = true;
            positionToMoveToAfterAttack.set(this.findRandomPoint(target.getPosition()));
        }
    }

    /**
     * ********************************Attack*****************************************
     */
    private Vector3f findRandomPoint(Vector3f around) {
        int count = 0;
        Vector3f point = this.getRandomPointOnNavMesh(around, 10);
        while (point.isSimilar(positionToMoveToAfterAttack, 0.001f) && count < 30
                || !this.isTargetCanBeSeenFrom(point, target) && count < 10
                || this.target.getPosition().distance(point) > MAX_ATTACK_DISTANCE && count < 10
                || this.target.getPosition().distance(point) < 4 && count < 10) {
            point = this.getRandomPointOnNavMesh(this.getTarget().getPosition(), MAX_ATTACK_DISTANCE - 5);
            count++;
        }
        return point;
    }

    @Override
    public float getMaxAttackDistance() {
        return MAX_ATTACK_DISTANCE;
    }

    @Override
    public void attack(float tpf) {
        if (this.control.onGround()) {
            if (this.canShoot()) {
                this.shoot();
            } else if (this.canKick()) {
                this.kick(tpf);
            } else {
                this.state = EnumActorState.WALKING;
            }

            if (this.shouldChangePosition() && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))) {
                this.navigateTo(positionToMoveToAfterAttack);
            } else if (this.shouldChangePosition() && this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))) {
                this.control.setWalkDirection(new Vector3f(0, 0, 0));
            }
        }
    }

    private void updateAfterAttack() {
        if (currentTime > timeBetweenShots) {
            this.hideMuzzleFlash();
        }
    }

    /**
     * ********************************Sounds*****************************************
     */
    private void selectZombieDetectionSound() {
        int randomNum = GeneralUtils.randomInt(1, 4);
        switch (randomNum) {
            case 1:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_ZOMBIE_SOUND_1, DataType.Buffer);
                break;
            case 2:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_ZOMBIE_SOUND_2, DataType.Buffer);
                break;
            case 3:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_ZOMBIE_SOUND_3, DataType.Buffer);
                break;
            case 4:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_ZOMBIE_SOUND_4, DataType.Buffer);
                break;
            default:
                break;
        }

        this.detectionSound.setVolume(4);
    }

    private void playDetectionSound() {
        if (this.getTarget() instanceof ZombieNormal) {
            this.selectZombieDetectionSound();
            this.detectionSound.play();
            this.isDetectionSoundPlayed = true;
        } else if (this.getTarget() instanceof Player) {
            this.selectFoundPlayerSound();
            this.detectionSound.play();
            this.isDetectionSoundPlayed = true;
        }
    }

    private void selectDeadSound() {
        int randomNum = GeneralUtils.randomInt(1, 5);
        switch (randomNum) {
            case 1:
                this.deadSound = new AudioNode(this.assetManager, PATH_TO_DEAD_SOUND_1, DataType.Buffer);
                break;
            case 2:
                this.deadSound = new AudioNode(this.assetManager, PATH_TO_DEAD_SOUND_2, DataType.Buffer);
                break;
            case 3:
                this.deadSound = new AudioNode(this.assetManager, PATH_TO_DEAD_SOUND_3, DataType.Buffer);
                break;
            case 4:
                this.deadSound = new AudioNode(this.assetManager, PATH_TO_DEAD_SOUND_4, DataType.Buffer);
                break;
            case 5:
                this.deadSound = new AudioNode(this.assetManager, PATH_TO_DEAD_SOUND_5, DataType.Buffer);
                break;
            default:
                break;
        }

        this.deadSound.setVolume(4);
    }

    private void playDeadSound() {
        if (!this.isDeadSoundPlayed) {
            this.selectDeadSound();
            this.deadSound.playInstance();
            this.isDeadSoundPlayed = true;
        }
    }

    private void selectGrabbedSound() {
        int randomNum = GeneralUtils.randomInt(1, 3);
        switch (randomNum) {
            case 1:
                this.grabbedSound = new AudioNode(this.assetManager, PATH_TO_GRABBED_SOUND_1, DataType.Buffer);
                break;
            case 2:
                this.grabbedSound = new AudioNode(this.assetManager, PATH_TO_GRABBED_SOUND_2, DataType.Buffer);
                break;
            case 3:
                this.grabbedSound = new AudioNode(this.assetManager, PATH_TO_GRABBED_SOUND_3, DataType.Buffer);
                break;
            default:
                break;
        }
        this.grabbedSound.setVolume(4);
    }

    private void playGrabbedSound() {
        if (!this.isGrabbedSoundPlayed) {
            this.selectGrabbedSound();
            this.grabbedSound.playInstance();
            this.isGrabbedSoundPlayed = true;
        }
    }

    private void selectFoundPlayerSound() {
        int randomNum = GeneralUtils.randomInt(1, 3);
        switch (randomNum) {
            case 1:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_PLAYER_SOUND_1, DataType.Buffer);
                break;
            case 2:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_PLAYER_SOUND_2, DataType.Buffer);
                break;
            case 3:
                this.detectionSound = new AudioNode(this.assetManager, PATH_TO_FOUND_PLAYER_SOUND_1, DataType.Buffer);
                break;
            default:
                break;
        }
        this.detectionSound.setVolume(4);
    }

    @Override
    public boolean canMove() {
        return this.health > 0
                && (!this.state.equals(EnumActorState.ATTACKING)
                && !this.state.equals(EnumActorState.SHOOTING)
                && !this.isGrabbed
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE)))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_KICK_ONCE))
                || this.shouldChangePosition && !isGrabbed
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_REACT_TO_HIT_ONCE))
                && !this.animComposer.getCurrentAction().equals(this.animComposer.action(ANIM_ACTION_KICK_ONCE));
    }

    /**
     * ********************************Ragdoll*****************************************
     */
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

    /**
     * ********************************MuzzleFlash*****************************************
     */
    private void initMuzzleFlash() {
        PointLight muzzleLight = new PointLight();
        muzzleLight.setColor(ColorRGBA.Yellow);
        muzzleLight.setRadius(10f);

        this.skinningControl = ((Node) model).getChild(0).getControl(SkinningControl.class);
        this.rightHandJoint = skinningControl.getArmature().getJoint("mixamorig:RightHand");
        this.muzzleFlash = this.assetManager.loadModel("Models/weapons/muzzleFlash1/MuzzleFlash.j3o");
        this.muzzleFlash.setLocalScale(34.9f, 34.9f, 34.9f);
        this.muzzleFlash.setLocalTranslation(13, 209.f, -5);
        this.muzzleFlash.setLocalRotation(new Quaternion().fromAngles(0, 90, 7.8f));
        this.muzzleFlash.setShadowMode(RenderQueue.ShadowMode.Off);
        skinningControl.getAttachmentsNode(this.rightHandJoint.getName()).attachChild(muzzleFlash);

        muzzleLight.setPosition(new Vector3f(muzzleFlash.getLocalTranslation()));
        skinningControl.getAttachmentsNode(this.rightHandJoint.getName()).addLight(muzzleLight);

    }

    private void hideMuzzleFlash() {
        this.skinningControl.getAttachmentsNode(rightHandJoint.getName()).detachChild(muzzleFlash);
    }

    private void showMuzzleFlash() {
        this.skinningControl.getAttachmentsNode(rightHandJoint.getName()).attachChild(muzzleFlash);
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
        return (float) (Math.random() * (0.1f - DAMAGE_TO_PLAYER_RECOIL_AMOUNT) + DAMAGE_TO_PLAYER_RECOIL_AMOUNT);
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
        return grabber;
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
