import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlightDetailModalComponent } from './flight-detail-modal.component';

describe('FlightDetailModalComponent', () => {
  let component: FlightDetailModalComponent;
  let fixture: ComponentFixture<FlightDetailModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlightDetailModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlightDetailModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
