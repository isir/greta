/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.player.ogre;

import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import vib.auxiliary.player.ogre.natives.Animation;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Mesh;
import vib.auxiliary.player.ogre.natives.MeshManager;
import vib.auxiliary.player.ogre.natives.NameValuePairList;
import vib.auxiliary.player.ogre.natives.Node;
import vib.auxiliary.player.ogre.natives.RenderTarget;
import vib.auxiliary.player.ogre.natives.RenderWindow;
import vib.auxiliary.player.ogre.natives.ResourceGroupManager;
import vib.auxiliary.player.ogre.natives.Root;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneManager.PrefabType;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.VertexAnimationTrack;
import vib.auxiliary.player.ogre.natives.VertexData;
import vib.auxiliary.player.ogre.natives.VertexDeclaration;

/**
 *
 * @author Andre-Marie Pez
 */
public class Ogre {

    private static final String OpenGL = "OpenGL Rendering Subsystem";
    private static final String DirectX9 = "Direct3D9 Rendering Subsystem";
    private static Root root;
    private static ArrayList<String> secnes = new ArrayList<String>();
    private static int renderCount = 0;
    private static boolean useOpenGL = true;
    private static LinkedList<Object> keepIt = new LinkedList<Object>();
    public static final boolean DEBUG;
    private static String resourceGroup;
    public static boolean realTime = true;
    private static String externalLibPath = IniManager.getProgramPath() + "Player/Lib/External/";

