# Contributing to Eclipse Tractus-X

Thanks for your interest in this project.

# Table of Contents
1. [Project description](#project-description)
2. [Developer resources](#developer-resources)
3. [Eclipse Development Process](#eclipse-development-process)
4. [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
5. [Code of Conduct](#code-of-conduct)
6. [General contribution to the project](#general-contribution-to-the-project)
7. [Contributing as a Consultant](#contributing-as-a-consultant)
8. [Contributing as a Developer](#contributing-as-a-developer)
9. [Contact](#contact)

## Project description

The companies involved want to increase the automotive industry's
competitiveness, improve efficiency through industry-specific cooperation and
accelerate company processes through standardization and access to information
and data. A special focus is also on SMEs, whose active participation is of
central importance for the network's success. That is why Catena-X has been
conceived from the outset as an open network with solutions ready for SMEs,
where these companies will be able to participate quickly and with little IT
infrastructure investment. Tractus-X is meant to be the PoC project of the
Catena-X alliance focusing on parts traceability.

* https://projects.eclipse.org/projects/automotive.tractusx
* https://github.com/eclipse-tractusx/item-relationship-service
* https://github.com/catenax-ng/tx-item-relationship-service

## Developer resources

Information regarding source code management, builds, coding standards, and
more.

* https://projects.eclipse.org/projects/automotive.tractusx/developer

The project maintains the source code repositories in the following GitHub organization:

* https://github.com/eclipse-tractusx/

## Eclipse Development Process

This Eclipse Foundation open project is governed by the Eclipse Foundation
Development Process and operates under the terms of the Eclipse IP Policy.

* https://eclipse.org/projects/dev_process
* https://www.eclipse.org/org/documents/Eclipse_IP_Policy.pdf

## Eclipse Contributor Agreement

In order to be able to contribute to Eclipse Foundation projects you must
electronically sign the Eclipse Contributor Agreement (ECA).

* http://www.eclipse.org/legal/ECA.php

The ECA provides the Eclipse Foundation with a permanent record that you agree
that each of your contributions will comply with the commitments documented in
the Developer Certificate of Origin (DCO). Having an ECA on file associated with
the email address matching the "Author" field of your contribution's Git commits
fulfills the DCO's requirement that you sign-off on your contributions.

For more information, please see the Eclipse Committer Handbook:
https://www.eclipse.org/projects/handbook/#resources-commit

## Code of Conduct

See [CODE_OF_CONDUCT](CODE_OF_CONDUCT.md).

## Eclipse Dependency License Check

In case of new dependencies or version updates, it might be necessary to have the new library checked and accepted by the Eclipse foundation. Do create new tickets for this, you can use this command:
```
mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.iplab.token=$ECLIPSE_DASH_TOKEN -Ddash.projectId=automotive.tractusx --batch-mode -DskipTests
```

For more information on the tool and how to acquire the token, check https://github.com/eclipse/dash-licenses.

## General contribution to the project

General contributions e.g. contributions to improve documentation are welcome.
If you need ideas for contributions, you can check the following links:
- [open documentation stories](https://github.com/orgs/eclipse-tractusx/projects/8/views/4?filterQuery=label%3Adocumentation++status%3Ainbox%2Cbacklog)
- [discussion page concerning documentation improvements](https://github.com/eclipse-tractusx/item-relationship-service/discussions/407)


## Contributing as a Consultant

### Conceptual work and specification guidelines
1. The prerequisite for a concept is always a github issue that defines the business value and the acceptance criteria that are to be implemented with the concept
2. Copy and rename directory /docs/#000-concept-name-template /docs/#<DDD>-<target-name>
3. Copy the template [/docs/concept/TEMPLATE_Concept.md](docs/concept/TEMPLATE_Concept.md) into new directory `/docs/#<DDD>-<target-name>`.

### Diagrams
[PlantUML](https://plantuml.com/) and [Mermaid](https://mermaid.js.org/) is used for conceptual work.


#### PlantUML 
default skinparam for plantUml diagrams 
````
@startuml
skinparam monochrome true
skinparam shadowing false
skinparam linetype ortho
skinparam defaultFontName "Architects daughter"
autonumber "<b>[000]"

@enduml
````

####  Mermaid
Default header for mermaid sequence diagrams
````
sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
 ````   

## Contributing as a Developer

### Commit messages

The commit messages have to match a pattern in the form of:  
``< type >(scope):[<Ticket_ID>] < description >``  
where type is: `build|chore|ci|docs|feat|fix|perf|refactor|revert|style|test`

Example:  
``chore(api):[#123] some text``
``chore(api): some text``

Detailed pattern can be found here: [commit-msg](local/development/commit-msg)

#### How to use
```shell
cp local/development/commit-msg .git/hooks/commit-msg && chmod 500 .git/hooks/commit-msg
```

For further information please see https://github.com/hazcod/semantic-commit-hook

### Code formatting

#### Deprecated soon:
Please use the following code formatter: [.idea/codeStyles](.idea/codeStyles)

#### Upcoming change (not available until whole project base will be formatted to new standard):  
Google Java Format will be used as code format standard.  
Please install `google-java-format` plugin and edit customer VM options (for IntelliJ `Help → Edit Custom VM Options...`) and paste following configuration: 
```
-Xmx4096m
--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```
The plugin will be disabled by default. To enable it in the current project, go to `File→Settings...→google-java-format` Settings (or `IntelliJ IDEA→Preferences...→Other Settings→google-java-format` Settings on macOS) and check the Enable google-java-format checkbox. (A notification will be presented when you first open a project offering to do this for you.)

More info:  
https://github.com/google/google-java-format/blob/master/README.md#intellij-jre-config

### Create a release

1. Choose a release version. Use semantic versioning! Create a respective branch e.g. `chore/prepare-release-2.6.1`.
2. Add release notes for new version in [CHANGELOG.md](CHANGELOG.md) and [charts/irs-helm/CHANGELOG.md](charts/irs-helm/CHANGELOG.md) (e.g. https://github.com/catenax-ng/tx-item-relationship-service/pull/328)
3. Update [COMPATIBILITY_MATRIX.md](COMPATIBILITY_MATRIX.md) (see [catena-x-environments](https://github.com/catenax-ng/tx-item-relationship-service/tree/catena-x-environments/charts/irs-environments))
4. Create pull request and merge to main
5. Create Git tag for the desired release version `git tag x.x.x`
   (the irs-helm tag will be created by the github workflow based on the version in the irs-helm changelog) 
6. Push Git tag to repository `git push origin x.x.x`
7. Wait for release workflow to complete
8. Merge the automatically opened PR by github-actions bot
9. Create pull request to eclipse-tractusx
10. Notify about the release in IRS Matrix Chat. Template: 
   
   >   **IRS Release x.x.x**
   >
   >   IRS version x.x.x is released. 
   >
   >   https://github.com/eclipse-tractusx/item-relationship-service/releases/tag/x.x.x<br>
   >   https://github.com/eclipse-tractusx/item-relationship-service/releases/tag/irs-helm-y.y.y<br>
   >   **Full Changelog:** https://github.com/eclipse-tractusx/item-relationship-service/compare/w.w.w...x.x.x

   _(replace x.x.x with IRS version to release, y.y.y with IRS helm version to release and w.w.w with previous IRS version)_


## Contact

See [CONTACT](CONTACT.md)

