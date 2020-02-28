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

import greta.core.util.command.Command;
import greta.core.util.command.CommandPerformer;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nadine Glas
 */
public class SubjectPlanner implements CommandPerformer {

    public void setUserEngagementValue(double userEngagement){
        Candidates.candidates.UserEng.SetUserEngagementValue(userEngagement);
    }

    public double getUserEngagementValue(){
        return Candidates.candidates.UserEng.GetUserEngagementValue();
    }

    @Override
    public String performCommandReturnString(Command command) {
        String answer = "";
        Method[] methods = Candidates.class.getMethods();
        for (Method m : methods) {
            if (command.commandName.equals(m.getName())) {
                Object object;
                if (command.commandParameterString == null) {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{});
                        return object.toString();
                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{command.commandParameterString});
                        return object.toString();
                        //foo(String[])

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        Object[] params = new Object[command.commandParameterString.length];
                        for (int i = 0; i < command.commandParameterString.length; ++i) {
                            params[i] = command.commandParameterString[i];
                        }
                        object = m.invoke(Candidates.candidates, params);
                        return object.toString();
                        //foo(String, String, String)

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return answer;
    }

    @Override
    public int performCommandReturnInt(Command command) {
        int answer = 1000;
        Method[] methods = Candidates.class.getMethods();
        for (Method m : methods) {
            if (command.commandName.equals(m.getName())) {
                Object object;
                if (command.commandParameterString == null) {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{});
                        String answerString = object.toString();
                        return Integer.parseInt(answerString);
                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{command.commandParameterString});
                        String answerString = object.toString();
                        return Integer.parseInt(answerString);
                        //foo(String[])

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        Object[] params = new Object[command.commandParameterString.length];
                        for (int i = 0; i < command.commandParameterString.length; ++i) {
                            params[i] = command.commandParameterString[i];
                        }
                        object = m.invoke(Candidates.candidates, params);
                        String answerString = object.toString();
                        return Integer.parseInt(answerString);
                        //foo(String, String, String)

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return answer;
    }

    @Override
    public boolean performCommandReturnBoolean(Command command) {
        boolean answer = false;
        Method[] methods = Candidates.class.getMethods();
        for (Method m : methods) {
            if (command.commandName.equals(m.getName())) {
                Object object;
                if (command.commandParameterString == null) {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{});
                        String answerString = object.toString();
                        return Boolean.getBoolean(answerString);
                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    try {
                        object = m.invoke(Candidates.candidates, new Object[]{command.commandParameterString});
                        String answerString = object.toString();
                        return Boolean.getBoolean(answerString);
                        //foo(String[])

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    try {
                        Object[] params = new Object[command.commandParameterString.length];
                        for (int i = 0; i < command.commandParameterString.length; ++i) {
                            params[i] = command.commandParameterString[i];
                        }
                        object = m.invoke(Candidates.candidates, params);
                        String answerString = object.toString();
                        return Boolean.getBoolean(answerString);
                        //foo(String, String, String)

                    } catch (Exception ex) {
                        Logger.getLogger(SubjectPlanner.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return answer;
    }
}
