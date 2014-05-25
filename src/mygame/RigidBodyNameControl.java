/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;

/**
 *
 * @author Finn
 */
public class RigidBodyNameControl extends RigidBodyControl{
    public String name="";
    
    public RigidBodyNameControl(CollisionShape shape)
    {
        super(shape);
    }
    
    public RigidBodyNameControl(CollisionShape shape,float mass)
    {
        super(shape,mass);
    }
}
