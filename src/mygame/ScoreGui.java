/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.FastMath;
import com.jme3.ui.Picture;

/**
 *
 * @author Finn
 */
public class ScoreGui {
    private Picture score_hg=null;
    private Picture score_fg=null;
    private ScoreNumber[] scorenumbers=new ScoreNumber[10];
    private Level lev=null;
    
    public ScoreGui(Level lev)
    {
        score_hg=new Picture("score_hg");
        score_hg.setImage(lev.assetManager, "Textures/score_hg.png", false);
        score_hg.setPosition(16, 10);
        score_hg.setWidth(200);
        score_hg.setHeight(32);
        lev.app.getGuiNode().attachChild(score_hg);
        this.lev=lev;
        for(int i=0;i<scorenumbers.length;i++)
        {
            ScoreNumber sn=new ScoreNumber("Textures/scorenumbers.png",lev.assetManager);
            sn.attach(lev.app.getGuiNode());
            scorenumbers[i]=sn;
            sn.setPosition(i*18+20, 10);
        }
        
        score_fg=new Picture("score_fg");
        score_fg.setImage(lev.assetManager, "Textures/score_fg.png", true);
        score_fg.setPosition(16, 10);
        score_fg.setWidth(200);
        score_fg.setHeight(32);
        lev.app.getGuiNode().attachChild(score_fg);
    }
    
    public void setNumber(int number)
    {
        for(int i=0;i<scorenumbers.length;i++)
        {
            
            
            scorenumbers[scorenumbers.length-1-i].setNumber(number%10);
            number/=10;
        }
    }
    
    public void Dispose()
    {
        for(ScoreNumber sn:scorenumbers)
        {
            sn.detach(lev.app.getGuiNode());
        }
        lev.app.getGuiNode().detachChild(score_hg);
        lev.app.getGuiNode().detachChild(score_fg);
    }
    
    public void update(float tpf)
    {
        for(int i=0;i<scorenumbers.length;i++)
        {
            
            
            scorenumbers[i].update(tpf);
            
        }
    }
}
