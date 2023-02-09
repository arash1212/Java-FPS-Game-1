/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.ui;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.mygame.settings.Managers;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.IconComponent;

/**
 *
 * @author Arash
 */
public class Crosshair extends Container {

    private final float DEFAULT_EXPAND_AMOUNT = 40.f;

    private final String PATH_TO_CROSSHAIR_HORIZONTAL = "Textures/ui/Crosshair_Horizontal.png";
    private final String PATH_TO_CROSSHAIR_VERTICAL = "Textures/ui/Crosshair_Vertical.png";
    private final String PATH_TO_HIT_MARKER = "Textures/ui/HitMarker2.png";

    private AssetManager assetManager;
    private Node guiNode;

    private Panel top;
    private Panel right;
    private Panel bottom;
    private Panel left;
    private Panel hitMarker;

    private boolean shouldExpand = false;
    private float expandAmount = 40.f;

    private final Vector3f CENTER = new Vector3f(
            Managers.getInstance().getGuiViewPort().getCamera().getWidth() / this.getLocalScale().x / 2 + 1,
            Managers.getInstance().getGuiViewPort().getCamera().getHeight() / this.getLocalScale().y / 2 - 4,
            0);

    //
    private final Vector3f RIGHT_DEFFAULT_POS = new Vector3f(CENTER.x + 34, CENTER.y, CENTER.z);
    private final Vector3f LEFT_DEFFAULT_POS = new Vector3f(CENTER.x - 37, CENTER.y, CENTER.z);
    private final Vector3f TOP_DEFFAULT_POS = new Vector3f(CENTER.x + 3, CENTER.y + 40, CENTER.z);
    private final Vector3f BOTTOM_DEFFAULT_POS = new Vector3f(CENTER.x + 3, CENTER.y - 30, CENTER.z);

    private final Vector3f HIT_MARKER_POS = new Vector3f(CENTER.x - 16, CENTER.y + 16.5f, CENTER.z);

    private final Vector3f SCALE = new Vector3f(0.08f, 0.08f, 1);

    public Crosshair() {
        this.guiNode = Managers.getInstance().getGuiNode();
        this.setLocalTranslation(CENTER);
        this.right = this.getPanel(PATH_TO_CROSSHAIR_HORIZONTAL, RIGHT_DEFFAULT_POS, SCALE);
        this.left = this.getPanel(PATH_TO_CROSSHAIR_HORIZONTAL, LEFT_DEFFAULT_POS, SCALE);
        this.top = this.getPanel(PATH_TO_CROSSHAIR_VERTICAL, TOP_DEFFAULT_POS, SCALE);
        this.bottom = this.getPanel(PATH_TO_CROSSHAIR_VERTICAL, BOTTOM_DEFFAULT_POS, SCALE);
        this.hitMarker = this.getPanel(PATH_TO_HIT_MARKER, HIT_MARKER_POS, new Vector3f(0.5f, 0.5f, 0.5f));
    }

    public void show() {
        this.guiNode.attachChild(right);
        this.guiNode.attachChild(left);
        this.guiNode.attachChild(top);
        this.guiNode.attachChild(bottom);
        this.guiNode.attachChild(hitMarker);
    }

    public void hide() {
        this.guiNode.detachChild(right);
        this.guiNode.detachChild(left);
        this.guiNode.detachChild(top);
        this.guiNode.detachChild(bottom);
        // this.guiNode.detachChild(hitMarker);
    }

    public void update(float tpf) {
        if (shouldExpand) {
            this.expand(tpf);
        } else {
            this.resetToDefault(tpf);
        }

        if (hitMarker.getAlpha() > 0) {
            hideHitMarker(tpf);
        }
    }

    private Panel getPanel(String pathToImage, Vector3f position, Vector3f scale) {
        Panel panel = new Panel();
        IconComponent image = new IconComponent(pathToImage);//
        panel.setBackground(image);
        panel.setLocalTranslation(position);//
        panel.setLocalScale(scale);//
        return panel;
    }

    /**
     * ********************************HitMarker*****************************************
     */
    public void showHitMarker() {
        this.hitMarker.setAlpha(1);
        System.out.println("show hitMarker");
    }

    private void hideHitMarker(float tpf) {
        this.hitMarker.setAlpha(this.hitMarker.getAlpha() - tpf);
    }

    /**
     * ********************************CrosshairAnim*****************************************
     */
    private void expand(float tpf) {
        this.animate(tpf, right, RIGHT_DEFFAULT_POS, true, expandAmount);
        this.animate(tpf, left, LEFT_DEFFAULT_POS, true, -expandAmount);
        this.animate(tpf, top, TOP_DEFFAULT_POS, false, expandAmount);
        this.animate(tpf, bottom, BOTTOM_DEFFAULT_POS, false, -expandAmount);
    }

    private void resetToDefault(float tpf) {
        this.backToDefaultPos(tpf, top, TOP_DEFFAULT_POS);
        this.backToDefaultPos(tpf, bottom, BOTTOM_DEFFAULT_POS);
        this.backToDefaultPos(tpf, left, LEFT_DEFFAULT_POS);
        this.backToDefaultPos(tpf, right, RIGHT_DEFFAULT_POS);
    }

    private void animate(float tpf, Panel panel, Vector3f defaultPos, boolean isHorizontal, float amount) {
        if (isHorizontal) {
            panel.setLocalTranslation(panel.getLocalTranslation().interpolateLocal(new Vector3f(defaultPos.x + amount, defaultPos.y, defaultPos.z), tpf * 4));
        } else {
            panel.setLocalTranslation(panel.getLocalTranslation().interpolateLocal(new Vector3f(defaultPos.x, defaultPos.y + amount, defaultPos.z), tpf * 4));
        }
    }

    private void backToDefaultPos(float tpf, Panel panel, Vector3f defaultPos) {
        panel.setLocalTranslation(panel.getLocalTranslation().interpolateLocal(defaultPos, tpf * 4));
    }

    public boolean isShouldExpand() {
        return shouldExpand;
    }

    public void setShouldExpand(boolean shouldExpand) {
        this.shouldExpand = shouldExpand;
    }

    public float getExpandAmount() {
        return expandAmount;
    }

    public void setExpandAmount(float expandAmount) {
        this.expandAmount = expandAmount;
    }

    public float getDefaultExpandAmount() {
        return DEFAULT_EXPAND_AMOUNT;
    }

}
