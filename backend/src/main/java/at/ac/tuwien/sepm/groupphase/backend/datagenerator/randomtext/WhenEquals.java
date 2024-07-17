package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.AbstractMap;
import java.util.Random;

@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class WhenEquals extends Node {

    @NonNull
    private String attribute;
    @NonNull
    private Object value;
    @NonNull
    private Node consequence;
    private Node otherwise = null;


    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        Object attr = context.get(attribute);
        if (attr != null && attr.equals(value)) {
            consequence.generate(builder, random, context);
        } else {
            if (otherwise != null) {
                otherwise.generate(builder, random, context);
            }
        }
    }
}
