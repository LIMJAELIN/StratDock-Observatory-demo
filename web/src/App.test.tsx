import { afterEach, describe, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import App from './App';

afterEach(() => { cleanup(); vi.unstubAllGlobals(); });

describe('demo record states', () => {
  it('labels linked, duplicate and missing provenance distinctly', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [
      { id: 1, title: 'linked sample', sourceReference: 'ref-1', observedAt: '2026-01-12T09:30:00Z', quality: 'LINKED' },
      { id: 2, title: 'duplicate sample', sourceReference: 'ref-2', observedAt: '2026-01-12T09:35:00Z', quality: 'DUPLICATE_SOURCE' },
      { id: 3, title: 'missing sample', sourceReference: null, observedAt: '2026-01-12T09:40:00Z', quality: 'MISSING_SOURCE' },
    ] }));

    render(<App />);
    expect(await screen.findByText('linked sample')).toBeInTheDocument();
    expect(screen.getByText('출처 연결')).toBeInTheDocument();
    expect(screen.getByText('중복 출처')).toBeInTheDocument();
    expect(screen.getByText('출처 없음')).toBeInTheDocument();
  });

  it('shows an empty result separately from an unavailable source', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, json: async () => [] }));
    render(<App />);
    expect(await screen.findByText('표시할 자료가 없습니다.')).toBeInTheDocument();

    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('offline')));
    fireEvent.click(screen.getByRole('button', { name: '다시 불러오기' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('자료 연결을 확인할 수 없습니다.');
  });
});
