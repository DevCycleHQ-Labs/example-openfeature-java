package com.hello_togglebot;

import java.util.Map;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.MutableContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.WebRequest;

import com.devcycle.sdk.server.local.api.DevCycleLocalClient;
import com.devcycle.sdk.server.common.model.DevCycleUser;
import com.devcycle.sdk.server.common.model.BaseVariable;

@Controller
public class HelloController {
	private DevCycleLocalClient devcycleClient;
	private DevCycleUser user;

	public HelloController() {}

	@GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody()
	public String index(WebRequest request) {
		DevCycleWithOpenFeatureClient ofDVCClient = DevCycleWithOpenFeatureClient.getInstance();
		Client openFeatureClient = ofDVCClient.getOpenFeatureClient();

		DevCycleUser user = (DevCycleUser) request.getAttribute("user", WebRequest.SCOPE_REQUEST);
		if (user == null)  {
			throw new RuntimeException("User not found");
		}

        MutableContext context = new MutableContext(user.getUserId());
		String step = openFeatureClient.getStringValue("example-text", "default", context);

		String header;
		String body;
		switch(step) {
			case "step-1":
				header = "Welcome to DevCycle's example app.";
				body = "If you got here through the onboarding flow, just follow the instructions to change and create new Variations and see how the app reacts to new Variable values.";
				break;
			case "step-2":
				header = "Great! You've taken the first step in exploring DevCycle.";
				body = "You've successfully toggled your very first Variation. You are now serving a different value to your users and you can see how the example app has reacted to this change. Next, go ahead and create a whole new Variation to see what else is possible in this app.";
				break;
			case "step-3":
				header = "You're getting the hang of things.";
				body = "By creating a new Variation with new Variable values and toggling it on for all users, you've already explored the fundamental concepts within DevCycle. There's still so much more to the platform, so go ahead and complete the onboarding flow and play around with the feature that controls this example in your dashboard.";
				break;
			default:
				header = "Welcome to DevCycle's example app.";
				body = "If you got to the example app on your own, follow our README guide to create the Feature and Variables you need to control this app in DevCycle.";
		}

		return String.format("<h2>%s</h2><p>%s</p><p><a href=\"/variables\">All Variables</a></p>", header, body);
	}

	@GetMapping("/variables")
	@ResponseBody()
	public Map<String, BaseVariable> variables(WebRequest request) {
		DevCycleWithOpenFeatureClient ofDVCClient = DevCycleWithOpenFeatureClient.getInstance();
		DevCycleLocalClient devcycleClient = ofDVCClient.getDevCycleClient();
		DevCycleUser user = (DevCycleUser) request.getAttribute("user", WebRequest.SCOPE_REQUEST);
		Map<String, BaseVariable> variables = devcycleClient.allVariables(user);

		return variables;
	}

}