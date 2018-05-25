package vib.core.SubjectPlanner;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
