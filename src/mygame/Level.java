/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioRenderer;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsSweepTestResult;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.binary.BinaryExporter;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.MatParamTexture;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.filters.RadialBlurFilter;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.*;
import com.jme3.scene.control.LodControl;
import com.jme3.shader.VarType;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import com.jme3.util.TangentBinormalGenerator;
import com.jme3.water.WaterFilter;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
/**
 *
 * @author Finn
 */
public class Level extends GameState implements ScreenController {
    
    public enum EndAction{
        LevelFinish,
        GameEnd,
        LevelAbort,
        EnterLevel
        
    }
    
    List<GameObject> gameObjects=new ArrayList<GameObject>();
    BulletAppState bulletAppState=null;
    private Boolean levelBeenden=false;
    public EndAction endAction=EndAction.GameEnd;
    private Texture skytex=null;
    private List<GameObject> goAddList = new ArrayList<GameObject>();
    private List<GameObject> goRemList = new ArrayList<GameObject>();
    public Go_Marble player=null;
    public AudioNode trianglesound=null;
    public RadialBlurFilter blurFilter = null;
    public Main app=null;
    private ScoreGui scoregui=null;
    public int score=0;
    public int golevelnumber=0;
    private AudioNode music=null;
    
    public Level(Node _rootNode,AssetManager _assetManager,InputManager _inputManager,Camera _cam,ViewPort _viewPort,AudioRenderer _audioRenderer,ViewPort _guiViewPort,FlyByCamera _flyCam,AppStateManager _stateManager,String levelFile,Main sa)
    {
        
        super(_rootNode, _assetManager, _inputManager, _cam, _viewPort, _audioRenderer, _guiViewPort, _flyCam,_stateManager);
        app=sa;
        bulletAppState = new BulletAppState();
        
        stateManager.attach(bulletAppState);
        //Sky
        TextureKey skyhi = new TextureKey("Textures/park_cubemap/park_cubemap.dds", true);
        skyhi.setGenerateMips(true);
        skyhi.setAsCube(true);
      skytex = assetManager.loadTexture(skyhi);

      
//      TextureKey skylow = new TextureKey("Textures/Water32.dds", true);
//        skylow.setGenerateMips(true);
//        skylow.setAsCube(true);
      final  Texture texlow = assetManager.loadTexture(skyhi);
         rootNode.attachChild(SkyFactory.createSky(assetManager, texlow, false));
        
        
        
        LoadLevel(levelFile);
        
cam.setFrustumPerspective(60.0f,(float)Display.getWidth()/(float)Display.getHeight(), 0.1f,400);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        trianglesound=new AudioNode(assetManager, "Sounds/triangle.wav");
        trianglesound.setReverbEnabled(false);
        trianglesound.setDirectional(false);
        
        scoregui=new ScoreGui(this);
        
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
    }
    
    private float beicoinpitch=0.6f;
    private float timesincelastcoin=0f;
    
    public void getCoin()
    {
        
        
        
        
        
        if(timesincelastcoin>1f)
        {
            beicoinpitch=0.6f;
        }else
        {
            beicoinpitch+=0.1f;
            if(beicoinpitch>2f)
            {
                beicoinpitch=2f;
            }
        }
        timesincelastcoin=0;
        
        score+=(beicoinpitch*10f)-5;
        scoregui.setNumber(score);
        
        trianglesound.setPitch(beicoinpitch);
        trianglesound.playInstance();
    }
    
    public void setscore(int score)
    {
        this.score=score;
        scoregui.setNumber(score);
    }
    
    public boolean playernameinputting=false;
    private NiftyJmeDisplay niftyNameInput=null;
    private Nifty nifty=null;
    private HighScore tempHighScore=null;
    private int tempScore=0;
    private ViewPort niftyViewPort=null;
    
    public void startInputPlayerName(HighScore hs,int score)
    {
        this.tempHighScore=hs;
        tempScore=score;
        niftyViewPort = app.getRenderManager().createPostView("Nifty view", cam);
        niftyViewPort.setClearDepth(true);
        niftyNameInput=new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, niftyViewPort);
        nifty = niftyNameInput.getNifty();
        nifty.fromXml("Interface/Nifty_Nameneingeben.xml", "start");
       // guiViewPort.addProcessor(niftyNameInput);
        
