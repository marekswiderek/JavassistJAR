// Author Marek Swiderek

package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import javassist.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Main extends Application {

    private static String path;
    private static ClassPool cp = new ClassPool(true);
    private static List<String> classNames = new ArrayList<String>();
    private static List<CtClass> ctClasses = new ArrayList<CtClass>();
    private static Map<String,TreeItem<String>> addedPackages = new HashMap<String,TreeItem<String>>();
    private static Manifest manifest;
    private static JarFile jarFile;
    private static List<JarEntry> importedFiles = new ArrayList<JarEntry>();

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("JavassistARexplorer");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static void setJarPath(String jarPath){
        path=jarPath;
    }

    public static String getJarPath(){
        return path;
    }

    public static JarFile getJarFile(){
        return jarFile;
    }

    public  static List<JarEntry> getImportedFiles(){
        return importedFiles;
    }

    public static Manifest getManifest(){
        return manifest;
    }

    public static List<CtClass> getCtClasses(){
        return ctClasses;
    }

    public static void createClassPool(){
        cp.getDefault();
        try {
            cp.appendClassPath(cp.insertClassPath(path));
        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void setClassNames(List<String> names){
        classNames = names;
    }

    public static void loadClassNames() {
        try {
            File pathToJar = new File(path);

            jarFile = new JarFile(pathToJar);
            Enumeration<JarEntry> entries = jarFile.entries();
            ArrayList<String> classes = new ArrayList<String>();

            URL[] urls = {new URL("jar:file:" + pathToJar + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                if (je.getName().endsWith(".class")) {
                    String className = je.getName().substring(0,je.getName().length()-6);
                    className = className.replaceAll("/",".");
                    classes.add(className);
                } else if(!je.isDirectory() && !je.getName().equals("META-INF/MANIFEST.MF")){
                    System.out.println("File - "+ je.getName() + " - successfully loaded.");
                    importedFiles.add(je);
                }

            }
            setClassNames(classes);
        } catch (
                IOException el) {
            // TODO Auto-generated catch block
            el.printStackTrace();

        }
    }

    public static void loadCtClasses(){
        JarInputStream jarFile = null;
        try {
            jarFile = new JarInputStream(new FileInputStream(path));
            manifest = jarFile.getManifest();
            if(manifest.hashCode()!=0) System.out.println("MANIFEST successfully loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] toLoad = new String[classNames.size()];
        for(int i=0; i<classNames.size(); i++){
            toLoad[i] = classNames.get(i);
        }
        try {
            CtClass[] ct = cp.get(toLoad);
            for(int i=0; i<ct.length;i++){
                ctClasses.add(ct[i]);
                System.out.println("Class - " + ctClasses.get(i).getName() + " - successfully loaded.");
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public static TreeItem<String> buildTreeView(){
        TreeItem<String> treeRoot = new TreeItem<String>();
        for(CtClass c: ctClasses){
            String pack = c.getPackageName();
            if(!addedPackages.containsKey(pack)){
                addedPackages.put(pack,makeBranch(pack,treeRoot));
            }
            String name = c.getName();
            TreeItem<String> classBranch = makeBranch(name,addedPackages.get(c.getPackageName()));
            for(CtMethod m: c.getDeclaredMethods()){
                makeBranch(m.getName(),classBranch);
            }
            for(CtField f: c.getDeclaredFields()){
                makeBranch(f.getName(),classBranch);
            }
            for(CtConstructor cc: c.getDeclaredConstructors()){
                makeBranch(cc.getName(), classBranch);
            }
        }
        return treeRoot;
    }

    public static TreeItem<String> makeBranch(String title, TreeItem<String> parent){
    TreeItem<String> item = new TreeItem<String>(title);
    parent.getChildren().add(item);
    return item;
    }

    public static ClassPool getClassPool(){
        return cp;
    }
}
