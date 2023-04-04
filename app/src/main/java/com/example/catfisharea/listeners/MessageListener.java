package com.example.catfisharea.listeners;

import com.example.catfisharea.models.ChatMessage;

import java.util.List;

public interface MessageListener {

    void onMessageSelection(Boolean isSelected, int position, List<ChatMessage> chatMessages, ChatMessage chatMessage);

}
