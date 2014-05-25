/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;


import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.PhysicsSweepTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Finn
 */
public class Go_Marble extends GameObject implements ActionListener {

    public enum CameraType {

        follow,
        fixed
    }
    public CameraType camTyp = CameraType.follow;
    public RigidBodyControl marble_phy = null;
    private PointLight pl = null;
    //Keys
    private boolean left = false, right = false, up = false, down = false, cheatkey = false;
    private Vector3f startPos = Vector3f.ZERO;
    private boolean death = false;
    private float deathstarty = 0;
    public AudioNode hahaSound=null;
    public AudioNode marblerollingSound=null;
    public AudioNode bounceSound=null;

    public Go_Marble(Spatial spat, Level lev) {

        super(spat, lev);
        startPos = spat.getWorldTranslation().clone();
        lev.bulletAppState.getPhysicsSpace().addCollisionListener(new RigidBodyMarble());
        hahaSound=new AudioNode(lev.assetManager, "Sounds/laugh.wav");

        hahaSound.setPositional(false);
        hahaSound.setReverbEnabled(false);
        
        marblerollingSound=new AudioNode(lev.assetManager,"Sounds/marblerolling.wav");
        marblerollingSound.setReverbEnabled(false);
        marblerollingSound.setDirectional(false);
        marblerollingSound.setPositional(false);
        marblerollingSound.setVolume(0f);
        marblerollingSound.setLooping(true);
        marblerollingSound.play();
        
        bounceSound=new AudioNode(lev.assetManager,"Sounds/marblebounce.wav");
        bounceSound.setReverbEnabled(false);
        bounceSound.setPositional(false);
        bounceSound.setDirectional(false);
        
        
    }

