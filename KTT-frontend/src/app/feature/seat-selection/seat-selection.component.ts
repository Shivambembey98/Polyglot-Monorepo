import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-seat-selection',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './seat-selection.component.html',
  styleUrl: './seat-selection.component.css'
})
export class SeatSelectionComponent implements OnInit {
  seatRows: any[][] = [];

  ngOnInit() {
    this.fetchSeatData();
  }

  fetchSeatData() {
    this.seatRows = [];

    for (let i = 1; i <= 15; i++) {
      this.seatRows.push([
        { label: `${i}A`, status: this.getRandomStatus() },
        { label: `${i}B`, status: this.getRandomStatus() },
        { label: `${i}C`, status: this.getRandomStatus() },
        { label: `${i}D`, status: this.getRandomStatus() },
      ]);
    }
  }

  getRandomStatus(): string {
    const statuses = ['occupied', 'paid', 'empty'];
    return statuses[Math.floor(Math.random() * statuses.length)];
  }

  onSeatSelect(seat: any) {
    if (seat.status === 'empty') {
      seat.status = 'selected';
    }
  }

  getSeatImage(status: string): string {
    // Images should be in: src/assets/seats/
    return `assets/svgs/seat-${status}.svg`;
  }
}
