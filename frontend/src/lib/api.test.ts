import { afterEach, describe, expect, it, vi } from "vitest";
import { getStats, getUrls } from "@/lib/api";

const mockFetch = (status: number, body: unknown = {}) =>
  vi.stubGlobal(
    "fetch",
    vi.fn().mockResolvedValue(new Response(JSON.stringify(body), { status })),
  );

afterEach(() => vi.unstubAllGlobals());

describe("api", () => {
  it("getUrls fetches from backend without cache", async () => {
    mockFetch(200, []);

    expect(await getUrls()).toEqual([]);
    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/urls", { cache: "no-store" });
  });

  it("getStats returns null on 404", async () => {
    mockFetch(404, { error: "not found" });

    expect(await getStats("missing")).toBeNull();
  });

  it("getStats throws on server error", async () => {
    mockFetch(500);

    await expect(getStats("abc")).rejects.toThrow("500");
  });
});
