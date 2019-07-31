/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.net.deniz;

/**
 *
 * @author yukseldeniz
 */
public class LinkedListNote {
    
    private int size;
    private NoteNode head;
    private NoteNode tail;
    
    public LinkedListNote(){
        head = null;
        tail = null;
        size = 0;
    }
          
    public void addNodeHead( int string, int fret){
        
        if( head == null){
            head = new NoteNode(string, fret);
            tail = head;          
        }
        else{
            NoteNode addNode = new NoteNode(string, fret);
            addNode.next = head;
            head = addNode;
        }   
        size++;
    }
    
    public void add(int string, int fret){
        if( head == null){
            head = new NoteNode(string, fret);
            tail = head;          
        }
        else{
            tail.next = new NoteNode(string, fret);
            tail = tail.next;
        }
        size++;
    }

    @Override
    public String toString() {
        return "LinkedListNote{" + "size=" + size + ", head=" + head + ", tail=" + tail + '}';
    }
    
    public void printList(){
        for( NoteNode cur = head; cur != null; cur = cur.next){
            System.out.print("(" + cur.guitarString + "," + cur.fret + ")  ");
        }
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public NoteNode getHead() {
        return head;
    }

    public void setHead(NoteNode head) {
        this.head = head;
    }
    
    public NoteNode getTail(){
        return tail;
    }
    
    public void setTail(NoteNode tail) {
        this.tail = tail;
    }
}
