/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.FastMath;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AreaUtils;
import com.jme3.scene.control.LodControl;

/**
 *
 * @author Finn
 */
public class MyLodControl extends LodControl{
    
    private float lastDistance=0f;
    int lastLevel=0;
    float distTolerance=0.1f;
    int numLevels=0;
    int numTris[]=null;
    
    
    public void setSpatial(Spatial spatial){
        if (!(spatial instanceof Geometry))
            throw new IllegalArgumentException("LodControl can only be attached to Geometry!");

        super.setSpatial(spatial);
        Geometry geom = (Geometry) spatial;
        Mesh mesh = geom.getMesh();
        numLevels = mesh.getNumLodLevels();
        numTris = new int[numLevels];
        for (int i = numLevels - 1; i >= 0; i--)
            numTris[i] = mesh.getTriangleCount(i);
    }
    
    protected void controlRender(RenderManager rm, ViewPort vp){
        BoundingVolume bv = spatial.getWorldBound();
        
        Camera cam = vp.getCamera();
        float atanNH = FastMath.atan(cam.getFrustumNear() * cam.getFrustumTop());
        float ratio = (FastMath.PI / (8f * atanNH));
        float newDistance = bv.distanceTo(vp.getCamera().getLocation()) / ratio;
        int level=0;

        distTolerance=super.getDistTolerance();
        
        level=(int)(newDistance/distTolerance);
        if(level>=numLevels)
            level=numLevels-1;

        spatial.setLodLevel(level);
    }
}
