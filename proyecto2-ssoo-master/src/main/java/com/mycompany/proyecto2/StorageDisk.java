package com.mycompany.proyecto2;
import java.awt.Color;
/**
 *
 * @author beacardozo
 */

public class StorageDisk {
    private int totalBlocks;       
    private String[] blocks;    
    private Color[] blockColors;
    private String[] block;

    public StorageDisk(int totalBlocks) {
        this.totalBlocks = 64; 
        this.blocks = new String[totalBlocks]; 
        this.blockColors = new Color[totalBlocks];
    }

    // Método para obtener el primer bloque libre
    public int getFirstBlock() {
        for (int i = 0; i < totalBlocks; i++) {
            if (blocks[i]==null) {
                return i;            
            }
        }
        return -1; 
    }

    // Método para asignar bloques
    public boolean allocateBlocks(String fileName,int size, Color color) {
        int startBlock = getFirstBlock();
        if (startBlock == -1 || startBlock + size > totalBlocks) {
            return false; // No hay suficiente espacio
        }
        int count = size;
        for (int i =0; i < blocks.length; i++) {
            if(blocks[i]==null){
                blocks[i] = fileName; // Marca los bloques como ocupados
                blockColors[i] = color; // Asigna el color del archivo a los bloques
                count--;
            if (count == 0){
                break;
            }
            }
        }
        for(int i=0;i<blocks.length;i++){
                        System.out.println(blocks[i]);
                    }
                    System.out.println("Nuevos bloques asignados.");
        return true;
    }

    // Método para liberar bloques
    public void freeBlocks(String name, int startBlock, int size) {
    for (int i = startBlock; i < blocks.length; i++) {
        if (blocks[i] == name) {
            blocks[i] = null; // Marca los bloques como libres
            blockColors[i] = Color.GREEN; // Restablece los colores a verde
        }
    }
}

    /**
     * Devuelve el arreglo de colores de los bloques.
     */
    public Color[] getBlockColors() {
        return blockColors;
    }
    
    public void clear() {
        for (int i = 0; i < totalBlocks; i++) {
            blocks[i] = null; // Marca todos los bloques como libres
        }
    }

    // Método para obtener el número de bloques disponibles
    public int getAvailableBlocks() {
        int availableCount = 0;
        for (String block : blocks) {
            if (block==null) availableCount++; 
        }
        return availableCount;
    }
    
    public String[] getBlocks() {
        return blocks; // Devuelve el arreglo de bloques
    }
}
