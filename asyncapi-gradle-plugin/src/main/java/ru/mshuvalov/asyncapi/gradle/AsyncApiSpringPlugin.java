package ru.mshuvalov.asyncapi.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSetContainer;

public final class AsyncApiSpringPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        AsyncApiSpringExtension extension = project.getExtensions().create("asyncApiSpring", AsyncApiSpringExtension.class);
        extension.getBasePackage().convention("generated.asyncapi");
        extension.getTransport().convention("kafka");
        extension.getSpecification().convention(project.getLayout().getProjectDirectory().file("src/main/asyncapi/asyncapi.yaml"));

        var task = project.getTasks().register("generateAsyncApi", GenerateAsyncApiTask.class, t -> {
            t.setGroup("code generation");
            t.setDescription("Generates Java Spring messaging sources from AsyncAPI.");
            t.getSpecification().set(extension.getSpecification());
            t.getBasePackage().set(extension.getBasePackage());
            t.getTransport().set(extension.getTransport());
            t.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/sources/asyncapi/java/main"));
        });
        project.getPluginManager().withPlugin("java", ignored -> {
            SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
            sets.named("main", main -> main.getJava().srcDir(task));
            project.getTasks().named("compileJava").configure(t -> t.dependsOn(task));
        });
    }
}
