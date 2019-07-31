/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.net.deniz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author yukseldeniz
 */
public class GuitarModel {

    private static Map<String, LinkedListNote> guitarMap;
    private final String E_LOW_START = "E4";
    private final String B_START = "B3";
    private final String G_START = "G3";
    private final String D_START = "D3";
    private final String A_START = "A2";
    private final String E_HIGH_START = "E2";
    private static final int MAX_FRET = 21;
    private static final int STRINGS = 6;
    private static final String[] STRING_HEADS = {"E4", "B3", "G3", "D3", "A2", "E2"};
    private static final String[] solfege = {"C", "Cs", "D", "Ds", "E", "F", "Fs", "G", "Gs", "A", "As", "B"};

    public GuitarModel() {

        guitarMap = initMap();
        fillMap();

    }

    private Map initMap() {

        Properties noteMap = TabTranscriptor.getNoteMap();
        int size = noteMap.size();

        guitarMap = new LinkedHashMap<String, LinkedListNote>();
        String[] noteKeys = TabTranscriptor.getNoteKeys();

        for (int i = 0; i < noteKeys.length; i++) {
            guitarMap.put(noteKeys[i], new LinkedListNote());
        }
        return guitarMap;

    }

    public void printGuitarFretBoard() {

        //List<String> keyList = new ArrayList<String>(guitarMap.keySet());
        //Collections.sort(keyList);
        String[] keyList = guitarMap.keySet().toArray(new String[0]);
        for (int i = 0; i < guitarMap.size(); i++) {
            System.out.println(keyList[i]);
            guitarMap.get(keyList[i]).printList();
            System.out.println();
        }

    }

    public Map getGuitarMap() {
        return guitarMap;
    }

    public int getStringCount() {
        return STRINGS;
    }
    
    public String[] getSolfege(){
        return solfege;
    }

    public void fillMap() {
        int strCount = 0;
        int fretCount = 0;
        String curStringStart;

        while (strCount < STRINGS) {

            curStringStart = STRING_HEADS[strCount];
            System.out.println("TEL: " + curStringStart);
            //int keyPosition = keyList.indexOf(curStringStart);
            //int destination = keyPosition + MAX_FRET;

            int solfejPos = -1;
            int octave = Integer.valueOf(curStringStart.substring(1));
            String noteWithoutOctave = curStringStart.substring(0, curStringStart.length() - 1);
            for (int i = 0; i < solfege.length; i++) {
                if (noteWithoutOctave.equals(solfege[i])) {
                    solfejPos = i;
                }
            }

            if (solfejPos < 0) {
                System.out.println("UYARI");
            } else {
                for (int fret = 0; fret < MAX_FRET; fret++) {
                    String note = solfege[solfejPos] + octave;
                    if (solfejPos == solfege.length - 1) {
                        solfejPos = 0;
                        octave++;
                    } else {
                        solfejPos++;
                    }
                    //System.out.println("Note: " + note);

                    LinkedListNote listPositions = guitarMap.get(note);
                    listPositions.add(strCount, fret);

                }
            }
            strCount++;
        }
    }

   /*
    public static void main(String[] args) {
        GuitarModel model = new GuitarModel();
        model.printGuitarFretBoard();
    }
    */
     
}
