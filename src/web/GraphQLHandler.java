package web;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.GraphQLArgument.newArgument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import providers.htm.HTMProvider;

public class GraphQLHandler {
	
	static GraphQLSchema schema = null;
	static GraphQL graphql = null;
	
	// Model
	
	static GraphQLObjectType forecastingType = newObject()
			.name("Forecasting")
			.description("Predicción")
			
			.field(newFieldDefinition()
					.name("predictedValue")
					.description("Valor predicho")
					.type(new GraphQLNonNull(GraphQLFloat))
					.dataFetcher(new DataFetcher() {
						@Override
						public Double get(DataFetchingEnvironment environment) {
							return (Double) ((Map<String, Object>) environment.getSource()).get("predictedValue");
						}
					}))
			
			.build();
	
	// Queries
	static GraphQLObjectType queryType = newObject()
			.name("QueryType")
			
			.field(newFieldDefinition()
					.name("forecasting")
					.type(forecastingType)
					.argument(newArgument()
							.name("currentValue")
							.type(new GraphQLNonNull(GraphQLFloat)))
					.argument(newArgument()
							.name("timeInMillis")
							.type(GraphQLLong))
					.dataFetcher(new DataFetcher() {
						@Override
						public Map<String, Object> get(DataFetchingEnvironment environment) {
							
							Double currentValue = environment.getArgument("currentValue");
							Long timeInMillis = environment.getArgument("timeInMillis");
							Double predictedValue = HTMProvider.forecast(currentValue, timeInMillis, false);
							Map<String, Object> result = new HashMap<String, Object>();
							result.put("predictedValue", predictedValue);
							return result;
						}
					}))
			
			.build();
	
	// Mutations
	static GraphQLObjectType mutationType = newObject()
			.name("MutationType")
			
			.field(newFieldDefinition()
					.name("resetMemory")
					.type(new GraphQLNonNull(GraphQLString))
					.dataFetcher(new DataFetcher() {
						@Override
						public String get(DataFetchingEnvironment environment) {
							HTMProvider.initialize();
							return "OK";
						}
					}))
			
			.build();
	
	// Schema
	public static GraphQLSchema getSchema() {
		if (schema == null) {
			schema = GraphQLSchema.newSchema()
					.query(queryType)
					.mutation(mutationType)
					.build();
		}
		return schema;
	}
	
	public static GraphQL getGraphQL() {
		if (graphql == null) {
		    graphql = new GraphQL(getSchema());
		}
		return graphql;
	}
	
	public static void execute(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		
		String query = request.getParam("query");
		
		String operationName = null;
		HashMap<String, Object> variables = new HashMap<String, Object>();
		
		if (request.method() == HttpMethod.POST) {
			try {
				JsonObject body = new JsonObject(routingContext.getBodyAsString().replaceAll("\\n", "").replaceAll("\\t", ""));
				if (query == null) {
					query = body.getString("query");
				}
				operationName = body.getString("operationName");
				JsonObject variablesJson = body.getJsonObject("variables");
				if (variablesJson != null) {
					Iterator<String> fieldNames = variablesJson.fieldNames().iterator();
					while (fieldNames.hasNext()) {
						String fieldName = fieldNames.next();
						variables.put(fieldName, variablesJson.getValue(fieldName));
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	    JsonObject result = new JsonObject();
	    
	    try {
	    	ExecutionResult executionResult = null;
	    	if (operationName != null) {
		    	executionResult = getGraphQL().execute(query, operationName, (Object) null, variables);
	    	}
	    	else {
		    	executionResult = getGraphQL().execute(query, (Object) null, variables);
	    	}
		    
		    if (executionResult.getErrors().size() > 0) {
		    	result.put("errors", executionResult.getErrors());
		    }
		    result.put("data", executionResult.getData());
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    response.headers().add("Content-Type", "application/json");
	    response.end(result.encode());
	}
}
