package org.jenkinsci.plugins.slacknotifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;

import hudson.model.FreeStyleProject;
import hudson.model.Run;

public class SlackClientTest {

	public @Rule JenkinsRule j = new JenkinsRule();

	@Test
	public void canGenerateFullSuccessfulSlackMessage() throws Exception {
		
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		Run<?,?> build = p.getBuildByNumber(1);
				
		JsonElement element = loadTestResultFile("successful-result.json");
		assertNotNull(element);
		CucumberResult result = new SlackClient("http://slack.com/", "http://jenkins:8080/", "channel", false).processResults(element);
		assertNotNull(result);
		assertNotNull(result.getFeatureResults());
		assertEquals(8, result.getTotalScenarios());
		assertEquals(8, result.getTotalFeatures());
		assertEquals(100, result.getPassPercentage());
		
		String slackMessage = result.toSlackMessage(build, "channel", "http://jenkins:8080/", null);
		assertNotNull(slackMessage);
		assertTrue(slackMessage.contains("<http://jenkins:8080/job/test-job/1/cucumber-html-reports/validate_gerrit_home_page-feature.html|validate gerrit home page>"));
	}

	@Test
	public void canGenerateMinimalSuccessfulSlackMessage() throws Exception {
		
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		Run<?,?> build = p.getBuildByNumber(1);
		
		JsonElement element = loadTestResultFile("successful-result.json");
		assertNotNull(element);
		CucumberResult result = new SlackClient("http://slack.com/", "http://jenkins:8080/", "channel", true).processResults(element);
		assertNotNull(result);
		assertNotNull(result.getFeatureResults());
		assertEquals(8, result.getTotalScenarios());
		assertEquals(0, result.getTotalFeatures());
		assertEquals(100, result.getPassPercentage());

		String slackMessage = result.toSlackMessage(build, "channel", "http://jenkins:8080/", null);
		assertNotNull(slackMessage);
	}
	
	@Test
	public void canGenerateFullFailedSlackMessage() throws FileNotFoundException {
		JsonElement element = loadTestResultFile("failed-result.json");
		assertNotNull(element);
		CucumberResult result = new SlackClient("http://slack.com/", "http://jenkins:8080/", "channel", false).processResults(element);
		assertNotNull(result);
		assertNotNull(result.getFeatureResults());
		assertEquals(8, result.getTotalScenarios());
		assertEquals(8, result.getTotalFeatures());
		assertEquals(87, result.getPassPercentage());
	}

	@Test
	public void canGenerateMinimalFailedSlackMessage() throws FileNotFoundException {
		JsonElement element = loadTestResultFile("failed-result.json");
		assertNotNull(element);
		CucumberResult result = new SlackClient("http://slack.com/", "http://jenkins:8080/", "channel", true).processResults(element);
		assertNotNull(result);
		assertNotNull(result.getFeatureResults());
		assertEquals(8, result.getTotalScenarios());
		assertEquals(1, result.getTotalFeatures());
		assertEquals(87, result.getPassPercentage());
	}
	
	@Test
	public void canGenerateGoodMessage() throws Exception {
		
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		Run<?,?> build = p.getBuildByNumber(1);
		
		String slackMessage = successfulResult().toSlackMessage(build, "channel", "http://jenkins:8080/", null);
		assertNotNull(slackMessage);
		assertTrue(slackMessage.contains("good"));
	}

	@Test
	public void canGenerateMarginalMessage() throws Exception {
		
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		Run<?,?> build = p.getBuildByNumber(1);
		
		String slackMessage = marginalResult().toSlackMessage(build, "channel", "http://jenkins:8080/", null);
		assertNotNull(slackMessage);
		assertTrue(slackMessage.contains("warning"));
	}

	@Test
	public void canGenerateBadMessage() throws Exception {
		
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		Run<?,?> build = p.getBuildByNumber(1);
		
		String slackMessage = badResult().toSlackMessage(build, "channel", "http://jenkins:8080/", null);
		assertNotNull(slackMessage);
		assertTrue(slackMessage.contains("danger"));
	}

	private JsonElement loadTestResultFile(String filename) throws FileNotFoundException {
		File result = new File("src/test/resources", filename);
		assertNotNull(result);
		assertTrue(result.exists());
		return getResultFileAsJsonElement(new FileInputStream(result));
	}

	private JsonElement getResultFileAsJsonElement(InputStream stream) {
		final Gson gson = new Gson();
		final JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));
		return gson.fromJson(jsonReader, JsonElement.class);
	}
	
	private CucumberResult successfulResult() {
		return new CucumberResult(Arrays.asList(new FeatureResult("Dummy Test", 100)),1,100);
	}
	
	private CucumberResult badResult() {
		return new CucumberResult(Arrays.asList(new FeatureResult("Dummy Test", 0)),1,0);
	}
	
	private CucumberResult marginalResult() {
		return new CucumberResult(Arrays.asList(new FeatureResult("Dummy Test", 99)),1,99);
	}
}
