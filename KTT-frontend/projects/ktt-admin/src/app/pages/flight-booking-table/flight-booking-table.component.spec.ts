import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlightBookingTableComponent } from './flight-booking-table.component';

describe('FlightBookingTableComponent', () => {
  let component: FlightBookingTableComponent;
  let fixture: ComponentFixture<FlightBookingTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlightBookingTableComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlightBookingTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
