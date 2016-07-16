package com.taozeyu.calico;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by taozeyu on 16/7/16.
 */
public class ArgumentsHandler {

    private final Map<String, String> argsMap = new HashMap<>();
    private final Map<String, Type> constraintsMap = new HashMap<>();
    private final Map<String, String> abbreviationsMap = new HashMap<>();
    private String command;

    protected ArgumentsHandler(String args[]) {
        if (args != null) {
            for (String arg : args) {
                arg = arg.trim();
                if (arg.matches("^\\w+$")) {
                    handleCommand(arg);
                } else {
                    handleArg(arg);
                }
            }
        }
    }

    protected  void abbreviation(String abbreviation, String name) {
        abbreviationsMap.put(abbreviation, name);
    }

    protected void constraint(String name, Type type) {
        constraintsMap.put(name, type);
    }

    protected enum Type {
        Integer, Number, String
    }

    private void handleCommand(String arg) {
        if (command != null) {
            throw new RuntimeException("duplicate arguments `"+ command +"` and `"+ arg + "`");
        }
        command = arg;
    }

    private void handleArg(String arg) {
        String name = null;
        String value = null;
        if (arg.matches("^\\-\\w+$")) {
            arg = arg.replaceAll("^\\-", "");
            name = abbreviationsMap.get(arg);

        } else if (arg.matches("^\\-\\-\\w+(=(\\w|_|\\-|\\\\|/)+)?$")){
            arg = arg.replaceAll("(^\\-\\-)", "");
            String splits[] = arg.split("=");
            name = splits[0];
            if (splits.length > 0) {
                value = splits[1];
            }
        } else {
            throw new RuntimeException("unrecognized argument `"+ arg + "`");
        }
        if (name == null) {
            throw new RuntimeException("unrecognized argument `"+ arg + "`");
        }
        if (value == null) {
            value = name;
        }
        Type constraintType = constraintsMap.get(name);
        if (constraintType != null) {
            if (constraintType == Type.Integer && !isInteger(value)) {
                throw new RuntimeException("argument "+ name + "'s value must be integer, instead of `"+ value +"`");
            }
            if (constraintType == Type.Number && !isNumber(value)) {
                throw new RuntimeException("argument "+ name + "'s value must be number, instead of `"+ value +"`");
            }
        }
        if (argsMap.containsKey(name)) {
            throw new RuntimeException("duplicate arguments `"+ name +"`");
        }
        argsMap.put(name, value);
    }

    public int getInteger(String name) {
        try {
            return Integer.valueOf(argsMap.get(name));
        } catch (Exception e) {
            return 0;
        }
    }

    public double getNumber(String name) {
        try {
            return Double.valueOf(argsMap.get(name));
        } catch (Exception e) {
            return 0;
        }
    }

    public String getString(String name) {
        return argsMap.get(name);
    }

    public boolean hasValue(String name) {
        return argsMap.containsKey(name);
    }

    public String getCommand() {
        return command;
    }

    private boolean isInteger(String value) {
        try {
            Integer.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isNumber(String value) {
        try {
            Double.valueOf(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
