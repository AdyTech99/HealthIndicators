package io.github.adytech99.healthindicators.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.autogen.ListGroup;
import dev.isxander.yacl3.config.v2.api.autogen.OptionAccess;
import net.minecraft.entity.EntityType;

import java.lang.annotation.Annotation;
import java.util.List;

public final class EntitiesListGroup implements ListGroup.ValueFactory<String>, ListGroup.ControllerFactory<String> {


    @Override
    public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
        return StringControllerBuilder.create(option);
    }

    @Override
    public String provideNewValue() {
        return "";
    }
}