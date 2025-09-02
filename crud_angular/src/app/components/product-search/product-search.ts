import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Customer, Product } from '../../service/customer';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-product-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-search.html',
  styleUrls: ['./product-search.css']
})
export class ProductSearchComponent implements OnInit {
  query: string = '';
  loading: boolean = false;
  errorMessage: string = '';
  products: Product[] = [];
  selectedStores: Set<string> = new Set(['Flipkart','Amazon']);
  sortBy: 'relevance' | 'priceAsc' | 'priceDesc' = 'relevance';
  viewMode: 'grid' | 'compare' = 'compare';

  constructor(private customerService: Customer, private route: ActivatedRoute, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    // Auto-run when /search?q=...
    this.route.queryParamMap.subscribe((params) => {
      const q = (params.get('q') || '').trim();
      if (q) {
        this.query = q;
        this.search();
      }
    });
  }

  search(): void {
    const trimmed = (this.query || '').trim();
    if (!trimmed) {
      this.products = [];
      this.errorMessage = '';
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.customerService.searchProducts(trimmed).subscribe({
      next: (results) => {
        this.products = results || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to fetch products.';
        console.error(err);
        this.cdr.detectChanges();
      }
    });
  }

  toggleStore(store: string): void {
    if (this.selectedStores.has(store)) {
      this.selectedStores.delete(store);
    } else {
      this.selectedStores.add(store);
    }
    this.cdr.detectChanges();
  }

  onSortChange(value: string): void {
    if (value === 'priceAsc' || value === 'priceDesc' || value === 'relevance') {
      this.sortBy = value;
      this.cdr.detectChanges();
    }
  }

  get filteredProducts(): Product[] {
    const filtered = (this.products || []).filter(p => {
      const src = (p.source || 'Unknown').trim();
      return this.selectedStores.has(src);
    });
    if (this.sortBy === 'relevance') return filtered;
    const withPrice = [...filtered];
    withPrice.sort((a, b) => this.parsePrice(a.price) - this.parsePrice(b.price));
    return this.sortBy === 'priceAsc' ? withPrice : withPrice.reverse();
  }

  get flipkartCompare(): Product[] {
    return this.sortForCompare(
      (this.products || []).filter(p => (p.source || '').trim() === 'Flipkart')
    );
  }

  get amazonCompare(): Product[] {
    return this.sortForCompare(
      (this.products || []).filter(p => (p.source || '').trim() === 'Amazon')
    );
  }

  setView(mode: 'grid' | 'compare'): void {
    this.viewMode = mode;
    this.cdr.detectChanges();
  }

  private sortForCompare(list: Product[]): Product[] {
    let arr = [...list];
    if (this.sortBy === 'relevance') return arr;
    arr.sort((a, b) => this.parsePrice(a.price) - this.parsePrice(b.price));
    return this.sortBy === 'priceAsc' ? arr : arr.reverse();
  }

  private parsePrice(price: string | undefined | null): number {
    if (!price) return Number.POSITIVE_INFINITY;
    const digits = (price.match(/[0-9]+/g) || []).join('');
    if (!digits) return Number.POSITIVE_INFINITY;
    return Number(digits);
  }
}


