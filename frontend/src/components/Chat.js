import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  List,
  ListItem,
  ListItemText,
  Divider,
} from '@mui/material';
import { Client } from '@stomp/stompjs';
import axios from 'axios';

const Chat = () => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [currentChat, setCurrentChat] = useState(null);
  const [chats, setChats] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    fetchChats();
    setupWebSocket();
    return () => {
      if (stompClient) {
        stompClient.deactivate();
      }
    };
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const setupWebSocket = () => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('token')}`,
      },
      debug: function (str) {
        console.log(str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    client.onConnect = () => {
      client.subscribe('/user/queue/messages', (message) => {
        const receivedMessage = JSON.parse(message.body);
        setMessages((prevMessages) => [...prevMessages, receivedMessage]);
      });
    };

    client.activate();
    setStompClient(client);
  };

  const fetchChats = async () => {
    try {
      const response = await axios.get('/api/chats');
      setChats(response.data);
      if (response.data.length > 0 && !currentChat) {
        setCurrentChat(response.data[0]);
        fetchMessages(response.data[0].id);
      }
    } catch (error) {
      console.error('Error fetching chats:', error);
    }
  };

  const fetchMessages = async (chatId) => {
    try {
      const response = await axios.get(`/api/chats/${chatId}/messages`);
      setMessages(response.data);
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  };

  const createNewChat = async () => {
    try {
      const response = await axios.post('/api/chats', { title: 'New Chat' });
      setChats((prevChats) => [response.data, ...prevChats]);
      setCurrentChat(response.data);
      setMessages([]);
    } catch (error) {
      console.error('Error creating chat:', error);
    }
  };

  const sendMessage = () => {
    if (!newMessage.trim() || !currentChat) return;

    const message = {
      chatId: currentChat.id,
      content: newMessage,
      isUserMessage: true,
    };

    if (stompClient && stompClient.connected) {
      stompClient.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(message),
      });
    }

    setMessages((prevMessages) => [...prevMessages, message]);
    setNewMessage('');
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  return (
    <Box sx={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
      {/* Sidebar */}
      <Paper
        sx={{
          width: 250,
          p: 2,
          display: 'flex',
          flexDirection: 'column',
          gap: 2,
        }}
      >
        <Button
          variant="contained"
          fullWidth
          onClick={createNewChat}
        >
          New Chat
        </Button>
        <List>
          {chats.map((chat) => (
            <React.Fragment key={chat.id}>
              <ListItem
                button
                selected={currentChat?.id === chat.id}
                onClick={() => {
                  setCurrentChat(chat);
                  fetchMessages(chat.id);
                }}
              >
                <ListItemText primary={chat.title} />
              </ListItem>
              <Divider />
            </React.Fragment>
          ))}
        </List>
      </Paper>

      {/* Chat area */}
      <Box sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
        <Paper
          sx={{
            flexGrow: 1,
            p: 2,
            overflow: 'auto',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          {messages.map((message, index) => (
            <Box
              key={index}
              sx={{
                alignSelf: message.isUserMessage ? 'flex-end' : 'flex-start',
                maxWidth: '70%',
                mb: 2,
              }}
            >
              <Paper
                sx={{
                  p: 2,
                  bgcolor: message.isUserMessage ? 'primary.main' : 'background.paper',
                  color: message.isUserMessage ? 'white' : 'text.primary',
                }}
              >
                <Typography>{message.content}</Typography>
              </Paper>
            </Box>
          ))}
          <div ref={messagesEndRef} />
        </Paper>

        {/* Message input */}
        <Box sx={{ p: 2 }}>
          <TextField
            fullWidth
            variant="outlined"
            placeholder="Type a message..."
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyPress={(e) => {
              if (e.key === 'Enter') {
                sendMessage();
              }
            }}
            InputProps={{
              endAdornment: (
                <Button
                  variant="contained"
                  onClick={sendMessage}
                  disabled={!newMessage.trim()}
                >
                  Send
                </Button>
              ),
            }}
          />
        </Box>
      </Box>
    </Box>
  );
};

export default Chat; 