/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.tools.ogre.capture.video;

import com.xuggle.ferry.JNIReference;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import greta.auxiliary.player.ogre.capture.CaptureOutput;
import greta.core.util.Constants;
import greta.core.util.audio.Audio;
import greta.core.util.audio.Mixer;
import greta.core.util.log.Logs;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Andre-Marie
 */
public class XuggleVideoCapture implements CaptureOutput {

    public static IPixelFormat.Type GretaPixelType = IPixelFormat.Type.RGB24;
    public static IAudioSamples.Format GretaSamplesFormat = IAudioSamples.Format.FMT_S16;
    private IContainer outContainer;
    private IStreamCoder outVideoStreamCoder;
    private IStreamCoder outAudioStreamCoder;
    private long beginTime;
    private int width;
    private int height;
    protected IVideoResampler mToPictureResampler;
    private IContainerFormat wantedContainerFormat = null;
    private ICodec wantedVideoCodec = null;
    private ICodec wantedAudioCodec = null;
    public String audio_name=null;
    public String video_name=null;

    public XuggleVideoCapture() {
        wantedContainerFormat = IContainerFormat.getInstalledOutputFormat(0);
        for (IContainerFormat f : IContainerFormat.getInstalledOutputFormats()) {
            if (f.getOutputFormatShortName().equalsIgnoreCase("avi")) {
                wantedContainerFormat = f;
            }
        }
        wantedVideoCodec = ICodec.findEncodingCodec(wantedContainerFormat.getOutputDefaultVideoCodec());
        wantedAudioCodec = ICodec.findEncodingCodec(wantedContainerFormat.getOutputDefaultAudioCodec());
    }

    public XuggleVideoCapture(IContainerFormat containerFormat, ICodec video, ICodec audio) {
        wantedContainerFormat = containerFormat;
        wantedVideoCodec = video;
        wantedAudioCodec = audio;
    }

    public XuggleVideoCapture(IContainerFormat containerFormat, ICodec video, ICodec audio, String audio_file) {
        wantedContainerFormat = containerFormat;
        wantedVideoCodec = video;
        wantedAudioCodec = audio;
        audio_name=audio_file;
    }
    
    public void setAudioName(String audio){
        audio_name=audio;
    }
    public synchronized void setWantedFormat(IContainerFormat containerFormat, ICodec video, ICodec audio) {
        wantedContainerFormat = containerFormat;
        wantedVideoCodec = video;
        wantedAudioCodec = audio;
    }
    
