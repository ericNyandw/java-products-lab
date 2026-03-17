package be.nyerdi.java_products_lab.repositories;


import be.nyerdi.java_products_lab.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}

