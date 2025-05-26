import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-flight-booking-table',
  standalone: true,
  imports: [CommonModule , FormsModule],
  templateUrl: './flight-booking-table.component.html',
  styleUrl: './flight-booking-table.component.css'
})
export class FlightBookingTableComponent {
  searchText = '';
  currentPage = 1;
  itemsPerPage = 10;

  data = Array(25).fill(null).map((_, i) => ({
    avatar: 'https://i.pravatar.cc/100?img=' + (i + 1),
    name: 'Abhishek Sharma',
    email: 'absdddbc@gmail.com',
    phone: '+91 888 565 2556',
    pnr: '14329JFWO',
    date: '23rd Mar.25',
    transactionId: 'T1234-5678-9012-3456',
    payment: 'Online',
    status: ['Success', 'Failed', 'Pending'][i % 3],
  }));

  getStatusClass(status: string) {
    return {
      'status-success': status === 'Success',
      'status-failed': status === 'Failed',
      'status-pending': status === 'Pending',
    };
  }  

  filteredData() {
    let filtered = this.data;
    if (this.searchText) {
      const term = this.searchText.toLowerCase();
      filtered = this.data.filter(d =>
        d.name.toLowerCase().includes(term) ||
        d.email.toLowerCase().includes(term) ||
        d.pnr.toLowerCase().includes(term)
      );
    }
    const start = (this.currentPage - 1) * this.itemsPerPage;
    return filtered.slice(start, start + this.itemsPerPage);
  }

  totalPages(): number {
    return Math.ceil(this.data.length / this.itemsPerPage);
  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages() }, (_, i) => i + 1);
  }

  goToPage(page: number) {
    this.currentPage = page;
  }

  prevPage() {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage() {
    if (this.currentPage < this.totalPages()) this.currentPage++;
  }
}
