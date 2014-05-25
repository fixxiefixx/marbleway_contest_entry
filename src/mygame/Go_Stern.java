/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

/**
 *
 * @author Finn
 */
public class Go_Stern extends GameObject {
    
    private GameObject player=null;
    private float radius=1f; 
    
    public Go_Stern(Geometry geo,Level lev)
    {
        super(geo,lev);
        
        radius=((BoundingBox) Level.getFirstGeomFromNode(spat).getModelBound()).getXExtent();
        float radius2=((BoundingBox)Level.getFirstGeomFromNode(spat).getModelBound()).getZExtent();
        if(radius2>radius)
            radius=radius2;
        
    }
    
    public void setPlayer(GameObject player)
    {
        this.player=player;
    }
    
    float rotation=0f;
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(1,0,0));
    public static final Quaternion ROLL090  = new Quaternion().fromAngleAxis(FastMath.PI/2,   new Vector3f(0,0,1));
    public void Update(float tpf)
    {
        if(this.spat!=null)
        {
            rotation+=tpf*2f;
            if(rotation>FastMath.TWO_PI)
                rotation-=FastMath.TWO_PI;
            spat.setLocalRotation(new Quaternion().fromAngles(FastMath.HALF_PI, rotation, 0));
            
            if(player!=null)
            {
                if(player.spat.getWorldTranslation().distance(spat.getWorldTranslation())<=radius)
                {
                    lev.levelFinish();
                }
            }
        }
    }
}