    private boolean executeCMD(ProcessBuilder pb){
         pb.redirectErrorStream(true);
         Process p = null;

         try {
            p = pb.start();

         } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("oops");
            p.destroy();
            return false;
        }
        // wait until the process is done
        try {
         p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("woopsy");
            p.destroy();
            return false;
        }
        return true;
        }

    protected IContainer instanciateContainer(String id) {
        IContainer container = IContainer.make(wantedContainerFormat);
        String extention = wantedContainerFormat.getOutputExtensions();
        if (extention != null && !extention.isEmpty()) {
            int comma = extention.indexOf(",");
            extention = "." + (comma < 0 ? extention : extention.substring(0, comma));
        } else {
            extention = "";
        }
        
        String [] in= id.split("\\\\");
        String outFileName = System.getProperty("user.dir")+"\\video\\"+in[in.length-1];
        video_name=outFileName;
        if (outFileName == null) {
            outFileName = "Capture_" + System.currentTimeMillis();
        }
        outFileName += extention;
        int retval = container.open(outFileName, IContainer.Type.WRITE, wantedContainerFormat);
        if (retval < 0) {
            Logs.error("Could not open output file " + outFileName);
            return null;
        }
        Logs.info("Create file ");
        System.out.println("Created file"+outFileName);
         System.out.println(audio_name);
        // add audio ffmpeg
        
        

        return container;
    }

    protected IStreamCoder instanciateVideoStreamCoder(IContainer container, int w, int h) {
        IStream outVideoStream = container.addNewStream(wantedVideoCodec);
        IStreamCoder videoStreamCoder = outVideoStream.getStreamCoder();
        videoStreamCoder.setNumPicturesInGroupOfPictures(50);
        if (wantedVideoCodec.getSupportedVideoPixelFormats().contains(GretaPixelType)) {
            videoStreamCoder.setPixelType(GretaPixelType);
        } else {
            videoStreamCoder.setPixelType(wantedVideoCodec.getSupportedVideoPixelFormat(0));
        }
        videoStreamCoder.setHeight(h);
        videoStreamCoder.setWidth(w);
        videoStreamCoder.setGlobalQuality(0);
        videoStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);

        IRational frameRate = IRational.make(Constants.FRAME_PER_SECOND, 1);
        videoStreamCoder.setFrameRate(frameRate);
        videoStreamCoder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));

        videoStreamCoder.open();

        return videoStreamCoder;
    }

    protected IStreamCoder instanciateAudioStreamCoder(IContainer container) {
        IStream outAudioStream = container.addNewStream(wantedAudioCodec);
        IStreamCoder audioStreamCoder = outAudioStream.getStreamCoder();

//        if (wantedAudioCodec.getSupportedAudioChannelLayouts().contains((long) Audio.GRETA_AUDIO_FORMAT.getChannels())) {
        audioStreamCoder.setChannels(Audio.GRETA_AUDIO_FORMAT.getChannels());
//        } else {
//            audioStreamCoder.setChannels(2);
//        }

//        if (wantedAudioCodec.getSupportedAudioSampleRates().contains((int) Audio.GRETA_AUDIO_FORMAT.getSampleRate())) {
        audioStreamCoder.setSampleRate((int) Audio.GRETA_AUDIO_FORMAT.getSampleRate());
//        } else {
//            audioStreamCoder.setSampleRate(wantedAudioCodec.getSupportedAudioSampleRate(0));
//        }

        if (wantedAudioCodec.getSupportedAudioSampleFormats().contains(GretaSamplesFormat)) {
            audioStreamCoder.setSampleFormat(GretaSamplesFormat);
        } else {
            audioStreamCoder.setSampleFormat(wantedAudioCodec.getSupportedAudioSampleFormat(0));
        }

        long numsamples = Mixer.BUFFER_SIZE / Audio.GRETA_AUDIO_FORMAT.getFrameSize();
//        if (audioStreamCoder.getDefaultAudioFrameSize() < numsamples) {
        audioStreamCoder.setDefaultAudioFrameSize((int) numsamples); //try to set to the Greta's frame size
//        }

        audioStreamCoder.setGlobalQuality(0);
        audioStreamCoder.open();
        return audioStreamCoder;
    }

    protected void writeHeader() {
        outContainer.writeHeader();
    }

    @Override
    public synchronized void begin(int w, int h, long beginTime, String id) {
        this.beginTime = beginTime;
        width = w;
        height = h;

        outContainer = instanciateContainer(id);
        if (outContainer == null) {
            return;
        }
        
        outVideoStreamCoder = instanciateVideoStreamCoder(outContainer, width, height);
        outAudioStreamCoder = instanciateAudioStreamCoder(outContainer);
        writeHeader();

        if (!GretaPixelType.equals(outVideoStreamCoder.getPixelType())) {
            mToPictureResampler = IVideoResampler.make(
                    width, height, outVideoStreamCoder.getPixelType(),
                    width, height, GretaPixelType);
        } else {
            mToPictureResampler = null;
        }

        if (outAudioStreamCoder.getChannels() != Audio.GRETA_AUDIO_FORMAT.getChannels()
                || outAudioStreamCoder.getSampleRate() != Audio.GRETA_AUDIO_FORMAT.getSampleRate()
                || !outAudioStreamCoder.getSampleFormat().equals(GretaSamplesFormat)) {
            audioResampler = IAudioResampler.make(
                    outAudioStreamCoder.getChannels(), Audio.GRETA_AUDIO_FORMAT.getChannels(),
                    outAudioStreamCoder.getSampleRate(), (int) Audio.GRETA_AUDIO_FORMAT.getSampleRate(),
                    outAudioStreamCoder.getSampleFormat(), GretaSamplesFormat);
        } else {
            audioResampler = null;
        }
        
    }
    private IAudioSamples currentSamples;
    private long samplesWriten;
    private IAudioResampler audioResampler;

    @Override
    public void newAudioPacket(byte[] data, long time) {
        if (outContainer != null && outAudioStreamCoder != null && time>beginTime) {
            int read = 0;
            if (currentSamples != null){
                long  wantedTime = (long)((samplesWriten/(Audio.GRETA_AUDIO_FORMAT.getSampleRate()/1000f))+currentSamples.getTimeStamp()/1000+beginTime);

                if (time  > wantedTime) {
                    int buffsize = (int)((time-wantedTime) * (Audio.GRETA_AUDIO_FORMAT.getFrameRate()/1000.0)*Audio.GRETA_AUDIO_FORMAT.getFrameSize());
//if(buffsize%512 != 0 ){
//    System.err.println("nombre de milliseconds flottant !! "+buffsize);
//    System.err.println("(long)((("+samplesWriten+"/"+Audio.GRETA_AUDIO_FORMAT.getSampleRate()+")*1000000f)+"+currentSamples.getTimeStamp()+"+"+beginTime+"*1000)");
//}
                    newAudioPacket(new byte[buffsize], wantedTime);
                }
                else{
                    if(time < wantedTime){
                        //skip lated bytes
                        read = (int)((wantedTime - time) * (Audio.GRETA_AUDIO_FORMAT.getFrameRate()/1000.0));
//if(read%16!=0){
//    System.err.println(read+"  = (int)(("+wantedTime+" - "+time+") * ("+Audio.GRETA_AUDIO_FORMAT.getFrameRate()/1000+"))");
//    System.err.println("(long)((("+samplesWriten+"/"+Audio.GRETA_AUDIO_FORMAT.getSampleRate()+")*1000000f)+"+currentSamples.getTimeStamp()+"+"+beginTime+"*1000)");
//}
                    }
                }
            }

            long numsamples = data.length / Audio.GRETA_AUDIO_FORMAT.getFrameSize();

            while (read < numsamples) {
                if (currentSamples == null) {
                    currentSamples = IAudioSamples.make(
                            outAudioStreamCoder.getAudioFrameSize(),
                            Audio.GRETA_AUDIO_FORMAT.getChannels(),
                            GretaSamplesFormat);
                    samplesWriten = 0;
//if(read%16 != 0){
//    System.err.println(read);
//}
                    long timeStamp = (time - beginTime) * 1000 + (long) (read * (1000000 / Audio.GRETA_AUDIO_FORMAT.getFrameRate())); // convert to microseconds
//if(timeStamp%1000 != 0){
//    System.err.println("timestamp : nombre de milliseconds flottant !!");
//}
                    currentSamples.setTimeStamp(timeStamp);
                }
                long samplesToRead = Math.min(outAudioStreamCoder.getAudioFrameSize() - samplesWriten, numsamples - read);

                currentSamples.put(data,
                        (int) (read * Audio.GRETA_AUDIO_FORMAT.getFrameSize()),
                        (int) (samplesWriten * Audio.GRETA_AUDIO_FORMAT.getFrameSize()),
                        (int) (samplesToRead * Audio.GRETA_AUDIO_FORMAT.getFrameSize()));

                samplesWriten += samplesToRead;
                if (samplesWriten == outAudioStreamCoder.getAudioFrameSize()) {
                    currentSamples.setComplete(
                            true, outAudioStreamCoder.getAudioFrameSize(),
                            (int) Audio.GRETA_AUDIO_FORMAT.getFrameRate(),
                            Audio.GRETA_AUDIO_FORMAT.getChannels(),
                            GretaSamplesFormat, currentSamples.getTimeStamp());

                    //resample as needed
                    IAudioSamples reSampled;
                    if (audioResampler != null) {
                        reSampled = IAudioSamples.make(
                                currentSamples.getNumSamples(),
                                audioResampler.getOutputChannels(),
                                audioResampler.getOutputFormat());
                        audioResampler.resample(
                                reSampled,
                                currentSamples,
                                outAudioStreamCoder.getAudioFrameSize());
                    } else {
                        reSampled = currentSamples;
                    }

                    IPacket packet = IPacket.make();
                    outAudioStreamCoder.encodeAudio(packet, reSampled, 0);

                    if (packet.isComplete()) {
                        synchronized (outContainer) {
                            if (outContainer != null) {
                                outContainer.writePacket(packet, true);
                            }
                        }
                    }
                    else {
                        System.err.println("incomplete audio " + packet.getTimeStamp() + " " + packet.getDuration() +  " " + packet.getSize());
                    }
                    currentSamples = null;
                }
                read += samplesToRead;
            }
        }
    }

    @Override
    public void newFrame(byte[] data, long time) {
        if (outContainer != null && outVideoStreamCoder != null) {
            IPacket packet = IPacket.make();

            long timeStamp = (time - beginTime) * 1000; // convert to microseconds

            IVideoPicture outFrame = toPicture(data, timeStamp);
            outFrame.setQuality(0);
            outVideoStreamCoder.encodeVideo(packet, outFrame, 0);
            if (packet.isComplete()) {
                synchronized (outContainer) {
                    outContainer.writePacket(packet, true);
                }
            }
            else {
                System.err.println("incomplete video " + packet.getTimeStamp() + " " + packet.getDuration() +  " " + packet.getSize());
            }
        }
    }

    @Override
    public void end() {
        System.out.println("greta.tools.ogre.capture.video.XuggleVideoCapture.end()");  
        if (outContainer != null) {
            synchronized (outContainer) {
                outContainer.close();
                outContainer = null;
            }
            currentSamples = null;
        }
        outVideoStreamCoder = null;
        outAudioStreamCoder = null;
        System.gc();
        
        String outputFile_test=System.getProperty("user.dir")+"\\video\\output_"+System.currentTimeMillis()+".mov";
        if(audio_name!= null){
            System.out.println("TEST "+audio_name+"  "+video_name+".avi"+"  "+outputFile_test+" "+System.getProperty("user.dir"));
            String[] exeCmd = new String[]{"ffmpeg", "-i", audio_name, "-i", video_name+".avi" ,"-acodec", "copy", "-vcodec", "copy",outputFile_test};
            ProcessBuilder pb = new ProcessBuilder(exeCmd);
            boolean exeCmdStatus=executeCMD(pb);
            System.out.println("AUDIO AND VIDEO MERGED "+exeCmdStatus);
            File f = new File(outputFile_test);
            if(!f.isFile()){
                 String inputVideoFilePath = video_name+".avi";
        String inputAudioFilePath = audio_name;
        String outputVideoFilePath = outputFile_test.replace(".mov", ".mp4");
        
        IMediaWriter mWriter = ToolFactory.makeWriter(outputVideoFilePath);

    IContainer containerVideo = IContainer.make();
    IContainer containerAudio = IContainer.make();

    // check files are readable
    if (containerVideo.open(inputVideoFilePath, IContainer.Type.READ, null) < 0)
        throw new IllegalArgumentException("Cant find " + inputVideoFilePath);
    if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0)
        throw new IllegalArgumentException("Cant find " + inputAudioFilePath);

    // read video file and create stream
    IStreamCoder coderVideo = containerVideo.getStream(0).getStreamCoder();
    if (coderVideo.open(null, null) < 0)
        throw new RuntimeException("Cant open video coder");
    IPacket packetvideo = IPacket.make();
    int width = coderVideo.getWidth();
    int height = coderVideo.getHeight();

    // read audio file and create stream
    IStreamCoder coderAudio = containerAudio.getStream(0).getStreamCoder();
    if (coderAudio.open(null, null) < 0)
        throw new RuntimeException("Cant open audio coder");
    IPacket packetaudio = IPacket.make();

    mWriter.addAudioStream(1, 0, coderAudio.getChannels(), coderAudio.getSampleRate());
    mWriter.addVideoStream(0, 0, width, height);

    while (containerVideo.readNextPacket(packetvideo) >= 0) {

        containerAudio.readNextPacket(packetaudio);

        // video packet
        IVideoPicture picture = IVideoPicture.make(coderVideo.getPixelType(), width, height);
        coderVideo.decodeVideo(picture, packetvideo, 0);
        if (picture.isComplete()) 
            mWriter.encodeVideo(0, picture);

        // audio packet 
        IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
        coderAudio.decodeAudio(samples, packetaudio, 0);
        if (samples.isComplete()) 
            mWriter.encodeAudio(1, samples);

    }

    coderAudio.close();
    coderVideo.close();
    containerAudio.close();
    containerVideo.close();
    mWriter.close();
            }
        }
        System.out.println("FINISHED");
    }

    private IVideoPicture toPicture(byte[] imageBytes, long timestamp) {
        //copied and simplified from com.xuggle.xuggler.video.BgrConverter

        // create the video picture and get it's underling buffer
        final AtomicReference<JNIReference> ref =
                new AtomicReference<JNIReference>(null);
        IVideoPicture resamplePicture = null;
        try {
            IVideoPicture picture = IVideoPicture.make(GretaPixelType, width, height);

            picture.getByteBuffer(ref).put(imageBytes);

            picture.setComplete(true, GretaPixelType, width, height, timestamp);

            // resample as needed
            if (mToPictureResampler != null) {
                picture = resample(picture, mToPictureResampler);
            }
            return picture;
        } finally {
            if (resamplePicture != null) {
                resamplePicture.delete();
            }
            if (ref.get() != null) {
                ref.get().delete();
            }
        }
    }

    private static IVideoPicture resample(IVideoPicture picture1, IVideoResampler resampler) {
        //copied from com.xuggle.xuggler.video.AConverter

        // create new picture object
        IVideoPicture picture2 = IVideoPicture.make(
                resampler.getOutputPixelFormat(),
                resampler.getOutputWidth(),
                resampler.getOutputHeight());

        // resample
        if (resampler.resample(picture2, picture1) < 0) {
            throw new RuntimeException(
                    "could not resample from " + resampler.getInputPixelFormat()
                    + " to " + resampler.getOutputPixelFormat()
                    + " for picture of type " + picture1.getPixelType());
        }

        // test that it worked
        if (picture2.getPixelType() != resampler.getOutputPixelFormat()
                || !picture2.isComplete()) {
            throw new RuntimeException(
                    "did not resample from " + resampler.getInputPixelFormat()
                    + " to " + resampler.getOutputPixelFormat()
                    + " for picture of type " + picture1.getPixelType());
        }

        // return the resample picture
        return picture2;
    }
}
