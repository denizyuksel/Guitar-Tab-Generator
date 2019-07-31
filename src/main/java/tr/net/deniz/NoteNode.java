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

/*
e -> 1
B -> 2
G -> 3
D -> 4
A -> 5
E -> 6
*/
public class NoteNode {
    public NoteNode next;
    public int guitarString;
    public int fret;
    
    public NoteNode(int guitarString, int fret){
        this.guitarString = guitarString;
        this.fret = fret;
        next = null;
    }   
}
