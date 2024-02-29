[[Back to main README](../README.md)]

## IRS Documentation Sources

The IRS documentation is build and published by the GitHub workflow [Lint and Publish documentation](../.github/workflows/publish-documentation.yaml) automatically.

### Building the Documentation Locally

In order to build the documentation locally for testing adjustments to the documentation
the script [build-docs-locally.sh](build-docs-locally.sh) can be used.


### Source Layout

| Folder                     | Description                                                                                                                                                                                                                                      |
|----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| concept                    | Concepts including a [template for concepts](concept/TEMPLATE_Concept.md).                                                                                                                                                                       |
| src/api                    | See [README](src/api/README.md).                                                                                                                                                                                                                 |
| src/diagram-replacer       | Scripts that create images from [PlantUML](https://plantuml.com) diagrams and replace them in the documentation, when the documentation is published via [GitHub workflow](../.github/workflows/publish-documentation.yaml).                     |
| src/docs                   | The sources for the [Item Relationship Service Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/)                                                                                                                |
| src/docs/administration    | The sources for the [IRS Administration Guide](https://eclipse-tractusx.github.io/item-relationship-service/docs/administration/administration-guide.html).                                                                                      |
| src/docs/api-specification | The source for the [IRS REST API Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/api-specification/api-specification.html) published by the [github workflow](../.github/workflows/publish-documentation.yaml). |
| src/docs/arc42             | The sources for the [IRS Architecture Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42) (based on the [arc42](https://www.arc42.de/) template).                                                            |
| src/docs/security          | Documentation relevant for security assessment.                                                                                                                                                                                                  |
| src/post-processing        | Some post-processing used by the [GitHub workflow](../.github/workflows/publish-documentation.yaml).                                                                                                                                             |
| src/uml-diagrams           | UML diagrams for the documentation                                                                                                                                                                                                               |
| target                     | The output directory for the documentation build.                                                                                                                                                                                                |
