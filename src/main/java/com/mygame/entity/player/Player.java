/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.player;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.system.Timer;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.interfaces.EnumActorGroup;
import com.mygame.entity.interfaces.EnumActorState;
import com.mygame.entity.interfaces.Weapon;
import com.mygame.entity.weapons.pistol.PistolMakarove;
import com.mygame.settings.Managers;
import com.mygame.settings.input.InputState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Arash
 */
public class Player extends Node implements Actor {

    //constants
    private static final float MOVEMENT_SPEED = 6;
    private static final float RUN_SPEED = 3;
    private static final float GRAVITY_SPEED = 20;
    private static final float JUMP_SPEED = 10;
    private static final float HEIGHT = 1.8f;
    private static final float MIN_CAMERA_X = -1.4F;
    private static final float MAX_CAMERA_X = 1.4F;

    private static final EnumActorGroup GROUP = EnumActorGroup.GROUP2;

    private float currentSpeed = 6;
    private float currentFov = 50f;

    //Managers
    private final InputState inputState;
    private final AssetManager assetManager;
    private final Camera cam;
    private final BulletAppState bulletAppSate;
    private final Node rootNode;
    private final CameraNode cameraNode;
    private final Node shootables;

    //actor specifics
    private CharacterControl control;

    //Movement
    private final Vector3f camDir = new Vector3f();
    private final Vector3f camLeft = new Vector3f();
    private final Vector3f walkDirection = new Vector3f();
    private EnumActorState state = EnumActorState.STAND_STILL;
    private final Vector3f camPosition = new Vector3f(0, 0, 0);
    private final float mouseSensitivity = 1.1f;
    private boolean isGrabbed = false;

    //Weapons
    private final List<Weapon> weapons = new ArrayList(3);
    private Weapon selectedWeapon;

    //Health
    private float health = 100;
    private Actor attacker;
    private Actor grabber;

    //HeadBob
    private final Timer timer;
    private float headBobTimer = 0;
    private final float headBobWalkSpeed = 8.8f;
    private final float headBobRunSpeed = 13.6f;
    private final float headBobAmount = 0.2f;

    //Camera
    private Vector3f viewDirection = new Vector3f();
    private final float angles[] = {0, 0, 0};
    private final Quaternion tmpRot = new Quaternion();
    private final Node cameraBase = new Node();
    private float cameraBaseY = 0;
    private float cameraBaseX = 0;

    //recoil 
    private Vector3f currentRotation = new Vector3f();
    private Vector3f targetRotation = new Vector3f();
    private final float recoilSpeed = 25;
    private final float returnSpeed = 2;

    public Player() {
        this.inputState = InputState.getInstance();
        this.assetManager = Managers.getInstance().getAsseManager();
        this.cam = Managers.getInstance().getCam();
        this.bulletAppSate = Managers.getInstance().getBulletAppState();
        this.rootNode = Managers.getInstance().getRootNode();
        this.cameraNode = Managers.getInstance().getCameraNode();
        this.shootables = Managers.getInstance().getShooteables();
        this.timer = Managers.getInstance().getTimer();
    }

    private void init() {

        CapsuleCollisionShape capsule = new CapsuleCollisionShape(1.3f, HEIGHT, 1);
        control = new CharacterControl(capsule, 0.1f);
        control.setSpatial(this);
        this.control.setUseViewDirection(true);
        this.bulletAppSate.getPhysicsSpace().add(control);

        control.setGravity(GRAVITY_SPEED);
        control.setJumpSpeed(JUMP_SPEED);

        this.rootNode.attachChild(this);
        this.addControl(control);

        cameraBase.getLocalTransform().getTranslation().y += 2.0f;

        cameraBaseY = cameraBase.getLocalTransform().getTranslation().y;
        cameraBaseX = cameraBase.getLocalTransform().getTranslation().x;
        cameraBase.attachChild(cameraNode);
        this.attachChild(cameraBase);
    }

    @Override
    public void spawn(Vector3f spawnPoint) {
        this.init();

        this.control.setPhysicsLocation(spawnPoint);

        initWeapons();
    }

    @Override
    public void update(float tpf) {

        cameraBase.getLocalRotation().lookAt(this.control.getViewDirection(), Vector3f.UNIT_Z);

        this.cam.setFov(currentFov);

        updateCamera(tpf);

        if (this.canMove()) {
            this.updateMovements(tpf);
            this.headBob(tpf);
        }

        updateActorState();

        if (selectedWeapon != null) {
            selectedWeapon.update(tpf);
            selectedWeapon.updateAnimations(this.state);
        }

        this.fire();

        this.aim(tpf);

        this.recoil(tpf);

    }

    /**
     * ********************************Camera*****************************************
     */
    private void updateCameraRotation() {
        float v = inputState.mouseXY.getY() / 1024;

        cameraBaseX = FastMath.clamp(this.cameraBaseX + (-v), MIN_CAMERA_X, MAX_CAMERA_X);
        this.cameraBase.setLocalRotation(new Quaternion().fromAngles(cameraBaseX, 0, 0));
        if (inputState.mouseXY.y != 0) {
            inputState.mouseXY.y = 0;
        }
    }

    private void updateRotation() {
        float h = inputState.mouseXY.getX() / 1024;
        if (inputState.mouseXY.x != 0) {
            angles[1] += -h * mouseSensitivity;
            inputState.mouseXY.x = 0;
        }

        viewDirection = control.getViewDirection();
        viewDirection.set(Vector3f.UNIT_Z);
        tmpRot.fromAngles(angles);
        tmpRot.multLocal(viewDirection);
        control.setViewDirection(viewDirection);
    }

