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
package greta.core.util.audio;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 * Abstract class to encapsulate callbacks to update an {@code MixerSource} in
 * its {@code Mixer}.
 *
 * @author Andre-Marie Pez
 */
public abstract class SourceUpdater {

    public static SourceUpdater getSourceUpdater(String param, Mixer m){
        if(param!=null){
            if(param.equalsIgnoreCase("mono")){
                return new UpdateOnlyDistance(m);
            }
            if(param.equalsIgnoreCase("stereo")){
                return new UpdateStereoSounds(m);
            }
            if(param.equalsIgnoreCase("stereo+")){
                return new UpdateStereoSoundsWithEars(m);
            }
            if(param.equalsIgnoreCase("no")){
                return new NoUpdate(m);
            }
        }
        return new NoUpdate(m);
    }

    protected Mixer mixer;

    public SourceUpdater(Mixer mixer) {
        this.mixer = mixer;
    }

    /**
     * The callback function.
     *
     * @param source the {@code MixerSource} to update.
     */
    public void updateSource(MixerSource source) {
        if (source == null || source.getCurrentAudio() == null) {
            return;
        }
        _updateSource(source);
    }

    public abstract void _updateSource(MixerSource source);

    /**
     * Notify that the player has changed.
     */
    public abstract void updateMixer();

    private static double getVolumeFromDistance(double distance, double baseBaseVolume) {
        if (distance == 0) {
            return Double.MAX_VALUE;
        }
        if (distance == Double.POSITIVE_INFINITY || distance == Double.NaN) {
            return 0;
        }
        return baseBaseVolume/distance;
    }


    /**
     * Implementation of {@code SourceUpdater}.<br/> The {@code MixerSource} is
     * not modified.
     */
    public static class NoUpdate extends SourceUpdater {

        public NoUpdate(Mixer mixer) {
            super(mixer);
        }

        @Override
        public void _updateSource(MixerSource source) {
        }

        @Override
        public void updateMixer() {
        }
    }

    /**
     * Implementation of {@code SourceUpdater}.<br/> Only the volume of the
     * {@code MixerSource} is modified using the distance between
     * {@code MixerSource} and the {@code Mixer}.
     */
    public static class UpdateOnlyDistance extends SourceUpdater {

        /**
         * the global position of the Mixer
         */
        private Vec3d playerPosition;

        /**
         * Constructor.
         */
        public UpdateOnlyDistance(Mixer mixer) {
            super(mixer);
            updateMixer();
        }

        @Override
        public void _updateSource(MixerSource source) {
            //Calculate the relative position from the Mixer to the MixerSource
            Vec3d sourcePosition = source.getAudioNode() == null ? playerPosition : source.getAudioNode().getGlobalCoordinates();
            Vec3d relativepos = Vec3d.substraction(sourcePosition, playerPosition);

            //Calculate and set the new volume
            source.setVolume(getVolumeFromDistance(relativepos.length(), source.getCurrentAudio().getBaseVolume()));
        }

        @Override
        public void updateMixer() {
            //get the global position of the Mixer
            playerPosition = mixer.getGlobalCoordinates();
        }
    }

    /**
     * Implementation of {@code SourceUpdater}.<br/> Uses positions and
     * orientations of {@code Mixer} and {@code MixerSource} to update the
     * volume and the balance of the {@code MixerSource}.
     */
    public static class UpdateStereoSounds extends SourceUpdater {

        /**
         * the global position of the Mixer
         */
        private Vec3d playerPosition;
        /**
         * the global orientation of the Mixer
         */
        private Quaternion playerOrientation;

        /**
         * Constructor.
         */
        public UpdateStereoSounds(Mixer mixer) {
            super(mixer);
            updateMixer();
        }

        @Override
        public void _updateSource(MixerSource source) {

            //Calculate the relative position from the Mixer to the MixerSource
            Vec3d sourcePosition = source.getAudioNode() == null ? playerPosition : source.getAudioNode().getGlobalCoordinates();
            Vec3d relativepos = playerOrientation.inverseRotate(Vec3d.substraction(sourcePosition, playerPosition));

            double distance = relativepos.length();

            //Calculate the new volume
            double volume = getVolumeFromDistance(distance, source.getCurrentAudio().getBaseVolume());
            if (relativepos.x() == 0) {
                source.setVolume(volume);
            } else {
                if (relativepos.x() > 0) { // the source is at the right of the mixer
                    source.setRightVolume(volume);
                    source.setLeftVolume(volume * (1 + relativepos.x() / distance));
                } else {
                    if (relativepos.x() < 0) { // the source is at the left of the mixer
                        source.setRightVolume(volume * (1 - relativepos.x() / distance));
                        source.setLeftVolume(volume);
                    }
                }
            }
        }

