/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.keyframes.face;

import greta.core.repositories.AUAPFrame;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class AUEmitterImpl implements AUEmitter, AUPerformer {

    protected List<AUPerformer> auPerformers = new ArrayList<>();

    @Override
    public void addAUPerformer(AUPerformer auPerformer) {
        if (auPerformer != null && !auPerformers.contains(auPerformer))
            auPerformers.add(auPerformer);
    }

    @Override
    public void removeAUPerformer(AUPerformer auPerformer) {
        if (auPerformer != null && auPerformers.contains(auPerformer))
            auPerformers.remove(auPerformer);
    }

    @Override
    public void performAUAPFrame(AUAPFrame auapAnimation, ID requestId) {
        auPerformers.forEach((performer) -> {
            performer.performAUAPFrame(auapAnimation, requestId);
        });
    }

    @Override
    public void performAUAPFrames(List<AUAPFrame> auapAnimation, ID requestId) {
        auPerformers.forEach((performer) -> {
            performer.performAUAPFrames(auapAnimation, requestId);
        });
    }
}
