// Author Marek Swiderek

package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javassist.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Controller {

    @FXML
    Button browseButton;
    @FXML
    Button loadButton;
    @FXML
    TreeView tree;
    @FXML
    TextArea text;

    public void browseJar(ActionEvent actionEvent){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose JAR File");
        try{
            Main.setJarPath(fileChooser.showOpenDialog(stage).toString());
            System.out.println(Main.getJarPath());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        browseButton.setDisable(true);

    }

    public void loadJar(ActionEvent actionEvent){
        Main.createClassPool();
        Main.loadClassNames();
        Main.loadCtClasses();
        loadButton.setDisable(true);
        tree.setRoot(Main.buildTreeView());
        tree.getRoot().setExpanded(true);
        tree.setShowRoot(false);
        tree.setVisible(true);
    }

    public void addPackage(ActionEvent actionEvent){
        try {
            Main.getClassPool().makePackage(new ClassLoader() {
            },text.getText());
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Adding package error.");
        }
        Main.makeBranch(text.getText(),tree.getRoot());
    }

    public void removePackage(ActionEvent actionEvent){
        TreeItem<String> packageNode = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem());
        try{
            for (Iterator<CtClass> iterator = Main.getCtClasses().iterator(); iterator.hasNext();) {
                CtClass clazz = iterator.next();
                if(clazz.getPackageName().equals(packageNode.getValue())){
                    Main.getClassPool().get(clazz.getName()).detach();
                    iterator.remove();
                }
            }
            packageNode.getParent().getChildren().removeAll(packageNode);
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a package!");
        }

    }

    public void addClass(ActionEvent actionEvent){
        String packageName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            if(Main.getClassPool().getOrNull(packageName+"."+text.getText()) == null) {
                CtClass newClass = Main.getClassPool().makeClass(packageName + "." + text.getText());
                TreeItem<String> classBranch = Main.makeBranch(newClass.getName(), ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
                Main.makeBranch(text.getText(), classBranch);
                newClass.writeFile();
                newClass.defrost();
                Main.getCtClasses().add(newClass);
            } else System.out.println("This class already exists.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Adding class error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Class I/O error.");
        } catch (CannotCompileException e) {
            System.out.println("Cannot compile class.");
//            e.printStackTrace();
        }

    }

    public void removeClass(ActionEvent actionEvent){
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            classObj.detach();
            Main.getCtClasses().remove(classObj);
            (((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent()).getChildren().remove(((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));

        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a class!");
        }
    }

    public void addInterface(ActionEvent actionEvent){
        String packageName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            CtClass newInterface = Main.getClassPool().makeInterface(packageName+"."+text.getText());
            Main.getCtClasses().add(newInterface);
            Main.makeBranch(newInterface.getName(), ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
            newInterface.writeFile();
            newInterface.defrost();
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Adding interface error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Interface I/O error.");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Cannot compile interface.");
        }
    }

    public void removeInterface(ActionEvent actionEvent){
        String interfaceName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            CtClass interfaceObj = Main.getClassPool().get(interfaceName);
            interfaceObj.detach();
            Main.getCtClasses().remove(interfaceObj);
            (((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent()).getChildren().remove(((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));

        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose an interface!");
        }
    }

    public void addMethod(ActionEvent actionEvent){
       String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            CtMethod newMet = new CtNewMethod().make(text.getText(),classObj);
            classObj.addMethod(newMet);
            classObj.writeFile();
            classObj.defrost();
            Main.makeBranch(newMet.getName(),((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a class!");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling method error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Method I/O error.");
        }
    }

    public void removeMethod(ActionEvent actionEvent){
        String methodName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            classObj.removeMethod(classObj.getDeclaredMethod(methodName));
            classObj.writeFile();
            classObj.defrost();
            (((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent()).getChildren().remove(((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a method!");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling method error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Method I/O error.");
        }
    }

    public void insertIntoMethodBegin(ActionEvent actionEvent){
        String methodName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            classObj.getDeclaredMethod(methodName).insertBefore(text.getText());
            classObj.writeFile();
            classObj.defrost();
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling method error.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a method!");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Method I/O error.");
        }
    }

    public void insertIntoMethodEnd(ActionEvent actionEvent){
        String methodName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            classObj.getDeclaredMethod(methodName).insertAfter(text.getText());
            classObj.writeFile();
            classObj.defrost();
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling method error.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a method!");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Method I/O error.");
        }
    }

    public void overwriteMethodBody(ActionEvent actionEvent){
        try {
            CtClass classObj = Main.getClassPool().get(((TreeItem<String>)tree.getSelectionModel().getSelectedItem()).getParent().getValue());
            CtMethod currentMethod = classObj.getDeclaredMethod(((TreeItem<String>)tree.getSelectionModel().getSelectedItem()).getValue());
            currentMethod.setBody(text.getText());
            classObj.writeFile();
            classObj.defrost();
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling method error.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a method!");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Method I/O error.");
        }

    }

    public void addConstructor(ActionEvent actionEvent){
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            if(!classObj.isInterface()) {
                CtConstructor newConstructor = CtNewConstructor.make(text.getText(),classObj);
                classObj.addConstructor(newConstructor);
                Main.makeBranch(classObj.getSimpleName(),((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
                classObj.writeFile();
                classObj.defrost();
            } else System.out.println("Please select a class object.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a class!");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling constructor error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Constructor I/O error.");
        }
    }

    public void removeConstructor(ActionEvent actionEvent){
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            CtConstructor constructorObj = classObj.getConstructors()[0];
            classObj.removeConstructor(constructorObj);
            classObj.writeFile();
            classObj.defrost();
            (((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent()).getChildren().remove(((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));

        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a constructor!");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling constructor error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Constructor I/O error.");
        }

    }

    public void overwriteConstructorBody(ActionEvent actionEvent){
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            CtClass classObj = Main.getClassPool().get(className);
            CtConstructor constructorObj = classObj.getConstructors()[0];
            constructorObj.setBody(text.getText());
            classObj.writeFile();
            classObj.defrost();
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a constructor!");
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling constructor error.");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Constructor I/O error.");
        }
    }


    public void addField(ActionEvent actionEvent){
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();

        try {
            CtClass classObj = Main.getClassPool().get(className);
            CtField newFie = CtField.make(text.getText(),classObj);
            classObj.addField(newFie);
            classObj.writeFile();
            classObj.defrost();
            Main.makeBranch(newFie.getName(),((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling field error.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a class!");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Field I/O error!");
        }
    }

    public void removeField(ActionEvent actionEvent){
        String fieldName = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getValue();
        String className = ((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent().getValue();
        try {
            Main.getClassPool().get(className).removeField(Main.getClassPool().get(className).getDeclaredField(fieldName));
            Main.getClassPool().get(className).writeFile();
            Main.getClassPool().get(className).defrost();
            (((TreeItem<String>) tree.getSelectionModel().getSelectedItem()).getParent()).getChildren().remove(((TreeItem<String>) tree.getSelectionModel().getSelectedItem()));
        } catch (CannotCompileException e) {
//            e.printStackTrace();
            System.out.println("Compiling field error.");
        } catch (NotFoundException e) {
//            e.printStackTrace();
            System.out.println("Choose a field!");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Field I/O error!");
        }
    }

    public void exportJAR(ActionEvent actionEvent){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose JAR File");
        String destinationPath;
        try {
            destinationPath = fileChooser.showSaveDialog(stage).toString();
            Manifest manifest = Main.getManifest();
            JarOutputStream jarOutputStream = null;
            try {
                File file = new File(destinationPath);
                OutputStream outputStream = new FileOutputStream(file);
                jarOutputStream = new JarOutputStream(outputStream, manifest);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can't create file.");
            }

            List<String> files = new ArrayList<String>();
            for (CtClass clazz : Main.getCtClasses()) {
                files.add((clazz.getName()).replaceAll("\\.", "/") + ".class");
            }

            int i = 0;
            int l = 0;
            byte[] buf = new byte[1024];
            for (String file : files) {
                try {
                    JarEntry jarEntry = new JarEntry(file);
                    Objects.requireNonNull(jarOutputStream).putNextEntry(jarEntry);

                    InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(Main.getCtClasses().get(i).toBytecode()));
                    while ((l = inputStream.read(buf, 0, buf.length)) != -1) {
                        jarOutputStream.write(buf, 0, l);
                    }
                    i++;
                    inputStream.close();
                    jarOutputStream.closeEntry();

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error saving class while exporting JAR.");
                } catch (CannotCompileException e) {
                    e.printStackTrace();
                    System.out.println("Compilation error while exporting JAR.");
                }
            }

            for (JarEntry je : Main.getImportedFiles()) {
                try {
                    InputStream inputStream = Main.getJarFile().getInputStream(je);
                    Objects.requireNonNull(jarOutputStream).putNextEntry(je);
                    while ((l = inputStream.read(buf, 0, buf.length)) != -1) {
                        jarOutputStream.write(buf, 0, l);
                    }
                    jarOutputStream.closeEntry();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error saving non-class files.");
                }
            }

            try {
                Objects.requireNonNull(jarOutputStream).close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error closing output stream.");
            }

            System.out.println("JAR file successfully exported.");
        }catch(NullPointerException e){
            System.out.println("JAR file could not been exported.");
        }
    }



}

