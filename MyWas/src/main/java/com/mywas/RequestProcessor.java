package com.mywas;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestProcessor.class.getCanonicalName());
    private static Configuration config = new Configuration();

    private Socket connection;
    private File rootDirectory;
    private Map<String, String> serverInfo = new HashMap<String, String>();

    public RequestProcessor(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
        	// Response
            OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out = new OutputStreamWriter(raw);

        	// Request
        	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(connection.getInputStream()), "UTF-8"));
			StringBuilder requestLine = new StringBuilder();
			String line;

			// Get header message
			while ( !"".equals((line = bufferedReader.readLine())) ) {
				requestLine.append(line);
			}

			String get = requestLine.toString();
            logger.info("{}, {}",connection.getRemoteSocketAddress(), get);

            // HTTP Header parsing
            List<String> headerInfoList = Arrays.asList(get.split("\\s+"));
            String method = headerInfoList.get(0);
            String fileName = headerInfoList.get(1);
            String version = headerInfoList.get(2);
            String hostname = headerInfoList.get(3).split(":")[0];
            logger.info("HTTP Header parsing complated.");

            if (method.equals("GET")) {
            	// Settings HostInfo
				setHostInfo(hostname);

                if (fileName.endsWith("/")) {
                	fileName += serverInfo.get("indexFile");
                }

                String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
                File theFile = new File(rootDirectory, fileName.substring(1, fileName.length()));
                String docroot = serverInfo.get("docroot");

                // 200 OK
                if (theFile.canRead() && theFile.getCanonicalPath().startsWith(docroot)) {
                    byte[] theData = Files.readAllBytes(theFile.toPath());

                    if (version.startsWith("HTTP/")) { // send a MIME header
                        sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
                    }

                    raw.write(theData);
                    raw.flush();

                } else {
                	// 404 Error : can't find the file
                	errorHandler(connection, "404");
                }

            } else {
            	// 501 Error : method does not equal "GET"
            	errorHandler(connection, "501");
            }

        } catch (IOException ex) {
            logger.error("Error talking to {}, {}", connection.getRemoteSocketAddress(), ex);
		} catch (Exception ex) {
			logger.error("{}", ex);
		} finally {
            try {
                connection.close();
            } catch (IOException ex) {
            	logger.error("{}", ex);
            }
        }
    }

    /**
     * @param hostname
     */
    private void setHostInfo(String hostname) {
    	JSONArray hostInfo = config.getHostInfo();
    	JSONObject hostInfoObj;
		int size = hostInfo.size();
		for (int i=0; i<size; i++) {
			hostInfoObj = (JSONObject) hostInfo.get(i);

			if (hostname.equals((String) hostInfoObj.get("hostname"))) {
				serverInfo.put("hostname", (String) hostInfoObj.get("hostname"));
				serverInfo.put("docroot", (String) hostInfoObj.get("docroot"));
				serverInfo.put("indexFile", (String) hostInfoObj.get("indexFile"));

				rootDirectory = new File(serverInfo.get("docroot"));
				if (rootDirectory.isFile()) {
					throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
		        }

	            try {
					rootDirectory = rootDirectory.getCanonicalFile();
				} catch (IOException ex) {
					logger.error("{}", ex);
				}
			}
		}
    }

    /**
     * @param socket
     * @param resCode
     * @throws Exception
     */
    private void errorHandler(Socket socket, String resCode) throws Exception {
    	OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
        Writer out = new OutputStreamWriter(raw);

        JSONArray errorPageInfo = config.getErrorPageInfo();
        JSONObject errorPageObj = (JSONObject) errorPageInfo.get(0);
        JSONObject errorObj  = (JSONObject) errorPageObj.get(resCode);

		String path = (String) errorPageObj.get("path");
		String htmlFileName = (String) errorObj.get("fileName");
		String resCodeMessage = (String) errorObj.get("message");

		String contentType = URLConnection.getFileNameMap().getContentTypeFor(htmlFileName);
        File theFile = new File(new File(path), htmlFileName.substring(0, htmlFileName.length()));
        byte[] theData = Files.readAllBytes(theFile.toPath());

        out.write("HTTP/1.0 " + resCodeMessage + "\r\n");
        out.write("Date: " + new Date() + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + theFile.length() + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();

        raw.write(theData);
        raw.flush();
    }

    /**
     * @param out
     * @param resCodeMessage
     * @param contentType
     * @param length
     * @throws IOException
     */
    private void sendHeader(Writer out, String resCodeMessage, String contentType, int length) throws IOException {
    	Date now = new Date();
        out.write(resCodeMessage + "\r\n");
        out.write("HTTP/1.0 " + resCodeMessage + "\r\n");
        out.write("Date: " + now + "\r\n");
        out.write("Server: JHTTP 2.0\r\n");
        out.write("Content-length: " + length + "\r\n");
        out.write("Content-type: " + contentType + "\r\n\r\n");
        out.flush();
    }
}