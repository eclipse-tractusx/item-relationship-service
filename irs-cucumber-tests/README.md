# Download feature files

In order to download latest feature files from jira execute following command

``curl -s --show-error -w "%{http_code}" -u $JIRA_USERNAME:$JIRA_PASSWORD "https://jira.catena-x.net/rest/raven/1.0/export/test?filter=11349&fz=true" -o features.zip``

replace username and password with Your onw jira credentials

after that extract features.zip to irs-cucumber-tests/src/test/resources/features

# Execute cucumber tests with Maven

Be aware that you need to provide the following system variables before running it with Maven.

````
REGULAR_USER_API_KEY: {take value from vault}
ADMIN_USER_API_KEY: {take value from vault}
ISSUE_FILTER: {desired issue filter}
````

After providing these variables in you system, you can execute maven as follows.

``mvn --batch-mode clean install -pl irs-cucumber-tests -D"cucumber.filter.tags"="$ISSUE_FILTER"``

# Execute cucumber tests with IntelliJ

If you want to execute the cucumber tests within IntelliJ, you have to edit you Cucumber Java configuration Template.
To do this, head to your run configurations on the top right. Click "Edit configurations..." -> "Edit configuration Templates...".
Next, head to "Cucumber Java" and paste below variables into the "Environment Variables" Section. Add the correct values for the missing ones after.

````
REGULAR_USER_API_KEY: {take value from vault}
ADMIN_USER_API_KEY: {take value from vault}
ISSUE_FILTER: {desired issue filter}
````

Now you should be able to use IntelliJ to run Cucumber tests by just clicking run on the desired test.
