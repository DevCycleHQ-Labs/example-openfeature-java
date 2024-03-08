package com.hello_togglebot;

import java.util.Map;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.devcycle.sdk.server.common.model.DevCycleUser;
import com.devcycle.sdk.server.local.api.DevCycleLocalClient;
import com.devcycle.sdk.server.common.model.Feature;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.Client;


public class VariationLogger {
	private final DevCycleWithOpenFeatureClient ofDVCClient = DevCycleWithOpenFeatureClient.getInstance();

	// Since this is used outside a request context, we define a service user context.
	// This can contain properties unique to this service, and allows you to target
	// services in the same way you would target app users.
	private final DevCycleUser serviceUser = DevCycleUser.builder()
			.userId("api-service")
			.build();
	private final ImmutableContext context = new ImmutableContext("api-service");

	public VariationLogger() {}

	public void start() {
		System.out.printf("\n");
		renderFrame(0);
	}

	private void renderFrame(int idx) {
		Client openFeatureClient = ofDVCClient.getOpenFeatureClient();
		DevCycleLocalClient devcycleClient = ofDVCClient.getDevCycleClient();
		Map<String, Feature> features = devcycleClient.allFeatures(serviceUser);
		String variationName = features.containsKey("hello-togglebot")
			? features.get("hello-togglebot").getVariationName()
			: "Default";


		Boolean wink = openFeatureClient.getBooleanValue("togglebot-wink", false, context);
		String speed = openFeatureClient.getStringValue("togglebot-speed", "off", context);

		String[] spinChars = {"◜", "◠", "◝", "◞", "◡", "◟"};
		if (speed.equals("slow")) {
			spinChars = new String[] {"◜", "◝", "◞", "◟"};
		}

		idx = (idx + 1) % spinChars.length;
		String spinner = speed.equals("off") ? "○" : spinChars[idx];
		String face = wink ? "(- ‿ ○)" : "(○ ‿ ○)";

		String frame = String.format("%s Serving variation: %s %s", spinner, variationName, face);
		String color = speed.equals("surprise") ? "rainbow" : "blue";

		writeToConsole(frame, color);

		int timeout = speed.equals("off") || speed.equals("slow") ? 500 : 100;
		final int nextIdx = idx;
		CompletableFuture.delayedExecutor(timeout, TimeUnit.MILLISECONDS).execute(() -> {
			renderFrame(nextIdx);
		});
	}

	private String addColor(String frame, String color) {
		Map<String, String> colors = Map.of(
			"red", "\033[31m",
			"green", "\033[32m",
			"yellow", "\033[33m",
			"blue", "\033[34m",
			"magenta", "\033[35m",
			"rainbow", "\033[38;5;" + (System.nanoTime() % 230) + "m"
		);
		String endChar = "\033[0m";

		return colors.containsKey(color)
			? colors.get(color) + frame + endChar
			: frame;
	}

	private void writeToConsole(String frame, String color) {
		frame = addColor(frame, color);
		System.out.printf(
			String.format("\033[2K  %s\r", frame)
		);
	}

}