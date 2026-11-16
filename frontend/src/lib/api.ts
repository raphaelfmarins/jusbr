export type ShortUrl = {
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  createdAt: string;
  expiresAt: string | null;
  analyticsUrl: string;
  clickCount: number;
};

export type DailyClicks = { date: string; count: number };

export type ReferrerCount = { referrer: string; count: number };

export type Stats = {
  shortCode: string;
  originalUrl: string;
  totalClicks: number;
  clicksByDay: DailyClicks[];
  topReferrers: ReferrerCount[];
};

const backendUrl = () => process.env.BACKEND_URL ?? "http://localhost:8080";

async function request(path: string): Promise<Response> {
  return fetch(`${backendUrl()}${path}`, { cache: "no-store" });
}

export async function getUrls(): Promise<ShortUrl[]> {
  const response = await request("/api/urls");
  if (!response.ok) throw new Error(`Failed to load URLs: ${response.status}`);
  return response.json();
}

export async function getStats(code: string): Promise<Stats | null> {
  const response = await request(`/api/stats/${encodeURIComponent(code)}`);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(`Failed to load stats: ${response.status}`);
  return response.json();
}
