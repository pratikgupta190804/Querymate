// src/components/chat/ChatSection.jsx
import { useEffect, useRef, useState } from "react";
import { fetchChatHistory, sendChatMessage } from "../api/chatService";

const ChatSection = ({ projectId }) => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);

  const loadChat = async () => {
    try {
      const res = await fetchChatHistory(projectId);
      setMessages(res.data);
    } catch (err) {
      console.error("Failed to load chat", err);
    }
  };

  const handleSend = async () => {
    if (!input.trim()) return;

    const userMessage = {
      messageId: Date.now(),
      sender: "user",
      content: input,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setLoading(true);

    try {
      const res = await sendChatMessage(projectId, input);
      const botReply = res.data;
      setMessages((prev) => [...prev, botReply]);
    } catch (err) {
      console.error("Failed to send message", err);
    }

    setLoading(false);
  };

  useEffect(() => {
    if (projectId) {
      loadChat();
    }
  }, [projectId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <div className="flex flex-col h-full">
      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50 border rounded">
        {messages.map((msg) => (
          <div
            key={msg.messageId}
            className={`p-3 rounded-md max-w-[80%] ${
              msg.sender === "user" ? "bg-blue-100 ml-auto" : "bg-gray-200"
            }`}
          >
            <strong>{msg.sender === "user" ? "You" : "AI"}:</strong> {msg.content}
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      {/* Input Box */}
      <div className="mt-4 sticky bottom-0 bg-white py-2">
        <div className="flex gap-2 border-t pt-2 px-1">
          <input
            className="flex-1 border rounded px-3 py-2"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Ask something about your data..."
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
          />
          <button
            className="bg-blue-600 text-white px-4 rounded hover:bg-blue-700"
            onClick={handleSend}
            disabled={loading}
          >
            {loading ? "Sending..." : "Send"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatSection;
