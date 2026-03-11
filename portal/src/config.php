<?php
/**
 * LiteRise Portal — API configuration.
 *
 * Set API_BASE to wherever your LiteRiseAPI PHP files are served.
 *   Local XAMPP  → 'http://192.168.1.13'  (no trailing slash)
 *   Azure        → 'https://literiseapi-bgfcfgdydvc6djhk.southeastasia-01.azurewebsites.net'
 *
 * The constant is also output as a JS variable by apiBaseTag() so that
 * inline <script> blocks can do:  fetch(API_BASE + '/get_student_progress.php?...')
 */

if (defined('LITERISE_CONFIG_LOADED')) return;
define('LITERISE_CONFIG_LOADED', true);

// ── Change this one line to switch environments ───────────────────────────────
define('API_BASE', 'http://192.168.1.13');
// define('API_BASE', 'https://literiseapi-bgfcfgdydvc6djhk.southeastasia-01.azurewebsites.net');
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Outputs a <script> block that exposes API_BASE as a JS constant.
 * Call this once inside <head> or just before your page scripts.
 */
function apiBaseTag(): void
{
    $base = json_encode(API_BASE);
    echo "<script>const API_BASE = {$base};</script>\n";
}
