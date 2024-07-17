package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Random;

public abstract class Node {

    public abstract void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context);

    public void generate(StringBuilder builder, Random random) {
        generate(builder, random, new HashMap<>());
    }

    public String generateString(Random random, AbstractMap<String, Object> context) {
        var builder = new StringBuilder();
        generate(builder, random, context);
        return builder.toString();
    }

    public String generateString(Random random) {
        return generateString(random, new HashMap<>());
    }

}
