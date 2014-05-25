/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

/**
 *
 * @author Finn
 */
public class Go_Trigger_Levelwarp extends Go_Trigger implements ActionListener {
    private Picture rechteckpic=null;
    private boolean aktiv=false;
    private int levelid=0;
    private BitmapText highscoreText=null;
    public Go_Trigger_Levelwarp(Spatial spat,Level lev,int levelid)
    {
        super(spat,lev,TriggerType.Multi);
        rechteckpic=new Picture("Rechteck");
        rechteckpic.setImage(lev.assetManager, "Textures/rechteck.png", true);
        rechteckpic.setWidth(320);
        rechteckpic.setHeight(395);
        rechteckpic.setLocalTranslation(lev.app.getContext().getSettings().getWidth()/2-160,
                lev.app.getContext().getSettings().getHeight()-395,-1);
        setUpKeys();
        this.levelid=levelid;
        BitmapFont bf= lev.assetManager.loadFont("Interface/Fonts/Console.fnt");
        highscoreText=new BitmapText(bf,false);
        highscoreText.setSize((int)(bf.getCharSet().getRenderedSize()));      // font size
        highscoreText.setColor(ColorRGBA.White);                             // font color
        highscoreText.setText(lev.app.getHighScore(levelid).toString());  // the text
        
        highscoreText.setLocalTranslation(lev.app.getContext().getSettings().getWidth()/2-140, 
                lev.app.getContext().getSettings().getHeight()-2, 0);
    }
    
    public void updateHighScore()
    {
         highscoreText.setText(lev.app.getHighScore(levelid).toString());  // the text
    }
    
    private void clearKeys() {
       try
       {
        lev.inputManager.deleteMapping("Enter");
        lev.inputManager.deleteMapping("Back");
       }catch(Exception ex)
       {
           
       }
        
        lev.inputManager.removeListener(this);
    }
    
    private void setUpKeys() {
        lev.inputManager.addMapping("Enter", new KeyTrigger(KeyInput.KEY_RETURN));
        lev.inputManager.addMapping("Back", new KeyTrigger(KeyInput.KEY_BACK));
       

        lev.inputManager.addListener(this, "Enter");
        lev.inputManager.addListener(this, "Back");
        
    }
    
    @Override
    protected void OnTrigger()
    {
        lev.app.getGuiNode().attachChild(rechteckpic);
        lev.app.getGuiNode().attachChild(highscoreText);
        aktiv=true;
    }
    
    @Override
    protected void OnUnTrigger()
    {
        lev.app.getGuiNode().detachChild(rechteckpic);
        lev.app.getGuiNode().detachChild(highscoreText);
        aktiv=false;
    }
    
    @Override
     public void deInit()
    {
        lev.app.getGuiNode().detachChild(rechteckpic);
        lev.app.getGuiNode().detachChild(highscoreText);
        clearKeys();
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if(aktiv && !lev.playernameinputting)
        {
            if(name.equals("Enter") && !isPressed)
            {
                lev.enterLevel(levelid);
            }
            
            if(name.equals("Back") && !isPressed)
            {
                lev.player.marble_phy.applyImpulse(new Vector3f(1f,5f,0f), Vector3f.ZERO);
            }
        }
        
        if(aktiv && lev.playernameinputting && (name.equals("Enter") && !isPressed))
        {
            lev.endInputPlayerName();
        }
    }
}
