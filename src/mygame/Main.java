package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Logger;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    private String startlevel = "Scenes/Building_start/Building.j3o";
    private String[] levelList = {"Scenes/Building/Building.j3o"};
    private int beiLevel = 0;
    private static String[] argumente = null;
    private Vector3f playerLevelPos = Vector3f.ZERO;
    private HashMap<Integer, HighScore> highscores = new HashMap<Integer, HighScore>();

    public static void main(String[] args) {
        argumente = args;
        Main app = new Main();
        AppSettings newSetting = new AppSettings(true);
        newSetting.setBitsPerPixel(32);
        newSetting.setFullscreen(true);
        newSetting.setVSync(true);
        
         Toolkit toolkit =  Toolkit.getDefaultToolkit ();
        Dimension dim = toolkit.getScreenSize();
        
        newSetting.setHeight(dim.height);
        newSetting.setWidth(dim.width);
        newSetting.setFrameRate(60);
        app.setSettings(newSetting);
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
        app.start();
    }
    private Level lev = null;

    @Override
    public void simpleInitApp() {
        loadHighScore();
        if (argumente.length > 0) {
            try {
                beiLevel = Integer.parseInt(argumente[0]);
            } catch (Exception ex) {
            }

        }
        this.flyCam.setMoveSpeed(0);
        this.flyCam.setRotationSpeed(0);
        lev = new Level(rootNode, assetManager, inputManager, cam, viewPort, audioRenderer, guiViewPort, flyCam, stateManager, startlevel, this);
        lev.setscore(getGesamtScore());
    }

    public HighScore getHighScore(int levelid) {
        HighScore hs = highscores.get(levelid);
        if (hs == null) {
            hs = new HighScore();
            highscores.put(levelid, hs);
        }
        return hs;
    }

    public int getGesamtScore() {
        int score = 0;
        Set<Entry<Integer, HighScore>> set = highscores.entrySet();

        for (Entry<Integer, HighScore> ent : set) {
            if (ent.getValue().entries.length > 0) {
                score += ent.getValue().entries[0].score;
            }
        }
        return score;
    }

    public void loadHighScore() {
        highscores.clear();
        try {
            File file = new File("scores.dat");
            if (file.exists()) {
                Scanner scn = new Scanner(file);
                while (scn.hasNextLine()) {
                    String line = scn.nextLine();
                    String[] values = line.split(";");
                    if (values.length == 3) {
                        int levelid = Integer.parseInt(values[0]);
                        String name = values[1];
                        int score = Integer.parseInt(values[2]);
                        HighScore hs = getHighScore(levelid);
                        hs.addEntry(new HighScoreEntry(score, name));
                    }
                }

                scn.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveHighScore() {
        try {
            File file = new File("scores.dat");
            FileWriter fw = new FileWriter(file);

            //fw.write("etwas Text");
            Set<Entry<Integer, HighScore>> set = highscores.entrySet();

            for (Entry<Integer, HighScore> ent : set) {
                Integer levelid = ent.getKey();
                for (HighScoreEntry hsent : ent.getValue().entries) {
                    fw.write(levelid.toString() + ";" + hsent.name + ";" + hsent.score + "\r\n");
                }
            }

            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
        if (lev != null) {
            if (!lev.Update(tpf)) {
                if (lev.endAction == Level.EndAction.EnterLevel) {
                    playerLevelPos = lev.player.marble_phy.getPhysicsLocation().clone();
                    lev.deInit();

                    beiLevel = lev.golevelnumber;
                    lev = new Level(rootNode, assetManager, inputManager, cam, viewPort, audioRenderer, guiViewPort, flyCam, stateManager, levelList[lev.golevelnumber], this);

                } else if (lev.endAction == Level.EndAction.LevelFinish) {
                    int score = lev.score;
                    HighScore hg = getHighScore(beiLevel);
                    lev.deInit();
                    lev = new Level(rootNode, assetManager, inputManager, cam, viewPort, audioRenderer, guiViewPort, flyCam, stateManager, startlevel, this);
                    lev.player.setPos(playerLevelPos);
                    lev.setscore(getGesamtScore());
                    if (hg.checkScorePos(score) >= 0) {
                        /*JFrame_EnterScore fs = new JFrame_EnterScore();
                         fs.setVisible(true);
                         while (fs.isShowing()) {
                         try {
                         Thread.sleep(100);
                         } catch (InterruptedException ex) {
                         Logger.getGlobal().warning(ex.toString());
                         }
                            
                         }
                         hg.addEntry(new HighScoreEntry(score, fs.jTextField1.getText()));
                         fs.dispose();*/
                        lev.startInputPlayerName(hg, score);
                    }


                }
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
        if (lev != null) {
            lev.Render(rm);
        }
    }
}
