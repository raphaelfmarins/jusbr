import { Suspense } from "react";
import { UrlList } from "@/components/url-list";
import { getUrls } from "@/lib/api";

async function Urls() {
  return <UrlList urls={await getUrls()} />;
}

export default function Home() {
  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-4 p-6">
      <h1 className="text-2xl font-semibold">Short links</h1>
      <Suspense fallback={<p>Loading…</p>}>
        <Urls />
      </Suspense>
    </main>
  );
}
