import Link from "next/link";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { ShortUrl } from "@/lib/api";

export function UrlList({ urls }: { urls: ShortUrl[] }) {
  if (urls.length === 0) {
    return <p className="text-muted-foreground">No links yet</p>;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Short URL</TableHead>
          <TableHead>Original URL</TableHead>
          <TableHead className="text-right">Clicks</TableHead>
          <TableHead />
        </TableRow>
      </TableHeader>
      <TableBody>
        {urls.map((url) => (
          <TableRow key={url.shortCode}>
            <TableCell>
              <a href={url.shortUrl} className="hover:underline">
                {url.shortUrl}
              </a>
            </TableCell>
            <TableCell className="max-w-md truncate">{url.originalUrl}</TableCell>
            <TableCell className="text-right">{url.clickCount}</TableCell>
            <TableCell>
              <Link href={`/analytics/${url.shortCode}`} className="underline">
                Analytics
              </Link>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
