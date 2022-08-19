package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Objects;

public class LabelNutrients {

    private final Calories calories;
    private final Protein protein;
    private final Fat fat;
    private final Carbohydrates carbohydrates;
    private final Fiber fiber;

    public LabelNutrients(Calories calories, Protein protein, Fat fat, Carbohydrates carbohydrates, Fiber fiber) {
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
        this.fiber = fiber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LabelNutrients that = (LabelNutrients) o;
        return Objects.equals(calories, that.calories) && Objects.equals(protein, that.protein) && Objects.equals(fat, that.fat) && Objects.equals(carbohydrates, that.carbohydrates) && Objects.equals(fiber, that.fiber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(calories, protein, fat, carbohydrates, fiber);
    }

    @Override
    public String toString() {
        return "LabelNutrients{" +
                "calories='" + calories + '\'' +
                ", protein='" + protein + '\'' +
                ", fat='" + fat + '\'' +
                ", carbohydrates='" + carbohydrates + '\'' +
                ", fiber='" + fiber + '\'' +
                '}';
    }
}
