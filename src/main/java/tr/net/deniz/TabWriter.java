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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import GUI.MainFrame;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yukseldeniz
 */
public class TabWriter implements Runnable {

    private BlockingQueue queue;

    public TabWriter(BlockingQueue queue) {
        this.queue = queue;
    }

    public static String adaptFret(int fret) {
        if (fret < 10) {
            return "--" + fret;
        }
        return "-" + fret;
    }

    public String analyze(List<String> notes, GuitarModel guitar, String[] noteKeys, Map<String, Integer> notesCountMap) {
        String[] pickedNotes = notes.toArray(new String[0]);

        for (int i = 0; i < pickedNotes.length; i++) {
            notesCountMap.put(pickedNotes[i], notesCountMap.get(pickedNotes[i]) + 1);
        }

        int max = 0;

        String theNoteFinally = noteKeys[0];

        for (int i = 0; i < notesCountMap.size(); i++) {
            if (max < notesCountMap.get(noteKeys[i])) {
                max = notesCountMap.get(noteKeys[i]);
                theNoteFinally = noteKeys[i];
            }
        }

        System.out.println("MAX : " + max);
        System.out.println("NOTES MAP: " + notesCountMap.toString());
        return theNoteFinally;
    }

//Map<String, LinkedListNote> guitarMap = guitar.getGuitarMap();
    @Override
    public void run() {

        GuitarModel guitar = new GuitarModel();
        Map<String, LinkedListNote> guitarMap = guitar.getGuitarMap();
        String[] solfege = guitar.getSolfege();
        Map<String, Integer> notesCountMap;

        String[] noteKeys = TabTranscriptor.getNoteKeys();

        String[] tabArray = new String[guitar.getStringCount()];

        tabArray[0] = "";
        tabArray[1] = "";
        tabArray[2] = "";
        tabArray[3] = "";
        tabArray[4] = "";
        tabArray[5] = "";

        String[] header = new String[guitar.getStringCount()];
        header[0] = "e --";
        header[1] = "B --";
        header[2] = "G --";
        header[3] = "D --";
        header[4] = "A --";
        header[5] = "E --";

        for (int i = 0; i < tabArray.length; i++) {
            MainFrame.mainFrame.getTabTextArea().append(tabArray[i]);
        }
        
        boolean[] hasNotes = new boolean[guitar.getStringCount()];

        while (MainFrame.longTrainRunning) {

            notesCountMap = new HashMap<String, Integer>();
            for (int i = 0; i < noteKeys.length; i++) {
                notesCountMap.put(noteKeys[i], 0);
            }
            
            for (int i = 0; i < hasNotes.length; i++) {
                hasNotes[i] = false;
            }

            try {
                List<String> notes = (List<String>) queue.take();
                List<String> notes2 = (List<String>) queue.take();
                notes.addAll(notes2);

                if (!notes.isEmpty()) {

                    String theNoteFinally = analyze(notes, guitar, noteKeys, notesCountMap);
                    System.out.println("NOTE FINALLY  " + theNoteFinally);
                    //for (int i = 0; i < pickedNotes.length; i++) {
                    LinkedListNote positions = guitarMap.get(theNoteFinally); //pickedNotes[i]
                    int possiblePositions = positions.getSize();
                    NoteNode node = positions.getHead();

                    if (node == null) {
                        continue;
                    }
                    int str = node.guitarString;
                    int fret = node.fret;

                    if (possiblePositions > 0 && !hasNotes[str]) {  //condition:

                        tabArray[str] += adaptFret(fret);

                        //tabArray[j] = tabArray[j].substring(0, tabArray[j].length() - 1);
                        hasNotes[str] = true;
                        //tabArray[j] += "\n";
                    }

                    //}
                    for (int j = 0; j < hasNotes.length; j++) {
                        if (!hasNotes[j]) {
                            tabArray[j] += "---";
                        }
                    }

                    int count = 0;
                    int columnLimit = MainFrame.mainFrame.getColumnLimit();
                    StringBuilder sb = new StringBuilder(tabArray.length);

                    for (int chunk = 0; chunk < tabArray[0].length(); chunk += columnLimit) {
                        int endIndex = tabArray[0].length() - 1;

                        for (int i = 0; i < tabArray.length; i++) {
                            sb.append(header[i]);
                            //System.out.println("Math min: " + Math.min(endIndex, chunk + columnLimit - 1) + "tabArray[" + i + "] +=" + tabArray[i] + " LENGTH " + tabArray[i].length());
                            sb.append(tabArray[i].substring(chunk, Math.min(endIndex, chunk + columnLimit - 1)));
                            sb.append("\n");
                        }
                        sb.append("\n");
                    }

                    //System.out.println("sb: " + sb.toString());
                    MainFrame.mainFrame.getTabTextArea().setText(sb.toString());

                }
            } catch (InterruptedException ex) {
                Logger.getLogger(TabWriter.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

}
