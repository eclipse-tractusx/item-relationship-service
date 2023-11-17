# Contributing to Eclipse Tractus-X

Thanks for your interest in this project.

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

# Eclipse Dependency License Check

In case of new dependencies or version updates, it might be necessary to have the new library checked and accepted by the Eclipse foundation. Do create new tickets for this, you can use this command:
```
mvn org.eclipse.dash:license-tool-plugin:license-check -Ddash.iplab.token=$ECLIPSE_DASH_TOKEN -Ddash.projectId=automotive.tractusx --batch-mode -DskipTests
```

For more information on the tool and how to acquire the token, check https://github.com/eclipse/dash-licenses.

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

## Code formatting 
Please use the following code formatter: [.idea/codeStyles](.idea/codeStyles)

## Contact

Contact the project developers via the project's "dev" list.

* https://accounts.eclipse.org/mailing-list/tractusx-dev
