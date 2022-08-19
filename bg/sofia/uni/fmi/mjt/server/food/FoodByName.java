package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Arrays;

public class FoodByName {

    private final Food[] foods;

    public FoodByName(Food[] foods) {
        this.foods = foods;
    }

    public Food[] getFoods() {
        return foods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FoodByName that = (FoodByName) o;
        return Arrays.equals(foods, that.foods);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(foods);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Food food : foods) {
            sb.append(food).append(System.lineSeparator());
        }

        return sb.toString();
    }
}
