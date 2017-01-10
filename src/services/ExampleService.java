package services;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ExampleService extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(ExampleService.class);
	
	@Override
	public void start() throws Exception {
		
		vertx.eventBus().consumer("services.ExampleService", message -> {
			
			JsonObject response = new JsonObject();
			try {
				JsonObject request = (JsonObject) message.body();
				response.put("parameters", request.getJsonObject("parameters"));
				message.reply(new JsonObject());
			}
			catch (Exception e) {
				response.put("error", e.getMessage());
				logger.error("error", e);
				message.reply(response);
			}
		});
	}
}