        niftyViewPort.addProcessor(niftyNameInput);
        playernameinputting=true;
        
    }
    
    public void endInputPlayerName()
    {
       
        String playername=nifty.getScreen("start").findNiftyControl("text", TextField.class).getText().replace(';', ' ');
        tempHighScore.addEntry(new HighScoreEntry(tempScore, playername));
        app.saveHighScore();
        nifty.exit();
        niftyNameInput.cleanup();
        niftyViewPort.removeProcessor(niftyNameInput);
        
        app.getRenderManager().removePostView("Nifty view");
        
        for(GameObject go:gameObjects)
        {
            if(go instanceof Go_Trigger_Levelwarp)
            {
                ((Go_Trigger_Levelwarp)go).updateHighScore();
            }
        }
        playernameinputting=false;
        setscore(app.getGesamtScore());
    }
    
    public void levelFinish()
    {
        levelBeenden=true;
        endAction=EndAction.LevelFinish;
    }
    
    public void enterLevel(int levelid)
    {
        levelBeenden=true;
        endAction=EndAction.EnterLevel;
        golevelnumber=levelid;
    }
    
    public void AddGameObject(GameObject go)
    {
        goAddList.add(go);
    }
    
    public void RemoveGameObject(GameObject go)
    {
        goRemList.add(go);
    }
    
    public void Reset()
    {
        for(GameObject go:gameObjects)
        {
            go.Reset();
        }
        score=0;
        scoregui.setNumber(0);
    }
    
    public static PhysicsSweepTestResult getClosestSweepCollision(List<PhysicsSweepTestResult> collisions,PhysicsCollisionObject ignoreObj)
    {
        PhysicsSweepTestResult mincoll=null;
        for(int i=0;i<collisions.size();i++){
            if(collisions.get(i).getCollisionObject()!=ignoreObj){
            if(mincoll==null)
            {
                mincoll=collisions.get(i);
            }else{
                if(mincoll.getHitFraction()>collisions.get(i).getHitFraction())
                    mincoll=collisions.get(i);
            }
        }
        }
        return mincoll;
    }
    
    public static Geometry getFirstGeomFromNode(Spatial spat)
    {
        if(spat==null)
        {
            return null;
            
        }else
        {
            if(spat instanceof Geometry)
            {
                return (Geometry)spat;
            }else
            {
                if(spat instanceof Node)
                {
                    Node node=(Node)spat;
                    List<Spatial> sl=node.getChildren();
                    if(sl.size()>0)
                    {
                        return getFirstGeomFromNode(sl.get(0));
                    }
                }
            }
        }
        return null;
    }
    
    public static List<Geometry> getGeometriesFromNode(Spatial spat)
    {
        List<Geometry> geolist=new ArrayList<Geometry>();
        if(spat==null)
        {
            return geolist;
            
        }else
        {
            if(spat instanceof Geometry)
            {
                geolist.add((Geometry)spat);
            }else
            {
                if(spat instanceof Node)
                {
                    Node node=(Node)spat;
                    List<Spatial> sl=node.getChildren();
                    for(Spatial sp:sl)
                    {
                        geolist.addAll(getGeometriesFromNode(sp));
                    }
                }
            }
        }
        return geolist;
    }
    
    private void setFogMaterials(Spatial spat)
    {
        List<Geometry> geolist=getGeometriesFromNode(spat);
        
        for(Geometry geom:geolist)
        {
            Material oldmat=geom.getMaterial();
            Material mat = new Material(assetManager, "MatDefs/LightBlow/LightBlow.j3md");
            
            
            MatParamTexture tp= oldmat.getTextureParam("DiffuseMap");
            if(tp!=null)
            {
                mat.setTexture("FogSkyBox", skytex);
            mat.setTexture("DiffuseMap", tp.getTextureValue());
            mat.setColor("FogColor", new ColorRGBA(0.7f,0.7f,0.7f,20f));
            geom.setMaterial(mat);
            }
        }
        TangentBinormalGenerator.generate(spat);
    }
    
    private void debugsave(Node node)
    {
        String userHome = System.getProperty("user.home");
    BinaryExporter exporter = BinaryExporter.getInstance();
    File file = new File(userHome+"/Models/"+"MyModel.j3o");
    try {
      exporter.save(rootNode, file);
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, "Error: Failed to save game!", ex);
    }
    }
    
    private void setDeatail(Spatial sp)
    {
        List<Geometry> geoms =getGeometriesFromNode(sp);
        
        for(Geometry geom:geoms)
        {
            Mesh ms= geom.getMesh();
            VertexBuffer emptybuffer=new VertexBuffer(VertexBuffer.Type.Index);
            emptybuffer.setupData(VertexBuffer.Usage.Static, 1, VertexBuffer.Format.Int,BufferUtils.createIntBuffer(0));
            VertexBuffer indbuf=ms.getBuffer(VertexBuffer.Type.Index);
            ms.setLodLevels(new VertexBuffer[]  {indbuf,emptybuffer});
            
            //geom.setLodLevel(1);
            MyLodControl lc=new MyLodControl();
            lc.setDistTolerance(0.5f);
            lc.setTrisPerPixel(0f);
            geom.addControl(lc);
        
        }
        //LodControl lc=new LodControl();
      //  sp.addControl(lc);
    }
    
    private static  ArrayList<Integer> editedmeshes=new ArrayList<Integer>();
    
    private void setVertexAlpha(Spatial sp)
    {
        List<Geometry> geos=getGeometriesFromNode(sp);
        
        for(Geometry geo:geos)
        {
        
        Material mat= geo.getMaterial();
                //VertexBuffer vb=geo.getMesh().getBuffer(VertexBuffer.Type.Color);
                Mesh mesh=geo.getMesh();
                if(!editedmeshes.contains(mesh.hashCode())){
                    
                    editedmeshes.add(mesh.hashCode());
                   
                    
                int vertexCount=mesh.getVertexCount();
                float[] colorArray = new float[vertexCount * 4];
                FloatBuffer colorBuffer = mesh.getFloatBuffer(VertexBuffer.Type.Color);
               // colorArray=colorBuffer.array();
                for(int j=0;j<vertexCount*4;j++){
                    colorArray[j]=colorBuffer.get();
                }
                
                 for (int j = 0; j < vertexCount; j++) {
                     
                     colorArray[j*4+3]=colorArray[j*4];
                     colorArray[j*4]=1f;
                     colorArray[j*4+1]=1f;
                     colorArray[j*4+2]=1f;

                }
                 
                colorBuffer.clear();
                colorBuffer.put(colorArray);
                mesh.setBuffer(VertexBuffer.Type.Color, 4, colorBuffer);
                }
                mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                //mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off); // show back side too
                mat.getAdditionalRenderState().setAlphaTest(true); // alpha on each face
                geo.setMesh(mesh);
                geo.setQueueBucket(RenderQueue.Bucket.Transparent); // IMPORTANT
                //colorBuffer.;
        }
    }
    
    private void setPerPixelLighting(Geometry geo)
    {
        /*Material mat= geo.getMaterial();
        if(mat.getParam("VertexLighting")!=null)
        {
        mat.setBoolean("VertexLighting", false);
        mat.setBoolean("HighQuality", true);
        mat.setBoolean("LowQuality", false);
        }*/
        /*Material mat2 = assetManager.loadMaterial("Materials/bk.j3m");
        geo.setMaterial(mat2);*/
        geo.getMaterial().getAdditionalRenderState().setWireframe(true);
    }
    
    private Geometry marblegeom=null;
    Spatial marblespat=null;
    private FilterPostProcessor fpp=null;
    public Node levelnode=null;
    RigidBodyControl landscape=null;
    private void LoadLevel(String LevelDatei)
    {
        levelnode=(Node)assetManager.loadModel(LevelDatei);
        Util.removeLightsFromSpatial(levelnode);
        
        rootNode.attachChild(levelnode);
        List<Spatial> objectList=levelnode.getChildren();
        List<Spatial> removeList=new ArrayList<Spatial>();
        List<Spatial> addList=new ArrayList<Spatial>();
        GameObject player=null;
        Go_Stern sternobj=null;
        fpp=new FilterPostProcessor(assetManager);
        String musicfile=null;
        
        for(Spatial sp:objectList)
        {
            if(sp.getName().equals("player_start"))
            {
                removeList.add(sp);
                //Player erstellen
                marblespat=assetManager.loadModel("Models/Marble/Marble2.j3o");
                marblegeom=getFirstGeomFromNode(marblespat);
                
                //marblegeom.getMaterial().setTransparent(true);
                
                
                marblespat.setLocalTranslation(sp.getWorldTranslation());
                marblespat.setLocalScale(10f);
                
                Go_Marble marble=new Go_Marble(marblespat,this);
                Object fc=sp.getUserData("fixedcam");
                if(fc instanceof String)
                {
                    fc=fc.equals("True");
                }
                if(fc!=null)
                {
                    if((Boolean)fc)
                    {
                        marble.camTyp=Go_Marble.CameraType.fixed;
                    }
                    Object fch=sp.getUserData("camhight");
                    if(fch!=null)
                    {
                        marble.fixedCamHight=(Float)fch;
                    }
                    
                }
                
                Object musfile=sp.getUserData("music");
                if(musfile!=null)
                {
                    if(musfile instanceof String)
                    {
                        musicfile=(String)musfile;
                    }
                }
                
                player=marble;
                
                gameObjects.add(marble);
                this.player=marble;
                
            }
            else
                if(sp.getName().equals("level_end"))
                {
                    removeList.add(sp);
                    Spatial sternspat=assetManager.loadModel("Models/Stern/Stern.j3o");
                    Geometry sterngeom=getFirstGeomFromNode(sternspat);
                    sterngeom.setLocalTranslation(sp.getWorldTranslation());
                    sterngeom.setLocalRotation(sp.getWorldRotation());
                    Material mat=sterngeom.getMaterial();
                    mat.setColor("Diffuse", new ColorRGBA(1, 1, 1, 0.5f));
                    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                    sterngeom.setQueueBucket(RenderQueue.Bucket.Transparent);
                    addList.add(sterngeom);
                    Go_Stern stern=new Go_Stern(sterngeom,this);
                    gameObjects.add(stern);
                    sternobj=stern;
                    
                }
                else
                    if(sp.getName().startsWith("death_"))
                    {
                        removeList.add(sp);
                        
                        
                        
                        addList.add(sp);
                        CollisionShape deathShape =
                            BetterCollisionShapeFactory.createMeshShape( sp);
        
                        RigidBodyNameControl deathscape = new RigidBodyNameControl(deathShape, 0);
                        sp.addControl(deathscape);
                        deathscape.name="death";
                        bulletAppState.getPhysicsSpace().add(deathscape);
                    }else
                    if(sp.getName().startsWith("lava_"))
                    {
                        removeList.add(sp);
                        
                        
                        
                        addList.add(sp);
                        CollisionShape deathShape =
                            BetterCollisionShapeFactory.createMeshShape( sp);
        
                        RigidBodyNameControl deathscape = new RigidBodyNameControl(deathShape, 0);
                        sp.addControl(deathscape);
                        deathscape.name="death";
                        bulletAppState.getPhysicsSpace().add(deathscape);
                    }
                else
                    if(sp.getName().startsWith("muenze_"))
                    {
                        removeList.add(sp);
                        addList.add(sp);
                        Geometry muenzgeom=getFirstGeomFromNode(sp);
                        Go_Muenze muenze=new Go_Muenze(muenzgeom,this);
                        gameObjects.add(muenze);
                    }
                else
                    if(sp.getName().startsWith("levelwarp_"))
                    {
                        removeList.add(sp);
                        
                        int levelid=1;
                        Object li=sp.getUserData("levelid");
                        if(li!=null)
                        {
                            if(li instanceof Integer)
                            {
                                levelid=(Integer)li;
                            }
                            if(li instanceof String)
                            {
                                levelid=Integer.parseInt((String)li);
                            }
                        }
                        Go_Trigger_Levelwarp lw=new Go_Trigger_Levelwarp(sp, this,levelid);
                        gameObjects.add(lw);
                    }
                else
                    if(sp.getName().startsWith("water_"))
                    {
                        removeList.add(sp);
                        WaterFilter wf=new WaterFilter(rootNode,new Vector3f(0,-1,0));
                        wf.setWaterHeight(sp.getWorldTranslation().y);
                        wf.setUseFoam(false);
                        wf.setWaterColor(ColorRGBA.Red);
                        wf.setLightColor(ColorRGBA.Gray);
                        wf.setWaterTransparency(0.50f);
                        wf.setDeepWaterColor(ColorRGBA.Red);
                        wf.setNormalScale(1f);
                        wf.setWaveScale(0.005f);
                        wf.setUseSpecular(false);
                        wf.setMaxAmplitude(0.1f);
                        wf.setRefractionStrength(0f);
                        wf.setSpeed(0.3f);
                        wf.setReflectionDisplace(10);
                        wf.setRefractionConstant(0f);
                        wf.setColorExtinction(new Vector3f(1f,1f,1f));
                        
                        fpp.addFilter(wf);
                    }
               else
                   if(sp.getName().startsWith("soccerplayer_"))
                   {
                       removeList.add(sp);
                       Spatial soccerspat=assetManager.loadModel("Models/Soccerplayer/soccerplayer.j3o");
                       soccerspat.setLocalTranslation(sp.getWorldTranslation());
                       soccerspat.setLocalScale(0.1f);
                       addList.add(soccerspat);
                       Go_SoccerPlayer soccerp=new Go_SoccerPlayer(soccerspat, this);
                       gameObjects.add(soccerp);
                   }
               else
                   if(sp.getName().startsWith("nocollision_"))
                   {
                       removeList.add(sp);
                       addList.add(sp);
                   }
                   if(sp.getName().startsWith("detail_"))
                   {
                       setDeatail(sp);
                   }
                   else
                   if(sp.getName().startsWith("vertalpha_"))
                   {
                       setVertexAlpha(sp);
                   }
                   else
                   if(sp.getName().startsWith("invisible_"))
                   {
                       sp.setCullHint(Spatial.CullHint.Always);
                   }
                        
            else
            {
                List<Geometry> geomlist=getGeometriesFromNode(sp);
                for(Geometry geom:geomlist)
                {
                    //setPerPixelLighting(geom);
                }
            }
            
            
        }
        
        for(Spatial sp:removeList)
        {
            ((Node)levelnode).detachChild(sp);
        }
        
        
        
        CollisionShape sceneShape =
                BetterCollisionShapeFactory.createMeshShape((Node) levelnode);
        
        landscape = new RigidBodyControl(sceneShape, 0);
        
        levelnode.addControl(landscape);
        bulletAppState.getPhysicsSpace().add(landscape);
        rootNode.attachChild(marblespat);
        for(Spatial sp:addList)
        {
            ((Node)levelnode).attachChild(sp);
        }
        if(player!=null && sternobj!=null)
        {
            sternobj.setPlayer(player);
        }
        
        
        
        //Light
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(0.4f));
        //setFogMaterials(rootNode);
        
        
        rootNode.addLight(al);
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White.mult(0.6f));
        dl.setDirection(new Vector3f(2.8f, -6.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
        
        
        //fog
        
        blurFilter = new RadialBlurFilter();
        blurFilter.setSampleStrength(0f);
        blurFilter.setSampleDistance(0f);
        fpp.addFilter(blurFilter);
        viewPort.addProcessor(fpp);
        
        Util.EnhanceSpatMat(this.rootNode, assetManager);
        
        for(GameObject go:gameObjects)
        {
            go.Init();
        }
        
        //debugsave(rootNode);
        
        //Musik
        if(musicfile!=null)
        {
            music=new AudioNode(assetManager, musicfile);
            music.setDirectional(false);
            music.setPositional(false);
            music.setReverbEnabled(false);
            music.setLooping(true);
            music.play();
        }
    }
    
    @Override
    public void Init()
    {
        
    }
    
    @Override
    public void deInit()
    {
        for(GameObject go:gameObjects)
        {
            go.deInit();
        }
        fpp.removeAllFilters();
        viewPort.removeProcessor(fpp);
        scoregui.Dispose();
        
        if(music!=null)
        {
            music.stop();
        }
        bulletAppState.getPhysicsSpace().remove(landscape);
        super.deInit();
    }
    
    @Override
    public boolean Update(float tpf)
    {
        for(GameObject go:gameObjects)
        {
            go.Update(tpf);
        }
        
        for(GameObject go:goAddList)
        {
            gameObjects.add(go);
            go.Init();
        }
        goAddList.clear();
        
        for(GameObject go:goRemList)
        {
            go.deInit();
            gameObjects.remove(go);
        }
        goRemList.clear();
        scoregui.update(tpf);
        timesincelastcoin+=tpf;
        return !levelBeenden;
    }
    
    public void Render(RenderManager rm) {
        //TODO: add render code
         
    }
}
