package ru.mshuvalov.asyncapi.core.sourcegenerator.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import ru.mshuvalov.asyncapi.core.sourcegenerator.SourceGenerator;
import ru.mshuvalov.asyncapi.core.sourcegenerator.SourceGeneratorInputData;

import java.io.StringWriter;

public class MustacheSourceGenerator implements SourceGenerator {

    @Override
    public String generate(SourceGeneratorInputData data) {
        var template = MustacheTemplateResolver.resolve(data);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(template);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, data);
        return writer.toString();
    }
}
