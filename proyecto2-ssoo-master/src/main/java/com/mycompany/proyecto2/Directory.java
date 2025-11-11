package com.mycompany.proyecto2;

/**
 *
 * @author beacardozo
 */
public class Directory {
    private String name;
    public Directory[] subdirectories; 
    public File[] files;
    public int subDirCount; 
    public int fileCount; 

    public Directory(String name) {
        this.name = name;
        this.subdirectories = new Directory[10]; // Limite de 10 subdirectorios
        this.files = new File[10]; // Limite de 10 archivos
        this.subDirCount = 0;
        this.fileCount = 0;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void addFile(File file) {
        if (fileCount < files.length) {
            files[fileCount++] = file; // Añade el archivo
        } else {
            System.out.println("No se pueden agregar más archivos a este directorio.");
        }
    }

    public void addDirectory(Directory directory) {
        if (subDirCount < subdirectories.length) {
            subdirectories[subDirCount++] = directory; // Añade el subdirectorio
        } else {
            System.out.println("No se pueden agregar más subdirectorios a este directorio.");
        }
    }

    public Directory[] getSubdirectories() {
        return subdirectories;
    }

    public File[] getFiles() {
        return files;
    }

    public int getSubDirCount() {
        return subDirCount;
    }

    public int getFileCount() {
        return fileCount;
    }
    
    @Override
    public String toString() {
        return name; 
    }
}
