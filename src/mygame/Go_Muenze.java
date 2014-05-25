/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
 *
 * @author Finn
 */
public class Go_Muenze extends GameObject {
    private boolean eingesammelt =false;
    private float gravity=-40f;
    private float yspeed=0;
    private float ystart=0;
    float rotation=0f;
    float rotatespeed=4f;
    public Go_Muenze(Geometry geo,Level lev)
    {
        super(geo,lev);
        ystart=geo.getWorldTranslation().y;
        rotation=(float)Math.random()%2*(float)Math.PI;
    }
    
    @Override
    public void Update(float tpf)
    {
        if(eingesammelt)
        {
            yspeed+=gravity*tpf;
            spat.setLocalTranslation( spat.getLocalTranslation().add(0, yspeed*tpf,0));
            if(spat.getWorldTranslation().y<ystart-0.4f)
            {
                spat.setCullHint(Spatial.CullHint.Always);
                //lev.RemoveGameObject(this);
                gravity=0;
            }
        }else
        {
            if(lev.player!=null)
            {
                if(lev.player.spat.getWorldTranslation().distance(spat.getWorldTranslation())<0.8f)
                {
                    eingesammelt=true;
                    yspeed=13f;
                    rotatespeed=14f;
                    lev.getCoin();
                }
            }
        }
        rotation+=tpf*rotatespeed;
            if(rotation>FastMath.TWO_PI)
                rotation-=FastMath.TWO_PI;
            spat.setLocalRotation(new Quaternion().fromAngles(0, rotation, 0));
    }
    
    @Override
    public void Reset()
    {
        rotatespeed=4f;
        eingesammelt=false;
        spat.setCullHint(Spatial.CullHint.Inherit);
        spat.setLocalTranslation(0, 0, 0);
        gravity=-40f;
    }
}
