/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.settings.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.math.Vector2f;
import com.mygame.settings.Managers;
import java.util.Arrays;

/**
 *
 * @author Arash
 */
public class InputSettings {

    //constants
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String FORWARD = "forward";
    private static final String BACKWARD = "backward";
    private static final String JUMP = "jump";
    private static final String RUN = "run";
    private static final String FIRE = "fire";
    private static final String AIM = "aim";

    //
    private final InputManager inputManager;
    private final InputState inputState = InputState.getInstance();

    private Vector2f mouseDeltaXY = new Vector2f();
    private Vector2f mouseXY = new Vector2f();

    public InputSettings() {
        this.inputManager = Managers.getInstance().getInputManager();
        this.inputState.mouseDeltaXY = this.mouseDeltaXY;
        this.inputState.mouseXY = this.mouseXY;
    }

    public void initInputs() {
        this.inputManager.addMapping(LEFT, new KeyTrigger(KeyInput.KEY_A));
        this.inputManager.addMapping(RIGHT, new KeyTrigger(KeyInput.KEY_D));
        this.inputManager.addMapping(FORWARD, new KeyTrigger(KeyInput.KEY_W));
        this.inputManager.addMapping(BACKWARD, new KeyTrigger(KeyInput.KEY_S));
        this.inputManager.addMapping(JUMP, new KeyTrigger(KeyInput.KEY_SPACE));
        this.inputManager.addMapping(RUN, new KeyTrigger(KeyInput.KEY_LSHIFT));
        this.inputManager.addMapping(FIRE, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        this.inputManager.addMapping(AIM, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

//        inputManager.addMapping("Rotate_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true));
//        inputManager.addMapping("Rotate_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false));
        this.inputManager.addListener(this.actionListener, LEFT, RIGHT, FORWARD, BACKWARD, JUMP, RUN, FIRE, AIM);
        this.inputManager.addRawInputListener(this.inputListener);
//        this.inputManager.addListener(analogListener, "Rotate_Left", "Rotate_Right");
    }

    private final ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if (name.equals(LEFT)) {
                inputState.isPressedLeft = isPressed;
            }
            if (name.equals(RIGHT)) {
                inputState.isPressedRight = isPressed;
            }
            if (name.equals(FORWARD)) {
                inputState.isPressedForward = isPressed;
            }
            if (name.equals(BACKWARD)) {
                inputState.isPressedBackward = isPressed;
            }
            if (name.equals(JUMP)) {
                inputState.isPressedJump = isPressed;
            }
            if (name.equals(RUN)) {
                inputState.isPressedRun = isPressed;
            }
            if (name.equals(FIRE)) {
                inputState.isPressedFire = isPressed;
            }
            if (name.equals(AIM)) {
                inputState.isPressedAim = isPressed;
            }
        }
    };

//    private final AnalogListener analogListener = new AnalogListener() {
//        @Override
//        public void onAnalog(String name, float value, float tpf) {
//            if (name.equals("Rotate_Left")) {
//                System.out.println("left_value" + value);
//                mouseDeltaXY.x = value;
//            } else if (name.equals("Rotate_Right")) {
//                mouseDeltaXY.x = -value;
//            } else {
//                mouseDeltaXY.x = 0;
//            }
//        }
//    };
    private final RawInputListener inputListener = new RawInputListener() {
        @Override
        public void beginInput() {
        }

        @Override
        public void endInput() {
        }

        @Override
        public void onJoyAxisEvent(JoyAxisEvent evt) {
        }

        @Override
        public void onJoyButtonEvent(JoyButtonEvent evt) {
        }

        @Override
        public void onMouseMotionEvent(MouseMotionEvent evt) {
            mouseDeltaXY.set(evt.getDX(), evt.getDY());
            mouseXY.set(evt.getDX(), evt.getDY());
            // inputState.mouseDeltaXY = mouseDeltaXY;
        }

        @Override
        public void onMouseButtonEvent(MouseButtonEvent evt) {
        }

        @Override
        public void onKeyEvent(KeyInputEvent evt) {
        }

        @Override
        public void onTouchEvent(TouchEvent evt) {
        }

    };
}
