package ru.mshuvalov.asyncapi.gradle;

import ru.mshuvalov.asyncapi.core.AsyncApiCodeGenerator;
import ru.mshuvalov.asyncapi.core.GenerationRequest;
import java.io.IOException;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

@CacheableTask
public abstract class GenerateAsyncApiTask extends DefaultTask {
    @InputFile @PathSensitive(PathSensitivity.RELATIVE) public abstract RegularFileProperty getSpecification();
    @Input public abstract Property<String> getBasePackage();
    @Input public abstract Property<String> getTransport();
    @OutputDirectory public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() throws IOException {
        new AsyncApiCodeGenerator().generate(new GenerationRequest(
            getSpecification().get().getAsFile().toPath(),
            getOutputDirectory().get().getAsFile().toPath(),
            getBasePackage().get(), getTransport().get()));
    }
}
