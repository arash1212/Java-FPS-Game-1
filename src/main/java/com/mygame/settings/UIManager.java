/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.settings;

import com.jme3.scene.Node;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.player.Player;
import com.mygame.ui.Crosshair;

/**
 *
 * @author Arash
 */
public class UIManager {

    private final static UIManager instance = new UIManager();

    private final Node guiNode = Managers.getInstance().getGuiNode();

    private final Actor player = Managers.getInstance().getPlayer();
    private final Crosshair crosshair = new Crosshair();

    private UIManager() {
    }

    public static UIManager getInstance() {
        return instance;
    }

    public void update(float tpf) {
        crosshair.update(tpf);
    }

    public void showInGameUI() {
        displayCrosshair();
    }

    /**
     * ********************************Crosshair*****************************************
     */
    public void displayCrosshair() {
        this.crosshair.show();
    }

    public void hideCrosshair() {
        this.crosshair.hide();
    }

    public Crosshair getCrosshair() {
        return crosshair;
    }
}
