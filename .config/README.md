
[[Back to main IRS README](../README.md)]

# .config Folder

This folder contains configuration, rules and suppression files for code quality check tools etc.

| File / Folder               | Description                                                                               | Further information                                                                                                    |
|-----------------------------|-------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| spectral                    |                                                                                           |                                                                                                                        | 
| .trivyignore                | Ignore file for [Trivy](https://trivy.dev/) to configure which false positives to ignore. | [Trivy ignorefile documentation](https://aquasecurity.github.io/trivy/v0.49/docs/configuration/filtering/#trivyignore) |
| checkstyle.xml              | [Checkstyle](https://checkstyle.sourceforge.io) configuration.                            | [Checkstyle configuration](https://checkstyle.sourceforge.io/config.html)                                              |
| checkstyle-suppressions.xml | [Checkstyle](https://checkstyle.sourceforge.io/) suppressions.                            | [Checkstyle suppressions documentation](https://checkstyle.sourceforge.io/filters/suppressionfilter.html)              |
| irs.header                  | Copyright header definition for the checkstyle module RegexpHeader.                       | see checkstyle.xml file                                                                                                |
| owasp-suppressions.xml      | [OWASP dependendy check](https://owasp.org/www-project-dependency-check/) suppressions.   | [OWASP suppressions documentation](https://jeremylong.github.io/DependencyCheck/general/suppression.html)              |
| pmd-rules.xml               | [PMD Source Code Analyzer](https://pmd.github.io/) rules.                                 | [PMD rules documentation](https://pmd.github.io/pmd/pmd_userdocs_making_rulesets.html)                                 |

 
