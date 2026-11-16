import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StatsView } from "@/components/stats-view";
import type { Stats } from "@/lib/api";

const stats = (overrides: Partial<Stats> = {}): Stats => ({
  shortCode: "abc123",
  originalUrl: "https://example.com/long",
  totalClicks: 42,
  clicksByDay: [
    { date: "2026-11-15", count: 2 },
    { date: "2026-11-16", count: 40 },
  ],
  topReferrers: [
    { referrer: "https://google.com", count: 30 },
    { referrer: "direct", count: 12 },
  ],
  ...overrides,
});

describe("StatsView", () => {
  it("renders short code, original url and total clicks", () => {
    render(<StatsView stats={stats()} />);

    expect(screen.getByText("/abc123")).toBeInTheDocument();
    expect(screen.getByText("https://example.com/long")).toBeInTheDocument();
    expect(screen.getByTestId("total-clicks")).toHaveTextContent("42");
  });

  it("renders referrers table", () => {
    render(<StatsView stats={stats()} />);

    const rows = within(screen.getByRole("table")).getAllByRole("row");
    expect(rows).toHaveLength(3);
    expect(rows[1]).toHaveTextContent("https://google.com30");
    expect(rows[2]).toHaveTextContent("direct12");
  });

  it("renders empty state when there are no referrers", () => {
    render(<StatsView stats={stats({ totalClicks: 0, topReferrers: [] })} />);

    expect(screen.getByText("No clicks yet")).toBeInTheDocument();
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
  });
});
