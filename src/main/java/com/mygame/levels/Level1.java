/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.levels;

import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.mygame.entity.ai.soldiers.SoldierAiNormal;
import com.mygame.entity.ai.zombie.ZombieNormal;
import com.mygame.entity.interfaces.AIControllable;
import com.mygame.entity.interfaces.Actor;
import com.mygame.settings.Managers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Arash
 */
public class Level1 implements Level {

    //Level
    private static final String PATH_TO_SCENE = "Scenes/Level1/Level1.j3o";
    private final List<Actor> actors = new CopyOnWriteArrayList();
    //player
    private static final Vector3f PLAYER_SPAWN_POINT = new Vector3f(4, 0, 20);

    private FilterPostProcessor filter;

    public Level1() {
        Managers.getInstance().setActors(actors);
    }

    //Shadow
    private void initSunAndShadows() {
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.1f));
        sun.setDirection(new Vector3f(0, -120, -20));
        Managers.getInstance().getRootNode().addLight(sun);

        final int SHADOWMAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(Managers.getInstance().getAsseManager(), SHADOWMAP_SIZE, 3);
        dlsr.setLight(sun);
        Managers.getInstance().getViewPort().addProcessor(dlsr);
        //Managers.getInstance().getViewPort().addProcessor(filter);
    }

    @Override
    public void load() {
        this.init();

        initFog();

        this.initSunAndShadows();

        //
        Managers.getInstance().setCurrentlyLoadedLevel(this);

        this.spawnPlayer();

        //this.spawnZombies(new Vector3f(0, 0, 5));
        // this.spawnZombies(new Vector3f(0, 9, 5));
        //  this.spawnEnemySoldier(new Vector3f(0, 1, -40));
        this.spawnEnemySoldier(new Vector3f(0, 1, -35));
    }

    @Override
    public void update(float tpf) {
        for (Actor actor : actors) {
            actor.update(tpf);

            //Testing
//            if (actors.size() <= 2) {
//                this.spawnZombies();
//            }
        }
    }

    @Override
    public String getPathToScene() {
        return Level1.PATH_TO_SCENE;
    }

    @Override
    public List<Actor> getActors() {
        return this.actors;
    }

    @Override
    public Vector3f getPlayerSpawnPoint() {
        return Level1.PLAYER_SPAWN_POINT;
    }

    public void spawnZombies(Vector3f position) {
        ZombieNormal testZombie = new ZombieNormal();
        testZombie.spawn(position);
        // testZombie.setTarget(Managers.getInstance().getPlayer());
        this.actors.add(testZombie);
    }

    public void spawnEnemySoldier(Vector3f position) {
        SoldierAiNormal testEnemySoldier = new SoldierAiNormal();
        testEnemySoldier.spawn(position);
        // testEnemySoldier.setTarget(Managers.getInstance().getPlayer());
        this.actors.add(testEnemySoldier);
    }

    @Override
    public String getPath() {
        return Level1.PATH_TO_SCENE;
    }

    private void initFog() {
        filter = new FilterPostProcessor(Managers.getInstance().getAsseManager());
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 1.0f));
        fog.setFogDistance(155);
        fog.setFogDensity(1.0f);
        filter.addFilter(fog);
    }

}
