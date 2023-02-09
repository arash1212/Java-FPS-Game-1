package com.mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.util.SkyFactory;
import com.mygame.levels.Level;
import com.mygame.levels.Level1;
import com.mygame.settings.Managers;
import com.mygame.settings.UIManager;
import com.mygame.settings.input.InputSettings;
import com.mygame.ui.Crosshair;
import com.mygame.ui.TestUi;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;

public class Main extends SimpleApplication {

    private BulletAppState bulletAppState = new BulletAppState();
    private final Node shootables = new Node();
    private InputSettings inputSettings;

    private Level level;

//    private UIManager uiManager = UIManager.getInstance();
    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setEnabled(false);
        this.cam.setFov(50.f);
        cam.setFrustumNear(0.001f);

        this.stateManager.attach(bulletAppState);
       // bulletAppState.setDebugEnabled(true);

        initManagers();

        initNodes();

        initInputSettings();

        loadLevel();

        //test ui
        //inits
        GuiGlobals.initialize(this);

        //TestUI
//        guiNode.attachChild(new TestUi());
        UIManager.getInstance().showInGameUI();

        //getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/skybox/SkyBox3.png", SkyFactory.EnvMapType.CubeMap));

    }

    @Override
    public void simpleUpdate(float tpf) {

        if (level != null) {
            level.update(tpf);
        }

        UIManager.getInstance().update(tpf);
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    //load level
    private void loadLevel() {
        this.level = new Level1();
        level.load();
    }

    //inits
    private void initManagers() {
        Managers.getInstance().setAsseManager(this.assetManager);
        Managers.getInstance().setAudioRenderer(this.audioRenderer);
        Managers.getInstance().setInputManager(this.inputManager);
        Managers.getInstance().setRootNode(this.rootNode);
        Managers.getInstance().setShooteables(this.shootables);
        Managers.getInstance().setCam(this.cam);
        Managers.getInstance().setBulletAppState(this.bulletAppState);
        Managers.getInstance().setStateManager(this.stateManager);
        Managers.getInstance().setAppSettings(this.settings);
        Managers.getInstance().setCameraNode(new CameraNode("cameraNode", this.cam));
        Managers.getInstance().setTimer(this.getTimer());
        Managers.getInstance().setGuiViewPort(this.guiViewPort);
        Managers.getInstance().setViewPort(this.viewPort);
        Managers.getInstance().setGuiNode(guiNode);
//        Managers.getInstance().setUiManager(UIManager.getInstance());
    }

    private void initNodes() {
        rootNode.attachChild(shootables);
    }

    private void initInputSettings() {
        this.inputSettings = new InputSettings();
        inputSettings.initInputs();
    }
}
