package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import java.util.AbstractMap;
import java.util.Random;

public class Sequence extends Container {

    private Sequence(Object... children) {
        super(children);
    }

    public static Sequence of(Object... children) {
        return new Sequence(children);
    }

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        for (var c : children) {
            c.generate(builder, random, context);
        }
    }
}
