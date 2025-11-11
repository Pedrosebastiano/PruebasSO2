package com.mycompany.proyecto2;

import java.awt.Color;

/**
 *
 * @author beacardozo
 */
public class File {
    public String name;
    private int size;
    private int firstBlock; 
    private Color fileColor;
    private int[] addresses;
    public Directory fileDirectory;

    public File(Directory fileDirectory, String name, int size, int firstBlock, Color fileColor) {
        this.name = name;
        this.size = size;
        this.firstBlock = firstBlock;
        this.fileColor = fileColor;
        this.fileDirectory = fileDirectory;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getFirstBlock() {
        return firstBlock;
    }

    public void setName(String name) {
        this.name = name; 
    }

    public void setSize(int size) {
        this.size = size; 
    }

    public void setFirstBlock(int firstBlock) {
        this.firstBlock = firstBlock;
    }
    
    public Color getFileColor() {
        return fileColor;
    }
    
    public Directory getFileDirectory(){
        return fileDirectory;
    }
    
    @Override
    public String toString() {
        return name; 
    }
}
