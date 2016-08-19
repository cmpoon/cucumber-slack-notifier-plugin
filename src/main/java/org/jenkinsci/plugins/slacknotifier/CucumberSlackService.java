package org.jenkinsci.plugins.slacknotifier;

import hudson.FilePath;
import hudson.model.Run;
import jenkins.model.JenkinsLocationConfiguration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

public class CucumberSlackService {

	private static final Logger LOG = Logger.getLogger(CucumberSlackService.class.getName());

	private final String webhookUrl;
	private final String jenkinsUrl;

	public CucumberSlackService(String webhookUrl) {
		this.webhookUrl = webhookUrl;
		this.jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();
	}

	public void sendCucumberReportToSlack(Run<?,?> build, FilePath workspace, String json, String channel, String extra, boolean hideSuccessfulResults) {
		LOG.info("Posting cucumber reports to slack for '" + build.getParent().getDisplayName() + "'");
		LOG.info("Cucumber reports are in '" + workspace + "'");

		JsonElement jsonElement = getResultFileAsJsonElement(workspace, json);
		SlackClient client = new SlackClient(webhookUrl, jenkinsUrl, channel, hideSuccessfulResults);

		client.postToSlack(jsonElement, build, extra);
	}

	private JsonElement getResultFileAsJsonElement(FilePath workspace, String json) {
		final FilePath jsonPath = new FilePath(workspace, json);
		LOG.info("file path: " + jsonPath);
		
		final Gson gson = new Gson();
		try {
			final JsonReader jsonReader = new JsonReader(new InputStreamReader(jsonPath.read()));
			return gson.fromJson(jsonReader, JsonElement.class);
		} catch (IOException e) {
			LOG.severe("Exception occurred while reading test results: " + e);
			throw new RuntimeException("Exception occurred while reading test results", e);
		} catch (InterruptedException e) {
			LOG.severe("Exception occurred while reading test results: " + e);
			throw new RuntimeException("Exception occurred while reading test results", e);
		}
	}
}
