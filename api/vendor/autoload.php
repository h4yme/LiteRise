<?php

/**
 * Simple autoloader for PHPMailer
 * This file loads PHPMailer classes if they exist
 */

// If Composer's vendor/autoload.php exists, use it
$composerAutoload = __DIR__ . '/autoload_real.php';
if (file_exists($composerAutoload)) {
    require_once $composerAutoload;
    return;
}

// Manual autoloader for PHPMailer if installed manually
spl_autoload_register(function ($class) {
    // Only handle PHPMailer namespace
    if (strpos($class, 'PHPMailer\\PHPMailer\\') === 0) {
        $className = str_replace('PHPMailer\\PHPMailer\\', '', $class);
        $file = __DIR__ . '/phpmailer/phpmailer/src/' . $className . '.php';

        if (file_exists($file)) {
            require_once $file;
        }
    }
});
