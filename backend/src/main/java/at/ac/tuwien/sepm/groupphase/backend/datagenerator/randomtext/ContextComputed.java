package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import lombok.AllArgsConstructor;

import java.util.AbstractMap;
import java.util.Random;
import java.util.function.Function;

@AllArgsConstructor(staticName = "of")
public class ContextComputed<T> extends Node {

    private String attribute;
    private T defaultValue;
    private Function<T, String> function;

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        if (context.containsKey(attribute)) {
            builder.append(function.apply((T) context.get(attribute)));
        } else {
            builder.append(function.apply(defaultValue));
        }
    }
}
