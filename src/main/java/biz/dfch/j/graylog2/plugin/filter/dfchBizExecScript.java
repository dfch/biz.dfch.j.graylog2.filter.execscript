package biz.dfch.j.graylog2.plugin.filter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.configuration.fields.BooleanField;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.filters.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class dfchBizExecScript implements MessageFilter
{
    private static final String DF_PLUGIN_NAME = "d-fens SCRIPT Filter";
    private static final String DF_PLUGIN_HUMAN_NAME = "biz.dfch.j.graylog2.plugin.filter.execscript";
    private static final String DF_PLUGIN_DOC_LINK = "https://github.com/dfch/biz.dfch.j.graylog2.plugin.filter.execscript";
    private static final int DF_PLUGIN_PRIORITY = 0;
    
    private static final String DF_SCRIPT_ENGINE = "DF_SCRIPT_ENGINE";
    private static final String DF_SCRIPT_PATH_AND_NAME = "DF_SCRIPT_PATH_AND_NAME";
    private static final String DF_DISPLAY_SCRIPT_OUTPUT = "DF_DISPLAY_SCRIPT_OUTPUT";
    private static final String DF_SCRIPT_CACHE_CONTENTS = "DF_SCRIPT_CACHE_CONTENTS";

    private boolean _isRunning = false;
    private Configuration _configuration;

    private static ScriptEngineManager _scriptEngineManager;
    private static ScriptEngine _scriptEngine;
    private static ScriptContext _scriptContext;
    private File _file;

    private static final Logger LOG = LoggerFactory.getLogger(dfchBizExecScript.class);


    public dfchBizExecScript()
    {
        _configuration.setString("DF_SCRIPT_ENGINE", "javascript");
        _configuration.setString("DF_SCRIPT_PATH_AND_NAME", "javascript");
        initialize(_configuration);
    }

    private void initialize(final Configuration configuration)
    {
        try
        {
            String s = "*** " + DF_PLUGIN_NAME + "::initialize()";
            LOG.trace(s);

//            _configuration = configuration;
            _isRunning = true;

//            LOG.trace("DF_SCRIPT_ENGINE         : %s\r\n", _configuration.getString("DF_SCRIPT_ENGINE"));
//            LOG.trace("DF_SCRIPT_PATH_AND_NAME  : %s\r\n", _configuration.getString("DF_SCRIPT_PATH_AND_NAME"));
//            LOG.trace("DF_DISPLAY_SCRIPT_OUTPUT : %b\r\n", _configuration.getBoolean("DF_DISPLAY_SCRIPT_OUTPUT"));
//            LOG.trace("DF_SCRIPT_CACHE_CONTENTS : %b\r\n", _configuration.getBoolean("DF_SCRIPT_CACHE_CONTENTS"));

            _file = new File(_configuration.getString("DF_SCRIPT_PATH_AND_NAME"));
            _scriptEngine = _scriptEngineManager.getEngineByName(_configuration.getString("DF_SCRIPT_ENGINE"));
            _scriptContext = _scriptEngine.getContext();

        }
        catch(Exception ex)
        {
            _isRunning = false;

            LOG.error("*** " + DF_PLUGIN_NAME + "::write() - Exception");
            ex.printStackTrace();
        }
    }
    @Override
    public boolean filter(Message msg)
    {
        //System.out.printf("*** %s: '%s'\r\n", msg.getId(), msg.getMessage());
        //LOG.trace("*** %s: '%s'\r\n", msg.getId(), msg.getMessage());
        msg.addField("DF_PLUGIN_NAME", DF_PLUGIN_NAME);


        // TODO 
        // add your code to forward messages to where it is needed

        return false;
    }

    @Override
    public String getName()
    {
        return DF_PLUGIN_NAME;
    }
    @Override
    public int getPriority()
    {
        return DF_PLUGIN_PRIORITY;
    }


}

/**
 *
 *
 * Copyright 2015 Ronald Rink, d-fens GmbH
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
 *
 */
