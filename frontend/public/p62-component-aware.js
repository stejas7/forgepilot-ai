(() => {
  const scoreFile = (file, el) => {
    const text = (el.innerText || '').trim().slice(0, 120);
    const cls = typeof el.className === 'string' ? el.className.split(/\s+/).filter(Boolean).slice(0, 4) : [];
    const tag = el.tagName.toLowerCase();
    let score = 0;
    if (text && file.content.includes(text)) score += 12;
    if (el.id && file.content.includes(el.id)) score += 8;
    cls.forEach(c => { if (c && file.content.includes(c)) score += 3; });
    if (file.content.includes(`<${tag}`) || file.content.includes(tag.toUpperCase())) score += 1;
    if (/\.(tsx|jsx|html)$/.test(file.path)) score += 2;
    return score;
  };

  const ensurePanel = () => {
    let panel = document.getElementById('fp-component-source-panel');
    if (panel) return panel;
    panel = document.createElement('aside');
    panel.id = 'fp-component-source-panel';
    panel.style.cssText = 'position:fixed;right:18px;bottom:18px;z-index:9999;width:min(380px,calc(100vw - 36px));background:#111216;color:#fff;border:1px solid #303139;border-radius:16px;padding:14px;box-shadow:0 22px 60px #0005;font:12px/1.45 system-ui;display:none';
    document.body.appendChild(panel);
    return panel;
  };

  const mapSelection = async (frame, el) => {
    const match = frame.getAttribute('src')?.match(/\/api\/projects\/([^/]+)\/preview/);
    if (!match) return;
    const projectId = match[1];
    const panel = ensurePanel();
    panel.style.display = 'block';
    panel.innerHTML = '<b>Component source</b><p style="color:#aaa">Locating the selected preview element…</p>';
    try {
      const r = await fetch(`/api/projects/${projectId}/workspace/files`);
      if (!r.ok) throw new Error('Workspace files unavailable');
      const files = await r.json();
      const ranked = files.map(file => ({file, score: scoreFile(file, el)})).filter(x => x.score > 0).sort((a,b) => b.score-a.score).slice(0,5);
      const selector = el.dataset.forgepilotId ? `[data-forgepilot-id="${el.dataset.forgepilotId}"]` : el.tagName.toLowerCase();
      panel.innerHTML = `<div style="display:flex;justify-content:space-between;gap:12px"><b>Component source</b><button id="fp-source-close" style="border:0;background:transparent;color:#fff;font-size:18px">×</button></div><p style="color:#aaa;margin:6px 0 10px">${selector}</p>${ranked.length ? ranked.map(({file,score}) => `<button class="fp-source-result" data-path="${encodeURIComponent(file.path)}" style="display:block;width:100%;text-align:left;margin:6px 0;padding:9px;border:1px solid #363842;border-radius:9px;background:#1b1c21;color:#fff"><b>${file.path}</b><span style="display:block;color:#999">match score ${score}</span></button>`).join('') : '<p>No reliable source match. Use Ask AI about selection to patch safely.</p>'}`;
      panel.querySelector('#fp-source-close')?.addEventListener('click', () => panel.style.display='none');
      panel.querySelectorAll('.fp-source-result').forEach(btn => btn.addEventListener('click', () => {
        const path = decodeURIComponent(btn.dataset.path || '');
        sessionStorage.setItem('forgepilot.sourcePath', path);
        document.querySelector('.preview-top button:nth-child(2)')?.click();
        window.dispatchEvent(new CustomEvent('forgepilot:open-source', {detail:{path, projectId}}));
        panel.style.display='none';
      }));
    } catch (e) {
      panel.innerHTML = `<b>Component source</b><p>${e instanceof Error ? e.message : 'Unable to locate source'}</p>`;
    }
  };

  const bindFrame = frame => {
    if (frame.dataset.fpP62Bound === 'true') return;
    frame.dataset.fpP62Bound = 'true';
    frame.addEventListener('load', () => {
      const doc = frame.contentDocument;
      if (!doc) return;
      doc.addEventListener('click', event => {
        const active = document.querySelector('.visual-toolbar button.active');
        if (!active || !/select/i.test(active.textContent || '')) return;
        const el = event.target;
        if (!(el instanceof frame.contentWindow.HTMLElement)) return;
        setTimeout(() => void mapSelection(frame, el), 0);
      }, true);
    });
  };

  const observer = new MutationObserver(() => document.querySelectorAll('.visual-workspace iframe').forEach(bindFrame));
  observer.observe(document.documentElement,{childList:true,subtree:true});
  document.querySelectorAll('.visual-workspace iframe').forEach(bindFrame);
})();
