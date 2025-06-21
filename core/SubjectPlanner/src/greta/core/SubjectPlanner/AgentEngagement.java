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
