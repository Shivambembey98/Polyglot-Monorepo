import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TopFlightRoutesComponent } from './top-flight-routes.component';

describe('TopFlightRoutesComponent', () => {
  let component: TopFlightRoutesComponent;
  let fixture: ComponentFixture<TopFlightRoutesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TopFlightRoutesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TopFlightRoutesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