    static {
        DEBUG = IniManager.getGlobals().getValueBoolean("OGRE_DEBUG");
        int jvmArchitecture = Integer.parseInt(System.getProperty("sun.arch.data.model"));

        externalLibPath = IniManager.getProgramPath() + "Player/Lib/External/";

        //define path where are Ogre's native libraries
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            //Windows:
            if (jvmArchitecture == 64) {
                externalLibPath += "Win64/";
            } else {
                externalLibPath += "Win32/";
            }
        } else {
            //TODO Mac OS

            //linux:
            if (jvmArchitecture == 64) {
                externalLibPath += "Linux64/";
            } else {
                externalLibPath += "Linux32/";
            }
        }
        try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = externalLibPath;
            field.set(null, tmp);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //only "cg" is needed on windows. there is several better ways to do that (like using some config files)
        //but i don't have the time...
        String[] dependenciesToLoad = {"boost_system", "boost_thread", "openjpeg", "jpeg", "raw", "Half", "Iex", "IlmThread", "IlmImf", "freeimage", "zzip", "Cg"};
        for(String s : dependenciesToLoad){
            try {
                System.loadLibrary(s);
            }
            catch (Throwable e) {
                if(DEBUG){
                    System.out.println("Lib "+ s + " couldn't be loaded : "+e.getMessage());
                }
            }
        }
        System.loadLibrary("OgreMain");
        System.loadLibrary("OgrePlayerJNI");
    }
    private static OgreThread.Callback initRoot = new OgreThread.Callback() {
        @Override
        public void run() {

            // Little Hack:
            //     we write a fonctionnal pluginsFile from the pluginsFileModel
            //     with an absolute path for the "PluginFolder" attribute because
            //     the relative path does not work anymore on Windows 8 and 10!

            String pluginsFileModel = externalLibPath + "Configs/Plugins.cfg";
            String pluginsFile = externalLibPath + "Plugins.cfg";

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                if (IniManager.getGlobals().getValueBoolean("OGRE_DIRECTX_9")) {
                    pluginsFileModel = externalLibPath + "Configs/Plugins_DX9.cfg";
                    pluginsFile = externalLibPath + "Plugins_DX9.cfg";
                } else {
                    pluginsFileModel = externalLibPath + "Configs/Plugins_OpenGL.cfg";
                    pluginsFile = externalLibPath + "Plugins_OpenGL.cfg";
                }
            }

            try {
                StringBuilder pluginsFileContent = new StringBuilder();

                BufferedReader reader = new BufferedReader(new FileReader(pluginsFileModel));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.trim().startsWith("PluginFolder=")) {
                        currentLine = "PluginFolder=" + externalLibPath + "Libs";
                    }
                    pluginsFileContent.append(currentLine).append("\n");
                }

                FileWriter writer = new FileWriter(new File(pluginsFile));
                writer.write(pluginsFileContent.toString());
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            Root tempRoot = new Root(pluginsFile, "", "./Log/Ogre.log");
            if (DEBUG) {
                vib.auxiliary.player.ogre.natives.LogManager.set_LL_BOREME();
            } else {
                vib.auxiliary.player.ogre.natives.LogManager.set_LL_LOW();
            }
            resourceGroup = ResourceGroupManager.getDEFAULT_RESOURCE_GROUP_NAME();

            if (System.getProperty("os.name").toLowerCase().contains("windows")
                    && IniManager.getGlobals().getValueBoolean("OGRE_DIRECTX_9")) {
                useOpenGL = false;
                tempRoot.setRenderSystem(DirectX9);
            } else {
                useOpenGL = true;
                tempRoot.setRenderSystem(OpenGL);
            }

            try {
                tempRoot.initialise(false, "", "");
            } catch (Exception e) {
                //It is OK... we use external windows
            }

            printThread("initRoot");
            root = tempRoot;
        }
    };


    private static class Ref<O> {

        public O instance = null;
    }

    public static boolean useOpenGL() {
        return useOpenGL;
    }

    public static void call(OgreThread.Callback callback) {
        OgreThread.getSingleton().call(callback);
    }

    public static void callSync(OgreThread.Callback callback) {
        OgreThread.getSingleton().callSync(callback);
    }
    public static OgreThread.Callback waitAndRefresh = new OgreThread.Callback() {
        @Override
        public void run() {
            root.renderOneFrame();
            try {
                Thread.sleep(5);
            } catch (Exception e) {
            }
        }
    };

    public static synchronized Root getRoot() {
        if (root == null) {
            //initialize ogre
            callSync(initRoot);

            //add correct cleanup at shutdown
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    OgreThread.getSingleton().setWaitCallback(null);
                    callSync(new OgreThread.Callback() {
                        @Override
                        public void run() {
                            OgreThread.getSingleton().shutdown();
                            root.shutdown();
                        }
                    });
                }
            });

            //to refresh actives windows
            OgreThread.getSingleton().setWaitCallback(waitAndRefresh);
        }
        return root;
    }

    public static synchronized SceneManager getSceneManager(final String sceneName) {
        if (secnes.contains(sceneName)) {
            return getRoot().getSceneManager(sceneName);
        } else {
            getRoot();//ensure root is initialized
            callSync(new OgreThread.Callback() {
                @Override
                public void run() {
                    printThread("createSceneManager");
                    SceneManager newSceneManager = root.createSceneManager(
                            //"OctreeSceneManager"
                            "DefaultSceneManager"
                            , sceneName);
                    if (DEBUG) {
                        newSceneManager.showBoundingBoxes(true);
                    }
                    //"TerrainSceneManager" "DefaultSceneManager" greta.auxiliary.player.ogre.natives.SceneType.ST_GENERIC.getValue()
                    secnes.add(sceneName);
                }
            });
            return getRoot().getSceneManager(sceneName);
        }
    }

    public static RenderWindow createWindow(String handle, int width, int height) {
        return createWindow(handle, width, height, 4, false);
    }
    public static RenderWindow createWindow(final String handle, final int width, final int height, final int fsaaLevel, final boolean useVsych) {
        getRoot();//initialize ogre
        final Ref<RenderWindow> renderWindow = new Ref<RenderWindow>();
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                NameValuePairList params = new NameValuePairList();

                if(handle!=null){
                    String windowHandlePointer = handle;
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        params.insert("externalWindowHandle", windowHandlePointer); //windows
                    } else {
                        params.insert("parentWindowHandle", windowHandlePointer); //linux
                    }
                }

                if(fsaaLevel != 0){
                    params.insert("FSAA", ""+fsaaLevel);
                }

                if(useVsych){
                    params.insert("vsync", "true");
                }

                renderWindow.instance = getRoot().createRenderWindow("Render-" + (renderCount++), width, height, false, params);
                renderWindow.instance.setVisible(true);
                renderWindow.instance.setAutoUpdated(true);
                renderWindow.instance.setActive(true);

                //initialize resources after the first window was ceated
                initialiseResources();


                printThread("createWindow");
            }
        });
        return renderWindow.instance;
    }

    private static void initialiseResources() {
        if (!ResourceGroupManager.getSingleton().isResourceGroupInitialised(resourceGroup)) {
            String shadersPath = "./Player/Data/Shaders/";
            String baseMaterialPath = shadersPath+IniManager.getGlobals().getValueString("OGRE_BASE_SHADER");
            if(shadersPath.equals(baseMaterialPath) || !(new java.io.File(baseMaterialPath+"/base.material")).exists()){
                baseMaterialPath = shadersPath+"Basic";
            }

            ResourceGroupManager.getSingleton().addResourceLocation(baseMaterialPath, "FileSystem", resourceGroup, true);
            ResourceGroupManager.getSingleton().addResourceLocation("./Player/Data/media", "FileSystem", resourceGroup, true);
            ResourceGroupManager.getSingleton().initialiseAllResourceGroups();
            printThread("Resources initialised");
        }
    }

    public static boolean isResourceExists(String fileName){
        return ResourceGroupManager.getSingleton().resourceExists(resourceGroup, fileName);
    }

    /**
     * waiting for the the end of the OgreThread stack
     */
    public static void waitingForEndStack() {
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                System.out.println("toto");
            }
        });
    }

    protected static void printTread() {
        printThread(null);
    }

    protected static void printThread(String msg) {
        if (DEBUG) {
            Logs.debug(Thread.currentThread().getName() + (msg == null || msg.isEmpty() ? " Ogre.printThread" : " Ogre.printThread : " + msg));
        }
    }

    public static void requestResize(final RenderTarget toResize) {
        if (toResize instanceof RenderWindow) {
            callSync(new OgreThread.Callback() {
                @Override
                public void run() {
                    ((RenderWindow) toResize).windowMovedOrResized();
                }
            });
        }

    }

    public static void print(vib.auxiliary.player.ogre.natives.Vector3 vect) {
        if (DEBUG) {
            Logs.debug("[x=" + vect.getx() + " y=" + vect.gety() + " z=" + vect.getz() + "]");
        }
    }

    public static Entity createEntity(final SceneManager sceneManager, final String id, final String meshName, final boolean visible) {
        final Ref<Entity> entity = new Ref<Entity>();
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                printThread("create entity " + meshName);
                entity.instance = sceneManager.createEntity(id, meshName);
                entity.instance.setVisible(visible);
            }
        });
        return entity.instance;
    }

    public static void updateNode(final Node node, final boolean updateChildren, final boolean parentHasChanged) {
        call(new OgreThread.Callback() {
            @Override
            public void run() {
                node._update(updateChildren, parentHasChanged);
            }
        });
    }

    public static void updateNodeSync(final Node node, final boolean updateChildren, final boolean parentHasChanged) {
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                node._update(updateChildren, parentHasChanged);
            }
        });
    }

    public static Entity createEntity(final SceneManager sceneManager, final String id, final PrefabType prefabType, final boolean visible) {
        final Ref<Entity> entity = new Ref<Entity>();
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                printThread("create entity " + prefabType);
                entity.instance = sceneManager.createEntity(id, prefabType);
                entity.instance.setVisible(visible);
            }
        });
        return entity.instance;
    }

    public static void setMaterial(final Entity entity, final String materialName) {
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                printThread("setMaterial " + materialName);
                entity.setMaterialName(materialName);
            }
        });
    }

    public static void setMaterial(final Entity entity, final String materialName, final int target) {
        if (target == 0) {
            setMaterial(entity, materialName);
        } else {
            if (target < 0 || target > entity.getNumSubEntities()) {
                return;
            }
            callSync(new OgreThread.Callback() {
                @Override
                public void run() {
                    printThread("setMaterial " + materialName);
                    entity.getSubEntity(target - 1).setMaterialName(materialName);
                }
            });
        }
    }

    public static void dontDelete(Object o) {
        keepIt.add(o);
    }

    public static Mesh getMesh(Entity entity) {
        return entity.getMesh();
    }

    public static Mesh getMesh(final String meshFileName) {
        final Ref<Mesh> mesh = new Ref<Mesh>();
        callSync(new OgreThread.Callback() {
            @Override
            public void run() {
//                MeshPtr meshPtr = new MeshPtr();
                mesh.instance = MeshManager.load(meshFileName, resourceGroup);
//                mesh.instance = meshPtr.getPointer();
            }
        });
        return mesh.instance;
    }

    public static VertexAnimationTrack getPoseAnimationTrack(final Mesh mesh, final String AnimationName, final int targetSubmesh) {
        if (!mesh.hasAnimation(AnimationName)) {
            final Ref<VertexAnimationTrack> vertexAnimationTrack = new Ref<VertexAnimationTrack>();
            callSync(new OgreThread.Callback() {
                @Override
                public void run() {
                    Animation anim = mesh.createAnimation(AnimationName, 0);
                    vertexAnimationTrack.instance = anim.createVertexTrack_VAT_POSE(targetSubmesh);
                    //update changes
                    VertexData vdata = targetSubmesh == 0 ? mesh.getsharedVertexData() : mesh.getSubMesh(targetSubmesh - 1).getvertexData();
                    VertexDeclaration newdcl = vdata.getvertexDeclaration().getAutoOrganisedDeclaration(false, true);
                    vdata.reorganiseBuffers(newdcl);
                    Ogre.printThread(mesh.getName() + " " + targetSubmesh);
                }
            });

            return vertexAnimationTrack.instance;
        }
        return mesh.getAnimation(AnimationName).getVertexTrack(targetSubmesh);
    }

    public static void pitch(final SceneNode node, final double radian) {
        call(new OgreThread.Callback() {
            @Override
            public void run() {
                node.pitch(radian);
            }
        });
    }

    public static void yaw(final SceneNode node, final double radian) {
        call(new OgreThread.Callback() {
            @Override
            public void run() {
                node.yaw(radian);
            }
        });
    }

    public static void roll(final SceneNode node, final double radian) {
        call(new OgreThread.Callback() {
            @Override
            public void run() {
                node.roll(radian);
            }
        });
    }

    public static vib.auxiliary.player.ogre.natives.Vector3 convert(greta.core.util.math.Vec3d vec) {
        return new vib.auxiliary.player.ogre.natives.Vector3((float)vec.x(), (float)vec.y(), (float)vec.z());
    }

    public static greta.core.util.math.Vec3d convert(vib.auxiliary.player.ogre.natives.Vector3 vec) {
        return new greta.core.util.math.Vec3d(vec.getx(), vec.gety(), vec.getz());
    }

    public static vib.auxiliary.player.ogre.natives.Quaternion convert(greta.core.util.math.Quaternion quat) {
        return new vib.auxiliary.player.ogre.natives.Quaternion(quat.w(), quat.x(), quat.y(), quat.z());
    }

    public static greta.core.util.math.Quaternion convert(vib.auxiliary.player.ogre.natives.Quaternion quat) {
        return new greta.core.util.math.Quaternion(quat.getx(), quat.gety(), quat.getz(), quat.getw());
    }

    public static vib.auxiliary.player.ogre.natives.ColourValue convertToColor(vib.auxiliary.player.ogre.natives.Vector3 vec) {
        return new vib.auxiliary.player.ogre.natives.ColourValue(vec.getx(), vec.gety(), vec.getz(), 1);
    }

    static vib.auxiliary.player.ogre.natives.ColourValue convertToColor(greta.core.util.math.Vec3d vec) {
        return new vib.auxiliary.player.ogre.natives.ColourValue(vec.x(), vec.y(), vec.z(), 1);
    }
}
