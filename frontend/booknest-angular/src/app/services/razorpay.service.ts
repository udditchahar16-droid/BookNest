import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Wallet } from '../models/models';

export interface RazorpayOrderResponse {
  orderId: string;
  amount: number;
  currency: string;
  keyId: string;
  receipt: string;
}

export interface RazorpayVerifyRequest {
  userId: number;
  amount: number;
  razorpayOrderId: string;
  razorpayPaymentId: string;
  razorpaySignature: string;
  remarks: string;
}

@Injectable({ providedIn: 'root' })
export class RazorpayService {
  private readonly BASE = '/api/razorpay';

  constructor(private http: HttpClient) {}

  /**
   * Step 1 — Create a Razorpay order on the backend
   */
  createOrder(amount: number, receipt: string): Observable<RazorpayOrderResponse> {
    return this.http.post<RazorpayOrderResponse>(`${this.BASE}/create-order`, { amount, receipt });
  }

  /**
   * Step 2 — Verify payment signature + credit wallet
   */
  verifyPayment(req: RazorpayVerifyRequest): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.BASE}/verify`, req);
  }

  /**
   * Load Razorpay checkout.js script dynamically (needed for the popup)
   */
  loadScript(): Promise<boolean> {
    return new Promise(resolve => {
      if ((window as any).Razorpay) { resolve(true); return; }
      const script = document.createElement('script');
      script.src = 'https://checkout.razorpay.com/v1/checkout.js';
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  }

  /**
   * Open Razorpay checkout popup.
   * Returns a Promise that resolves with payment details on success,
   * or rejects on failure/dismissal.
   */
  openCheckout(
    orderResponse: RazorpayOrderResponse,
    userName: string,
    userEmail: string
  ): Promise<{ razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }> {
    return new Promise((resolve, reject) => {
      const options = {
        key: orderResponse.keyId,
        amount: orderResponse.amount * 100, // paise
        currency: orderResponse.currency,
        name: 'BookNest',
        description: 'Wallet Top-Up',
        order_id: orderResponse.orderId,
        prefill: {
          name: userName,
          email: userEmail,
        },
        theme: { color: '#2c3e6b' },
        handler: (response: any) => resolve(response),
        modal: {
          ondismiss: () => reject(new Error('Payment cancelled by user'))
        }
      };

      const rzp = new (window as any).Razorpay(options);
      rzp.on('payment.failed', (response: any) => {
        reject(new Error(response.error?.description || 'Payment failed'));
      });
      rzp.open();
    });
  }
}
