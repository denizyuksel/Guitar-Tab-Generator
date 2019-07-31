/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.net.deniz;

import GUI.MainFrame;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author yukseldeniz
 */
public class TabTranscriptor implements Runnable {

    private PipedInputStream tabTransIn;
    private BlockingQueue queue;
    private static final int SILENT_THRESHOLD = 18;
    public static final float PASS_FRACTION = 0.1f;
    public static final float MAX_BIN_VALUE = 127.0f;

    public TabTranscriptor(PipedInputStream tabTransIn, BlockingQueue queue) {
        this.tabTransIn = tabTransIn;
        this.queue = queue;
    }

    public static int findClosest(int search, int[] array) {

        int start = 0;
        int end = array.length - 1;
        int middle = (start + end) / 2;

        while (start < end) {

            //System.out.println("start: " + start + "end: " + end);
            middle = (start + end) / 2;

            if (search == array[start]) {
                return array[start];
            }

            if (search == array[middle]) {
                return array[middle];
            }

            if (search == array[end]) {
                return array[end];
            }

            if (end - start == 1) {
                int dif1 = Math.abs(search - array[start]);
                int dif2 = Math.abs(search - array[end]);

                if (dif1 > dif2) {
                    return array[end];
                } else {
                    return array[start];
                }
            }

            if (search < array[middle]) {
                end = middle;
            } else if (search > array[middle]) {
                start = middle;
            }
        }
        return array[start];
    }

    public static float getSharpest(float[] notes) {

        float max = 0.0f;
        for (int i = SILENT_THRESHOLD; i < notes.length; i++) {
            if (notes[i] > max) {
                max = notes[i];
            }
        }
        return max;
    }

    public static Properties getNoteMap() {
        Properties noteMap = new Properties();
        try {

            if (MainFrame.useCepstrum) {
                noteMap.load(TabTranscriptor.class.getResourceAsStream("CepstrumNotes.properties"));
            } else {
                noteMap.load(TabTranscriptor.class.getResourceAsStream("AllNotesScale.properties"));
            }
        } catch (IOException ex) {
            Logger.getLogger(TabTranscriptor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return noteMap;
    }

    public static int[] getNoteScale() {
        Properties noteMap = getNoteMap();
        Object[] objScale = noteMap.values().toArray();
        int[] noteScale = new int[objScale.length];

        for (int i = 0; i < objScale.length; i++) {
            noteScale[i] = Integer.parseInt(objScale[i].toString());
        }
        return noteScale;
    }

    public static String[] getNoteKeys() {
        Properties noteMap = getNoteMap();
        Object[] objScale = noteMap.keySet().toArray();
        String[] noteScale = new String[objScale.length];

        for (int i = 0; i < objScale.length; i++) {
            noteScale[i] = objScale[i].toString();
        }
        return noteScale;
    }

    public static String[] getNoteKeysDecreasing() {
        Properties noteMap = getNoteMap();
        Object[] objScale = noteMap.keySet().toArray();
        String[] noteScale = new String[objScale.length];
        for (int i = objScale.length - 1; i >= 0; i--) {
            noteScale[objScale.length - 1 - i] = objScale[i].toString();
        }
        return noteScale;
    }

    public static String adaptFret(int fret) {
        if (fret < 10) {
            return "--" + fret;
        }
        return "-" + fret;
    }

    public static Map getReverseLookup() {

        Properties noteMap = getNoteMap();
        Map reverseLookup = new HashMap();

        for (Map.Entry entry : noteMap.entrySet()) {
            reverseLookup.put(entry.getValue(), entry.getKey());
        }
        return reverseLookup;
    }

    @Override
    public void run() {
        Properties noteMap = getNoteMap();
        int[] noteScale = getNoteScale();
        Map reverseLookup = getReverseLookup();
        Arrays.sort(noteScale);

        System.out.println(reverseLookup.toString());

        int maxView = MainFrame.VIEW_LENGTH;

        if (maxView > VoiceRecorder.PRECISION / 2) {
            maxView = VoiceRecorder.PRECISION / 2;
        }

        if (MainFrame.useCepstrum) {
            maxView = FourierTransformer.CEPSTRUM_LENGTH;
        }

        while (MainFrame.longTrainRunning) {
            try {

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int remaining = (maxView - FourierTransformer.START_FOURIER_POS) * 4;

                int n;

                do {
                    byte[] fourierPiece = new byte[remaining];
                    n = tabTransIn.read(fourierPiece);
                    if (n > 0) {
                        bout.write(fourierPiece, 0, n);
                        remaining -= n;
                    }
                } while (n >= 0 && remaining > 0);
                ByteBuffer buf = ByteBuffer.wrap(bout.toByteArray());

                float[] floatArray = new float[maxView - FourierTransformer.START_FOURIER_POS];

                for (int i = 0; i < floatArray.length; i++) {
                    floatArray[i] = buf.getFloat();
                    //System.out.println("Transcriptor floatArray[i] = " + floatArray[i] + " i = " + i);
                }

                //float highestNote = getSharpest(floatArray);
                float highestNote = MAX_BIN_VALUE;
                //System.out.println("Transcriptor sharpest = " + highestNote);

                //Set<String> notes = new HashSet<String>();
                List<String> notes = new ArrayList<String>();

                boolean found = false;
                float max = 0f;
                float sum = 0f;
                int maxPos = 0;

                for (int i = SILENT_THRESHOLD; i < 37; i++) {
                    if (floatArray[i] > max) {
                        max = floatArray[i];
                        maxPos = i;
                    }
                    sum += floatArray[i];
                }

                float avg = sum / (37 - SILENT_THRESHOLD);

                if (max > avg * 4) {
                    int notePosition = findClosest(maxPos, noteScale);
                    String note = (String) reverseLookup.get(Integer.toString(notePosition));
                    notes.add(note);
                    found = true;
                }

                for (int i = 37; !found && i < floatArray.length; i++) {
                    //System.out.println(floatArray[i]);
                    if (Math.abs(floatArray[i] - highestNote) < highestNote * PASS_FRACTION) {
                        int notePosition = findClosest(i, noteScale);
                        String note = (String) reverseLookup.get(Integer.toString(notePosition));
                        notes.add(note);
                    }
                }

                String caughtNotes = notes.toString();
                //System.out.println("Time: " + System.currentTimeMillis() + " Picked notes: " + caughtNotes);
                System.out.println("Caught notes: " + caughtNotes);
                //System.out.println("Main note: " + mainNote);
                queue.put(notes);

                //MainFrame.mainFrame.getTabTextArea().insert("hebele hübele", 1);
            } catch (IOException ex) {
                Logger.getLogger(TabTranscriptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Throwable t) {
                Logger.getLogger(TabTranscriptor.class.getName()).log(Level.SEVERE, null, t);
            }
        }
    }
}
