import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TravelBlogsComponent } from './travel-blogs.component';

describe('TravelBlogsComponent', () => {
  let component: TravelBlogsComponent;
  let fixture: ComponentFixture<TravelBlogsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TravelBlogsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TravelBlogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
