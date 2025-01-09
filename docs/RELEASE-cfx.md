
# Release Guide for Cofinity-X

## General release conventions / guidelines

Please find [Release Guidelines Upstream](https://github.com/Cofinity-X/item-relationship-service/blob/main/CONTRIBUTING.md#create-a-release)

### Prerequisite:

- Before releasing IRS, it is required to check if the edc client library needs to be released through the [EDC Client Library Release](#edc-client-library-release) steps

### EDC Client Library Release

1) Check if the irs-registry-client version has a -SNAPSHOT suffix:  [IRS Repo](https://github.com/Cofinity-X/item-relationship-service/blob/main/pom.xml)
2) Click on the [Update irs-registry-client Version workflow](https://github.com/Cofinity-X/item-relationship-service/actions/workflows/update-registry-library.yaml).
3) Select "Run workflow" select the type of version increment major, minor or patch (Can be adjusted on generated PR branch). Check the box to remove the snapshot. Click on "Run".
4) A pull request (name: Update irs-registry-client to "Version") will be generated in which you have to make sure the irs lib version is now correct (change it manually if necessary).
5) Merge the generated Pull request
7) The GitHub action [Upload to Github Packages ](https://github.com/Cofinity-X/item-relationship-service/actions/workflows/cfx-maven-deploy-github-packages.yaml) will automatically release the irs-registry-client library with the new version defined in step 4

### IRS Release process

1) Decide which to which version the release irs will be incremented. Following shows example for releasing a version 1.0.0-cfx-1
2) Create and checkout release branch /release/1.0.0-cfx-1. The name must always be exactly `/release/<releaseVersion>-cfx-<version>`.
3) Only on changes: [EDC Client Library Release](#edc-client-library-release)
4) Edit changelog: Align the new version (1.0.0-cfx-1) with the changes and add a new UNRELEASED section
5) Update the [Compatability Matrix](https://github.com/Cofinity-X/item-relationship-service/blob/main/COMPATIBILITY_MATRIX.md) with a new entry for the release version
6) Push onto /release/1.0.0-cfx-1
9) Open releases page: https://github.com/Cofinity-X/item-relationship-service/releases
10) Draft a new release
11) On dropdown choose a tag - use the version 1.0.0-cfx-1 (Create new tag will appear - select it)
12) On dropdown target use your /release/1.0.0-cfx-1
13) Title = Version of app -> 1.0.0-cfx-1
14) Description = Changelog Content of app
15) Verify that GitHub action [Release](https://github.com/Cofinity-X/item-relationship-service/actions/workflows/cofinity-docker-image.yml) generation has been triggered
18) Merge release branch into main (when merging make sure to restore release branch since it should stay)
