package ru.finam.GradlePlugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import groovy.io.FileType

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
    private boolean isAndroidMain
    private boolean isAndroidLib
    def mainSrc
    def outputBustardDir
    def outputDaggerDir
    def outputBustardTestDir
    def outputDaggerTestDir

    void apply(Project project) {
        this.project = project
        isAndroidMain = project.getPlugins().hasPlugin('android')
        isAndroidLib = project.getPlugins().hasPlugin('android-library')
        project.extensions.create("annotationProcessing", AnnotationProcessingPluginExtension)
        if (isAndroidMain || isAndroidLib) {
            configureAndroidSources()
        }
        project.gradle.taskGraph.whenReady { taskGraph ->
            configureBuildScript()
        }
    }

    private void configureAndroidSources() {
        project.annotationProcessing.outputDirPrefix = 'generated/'    //TODO: move this to buildscript somehow
        project.android.sourceSets {
            main {
                java {
                    srcDir(project.annotationProcessing.outputDirPrefix + bustardDir)
                    srcDir(project.annotationProcessing.outputDirPrefix + daggerDir)
                }
            }
        }
    }

    private void configureJavaSources() {
        project.sourceSets {
            main {
                java {
                    srcDir(project.annotationProcessing.outputDirPrefix + bustardDir)
                    srcDir(project.annotationProcessing.outputDirPrefix + daggerDir)
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
    }

    private void configureBuildScript() {
        if (isAndroidMain || isAndroidLib) {
            mainSrc = project.android.sourceSets.main
            if (isAndroidMain) {
                def compileTasks = project.android.applicationVariants.collect { it.javaCompile.name }
                def allTasks = project.gradle.getTaskGraph().getAllTasks().collect { it.name }
                def activeCompileTasks = allTasks.intersect(compileTasks)

                activeCompileTasks.each {
                    project.getTasksByName(it, false).each {
                        it.doFirst {
                            _processSourceAnnotations(project.annotationProcessing.compileBustardClasses, project, (it.name.contains('FT') ? 'FinamTrade' : 'WhoTrades'))
                        }
                        [project[it.name]]*.options.collect { options ->
                            options.encoding = 'UTF-8'
                            options.compilerArgs = ['-proc:none']
                        }
                    }
                }
            } else {
                _processSourceAnnotations(project.annotationProcessing.compileBustardClasses, project, "")
                [project.compileRelease]*.options.collect { options ->
                    options.encoding = 'UTF-8'
                    options.compilerArgs = ['-proc:none']
                }
            }
        } else {
            mainSrc = project.sourceSets.main
            project.compileJava {
                doFirst {
                    _processSourceAnnotations(project.annotationProcessing.compileBustardClasses, project, "")
                }
            }
            project.compileTestJava {
                doFirst {
                    _processTestAnnotations(project.annotationProcessing.compileBustardTestClasses, project)
                }
            }

            configureJavaSources()

            [project.compileJava, project.compileTestJava]*.options.collect { options ->
                options.encoding = 'UTF-8'
                options.compilerArgs = ['-proc:none']
            }
        }
    }

    void _processSourceAnnotations(boolean compileBustardClasses, Project project, flavorPath) {
        project.delete project.annotationProcessing.outputDirPrefix
        outputBustardDir = project.file(project.annotationProcessing.outputDirPrefix + bustardDir)
        outputDaggerDir = project.file(project.annotationProcessing.outputDirPrefix + daggerDir)
        outputBustardDir.mkdirs()
        outputDaggerDir.mkdirs()

        def cp = []
        if (isAndroidMain || isAndroidLib) {
            cp += project.file('build/source')
        }
        if (isAndroidMain) {
            cp += project.file(flavorPath + '/src')
        }
        //traversing android libraries because android library artifacts are AARs
        ['compile', 'provided'].each { configuration ->
            project.configurations[configuration].dependencies.
                    findAll { Object o -> o instanceof ProjectDependency }.
                    each { ProjectDependency dependency ->
                        def plugins = dependency.dependencyProject.getPlugins()
                        if (plugins.hasPlugin('android-library')) {
                            //TODO: refactor to get compiled classes instead sources
                            cp.addAll(dependency.dependencyProject.android.sourceSets.main.java.srcDirs)
                        }
                    }
        }
        cp = cp.findAll { File f ->
            f.exists()
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            cp.each {
                src(path: it)
            }
            mainSrc.java.srcDirs.each { File file ->
                src(path: file)
            }
            ['compile', 'provided'].each { configuration ->
                project.configurations[configuration].addToAntBuilder(ant, 'classpath')
            }
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'ru.finam.bustard.codegen.ListenerProcessor,ru.finam.bustard.codegen.InjectChannelProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: outputBustardDir)
            compilerarg(value: '-Apath=' + ((File) outputBustardDir).absolutePath)
            if (!compileBustardClasses) compilerarg(value: '-Anobustards=true')
        }

        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            src(path: outputBustardDir)
            cp.each {
                src(path: it)
            }
            mainSrc.java.srcDirs.each { File file ->
                src(path: file)
            }
            ['compile', 'provided'].each { configuration ->
                project.configurations[configuration].addToAntBuilder(ant, 'classpath')
            }
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
            project.sourceSets.test.java.srcDirs.each { File file ->
                src(path: file)
            }
            mainSrc.java.srcDirs.each { File file ->
                if (file != project.file(project.annotationProcessing.outputDirPrefix + daggerDir) && (file != project.file(project.annotationProcessing.outputDirPrefix + bustardDir))) {
                    src(path: file)
                }
            }
            project.configurations.compile.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.compileClasspath.addToAntBuilder(ant, 'classpath')
            mainSrc.output.addToAntBuilder(ant, 'classpath')
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

        println '_processTestAnnotations-dagger'
        project.ant.javac(includeantruntime: false, encoding: 'UTF-8') {
            project.sourceSets.test.java.srcDirs.each { File file ->
                src(path: file)
            }
            mainSrc.java.srcDirs.each { File file ->
                if (file != project.file(project.annotationProcessing.outputDirPrefix + daggerDir) && (file != project.file(project.annotationProcessing.outputDirPrefix + bustardDir))) {
                    src(path: file)
                }
            }
            src(path: outputBustardTestDir)
            project.configurations.compile.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.compileClasspath.addToAntBuilder(ant, 'classpath')
            mainSrc.output.addToAntBuilder(ant, 'classpath')
            project.sourceSets.test.allJava.addToAntBuilder(ant, 'classpath')
            compilerarg(value: '-source')
            compilerarg(value: '1.6')
            compilerarg(value: "-processor")
            compilerarg(value: 'dagger.internal.codegen.InjectProcessor,dagger.internal.codegen.ProvidesProcessor,dagger.internal.codegen.FullGraphProcessor')
            compilerarg(value: '-proc:only')
            compilerarg(value: '-s')
            compilerarg(value: outputDaggerTestDir)
        }
    }
}
