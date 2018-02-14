package net.obstfelder.celleye.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by paven on 17.08.2017.
 */
public class CellResponseData implements Serializable
{
    public String command;
    public Map<String,String> params = new HashMap<>();
}
