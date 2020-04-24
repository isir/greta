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
