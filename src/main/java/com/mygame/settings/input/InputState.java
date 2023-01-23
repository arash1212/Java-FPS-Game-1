/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.settings.input;

import com.jme3.math.Vector2f;

/**
 *
 * @author Arash
 */
public class InputState {

    private static final InputState instance = new InputState();

    private InputState() {
    }

    //Mouse
    public Vector2f mouseDeltaXY, mouseXY;

    //Movement
    public boolean isPressedLeft, isPressedRight, isPressedForward, isPressedBackward, isPressedJump, isPressedRun;

    //Weapons
    public boolean isPressedFire, isPressedAim;

    public static InputState getInstance() {
        return instance;
    }
}
