function renderingNavBar(active) {
  const links = [
    { label: 'Home', href: 'designSystem.html' }, { label: 'Brand Foundation', href: 'brand-foundation.html' },
    { label: 'Design Principles', href: 'principles.html' }, { label: 'Logo', href: 'logo.html' },
    { label: 'Colour', href: 'colour.html' }, { label: 'Typography', href: 'typography.html' },
    { label: 'Iconography', href: 'iconography.html' }, { label: 'Borders', href: 'borders.html' },
    { label: 'Components', href: 'components.html' }, { label: 'Spacing', href: 'spacing.html' },
    { label: 'Wireframes', href: 'wireframes.html' },
  ];
  
  const linksHTML = links.map(l => {
    const isActive = l.label === active;
    return `<a href="${l.href}" class="nav-link${isActive ? ' nav-active' : ''}">${l.label}</a>`;
  }).join('');
  
  const mobileLinksHTML = links.map(l => {
    const isActive = l.label === active;
    return `<a href="${l.href}" class="${isActive ? 'nav-active' : ''}">${l.label}</a>`;
  }).join('');
  
  const navContainer = document.getElementById('nav-mount');

  if(!navContainer){
    return;
  }
  
  navContainer.innerHTML = `
    <nav style="background:#0d0f2e;border-bottom:1px solid #1e1e6a;height:52px;display:flex;align-items:center;justify-content:space-between;padding:0 36px;position:sticky;top:0;z-index:50;gap:16px;">
      <a href="designSystem.html" style="display:flex;align-items:center;gap:10px;flex-shrink:0;text-decoration:none;">
        <img src="CloudSherpaLogo.png" alt="CloudSherpa" style="width:100px;height:100px;object-fit:contain;" onerror="this.parentElement.innerHTML='<span style=&quot;font-size:9px;font-weight:700;color:#2f2fe4;font-family:monospace;&quot;>CS</span>'" />
        <span style="font-size:12px;font-weight:500;color:var(--text);display:none;@media(min-width:768px){display:inline;}">CloudSherpa</span>
      </a>
      <div style="display:flex;align-items:center;gap:2px;flex:1;justify-content:center;flex-wrap:wrap;">
        ${linksHTML}
      </div>
      <div style="flex-shrink:0;display:flex;align-items:center;gap:12px;">
        <span style="font-size:9px;font-family:'DM Mono',monospace;font-weight:500;color:#4a4a9a;border:1px solid #1e1e6a;background:#141450;padding:2px 7px;border-radius:4px;letter-spacing:0.08em;">v1.0</span>
        <button class="hamburger-btn" aria-label="Menu" style="display:none;background:none;border:none;cursor:pointer;padding:0;margin:0;">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </nav>
    <div class="mobile-menu">
      ${mobileLinksHTML}
    </div>
    <div class="menu-overlay"></div>
  `;
  
  const hamburgerButton = document.querySelector('.hamburger-btn');
  const mobileMenu = document.querySelector('.mobile-menu');

  const menuOverlay = document.querySelector('.menu-overlay');
  
  function toggleMenu(){
    hamburgerButton.classList.toggle('open');
    mobileMenu.classList.toggle('open');
    menuOverlay.classList.toggle('open');
    document.body.style.overflow = mobileMenu.classList.contains('open') ? 'hidden' : '';
  }
  
  if(hamburgerButton && mobileMenu && menuOverlay){
    function checkMobile() {

      if(window.innerWidth <= 768){
        hamburgerButton.style.display = 'flex';
      } else{
        hamburgerButton.style.display = 'none';
        
        if(mobileMenu.classList.contains('open')){
          toggleMenu();
        }

      }
    }
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    
    hamburgerButton.addEventListener('click', toggleMenu);
    menuOverlay.addEventListener('click', toggleMenu);
    
    const mobileLinks = mobileMenu.querySelectorAll('a');
    mobileLinks.forEach(link => {
      link.addEventListener('click', () => {

        if(mobileMenu.classList.contains('open')){
          toggleMenu();
        }

      });
    });
    
  }
}