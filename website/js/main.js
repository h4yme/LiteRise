/* ================================================================
   LiteRise Landing Page — main.js
   ================================================================ */

/* ── Navbar scroll effect ──────────────────────────────────────── */
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 40);
}, { passive: true });

/* ── Scroll reveal ─────────────────────────────────────────────── */
const revealObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting) {
      entry.target.classList.add('visible');
    }
  });
}, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });

document.querySelectorAll('.reveal').forEach(el => revealObserver.observe(el));

/* ── Animated stat counters ────────────────────────────────────── */
function animateCounter(el) {
  const target = el.dataset.target;
  const isText = isNaN(parseInt(target));
  if (isText) { el.textContent = target; return; }

  const suffix = target.replace(/[0-9]/g, '');
  const num    = parseInt(target);
  const duration = 1200;
  const start    = performance.now();

  function tick(now) {
    const elapsed  = now - start;
    const progress = Math.min(elapsed / duration, 1);
    const ease     = 1 - Math.pow(1 - progress, 3); // ease-out cubic
    const current  = Math.round(ease * num);
    el.textContent = current + suffix;
    if (progress < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}

const statObserver = new IntersectionObserver((entries) => {
  entries.forEach(entry => {
    if (entry.isIntersecting && !entry.target.dataset.counted) {
      entry.target.dataset.counted = 'true';
      entry.target.querySelectorAll('.stat-num[data-target]').forEach(animateCounter);
    }
  });
}, { threshold: 0.5 });

const statsSection = document.querySelector('.hero-stats');
if (statsSection) statObserver.observe(statsSection);

/* ── Smooth anchor scroll ──────────────────────────────────────── */
document.querySelectorAll('a[href^="#"]').forEach(link => {
  link.addEventListener('click', e => {
    const target = document.querySelector(link.getAttribute('href'));
    if (!target) return;
    e.preventDefault();
    const offset = 80;
    const top = target.getBoundingClientRect().top + window.scrollY - offset;
    window.scrollTo({ top, behavior: 'smooth' });
  });
});

/* ── Download button ripple ────────────────────────────────────── */
document.querySelectorAll('.btn-primary, .btn-cta-primary').forEach(btn => {
  btn.addEventListener('click', function(e) {
    const rect   = this.getBoundingClientRect();
    const circle = document.createElement('span');
    const size   = Math.max(rect.width, rect.height);
    circle.style.cssText = `
      position: absolute;
      width: ${size}px; height: ${size}px;
      border-radius: 50%;
      background: rgba(255,255,255,0.25);
      transform: translate(-50%, -50%) scale(0);
      top: ${e.clientY - rect.top}px;
      left: ${e.clientX - rect.left}px;
      animation: ripple 0.6s linear;
      pointer-events: none;
    `;
    if (getComputedStyle(this).position === 'static') {
      this.style.position = 'relative';
    }
    this.style.overflow = 'hidden';
    this.appendChild(circle);
    setTimeout(() => circle.remove(), 700);
  });
});

/* Inject ripple keyframe */
const rippleStyle = document.createElement('style');
rippleStyle.textContent = `
  @keyframes ripple {
    to { transform: translate(-50%, -50%) scale(2.5); opacity: 0; }
  }
`;
document.head.appendChild(rippleStyle);
