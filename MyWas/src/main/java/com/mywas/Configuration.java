package com.mywas;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
	private static Logger logger = LoggerFactory.getLogger(Configuration.class.getCanonicalName());
	private final String CONFIG_FILE_PATH = getClass().getClassLoader().getResource("config.json").getPath();

	private JSONObject configData;
	private Integer port;
	private Integer threadsCnt;
	private String docroot;
	private String indexFile;
	private JSONArray hostInfo;
	private JSONArray errorPageInfo;

	Configuration () {
		try {
			FileReader reader = new FileReader(CONFIG_FILE_PATH);
			JSONParser jsonParser = new JSONParser();
			configData = (JSONObject) jsonParser.parse(reader);

			// Setting values
			// threadsCnt
			threadsCnt = Integer.parseInt(configData.get("threadsCnt").toString());

			// port
			port = Integer.parseInt(configData.get("port").toString()); // port
			if (port < 0 || port > 65535 || port == null) {
				port = 8080;
			}

			// hostInfo
			hostInfo = (JSONArray) configData.get("hostInfo");

			// errorPageInfo
			errorPageInfo = (JSONArray) configData.get("errorPage");

		} catch (Exception ex) {
			logger.error("error!!", ex);
		}
	}

	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @return the threadsCnt
	 */
	public Integer getThreadsCnt() {
		return threadsCnt;
	}

	/**
	 * @return the docroot
	 */
	public String getDocroot() {
		return docroot;
	}

	/**
	 * @return the indexFile
	 */
	public String getIndexFile() {
		return indexFile;
	}

	/**
	 * @return the hostInfo
	 */
	public JSONArray getHostInfo() {
		return hostInfo;
	}

	/**
	 * @return the errorPageInfo
	 */
	public JSONArray getErrorPageInfo() {
		return errorPageInfo;
	}
}