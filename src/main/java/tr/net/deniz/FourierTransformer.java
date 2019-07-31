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
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtransforms.fft.FloatFFT_1D;

/**
 *
 * @author yukseldeniz
 */
public class FourierTransformer implements Runnable {

    private PipedInputStream fourierIn;
    private PipedOutputStream tabTransOut;
    private PipedOutputStream sonographOut;
    public static final int START_FOURIER_POS = 4;
    public static final int CEPSTRUM_LENGTH = 4096;

    public FourierTransformer(PipedInputStream fourierIn, PipedOutputStream sonographOut, PipedOutputStream tabTransOut) {
        this.fourierIn = fourierIn;
        this.tabTransOut = tabTransOut;
        this.sonographOut = sonographOut;
    }

    @Override
    public void run() {
        System.out.println("fourier started");
        int bufferSize = VoiceRecorder.calculateBufferSize();
        int bytesInSec = VoiceRecorder.calculateBytesInSec();
        int remaining = bytesInSec;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        while (MainFrame.longTrainRunning) {
            Map timeMap = new HashMap();
            try {
                long start = System.currentTimeMillis();
                long loopstart = start;
                int n;

                do {
                    byte[] fourierPiece = new byte[remaining];
                    n = fourierIn.read(fourierPiece);
//                    System.out.println("n: " + n);
                    if (n > 0) {
                        bout.write(fourierPiece, 0, n);
                        remaining -= n;
                    }
                } while (n >= 0 && remaining > 0);

                //System.out.println("Read buffer size: " + bout.size());
                byte[] readVoice = bout.toByteArray();

//                for( int i = 0; i < readVoice.length; i++)
//                    System.out.println("Read voice: " + readVoice[i]);
//                
                byte[] audioData = new byte[bufferSize];

//                for (int i = 0; i < bufferSize; i = i + readVoice.length) {
//
//                    if (i + readVoice.length > bufferSize) {
//                        System.arraycopy(readVoice, 0, audioData, i, bufferSize - i);
//                    } else {
//                        System.arraycopy(readVoice, 0, audioData, i, readVoice.length);
//                    }
//                }

                if( bufferSize > readVoice.length * 2){
                    System.arraycopy(readVoice, 0, audioData, audioData.length / 2, readVoice.length);
                    int halfAudioLength = audioData.length / 2;
                    
                    for( int i = 0; i < readVoice.length; i++){
                        audioData[halfAudioLength - readVoice.length + i] = audioData[ halfAudioLength + i];
                    }
                    
                }
                else{                                  
                System.arraycopy(readVoice, 0, audioData, 0, readVoice.length);
                }
                

                ByteBuffer byteBuf = ByteBuffer.wrap(audioData);

                long end = System.currentTimeMillis();
                timeMap.put("Read buffer", (end - start));
                start = end;

                float[] floatArray = new float[audioData.length / 2];

                short min = 0;
                short max = 0;

                for (int i = 0; i < audioData.length / 2; i++) {
                    short s = byteBuf.getShort();

                    floatArray[i] = s;
                    if (s < min) {
                        min = s;
                    }

                    if (s >= max) {
                        max = s;
                    }
                }

                // normalize the wave.
                /*
                for (int i = 0; i < audioData.length / 2; i++) {

                    floatArray[i] += -min;
                    floatArray[i] /= (max - min);

                    
                    if (floatArray[i] < 0.1f) {
                        floatArray[i] = 0.0f;
                    }
                    
                }
                */

                end = System.currentTimeMillis();
                timeMap.put("Normalizaiton: ", (end - start));
                start = end;

                //fouirer...
                FloatFFT_1D fft = new FloatFFT_1D(VoiceRecorder.PRECISION);
                fft.realForward(floatArray);

                end = System.currentTimeMillis();
                timeMap.put("Fourier: ", (end - start));
                start = end;

                int maxView = MainFrame.VIEW_LENGTH;

                if (maxView > VoiceRecorder.PRECISION / 2) {
                    maxView = VoiceRecorder.PRECISION / 2;
                }

                if (MainFrame.useCepstrum) {
                    maxView = FourierTransformer.CEPSTRUM_LENGTH;
                }

                //take absolute value...
                float[] absFourier = new float[floatArray.length];

                float maxFourier = 0f;
                int maxIndex = 0;
                for (int i = 0; i < absFourier.length / 2; i++) {

                    absFourier[i] = (floatArray[2 * i] * floatArray[2 * i] + floatArray[2 * i + 1] * floatArray[2 * i + 1]);

                    if (MainFrame.useCepstrum) {
                        absFourier[i] = (float) Math.log10(absFourier[i]);
                    } else {
                        absFourier[i] = (float) Math.sqrt(absFourier[i]);
                    }
                    if (i > START_FOURIER_POS - 1 && absFourier[i] > maxFourier) {
                        maxFourier = absFourier[i];
                    }
                }

                if (MainFrame.useCepstrum) {
                    maxFourier = 0f;

                    FloatFFT_1D fftCepstrum = new FloatFFT_1D(CEPSTRUM_LENGTH * 2);

                    float[] cepstrumData = new float[CEPSTRUM_LENGTH * 4];

                    for (int i = 0; i < cepstrumData.length; i = i + absFourier.length) {
                        if (i + absFourier.length > cepstrumData.length) {
                            System.arraycopy(absFourier, 0, cepstrumData, i, cepstrumData.length - i);
                        } else {
                            System.arraycopy(absFourier, 0, cepstrumData, i, absFourier.length);
                        }
                    }

                    fftCepstrum.realForward(cepstrumData);

                    float[] absCepstrum = new float[CEPSTRUM_LENGTH];

                    for (int i = 0; i < absCepstrum.length; i++) {
                        absCepstrum[i] = (cepstrumData[2 * i] * cepstrumData[2 * i] + cepstrumData[2 * i + 1] * cepstrumData[2 * i + 1]);
                        //absCepstrum[i] = (float) Math.sqrt(absCepstrum[i]);

                    }
                    
                    absFourier = new float[absCepstrum.length];
                    for (int i = 0; i < absCepstrum.length; i++) {
                        absFourier[i] = absCepstrum[i];

                        if (i < CEPSTRUM_LENGTH / 2 && i > 14 && absCepstrum[i] > maxFourier) {
                            maxFourier = absCepstrum[i];
                            maxIndex = i;
                        }
                    }
                }

                System.out.println("Max fourirei " + maxFourier + " Max index: " + maxIndex);
                //normalize fourier or cepstrum transformed wave.               
                float factor = 127.0f / maxFourier;

                int byteArrayLength = (maxView - START_FOURIER_POS) * 4;

                byte[] sendWave = new byte[byteArrayLength];
                ByteBuffer buf = ByteBuffer.wrap(sendWave);

                for (int i = START_FOURIER_POS; i < maxView; i++) {
                    buf.putFloat(absFourier[i] * factor);
                    //System.out.println("Absfourier[i] = " + absFourier[i] + " i: " + i);
                }

                end = System.currentTimeMillis();
                timeMap.put("Normalize Fourier", (end - start));
                start = end;

                sonographOut.write(sendWave);
                end = System.currentTimeMillis();
                timeMap.put("sonograph write: ", (end - start));
                start = end;

                tabTransOut.write(sendWave);
                end = System.currentTimeMillis();
                timeMap.put("tab write", (end - start));
                start = end;

                bout = new ByteArrayOutputStream();
                remaining = bytesInSec / 2;
                bout.write(audioData, remaining, remaining);
                end = System.currentTimeMillis();
                timeMap.put("byte array output: ", (end - start));
                start = end;

                timeMap.put("loop time : ", (end - loopstart));

                System.out.println("Fourier time: " + System.currentTimeMillis() + " durations: " + timeMap.toString());

            } catch (IOException ex) {
                Logger.getLogger(FourierTransformer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
