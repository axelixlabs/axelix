package com.sivalabs.ft.features.api.controllers;

import com.sivalabs.ft.features.api.models.CreateProductPayload;
import com.sivalabs.ft.features.api.models.UpdateProductPayload;
import com.sivalabs.ft.features.api.utils.SecurityUtils;
import com.sivalabs.ft.features.domain.Commands.CreateProductCommand;
import com.sivalabs.ft.features.domain.Commands.UpdateProductCommand;
import com.sivalabs.ft.features.domain.ProductService;
import com.sivalabs.ft.features.domain.dtos.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products API")
class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    @Operation(
            summary = "Find all products",
            description = "Find all products",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful response",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))))
            })
    List<ProductDto> getProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/{code}")
    @Operation(
            summary = "Find product by code",
            description = "Find product by code",
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful response",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProductDto.class))),
                @ApiResponse(responseCode = "404", description = "Product not found")
            })
    ResponseEntity<ProductDto> getProduct(@PathVariable String code) {
        return productService
                .findProductByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("")
    @Operation(
            summary = "Create a new product",
            description = "Create a new product",
            responses = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Successful response",
                        headers =
                                @Header(
                                        name = "Location",
                                        required = true,
                                        description = "URI of the created product")),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden"),
            })
    ResponseEntity<Void> createProduct(@RequestBody @Valid CreateProductPayload payload) {
        var username = SecurityUtils.getCurrentUsername();
        var cmd = new CreateProductCommand(
                payload.code(), payload.prefix(), payload.name(), payload.description(), payload.imageUrl(), username);
        Long id = productService.createProduct(cmd);
        log.info("Created product with id {}", id);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}")
                .buildAndExpand(payload.code())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{code}")
    @Operation(
            summary = "Update an existing product",
            description = "Update an existing product",
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful response"),
                @ApiResponse(responseCode = "400", description = "Invalid request"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden"),
            })
    void updateProduct(@PathVariable String code, @RequestBody UpdateProductPayload payload) {
        var username = SecurityUtils.getCurrentUsername();
        var cmd = new UpdateProductCommand(
                code, payload.prefix(), payload.name(), payload.description(), payload.imageUrl(), username);
        productService.updateProduct(cmd);
    }
}
