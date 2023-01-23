/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.settings;

import java.util.Random;

/**
 *
 * @author Arash
 */
public class GeneralUtils {

    private static Random random = new Random();

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
