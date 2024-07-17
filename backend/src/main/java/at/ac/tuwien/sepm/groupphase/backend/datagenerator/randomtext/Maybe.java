package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.AbstractMap;
import java.util.Random;

@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class Maybe extends Node {

    private double prob = 0.5;

    @NonNull
    private Node child;

    private Node otherChild = null;

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        if (random.nextDouble() < prob) {
            child.generate(builder, random, context);
        } else if (otherChild != null) {
            otherChild.generate(builder, random, context);
        }
    }
}
