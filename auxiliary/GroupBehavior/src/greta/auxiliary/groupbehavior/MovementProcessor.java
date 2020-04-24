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
