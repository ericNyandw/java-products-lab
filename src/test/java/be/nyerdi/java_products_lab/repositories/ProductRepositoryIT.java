package be.nyerdi.java_products_lab.repositories;


import be.nyerdi.java_products_lab.entities.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Crucial pour Testcontainers
public class ProductRepositoryIT  {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product product = Product.builder()
                .label("Écran 4K")
                .price(399.99)
                .description("Dalle IPS")
                .build();

        // When
        Product saved = productRepository.saveAndFlush(product);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldHaveInitialDataFromSqlFile() {
        // 1. On récupère tous les produits via le repository
        long count = productRepository.count();

        // 2. On vérifie qu'il y en a exactement 2 (ceux de la data.sql)
        assertThat(count).isEqualTo(3);

        // 3. (Optionnel) On vérifie qu'un des produits est bien notre clavier
        boolean hasClavier = productRepository.findAll().stream()
                .anyMatch(p -> p.getLabel().equals("Clavier Mécanique"));
        assertThat(hasClavier).isTrue();
    }

}
