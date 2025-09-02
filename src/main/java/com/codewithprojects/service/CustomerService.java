package com.codewithprojects.service;

import com.codewithprojects.entity.Product;
import lombok.RequiredArgsConstructor;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CustomerService {



    // Keep the '/all' endpoint working; Flipkart + Google only
    public List<Product> fetchAllStores(String searchTerm) {
        List<Product> all = new ArrayList<>();
        all.addAll(scrapeFlipkart(searchTerm));
        all.addAll(scrapeAmazon(searchTerm));
        all.addAll(scrapeRelianceDigital(searchTerm));
        all.addAll(scrapeCroma(searchTerm));
        all.addAll(scrapeTataCliq(searchTerm));
        String serpKey = System.getenv("SERPAPI_KEY");
        return all;
    }
    // Tata CLiQ
    public List<Product> scrapeTataCliq(String searchTerm) {
        List<Product> products = new ArrayList<>();
        try {
            String url = "https://www.tatacliq.com/search/?searchCategory=all&text=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .timeout(12000)
                    .get();
            Elements cards = doc.select("div.ProductModule__productCard");
            for (Element card : cards) {
                Product p = new Product();
                Element name = card.selectFirst("div.ProductModule__productName");
                Element price = card.selectFirst("div.ProductModule__price");
                Element img = card.selectFirst("img.ProductModule__image");
                Element link = card.selectFirst("a.ProductModule__card");
                p.setName(name != null ? name.text() : "");
                p.setPrice(price != null ? price.text() : "");
                p.setImageUrl(img != null ? img.absUrl("src") : "");
                p.setHyperlink(link != null ? "https://www.tatacliq.com" + link.attr("href") : "");
                p.setSource("Tata CLiQ");
                if (!p.getName().isEmpty()) products.add(p);
            }
            if (!products.isEmpty()) System.out.println("Tata CLiQ: " + products.get(0));
        } catch (IOException e) {
            System.out.println("Tata CLiQ scrape error: " + e.getMessage());
        }
        return products;
    }
    // Croma
    public List<Product> scrapeCroma(String searchTerm) {
        List<Product> products = new ArrayList<>();
        try {
            String url = "https://www.croma.com/search/?text=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .timeout(12000)
                    .get();
            Elements cards = doc.select("li.product-item");
            for (Element card : cards) {
                Product p = new Product();
                Element name = card.selectFirst("h3.product-title");
                Element price = card.selectFirst("span.amount");
                Element img = card.selectFirst("img.product-image-photo");
                Element link = card.selectFirst("a.product-item-link");
                p.setName(name != null ? name.text() : "");
                p.setPrice(price != null ? price.text() : "");
                p.setImageUrl(img != null ? img.absUrl("src") : "");
                p.setHyperlink(link != null ? link.absUrl("href") : "");
                p.setSource("Croma");
                if (!p.getName().isEmpty()) products.add(p);
            }
            if (!products.isEmpty()) System.out.println("Croma: " + products.get(0));
        } catch (IOException e) {
            System.out.println("Croma scrape error: " + e.getMessage());
        }
        return products;
    }
    // Reliance Digital
    public List<Product> scrapeRelianceDigital(String searchTerm) {
        List<Product> products = new ArrayList<>();
        try {
            String url = "https://www.reliancedigital.in/search?q=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .timeout(12000)
                    .get();
            Elements cards = doc.select("div.sp__product-card");
            for (Element card : cards) {
                Product p = new Product();
                Element name = card.selectFirst("p.sp__name");
                Element price = card.selectFirst("span.sp__offerPrice");
                Element img = card.selectFirst("img.sp__product-img");
                Element link = card.selectFirst("a.sp__product-link");
                p.setName(name != null ? name.text() : "");
                p.setPrice(price != null ? price.text() : "");
                p.setImageUrl(img != null ? img.absUrl("src") : "");
                p.setHyperlink(link != null ? "https://www.reliancedigital.in" + link.attr("href") : "");
                p.setSource("Reliance Digital");
                if (!p.getName().isEmpty()) products.add(p);
            }
            if (!products.isEmpty()) System.out.println("Reliance Digital: " + products.get(0));
        } catch (IOException e) {
            System.out.println("Reliance Digital scrape error: " + e.getMessage());
        }
        return products;
    }
    // Amazon India
    public List<Product> scrapeAmazon(String searchTerm) {
        List<Product> products = new ArrayList<>();
        try {
            String url = "https://www.amazon.in/s?k=" + URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .timeout(12000)
                    .get();
            Elements cards = doc.select("div.s-main-slot div.s-result-item[data-component-type='s-search-result']");
            for (Element card : cards) {
                Product p = new Product();
                Element name = card.selectFirst("h2 span");
                Element price = card.selectFirst("span.a-price > span.a-offscreen");
                Element img = card.selectFirst("img.s-image");
                // Try multiple selectors to reliably get a product link
                Element link = card.selectFirst("h2 a.a-link-normal[href]");
                if (link == null) link = card.selectFirst("a.a-link-normal.s-underline-text.s-underline-link-text.s-link-style.a-text-normal[href]");
                if (link == null) link = card.selectFirst("a.a-link-normal[href]");
                if (link == null) link = card.selectFirst("a[href*='/dp/']");
                p.setName(name != null ? name.text() : "");
                p.setPrice(price != null ? price.text() : "");
                p.setImageUrl(img != null ? img.absUrl("src") : "");
                String href = link != null ? link.attr("href") : "";
                if (href != null && !href.isBlank()) {
                    if (href.startsWith("/")) {
                        href = "https://www.amazon.in" + href;
                    }
                    p.setHyperlink(href);
                } else {
                    p.setHyperlink("");
                }
                // ratings (e.g., "4.4 out of 5 stars")
                Element ratingElem = card.selectFirst("span.a-icon-alt");
                if (ratingElem != null) {
                    p.setRatings(ratingElem.text());
                }
                // reviews count (aria-label often contains "X ratings"); fallback to visible underline text
                Element reviewsElem = card.selectFirst("span[aria-label$='ratings'], span[aria-label$='rating']");
                String reviewsText = reviewsElem != null ? reviewsElem.attr("aria-label") : "";
                if (reviewsText == null || reviewsText.isBlank()) {
                    Element reviewsFallback = card.selectFirst("span.a-size-base.s-underline-text");
                    if (reviewsFallback != null) {
                        reviewsText = reviewsFallback.text();
                    }
                }
                if (reviewsText != null) {
                    String normalized = reviewsText.trim();
                    // If it's just a number (with optional commas), append " Ratings"
                    if (normalized.matches("^[0-9,]+$")) {
                        normalized = normalized + " Ratings";
                    }
                    p.setReviews(normalized);
                }
                p.setSource("Amazon");
                if (!p.getName().isEmpty()) products.add(p);
            }
            if (!products.isEmpty()) System.out.println("Amazon: " + products.get(0));
        } catch (IOException e) {
            System.out.println("Amazon scrape error: " + e.getMessage());
        }
        return products;
    }

    // Minimal Flipkart scraper with basic desktop/mobile UA retry
    private List<Product> scrapeFlipkart(String searchTerm) {
        List<Product> products = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            String url = "https://www.flipkart.com/search?q=" + encoded;
            Document doc;

            try {
                doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                        .referrer("https://www.flipkart.com/")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Cache-Control", "no-cache")
                        .header("Pragma", "no-cache")
                        .timeout(12000)
                        .maxBodySize(0)
                        .get();
            } catch (HttpStatusException statusEx) {
                if (statusEx.getStatusCode() == 403) {
                    doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                            .referrer("https://www.flipkart.com/")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .header("Cache-Control", "no-cache")
                            .header("Pragma", "no-cache")
                            .timeout(12000)
                            .maxBodySize(0)
                            .get();
                } else {
                    return products;
                }
            }

            // Common card containers (grid/list views)
            Elements productCards = doc.select("div._75nlfW, div._1AtVbE");
            // Amazon results removed; Flipkart-only

            for (Element card : productCards) {
                Product product = new Product();

                Element nameElem = card.selectFirst("div.KzDlHZ, div._4rR01T");
                product.setName(nameElem != null ? nameElem.text() : "");

                Element priceElem = card.selectFirst("div._4b5DiR, div._30jeq3");
                product.setPrice(priceElem != null ? priceElem.text() : "");

                Element imgElem = card.selectFirst("img.DByuf4, img._396cs4");
                product.setImageUrl(imgElem != null ? imgElem.absUrl("src") : "");

                Element linkElem = card.selectFirst("a.CGtC98, a.IRpwTa");
                product.setHyperlink(linkElem != null ? "https://www.flipkart.com" + linkElem.attr("href") : "");

                Element ratingElem = card.selectFirst("div.XQDdHH, div._3LWZlK");
                product.setRatings(ratingElem != null ? ratingElem.text() : "");

                Element reviewElem = card.selectFirst("span.Wphh3N, span._2_R_DZ");
                product.setReviews(reviewElem != null ? reviewElem.text() : "");

                product.setSource("Flipkart");

                if (!product.getName().isEmpty()) {
                products.add(product);
                }
            }

        } catch (IOException ignored) {
        }
        return products;
    }
}
