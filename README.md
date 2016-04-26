# Jenkins changelog-appender
Plugin to append changes from git to a changelog file

This can create a basic changelog file in markdown format from the changesets of all builds since the last successful one.

## Install
To install this you need to clone and build the project.
```
mvn package
```
When it's done you find a `.hdi` file in the `target` folder which you can install with the **Advanced** tab in the **Jenkins Plugin Manager**.
