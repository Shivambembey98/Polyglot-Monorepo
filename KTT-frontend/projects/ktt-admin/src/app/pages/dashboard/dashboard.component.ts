import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import Chart from 'chart.js/auto';
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements AfterViewInit {
  @ViewChild('revenueChart') revenueChart!: ElementRef;
  @ViewChild('deviceChart') deviceChart!: ElementRef;
  @ViewChild('salesPie') salesPie!: ElementRef;

  ngAfterViewInit(): void {
    new Chart(this.revenueChart.nativeElement, {
      type: 'line',
      data: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
        datasets: [
          {
            label: 'This year',
            data: [10, 18, 14, 11, 17, 21, 19],
            borderColor: '#000',
            tension: 0.4,
          },
          {
            label: 'Last year',
            data: [9, 16, 13, 10, 15, 19, 25],
            borderColor: '#ADD8E6',
            tension: 0.4,
          },
        ],
      }
    });

    new Chart(this.deviceChart.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Linux', 'Mac', 'iOS', 'Windows', 'Android', 'Other'],
        datasets: [{
          data: [20, 23, 21, 26, 12, 22],
          backgroundColor: '#66b2ff',
        }],
      }
    });

    new Chart(this.salesPie.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['United States', 'Canada', 'India', 'Other'],
        datasets: [{
          data: [38.6, 22.5, 30.8, 8.1],
          backgroundColor: ['#000', '#a2d2ff', '#73c2fb', '#e0e0e0'],
        }],
      }
    });
  }
}
