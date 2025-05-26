import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchFormComponent } from '../../feature/search-form/search-form.component';
import { FooterComponent } from '../../pages/footer/footer.component';
import { HeaderComponent } from '../../pages/header/header.component';
import { ActivatedRoute } from '@angular/router';
import { Router, RouterLink, RouterModule } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from '@angular/forms';
import { TopFlightRoutesComponent } from './components/top-flight-routes/top-flight-routes.component';
import { OffersComponent } from './components/offers/offers.component';
import { TravelBlogsComponent } from './components/travel-blogs/travel-blogs.component';
@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    SearchFormComponent,
    RouterModule,
    CommonModule,
    ReactiveFormsModule,
    TopFlightRoutesComponent,
    OffersComponent,
    TravelBlogsComponent
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.css',
})
export class LandingComponent implements OnInit {
  constructor(
    private routes: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder
  ) {}
  // eslint-disable-next-line @angular-eslint/no-empty-lifecycle-method
  ngOnInit(): void {}
}