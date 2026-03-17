package be.nyerdi.java_products_lab.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductDTO(
        Long id, // Optionnel lors de la création (POST)

        @NotBlank(message = "Le label est obligatoire")
        @Size(min = 3, max = 100)
        String label,

        @NotNull(message = "Le prix ne peut pas être nul")
        @Positive(message = "Le prix doit être supérieur à zéro")
        Double price,

        String description
) {}
