/*
 * This file is part of Greta.
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
package greta.core.SubjectPlanner;

/**
 *
 * @author Nadine
 */
public class AgentEngagement {

    private double AgentEngagementValue = 0;
    private boolean AgentEngagement;
    private double AgentEngagementWeight = 1;
    private String AgentOrientation = "period";

    public void SetAgentEngagementValue(double agentEngagement) {
        this.AgentEngagementValue = AgentEngagementValue;
    }

    public double GetAgentEngagementValue() {
        return AgentEngagementValue;
    }

    public void SetAgentEngagement(boolean agentEngagement) {
        this.AgentEngagement = AgentEngagement;
    }

    public boolean GetAgentEngagement() {
        if (AgentEngagementValue >= 0){
            AgentEngagement = true;
        }
        else{
            AgentEngagement = false;
        }
        return AgentEngagement;
    }

    public void SetAgentEngagementWeight(boolean agentEngagementWeight) {
        this.AgentEngagementWeight = AgentEngagementWeight;
    }

    public double GetAgentEngagementWeight() {
        return AgentEngagementWeight;
    }

    public void SetAgentOrientation(String agentEngagementWeight) {
        this.AgentEngagementWeight = AgentEngagementWeight;
    }

    public String GetAgentOrientation() {
        return AgentOrientation;
    }
}
