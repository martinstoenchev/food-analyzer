package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Objects;

public class Calories {

    private final double value;

    public Calories(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Calories that = (Calories) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Calories{" +
                "value=" + value +
                '}';
    }
}
