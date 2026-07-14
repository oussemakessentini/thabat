package com.thabat.quran.task;

import com.thabat.quran.security.JwtUserPrincipal;
import com.thabat.quran.task.dto.CompleteQuranTaskRequest;
import com.thabat.quran.task.dto.QuranTaskMutationResponse;
import com.thabat.quran.task.dto.QuranTodayTasksResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quran/tasks")
@RequiredArgsConstructor
@Validated
public class QuranDailyTaskController {

    private final QuranDailyTaskService taskService;

    @GetMapping("/today")
    public QuranTodayTasksResponse getTodayTasks(
            @AuthenticationPrincipal JwtUserPrincipal principal
    ) {
        return taskService.getTodayTasks(principal.getUserId());
    }

    @PostMapping("/{taskId}/complete")
    public QuranTaskMutationResponse completeTask(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("taskId") UUID taskId,
            @Valid @RequestBody CompleteQuranTaskRequest request
    ) {
        return taskService.completeTask(principal.getUserId(), taskId, request);
    }

    @PostMapping("/{taskId}/skip")
    public QuranTaskMutationResponse skipTask(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable("taskId") UUID taskId
    ) {
        return taskService.skipTask(principal.getUserId(), taskId);
    }
}
