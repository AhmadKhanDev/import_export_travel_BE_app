const STORAGE_KEY = "tm_delivery_codes";

type StoredCodes = Record<string, { code: string; savedAt: number }>;

function readAll(): StoredCodes {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? "{}") as StoredCodes;
  } catch {
    return {};
  }
}

export function saveDeliveryCode(bookingId: string, code: string): void {
  const stored = readAll();
  stored[bookingId] = { code, savedAt: Date.now() };
  localStorage.setItem(STORAGE_KEY, JSON.stringify(stored));
}

export function getDeliveryCode(bookingId: string): string | null {
  return readAll()[bookingId]?.code ?? null;
}
