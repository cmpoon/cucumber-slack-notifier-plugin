package org.jenkinsci.plugins.slacknotifier;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import hudson.model.Job;
import hudson.model.Run;

public class CucumberResultTest {

	public @Rule JenkinsRule j = new JenkinsRule();

	  
	@Test
	public void canGenerateHeader() throws Exception {
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		p.setU
		String header = successfulResult().toHeader(p.getBuildByNumber(1), "http://localhost:8080/", null);
		assertNotNull(header);
		assertTrue(header.contains("Features: 1"));
		assertTrue(header.contains("Scenarios: 1"));
		assertTrue(header.contains("Build: <http://localhost:8080/job/test-job/1/cucumber-html-reports/|1>"));
	}
	
	@Test
	public void canGenerateHeaderWithExtraInformation() throws IOException {
		FreeStyleProject p = j.createFreeStyleProject("test-job");
		p.scheduleBuild2(0);
		String header = successfulResult().toHeader(p.getBuildByNumber(1), "http://localhost:8080/", "Extra Content");
		assertNotNull(header);
		assertTrue(header.contains("Extra Content"));
		assertTrue(header.contains("Features: 1"));
		assertTrue(header.contains("Scenarios: 1"));
		assertTrue(header.contains("Build: <http://localhost:8080/job/test-job/1/cucumber-html-reports/|1>"));
	}
	
	private CucumberResult successfulResult() {
		return new CucumberResult(Arrays.asList(new FeatureResult("Dummy Test", 100)),1,100);
	}
}
