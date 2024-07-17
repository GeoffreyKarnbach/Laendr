package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import at.ac.tuwien.sepm.groupphase.backend.datagenerator.DatagenUtil;

import java.util.AbstractMap;
import java.util.Random;

public class OneOf extends Container {

    private OneOf(Object... children) {
        super(children);
    }

    public static OneOf of(Object... children) {
        return new OneOf(children);
    }

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        DatagenUtil.randomElement(random, children).generate(builder, random, context);
    }
}
