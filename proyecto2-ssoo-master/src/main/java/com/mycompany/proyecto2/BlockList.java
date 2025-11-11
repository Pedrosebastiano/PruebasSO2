/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyecto2;

/**
 *
 * @author Rodrigo
 */
public class BlockList {
    public String listId;
    private Block head; 
    private int size; 

    public BlockList(String listId) {
        this.listId = listId;
        this.head = null;
        this.size = 0;
    }

    public void addBlock(int id, String content) {
        Block newBlock = new Block(id, content);
        if (head == null) {
            head = newBlock; 
        } else {
            Block current = head;
            while (current.getNext() != null) {
                current = current.getNext(); 
            }
            current.setNext(newBlock); 
        }
        size++;
    }


    public boolean removeBlock(int id) {
        if (head == null) {
            return false; 
        }

        if (head.getFileId() == id) {
            head = head.getNext();
            size--;
            return true;
        }

        Block current = head;
        while (current.getNext() != null) {
            if (current.getNext().getFileId() == id) {
                current.setNext(current.getNext().getNext());
                size--;
                return true;
            }
            current = current.getNext();
        }
        return false; 
    }

    public Block searchBlock(int id) {
        Block current = head;
        while (current != null) {
            if (current.getFileId() == id) {
                return current; 
            }
            current = current.getNext();
        }
        return null; 
    }

    public void printList() {
        Block current = head;
        while (current != null) {
            System.out.println(current);
            current = current.getNext();
        }
    }

    public int getSize() {
        return size;
    }
}