        @Override
        public void updateMixer() {
            //get the global position and orientation of the Mixer
            playerPosition = mixer.getGlobalCoordinates();
            playerOrientation = mixer.getGlobalOrientation();
        }
    }

    /**
     * Implementation of {@code SourceUpdater}.<br/> Uses positions and
     * orientations of {@code Mixer} and {@code MixerSource} to update the
     * volume and the balance of the {@code MixerSource}.<br/> The difference
     * between this class and {@code UpdateStereoSounds} is that it uses ears
     * simulation.
     */
    public static class UpdateStereoSoundsWithEars extends SourceUpdater {

        /**
         * the global position of the Mixer
         */
        private Vec3d playerPosition = mixer.getGlobalCoordinates();
        /**
         * the global orientation of the Mixer
         */
        private Quaternion playerOrientation = mixer.getGlobalOrientation();
        /**
         * the base distance between one ear and the player
         */
        private float earsDistance = 0.2f;
        /**
         * the relative right ear position from the player
         */
        private Vec3d rightEarPosition;
        /**
         * the relative right ear direction
         */
        private Vec3d rightEarDirection;
        /**
         * the relative left ear position from the player
         */
        private Vec3d leftEarPosition;
        /**
         * the relative left ear direction
         */
        private Vec3d leftEarDirection;

        /**
         * Constructor.
         */
        public UpdateStereoSoundsWithEars(Mixer mixer) {
            super(mixer);
            rightEarDirection = new Vec3d(-1, 0, 0.5f);
            rightEarDirection.normalize();
            leftEarDirection = (new Vec3d(1, 0, 0.5f));
            leftEarDirection.normalize();
            updateMixer();
        }

        @Override
        public void _updateSource(MixerSource source) {

            //get the global position of the MixerSource
            Vec3d sourcePosition = source.getAudioNode() == null ? playerPosition : source.getAudioNode().getGlobalCoordinates();

            double sourceBaseVolume = source.getCurrentAudio().getBaseVolume();
            double distFactor = 4;

            //RIGHT EAR:
            //Calculate the relative position from the right ear to the MixerSource
            Vec3d relativeRpos = Vec3d.substraction(playerOrientation.inverseRotate(Vec3d.substraction(sourcePosition, playerPosition)), rightEarPosition);
            double distanceR = relativeRpos.length();

            //add a distance penalty if the MixerSource is not aligned with the right ear
            double dotR = distanceR == 0 ? 1 : 1 - (relativeRpos.dot3(rightEarDirection) / distanceR - 1) / 2 * (distFactor - 1);
            distanceR *= dotR;

            //Calculate the volume perceived by the rigth ear
            double volumeR = getVolumeFromDistance(distanceR, sourceBaseVolume);


            //LEFT EAR:
            //Calculate the relative position from the left ear to the MixerSource
            Vec3d relativeLpos = Vec3d.substraction(playerOrientation.inverseRotate(Vec3d.substraction(sourcePosition, playerPosition)), leftEarPosition);
            double distanceL = relativeLpos.length();

            //add a distance penalty if the MixerSource is not aligned with the left ear
            double dotL = distanceL == 0 ? 1 : 1 - (relativeLpos.dot3(leftEarDirection) / distanceL - 1) / 2 * (distFactor - 1);
            distanceL *= dotL;

            //Calculate the volume perceived by the left ear
            double volumeL = getVolumeFromDistance(distanceL, sourceBaseVolume);


            //Evaluate and set the new volume and balance of the MixerSource
            source.setRightVolume(volumeR);
            source.setLeftVolume(volumeL);
        }

        @Override
        public void updateMixer() {
            //get the global position, orientation and scaling of the Mixer
            playerPosition = mixer.getGlobalCoordinates();
            playerOrientation = mixer.getGlobalOrientation();
            Vec3d playerScalling = mixer.getGlobalScale();

            //scale ears
            rightEarPosition = Vec3d.multiplicationOfComponents((new Vec3d(-earsDistance, 0, 0)), playerScalling);
            leftEarPosition = Vec3d.multiplicationOfComponents((new Vec3d(earsDistance, 0, 0)), playerScalling);
        }
    }
}
