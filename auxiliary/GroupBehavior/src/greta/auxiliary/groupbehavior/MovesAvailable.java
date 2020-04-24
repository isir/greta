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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovesAvailable extends GbmRequest {

	private List<String> actors;
	private Map<String, String> agentBmlIdMap;
	private Map<String, String> agentTypeMap;
	private Map<String, List<MovesAvailable.Move>> moves;
	
	public MovesAvailable() {
		this.actors = new ArrayList<String>();
		this.agentBmlIdMap = new HashMap<String, String>();
		this.agentTypeMap = new HashMap<String, String>();
		this.moves = new HashMap<String, List<Move>>();
	}
	
	public static class Move {
		private String target;
		private String moveId;
		private String moveUid;
		private String opener;
		
		public Move(String target, String moveId, String moveUid, String opener) {
			this.target = target;
			this.moveId = moveId;
			this.moveUid = moveUid;
			this.opener = opener;
		}
		
		public String getTarget() {
			return target;
		}
		public String getMoveId() {
			return moveId;
		}
		public String getMoveUid() {
			return moveUid;
		}
		public String getOpener() {
			return opener;
		}
	}

	public List<String> getActors() {
		return actors;
	}

	public Map<String, String> getAgentBmlIdMap() {
		return agentBmlIdMap;
	}

	public Map<String, List<MovesAvailable.Move>> getMoves() {
		return moves;
	}

	public Map<String, String> getAgentTypeMap() {
		return agentTypeMap;
	}
	
	public long getTimeout() {
		long botTimeoutMs = 2 * 1000;
		long wozTimeoutMs = 4 * 1000;
		long userTimeoutMs = 10 * 1000;
		
		long timeoutMs = botTimeoutMs;
		
		for (Map.Entry<String, String> keyValue : agentTypeMap.entrySet()) {
			String agentType = keyValue.getValue();
			if ("BOT".equals(agentType)) {
				timeoutMs = Math.max(timeoutMs, botTimeoutMs);
			} else if ("WOZ".equals(agentType)) {
				timeoutMs = Math.max(timeoutMs, wozTimeoutMs);
			} else if ("USER".equals(agentType)) {
				timeoutMs = Math.max(timeoutMs, userTimeoutMs);
			} else {
				throw new RuntimeException("Unknown agentType: "+agentType);
			}
		}
		
		return timeoutMs;
	}
}
