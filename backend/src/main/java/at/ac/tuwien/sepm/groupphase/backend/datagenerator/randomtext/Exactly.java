package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.AbstractMap;
import java.util.Random;

@AllArgsConstructor(staticName = "of")
@RequiredArgsConstructor(staticName = "of")
public class Exactly extends Node {

    @NonNull
    private String value;
    private String contextKey = null;
    private Object contextValue = null;

    @Override
    public void generate(StringBuilder builder, Random random, AbstractMap<String, Object> context) {
        builder.append(value);
        if (contextKey != null) {
            context.put(contextKey, contextValue);
        }
    }
}
