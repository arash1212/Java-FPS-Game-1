/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.ui;

import com.jme3.math.ColorRGBA;
import com.mygame.settings.Managers;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.IconComponent;

/**
 *
 * @author Arash
 */
public class TestUi extends Container {

    public TestUi() {
//        Button btn = new Button("Click Test ?");
//        Label label = this.addChild(new Label("Test test test ets tes tstse"));
//        label.setColor(ColorRGBA.Red);
//        this.addChild(btn);

        //Image 
        Panel panel = new Panel();
        IconComponent icon = new IconComponent("Textures/ui/Crosshair_Dot.png");
        panel.setBackground(icon);
        panel.setLocalTranslation(700, 100, 0);
        panel.setLocalScale(0.2f, 0.2f, 1);
        this.addChild(panel);
        this.setLocalTranslation(
                Managers.getInstance().getGuiViewPort().getCamera().getWidth() / this.getLocalScale().x / 2,
                Managers.getInstance().getGuiViewPort().getCamera().getHeight() / this.getLocalScale().y / 2,
                0);
    }
}
