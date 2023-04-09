package com.example.catfisharea.listeners;

import com.example.catfisharea.models.Campus;

public interface MultipleCampusListener {
    void onMultipleUserSelection(Boolean isSelected);
    void onChangeTeamLeadClicker(Campus campus);
}
