/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

import vib.core.util.log.Logs;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * this {@code CaptureOutput} uses {@code java.awt.image.BufferedImage} to save
 * the capture into PNG images.
 * @author Andre-Marie Pez
 */
public class AWTImageCaptureOutput implements CaptureOutput {

    private BufferedImage image;
    private int width;
    private int height;
    private long count;
    private String baseFileName = "Capture";

    @Override
    public void begin(int w, int h, long beginTime, String id) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        if(id==null){
            baseFileName = "Capture_" + beginTime + "_";
        }
        else{
            baseFileName = id;
        }
        this.height = h;
        this.width = w;
    }

    /**
     *
     * @param r red component
     * @param g green component
     * @param b blue component
     * @return RGB int value
     */
    private int toIntRGB(byte r, byte g, byte b){
        return ((r & 0xff) << 16)
            | ((g & 0xff) << 8)
            | ((b & 0xff));
    }

    @Override
    public void newFrame(byte[] data, long time) {
        //Copy pixels from data to the BufferedImage
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int buffIndex = (i + j * width) * 3;
                image.setRGB(i, j,
                        toIntRGB(data[buffIndex], data[buffIndex+1], data[buffIndex+2]));
            }
        }

        //save the image
        try {
            javax.imageio.ImageIO.write(image, "png", new File(baseFileName + (count++) + ".png"));
        } catch (Exception ex) {
            Logs.error("Fail to save capture");
        }

//        A better way for this whole method will be:
//
//        java.awt.color.ColorSpace cs = java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB);
//        int[] nBits = {8, 8, 8};
//        java.awt.image.ColorModel colorModel = new java.awt.image.ComponentColorModel(cs, nBits, false, false,
//                                                     java.awt.Transparency.OPAQUE,
//                                                     java.awt.image.DataBuffer.TYPE_BYTE);
//        java.awt.image.MemoryImageSource mis = new java.awt.image.MemoryImageSource(width, height, colorModel, data, 0, data.length);
//        mis.setFullBufferUpdates(true);
//        java.awt.Image img = java.awt.Toolkit.getDefaultToolkit().createImage(mis);
//
//        but how to save the image? ImageIO works with BufferedImage and not Image.
    }

    @Override
    public void end() {/* nothing to do */}

    @Override
    public void newAudioPacket(byte[] data, long time) {/* nothing to do */}
}
