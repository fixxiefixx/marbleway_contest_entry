/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimControl;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.Control;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * @author Finn
 */
public class Util {

    public static PhysicsRayTestResult getClosestRayCollision(List<PhysicsRayTestResult> collisions) {
        return getClosestRayCollision(collisions, null);
    }

    public static PhysicsRayTestResult getClosestRayCollision(List<PhysicsRayTestResult> collisions, PhysicsCollisionObject ignorethis) {
        PhysicsRayTestResult mincoll = null;
        for (int i = 0; i < collisions.size(); i++) {
            if (collisions.get(i).getCollisionObject() != ignorethis) {
                if (mincoll == null) {
                    mincoll = collisions.get(i);
                } else {
                    if (mincoll.getHitFraction() > collisions.get(i).getHitFraction()) {
                        mincoll = collisions.get(i);
                    }
                }
            }
        }
        return mincoll;
    }

    public static Object getInstanceField(Object instance, String fieldName) throws Throwable {
        Field field = instance.getClass().getDeclaredField(fieldName);
        return field.get(instance);
    }

    public static void removeLightsFromSpatial(Spatial spat) {
        if (spat instanceof Node) {
            Node node = (Node) spat;
            for (Spatial subspat : node.getChildren()) {
                removeLightsFromSpatial(subspat);
            }
        }

        spat.getLocalLightList().clear();


    }

    public static boolean loadAdditionalTextures(Material mat, AssetManager assetmngr) {
        boolean hasnormal = false;
        //Get Diffuse Texture name
        MatParamTexture diffuseparam = mat.getTextureParam("DiffuseMap");
        if (diffuseparam != null) {
            String diffusename = diffuseparam.getTextureValue().getName();
            if (diffusename.length() > 4) {
                String ext = diffusename.substring(diffusename.length() - 4);
                String prefix = diffusename.substring(0, diffusename.length() - 4);
                String normaltexname = prefix + "_NRM" + ext;
                String spectexname = prefix + "_SPEC" + ext;
                String disptexname = prefix + "_DISP" + ext;
                //Normal map
                MatParamTexture texparam = mat.getTextureParam("NormalMap");
                if (texparam == null) {

                    try {

                        Texture tex = assetmngr.loadTexture(normaltexname);
                        tex.setWrap(Texture.WrapMode.Repeat);
                        mat.setTexture("NormalMap", tex);
                        hasnormal = true;
                    } catch (AssetNotFoundException ae) {
                    }
                }
                MatParamTexture spectexparam = mat.getTextureParam("SpecularMap");
                if (spectexparam == null) {
                    try {
                        Texture tex = assetmngr.loadTexture(spectexname);
                        tex.setWrap(Texture.WrapMode.Repeat);
                        mat.setTexture("SpecularMap", tex);
                    } catch (AssetNotFoundException ae) {
                    }
                }
                 MatParamTexture disptexparam = mat.getTextureParam("ParallaxMap");
                if (disptexparam == null) {
                    try {
                        Texture tex = assetmngr.loadTexture(disptexname);
                        tex.setWrap(Texture.WrapMode.Repeat);
                        mat.setTexture("ParallaxMap", tex);
                    } catch (AssetNotFoundException ae) {
                    }
                }
            }
        }
        return hasnormal;
    }

    public static void EnhanceSpatMat(Spatial spat, AssetManager mgr) {
        if (spat instanceof Geometry) {
            Geometry geom = (Geometry) spat;
            if(geom.getMaterial()!=null)
            {
            boolean hasnormal = loadAdditionalTextures(geom.getMaterial(), mgr);
            Material mat=geom.getMaterial();
            
 
           // mat.setParam("SteepParallax", VarType.Boolean, true);
            MatParamTexture diffuseparam = mat.getTextureParam("DiffuseMap");
            if (diffuseparam!=null) {
                mat.setBoolean("HighQuality", true);
                mat.setBoolean("LowQuality", false);
                mat.setBoolean("SteepParallax", true);
                mat.setFloat("ParallaxHeight", 0.05f);
                mat.setColor("Ambient", ColorRGBA.White);
                
                //Generate tangents
                if (geom.getMesh().getBuffer(com.jme3.scene.VertexBuffer.Type.Tangent) == null &&hasnormal) {
                    TangentBinormalGenerator.generate(geom.getMesh());
                }
                /*if(mat.getAdditionalRenderState().getBlendMode()!= RenderState.BlendMode.Alpha)
                {
                mat.getAdditionalRenderState().setAlphaTest(false);
                mat.getAdditionalRenderState().setAlphaFallOff(0.5f);
                }*/
            }
            }
        } else {
            Node node = (Node) spat;
            for (Spatial child : node.getChildren()) {
                EnhanceSpatMat(child, mgr);
            }
        }

    }

    public static Spatial getFirstSpatOfType(Spatial spat, Class type) {
        if (spat.getClass() == type) {
            return spat;
        }
        if (spat instanceof Node) {
            Node node = (Node) spat;
            for (Spatial spa : node.getChildren()) {
                Spatial retspat = getFirstSpatOfType(spa, type);
                if (retspat != null) {
                    return retspat;
                }
            }
        }
        return null;
    }

    public static CameraControl getFirstCameraControl(Spatial spat) {
        for (int i = 0; i < spat.getNumControls(); i++) {
            Control con = spat.getControl(i);
            if (con instanceof CameraControl) {
                return (CameraControl) con;
            }
        }
        if (spat instanceof Node) {
            Node node = (Node) spat;
            for (Spatial subspat : node.getChildren()) {
                CameraControl control = getFirstCameraControl(subspat);
                if (control != null) {
                    return control;
                }
            }
        }
        return null;
    }

    public static AnimControl getFirstAnimControl(Spatial spat) {
        for (int i = 0; i < spat.getNumControls(); i++) {
            Control con = spat.getControl(i);
            if (con instanceof AnimControl) {
                return (AnimControl) con;
            }
        }
        if (spat instanceof Node) {
            Node node = (Node) spat;
            for (Spatial subspat : node.getChildren()) {
                AnimControl control = getFirstAnimControl(subspat);
                if (control != null) {
                    return control;
                }
            }
        }
        return null;
    }

    public static float quaternuinDifference(Quaternion q1, Quaternion q2) {
        Vector3f v1 = q1.mult(Vector3f.UNIT_X);
        Vector3f v2 = q2.mult(Vector3f.UNIT_X);
        return v1.distance(v2);
    }

    public static void setAudioNodeGlobal(AudioNode an) {
        an.setReverbEnabled(false);
        an.setPositional(false);
        an.setDirectional(false);
    }

    public static Quaternion rotateSlowTowards(Quaternion current, Quaternion towards, float speed) {
        float angle = quaternuinDifference(current, towards);
        if (angle < speed) {
            return towards;
        } else {
            return current.slerp(current, towards, speed / angle);
        }

    }
    
    
    
    public static final Quaternion PITCH045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(1, 0, 0));
    public static final Quaternion PITCH270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(1, 0, 0));
    public static final Quaternion ROLL045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 0, 1));
    public static final Quaternion ROLL270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 0, 1));
    public static final Quaternion YAW045 = new Quaternion().fromAngleAxis(FastMath.PI / 4, new Vector3f(0, 1, 0));
    public static final Quaternion YAW090 = new Quaternion().fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 1, 0));
    public static final Quaternion YAW180 = new Quaternion().fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0));
    public static final Quaternion YAW270 = new Quaternion().fromAngleAxis(FastMath.PI * 3 / 2, new Vector3f(0, 1, 0));
}
