/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mygame.levels;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.mygame.entity.interfaces.Actor;
import com.mygame.entity.player.Player;
import com.mygame.settings.Managers;
import java.util.List;

/**
 *
 * @author Arash
 */
public interface Level {
    
    String getPath();
    
    void load();
    
    void update(float tpf);
    
    String getPathToScene();
    
    List<Actor> getActors();
    
    Vector3f getPlayerSpawnPoint();
    
    default void init() {
        Spatial level = Managers.getInstance().getAsseManager().loadModel(this.getPathToScene());
        //TODO
        CollisionShape collision = CollisionShapeFactory.createMeshShape(((Node) level).getChild(0));
        RigidBodyControl control = new RigidBodyControl(collision, 0);
        Managers.getInstance().getBulletAppState().getPhysicsSpace().add(control);
        level.addControl(control);
        
        Managers.getInstance().getShooteables().attachChild(level);
        control.setPhysicsLocation(new Vector3f(0, -5, 0));
    }
    
    default void spawnPlayer() {
        Player player = new Player();
        player.spawn(this.getPlayerSpawnPoint());
        this.getActors().add(player);
        
        Managers.getInstance().setPlayer(player);
    }
}
