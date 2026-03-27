package com.kombat.backend.controller;

import com.kombat.backend.dto.RoomConfigMessageRequest;
import com.kombat.backend.dto.RoomMessageRequest;
import com.kombat.backend.dto.UpdateConfigRequest;
import com.kombat.backend.model.RoomModel;
import com.kombat.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{roomId}/ready")
    public void toggleReady(
            @DestinationVariable String roomId,
            RoomMessageRequest request
    ) {
        RoomModel room = roomService.toggleReady(roomId, request.getUserId());

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                room
        );
    }

    @MessageMapping("/room/{roomId}/config")
    public void updateConfig(
            @DestinationVariable String roomId,
            RoomConfigMessageRequest request
    ) {
        UpdateConfigRequest updateRequest = new UpdateConfigRequest();

        updateRequest.setUserId(request.getUserId());
        updateRequest.setSpawnCost(request.getSpawnCost());
        updateRequest.setHexPurchaseCost(request.getHexPurchaseCost());
        updateRequest.setInitBudget(request.getInitBudget());
        updateRequest.setTurnBudget(request.getTurnBudget());
        updateRequest.setMaxBudget(request.getMaxBudget());
        updateRequest.setBaseInterestPct(request.getBaseInterestPct());
        updateRequest.setInitHp(request.getInitHp());
        updateRequest.setMaxSpawns(request.getMaxSpawns());
        updateRequest.setMaxTurns(request.getMaxTurns());

        RoomModel room = roomService.updateConfig(roomId, updateRequest);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
    }

    @MessageMapping("/room/{roomId}/start")
    public void startGame(
            @DestinationVariable String roomId,
            RoomMessageRequest request
    ) {
        RoomModel room = roomService.startGame(roomId, request.getUserId());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
    }
}