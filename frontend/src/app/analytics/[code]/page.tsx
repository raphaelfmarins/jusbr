import { Suspense } from "react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { StatsView } from "@/components/stats-view";
import { getStats } from "@/lib/api";

async function Stats({ params }: { params: Promise<{ code: string }> }) {
  const { code } = await params;
  const stats = await getStats(code);
  if (!stats) notFound();
  return <StatsView stats={stats} />;
}

export default function AnalyticsPage({ params }: PageProps<"/analytics/[code]">) {
  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-4 p-6">
      <Link href="/" className="text-sm underline">
        ← All links
      </Link>
      <Suspense fallback={<p>Loading…</p>}>
        <Stats params={params} />
      </Suspense>
    </main>
  );
}
