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

import greta.core.util.math.Functions;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class AudioPanel extends JPanel implements NormalizedAudioOutput{

    protected String title = "";
    private double[] current;
    private double[] old1;
    private double[] old2;
    protected boolean stereo = true;
    protected boolean rem = true;

    public boolean isStereo() {
        return stereo;
    }

    protected abstract void drawMono(Graphics g, Color c, double[] toDraw);

    protected abstract void drawStereo(Graphics g, Color c, double[] toDraw);

    private void draw(Graphics g, Color c, double[] toDraw) {
        if (stereo) {
            drawStereo(g, c, toDraw);
        } else {
            drawMono(g, c, toDraw);
        }
    }

    protected abstract double[] translate(double[] audio);

    @Override
    public void setCurrentNormalizedAudio(double[] currentAudio) {
        if(rem){
            old2 = old1;
            old1 = current;
        }
        current = translate(currentAudio);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(rem){
            if (old2 != null) {
                draw(g, Color.red, old2);
            }
            if (old1 != null) {
                draw(g, Color.yellow, old1);
            }
        }
        if (current != null) {
            draw(g, Color.green, current);
        }

        g.setColor(this.getForeground());
        if (stereo) {
            g.drawString("Left", 5, g.getFontMetrics().getHeight());
            g.drawString("Right", 5, getHeight() - 5);
            g.drawString(title, getWidth() - g.getFontMetrics().stringWidth(title) - 5, g.getFontMetrics().getHeight());
        } else {
            g.drawString(title, 5, g.getFontMetrics().getHeight());
        }
    }

    public static class SpectrumPanel extends AudioPanel {

        private int resampleFactor;
        public SpectrumPanel() {
            title = "Spectrum (DCT)";
            resampleFactor = 4;
        }

        @Override
        protected void drawMono(Graphics g, Color c, double[] toDraw) {
            g.setColor(c);
            for (int i = 0; i < toDraw.length; ++i) {
                g.fillRect((int) ((i - 0.0) / toDraw.length * getWidth()),
                        getHeight(),
                        (int) ((1.0 * getWidth()) / toDraw.length),
                        (int) Functions.changeInterval(toDraw[i], 0, 10, 0, -getHeight()));
            }
            g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        }

        @Override
        protected void drawStereo(Graphics g, Color c, double[] toDraw) {
            g.setColor(c);
            //left
            for (int i = 0; i < toDraw.length; i += 2) {
                g.fillRect((int) ((i - 0.0) / toDraw.length * getWidth()),
                        getHeight() / 2,
                        (int) ((2.0 * getWidth()) / toDraw.length),
                        (int) Functions.changeInterval(toDraw[i], 0, 10, 0, -getHeight() / 2));
            }
            //rigth
            for (int i = 1; i < toDraw.length; i += 2) {
                g.fillRect((int) ((i - 1.0) / toDraw.length * getWidth()),
                        getHeight() / 2,
                        (int) ((2.0 * getWidth()) / toDraw.length),
                        (int) Functions.changeInterval(toDraw[i], 0, 10, 0, getHeight() / 2));
            }
            g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        }

        @Override
        protected double[] translate(double[] audio) {
            if (stereo) {
                return DCTStereo(audio);
            } else {
                return DCTMono(audio);
            }
        }

        private double[] DCTStereo(double[] toTransform) {
            double[] dct = new double[toTransform.length / resampleFactor];
            synchronized (toTransform) {
                for (int i = 0; i < resampleFactor; ++i) {
                    for (int k = 0; k < dct.length; ++k) {
                        for (int n = k % 2; n < dct.length; n += 2) {
                            dct[k] += toTransform[dct.length * i + n] * Math.cos(Math.PI / dct.length * (((int) (n / 2)) + 0.5) * ((int) (k / 2)));
                        }
                    }
                }
            }
            for (int k = 0; k < dct.length; ++k) {
                dct[k] = Math.abs(dct[k]);
            }
            return dct;
        }

        private double[] DCTMono(double[] toTransform) {
            double[] dct = new double[toTransform.length / resampleFactor];
            synchronized (toTransform) {
                for (int i = 0; i < resampleFactor; ++i) {
                    for (int k = 0; k < dct.length; ++k) {
                        for (int n = 0; n < dct.length; ++n) {
                            dct[k] += toTransform[dct.length * i + n] * Math.cos(Math.PI / dct.length * (n + 0.5) * k);
                        }
                    }
                }
            }
            for (int k = 0; k < dct.length; ++k) {
                dct[k] = Math.abs(dct[k]);
            }
            return dct;
        }
    }

    public static class PCMPanel extends AudioPanel {

        public PCMPanel() {
            title = "PCM";
            rem = false;
        }

        @Override
        protected void drawMono(Graphics g, Color c, double[] toDraw) {
            g.setColor(c);
            for (int i = 1; i < toDraw.length; ++i) {
                g.drawLine((int) ((i - 1.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i - 1], -1, 1, getHeight(), 0),
                        (int) ((i - 0.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i], -1, 1, getHeight(), 0));
            }
        }

        @Override
        protected void drawStereo(Graphics g, Color c, double[] toDraw) {
            g.setColor(c);
            for (int i = 2; i < toDraw.length; i += 2) {
                g.drawLine((int) ((i - 2.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i - 2], -1, 1, getHeight() / 2, 0),
                        (int) ((i - 0.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i], -1, 1, getHeight() / 2, 0));
            }
            for (int i = 3; i < toDraw.length; i += 2) {
                g.drawLine((int) ((i - 3.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i - 2], -1, 1, getHeight(), getHeight() / 2),
                        (int) ((i - 1.0) / toDraw.length * getWidth()),
                        (int) Functions.changeInterval(toDraw[i], -1, 1, getHeight(), getHeight() / 2));
            }
        }

        @Override
        protected double[] translate(double[] audio) {
            return Arrays.copyOf(audio, audio.length);
        }
    }
}
