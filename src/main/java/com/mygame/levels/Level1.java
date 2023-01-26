/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.levels;

import com.jme3.math.Vector3f;
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
    private static final Vector3f PLAYER_SPAWN_POINT = new Vector3f(4, -2, 0);

    public Level1() {
        Managers.getInstance().setActors(actors);
    }

    @Override
    public void load() {
        this.init();
        Managers.getInstance().setCurrentlyLoadedLevel(this);

        this.spawnPlayer();

        this.spawnZombies();
    }

    @Override
    public void update(float tpf) {
        for (Actor actor : actors) {
            actor.update(tpf);

            //Testing
            if (actors.size() <= 1) {
                this.spawnZombies();
            }
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

    private void spawnZombies() {
        ZombieNormal testZombie = new ZombieNormal();
        testZombie.spawn(new Vector3f(0, 0, 5));
        testZombie.setTarget(Managers.getInstance().getPlayer());
        this.actors.add(testZombie);
    }

    @Override
    public String getPath() {
        return Level1.PATH_TO_SCENE;
    }

}
