import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  
  constructor() {
    this.client = null;
    this.connected = false;
  }

  connect(onMessage) {
    if (this.connected) return;

    this.client = new Client({
      webSocketFactory: () => 
          new SockJS('http://localhost:8083/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      
      onConnect: () => {
        console.log('WebSocket Connected ✅');
        this.connected = true;

        this.client.subscribe('/topic/fraud-alerts', 
            (message) => {
              const alert = JSON.parse(message.body);
              console.log('New alert received:', alert);
              if (onMessage) onMessage(alert);
            });
      },

      onDisconnect: () => {
        console.log('WebSocket Disconnected ❌');
        this.connected = false;
      },

      onStompError: (frame) => {
        console.error('WebSocket Error:', frame);
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
    }
  }
}

export default new WebSocketService();