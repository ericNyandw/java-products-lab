package be.nyerdi.java_products_lab.mappers;

import be.nyerdi.java_products_lab.dtos.ProductDTO;
import be.nyerdi.java_products_lab.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // Transforme l'entité de la base de données en DTO pour l'API
    ProductDTO toDto(Product product);

    // Transforme le DTO reçu de l'API en entité pour la base de données
    Product toEntity(ProductDTO productDto);
}
