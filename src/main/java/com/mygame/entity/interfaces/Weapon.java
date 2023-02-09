/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.entity.interfaces;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.settings.Managers;
import com.mygame.settings.UIManager;
import com.mygame.settings.input.InputState;

/**
 *
 * @author Arash
 */
public interface Weapon {

    void select();

    void updateAnimations(EnumActorState state);

    void update(float tpf);

    void fire();

    boolean isSingleShot();

    void setIsAiming(boolean isAiming);

    boolean isAiming();

    float getDamage();

    Camera getCamera();

    Spatial getModel();

    Quaternion getDefaultRotation();

    Vector3f getDefaultPosition();

    Quaternion getAimRotation();

    Vector3f getAimPosition();

    default CollisionResults rayTo(Vector3f origin, Vector3f direction, Node node) {
        Ray ray = new Ray(origin, direction);
        CollisionResults results = new CollisionResults();
        node.collideWith(ray, results);
        return results;
    }

    default float calculateDamage(Spatial hitObject) {
        if (hitObject.getName().contains("Head")) {
            return this.getDamage() * 4.f;
        } else if (hitObject.getName().contains("Body")) {
            return this.getDamage() * 2.f;
        } else {
            return this.getDamage();
        }
    }

    void setOwner(Actor owner);

    Actor getOwner();

    default void applyDamageToTarget(CollisionResults results) {
//        for (CollisionResult result : results) {
        if (results.getClosestCollision() != null) {
            Spatial result = results.getClosestCollision().getGeometry();
            Spatial hitObject = result;
            Spatial actorObject = result;
            System.out.println("pistol hit :" + hitObject.getName());
            while (!(actorObject instanceof Actor)) {
                if (actorObject.getParent() != null) {
                    actorObject = actorObject.getParent();
                } else {
                    break;
                }
            }
            if (actorObject instanceof Actor) {
                ((Actor) actorObject).applyDamage(this.calculateDamage(hitObject), this.getOwner());
                UIManager.getInstance().getCrosshair().showHitMarker();
//            break;
            }
        }
//        }
    }

    default void updatePosition(float tpf, Spatial model, Vector3f defaultPos, Quaternion defaultRot, Vector3f aimPos, Quaternion aimRot) {
        if (this.isAiming()) {
            model.getLocalTranslation().interpolateLocal(aimPos, tpf * 12);
            model.setLocalRotation(aimRot);
        } else {
            model.getLocalTranslation().interpolateLocal(defaultPos, tpf * 12);
            model.setLocalRotation(defaultRot);
        }
    }

    //TODO
    default void sway(float tpf) {
        if (InputState.getInstance().mouseDeltaXY != null) {
            float mouseX = ((InputState.getInstance().mouseDeltaXY.getX() / 1024) * 2);
            float mouseY = ((InputState.getInstance().mouseDeltaXY.getY() / 1024) * 2);

            if (this.isAiming()) {
                mouseX -= this.getAimRotation().getY() * 2f;
                mouseY += this.getAimRotation().getZ();
                this.getModel().getLocalTranslation().interpolateLocal(this.getAimPosition(), tpf * 12);
            } else {
                mouseX -= this.getDefaultRotation().getY() * 2.1f;
                mouseY += this.getDefaultRotation().getX() * 3.1f;
                this.getModel().getLocalTranslation().interpolateLocal(this.getDefaultPosition(), tpf * 12);
            }

            Quaternion rotationX = new Quaternion().fromAngleAxis(-mouseY, new Vector3f(1, 0, 0));
            Quaternion rotationY = new Quaternion().fromAngleAxis(-mouseX, new Vector3f(0, 1, 0));

            Quaternion finalRotation = rotationX.mult(rotationY);
            this.getModel().getLocalRotation().slerp(finalRotation, 8 * tpf);
        }
    }

}
