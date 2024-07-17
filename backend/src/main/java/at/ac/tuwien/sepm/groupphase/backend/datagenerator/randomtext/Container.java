package at.ac.tuwien.sepm.groupphase.backend.datagenerator.randomtext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Container extends Node {

    protected List<Node> children;

    private static Node mapChild(Object child) {
        if (child instanceof String) {
            return Exactly.of((String) child);
        } else if (child instanceof Node) {
            return (Node) child;
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected Container(Object... children) {
        this.children = Arrays.stream(children).map(Container::mapChild).collect(Collectors.toList());
    }

}
