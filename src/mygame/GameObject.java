/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.collision.Collidable;
import com.jme3.collision.CollisionResults;
import com.jme3.collision.UnsupportedCollisionException;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import java.util.Queue;

/**
 *
 * @author Finn
 */
public class GameObject {
    protected Spatial spat=null;
    protected Level lev=null;
    public GameObject(Spatial geo,Level lev)
    {
        spat=geo;
        this.lev=lev;
    }
    
    /**
     * Wird aufgerufen, wenn das Level geladen wurde.
     */
    public void Init()
    {
        
    }
    
    public void deInit()
    {
        if(spat!=null)
            lev.rootNode.detachChild(spat);
    }
    
    public void Update(float tpf)
    {
        
    }
    
    public void Render(RenderManager rm) {
        //TODO: add render code
         
    }
    
    public void Reset()
    {
        
    }
    
}
