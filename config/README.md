# Java formatting

The project follows the basic [Oracle Java code conventions](https://www.oracle.com/java/technologies/javase/codeconventions-content.html): four spaces per indentation level, eight spaces for continuation indentation, same-line opening braces, and an 80-column wrapping target. Long tokens and preserved comments can exceed that target.

The Maven formatter uses Eclipse JDT with the checked-in `java-formatter.xml` profile. This is a project adaptation, not an official Oracle formatter. It formats Java sources and tests without organizing imports or rewriting comments.

Apply formatting:

```sh
docker compose exec -T app mvn formatter:format
```

Check formatting:

```sh
docker compose exec -T app mvn formatter:validate
```

VS Code uses the installed `Oracle.oracle-java` extension and formats Java on save. The `.vscode/settings.json` file configures equivalent basic preferences. Oracle Java and Eclipse JDT use different formatting engines, so their wrapping decisions may differ; Maven validation defines the repository format.
