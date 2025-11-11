package com.mycompany.proyecto2;
import java.awt.Color;
/**
 *
 * @author beacardozo
 */

public class FileSystem {
    public Directory[] directories;
    public File[] files;
    private StorageDisk storageDisk;
    public int fileCount;
    public int dirCount;

    public FileSystem(int totalBlocks, int maxFiles, int maxDirectories) {
        this.directories = new Directory[maxDirectories];
        this.files = new File[maxFiles];
        this.storageDisk = new StorageDisk(totalBlocks);
        this.fileCount = 0;
        this.dirCount = 0;
    }

    public void createFile(Directory directory,String name, int size, int firstBlock, Color color) {
    if (fileCount < files.length && storageDisk.allocateBlocks(name,size, color)) {
        File newFile = new File(directory,name, size, firstBlock,color);
        files[fileCount++] = newFile; // Agregar archivo y aumentar el conteo
    } else {
        System.out.println("Espacio insuficiente o límite de archivos alcanzado.");
    }
}

    public void deleteFile(String name) {
        for (int i = 0; i < fileCount; i++) {
            if (files[i] != null && files[i].getName().equals(name)) {
                storageDisk.freeBlocks(name,files[i].getFirstBlock(), files[i].getSize());
                files[i] = null; // Marcar como eliminado
                System.arraycopy(files, i + 1, files, i, fileCount - i - 1); // Shift left
                files[--fileCount] = null; // Disminuir el contador de archivos
                return;
            }
        }
        System.out.println("Archivo no encontrado");
    }

    public void createDirectory(String name) {
        if (dirCount < directories.length) {
            directories[dirCount++] = new Directory(name);
        } else {
            System.out.println("No se puede crear el directorio. Límite alcanzado.");
        }
    }
    
    public void deleteDirectory(String name) {
        for (int i = 0; i < dirCount; i++) {
            if (directories[i] != null && directories[i].getName().equals(name)) {
                directories[i] = null; 
                System.arraycopy(directories, i + 1, directories, i, dirCount - i - 1);
                directories[--dirCount] = null;
                System.out.println("Directorio " + name + " eliminado con éxito.");
                return; 
            }
        }
        System.out.println("Directorio no encontrado.");
    }  
    
     public void clear() {
        this.files = new File[files.length];
        this.fileCount = 0;

        this.directories = new Directory[directories.length];
        this.dirCount = 0;
    }

}

