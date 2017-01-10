package web;

import java.io.File;
import java.util.Map.Entry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import providers.config.ConfigProvider;

public class WebHandler extends AbstractVerticle {
	final Logger logger = LoggerFactory.getLogger(WebHandler.class);
	
	@Override
	public void start() throws Exception {
		
		JsonObject vertxConfig = ConfigProvider.getConfig().getJsonObject("vertx");
		
		PemKeyCertOptions pemOptions = new PemKeyCertOptions()
				.setKeyPath(vertxConfig.getString("server.key.path"))
				.setCertPath(vertxConfig.getString("server.cert.path"));
		
		HttpServerOptions options = new HttpServerOptions()
				.setPemKeyCertOptions(pemOptions)
				.setSsl(true)
				.setUseAlpn(true);
		
		HttpServer server = vertx.createHttpServer(options);
		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());
		
		router.get("/graphql").blockingHandler(GraphQLHandler::execute);
		router.post("/graphql").blockingHandler(GraphQLHandler::execute);
		
		router.get("/example/:entityName*").blockingHandler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			String entityName = routingContext.pathParam("entityName");
			
			HttpServerRequest request = routingContext.request();
			JsonObject jsonRequest = new JsonObject();
			
			String authorization = request.getHeader("Authorization");
			if (authorization != null) {
				jsonRequest.put("authorization", authorization);
			}
			
			JsonObject jsonParameters = new JsonObject();
			for (Entry<String, String> entry: request.params().entries()) {
				jsonParameters.put(entry.getKey(), entry.getValue());
			}
			jsonRequest.put("parameters", jsonParameters);
			
			vertx.eventBus().send("services.ExampleService", jsonRequest, message -> {
				JsonObject jsonResponse = new JsonObject(message.result().body().toString());
				JsonArray validations = jsonResponse.getJsonArray("validations");
				
				MultiMap headers = response.headers();
				headers.add("Content-Location", "/api/validate/" + entityName);
				headers.add("Content-Type", "application/json; charset=utf-8");
				
				if (validations != null && validations.size() > 0) {
					response.putHeader("Error", "true");
				}
				response.end(jsonResponse.encode());
			});
		});
		
		router.get().handler(routingContext -> {
			File file = new File("web" + routingContext.request().path());
			if (file.exists() && file.isFile()) {
				routingContext.response().sendFile("web" + routingContext.request().path());
			}
			else {
				String path = routingContext.request().path() + (routingContext.request().path().endsWith("/") ? "" : "/") + "index.html";
				file = new File("web" + path);
				if (file.exists()) {
					routingContext.response().putHeader("Location", path);
					routingContext.response().setStatusCode(302);
				}
				else {
					routingContext.response().setStatusCode(404);
				}
				routingContext.response().end();
			}
		});
		
		server.requestHandler(router::accept).listen(vertxConfig.getInteger("port", 443));
	}
}
