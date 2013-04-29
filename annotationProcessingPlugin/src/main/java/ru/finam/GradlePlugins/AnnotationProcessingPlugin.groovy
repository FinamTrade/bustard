package ru.finam.GradlePlugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

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
    private project


    String outputBustardDir = 'src/generated/bustard/java'
    String outputDaggerDir = 'src/generated/dagger/java'
    String outputDir = 'src/generated/'

    String outputBustardDirForTest = 'src/test/generated/bustard/java'
    String outputDaggerDirForTest = 'src/test/generated/dagger/java'
    String outputDirForTest = 'src/test/generated/'

    File getDestination(project, destination) {
        project.file(destination)
    }

    void apply(Project project) {

        configurePluginConvention()
        configureBuildScript()

        project.extensions.create("annotationProcessing", AnnotationProcessingPluginExtension)

        /*project.compileJava {
            doFirst {
                _processSourceAnnotations(project.annotationProcessing.compileBustardClasses, project)
            }
        }

        project.compileTestJava {
            doFirst {
                _processTestAnnotations(project.annotationProcessing.compileBustardTestClasses, project)
            }
        }*/
    }

    private void configurePluginConvention() {
//        this.androidAnnotationsConvention = new JavaAnnotationsConvention()
//        project.convention.plugins.androidannotations = this.androidAnnotationsConvention
    }

    private void configureBuildScript() {
        project.plugins.apply(JavaPlugin.class)

        project.repositories {
            mavenCentral()
            maven {
                url 'https://oss.sonatype.org/content/repositories/snapshots/'
            }
        }

        project.configurations {
            annotationProcessor
            annotationsProcessor.extendsFrom(compile)
        }

        project.gradle.taskGraph.whenReady { taskGraph ->
            configureDependencies()
            configurePlugins()
        }
    }

    private void configureDependencies() {
        project.dependencies {
//            compile "com.googlecode.androidannotations:androidannotations:${androidAnnotationsConvention.androidAnnotationsVersion}:api"
//            androidannotations "com.googlecode.androidannotations:androidannotations:${androidAnnotationsConvention.androidAnnotationsVersion}"
        }
    }

    private void configurePlugins() {
        configureJavaPlugin()

        if (project.plugins.hasPlugin('idea')) {
            configureIdeaPlugin()
        }
    }

    private void configureJavaPlugin() {
        project.compileJava {
            doFirst {
                _processSourceAnnotations(project.annotationProcessing.compileBustardClasses, project)
            }
        }

        project.compileTestJava {
            doFirst {
                _processTestAnnotations(project.annotationProcessing.compileBustardTestClasses, project)
            }
        }
    }

    private void configureIdeaPlugin() {
        project.idea.module {
            scopes.PROVIDED.plus += project.configurations.androidannotations
        }

        project.idea.project.ipr.withXml { provider ->
            def compilerConfiguration = provider.node.component.find {
                it.@name == 'CompilerConfiguration'
            }

            def annotationProcessing = compilerConfiguration.annotationProcessing[0]
            annotationProcessing.@enabled = true
            annotationProcessing.@useClasspath = true
            annotationProcessing.appendNode(
                    'processor', [
                    name: 'com.googlecode.androidannotations.AndroidAnnotationProcessor',
                    options: ''
            ])
            annotationProcessing.appendNode(
                    'processModule', [
                    name: project.name,
                    generatedDirName: 'gen'
            ])
        }
    }

    void _processSourceAnnotations(boolean compileBustardClasses, Project project) {
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

    void _processTestAnnotations(boolean compileBustardClasses, Project project) {
        project.delete outputDirForTest
        def gen = getDestination(project, outputBustardDirForTest)
        gen.mkdirs()
        def gen2 = getDestination(project, outputDaggerDirForTest)
        gen2.mkdirs()

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.test.java.each { File file ->
                src(path: file.getParent())
            }
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.main.output.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'ru.finam.bustard.codegen.ListenerProcessor,ru.finam.bustard.codegen.InjectChannelProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: gen)
            if (!compileBustardClasses) compilerarg(value: '-Anobustards=true')
        }

        project.sourceSets {
            test {
                java {
                    srcDir outputBustardDirForTest
                }
                resources {
                    srcDir outputBustardDirForTest
                    include '**/*.bustard'
                }
            }
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.test.java.each { File file ->
                src(path: file.getParent())
            }
            src(path: gen)
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.main.output.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.allJava.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'dagger.internal.codegen.InjectProcessor,dagger.internal.codegen.ProvidesProcessor,dagger.internal.codegen.FullGraphProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: gen2)
        }


        project.sourceSets {
            test {
                java {
                    srcDir outputBustardDirForTest
                    srcDir outputDaggerDirForTest
                }
                resources {
                    srcDir outputBustardDirForTest
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
