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
