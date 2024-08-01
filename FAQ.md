[[Back to main IRS README](README.md)]

# IRS FAQ

## Documentation and Getting Help

-----
**Q: Where do I find documentation that helps me understand the big picture?**

A: The following resources will help to understand the big picture:

- [README - Introduction](README.md#introduction)
- [More information describing the big picture](https://eclipse-tractusx.github.io/docs-kits/kits/Data%20Chain%20Kit/Adoption%20View%20Data%20Chain%20Kit/)
  including the two main flows IRS Iterative and IRS Recursive and involved components and services.

Further information can be found in the IRS Architecture Documentation, for example:

- [Architecture Documentation - Business Context](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_business_context)
- [Architecture Documentation - Structure](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_structure)
- [Architecture Documentation - Building block view](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_building_block_view)
- [Architecture Documentation - Runtime view](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_runtime_view)
    - [Iterative Flow](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_irs_iterative)
    - [Recursive Flow](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_irs_recursive)

-----
**Q: Where do I find the main documentation?**

A: See [Item Relationship Service Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/).


-----
**Q: Is there a glossary?**

A: Yes,
see [Item Relationship Service Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_glossary).


-----
**Q: Where do I get help?**

A: Please read the documentation and tests first.
If that does not answer your questions, ask the developers (see [CONTACT](CONTACT.md)).

## Limitations, Bugs and Vulnerabilities

-----
**Q: Are there any known issues and limitation?**

A: See [Known Issues and Limitations](README.md#known-issues-and-limitations).


-----
**Q: Where can I report a vulnerability?**

A: See [SECURITY](SECURITY.md#reporting-a-vulnerability).


-----
**Q: Where can I report a bug?**

A: Normal bugs may be reported as [public github issues](https://github.com/orgs/eclipse-tractusx/projects/8/views/10).
For reporting vulnerabilities see [SECURITY](SECURITY.md#reporting-a-vulnerability) please.


-----
**Q: Can I autoscale irs across multiple pods? What do I have to do?**

A: at the current state it is technically possible to use Kubernetes autoscaling by setting "autoscaling.enabled=true"
in [values.yaml](charts/item-relationship-service/values.yaml).
This will spin up additional IRS Pods once the target load is hit.  
However, due to IRS being a stateful application, it is not very useful and would require additional implementation
efforts to balance IRS workload across multiple instances.

## Usage

-----
**Q: Where do I find information concerning compatibility of dependencies?**

A: See [COMPATIBILITY-MATRIX](COMPATIBILITY_MATRIX.md).

## Contributing

-----
**Q: How can I contribute?**

A: See [CONTRIBUTING](CONTRIBUTING.md).


-----
**Q: How do I create a release?**

A: See [CONTRIBUTING](CONTRIBUTING.md#create-a-release).


-----
**Q: How do I format the code?**

A: See [CONTRIBUTING](CONTRIBUTING.md#code-formatting).

## Testing

-----
**Q: Where can I find more information concerning the type of tests and used technologies and libraries?**

A: See [README - Tests](README.md#tests).

-----
**Q: Where do I find test data?**

A: Testdata is located under [local/testing/testdata](local/testing/testdata).
For more information see the [test data README](local/testing/testdata/README.md).

## Monitoring

-----
**Q: Where do I find information concerning monitoring?**

See [README - Monitoring](README.md#monitoring).

## Troubleshooting

-----
**Q: How to reset / purge the minio database?**

A: To reset the MinIO database, simply uninstall the item-relationship-service helm chart and install it again by
running

```
helm uninstall your-irs-app-name
helm install -f your-values.yaml your-irs-app-name irs/item-relationship-service
```

-----
**Q: How can a job that has hung up (no reaction) be canceled?**

A: To cancel a running job, call the Job cancellation endpoint `PUT` `/irs/jobs/<job-id>`. This will stop all further
processes from executing and sets the Job state to "CANCELED".

-----
**Q: Which resources are recommended for which use case? (cores/RAM)**

A: The item-relationship-service helm chart has default resources set to 1.5Gi memory with a minimum of 250m
and a limit of 750m CPU.  
Depending on your use-case, this might not be sufficient when requesting a lot of jobs in parallel or jobs which go very
deep in the chain.  
Based on our testing, a CPU limit of 1.5 is capable of handling >40 Jobs with a depth of ~3 simultaneously without any
issues.  
In case you run into OutOfMemory Exceptions, increase the memory request and limit.
