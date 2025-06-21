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

public class MoveSelected extends GbmRequest {

	private String actorName;
	private String targetId;
	private String moveUid;
	private String moveId;
	
	public MoveSelected(String actorName, String targetId, String moveUid, String moveId) {
		this.actorName = actorName;
		this.targetId = targetId;
		this.moveUid = moveUid;
		this.moveId = moveId;
	}
	
	public String getActorName() {
		return actorName;
	}
	public String getTargetId() {
		return targetId;
	}
	public String getMoveUid() {
		return moveUid;
	}
	public String getMoveId() {
		return moveId;
	}
}
