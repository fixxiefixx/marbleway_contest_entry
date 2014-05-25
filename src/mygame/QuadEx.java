/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Quad;

/**
 *
 * @author Finn
 */
public class QuadEx extends Mesh{
    private float width=1f;
    private float height=1f;
    
    public QuadEx(float width, float height,float texoffsetx,float texoffsety,float texscalex,float texscaley, boolean flipCoords){
        updateGeometry(width, height,texoffsetx,texoffsety,texscalex,texscaley,flipCoords);
    }
  
    public float getHeight() {
        return height;
    }

    
    public float getWidth() {
        return width;
    }
    
    
    public void updateGeometry(float width, float height,float texoffsetx,float texoffsety,float texscalex,float texscaley, boolean flipCoords) {
        this.width = width;
        this.height = height;
        setBuffer(Type.Position, 3, new float[]{0,      0,      0,
                                                width,  0,      0,
                                                width,  height, 0,
                                                0,      height, 0
                                                });
        

        if (flipCoords){
            setBuffer(Type.TexCoord, 2, new float[]{0+texoffsetx, texscaley+texoffsety,
                                                    texscalex+texoffsetx, texscaley+texoffsety,
                                                    texscalex+texoffsetx, 0+texoffsety,
                                                    0+texoffsetx, 0+texoffsety});
        }else{
            setBuffer(Type.TexCoord, 2, new float[]{0+texoffsetx, 0+texoffsety,
                                                    texscalex+texoffsetx, 0+texoffsety,
                                                    texscalex+texoffsetx, texscaley+texoffsety,
                                                    0+texoffsetx, texscaley+texoffsety});
        }
        setBuffer(Type.Normal, 3, new float[]{0, 0, 1,
                                              0, 0, 1,
                                              0, 0, 1,
                                              0, 0, 1});
        if (height < 0){
            setBuffer(Type.Index, 3, new short[]{0, 2, 1,
                                                 0, 3, 2});
        }else{
            setBuffer(Type.Index, 3, new short[]{0, 1, 2,
                                                 0, 2, 3});
        }
        
        updateBound();
    }
}
