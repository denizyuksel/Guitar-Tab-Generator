/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.net.deniz;

import GUI.MainFrame;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author yukseldeniz
 */
public class VoiceRecorder implements Runnable {

    private PipedOutputStream fourierOut;
    TargetDataLine microphone;

    public final static int PRECISION = 16384;
    public final static int SAMPLE_RATE = 44100;
    public final static int SAMPLE_SIZE_BITS = 16;
    public final static int ONE_MINUTE_MILIS = 60000;
    public final static int ONE_SECOND_MILIS = 1000;
    
    //public static int VIEW_LENGTH = 1000;
    

    public VoiceRecorder(PipedOutputStream fourierOut) {
        this.fourierOut = fourierOut;
    }

    public static int calculateBytesInSec() {
        int bpm = MainFrame.mainFrame.getBpm();
        int result = 2 * (SAMPLE_RATE / ONE_SECOND_MILIS) * (ONE_MINUTE_MILIS / (bpm * 4)); // *4 for sixteen beat notes.
        return result;
    }

    public static int calculateBufferSize() {
        int bpm = MainFrame.mainFrame.getBpm();
        int result = 2 * (SAMPLE_RATE / ONE_SECOND_MILIS) * (ONE_MINUTE_MILIS / (bpm * 4)); // *4 for sixteen beat notes.
        return Math.max(result, PRECISION * 2);
    }

    @Override
    public void run() {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, 1, true, true);
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            
            int numBytesRead;
            int CHUNK_SIZE = 4096;
            int bytesInSec = calculateBytesInSec();
            byte[] data = new byte[CHUNK_SIZE];
            int bytesRead = 0;

            while (MainFrame.longTrainRunning) {
                long start = System.currentTimeMillis();

                long micstart = start;
//                System.out.println("mic level: " + microphone.getLevel());
                numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                long micend = System.currentTimeMillis();
                bytesRead += numBytesRead;

//                for( int i = 0; i < data.length; i++)
//                    System.out.println("data: " + data[i] + "numbytesread: " + numBytesRead);
                
                try {
                    fourierOut.write(data, 0, numBytesRead);
                    fourierOut.flush();
                    //feed the data to sonograph.
                } catch (IOException ex) {
                    Logger.getLogger(VoiceRecorder.class.getName()).log(Level.SEVERE, null, ex);
                }
                long end = System.currentTimeMillis();
                //System.out.println("Recorder while loop time: " + (end - start));

            }
        } catch (LineUnavailableException ex) {
            Logger.getLogger(VoiceRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
        microphone.stop();
        microphone.close();
    }

}