    @Override
    public void Init() {
        //Make Rigid Body
        SphereCollisionShape sphereshape = new SphereCollisionShape(((BoundingBox) spat.getWorldBound()).getXExtent());
        marble_phy = new RigidBodyControl(sphereshape, 1f);
        spat.addControl(marble_phy);
        lev.bulletAppState.getPhysicsSpace().add(marble_phy);
        //arble_phy.applyCentralForce(new Vector3f(10f,10f,100f));
        setUpKeys();
        lastMarblepos = spat.getWorldTranslation();
        marble_phy.setLinearDamping(0.24f);
        marble_phy.setAngularDamping(0.01f);

        pl = new PointLight();
        pl.setRadius(50f);
        pl.setColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 1f));
        //lev.rootNode.addLight(pl);
        camerapos = spat.getWorldTranslation().add(new Vector3f(0.1f, 1, 0));
    }

    @Override
    public void deInit() {
        lev.bulletAppState.getPhysicsSpace().remove(marble_phy);
        lev.rootNode.removeLight(pl);
        marblerollingSound.stop();
        clearKeys();
        super.deInit();
    }

    private void clearKeys() {
        lev.inputManager.deleteMapping("Lefts");
        lev.inputManager.deleteMapping("Rights");
        lev.inputManager.deleteMapping("Ups");
        lev.inputManager.deleteMapping("Downs");
        lev.inputManager.deleteMapping("Jumps");
        lev.inputManager.deleteMapping("Cheats");
        lev.inputManager.removeListener(this);
    }

    private void setUpKeys() {
        lev.inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_LEFT));
        lev.inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_RIGHT));
        lev.inputManager.addMapping("Ups", new KeyTrigger(KeyInput.KEY_UP));
        lev.inputManager.addMapping("Downs", new KeyTrigger(KeyInput.KEY_DOWN));
        lev.inputManager.addMapping("Jumps", new KeyTrigger(KeyInput.KEY_SPACE));
        lev.inputManager.addMapping("Cheats", new KeyTrigger(KeyInput.KEY_E));

        lev.inputManager.addListener(this, "Lefts");
        lev.inputManager.addListener(this, "Rights");
        lev.inputManager.addListener(this, "Ups");
        lev.inputManager.addListener(this, "Downs");
        lev.inputManager.addListener(this, "Jumps");
        lev.inputManager.addListener(this, "Cheats");
    }

    private PhysicsRayTestResult getClosestRayCollision(List<PhysicsRayTestResult> collisions) {
        PhysicsRayTestResult mincoll = null;
        for (int i = 0; i < collisions.size(); i++) {
            if (mincoll == null) {
                mincoll = collisions.get(i);
            } else {
                if (mincoll.getHitFraction() > collisions.get(i).getHitFraction()) {
                    mincoll = collisions.get(i);
                }
            }
        }
        return mincoll;
    }
    Vector3f camerapos = Vector3f.ZERO;
    float camdist = 20f;
    Vector3f lastcampos = Vector3f.ZERO;
    Vector3f smoothpos = Vector3f.ZERO;
    public float fixedCamHight=3f;
    
    private void cameramove(float tpf) {
        RigidBodyControl character = marble_phy;

        final float smoothfactor = 5f;

        smoothpos = character.getPhysicsLocation().add(smoothpos.mult(smoothfactor)).divide(smoothfactor + 1);

        switch (camTyp) {
            case follow: {
                float camnormaldist = 3;
                Vector3f pp, zwischen;
                Vector3f normal=marble_phy.getGravity().normalize().multLocal(-1);
                
                pp = smoothpos.add(normal);
                zwischen = camerapos.subtract(pp);
                zwischen.normalizeLocal().multLocal(camnormaldist);
                camerapos = lev.cam.getLocation();

                camerapos = pp.add(zwischen);
                
                // cam.setLocation(camerapos.clone());

                /*float cammaxup=8;
                 float camminup=-8f;
                 if(camerapos.y < pp.y+camminup)
                 camerapos=(new Vector3f( camerapos.x,pp.y+camminup,camerapos.z));
        
                 if(camerapos.y > pp.y+cammaxup)
                 camerapos=(new Vector3f( camerapos.x,pp.y+cammaxup,camerapos.z));*/


                Vector3f cameraposNew=camerapos;
                Vector3f pos = new Vector3f(character.getPhysicsLocation());
                // Ray ray = new Ray(pp,camerapos.subtract(pp) );
                //CollisionResults results = new CollisionResults();
                List<PhysicsSweepTestResult> presults = new ArrayList<PhysicsSweepTestResult>();

                CollisionShape shape=new SphereCollisionShape(0.1f);
                 lev.bulletAppState.getPhysicsSpace().sweepTest(shape,new Transform(smoothpos),new Transform(camerapos), presults);
                //PhysicsSpace physicstate = lev.bulletAppState.getPhysicsSpace();
                //lev.bulletAppState.getPhysicsSpace().rayTest(pp, camerapos, presults);
                //PhysicsRayTestResult res = getClosestRayCollision(presults);
                PhysicsSweepTestResult res= Level.getClosestSweepCollision(presults,marble_phy);
                 if(res!=null)
                 {
                    
                  cameraposNew=(smoothpos.clone().interpolate(camerapos, res.getHitFraction()));
                  if(cameraposNew.distance(smoothpos)<1.5f)
                    camerapos.addLocal(0, tpf*4f, 0);
                 }

                boolean collision = false;
                Vector3f tempcampos = camerapos.clone();
                if (!presults.isEmpty()) {
                    //camdist--;
           /* camdist=pp.distance(camerapos)/(1/res.getHitFraction())-1f;
                     zwischen = camerapos.subtract(pp);
                     zwischen.normalizeLocal().multLocal(camdist);
                     camerapos=pp.add(zwischen);*/
                }
                /* if(camdist>1){
                 boolean leftfree= physicstate.rayTest(camerapos.add(new Vector3f(-1,0f,0f)), camerapos).isEmpty();
                 boolean rightfree= physicstate.rayTest(camerapos.add(new Vector3f(1,0f,0f)), camerapos).isEmpty();
                 boolean upfree= physicstate.rayTest(camerapos.add(new Vector3f(0f,0f,-1)), camerapos).isEmpty();
                 boolean downfree= physicstate.rayTest(camerapos.add(new Vector3f(0f,0f,1)), camerapos).isEmpty();
        
        
        
                 if(!leftfree && rightfree) camerapos.x+=1;
                 if(!rightfree && leftfree) camerapos.x-=1;
                 if(!upfree && downfree) camerapos.z+=1;
                 if(!downfree&&upfree)camerapos.z-=1;
                 }*/
                lev.cam.setLocation(cameraposNew);
                lev.cam.lookAt(smoothpos, normal);
                camerapos = tempcampos;
            }
            break;
            case fixed: {
                lev.cam.setLocation(smoothpos.add(+3f,fixedCamHight , 0f));
                lev.cam.lookAt(smoothpos, Vector3f.UNIT_Y);
            }
        }





    }

    private Vector3f rotateVectorY(Vector3f vec, float angle) {

        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(angle, Vector3f.UNIT_Y);
        mat.mult(vec, vec);
        return vec;
    }
    private Vector3f rotateVectorNormal(Vector3f vec, float angle,Vector3f normal) {

        Matrix3f mat = new Matrix3f();
        mat.fromAngleNormalAxis(angle, normal);
        mat.mult(vec, vec);
        return vec;
    }
    
    private Vector3f lastMarblepos = Vector3f.ZERO;
    private Boolean nextStepBallretten = false;

    private void ballRetten() {
        marble_phy.setPhysicsLocation(startPos);
        marble_phy.setAngularVelocity(Vector3f.ZERO);
        marble_phy.setLinearVelocity(Vector3f.ZERO);
        camerapos = startPos.add(new Vector3f(10f, 10f, 0));
        smoothpos = camerapos;
        lastcampos = camerapos;
        lev.cam.setLocation(camerapos);
        lev.Reset();
        hahaSound.play();
    }

    private void die() {
        deathstarty = spat.getWorldTranslation().y;
        death = true;
        marble_phy.setAngularVelocity(Vector3f.ZERO);
        marble_phy.setLinearVelocity(Vector3f.ZERO);
        marble_phy.setKinematic(true);

    }

    private void endDie() {

        marble_phy.setKinematic(false);
        nextStepBallretten = true;
        ballRetten();
        death = false;
        
    }

    public void setPos(Vector3f pos)
    {
        lastMarblepos=pos;
        marble_phy.setPhysicsLocation(pos);
    }
    
    boolean nextstepdie=false;
    
    @Override
    public void Update(float tpf) {
        if (death) {
            float zheight = 1f;
            if (spat.getWorldTranslation().y < (deathstarty - zheight)) {

                endDie();
            } else {
                spat.setLocalTranslation(spat.getLocalTranslation().add(0, tpf * -0.3f, 0));
                //cameramove(tpf);
            }
        } else {
            //Prüfen ob durch Wand gebuggt
            List<PhysicsRayTestResult> presults = new ArrayList<PhysicsRayTestResult>();

            //CapsuleCollisionShape capsule = new CapsuleCollisionShape(0.25f, 0.5f);
            // physicstate.sweepTest(capsule,new Transform(pp),new Transform(camerapos), presults);

            lev.bulletAppState.getPhysicsSpace().rayTest(lastMarblepos, marble_phy.getPhysicsLocation(), presults);
            for (PhysicsRayTestResult res : presults) {
                if (res.getCollisionObject() != marble_phy) {
                    marble_phy.setPhysicsLocation(lastMarblepos);
                    marble_phy.setLinearVelocity(marble_phy.getLinearVelocity().mult(0.5f));
                }
            }
            float playerspeed = 8f;
            Vector3f walkdirection = new Vector3f();
            if (left) {
                walkdirection.addLocal(new Vector3f(-1f, 0, 0));
            }
            if (right) {
                walkdirection.addLocal(new Vector3f(1f, 0f, 0f));
            }
            if (up) {
                walkdirection.addLocal(new Vector3f(0f, 0f, -1f));
            }
            if (down) {
                walkdirection.addLocal(new Vector3f(0f, 0f, 1f));
            }
            walkdirection.normalizeLocal().multLocal(playerspeed);


            float camdir = FastMath.atan2(lev.cam.getDirection().x, lev.cam.getDirection().z);

            //  updateHudText(camdir);
            //walkdirection = rotateVectorY(walkdirection.negate(), camdir);
            Vector3f normal=marble_phy.getGravity().normalize().multLocal(-1f);
            walkdirection = rotateVectorNormal(walkdirection.negate(), camdir,normal);

            if (up || down || left | right) {
                marble_phy.applyCentralForce(walkdirection);

            }

            //Ball retten
            if (marble_phy.getPhysicsLocation().y < -100f || nextStepBallretten) {
                nextStepBallretten = false;
                ballRetten();
            }

            //Die
            if(nextstepdie)
            {
                nextstepdie=false;
                die();
            }
            
             cameramove(tpf);
            pl.setPosition(marble_phy.getPhysicsLocation().add(0f, 15f, 0f));

            lastMarblepos = marble_phy.getPhysicsLocation().clone();
        }
        float speed=marble_phy.getLinearVelocity().length();
        float effekt=speed-15f;
        if(effekt<0f)
            effekt=0f;
        lev.blurFilter.setSampleDistance(0.3f);
        
        lev.blurFilter.setSampleStrength(effekt*0.6f);
        
        float vol=marblerollingSound.getVolume();
        vol-=tpf*4f;
        if(vol<0)
            vol=0f;
        marblerollingSound.setVolume(vol);
    }

    public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Lefts")) {
            left = value;
        } else if (binding.equals("Rights")) {
            right = value;
        } else if (binding.equals("Ups")) {
            up = value;
        } else if (binding.equals("Downs")) {
            down = value;
        }
        if (binding.equals("Jumps")) {
            //character.jump();
        }
        if (binding.equals("Cheats")) {

            cheatkey = value;
            if(value)
            {
               // marble_phy.setLinearVelocity(new Vector3f(0,5,0));
            }
        }
    }

    private class RigidBodyMarble extends RigidBodyControl implements PhysicsCollisionListener {

        public RigidBodyMarble() {
            lev.bulletAppState.getPhysicsSpace().addCollisionListener(this);
        }

        public void collision(PhysicsCollisionEvent event) {
            
            PhysicsCollisionObject[] objs = {event.getObjectA(), event.getObjectB()};
            if(objs[0]==marble_phy || objs[1]==marble_phy)
            {
            for (PhysicsCollisionObject obj : objs) {
                if (obj != null) {
                    if (obj instanceof RigidBodyNameControl) {
                        RigidBodyNameControl rbnc = (RigidBodyNameControl) obj;
                        if (rbnc.name.equals("death") && !death) {
                            nextstepdie=true;
                            
                            //marble_phy.applyCentralForce(new Vector3f(0,10f,0));
                            //nextStepBallretten=true;
                            break;
                        }
                    }
                }
            }
            /*PhysicsCollisionObject otherobj=objs[0];
            if(otherobj==marble_phy)
                otherobj=objs[1];
            Vector3f normal=event.getNormalWorldOnB().normalize();
            marble_phy.setGravity( normal.multLocal(-10f).interpolate(marble_phy.getGravity(), 0.99f));*/
            
            //Rolling sound
            
            float speed=marble_phy.getLinearVelocity().length()*0.2f;
        float effekt=speed-2f;
        if(effekt<0f)
            effekt=0f;
        if(effekt>0.8f)
            effekt=0.8f;
            marblerollingSound.setVolume(effekt);
            
        float bounce=Math.abs( event.getAppliedImpulse());
        effekt=bounce*0.03f;
            if(effekt<0f)
            effekt=0f;
        if(effekt>1f)
            effekt=1f;
                   if(effekt>0.1f)
                   {
                       bounceSound.setVolume(effekt);
                   bounceSound.play();
                   }
            }
        }
    }
}
