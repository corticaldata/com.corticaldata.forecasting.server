package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;

import providers.config.ConfigProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class Main extends AbstractVerticle {
	
	final Logger logger = LoggerFactory.getLogger(Main.class);
	
	@Override
	public void start() throws Exception {
		
		ConfigProvider.setConfig(config());
		
		vertx.deployVerticle("services.ExampleService", 
				new DeploymentOptions()
						.setWorker(true)
						.setConfig(config()),
				result -> {
					logger.info("Deployed successfully to address services.ExampleService");
				});
		
		vertx.deployVerticle("web.WebHandler",
				new DeploymentOptions()
						.setWorker(true)
						.setConfig(config()),
				result -> {
					logger.info("WebHandler deployed successfully");
				});
	}
	
	public static void main(String[] args) throws Exception {
		final Logger logger = LoggerFactory.getLogger(Main.class);
		final Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(new Main(), 
				new DeploymentOptions()
						.setConfig(new JsonObject(IOUtils.toString(new FileReader("config.json")))),
				
				result -> {
					if (result.succeeded()) {
						logger.info("OK");
					}
					else if (result.failed()) {
						logger.error("ERROR. " + result.cause().getMessage());
					}
					else {
						logger.error("ERROR. Unknown");
					}
				}
		);
		
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		vertx.close();
	}
}
