"use client";

import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from "@/components/ui/chart";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { Stats } from "@/lib/api";

const chartConfig = {
  count: { label: "Clicks", color: "var(--chart-1)" },
} satisfies ChartConfig;

export function StatsView({ stats }: { stats: Stats }) {
  return (
    <div className="flex flex-col gap-6">
      <header>
        <h1 className="text-2xl font-semibold">/{stats.shortCode}</h1>
        <a
          href={stats.originalUrl}
          className="break-all text-sm text-muted-foreground hover:underline"
        >
          {stats.originalUrl}
        </a>
      </header>

      <Card>
        <CardHeader>
          <CardTitle>Total clicks</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-4xl font-bold" data-testid="total-clicks">
            {stats.totalClicks}
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Clicks by day (last 30 days)</CardTitle>
        </CardHeader>
        <CardContent>
          <ChartContainer config={chartConfig} className="h-64 w-full">
            <BarChart data={stats.clicksByDay}>
              <CartesianGrid vertical={false} />
              <XAxis dataKey="date" tickLine={false} tickFormatter={(d: string) => d.slice(5)} />
              <YAxis allowDecimals={false} width={32} />
              <ChartTooltip content={<ChartTooltipContent />} />
              <Bar dataKey="count" fill="var(--color-count)" radius={4} />
            </BarChart>
          </ChartContainer>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Top referrers</CardTitle>
        </CardHeader>
        <CardContent>
          {stats.topReferrers.length === 0 ? (
            <p className="text-muted-foreground">No clicks yet</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Referrer</TableHead>
                  <TableHead className="text-right">Clicks</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {stats.topReferrers.map(({ referrer, count }) => (
                  <TableRow key={referrer}>
                    <TableCell className="break-all">{referrer}</TableCell>
                    <TableCell className="text-right">{count}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
