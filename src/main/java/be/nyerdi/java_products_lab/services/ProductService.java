package be.nyerdi.java_products_lab.services;

import be.nyerdi.java_products_lab.dtos.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> listProducts();
    ProductDTO createProduct(ProductDTO dto);
}
