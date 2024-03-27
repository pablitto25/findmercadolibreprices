package com.latamly.scrapy.utils;

import java.util.List;

import org.springframework.stereotype.Component;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class FindML {
    String nombre;

    public Producto findML(String nombre) {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\Pablo Cortes\\Documents\\ProjectosSelenium\\ProjectSelenium\\src\\main\\resources\\chromedriver\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        driver.get("https://listado.mercadolibre.com.ar/" + nombre);
        driver.manage().window().maximize();

        // Dividir la cadena de búsqueda en palabras individuales
        String[] keywords = nombre.split(" ");

        WebElement cheapestProduct = null;
        WebElement mostReviewedProduct = null;
        double minWeightedPrice = Double.MAX_VALUE;
        int maxReviews = 0;
        double maxRanking = 0;

        List<WebElement> lists = driver.findElements(By.className("ui-search-layout__item"));
        int count = 0;
        Producto productoMasBarato = null;
        Producto productoConMasReviews = null;
        for (WebElement li : lists) {
            WebElement title = li.findElement(By.className("ui-search-item__title"));
            WebElement price = li.findElement(By.className("andes-money-amount__fraction"));

            String titulo = title.getText();
            double precio = Double.parseDouble(price.getText().replace(".", "").replace(",", ".")); // Convertir precio a número
            int reviewCount = 0;
            double ranking = 0;

            // Verificar si el producto tiene review y ranking
            boolean hasReviewAndRanking = false;
            try {
                WebElement reviews = li.findElement(By.className("ui-search-reviews__amount"));
                WebElement rankings = li.findElement(By.className("ui-search-reviews__rating-number"));
                reviewCount = Integer.parseInt(reviews.getText().replaceAll("[^0-9]", "")); // Obtener la cantidad de reseñas
                ranking = Double.parseDouble(rankings.getText().replace(",", ".")); // Obtener el ranking del producto
                hasReviewAndRanking = true;
            } catch (org.openqa.selenium.NoSuchElementException | NumberFormatException e) {
                // Ignorar si no se encuentran reviews o ranking
            }

            // Verificar si el título contiene todas las palabras clave y si tiene review y ranking
            boolean allKeywordsPresent = true;
            for (String keyword : keywords) {
                if (!titulo.toLowerCase().contains(keyword.toLowerCase())) {
                    allKeywordsPresent = false;
                    break;
                }
            }

            if (allKeywordsPresent && hasReviewAndRanking) {
                count++;
                System.out.println("------------------------------------ "+ count + "---------------------------------------------");
                System.out.println(titulo);
                System.out.println("Precio: " + precio);
                System.out.println("Reviews: " + reviewCount);
                System.out.println("Ranking: " + ranking);

                // Calcular el precio ponderado
                double weightedPrice = precio * 0.5 + (reviewCount * ranking) * 0.5; // Ajusta el factor de ponderación según tus necesidades

                // Actualizar el producto más barato
                if (weightedPrice < minWeightedPrice) {
                    minWeightedPrice = weightedPrice;
                    cheapestProduct = li;
                    productoMasBarato = new Producto(titulo, precio, reviewCount, ranking);
                }

                // Actualizar el producto con más reviews y ranking más alto
                if (reviewCount > maxReviews || (reviewCount == maxReviews && ranking > maxRanking)) {
                    maxReviews = reviewCount;
                    maxRanking = ranking;
                    mostReviewedProduct = li;
                    productoConMasReviews = new Producto(titulo, precio, reviewCount, ranking);
                }
            }
        }

        System.out.println("\n\nEl producto más barato:");
        if (cheapestProduct != null) {
            WebElement cheapestTitle = cheapestProduct.findElement(By.className("ui-search-item__title"));
            WebElement cheapestPrice = cheapestProduct.findElement(By.className("andes-money-amount__fraction"));
            System.out.println(cheapestTitle.getText());
            System.out.println("Precio: " + cheapestPrice.getText());
        } else {
            System.out.println("No se encontraron productos.");
        }

        System.out.println("\n\nEl producto con más reviews y ranking más alto:");
        if (mostReviewedProduct != null) {
            WebElement mostReviewedTitle = mostReviewedProduct.findElement(By.className("ui-search-item__title"));
            WebElement mostReviewedPrice = mostReviewedProduct.findElement(By.className("andes-money-amount__fraction"));
            WebElement mostReviewedReviews = mostReviewedProduct.findElement(By.className("ui-search-reviews__amount"));
            WebElement mostReviewedRanking = mostReviewedProduct.findElement(By.className("ui-search-reviews__rating-number"));
            System.out.println(mostReviewedTitle.getText());
            System.out.println("Precio: " + mostReviewedPrice.getText());
            System.out.println("Reviews: " + mostReviewedReviews.getText());
            System.out.println("Ranking: " + mostReviewedRanking.getText());
        } else {
            System.out.println("No se encontraron productos con reviews y ranking.");
        }

        driver.quit();
        return productoMasBarato;
    }
}

class Producto {
    @JsonProperty("titulo")
    String titulo;
    @JsonProperty("precio")
    double precio;
    @JsonProperty("reviews")
    int reviews;
    @JsonProperty("ranking")
    double ranking;

    public Producto() {
        // Constructor vacío requerido por Jackson
    }

    public Producto(String titulo, double precio, int reviews, double ranking) {
        this.titulo = titulo;
        this.precio = precio;
        this.reviews = reviews;
        this.ranking = ranking;
    }

    // Getters
    @JsonProperty("titulo")
    public String getTitulo() {
        return titulo;
    }

    @JsonProperty("precio")
    public double getPrecio() {
        return precio;
    }

    @JsonProperty("reviews")
    public int getReviews() {
        return reviews;
    }

    @JsonProperty("ranking")
    public double getRanking() {
        return ranking;
    }
}