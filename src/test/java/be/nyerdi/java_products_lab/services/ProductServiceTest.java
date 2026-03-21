package be.nyerdi.java_products_lab.services;

import be.nyerdi.java_products_lab.dtos.ProductDTO;
import be.nyerdi.java_products_lab.entities.Product;
import be.nyerdi.java_products_lab.mappers.ProductMapper;
import be.nyerdi.java_products_lab.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductServiceImpl productService; // On injecte les mocks ici

    @Test
    void shouldMapAndSaveProduct() {
        // Given
        ProductDTO dto = new ProductDTO(null, "Clavier", 50.0, "Meca");
        Product entity = new Product();

        // On définit le comportement des mocks (le "STUBBING")
        when(mapper.toEntity(any(ProductDTO.class))).thenReturn(entity);
        when(repository.save(any(Product.class))).thenReturn(entity);
        when(mapper.toDto(any(Product.class))).thenReturn(dto);

        // When
        ProductDTO result = productService.createProduct(dto);

        // Then
        assertThat(result.label()).isEqualTo("Clavier");
        assertThat(result.description()).isEqualTo("Meca");
        assertThat(result.price()).isGreaterThan(49.0);
        verify(repository, times(1)).save(any());
    }
}
