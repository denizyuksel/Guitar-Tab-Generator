/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.jtransforms.fft.FloatFFT_1D;

/**
 *
 * @author yukseldeniz
 */
public class TestMic {

    public void startMic() {

        final int PRECISION = 4096;
        final int VIEW_LENGTH = 300;
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
        TargetDataLine microphone;
        AudioInputStream audioInputStream;
        SourceDataLine sourceDataLine;
        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numBytesRead;
            int CHUNK_SIZE = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            int bytesRead = 0;
            long startRecord = System.currentTimeMillis();
            try {
                while (bytesRead < 100000) { // Just so I can test if recording
                    // my mic works...
                    numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
                    bytesRead = bytesRead + numBytesRead;
                    //System.out.println(bytesRead);
                    out.write(data, 0, numBytesRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            long endRecord = System.currentTimeMillis();
            byte audioData[] = out.toByteArray();
            // Get an input stream on the byte array
            // containing the data
            
            //TEST FOURIER
            
            ByteBuffer byteBuf = ByteBuffer.wrap(audioData);
            
            float[] floatArray = new float[audioData.length / 2];
            
            short min = 0;
            short max = 0;
            
            for( int i = 0; i < audioData.length / 2; i++){
                short s = byteBuf.getShort();
                
                floatArray[i] = s;
                if( s < min)
                    min = s;
                
                if( s >= max)
                    max = s;
                
            }
            byteBuf.rewind();
            
            for( int i = 0; i < audioData.length / 2; i++){
                
                floatArray[i] += -min;
                floatArray[i] /= (max - min);
            }
            
            for( int i = 0; i < VIEW_LENGTH; i++){
                System.out.println(floatArray[i]);
            }
            
            //float[] floatArray = byteBuf.getFloat();
            System.out.println("FOURIER");
            
            long start, end;
            
            start = System.currentTimeMillis();
            FloatFFT_1D fft = new FloatFFT_1D(PRECISION);           
            fft.realForward(floatArray);
            
            //take absolute value...
            float[] absFourier = new float[audioData.length / 2];
            
            for(int i = 0; i < VIEW_LENGTH; i++){
                absFourier[i] = Math.abs(floatArray[i]);
            }
            
            
            end = System.currentTimeMillis();
            
            for( int i = 0; i < VIEW_LENGTH; i++){
                System.out.println(absFourier[i]);
            }
            
            System.out.println("TIME ELAPSED WITH n = " + PRECISION + ": " + (end - start) + "miliseconds.");
            System.out.println("RECORDED " + (endRecord - startRecord) + " miliseconds.");           
            
            //System.out.println(Arrays.toString(Arrays.copyOfRange(floatArray, 0, 300)));
            
            
            /*
            ByteBuffer bb = ByteBuffer.allocate(10);
            bb.put((byte) 0);   // 256'lar basamağı.
            bb.put((byte) 300); //birler basamağı. 256'da overflow atar. 
            bb.put((byte) 0);
            bb.put((byte) 0);
            bb.rewind();
           
            
            for( int i = 0; i < 5; i++){
                System.out.println("byte at loc " + i + ": " + bb.getShort());
            }
            */
            
            // TURN ON SPEAKERS...
            /*
            InputStream byteArrayInputStream = new ByteArrayInputStream(
                    audioData);
            audioInputStream = new AudioInputStream(byteArrayInputStream, format, audioData.length / format.getFrameSize());
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(format);
            sourceDataLine.start();
            int cnt = 0;
            byte tempBuffer[] = new byte[10000];
            try {
                while ((cnt = audioInputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (cnt > 0) {
                        // Write data to the internal buffer of
                        // the data line where it will be
                        // delivered to the speaker.
                        sourceDataLine.write(tempBuffer, 0, cnt);
                    }// end if
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Block and wait for internal buffer of the
            // data line to empty.
            sourceDataLine.drain();
            sourceDataLine.close();
            microphone.close();
            */
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /*
    AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
   

    public void startMic(JToggleButton btn) {
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
        DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
        //needed for AudioSystem to access.
        

        try {
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetLine.open(format);
            targetLine.start();

            SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceLine.open(format);
            sourceLine.start();

            int numBytesRead;
            byte[] targetData = new byte[targetLine.getBufferSize()];
            
            System.out.println(btn.isSelected());

            while (btn.isSelected()) {
                numBytesRead = targetLine.read(targetData, 0, targetData.length);

                if (numBytesRead == -1) {
                    break;
                }
                
                if(btn.isSelected())
                    btn.setSelected(false);

                sourceLine.write(targetData, 0, numBytesRead);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
     */
}
