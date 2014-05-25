/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import java.util.logging.Logger;

/**
 *
 * @author Finn
 */
public class ScoreNumber {
    private Picture numberpic=null;
    
    float solly=0;
    float isty=0;
    private int aktualnumber=0;
    
    public ScoreNumber(String filename,AssetManager assetmanager)
    {
       numberpic=new Picture("number");
       numberpic.setMesh(new QuadEx(1,1,0,0.9f,1,0.1f,false));
       setNumber(0);
       numberpic.setImage(assetmanager, filename, true);
       numberpic.setWidth(32);
       numberpic.setHeight(32);
       numberpic.getMaterial().getTextureParam("Texture").getTextureValue().setWrap(Texture.WrapMode.Repeat);
       
    }
    
    public void update(float tpf)
    {
         QuadEx qe=(QuadEx) numberpic.getMesh();
         if((int)aktualnumber%10!=(int)solly%10)
         {
             aktualnumber+=1;
             
         }
         float d=Math.abs(aktualnumber-isty)+1;
         if(d>0.1f)
         {
         float mul=1f/(d);
         isty=FastMath.interpolateLinear(mul*0.5f,isty , aktualnumber);
             qe.updateGeometry(1,1,0,0.9f-isty*0.1f,1,0.1f,false);
         }
    }
    
    public void setNumber(int number)
    {
       solly=number;
    }
    
    public void setPosition(float x,float y)
    {
        numberpic.setPosition(x, y);
    }
    
    
    public void attach(Node n)
    {
        n.attachChild(numberpic);
    }
    
    public void detach(Node n)
    {
        n.detachChild(numberpic);
    }
}
