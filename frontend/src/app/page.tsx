import { UrlList } from "@/components/url-list";
import { getUrls } from "@/lib/api";

export default async function Home() {
  const urls = await getUrls();

  return (
    <main className="mx-auto flex w-full max-w-4xl flex-col gap-4 p-6">
      <h1 className="text-2xl font-semibold">Short links</h1>
      <UrlList urls={urls} />
    </main>
  );
}
