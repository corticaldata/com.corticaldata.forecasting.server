package providers.config;

import io.vertx.core.json.JsonObject;

public final class ConfigProvider {
	private static JsonObject config;
	
	public static JsonObject getConfig() {
		return config;
	}
	
	public synchronized static void setConfig(JsonObject config) {
		ConfigProvider.config = config;
	}
}
