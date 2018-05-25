package vib.core.SubjectPlanner;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nadine
 */
public class PolitenessStrategy {

    int PolitenessLevel = 0;
    
    UserEngagement userEng = new UserEngagement();
//    double userEngagementValue = userEng.GetUserEngagementValue();
    
    AgentEngagement agentEng = new AgentEngagement();
//    double agentEngagementValue = agentEng.GetAgentEngagementValue();
    
    public double GetThreat(double agentPreference, double userPreference) {
        double threat;
        double distance = 0;
        double power = 0;
        System.out.println("User engagement value = " + userEng.GetUserEngagementValue());
        if (agentPreference >= userPreference) {
            threat = distance + power + (agentPreference - userPreference) - userEng.GetUserEngagementValue();
        } else {
            threat = distance + power + (userPreference - agentPreference) - userEng.GetUserEngagementValue();
        }
        System.out.println("threat = " + threat);
        return threat;
    }

    public double GetPolitenessStrategy(double agentPreference, double userPreference) {
        double politenessStrategy;
        double threat = GetThreat(agentPreference, userPreference);
        if (threat <= 0) {
            politenessStrategy = 1;
        } else if ((threat > 0) && (threat <= 4)) {
            politenessStrategy = 2;
        } else if ((threat > 4) && (threat <= 8)) {
            politenessStrategy = 3;
        } else if ((threat > 8) && (threat <= 11)) {
            politenessStrategy = 3;
        } else if ((threat > 11) && (threat <= 13)) {
            politenessStrategy = 4;
        } else if (threat > 13) {
            politenessStrategy = 5;
        } else {
            politenessStrategy = 3;
        }
        return politenessStrategy;
    }
// public double GetPolitenessStrategy (double AgentPreference, double UserPreference, double AgentEngagement){
//     double Threat = GetThreat(AgentPreference, UserPreference, AgentEngagement);
//           if (Threat <= 1){
//         }
// }
}
