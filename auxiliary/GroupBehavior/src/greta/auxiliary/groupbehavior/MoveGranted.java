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

public class MoveGranted {

	private String actorName;
	private String moveId;
	private String moveUid;
	private String bmlTemplate;
	
	public MoveGranted(String actorName, String moveId, String moveUid, String bmlTemplate) {
		this.actorName = actorName;
		this.moveId = moveId;
		this.moveUid = moveUid;
		this.bmlTemplate = bmlTemplate;
	}
	
	public String getActorName() {
		return actorName;
	}
	public String getMoveId() {
		return moveId;
	}
	public String getBmlTemplate() {
		return bmlTemplate;
	}
	public String getMoveUid() {
		return moveUid;
	}
	
}
