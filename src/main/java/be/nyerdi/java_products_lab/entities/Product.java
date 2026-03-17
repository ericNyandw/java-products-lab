package be.nyerdi.java_products_lab.entities;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder            // Design Pattern Builder (Pratique pour les tests)
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String label;
    @Column(nullable = false)
    private Double price;
    private String description;

}

