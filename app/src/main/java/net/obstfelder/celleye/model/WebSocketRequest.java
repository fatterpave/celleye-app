package net.obstfelder.celleye.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by obsjoa on 16.02.2017.
 */
public class WebSocketRequest
{
    private String command;
    private Map<String,String> params = new HashMap<>();

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String toString()
    {
        String message = String.format("WebSocketRequest, command: %s\n params:\n",getCommand());
        for(Map.Entry<String,String> entry : getParams().entrySet())
        {
            message+=String.format("%s : %s\n",entry.getKey(),entry.getValue());
        }
        return message;
    }
}
