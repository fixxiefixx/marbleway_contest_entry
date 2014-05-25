/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package mygame;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.*;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
/**
 *
 * @author Finn
 */
public class GameState implements ScreenController{
    
    protected Node rootNode;
    protected AssetManager assetManager;
    protected InputManager inputManager;
    protected Camera cam;
    protected ViewPort viewPort;
    protected AudioRenderer audioRenderer;
    protected ViewPort guiViewPort;
    protected FlyByCamera flyCam;
    protected AppStateManager stateManager;
    
    private Node superrootNode;
    
    /**
     * 0: Spiel beenden.
     * 1: zum Editor.
     * 2: Host Game.
     * 3: Join Game.
     */
    public int endaktion=0;
    
    public GameState(Node _rootNode,AssetManager _assetManager,InputManager _inputManager,Camera _cam,ViewPort _viewPort,AudioRenderer _audioRenderer,ViewPort _guiViewPort,FlyByCamera _flyCam,AppStateManager _stateManager)
    {
        superrootNode=_rootNode;
        rootNode=new Node("State Node");
        _rootNode.attachChild(rootNode);
        assetManager=_assetManager;
        inputManager=_inputManager;
        cam=_cam;
        viewPort=_viewPort;
        audioRenderer=_audioRenderer;
        guiViewPort=_guiViewPort;
        flyCam=_flyCam;
        stateManager=_stateManager;
        
    }
    
    public void Init()
    {
        
    }
    
    public void deInit()
    {
        superrootNode.detachChild(rootNode);
    }
    public boolean Update(float tpf)
    {
        return true;
    }
    
    
    
     public void Render(RenderManager rm) {
        //TODO: add render code
         
    }

    public void bind(Nifty nifty, Screen screen) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onStartScreen() {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void onEndScreen() {
     //   throw new UnsupportedOperationException("Not supported yet.");
    }
}
