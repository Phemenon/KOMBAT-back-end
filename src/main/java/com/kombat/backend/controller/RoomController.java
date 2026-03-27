package com.kombat.backend.controller;

import com.kombat.backend.dto.CreateRoomRequest;
import com.kombat.backend.dto.JoinRoomRequest;
import com.kombat.backend.dto.StartGameRequest;
import com.kombat.backend.dto.ToggleReadyRequest;
import com.kombat.backend.dto.UpdateConfigRequest;
import com.kombat.backend.game.Engine.GameState.Mode_State.GameMode;
import com.kombat.backend.model.RoomModel;
import com.kombat.backend.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public RoomModel createRoom(@RequestBody CreateRoomRequest request) {
        GameMode mode = GameMode.valueOf(request.getMode().toUpperCase());

        return roomService.createRoom(
                request.getHostId(),
                request.getUserName(),
                mode
        );
    }

    @PostMapping("/join")
    public RoomModel joinRoom(@RequestBody JoinRoomRequest request) {
        return roomService.joinRoom(
                request.getRoomId(),
                request.getUserId(),
                request.getUserName()
        );
    }

    @GetMapping("/{roomId}")
    public RoomModel getRoom(@PathVariable String roomId) {
        return roomService.getRoom(roomId);
    }

    @PostMapping("/toggle-ready")
    public RoomModel toggleReady(@RequestBody ToggleReadyRequest request) {
        return roomService.toggleReady(request.getRoomId(), request.getUserId());
    }

    @PostMapping("/{roomId}/config")
    public RoomModel updateConfig(
            @PathVariable String roomId,
            @RequestBody UpdateConfigRequest request
    ) {
        return roomService.updateConfig(roomId, request);
    }

    @PostMapping("/start")
    public RoomModel startGame(@RequestBody StartGameRequest request) {
        return roomService.startGame(request.getRoomId(), request.getUserId());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleRuntimeException(RuntimeException e) {
        return e.getMessage();
    }
}