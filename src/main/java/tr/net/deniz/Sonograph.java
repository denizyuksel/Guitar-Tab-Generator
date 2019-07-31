/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tr.net.deniz;

import GUI.MainFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import static tr.net.deniz.TabTranscriptor.findClosest;

/**
 *
 * @author yukseldeniz
 */
public class Sonograph implements Runnable {

    private PipedInputStream sonographIn;

    public Sonograph(PipedInputStream sonographIn) {
        this.sonographIn = sonographIn;
    }
//
//    public static BufferedImage scale(BufferedImage sbi, int dWidth, int dHeight) {
//        BufferedImage dbi = null;
//        int imageType = sbi.getType();
//
//        if (sbi != null) {
//            dbi = new BufferedImage(dWidth, dHeight, imageType);
//            System.out.println("dbi.getWidth() = " + dbi.getWidth());
//            System.out.println("dbi.getHeight() = " + dbi.getHeight());
//            Graphics2D g = dbi.createGraphics();
//
//            g.drawImage(dbi, 0, 0, dWidth, dHeight, null);
//
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(Sonograph.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            g.dispose();
//        }
//        return dbi;
//    }

    @Override
    public void run() {
        System.out.println("sonograph started");

        int column = 0;
        int row = 0;
        int maxView = (MainFrame.useCepstrum ? FourierTransformer.CEPSTRUM_LENGTH : VoiceRecorder.PRECISION / 2); //MainFrame.VIEW_LENGTH;
//
//        if (maxView > VoiceRecorder.PRECISION / 2) {
//            maxView = VoiceRecorder.PRECISION / 2;
//        }
//
//        if (MainFrame.useCepstrum) {
//            maxView = FourierTransformer.CEPSTRUM_LENGTH;
//        }

        BufferedImage image = null; //new BufferedImage(MainFrame.mainFrame.getVisualWidth(), MainFrame.mainFrame.getVisualHeight(), BufferedImage.TYPE_INT_RGB);
        if (MainFrame.mainFrame.isVertical()) {
            image = new BufferedImage(maxView,
                    MainFrame.mainFrame.getVisualHeight(),
                    BufferedImage.TYPE_INT_RGB);
        } else {
            image = new BufferedImage(MainFrame.mainFrame.getVisualWidth(),
                    maxView,
                    BufferedImage.TYPE_INT_RGB);
        }

        System.out.println("MainFrame.mainFrame.getVisualWidth() = " + MainFrame.mainFrame.getVisualWidth());
        System.out.println("MainFrame.mainFrame.getVisualHeight() = " + MainFrame.mainFrame.getVisualHeight());
        System.out.println("image.getWidth() = " + image.getWidth());
        System.out.println("image.getHeight() = " + image.getHeight());

        while (MainFrame.longTrainRunning) {
            try {

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                int remaining = (maxView - FourierTransformer.START_FOURIER_POS) * 4;

                int n;

                do {
                    byte[] fourierPiece = new byte[remaining];
                    n = sonographIn.read(fourierPiece);
                    if (n > 0) {
                        bout.write(fourierPiece, 0, n);
                        remaining -= n;
                    }
                } while (n >= 0 && remaining > 0);
                ByteBuffer buf = ByteBuffer.wrap(bout.toByteArray());

                float[] floatArray = new float[maxView - FourierTransformer.START_FOURIER_POS];

                for (int i = 0; i < floatArray.length; i++) {
                    floatArray[i] = buf.getFloat();
                }

                int loopLength = 0;
                if (!MainFrame.mainFrame.isVertical()) {
                    if (image.getHeight() <= floatArray.length) {
                        loopLength = image.getHeight();
                    } else {
                        loopLength = floatArray.length;
                    }

                    //float sharpest = TabTranscriptor.getSharpest(floatArray);
                    float sharpest = TabTranscriptor.MAX_BIN_VALUE;
                    //System.out.println("Sonograph sharpest = " + sharpest);

                    for (int i = 0; i < loopLength; i++) {
                        //System.out.println("Sonograph floatArray[i] = " + floatArray[i] + " i = " + i);  

                        float x = floatArray[i];
                        if (x > 127 || i < 15) {
                            x = 0;
                        }

                        int r = 255 - (int) (2 * x);
                        Color color = new Color(r, r, r);
                        //System.out.println("R: " + r + " X. " + i + " : " + x);

                        image.setRGB(column, image.getHeight() - 1 - i, color.getRGB());
                    }

                    column = (column + 1) % image.getWidth();

                    ImageIcon imageIcon = new ImageIcon(image);
                    MainFrame.mainFrame.setSonograph(imageIcon);
                } else {
                    if (image.getWidth() <= floatArray.length) {
                        loopLength = image.getWidth();
                    } else {
                        loopLength = floatArray.length;
                    }

                    float sharpest = TabTranscriptor.getSharpest(floatArray);

                    for (int i = 0; i < loopLength; i++) {

                        float x = floatArray[i];
                        if (x > 127 || i < 15) {
                            x = 0;
                        }

                        int r = 255 - (int) (2 * x);
                        Color color = new Color(r, r, r);

                        image.setRGB(i, row, color.getRGB());

                        //System.out.println("R: " + r + " X. " + i + " : " + x);
                    }

                    row = (row + 1) % image.getHeight();

                    ImageIcon imageIcon = new ImageIcon(image);
                    MainFrame.mainFrame.setSonograph(imageIcon);

                }

            } catch (Throwable ex) {
                Logger.getLogger(Sonograph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
