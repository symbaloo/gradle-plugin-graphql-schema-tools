# GraphQL Schema Tools Gradle Plugin

Various Gradle Tasks to do GraphQL stuff.

## SchemaIntrospectionTask

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
val graphqlSchemaIntrospection by tasks.registering(com.symbaloo.graphql.tools.SchemaIntrospectionTask::class) {
    schemaFile.set(file("src/schema/schema.gql"))
    destination.set(file("build/graphql/main/graphql/schema/schema.json"))
}
```

## `OperationOutputTask` for Persisted Queries

Generate an `OperationOutput.json` file at build time from a schema and `.graphql` query files.

This can be used server-side when only accepting
[Persisted Queries](https://www.apollographql.com/docs/apollo-server/performance/apq/), but only allowing a certain, at
compile time, set of queries.

```kotlin
val operationOutput by tasks.registering(com.symbaloo.graphql.tools.OperationOutputTask::class) {
    val jsonFile = graphqlSchemaIntrospection.get().destination.asFile.get()
    schemaFile.set(jsonFile)
    graphqlFiles.from("src/queries")
    graphqlFiles.include("**/*.graphql")
    destination.set(file("build/graphql/main/graphql/OperationOutput.json"))
    dependsOn(graphqlSchemaIntrospection)
}
```
