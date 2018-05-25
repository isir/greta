/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.SubjectPlanner;

import java.lang.reflect.InvocationTargetException;
import vib.core.util.command.Command;
import vib.core.util.command.CommandEmitter;
import vib.core.util.command.CommandPerformer;
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
