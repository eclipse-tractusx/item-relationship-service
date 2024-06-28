[[Back to main README](../README.md)]

# Feature files

Cucumber Feature Files are located in  
`irs-cucumber-tests/src/test/resources/org/eclipse/tractusx/irs/cucumber/features`

They are grouped by feature and tagged with e.g. `@INTEGRATION_TEST`.

# Execute cucumber tests with Maven

Be aware that you need to provide the following system variables before running it with Maven.

```
REGULAR_USER_API_KEY: {take value from vault}
ADMIN_USER_API_KEY: {take value from vault}
ISSUE_FILTER: {desired issue filter}
```

If the test result should be uploaded to
the [irs cucumber report collection](https://reports.cucumber.io/report-collections/b82bcadd-0d19-41c4-ae1a-c623e259c36f),
the `CUCUMBER_PUBLISH_TOKEN` has to be added as environment variable as well:

```
CUCUMBER_PUBLISH_TOKEN: {take value from vault}
```

After providing these variables in you system, you can execute maven as follows.

```bash
 mvn clean verify -P cucumber -Dgroups="$ISSUE_FILTER" -pl irs-cucumber-tests -am
```

# Execute cucumber tests with IntelliJ

If you want to execute the cucumber tests within IntelliJ, you have to edit you Cucumber Java configuration Template.
To do this, head to your run configurations on the top right. Click "Edit configurations..." -> "Edit configuration
Templates...".
Next, head to "Cucumber Java" and paste below variables into the "Environment Variables" Section. Add the correct values
for the missing ones after.

````
REGULAR_USER_API_KEY: {take value from vault}
ADMIN_USER_API_KEY: {take value from vault}
ISSUE_FILTER: {desired issue filter}
````

Now you should be able to use IntelliJ to run Cucumber tests by just clicking run on the desired test.
