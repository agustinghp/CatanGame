package org.skiBums;

import org.skiBums.Observers.IGameObserver;

import java.util.ArrayList;
import java.util.List;

public class TestObserver implements IGameObserver {
    List<String> receivedMessages = new ArrayList<>();

    public void update(String message) {
        receivedMessages.add(message);
    }

    public List<String> getMessages() {
        return receivedMessages;
    }

    public void clear(){
        receivedMessages.clear();
    }

    public int count() {
        return receivedMessages.size();
    }

    public int countMessage(String messageToCount) {
        int messageCount = 0;
        for (String message : receivedMessages) {
            if (message.equals(messageToCount)) {
                messageCount++;
            }
        }
        return messageCount;
    }

    public boolean hasMessage(String messageToCheck) {
        for (String message : receivedMessages) {
            if (message.equals(messageToCheck)) {
                return true;
            }
        }
        return false;
    }


}

