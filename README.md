## GraphQL Introspection Gradle Plugin

Generates a `schema.json` file from a `schema.graphql` file
containing GraphQL introspection data.

This is useful when other code generators require the introspection
data, but you're using a `.graphql` schema first approach.

### Usage

In your `build.gradle.kts`:

Add this plugin to your `plugins` section

```kotlin
plugins {
    id("com.symbaloo.graphql.graphql-introspection").version("1.0.0-SNAPSHOT")
}
```

Then register a task:

```kotlin
val graphqlSchemaIntrospection by tasks.registering(SchemaIntrospectionTask::class) {
    schemaFile.set(file("src/schema/schema.gql"))
    destination.set(file("build/graphql/main/graphql/schema/schema.json"))
}
```
