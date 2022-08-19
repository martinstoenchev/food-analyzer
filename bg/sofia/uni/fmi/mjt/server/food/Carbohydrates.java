package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Objects;

public class Carbohydrates {

    private final double value;

    public Carbohydrates(double value) {
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

        Carbohydrates that = (Carbohydrates) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Carbohydrates{" +
                "value=" + value +
                '}';
    }
}
