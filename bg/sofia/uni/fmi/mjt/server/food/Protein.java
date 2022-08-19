package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Objects;

public class Protein {

    private final double value;

    public Protein(double value) {
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

        Protein that = (Protein) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Protein{" +
                "value=" + value +
                '}';
    }
}
