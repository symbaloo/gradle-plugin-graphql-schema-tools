package com.symbaloo.graphql.tools

import com.apollographql.apollo.api.internal.QueryDocumentMinifier
import com.apollographql.apollo.compiler.OperationIdGenerator
import com.apollographql.apollo.compiler.PackageNameProvider
import com.apollographql.apollo.compiler.operationoutput.OperationDescriptor
import com.apollographql.apollo.compiler.operationoutput.toJson
import com.apollographql.apollo.compiler.parser.GraphQLDocumentParser
import com.apollographql.apollo.compiler.parser.Schema
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * A task to just generate an OperationOutput.json file
 */
@CacheableTask
abstract class OperationOutputTask : DefaultTask() {

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:PathSensitive(value = PathSensitivity.ABSOLUTE)
    abstract val graphqlFiles: ConfigurableFileTree

    @get:InputFile
    @get:PathSensitive(value = PathSensitivity.ABSOLUTE)
    abstract val schemaFile: RegularFileProperty

    @get:OutputFile
    abstract val destination: RegularFileProperty

    @TaskAction
    fun run() {
        val realSchemaFile = schemaFile.get().asFile
        val schema = Schema.invoke(realSchemaFile)

        val files = graphqlFiles.files
        val codeGenerationIR = GraphQLDocumentParser(schema, DummyPackageNameProvider).parse(files)

        val operationIdGenerator = OperationIdGenerator.Sha256()

        val operationOutput = codeGenerationIR.operations.associate {
            val minimizedSource = QueryDocumentMinifier.minify(it.sourceWithFragments)
            operationIdGenerator.apply(minimizedSource, it.filePath) to OperationDescriptor(
                it.operationName,
                minimizedSource
            )
        }

        val operationOutputFile = destination.get().asFile
        operationOutputFile.parentFile.mkdirs()
        operationOutputFile.writeText(operationOutput.toJson("    "))
    }

    private object DummyPackageNameProvider : PackageNameProvider {
        override val fragmentsPackageName: String = "com.symbaloo.tmp.fragments"
        override val typesPackageName: String = "com.symbaloo.tmp.types"
        override fun operationPackageName(filePath: String): String = "com.symbaloo.tmp.types.$filePath"
    }
}
