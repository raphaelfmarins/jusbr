import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { UrlList } from "@/components/url-list";

describe("UrlList", () => {
  it("renders links with analytics link", () => {
    render(
      <UrlList
        urls={[
          {
            shortCode: "abc123",
            shortUrl: "http://localhost:8080/abc123",
            originalUrl: "https://example.com",
            createdAt: "2026-11-16T10:00:00Z",
            expiresAt: null,
            analyticsUrl: "http://localhost:3000/analytics/abc123",
            clickCount: 7,
          },
        ]}
      />,
    );

    expect(screen.getByText("http://localhost:8080/abc123")).toBeInTheDocument();
    expect(screen.getByText("7")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Analytics" })).toHaveAttribute(
      "href",
      "/analytics/abc123",
    );
  });

  it("renders empty state", () => {
    render(<UrlList urls={[]} />);

    expect(screen.getByText("No links yet")).toBeInTheDocument();
  });
});
