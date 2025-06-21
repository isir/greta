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
package greta.auxiliary.groupbehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MovementProcessor {
	
	private Timer timer = null;
	private List<MoveSelected> movesSelected = null;
	private final Object mutexObject;
	private MovesAvailable movesAvailable = null;
	private GroupBehaviorSender groupBehaviourSender = null;
	
	public MovementProcessor() {
		this.mutexObject = new Object();
		movesSelected = new ArrayList<MoveSelected>();
	}
	
	public void addMoveSelected(MoveSelected arg) {
		synchronized(this.mutexObject) {
			if (this.movesAvailable == null) {
				throw new RuntimeException("The list of actors have not been initialized");
			}
			
			this.movesSelected.add(arg);
			if (this.timer == null) {
				this.timer = new Timer();
				this.timer.schedule(new TimerTask() {

					@Override
					public void run() {
						MovementProcessor.this.onTimeout();
					}
				}, movesAvailable.getTimeout());
			}
		}
	}
	
	private void onTimeout() {
		synchronized(this.mutexObject) {
			try {
				if (!this.movesSelected.isEmpty()) {
					int selectedIdx = (int) (Math.random() * this.movesSelected.size());
					if (this.movesSelected.size() == selectedIdx) {
						selectedIdx--;
					}
					
					String actorName = this.movesSelected.get(selectedIdx).getActorName();
					String moveId = this.movesSelected.get(selectedIdx).getMoveId();
					String moveUid = this.movesSelected.get(selectedIdx).getMoveUid();
					String bmlTemplate = "somefile.xml";
					
					if (this.groupBehaviourSender != null) {
						this.groupBehaviourSender.send(new MoveGranted(actorName, moveId, moveUid, bmlTemplate));
					}
				}
			} finally {
				this.movesSelected.clear();
				this.timer = null;		
			}					
		}
	}
	
	public void setMovesAvailable(MovesAvailable movesAvailable) {
		synchronized(this.mutexObject) {
			if (this.timer != null) {
				this.timer.cancel();
			}
			this.timer = new Timer();
			this.movesSelected.clear();
			this.movesAvailable = movesAvailable;
			this.timer.schedule(new TimerTask() {

				@Override
				public void run() {
					MovementProcessor.this.onTimeout();
				}
			}, movesAvailable.getTimeout());
		}
	}

	public void setGroupBehaviourSender(GroupBehaviorSender groupBehaviourSender) {
		this.groupBehaviourSender = groupBehaviourSender;
	}
}
