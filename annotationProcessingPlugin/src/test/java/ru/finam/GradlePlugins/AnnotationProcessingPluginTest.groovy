package ru.finam.GradlePlugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.*

import static org.junit.Assert.assertTrue

/**
 * User: svanin
 * Date: 17.04.13
 * Time: 14:49
 */
class AnnotationProcessingPluginTest {
    @Test
    public void greeterPluginAddsAnnotationProcessingTaskToProject() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'annotationProcessing'

//        assertTrue(project.tasks.processAnnotations instanceof AnnotationProcessingTask)
    }
}
