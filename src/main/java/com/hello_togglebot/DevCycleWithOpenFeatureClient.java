package com.hello_togglebot;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.OpenFeatureAPI;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import com.devcycle.sdk.server.local.api.DevCycleLocalClient;
import com.devcycle.sdk.server.local.model.DevCycleLocalOptions;

// Initialize a single DevCycle client and OpenFeature client to be used across the application
public class DevCycleWithOpenFeatureClient {
	public static final DevCycleWithOpenFeatureClient instance = new DevCycleWithOpenFeatureClient();
	private final DevCycleLocalClient devCycleClient;
	private final Client openFeatureClient;

	private DevCycleWithOpenFeatureClient() {
		this.devCycleClient = initializeDevCycle();
		this.openFeatureClient = initializeOpenFeatureClient(this.devCycleClient);
	}

	private static DevCycleLocalClient initializeDevCycle() {
		Dotenv dotenv = Dotenv.configure().load();
		String devcycleSdkKey = dotenv.get("DEVCYCLE_SERVER_SDK_KEY");

		if (devcycleSdkKey == null) {
			throw new DotenvException("DEVCYCLE_SERVER_SDK_KEY should be defined in the .env file");
		}

		DevCycleLocalOptions options = DevCycleLocalOptions.builder()
            .configPollingIntervalMS(5000)
            .eventFlushIntervalMS(5000)
            .build();

		return new DevCycleLocalClient(devcycleSdkKey, options);
    }

	private static Client initializeOpenFeatureClient(DevCycleLocalClient devCycleClient) {
		// Set the provider into the OpenFeature API
		OpenFeatureAPI api = OpenFeatureAPI.getInstance();
		api.setProvider(devCycleClient.getOpenFeatureProvider());

		// Get the OpenFeature client
		Client openFeatureClient = api.getClient();

		return openFeatureClient;
	}

	public DevCycleLocalClient getDevCycleClient() {
        return devCycleClient;
    }

	public Client getOpenFeatureClient() { return openFeatureClient; }

	public static DevCycleWithOpenFeatureClient getInstance() { return instance; }

}
