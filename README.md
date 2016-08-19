# cucumber-slack-notifier-plugin
Jenkins plugin to push summarised Cucumber reports to Slack via Jenkins proxy support. --CMPoon

## Installation

`mvn package`

Then upload the .hpi in `target/` to Jenkins -> Manage Jenkins -> Manage Plugin -> Advanced.

## Configuration

The following options are available:

### Global Configuration

* webHookEndpoint: Slack integration - Incoming Webhook URL - First register integration then regsiter the hook URL here.

### Build Configuration

* channel - slack channel to post result to (without #)
* json - json file containing the cucumber results, e.g. target/cucumber.json

TODO: add a screen shot here 
