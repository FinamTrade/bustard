package ru.finam.GradlePlugins

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * User: svanin
 * Date: 11.04.13
 * Time: 18:19
 */
class AnnotationProcessingPluginExtension {
    Boolean compileBustardClasses = false
    Boolean compileBustardTestClasses = false
}

class AnnotationProcessingPlugin implements Plugin<Project> {

    String outputBustardDir = 'src/generated/bustard/java'
    String outputDaggerDir = 'src/generated/dagger/java'
    String outputDir = 'src/generated/'

    File getDestination(project, destination) {
        project.file(destination)
    }

    void apply(Project project) {
        project.extensions.create("annotationProcessing", AnnotationProcessingPluginExtension)

        project.task('processAnnotations') << {
            _processAnnotations(project.annotationProcessing.compileBustardClasses, project)
        }

        project.task('testProcessAnnotations') << {
            _processAnnotations(project.annotationProcessing.compileBustardTestClasses, project)
        }

        project.compileJava.dependsOn project.processAnnotations
        project.compileTestJava.dependsOn project.testProcessAnnotations
    }

    void _processAnnotations(boolean compileBustardClasses, Project project) {
        project.delete outputDir
        def gen = getDestination(project, outputBustardDir)
        gen.mkdirs()
        def gen2 = getDestination(project, outputDaggerDir)
        gen2.mkdirs()

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.main.java.each { File file ->
                src(path: file.getParent())
            }
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'ru.finam.bustard.codegen.ListenerProcessor,ru.finam.bustard.codegen.InjectChannelProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: gen)
            if (!compileBustardClasses) compilerarg(value: '-Anobustards=true')
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.main.java.each { File file ->
                src(path: file.getParent())
            }
            src(path: gen)
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'dagger.internal.codegen.InjectProcessor,dagger.internal.codegen.ProvidesProcessor,dagger.internal.codegen.FullGraphProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: gen2)
        }

        project.sourceSets {
            main {
                java {
                    srcDir outputBustardDir
                    srcDir outputDaggerDir
                }
                resources {
                    srcDir outputBustardDir
                    include '**/*.bustard'
                }
            }
        }

        [project.compileJava, project.compileTestJava]*.options.collect { options ->
            options.encoding = 'UTF-8'
            options.compilerArgs = ['-proc:none']
        }
    }
}