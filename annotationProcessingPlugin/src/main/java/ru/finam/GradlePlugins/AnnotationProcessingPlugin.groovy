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
    Boolean compileBustardTestClasses = true

    String outputDirPrefix = 'src/generated/'
    String outputDirForTestPrefix = 'src/test/generated/'
}

class AnnotationProcessingPlugin implements Plugin<Project> {
    private project

    private String bustardDir = 'bustard/java'
    private String daggerDir = 'dagger/java'
    def outputBustardDir
    def outputDaggerDir
    def outputBustardTestDir
    def outputDaggerTestDir


    void apply(Project project) {
        this.project = project
        configureBuildScript()
        project.extensions.create("annotationProcessing", AnnotationProcessingPluginExtension)
    }

    private void configureBuildScript() {
        project.gradle.taskGraph.whenReady { taskGraph ->
            configureJavaPlugin()
            configureDependencies()
        }
    }

    private void configureDependencies() {
        project.sourceSets {
            main {
                java {
//                    srcDir(project.annotationProcessing.outputDirPrefix + bustardDir)
//                    srcDir(project.annotationProcessing.outputDirPrefix + daggerDir)
                }
                resources {
                    srcDir(project.annotationProcessing.outputDirPrefix + bustardDir)
                    include '**/*.bustard'
                }
            }
            test {
                java {
                    srcDir(project.annotationProcessing.outputDirForTestPrefix + bustardDir)
                    srcDir(project.annotationProcessing.outputDirForTestPrefix + daggerDir)
                }
                resources {
                    srcDir(project.annotationProcessing.outputDirForTestPrefix + bustardDir)
                    include '**/*.bustard'
                }
            }
        }

        [project.compileJava, project.compileTestJava]*.options.collect { options ->
            options.encoding = 'UTF-8'
            options.compilerArgs = ['-proc:none']
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

    void _processSourceAnnotations(boolean compileBustardClasses, Project project) {
        project.delete project.annotationProcessing.outputDirPrefix
        outputBustardDir = project.file(project.annotationProcessing.outputDirPrefix + bustardDir)
        outputBustardDir.mkdirs()
        outputDaggerDir = project.file(project.annotationProcessing.outputDirPrefix + daggerDir)
        outputDaggerDir.mkdirs()

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
            compilerarg(value: outputBustardDir)
            if (!compileBustardClasses) compilerarg(value: '-Anobustards=true')
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.main.java.each { File file ->
                src(path: file.getParent())
            }
            src(path: outputBustardDir)
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'dagger.internal.codegen.InjectProcessor,dagger.internal.codegen.ProvidesProcessor,dagger.internal.codegen.FullGraphProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: outputDaggerDir)
        }
    }

    void _processTestAnnotations(boolean compileBustardTestClasses, Project project) {
        project.delete project.annotationProcessing.outputDirForTestPrefix
        outputBustardTestDir = project.file(project.annotationProcessing.outputDirForTestPrefix + bustardDir)
        outputBustardTestDir.mkdirs()
        outputDaggerTestDir = project.file(project.annotationProcessing.outputDirForTestPrefix + daggerDir)
        outputDaggerTestDir.mkdirs()

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.test.java.each { File file ->
                src(path: file.getParent())
            }
            project.sourceSets.main.java.each { File file ->
                src(path: file.getParent())
            }
            project.sourceSets.main.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.compileClasspath.addToAntBuilder(ant, 'classpath')
            project.sourceSets.main.output.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.output.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'ru.finam.bustard.codegen.ListenerProcessor,ru.finam.bustard.codegen.InjectChannelProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: outputBustardTestDir)
            if (!compileBustardTestClasses) compilerarg(value: '-Anobustards=true')
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.test.java.each { File file ->
                src(path: file.getParent())
            }
            project.sourceSets.main.java.each { File file ->
                src(path: file.getParent())
            }
            src(path: outputBustardTestDir)
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
            compilerarg(value: outputDaggerTestDir)
        }
        project.sourceSets {
            main {
                java {
                    srcDir(project.annotationProcessing.outputDirPrefix + bustardDir)
                    srcDir(project.annotationProcessing.outputDirPrefix + daggerDir)
                }
            }
        }
    }
}
