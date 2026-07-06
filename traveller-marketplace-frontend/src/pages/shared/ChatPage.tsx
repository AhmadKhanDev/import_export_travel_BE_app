import { useEffect, useRef, useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import { chatApi, type ChatMessageResponse, type ChatRoomResponse } from "@/services/chatApi";
import { createRealtimeSocketClient, type RealtimeEvent } from "@/services/realtimeSocket";
import { Button } from "@/components/ui/Button";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { MessageSquare, Send } from "lucide-react";
import { formatDateTime } from "@/utils/formatters";

function sortRooms(rooms: ChatRoomResponse[]): ChatRoomResponse[] {
  return [...rooms].sort((a, b) => {
    const aTime = Date.parse(a.lastMessageAt ?? a.updatedAt ?? a.createdAt);
    const bTime = Date.parse(b.lastMessageAt ?? b.updatedAt ?? b.createdAt);
    return bTime - aTime;
  });
}

function mergeRooms(current: ChatRoomResponse[], nextRooms: ChatRoomResponse[]): ChatRoomResponse[] {
  const roomMap = new Map(current.map((room) => [room.id, room]));
  nextRooms.forEach((room) => {
    roomMap.set(room.id, { ...roomMap.get(room.id), ...room });
  });
  return sortRooms([...roomMap.values()]);
}

function mergeMessages(current: ChatMessageResponse[], nextMessages: ChatMessageResponse[]): ChatMessageResponse[] {
  const messageMap = new Map(current.map((message) => [message.id, message]));
  nextMessages.forEach((message) => {
    messageMap.set(message.id, { ...messageMap.get(message.id), ...message });
  });

  return [...messageMap.values()].sort(
    (a, b) => Date.parse(a.sentAt) - Date.parse(b.sentAt),
  );
}

export function ChatPage() {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const bookingIdParam = searchParams.get("bookingId");
  const [selectedRoomId, setSelectedRoomId] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const [rooms, setRooms] = useState<ChatRoomResponse[]>([]);
  const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { data: roomsData, isLoading: roomsLoading } = useQuery({
    queryKey: ["my-chat-rooms"],
    queryFn: () => chatApi.myRooms().then((r) => r.data.data),
  });

  const { data: messagesData } = useQuery({
    queryKey: ["chat-messages", selectedRoomId],
    queryFn: () => chatApi.getMessages(selectedRoomId!).then((r) => r.data.data),
    enabled: !!selectedRoomId,
  });

  const markReadMutation = useMutation({
    mutationFn: (roomId: string) => chatApi.markRead(roomId),
  });

  const createRoomMutation = useMutation({
    mutationFn: (bookingId: string) => chatApi.createOrGetRoom(bookingId),
    onSuccess: (res) => {
      setRooms((current) => mergeRooms(current, [res.data.data]));
      setSelectedRoomId(res.data.data.id);
    },
  });

  const sendMutation = useMutation({
    mutationFn: (content: string) =>
      chatApi.sendMessage(selectedRoomId!, { message: content, messageType: "TEXT" }),
    onSuccess: (res) => {
      setMessages((current) => mergeMessages(current, [res.data.data]));
      setMessage("");
    },
  });

  useEffect(() => {
    if (!roomsData?.content) return;
    setRooms((current) => mergeRooms(current, roomsData.content));
  }, [roomsData]);

  useEffect(() => {
    if (bookingIdParam && !selectedRoomId) {
      createRoomMutation.mutate(bookingIdParam);
    }
  }, [bookingIdParam, selectedRoomId]);

  useEffect(() => {
    if (!selectedRoomId && rooms.length > 0) {
      const preferredRoom = bookingIdParam
        ? rooms.find((room) => room.bookingId === bookingIdParam)
        : rooms[0];
      setSelectedRoomId(preferredRoom?.id ?? rooms[0].id);
    }
  }, [bookingIdParam, rooms, selectedRoomId]);

  useEffect(() => {
    if (!selectedRoomId) {
      setMessages([]);
      return;
    }

    setMessages([]);
  }, [selectedRoomId]);

  useEffect(() => {
    if (!messagesData?.content) return;
    setMessages((current) => mergeMessages(current, messagesData.content));
  }, [messagesData]);

  useEffect(() => {
    if (!user) return;

    const socket = createRealtimeSocketClient();
    const unsubscribeInbox = socket.subscribe(
      { channel: "chat-inbox" },
      (event: RealtimeEvent<ChatRoomResponse>) => {
        if (event.type !== "CHAT_ROOM_UPDATED") return;
        setRooms((current) => mergeRooms(current, [event.payload]));
      },
    );

    return () => {
      unsubscribeInbox();
    };
  }, [user]);

  useEffect(() => {
    if (!selectedRoomId || !user) return;

    markReadMutation.mutate(selectedRoomId);

    const socket = createRealtimeSocketClient();
    const unsubscribeRoom = socket.subscribe(
      { channel: "chat-room", roomId: selectedRoomId },
      (event: RealtimeEvent<ChatMessageResponse>) => {
        if (event.type !== "CHAT_MESSAGE_CREATED") return;

        setMessages((current) => mergeMessages(current, [event.payload]));
        if (event.payload.senderId !== user.id) {
          markReadMutation.mutate(selectedRoomId);
        }
      },
    );

    return () => {
      unsubscribeRoom();
    };
  }, [selectedRoomId, user]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSend = () => {
    if (!message.trim()) return;
    sendMutation.mutate(message.trim());
  };

  if (roomsLoading) return <PageLoader />;

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-4">
      <div className="w-64 shrink-0 overflow-y-auto rounded-xl border border-gray-200 bg-white">
        <div className="border-b border-gray-100 p-3">
          <h2 className="font-semibold text-gray-900">Chats</h2>
        </div>
        {rooms.length === 0 ? (
          <div className="p-4 text-center text-sm text-gray-400">No chats yet</div>
        ) : (
          rooms.map((room: ChatRoomResponse) => {
            const otherName = user?.id === room.buyerId ? room.travellerName : room.buyerName;
            return (
              <button
                key={room.id}
                className={`w-full border-b border-gray-100 px-3 py-3 text-left hover:bg-gray-50 ${selectedRoomId === room.id ? "bg-blue-50" : ""}`}
                onClick={() => setSelectedRoomId(room.id)}
              >
                <p className="text-sm font-medium text-gray-800">{otherName}</p>
                <p className="truncate text-xs text-gray-400">Booking: {room.bookingId.slice(0, 8)}...</p>
                {room.unreadCount && room.unreadCount > 0 ? (
                  <span className="inline-flex h-4 w-4 items-center justify-center rounded-full bg-primary-600 text-[10px] font-bold text-white">
                    {room.unreadCount}
                  </span>
                ) : null}
              </button>
            );
          })
        )}
      </div>

      <div className="flex flex-1 flex-col rounded-xl border border-gray-200 bg-white">
        {!selectedRoomId ? (
          <div className="flex flex-1 items-center justify-center">
            <EmptyState
              icon={MessageSquare}
              title="Select a chat"
              description="Choose a conversation to start messaging"
            />
          </div>
        ) : (
          <>
            <div className="flex-1 space-y-3 overflow-y-auto p-4">
              {messages.map((msg: ChatMessageResponse) => {
                const isMe = msg.senderId === user?.id;
                return (
                  <div key={msg.id} className={`flex ${isMe ? "justify-end" : "justify-start"}`}>
                    <div
                      className={`max-w-xs rounded-2xl px-4 py-2 ${isMe ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-800"}`}
                    >
                      <p className="text-sm">{msg.message}</p>
                      <p className={`mt-0.5 text-[10px] ${isMe ? "text-white/60" : "text-gray-400"}`}>
                        {formatDateTime(msg.sentAt)}
                      </p>
                    </div>
                  </div>
                );
              })}
              <div ref={messagesEndRef} />
            </div>

            <div className="border-t border-gray-100 p-3">
              <div className="flex gap-2">
                <input
                  className="flex-1 rounded-xl border border-gray-300 px-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-100"
                  placeholder="Type a message..."
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && handleSend()}
                />
                <Button onClick={handleSend} loading={sendMutation.isPending} icon={<Send size={15} />} size="sm">
                  Send
                </Button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
