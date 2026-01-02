# SMTP Email Setup Guide for LiteRise

This guide will help you configure real email delivery for OTP and welcome emails using Gmail SMTP.

## Option 1: Quick Setup with Gmail (Recommended for Testing)

### Step 1: Install PHPMailer

Open Command Prompt in your `C:\xampp\htdocs\api` directory and run:

```bash
cd C:\xampp\htdocs\api
composer install
```

If you don't have Composer installed, download it from: https://getcomposer.org/download/

**Alternative:** Manual Installation (without Composer)
1. Download PHPMailer from: https://github.com/PHPMailer/PHPMailer/releases
2. Extract to `C:\xampp\htdocs\api\vendor\phpmailer\phpmailer`
3. Make sure the folder structure is: `vendor/phpmailer/phpmailer/src/PHPMailer.php`

### Step 2: Get Gmail App Password

1. Go to your Google Account: https://myaccount.google.com/
2. Click on **Security** in the left sidebar
3. Under "How you sign in to Google", enable **2-Step Verification** (if not already enabled)
4. After 2-Step is enabled, go back to Security
5. Click on **App passwords** (you'll see this only after 2-Step is enabled)
6. Select "Mail" for the app and "Other" for the device
7. Enter "LiteRise API" as the device name
8. Click **Generate**
9. **COPY THE 16-CHARACTER PASSWORD** (you won't see it again!)

### Step 3: Update .env File

Copy the `.env.example` file to `.env`:

```bash
copy .env.example .env
```

Edit `C:\xampp\htdocs\api\.env` and update these fields:

```env
# SMTP Configuration
SMTP_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-actual-email@gmail.com
SMTP_PASSWORD=your-16-char-app-password
SMTP_ENCRYPTION=tls

# Email Configuration
EMAIL_FROM=your-actual-email@gmail.com
EMAIL_FROM_NAME=LiteRise
```

**Important:**
- Replace `your-actual-email@gmail.com` with your Gmail address
- Replace `your-16-char-app-password` with the password from Step 2
- Don't use your regular Gmail password - use the App Password!

### Step 4: Copy Updated Files to XAMPP

Copy the updated email.php to your XAMPP directory:

```bash
copy api\src\email.php C:\xampp\htdocs\api\src\email.php
copy api\.env.example C:\xampp\htdocs\api\.env.example
copy api\composer.json C:\xampp\htdocs\api\composer.json
```

Then edit `C:\xampp\htdocs\api\.env` with your actual Gmail credentials.

### Step 5: Test Email Sending

Create a test file `C:\xampp\htdocs\api\test_email.php`:

```php
<?php
require_once 'vendor/autoload.php';
require_once 'src/email.php';

// Load environment variables
$envFile = __DIR__ . '/.env';
if (file_exists($envFile)) {
    $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue;
        list($key, $value) = explode('=', $line, 2);
        $_ENV[trim($key)] = trim($value);
    }
}

// Test email
$testEmail = "your-test-email@gmail.com"; // Change this to your email
$result = sendOTPEmail($testEmail, "123456", "Test User");

if ($result) {
    echo "✅ Test email sent successfully! Check your inbox.\n";
} else {
    echo "❌ Failed to send test email. Check the error logs.\n";
}
?>
```

Run the test:
```bash
php C:\xampp\htdocs\api\test_email.php
```

Or visit in browser: `http://localhost/api/test_email.php`

## Option 2: Disable SMTP (Use Debug Mode Only)

If you just want to test with `debug_otp` in the response without sending real emails:

In your `.env` file, set:
```env
SMTP_ENABLED=false
```

The API will return the OTP in the `debug_otp` field instead of sending emails.

## Troubleshooting

### Error: "Class 'PHPMailer\PHPMailer\PHPMailer' not found"

**Solution:** Run `composer install` in the api directory or install PHPMailer manually.

### Error: "SMTP connect() failed"

**Possible solutions:**
1. Check that you're using an **App Password**, not your regular Gmail password
2. Make sure 2-Step Verification is enabled on your Google account
3. Verify SMTP settings:
   - Host: `smtp.gmail.com`
   - Port: `587`
   - Encryption: `tls`

### Error: "Invalid credentials"

**Solution:** Double-check your Gmail address and App Password in the `.env` file.

### Gmail Security Alert

If Gmail blocks the login attempt:
1. Check your Gmail inbox for a security alert
2. Click "Allow" or "Yes, it was me"
3. Try sending the email again

## Testing the Forgot Password Flow

1. Open the LiteRise app
2. Click "Forgot Password?" on the login screen
3. Enter a registered email address
4. Check your Gmail inbox for the OTP code
5. Enter the 6-digit code in the app
6. Create a new password

## Production Recommendations

For production deployment, consider using:
- **SendGrid** (https://sendgrid.com) - Free tier: 100 emails/day
- **Mailgun** (https://mailgun.com) - Free tier: 5,000 emails/month
- **Amazon SES** (https://aws.amazon.com/ses/) - Very cheap bulk email service

These services are more reliable than Gmail for sending emails at scale.

## Support

If you encounter any issues, check the PHP error log:
- XAMPP Error Log: `C:\xampp\php\logs\php_error_log`
- Apache Error Log: `C:\xampp\apache\logs\error.log`
