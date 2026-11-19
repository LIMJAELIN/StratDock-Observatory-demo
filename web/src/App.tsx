import { useEffect, useState } from 'react';
import { fetchRecords, type DemoRecord } from './api';

type LoadState = 'loading' | 'ready' | 'empty' | 'unavailable';

const qualityLabels = {
  LINKED: '출처 연결',
  DUPLICATE_SOURCE: '중복 출처',
  MISSING_SOURCE: '출처 없음',
} as const;

export default function App() {
  const [records, setRecords] = useState<DemoRecord[]>([]);
  const [state, setState] = useState<LoadState>('loading');

  async function load() {
    setState('loading');
    try {
      const result = await fetchRecords();
      setRecords(result);
      setState(result.length ? 'ready' : 'empty');
    } catch {
      setState('unavailable');
    }
  }

  useEffect(() => { void load(); }, []);

  return (
    <main className="shell">
      <header className="topbar">
        <a className="brand" href="#top" aria-label="읽기 전용 관측 데모 홈">
          <span className="brand-mark" aria-hidden="true">◉</span>
          <span>관측 데모</span>
        </a>
        <span className="demo-label"><span aria-hidden="true">●</span> 합성 데이터</span>
      </header>

      <section className="intro" id="top">
        <p className="eyebrow">READ-ONLY DATA FLOW</p>
        <h1>출처가 보이는<br />데이터 관측</h1>
        <p className="lede">화면에서 API를 거쳐 읽기 전용 데이터베이스까지 이어지는 흐름을 확인하는 작은 데모입니다.</p>
      </section>

      <section className="panel" aria-labelledby="records-title">
        <div className="panel-heading">
          <div>
            <p className="eyebrow">LOCAL SAMPLE</p>
            <h2 id="records-title">관측 기록</h2>
          </div>
          <button className="refresh" onClick={() => void load()} disabled={state === 'loading'}>
            다시 불러오기
          </button>
        </div>

        {state === 'loading' && <p className="notice" role="status">자료를 확인하고 있습니다…</p>}
        {state === 'empty' && <p className="notice" role="status">표시할 자료가 없습니다.</p>}
        {state === 'unavailable' && (
          <div className="notice notice-error" role="alert">
            <strong>자료 연결을 확인할 수 없습니다.</strong>
            <span>연결 오류를 정상 또는 빈 결과로 표시하지 않았습니다.</span>
          </div>
        )}
        {state === 'ready' && (
          <div className="table-wrap">
            <table>
              <thead><tr><th>기록</th><th>출처</th><th>확인</th><th>관측 시각</th></tr></thead>
              <tbody>{records.map((record) => (
                <tr key={record.id}>
                  <td className="record-title">{record.title}</td>
                  <td className="reference">{record.sourceReference ?? '—'}</td>
                  <td><span className={`quality quality-${record.quality.toLowerCase()}`}>{qualityLabels[record.quality]}</span></td>
                  <td className="timestamp">{new Date(record.observedAt).toLocaleString('ko-KR')}</td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        )}
        <p className="panel-footnote">모든 행은 로컬에서 생성한 예시입니다. 외부 서비스나 실제 운영 자료를 조회하지 않습니다.</p>
      </section>

      <section className="flow" aria-label="데이터 흐름">
        <div><span>01</span><strong>React 화면</strong><small>상태에 따라 구분해 표시</small></div>
        <b aria-hidden="true">→</b>
        <div><span>02</span><strong>Spring API</strong><small>읽기 요청만 처리</small></div>
        <b aria-hidden="true">→</b>
        <div><span>03</span><strong>PostgreSQL</strong><small>SELECT 전용 계정</small></div>
      </section>

      <footer>독립적으로 재구성한 개인 데모 · 합성 자료만 사용</footer>
    </main>
  );
}
