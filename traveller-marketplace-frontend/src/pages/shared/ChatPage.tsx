import { useState, useRef, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { useAuth } from "@/auth/AuthContext";
import { chatApi, type ChatRoomResponse, type ChatMessageResponse } from "@/services/chatApi";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { PageLoader } from "@/components/ui/LoadingSpinner";
import { EmptyState } from "@/components/common/EmptyState";
import { MessageSquare, Send } from "lucide-react";
import { formatDateTime } from "@/utils/formatters";

export function ChatPage() {
  const { user } = useAuth();
  const [searchParams] = useSearchParams();
  const bookingIdParam = searchParams.get("bookingId");
  const qc = useQueryClient();
  const [selectedRoomId, setSelectedRoomId] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Get chat rooms
  const { data: roomsData, isLoading: roomsLoading } = useQuery({
    queryKey: ["my-chat-rooms"],
    queryFn: () => chatApi.myRooms().then((r) => r.data.data),
    refetchInterval: 10_000,
  });

  // Create/get room for booking if bookingId passed
  const createRoomMutation = useMutation({
    mutationFn: (bookingId: string) => chatApi.createOrGetRoom(bookingId),
    onSuccess: (res) => {
      qc.invalidateQueries({ queryKey: ["my-chat-rooms"] });
      setSelectedRoomId(res.data.data.id);
    },
  });

  // If bookingId param exists, open chat for that booking
  useEffect(() => {
    if (bookingIdParam && !selectedRoomId) {
      createRoomMutation.mutate(bookingIdParam);
    }
  }, [bookingIdParam]);

  // Get messages for selected room
  const { data: messagesData } = useQuery({
    queryKey: ["chat-messages", selectedRoomId],
    queryFn: () => chatApi.getMessages(selectedRoomId!).then((r) => r.data.data),
    enabled: !!selectedRoomId,
    refetchInterval: 3_000,
  });

  const sendMutation = useMutation({
    mutationFn: (content: string) => chatApi.sendMessage(selectedRoomId!, { content }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["chat-messages", selectedRoomId] });
      setMessage("");
    },
  });

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messagesData]);

  const rooms = roomsData?.content ?? [];
  const messages = messagesData?.content ?? [];

  const handleSend = () => {
    if (!message.trim()) return;
    sendMutation.mutate(message.trim());
  };

  if (roomsLoading) return <PageLoader />;

  return (
    <div className="flex h-[calc(100vh-8rem)] gap-4">
      {/* Room list */}
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
                <p className="text-xs text-gray-400 truncate">Booking: {room.bookingId.slice(0, 8)}…</p>
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

      {/* Message area */}
      <div className="flex flex-1 flex-col rounded-xl border border-gray-200 bg-white">
        {!selectedRoomId ? (
          <div className="flex flex-1 items-center justify-center">
            <EmptyState icon={MessageSquare} title="Select a chat" description="Choose a conversation to start messaging" />
          </div>
        ) : (
          <>
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map((msg: ChatMessageResponse) => {
                const isMe = msg.senderId === user?.id;
                return (
                  <div key={msg.id} className={`flex ${isMe ? "justify-end" : "justify-start"}`}>
                    <div className={`max-w-xs rounded-2xl px-4 py-2 ${isMe ? "bg-primary-600 text-white" : "bg-gray-100 text-gray-800"}`}>
                      <p className="text-sm">{msg.content}</p>
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
                  placeholder="Type a message…"
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
