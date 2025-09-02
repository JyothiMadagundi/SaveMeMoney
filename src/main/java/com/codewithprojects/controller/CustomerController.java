package com.codewithprojects.controller;

import com.codewithprojects.entity.Product;
import com.codewithprojects.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/products/search")
    public List<Product> allProducts(@RequestParam String q) {
        return customerService.fetchAllStores(q);
    }

}