    private void updateCamera(float tpf) {
        Managers.getInstance().getInputManager().setCursorVisible(false);
        if (inputState.mouseDeltaXY == null) {
            return;
        }

        this.updateCameraRotation();
        this.updateRotation();
    }

    /**
     * ********************************Movement*****************************************
     */
    private void setCurrentSpeed() {
        if (this.state.equals(EnumActorState.WALKING) && currentSpeed < MOVEMENT_SPEED) {
            currentSpeed += 0.05f;
        } else if (this.state.equals(EnumActorState.RUNNING) && currentSpeed > RUN_SPEED) {
            currentSpeed -= 0.3f;
        } else if (this.state.equals(EnumActorState.STAND_STILL)) {
            currentSpeed = MOVEMENT_SPEED;
        }
    }

    private void updateMovements(float tpf) {
        if (!this.isGrabbed) {
            setCurrentSpeed();

            this.camPosition.set(this.getPosition().x, this.getPosition().y + HEIGHT, this.getPosition().z);

            this.camDir.set(cam.getDirection());
            this.camLeft.set(cam.getLeft());

            this.walkDirection.set(0, 0, 0);
            if (inputState.isPressedLeft) {
                this.walkDirection.addLocal(camLeft);
            }
            if (this.inputState.isPressedRight) {
                walkDirection.addLocal(camLeft.negate());
            }
            if (inputState.isPressedForward) {
                this.walkDirection.addLocal(camDir);
            }
            if (inputState.isPressedBackward) {
                this.walkDirection.addLocal(camDir.negate());
            }
            if (inputState.isPressedJump && this.canJump()) {
                this.control.jump();
            }

            this.walkDirection.y = 0;
            this.control.setWalkDirection(this.walkDirection.normalizeLocal().divide(currentSpeed));
        }
    }

    private void headBob(float tpf) {
        if (this.state == EnumActorState.WALKING || this.state == EnumActorState.RUNNING) {
            this.headBobTimer += tpf * (this.state == EnumActorState.WALKING ? this.headBobWalkSpeed : this.headBobRunSpeed);
            this.cameraBase.setLocalTranslation(
                    this.cameraNode.getLocalTranslation().x,
                    cameraBaseY + FastMath.sin(headBobTimer) * this.headBobAmount,
                    this.cameraNode.getLocalTranslation().z
            );
        } else {
            this.cameraBase.setLocalTranslation(
                    this.cameraNode.getLocalTranslation().x,
                    cameraBaseY,
                    this.cameraNode.getLocalTranslation().z
            );
        }
    }

    @Override
    public boolean isRunning() {
        return !this.control.getWalkDirection().equals(Vector3f.ZERO)
                && !this.inputState.isPressedBackward
                && this.inputState.isPressedForward
                && inputState.isPressedRun
                && !inputState.isPressedAim;
    }

    /**
     * ********************************Weapons*****************************************
     */
    private void initWeapons() {
        this.weapons.add(new PistolMakarove());

        this.selectedWeapon = this.weapons.get(0);
        this.selectedWeapon.select();
        this.selectedWeapon.setOwner(this);
    }

    private void fire() {
        if (this.inputState.isPressedFire) {
            this.selectedWeapon.fire();
            if (this.selectedWeapon.isSingleShot()) {
                this.inputState.isPressedFire = false;
            }
        }
    }

    private void aim(float tpf) {
        // System.out.println("fov : " + this.cam.getFov());
        if (this.selectedWeapon != null) {
            if (this.inputState.isPressedAim) {
                if (this.currentFov > 40) {
                    this.currentFov -= 40.4f * tpf;
                }
                this.selectedWeapon.setIsAiming(true);
            } else {
                if (this.currentFov < 50) {
                    this.currentFov += 40.4f * tpf;
                }
                this.selectedWeapon.setIsAiming(false);
            }
        }
    }

    private void recoil(float tpf) {
        this.targetRotation = new Vector3f().interpolateLocal(targetRotation, new Vector3f(0, 0, 0), returnSpeed * tpf);
        this.currentRotation = this.currentRotation.interpolateLocal(targetRotation, recoilSpeed * tpf);
        this.cameraNode.setLocalRotation(new Quaternion().fromAngles(currentRotation.x, currentRotation.y, currentRotation.z));
    }

    @Override
    public EnumActorState getState() {
        return state;
    }

    @Override
    public void setState(EnumActorState state) {
        this.state = state;
    }

    @Override
    public CharacterControl getControl() {
        return control;
    }

    @Override
    public void applyDamage(float damage, Actor attackers) {
        //this.health -= damage;

        this.targetRotation.addLocal(attackers.getDamageRecoilAmount(), 0, 0);
    }

    @Override
    public void die() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getHealth() {
        return this.health;
    }

    public void addRecoil(float recoilX, float recoilY, float recoilZ) {
        this.targetRotation.addLocal(recoilX, recoilY, recoilZ);
    }

    @Override
    public void setPosition(Vector3f position) {
        this.control.setPhysicsLocation(position);
    }

    public boolean isGrabbed() {
        return isGrabbed;
    }

    public void setIsGrabbed(boolean isGrabbed) {
        this.isGrabbed = isGrabbed;
    }

    @Override
    public boolean canMove() {
        return !this.isGrabbed;
    }

    @Override
    public boolean isDeath() {
        return false;
    }

    @Override
    public float getDamageRecoilAmount() {
        throw new UnsupportedOperationException("Not supported yet.");
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

}
