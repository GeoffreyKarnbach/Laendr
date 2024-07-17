package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import lombok.AllArgsConstructor;

import java.util.AbstractMap;
import java.util.Random;
import java.util.function.Function;

@AllArgsConstructor(staticName = "of")
public class RandomComputed extends Node {
    private Function<Random, String> function;

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        builder.append(function.apply(random));
    }
}
