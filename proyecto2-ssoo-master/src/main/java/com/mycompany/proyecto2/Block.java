/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto2;

/**
 *
 * @author Rodrigo
 */
public class Block {
    private int fileId; 
    private Block next; 

    public Block(int id, String content) {
        this.fileId = id;
        this.next = null; 
    }

    // Getters and Setters
    public int getFileId() {
        return fileId;
    }

    public void setIFileId(int fileId) {
        this.fileId = fileId;
    }

    public Block getNext() {
        return next;
    }

    public void setNext(Block next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + fileId +
                '}';
    }
}
