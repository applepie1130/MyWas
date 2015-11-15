package com.mywas;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
	private static Logger logger = LoggerFactory.getLogger(HttpServer.class.getCanonicalName());
	private static Configuration config = new Configuration();
    private final int port;

    public HttpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {

        ExecutorService pool = Executors.newFixedThreadPool(config.getThreadsCnt());

        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Accepting connections on port " + server.getLocalPort());

            while (true) {
                try {
                    Socket request = server.accept();
                    Runnable r = new RequestProcessor(request);
                    pool.submit(r);
                } catch (IOException ex) {
                    logger.error("Error accepting connection {}", ex);
                }
            }
		}
    }

    public static void main(String[] args) {
        int port = config.getPort();
        try {
            HttpServer webserver = new HttpServer(port);
            webserver.start();
        } catch (IOException ex) {
            logger.error("Server could not start {}", ex);
        }
    }
}