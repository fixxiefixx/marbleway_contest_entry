package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author Finn
 */
public class Go_Trigger extends GameObject{
    
    public enum TriggerType{
        Once,
        Step,
        Multi
    }
    
    private Boolean enabled=true;
    private TriggerType type;
    private BoundingVolume bv=null;
    public Go_Trigger(Spatial spa,Level lev,TriggerType type)
    {
        super(spa,lev);
        this.type=type;
        if(spat instanceof Node)
        {
        Node n=(Node)spat;
        bv=n.getWorldBound();
        }
    }
    
    
    
    protected void OnTrigger()
    {
        
    }
    
    protected void OnUnTrigger()
    {
        
    }
    
    
    
    private Boolean prevCollision=false;
    
    @Override
    public void Update(float tpf)
    {
       Boolean isCollision=false;
        if(enabled)
        {
            if(lev.player!=null)
            {
                
                if(bv!=null )
                {
                    
                    
                    if(bv.intersects(lev.player.marble_phy.getPhysicsLocation()))
                    {
                        isCollision=true;
                    }
                }
            }
            switch(type)
            {
                case Once:
                case Multi:
                {
                    if(!prevCollision && isCollision)
                    {
                        OnTrigger();
                        
                    }
                    if(prevCollision && !isCollision)
                    {
                        OnUnTrigger();
                        if(type==TriggerType.Once)
                        {
                            enabled=false;
                        }
                    }
                }break;
                case Step:
                {
                    if(isCollision)
                    {
                        OnTrigger();
                    }
                }break;
            }
        }
        prevCollision=isCollision;
    }
}
