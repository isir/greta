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
