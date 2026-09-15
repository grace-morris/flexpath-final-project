import { beforeEach, describe, expect, test, vi } from "vitest";
import { api } from "./apiClient";

/**
 * Unit tests for apiClient.js, exercised through the api.get/post/put/patch/del wrappers.
 */

function fakeResponse({ ok = true, status = 200, json = null, text = "" } = {}) {
  return {
    ok,
    status,
    json: async () => json,
    text: async () => text,
  };
}

describe("apiClient", () => {
  beforeEach(() => {
    global.fetch = vi.fn();
    global.localStorage = {
      store: {},
      getItem(key) {
        return Object.prototype.hasOwnProperty.call(this.store, key) ? this.store[key] : null;
      },
      setItem(key, value) {
        this.store[key] = value;
      },
      removeItem(key) {
        delete this.store[key];
      },
    };
    global.window = { location: { href: "" } };
  });

  describe("request building", () => {
    test("get sends a GET request to /api plus the given path, with no body", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ json: { hello: "world" } }));

      await api.get("/monsters", "my-token");

      expect(global.fetch).toHaveBeenCalledWith(
        "/api/monsters",
        expect.objectContaining({ method: "GET", body: undefined })
      );
    });

    test("post sends a POST request with a JSON-stringified body", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ status: 201, json: { id: 1 } }));

      await api.post("/monsters", "my-token", { name: "Goblin" });

      expect(global.fetch).toHaveBeenCalledWith(
        "/api/monsters",
        expect.objectContaining({ method: "POST", body: JSON.stringify({ name: "Goblin" }) })
      );
    });

    test("del sends a DELETE request", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ status: 204 }));

      await api.del("/monsters/1", "my-token");

      expect(global.fetch).toHaveBeenCalledWith("/api/monsters/1", expect.objectContaining({ method: "DELETE" }));
    });

    test("attaches an Authorization header when a token is provided", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ json: {} }));

      await api.get("/monsters", "my-token");

      const [, options] = global.fetch.mock.calls[0];
      expect(options.headers.Authorization).toBe("Bearer my-token");
    });

    test("omits the Authorization header entirely when no token is provided", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ json: {} }));

      await api.get("/monsters", undefined);

      const [, options] = global.fetch.mock.calls[0];
      expect(options.headers.Authorization).toBeUndefined();
    });
  });

  describe("response handling", () => {
    test("a successful response's JSON body is returned", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ json: { id: 1, name: "Goblin" } }));

      const result = await api.get("/monsters/1", "my-token");

      expect(result).toEqual({ id: 1, name: "Goblin" });
    });

    test("a 204 No Content response returns null instead of trying to parse JSON", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ status: 204 }));

      const result = await api.del("/monsters/1", "my-token");

      expect(result).toBeNull();
    });

    test("a non-401 error response throws using the response body as the message", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ ok: false, status: 500, text: "Something broke" }));

      await expect(api.get("/monsters", "my-token")).rejects.toThrow("Something broke");
    });

    test("a non-401 error response with no body falls back to a generic message naming the path and status", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ ok: false, status: 404, text: "" }));

      await expect(api.get("/monsters/999", "my-token")).rejects.toThrow(/monsters\/999.*404/);
    });
  });

  describe("401 handling", () => {
    test("a 401 clears the stored session and redirects to /login", async () => {
      localStorage.setItem("token", "expired-token");
      localStorage.setItem("username", "grace");
      localStorage.setItem("isAdmin", "false");
      global.fetch.mockResolvedValue(fakeResponse({ ok: false, status: 401 }));

      await expect(api.get("/monsters", "expired-token")).rejects.toThrow(/session expired/i);

      expect(localStorage.getItem("token")).toBeNull();
      expect(localStorage.getItem("username")).toBeNull();
      expect(localStorage.getItem("isAdmin")).toBeNull();
      expect(localStorage.getItem("sessionExpired")).toBe("true");
      expect(window.location.href).toBe("/login");
    });

    test("a 401 is handled before the generic error-response check, not swallowed by it", async () => {
      global.fetch.mockResolvedValue(fakeResponse({ ok: false, status: 401, text: "Unauthorized" }));

      await expect(api.get("/monsters", "expired-token")).rejects.toThrow(/session expired/i);
      expect(window.location.href).toBe("/login");
    });
  });
});