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
