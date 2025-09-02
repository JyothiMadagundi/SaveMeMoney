import { Routes } from '@angular/router';
import { ProductSearchComponent } from './components/product-search/product-search';

export const routes: Routes = [
    { path: '', redirectTo: '/search', pathMatch: 'full' },
    { path: 'search', component: ProductSearchComponent },
    
  ];