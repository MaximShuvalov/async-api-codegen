package ru.mshuvalov.asyncapi.core;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import ru.mshuvalov.asyncapi.core.model.TransportType;

import java.io.StringWriter;

public class MustacheSourceGenerator implements SourceGenerator {
    @Override
    public TransportType getTransport() {
        return TransportType.KAFKA;
    }

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
