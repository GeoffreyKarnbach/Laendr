package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import java.util.AbstractMap;
import java.util.Random;
import java.util.stream.IntStream;

public class Shuffled extends Container {

    private Shuffled(Object... children) {
        super(children);
    }

    public static Shuffled of(Object... children) {
        return new Shuffled(children);
    }

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        if (children.isEmpty()) {
            return;
        }
        var indices = IntStream.range(0, children.size()).toArray();
        var remaining = indices.length;
        while (remaining > 0) {
            int next = random.nextInt(remaining);
            children.get(indices[next]).generate(builder, random, context);
            if (next < (remaining - 1)) {
                int tmp = indices[remaining - 1];
                indices[remaining - 1] = indices[next];
                indices[next] = tmp;
            }
            remaining--;
        }
    }
}
