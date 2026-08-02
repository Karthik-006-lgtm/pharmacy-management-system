# 🚀 Razorpay Integration - Quick Setup Guide

## Step 1: Get Razorpay Test Credentials

1. Visit https://dashboard.razorpay.com/signup
2. Create a Razorpay account (if you don't have one)
3. Login to dashboard
4. Go to **Settings** → **API Keys**
5. Copy your **Test Key ID** (starts with `rzp_test_`)
6. Generate and copy your **Test Key Secret**

## Step 2: Configure Application

### Option A: Using Environment Variables (Recommended)

**Windows:**
```cmd
set RAZORPAY_KEY_ID=rzp_test_YOUR_KEY_ID
set RAZORPAY_KEY_SECRET=YOUR_KEY_SECRET
```

**Linux/Mac:**
```bash
export RAZORPAY_KEY_ID=rzp_test_YOUR_KEY_ID
export RAZORPAY_KEY_SECRET=YOUR_KEY_SECRET
```

### Option B: Edit application.properties

Edit `src/main/resources/application.properties`:

```properties
razorpay.key.id=rzp_test_YOUR_KEY_ID
razorpay.key.secret=YOUR_KEY_SECRET
```

⚠️ **Warning:** Don't commit real credentials to version control!

## Step 3: Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/online-pharmacy-management-1.0.0.jar
```

## Step 4: Test the Integration

### Test Online Payment

1. Open http://localhost:8080
2. Login as customer (john@example.com / john123)
3. Add medicines to cart
4. Go to checkout
5. Select any online payment method (Google Pay, PhonePe, etc.)
6. Click "Place Order"
7. You'll be redirected to Razorpay payment page
8. Use test card details:
   - **Card Number:** 4111 1111 1111 1111
   - **CVV:** Any 3 digits (e.g., 123)
   - **Expiry:** Any future date (e.g., 12/25)
   - **Name:** Any name
9. Complete payment
10. You'll be redirected to confirmation page
11. Check your profile - invoice will be available immediately!

### Test Cash on Delivery

1. Login as customer
2. Add medicines to cart
3. Go to checkout
4. Select "Cash on Delivery"
5. Click "Place Order"
6. You'll go directly to confirmation (NO Razorpay!)
7. Invoice will NOT be available yet
8. Login as pharmacist, accept order, mark as delivered
9. Now invoice will be generated

## Step 5: Verify Integration

### Check Payment Record in Database

```sql
SELECT * FROM payments ORDER BY created_at DESC LIMIT 5;
```

You should see:
- `razorpay_order_id` - Razorpay order ID
- `razorpay_payment_id` - Razorpay payment ID
- `payment_status` - SUCCESS
- `payment_gateway` - RAZORPAY

### Check Order Status

```sql
SELECT order_number, payment_status, payment_method 
FROM orders 
ORDER BY order_date DESC LIMIT 5;
```

You should see:
- `payment_status` = PAID (for online payments)
- `payment_status` = PENDING (for COD)

### Check Invoice

```sql
SELECT * FROM invoices ORDER BY created_at DESC LIMIT 5;
```

For online payments, invoice should exist immediately.
For COD, invoice should only exist after delivery.

## Troubleshooting

### Issue: "Failed to create payment order"

**Cause:** Invalid Razorpay credentials

**Solution:**
1. Verify your key ID and secret are correct
2. Make sure you're using TEST keys (rzp_test_)
3. Check environment variables are set
4. Restart the application

### Issue: "Payment verification failed"

**Cause:** Signature mismatch

**Solution:**
1. Ensure you're using the correct key secret for verification
2. Check that razorpay.key.secret property is set
3. Don't modify the signature in transit

### Issue: Build fails

**Cause:** Dependency resolution

**Solution:**
```bash
mvn clean install -U
```

### Issue: Razorpay Checkout doesn't open

**Cause:** JavaScript error or network issue

**Solution:**
1. Check browser console for errors
2. Ensure internet connection is active
3. Clear browser cache
4. Try different browser

## Production Deployment

### Before Going Live:

1. **Complete Razorpay KYC**
   - Submit business documents
   - Wait for approval

2. **Switch to Live Keys**
   ```properties
   razorpay.key.id=rzp_live_YOUR_LIVE_KEY
   razorpay.key.secret=YOUR_LIVE_SECRET
   ```

3. **Enable HTTPS**
   - Razorpay requires HTTPS in production
   - Configure SSL certificate

4. **Test Thoroughly**
   - Test all payment methods
   - Test failure scenarios
   - Test refund flow (if implemented)

5. **Monitor Transactions**
   - Check Razorpay dashboard regularly
   - Set up alerts for failed payments
   - Monitor settlement status

## Support

- **Razorpay Documentation:** https://razorpay.com/docs/
- **Test Cards:** https://razorpay.com/docs/payments/payments/test-card-upi-details/
- **Support:** https://razorpay.com/support/

## Quick Reference

| Environment | Key ID Prefix | Where to Get |
|-------------|---------------|--------------|
| Test Mode | `rzp_test_` | Dashboard → Settings → API Keys → Test Mode |
| Live Mode | `rzp_live_` | Dashboard → Settings → API Keys → Live Mode |

**Congratulations! Your Razorpay integration is ready to use! 🎉**
