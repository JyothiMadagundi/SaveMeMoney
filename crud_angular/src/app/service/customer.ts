import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


const BASIC_URL = "http://localhost:8080"; 

@Injectable({
  providedIn: 'root'
})
export class Customer {

  constructor(private http: HttpClient) { }

  searchProducts(query: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${BASIC_URL}/api/products/search`, { params: { q: query } });
  }

  // Aggregated /all endpoint removed; using Flipkart/Amazon via backend aggregator of /products/search
}

export interface Product {
  name: string;
  price: string;
  imageUrl: string;
  hyperlink: string;
  ratings: string;
  reviews: string;
  source?: string; // Flipkart | Amazon | Croma (optional)
}
