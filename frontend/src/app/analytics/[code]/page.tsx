import Link from "next/link";
import { notFound } from "next/navigation";
import { StatsView } from "@/components/stats-view";
import { getStats } from "@/lib/api";

export default async function AnalyticsPage({ params }: PageProps<"/analytics/[code]">) {
  const { code } = await params;
  const stats = await getStats(code);
  if (!stats) notFound();

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-4 p-6">
      <Link href="/" className="text-sm underline">
        ← All links
      </Link>
      <StatsView stats={stats} />
    </main>
  );
}
