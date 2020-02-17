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
package greta.core.animation;

/**
 *
 * @author Jing Huang
 */
public class FrameSequencesMixer {

    static public FrameSequence mixTwoSequences(FrameSequence fs0, FrameSequence fs1, int frames) {
        if (fs0 == null) {
            return fs1;
        }
        if (fs1 == null) {
            return fs0;
        }

        if (fs0.getSequence().size()< 2) {
            return fs1;
        }

        if (fs1.getSequence().size()< 2) {
            return fs0;
        }

        double start = Math.min(fs0.getStartTime(), fs1.getStartTime());
        double end = Math.max(fs0.getEndTime(), fs1.getEndTime());
        FrameSequence fs = new FrameSequence(start, end);
        double duration = end - start;
        double perframeTime = 1.0f / frames;
        int nbFrames = (int) (duration * frames);
        for (int i = 0; i < nbFrames; ++i) {
            double timing = perframeTime * i + start;
            Frame f = new Frame();
            Frame f0 = fs0.getFrameByTime(timing);
            Frame f1 = fs1.getFrameByTime(timing);
            if (f0 == null && f1 == null) {
            } else if (f0 == null) {
                f = f1.clone();
            } else if (f1 == null) {
                f = f0.clone();
            } else {
                f.mixAddition(f0, f1);
            }
            fs.add(f);
        }
        return fs;
    }

    static public FrameSequence mixTwoSequences(FrameSequence fs0, FrameSequence fs1, double start, double end, int frames) {
        if (fs0 == null) {
            return fs1;
        }

        if (fs1 == null) {
            return fs0;
        }

        if (fs0.getSequence().size()< 2) {
            return fs1;
        }

        if (fs1.getSequence().size()< 2) {
            return fs0;
        }

        FrameSequence fs = new FrameSequence(start, end);
        double duration = end - start;
        double perframeTime = 1.0f / frames;
        int nbFrames = (int) (duration * frames);
        for (int i = 0; i < nbFrames; ++i) {
            double timing = perframeTime * i + start;
            Frame f = new Frame();
            if (fs0 != null && fs1 != null) {
                Frame f0 = fs0.getFrameByTime(timing);
                Frame f1 = fs1.getFrameByTime(timing);
                if (f0 == null && f1 == null) {
                } else if (f0 == null) {
                    f = f1.clone();
                } else if (f1 == null) {
                    f = f0.clone();
                } else {
                    f.mixAddition(f0, f1);
                }
            } else if (fs0 != null) {
                f.addRotations(fs0.getFrameByTime(timing).getRotations());
            } else {
                f.addRotations(fs1.getFrameByTime(timing).getRotations());
            }
            fs.add(f);
        }
        return fs;
    }

    static public FrameSequence mixThreeSequences(FrameSequence fs0, FrameSequence fs1, FrameSequence fs2, double start, double end, int frames) {
        if (fs0 == null) {
            if (fs1 == null) {
                return fs2;
            } else if (fs2 == null) {
                return fs1;
            } else {
                return mixTwoSequences(fs1, fs2, start, end, frames);
            }
        } else if (fs1 == null) {
            if (fs2 == null) {
                return fs0;
            } else {
                return mixTwoSequences(fs0, fs2, start, end, frames);
            }
        } else if (fs2 == null) {
            if (fs1 == null) {
                return fs0;
            } else {
                return mixTwoSequences(fs0, fs1, start, end, frames);
            }
        }

        FrameSequence fs = new FrameSequence(start, end);
        double duration = end - start;
        double perframeTime = 1.0f / frames;
        int nbFrames = (int) (duration * frames);
        for (int i = 0; i < nbFrames; ++i) {
            double timing = perframeTime * i + start;
            Frame f = new Frame();
            f.mixAddition(fs0.getFrameByTime(timing), fs1.getFrameByTime(timing), fs2.getFrameByTime(timing));
            fs.add(f);
        }
        return fs;
    }
}
