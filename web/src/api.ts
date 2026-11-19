export type DemoRecord = {
  id: number;
  title: string;
  sourceReference: string | null;
  observedAt: string;
  quality: 'LINKED' | 'DUPLICATE_SOURCE' | 'MISSING_SOURCE';
};

export async function fetchRecords(): Promise<DemoRecord[]> {
  const response = await fetch('/api/demo/records');
  if (!response.ok) throw new Error(`HTTP ${response.status}`);
  return response.json() as Promise<DemoRecord[]>;
}
