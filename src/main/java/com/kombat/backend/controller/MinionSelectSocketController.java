package com.kombat.backend.controller;

import com.kombat.backend.dto.*;
import com.kombat.backend.model.MinionSelectState;
import com.kombat.backend.model.MinionStrategyEditorState;
import com.kombat.backend.service.MinionSelectService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MinionSelectSocketController {

    private final MinionSelectService minionSelectService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * frontend เรียกหลัง subscribe สำเร็จ
     * เพื่อขอ state ล่าสุดของหน้า minion select
     */
    @MessageMapping("/room/{roomId}/minion-select/sync")
    public void sync(
            @DestinationVariable String roomId,
            MinionSelectSyncRequest request
    ) {
        MinionSelectState state = minionSelectService.sync(
                roomId,
                request.getUserId()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                state
        );
    }

    /**
     * frontend เรียกตอนเลือก / ยกเลิกเลือก shared minion types
     */
    @MessageMapping("/room/{roomId}/minion-select/update")
    public void updateSelection(
            @DestinationVariable String roomId,
            MinionSelectionUpdateRequest request
    ) {
        MinionSelectState state = minionSelectService.updateSelection(
                roomId,
                request.getUserId(),
                request.getSelectedTypes()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                state
        );
    }

    /**
     * frontend เรียกตอนกด READY / UNREADY
     */
    @MessageMapping("/room/{roomId}/minion-select/ready")
    public void toggleReady(
            @DestinationVariable String roomId,
            MinionReadyRequest request
    ) {
        MinionSelectState state = minionSelectService.toggleReady(
                roomId,
                request.getUserId()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                state
        );
    }

    /**
     * frontend เรียกตอน host / guest กดเริ่มเกมจริง
     * ถ้าพร้อมครบจะสร้าง GameEngine และ mark gameStarted=true
     */
    @MessageMapping("/room/{roomId}/minion-select/start")
    public void startGameIfReady(
            @DestinationVariable String roomId
    ) {
        MinionSelectState state = minionSelectService.startGameIfReady(roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                state
        );
    }

    /**
     * เปิด editor ของ minion type ที่เลือก
     * ส่งกลับเฉพาะ user คนที่กด edit
     */
    @MessageMapping("/room/{roomId}/minion-select/strategy/open")
    public void openStrategyEditor(
            @DestinationVariable String roomId,
            OpenMinionStrategyRequest request
    ) {
        MinionStrategyEditorState response = minionSelectService.openStrategyEditor(
                roomId,
                request.getUserId(),
                request.getMinionType()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-strategy-editor/" + request.getUserId(),
                response
        );
    }

    /**
     * กดปุ่ม default ใน editor
     * ส่งค่า default strategy กลับเฉพาะ user คนที่กด
     */
    @MessageMapping("/room/{roomId}/minion-select/strategy/default")
    public void resetStrategyToDefault(
            @DestinationVariable String roomId,
            ResetMinionStrategyRequest request
    ) {
        MinionStrategyEditorState response = minionSelectService.resetStrategyToDefault(
                roomId,
                request.getUserId(),
                request.getMinionType()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-strategy-editor/" + request.getUserId(),
                response
        );
    }

    /**
     * กดปุ่ม valid ใน editor
     * backend จะ parse/validate strategy แล้วส่งผลกลับเฉพาะ user คนที่กด
     */
    @MessageMapping("/room/{roomId}/minion-select/strategy/validate")
    public void validateStrategy(
            @DestinationVariable String roomId,
            ValidateMinionStrategyRequest request
    ) {
        try {
            MinionStrategyEditorState response = minionSelectService.validateStrategy(
                    roomId,
                    request.getUserId(),
                    request.getMinionType(),
                    request.getStrategySource()
            );

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/minion-strategy-editor/" + request.getUserId(),
                    response
            );
        } catch (RuntimeException e) {
            MinionStrategyEditorState errorResponse = new MinionStrategyEditorState(
                    request.getMinionType(),
                    "",
                    request.getStrategySource(),
                    false,
                    e.getMessage()
            );

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/minion-strategy-editor/" + request.getUserId(),
                    errorResponse
            );
        }
    }

    /**
     * กดปุ่ม Confirm หลัง valid ผ่าน
     * backend จะ validate ซ้ำ และบันทึก strategy ลง room state จริง
     * จากนั้น broadcast state ใหม่ให้ทุกคนในห้อง
     */
    @MessageMapping("/room/{roomId}/minion-select/strategy/confirm")
    public void confirmStrategy(
            @DestinationVariable String roomId,
            ConfirmMinionStrategyRequest request
    ) {
        MinionSelectState state = minionSelectService.confirmStrategy(
                roomId,
                request.getUserId(),
                request.getMinionType(),
                request.getStrategySource()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                state
        );
    }

    @MessageMapping("/room/{roomId}/minion-select/defense/confirm")
    public void confirmDefense(
            @DestinationVariable String roomId,
            ConfirmMinionDefenseRequest req
    ) {
        MinionSelectState updatedState = minionSelectService.confirmDefenseFactor(
                roomId,
                req.getUserId(),
                req.getMinionType(),
                req.getDefenseFactor()
        );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                updatedState
        );
    }

    @MessageMapping("/room/{roomId}/minion-select/defense/default")
    public void resetDefense(
            @DestinationVariable String roomId,
            ConfirmMinionDefenseRequest req
    ) {
        MinionSelectState updatedState =
                minionSelectService.resetDefenseFactorToDefault(
                        roomId,
                        req.getUserId(),
                        req.getMinionType()
                );

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/minion-select",
                updatedState
        );
    }
}