/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;

/**
 *
 * @author Finn
 */
public class Go_SoccerPlayer extends GameObject{
    
    private AnimControl control;
    private AnimChannel channel;
    private RigidBodyControl capsule_phy = null;
    private Vector3f startpos=Vector3f.ZERO;
    private AudioNode kicksound=null;
    
    private enum AnimState
            {
        kick,
        run,
        idle
    }
    
    private AnimState state=AnimState.idle; 
    
    public Go_SoccerPlayer(Spatial spat,Level lev)
    {
        super(spat,lev);
        Node node = (Node) spat;
        //targetPoint = spat.getWorldTranslation().clone();
        //Level.removeLights(spat);
        Spatial zs = ((Node) ((Node) node.getChild(0)).getChild(0)).getChild(0);
        control = zs.getControl(AnimControl.class);
        channel = control.createChannel();
        control.addListener(new AnimListener());
        channel.setAnim("idle");
        channel.setSpeed(1f);
        
        //Make Rigid Body

        CapsuleCollisionShape capsuleshape = new CapsuleCollisionShape(0.6f, 1.2f);

        capsule_phy = new RigidBodyControl(capsuleshape, 1f);
        capsule_phy.setAngularFactor(0f);
        capsule_phy.setFriction(0f);
        capsule_phy.setLinearDamping(0.99f);
        spat.addControl(capsule_phy);
        ((Node) spat).getChild(0).getLocalTranslation().addLocal(0, -12, 0);
        lev.bulletAppState.getPhysicsSpace().add(capsule_phy);
        startpos=capsule_phy.getPhysicsLocation();
        
        //Sound
        kicksound= new AudioNode(lev.assetManager, "Sounds/kick.wav");
        kicksound.setReverbEnabled(false);
        kicksound.setPositional(false);
    }
    
    @Override
    public void Reset()
    {
        capsule_phy.setPhysicsLocation(startpos);
        capsule_phy.applyCentralForce(new Vector3f(0,-0.1f,0));
    }
    
    @Override
    public void deInit()
    {
        lev.bulletAppState.getPhysicsSpace().remove(capsule_phy);
        super.deInit();
    }
    
    private Boolean isPlayerVisible(Go_Marble player) {


        Vector3f srcvec = capsule_phy.getPhysicsLocation();
        Vector3f targetvec = player.spat.getWorldTranslation();
        if (srcvec.distance(targetvec) <= 30) {
            PhysicsSpace ps = lev.bulletAppState.getPhysicsSpace();
            List<PhysicsRayTestResult> results = ps.rayTest(srcvec, targetvec);
            for (PhysicsRayTestResult res : results) {
                if (res.getCollisionObject() != this.capsule_phy && res.getCollisionObject() != player.marble_phy) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private class AnimListener implements AnimEventListener
    {

        public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
            if(state==AnimState.kick)
            {
                state=AnimState.idle;
                channel.setAnim("idle");
            }
        }

        public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
            
        }
        
    }
    
    @Override
    public void Update(float tpf)
    {
        boolean playervisible=isPlayerVisible(lev.player);
        if(playervisible)
        {
            if(state==AnimState.idle)
            {
                state=AnimState.run;
                channel.setAnim("run");
                channel.setSpeed(3f);
            }
            
            if(state==AnimState.run 
               && lev.player.marble_phy.getPhysicsLocation().distance(capsule_phy.getPhysicsLocation().add(new Vector3f(0,-1,0)))<0.8f)
            {
                state=AnimState.kick;
                channel.setAnim("kick");
                channel.setLoopMode(LoopMode.DontLoop);
                
                kicksound.play();
                
                
                Vector3f direction = lev.player.marble_phy.getPhysicsLocation().subtract(this.capsule_phy.getPhysicsLocation());
                lev.player.marble_phy.applyCentralForce(direction.normalizeLocal().multLocal(1000f));
            }
                
            if(state==AnimState.run)
            {
                Vector3f direction = lev.player.marble_phy.getPhysicsLocation().subtract(this.capsule_phy.getPhysicsLocation());
                
                Quaternion newRot = new Quaternion().fromAngleAxis(FastMath.atan2(direction.x, direction.z) , Vector3f.UNIT_Y);
                
                capsule_phy.setPhysicsRotation(new Quaternion(capsule_phy.getPhysicsRotation(), newRot, 0.2f));
            direction.y=0f;
            capsule_phy.applyCentralForce(direction.normalize().mult(20));
                
            }
        }else
        {
            if(state!=AnimState.idle)
            {
                state=AnimState.idle;
                channel.setAnim("idle");
            }
        }
    }
}
