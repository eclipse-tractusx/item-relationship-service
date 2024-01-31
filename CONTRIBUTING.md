# Contributing to Eclipse Tractus-X

Thanks for your interest in this project.

# Table of Contents
1. [Project description](#project-description)
2. [Developer resources](#developer-resources)
3. [Eclipse Development Process](#eclipse-development-process)
4. [Eclipse Contributor Agreement](#eclipse-contributor-agreement)
5. [General contribution to the project](#general-contribution-to-the-project)
6. [Contributing as a Consultant](#contributing-as-a-consultant)
7. [Contributing as a Developer](#contributing-as-a-developer)
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

## Eclipse Dependency License Check

In case of new dependencies or version updates, it might be necessary to have the new library checked and accepted by the Eclipse foundation. Do create new tickets for this, you can use this command:
```
mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.iplab.token=$ECLIPSE_DASH_TOKEN -Ddash.projectId=automotive.tractusx --batch-mode -DskipTests
```

For more information on the tool and how to acquire the token, check https://github.com/eclipse/dash-licenses.

## General contribution to the project


## Contributing as a Consultant

### Conceptual work and specification guidelines
1. The prerequisite for a concept is always a github issue that defines the business value and the acceptance criteria that are to be implemented with the concept
2. Copy and rename directory /docs/#000-concept-name-template /docs/#<DDD>-<target-name>
3. Copy file /docs/Concept_TEMPLATE.md into new directory  /docs/#<DDD>-<target-name>

### Diagrams
PlantUML and Mermaid is used for conceptual work.
https://mermaid.js.org/
https://plantuml.com/


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
Please use the following code formatter: [.idea/codeStyles](.idea/codeStyles)


## Contact

Contact the project developers via the project's "dev" list.

* https://accounts.eclipse.org/mailing-list/tractusx-dev
* Eclipse Matrix Chat https://chat.eclipse.org/#/room/#tractusx-irs:matrix.eclipse.org
