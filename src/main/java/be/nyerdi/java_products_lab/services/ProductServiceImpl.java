package be.nyerdi.java_products_lab.services;
import be.nyerdi.java_products_lab.dtos.ProductDTO;
import be.nyerdi.java_products_lab.entities.Product;
import be.nyerdi.java_products_lab.mappers.ProductMapper;
import be.nyerdi.java_products_lab.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<ProductDTO> listProducts() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }


    @Transactional
    @Override
    public ProductDTO createProduct(ProductDTO dto) {
        Product entity = mapper.toEntity(dto);
        Product saved = repository.save(entity);
        return mapper.toDto(saved);
    }
}

