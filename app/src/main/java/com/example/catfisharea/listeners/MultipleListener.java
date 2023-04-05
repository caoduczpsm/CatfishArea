package com.example.catfisharea.listeners;

import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;

public interface MultipleListener {
    void onMultipleUserSelection(Boolean isSelected);
    void onChangeTeamLeadClicker(User user);
    void onTaskClicker(Task task);
    void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection);
}