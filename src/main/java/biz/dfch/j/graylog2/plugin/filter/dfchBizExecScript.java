package biz.dfch.j.graylog2.plugin.filter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FilenameUtils;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.filters.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class dfchBizExecScript implements MessageFilter
{
    private static final String DF_PLUGIN_NAME = "d-fens SCRIPT Filter";

    private static final String DF_SCRIPT_ENGINE = "DF_SCRIPT_ENGINE";
    private static final String DF_SCRIPT_PATH_AND_NAME = "DF_SCRIPT_PATH_AND_NAME";
    private static final String DF_SCRIPT_NAME = "DF_SCRIPT_NAME";
    private static final String DF_SCRIPT_DISPLAY_OUTPUT = "DF_SCRIPT_DISPLAY_OUTPUT";
    private static final String DF_SCRIPT_CACHE_CONTENTS = "DF_SCRIPT_CACHE_CONTENTS";
    private static final String DF_PLUGIN_PRIORITY = "DF_PLUGIN_PRIORITY";
    private static final String DF_PLUGIN_DROP_MESSAGE = "DF_PLUGIN_DROP_MESSAGE";
    private static final String DF_PLUGIN_DISABLED = "DF_PLUGIN_DISABLED";

    // for performance reasons these configuration items have internal variables
    // DF_PLUGIN_DISABLED, DF_PLUGIN_DROP_MESSAGE
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private boolean dropMessage = false;
    private Configuration configuration;
    private String configurationFileName;

    private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
    private ScriptEngine scriptEngine;
    private ScriptContext scriptContext;
    private File file;

    private static final Logger LOG = LoggerFactory.getLogger(dfchBizExecScript.class);

    public dfchBizExecScript() throws IOException, URISyntaxException
    {
        try
        {
            LOG.debug(String.format("*** [%d] %s: Initialising plugin ...\r\n", Thread.currentThread().getId(), DF_PLUGIN_NAME));

            // get config file
            CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
            URI uri = codeSource.getLocation().toURI();

            // String path = uri.getSchemeSpecificPart();
            // path would contain absolute path including jar file name with extension
            // String path = FilenameUtils.getPath(uri.getPath());
            // path would contain relative path (no leadig '/' and no jar file name

            String path = FilenameUtils.getPath(uri.getPath());
            if(!path.startsWith("/"))
            {
                path = String.format("/%s", path);
            }
            String baseName = FilenameUtils.getBaseName(uri.getSchemeSpecificPart());
            if(null == baseName || baseName.isEmpty())
            {
                baseName = this.getClass().getPackage().getName();
            }

            // get config values
            configurationFileName = FilenameUtils.concat(path, baseName + ".conf");
            JSONParser jsonParser = new JSONParser();
            Object object = jsonParser.parse(new FileReader(configurationFileName));

            JSONObject jsonObject = (JSONObject) object;
            String scriptEngine = (String) jsonObject.get(DF_SCRIPT_ENGINE);
            String scriptPathAndName = (String) jsonObject.get(DF_SCRIPT_PATH_AND_NAME);
            if(null == scriptPathAndName || scriptPathAndName.isEmpty())
            {
                scriptPathAndName = FilenameUtils.concat(path, (String) jsonObject.get(DF_SCRIPT_NAME));
            }
            Boolean scriptCacheContents = (Boolean) jsonObject.get(DF_SCRIPT_CACHE_CONTENTS);
            Boolean scriptDisplayOutput = (Boolean) jsonObject.get(DF_SCRIPT_DISPLAY_OUTPUT);
            String pluginPriority = (String) jsonObject.get(DF_PLUGIN_PRIORITY);
            Boolean pluginDropMessage = (Boolean) jsonObject.get(DF_PLUGIN_DROP_MESSAGE);
            Boolean pluginDisabled = (Boolean) jsonObject.get(DF_PLUGIN_DISABLED);

            // set configuration
            Map<String, Object> map = new HashMap<>();
            map.put(DF_SCRIPT_ENGINE, scriptEngine);
            map.put(DF_SCRIPT_PATH_AND_NAME, scriptPathAndName);
            map.put(DF_SCRIPT_DISPLAY_OUTPUT, scriptDisplayOutput);
            map.put(DF_SCRIPT_CACHE_CONTENTS, scriptCacheContents);
            map.put(DF_PLUGIN_PRIORITY, pluginPriority);
            map.put(DF_PLUGIN_DROP_MESSAGE, pluginDropMessage);
            map.put(DF_PLUGIN_DISABLED, pluginDisabled);

            initialize(new Configuration(map));
        }
        catch(IOException ex)
        {
            LOG.error(String.format("*** [%d] %s: Initialising plugin FAILED. Filter will be disabled.\r\n%s\r\n", Thread.currentThread().getId(), DF_PLUGIN_NAME, ex.getMessage()));
            LOG.error("*** " + DF_PLUGIN_NAME + "::dfchBizExecScript() - IOException - Filter will be disabled.");
            ex.printStackTrace();
        }
        catch(Exception ex)
        {
            LOG.error(String.format("*** [%d] %s: Initialising plugin FAILED. Filter will be disabled.\r\n", Thread.currentThread().getId(), DF_PLUGIN_NAME));
            LOG.error("*** " + DF_PLUGIN_NAME + "::dfchBizExecScript() - Exception - Filter will be disabled.");
            ex.printStackTrace();
        }
    }

    // we define an 'initialize' method so it is similar to the plugin types with UI configuration options
    private void initialize(final Configuration configuration)
    {
        try
        {
            this.configuration = configuration;
            LOG.trace(String.format("DF_SCRIPT_ENGINE         : %s\r\n", configuration.getString(DF_SCRIPT_ENGINE)));
            LOG.trace(String.format("DF_SCRIPT_PATH_AND_NAME  : %s\r\n", configuration.getString(DF_SCRIPT_PATH_AND_NAME)));
            LOG.trace(String.format("DF_SCRIPT_DISPLAY_OUTPUT : %b\r\n", configuration.getBoolean(DF_SCRIPT_DISPLAY_OUTPUT)));
            LOG.trace(String.format("DF_SCRIPT_CACHE_CONTENTS : %b\r\n", configuration.getBoolean(DF_SCRIPT_CACHE_CONTENTS)));
            LOG.trace(String.format("DF_PLUGIN_PRIORITY       : %d\r\n", Integer.parseInt(configuration.getString(DF_PLUGIN_PRIORITY))));
            LOG.trace(String.format("DF_PLUGIN_DROP_MESSAGE   : %b\r\n", configuration.getBoolean(DF_PLUGIN_DROP_MESSAGE)));
            LOG.trace(String.format("DF_PLUGIN_DISABLED       : %b\r\n", configuration.getBoolean(DF_PLUGIN_DISABLED)));
            LOG.trace(String.format("DF_PLUGIN_PRIORITY       : %d\r\n", (int) configuration.getInt(DF_PLUGIN_PRIORITY)));

            file = new File(configuration.getString(DF_SCRIPT_PATH_AND_NAME));
            scriptEngine = scriptEngineManager.getEngineByName(configuration.getString(DF_SCRIPT_ENGINE));
            scriptContext = scriptEngine.getContext();

            dropMessage = configuration.getBoolean(DF_PLUGIN_DROP_MESSAGE);
            isRunning.set(!configuration.getBoolean(DF_PLUGIN_DISABLED));

            LOG.info(String.format("*** [%d] %s: Initialising plugin SUCCEEDED. Configuration loaded from '%s'. \r\n", Thread.currentThread().getId(), DF_PLUGIN_NAME, configurationFileName));
        }
        catch(Exception ex)
        {
            LOG.error("*** " + DF_PLUGIN_NAME + "::initialize() - Exception");
            ex.printStackTrace();
        }
    }
    @Override
    public boolean filter(Message msg)
    {
        if(!isRunning.get())
        {
            return false;
        }
        try
        {
            LOG.trace(String.format("Executing '%s' ...", configuration.getString(DF_SCRIPT_PATH_AND_NAME)));

            // TODO
            // add your code to whatever is needed

            StringWriter stringWriter = new StringWriter();
            scriptContext.setWriter(stringWriter);
            scriptEngine.put("message", msg);

            if(!configuration.getBoolean(DF_SCRIPT_CACHE_CONTENTS))
            {
                file = new File(configuration.getString(DF_SCRIPT_PATH_AND_NAME));
            }
            Reader _reader = new FileReader(file);
            scriptEngine.eval(_reader);
            if(configuration.getBoolean(DF_SCRIPT_DISPLAY_OUTPUT))
            {
                LOG.info(String.format("%s\r\n", stringWriter.toString()));
            }
        }
        catch (Exception ex)
        {
            LOG.error(ex.getMessage());
            ex.printStackTrace();
        }
        return dropMessage;
    }

    @Override
    public String getName()
    {
        return DF_PLUGIN_NAME;
    }
    @Override
    public int getPriority()
    {
        if(!isRunning.get())
        {
            // if the plugin is disabled set it to the lowest priority
            return 99;
        }
        return Integer.parseInt(configuration.getString(DF_PLUGIN_PRIORITY));
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
