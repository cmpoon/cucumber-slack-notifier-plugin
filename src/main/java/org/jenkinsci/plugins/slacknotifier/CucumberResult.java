package org.jenkinsci.plugins.slacknotifier;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hudson.model.Run;

public class CucumberResult {
	final List<FeatureResult> featureResults;
	final int passPercentage;
	final int totalScenarios;
	
	public CucumberResult(List<FeatureResult> featureResults, int totalScenarios, int passPercentage) {
		this.featureResults = featureResults;
		this.totalScenarios = totalScenarios;
		this.passPercentage = passPercentage;
	}
	
	public int getPassPercentage() {
		return this.passPercentage;
	}
	
	public int getTotalFeatures() {
		return this.featureResults.size();
	}
	
	public int getTotalScenarios() {
		return this.totalScenarios;
	}
	
	public List<FeatureResult> getFeatureResults() {
		return this.featureResults;
	}
	
	public String toSlackMessage(final Run<?,?> build, final String channel, final String jenkinsUrl, final String extra) {
		final JsonObject json = new JsonObject();
		json.addProperty("channel", "#" + channel);
		addCaption(json, build, jenkinsUrl, extra);
		json.add("fields", getFields(build, jenkinsUrl));

		if (getPassPercentage() == 100) {
			addColourAndIcon(json, "good", ":thumbsup:");
		} else if (getPassPercentage() >= 98) {
			addColourAndIcon(json, "warning", ":hand:");
		} else {
			addColourAndIcon(json, "danger", ":thumbsdown:");
		}

		json.addProperty("username", build.getParent().getDisplayName());
		return json.toString();
	}

	private String getJenkinsHyperlink(final String jenkinsUrl, final Run<?,?> build) {
		StringBuilder s = new StringBuilder();
		s.append(jenkinsUrl);
		if (!jenkinsUrl.trim().endsWith("/")) {
			s.append("/");
		}
		s.append(build.getUrl());
		/**
		Incompatible with Jenkins pipeline

		s.append(jobName);
		s.append("/");
		s.append(buildNumber);
		s.append("/");
		*/
		return s.toString();
	}
	
	public String toHeader(final Run<?,?> build , final String jenkinsUrl, final String extra) {
		StringBuilder s = new StringBuilder();
		if (StringUtils.isNotEmpty(extra)) {
			s.append(extra);
		}
		s.append("Features: ");
		s.append(getTotalFeatures());
		s.append(", Scenarios: ");
		s.append(getTotalScenarios());
		s.append(", Build: <");
		s.append(getJenkinsHyperlink(jenkinsUrl, build));
		s.append("cucumber-html-reports/|");
		s.append(build.getNumber());
		s.append(">");
		return s.toString();
	}
	
	private void addCaption(final JsonObject json, final Run<?,?> build, final String jenkinsUrl, final String extra) {
		json.addProperty("pretext", toHeader(build, jenkinsUrl, extra));
	}
	
	private void addColourAndIcon(JsonObject json, String good, String value) {
		json.addProperty("color", good);
		json.addProperty("icon_emoji", value);
	}

	private JsonArray getFields(final Run<?,?> build , final String jenkinsUrl) {
		final String hyperLink = getJenkinsHyperlink(jenkinsUrl, build) + "cucumber-html-reports/";
		final JsonArray fields = new JsonArray();
		fields.add(shortTitle("Features"));
		fields.add(shortTitle("Pass %"));
		for (FeatureResult feature : getFeatureResults()) {
			final String featureDisplayName = feature.getDisplayName();
			final String featureFileName = feature.getFeatureUri();
			fields.add(shortObject("<" + hyperLink + featureFileName + "|" + featureDisplayName + ">"));
			fields.add(shortObject(feature.getPassPercentage() + " %"));
		}
		fields.add(shortObject("-------------------------------"));
		fields.add(shortObject("-------"));
		fields.add(shortObject("Total Passed"));
		fields.add(shortObject(getPassPercentage() + " %"));
		return fields;
	}

	
	private JsonObject shortObject(final String value) {
		JsonObject obj = new JsonObject();
		obj.addProperty("value", value);
		obj.addProperty("short", true);
		return obj;
	}

	private JsonObject shortTitle(final String title) {
		JsonObject obj = new JsonObject();
		obj.addProperty("title", title);
		obj.addProperty("short", true);
		return obj;
	}
}