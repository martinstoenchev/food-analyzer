package bg.sofia.uni.fmi.mjt.server.food;

import java.util.Objects;

public class Food {

    private final String fdcId;
    private final String description;
    private final String gtinUpc;
    private final String ingredients;
    private final LabelNutrients labelNutrients;

    public Food(String fdcId, String description, String gtinUpc, String ingredients, LabelNutrients labelNutrients) {
        this.fdcId = fdcId;
        this.description = description;
        this.gtinUpc = gtinUpc;
        this.ingredients = ingredients;
        this.labelNutrients = labelNutrients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Food food = (Food) o;
        return Objects.equals(fdcId, food.fdcId) && Objects.equals(description, food.description) && Objects.equals(gtinUpc, food.gtinUpc) && Objects.equals(ingredients, food.ingredients) && Objects.equals(labelNutrients, food.labelNutrients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fdcId, description, gtinUpc, ingredients, labelNutrients);
    }

    @Override
    public String toString() {
        return "Food{" +
                "fdcId='" + fdcId + '\'' +
                ", description='" + description + '\'' +
                ", gtinUpc='" + gtinUpc + '\'' +
                ", ingredients='" + ingredients + '\'' +
                ", labelNutrients=" + labelNutrients +
                '}';
    }
}
