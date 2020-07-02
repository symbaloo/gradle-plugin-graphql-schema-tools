package com.symbaloo.graphql.introspection

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import graphql.introspection.IntrospectionQuery
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import graphql.schema.idl.EchoingWiringFactory
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.ScalarInfo
import graphql.schema.idl.ScalarWiringEnvironment
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class SchemaIntrospectionTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputFile
    val schemaFile: RegularFileProperty = objects.fileProperty()

    @OutputFile
    val destination: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun run() {
        val inFile = schemaFile.get().asFile
        val mockRuntimeWiring = RuntimeWiring.newRuntimeWiring().wiringFactory(MockedWiringFactory()).build()
        val typeRegistry = SchemaParser().parse(inFile)
        val schema = SchemaGenerator().makeExecutableSchema(typeRegistry, mockRuntimeWiring)
        val graphQL = GraphQL.newGraphQL(schema).build()
        val executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY)

        logger.info("Writing GraphQL schema file from ${schemaFile.get()} to ${destination.get()}")

        val objectMapper = ObjectMapper()
        objectMapper.writeValue(destination.get().asFile, executionResult.toSpecification())
    }

    /**
     * https://github.com/graphql-java/graphql-java/blob/d4f9b63eb2f240c3d32aa6e9a135461836f40400/src/test/groovy/graphql/schema/idl/MockedWiringFactory.groovy
     */
    class MockedWiringFactory : EchoingWiringFactory() {
        override fun providesScalar(environment: ScalarWiringEnvironment): Boolean =
            !ScalarInfo.isGraphqlSpecifiedScalar(environment.scalarTypeDefinition.name)

        override fun getScalar(environment: ScalarWiringEnvironment): GraphQLScalarType =
            GraphQLScalarType.newScalar()
                .name(environment.scalarTypeDefinition.name)
                .coercing(object : Coercing<Any, Any> {
                    override fun parseValue(input: Any): Any =
                        throw UnsupportedOperationException("not implemented")

                    override fun parseLiteral(input: Any): Any =
                        throw UnsupportedOperationException("not implemented")

                    override fun serialize(dataFetcherResult: Any): Any =
                        throw UnsupportedOperationException("not implemented")
                })
                .build()
    }
}
