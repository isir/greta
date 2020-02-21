/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPParser;
import greta.core.util.id.IDProvider;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPReceiver extends TextReceiver implements BAPFrameEmitter {

    private BAPFrameEmitterImpl bapEmitter = new BAPFrameEmitterImpl();
    private BAPParser parser = new BAPParser();

    public BAPReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "greta.BAP");
    }

    public BAPReceiver(String host, String port, String topic) {
        super(host, port, topic);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {

        List<BAPFrame> frames = parser.readFromString(content.toString(), false, false);

        if (!frames.isEmpty()) {
            String id = "ActiveMQ_BAP_Receiver";
            if (properties.containsKey("content-id")) {
                id = properties.get("content-id").toString();
            } else {
                if (properties.containsKey("id")) {
                    id = properties.get("id").toString();
                }
            }
            bapEmitter.sendBAPFrames(IDProvider.createID(id), frames);
        }
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        bapEmitter.addBAPFramePerformer(bapFramePerformer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapFramePerformer) {
        bapEmitter.removeBAPFramePerformer(bapFramePerformer);
    }

}
