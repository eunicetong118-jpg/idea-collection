import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = [];
  }

  connect(token, onConnected, onError) {
    this.client = new Client({
      webSocketFactory: () => new SockJS(`${API_URL}/ws`),
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      onConnect: () => {
        if (onConnected) onConnected();
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
      },
      onStompError: (frame) => {
        if (onError) onError(frame);
      }
    });

    this.client.activate();
  }

  subscribe(topic, callback) {
    if (!this.client || !this.client.connected) {
      console.warn('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe(topic, (message) => {
      const data = JSON.parse(message.body);
      callback(data);
    });

    this.subscriptions.push(subscription);
    return subscription;
  }

  subscribeToSubTopic(subTopicId, callback) {
    return this.subscribe(`/topic/ideas/${subTopicId}`, callback);
  }

  subscribeToLikes(ideaId, callback) {
    return this.subscribe(`/topic/ideas/${ideaId}/likes`, callback);
  }

  subscribeToComments(ideaId, callback) {
    return this.subscribe(`/topic/ideas/${ideaId}/comments`, callback);
  }

  disconnect() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.subscriptions = [];

    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
  }
}

export default new WebSocketService();
