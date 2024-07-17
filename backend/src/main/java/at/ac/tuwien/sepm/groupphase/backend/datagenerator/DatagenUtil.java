package at.ac.tuwien.sepm.groupphase.backend.datagenerator;

import java.util.List;
import java.util.Random;

public class DatagenUtil {

    public static <T> T randomElement(Random random, T[] array) {
        if (array.length == 0) {
            return null;
        } else {
            return array[random.nextInt(array.length)];
        }
    }

    public static <T> T randomElement(Random random, List<T> list) {
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(random.nextInt(list.size()));
        }
    }

    public static <T> List<T> randomSubset(int subsetLength, List<T> list) {
        if (list.size() < subsetLength) {
            return null;
        }
        return list.subList(0, subsetLength);
    }
}
