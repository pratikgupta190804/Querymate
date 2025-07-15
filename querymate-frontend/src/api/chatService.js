// src/api/chatService.js
import api from "./axios";

export const fetchChatHistory = (projectId) => {
  return api.get(`/projects/${projectId}/chat`);
};

export const sendChatMessage = (projectId, content) => {
  return api.post(`/projects/${projectId}/chat`, { content });
};
