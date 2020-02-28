/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.mpeg4;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPParser;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPParser;
import greta.core.util.audio.Audio;
import greta.core.util.audio.AudioEmitter;
import greta.core.util.audio.AudioEmitterImpl;
import greta.core.util.audio.AudioPerformer;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.io.File;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Andre-Marie Pez
 */
public class MPEG4FileReader implements FAPFrameEmitter, BAPFrameEmitter, AudioEmitter{

    private final FAPFrameEmitterImpl FAPEmitters = new FAPFrameEmitterImpl();
    private final BAPFrameEmitterImpl BAPEmitters = new BAPFrameEmitterImpl();
    private final AudioEmitterImpl audioEmitters = new AudioEmitterImpl();
    private final BAPParser BAPParser = new BAPParser();
    private final FAPParser FAPParser = new FAPParser();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        BAPEmitters.addBAPFramePerformer(performer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        BAPEmitters.removeBAPFramePerformer(performer);
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer performer) {
        FAPEmitters.addFAPFramePerformer(performer);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer performer) {
        FAPEmitters.removeFAPFramePerformer(performer);
    }

    @Override
    public void addAudioPerformer(AudioPerformer ap) {
        audioEmitters.addAudioPerformer(ap);
    }

    @Override
    public void removeAudioPerformer(AudioPerformer ap) {
        audioEmitters.removeAudioPerformer(ap);
    }

    public void load(String fileName) {

        String base = hasMPEG4Extention(fileName) ? fileName.substring(0, fileName.length()-4) : fileName;

        List<BAPFrame> bap_animation = BAPParser.readFromFile(base+".bap", true);
        List<FAPFrame> fap_animation = FAPParser.readFromFile(base+".fap", true);
        Audio audio = null;
        try {
            audio = Audio.getAudio(base+".wav");
            audio.setTimeMillis(Timer.getTimeMillis());
        } catch (Exception ex) {}

        ID id = IDProvider.createID(base);
        if (!bap_animation.isEmpty()) {
            BAPEmitters.sendBAPFrames(id, bap_animation);
        }

        if (!fap_animation.isEmpty()) {
            FAPEmitters.sendFAPFrames(id, fap_animation);
        }
        if(audio != null){
            audioEmitters.sendAudio(id, audio);
        }
    }

    private static boolean hasMPEG4Extention(String fileName){
        String fileNameLower = fileName.toLowerCase();
        return fileNameLower.endsWith(".fap") || fileNameLower.endsWith(".bap") || fileNameLower.endsWith(".wav");
    }

    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                return hasMPEG4Extention(pathName.getName());
            }
        };
    }
}
