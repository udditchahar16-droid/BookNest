import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WalletService } from '../../services/services';
import { AuthService } from '../../services/auth.service';
import { Statement } from '../../models/models';

@Component({
  selector: 'app-statements',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container">
      <div class="page-header">
        <h1><i class="fas fa-file-alt"></i> Transaction Statements</h1>
        <a routerLink="/wallet" class="btn btn-outline btn-sm"><i class="fas fa-arrow-left"></i> Back to Wallet</a>
      </div>

      <div class="loading" *ngIf="loading"><div class="spinner"></div></div>

      <div class="card" *ngIf="!loading">
        <table *ngIf="statements.length">
          <thead>
            <tr>
              <th>#</th>
              <th>Type</th>
              <th>Amount</th>
              <th>Remarks</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let s of statements; let i = index">
              <td>{{i + 1}}</td>
              <td>
                <span class="badge" [class]="s.transactionType === 'DEPOSIT' ? 'badge-success' : 'badge-error'">
                  <i [class]="s.transactionType === 'DEPOSIT' ? 'fas fa-arrow-down' : 'fas fa-arrow-up'"></i>
                  {{s.transactionType}}
                </span>
              </td>
              <td [class]="s.transactionType === 'DEPOSIT' ? 'credit' : 'debit'">
                {{s.transactionType === 'DEPOSIT' ? '+' : '-'}}₹{{s.amount | number:'1.2-2'}}
              </td>
              <td>{{s.transactionRemarks || '—'}}</td>
              <td>{{s.dateTime | date:'medium'}}</td>
            </tr>
          </tbody>
        </table>

        <div class="empty-state" *ngIf="!statements.length">
          <i class="fas fa-receipt"></i>
          <h3>No transactions yet</h3>
          <p>Your transaction history will appear here</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .credit { color: var(--success); font-weight: 600; }
    .debit { color: var(--error); font-weight: 600; }
    .card { padding: 0; overflow: hidden; }
    table { margin: 0; }
  `]
})
export class StatementsComponent implements OnInit {
  statements: Statement[] = [];
  loading = true;

  constructor(private walletSvc: WalletService, private auth: AuthService) {}

  ngOnInit() {
    const uid = this.auth.currentUser!.userId;
    this.walletSvc.getStatements(uid).subscribe({ next: s => { this.statements = s.reverse(); this.loading = false; }, error: () => this.loading = false });
  }
}
