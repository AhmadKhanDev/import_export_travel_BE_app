import { tokenStorage } from "@/utils/tokenStorage";

export type RealtimeChannel = "tracking-booking" | "chat-room" | "chat-inbox";

export interface RealtimeSubscription {
  channel: RealtimeChannel;
  bookingId?: string;
  roomId?: string;
}

export interface RealtimeEvent<T = unknown> {
  type: string;
  channel: RealtimeChannel;
  bookingId?: string;
  roomId?: string;
  payload: T;
  timestamp: string;
}

type RealtimeListener<T = unknown> = (event: RealtimeEvent<T>) => void;

function buildSubscriptionKey(subscription: RealtimeSubscription): string {
  return [
    subscription.channel,
    subscription.bookingId ?? "",
    subscription.roomId ?? "",
  ].join("|");
}

function buildSocketUrl(): string {
  const explicitUrl = import.meta.env.VITE_WS_BASE_URL as string | undefined;
  if (explicitUrl) return explicitUrl;

  const apiBaseUrl = new URL(import.meta.env.VITE_API_BASE_URL as string);
  apiBaseUrl.protocol = apiBaseUrl.protocol === "https:" ? "wss:" : "ws:";
  apiBaseUrl.pathname = "/ws";
  apiBaseUrl.search = "";
  apiBaseUrl.hash = "";
  return apiBaseUrl.toString();
}

export class RealtimeSocketClient {
  private socket: WebSocket | null = null;
  private manuallyClosed = false;
  private reconnectTimer: number | null = null;
  private disconnectTimer: number | null = null;
  private shouldCloseAfterConnect = false;
  private readonly listeners = new Map<string, Set<RealtimeListener<any>>>();
  private readonly subscriptions = new Map<string, RealtimeSubscription>();

  subscribe<T>(subscription: RealtimeSubscription, listener: RealtimeListener<T>): () => void {
    this.cancelPendingDisconnect();
    const key = buildSubscriptionKey(subscription);
    const current = this.listeners.get(key) ?? new Set<RealtimeListener<any>>();
    current.add(listener as RealtimeListener<any>);
    this.listeners.set(key, current);
    this.subscriptions.set(key, subscription);

    this.connect();
    this.sendSubscriptionMessage("subscribe", subscription);

    return () => {
      const remaining = this.listeners.get(key);
      if (!remaining) return;

      remaining.delete(listener);
      if (remaining.size === 0) {
        this.listeners.delete(key);
        this.subscriptions.delete(key);
        this.sendSubscriptionMessage("unsubscribe", subscription);
      }

      if (this.listeners.size === 0) {
        this.disconnect();
      }
    };
  }

  disconnect(): void {
    this.manuallyClosed = true;
    if (this.reconnectTimer !== null) {
      window.clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (!this.socket) {
      return;
    }

    if (this.socket.readyState === WebSocket.CONNECTING) {
      this.shouldCloseAfterConnect = true;
      this.scheduleDisconnect();
      return;
    }

    this.closeSocket();
  }

  private connect(): void {
    this.cancelPendingDisconnect();
    const token = tokenStorage.getAccessToken();
    if (!token) return;
    if (this.socket && (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)) {
      return;
    }

    this.manuallyClosed = false;
    const url = new URL(buildSocketUrl());
    url.searchParams.set("token", token);

    this.socket = new WebSocket(url.toString());
    this.socket.onopen = () => {
      if (this.shouldCloseAfterConnect && this.listeners.size === 0) {
        this.shouldCloseAfterConnect = false;
        this.closeSocket();
        return;
      }

      this.subscriptions.forEach((subscription) => {
        this.sendSubscriptionMessage("subscribe", subscription);
      });
    };
    this.socket.onmessage = (event) => {
      const payload = JSON.parse(event.data) as RealtimeEvent;
      const key = buildSubscriptionKey({
        channel: payload.channel,
        bookingId: payload.bookingId,
        roomId: payload.roomId,
      });
      this.listeners.get(key)?.forEach((listener) => listener(payload));
    };
    this.socket.onclose = () => {
      this.socket = null;
      this.shouldCloseAfterConnect = false;
      if (!this.manuallyClosed && this.listeners.size > 0) {
        this.reconnectTimer = window.setTimeout(() => this.connect(), 2000);
      }
    };
  }

  private sendSubscriptionMessage(type: "subscribe" | "unsubscribe", subscription: RealtimeSubscription): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;

    this.socket.send(JSON.stringify({
      type,
      channel: subscription.channel,
      bookingId: subscription.bookingId,
      roomId: subscription.roomId,
    }));
  }

  private scheduleDisconnect(): void {
    this.cancelPendingDisconnect();
    this.disconnectTimer = window.setTimeout(() => {
      this.disconnectTimer = null;
      if (this.listeners.size > 0) {
        this.shouldCloseAfterConnect = false;
        return;
      }

      if (!this.socket) {
        return;
      }

      if (this.socket.readyState === WebSocket.CONNECTING) {
        this.shouldCloseAfterConnect = true;
        return;
      }

      this.closeSocket();
    }, 800);
  }

  private cancelPendingDisconnect(): void {
    if (this.disconnectTimer !== null) {
      window.clearTimeout(this.disconnectTimer);
      this.disconnectTimer = null;
    }
    this.shouldCloseAfterConnect = false;
  }

  private closeSocket(): void {
    this.cancelPendingDisconnect();
    if (!this.socket) {
      return;
    }
    this.socket.close();
    this.socket = null;
  }
}

const sharedRealtimeSocketClient = new RealtimeSocketClient();

export function createRealtimeSocketClient(): RealtimeSocketClient {
  return sharedRealtimeSocketClient;
}
