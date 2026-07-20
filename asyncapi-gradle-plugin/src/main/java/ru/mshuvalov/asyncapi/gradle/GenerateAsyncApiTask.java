package ru.mshuvalov.asyncapi.gradle;

import ru.mshuvalov.asyncapi.core.AsyncApiCodeGenerator;
import ru.mshuvalov.asyncapi.core.GenerationRequest;
import ru.mshuvalov.asyncapi.core.GenerationOptions;
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
    @Input public abstract Property<String> getKafkaPackage();
    @Input public abstract Property<Boolean> getGenerateModels();
    @Input public abstract Property<Boolean> getGenerateProducers();
    @Input public abstract Property<Boolean> getGenerateConsumers();
    @OutputDirectory public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() throws IOException {
        new AsyncApiCodeGenerator().generate(new GenerationRequest(
            getSpecification().get().getAsFile().toPath(),
            getOutputDirectory().get().getAsFile().toPath(),
            new GenerationOptions(getKafkaPackage().get(), getGenerateModels().get(),
                getGenerateProducers().get(), getGenerateConsumers().get())));
    }
}
