package io.elasticore.demo.crm.adapter.in.web;

import io.elasticore.demo.crm.application.MemberService;
import io.elasticore.demo.crm.adapter.in.assistant.AssistantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/crm/assistant {"message": "...", "conversationId": "...", "language": "ko|en"}
 * - the assistant's answer and the records it used. 503 when no AI model is configured.
 */
@RestController
@RequestMapping("/api/crm/assistant")
public class AssistantController {

    public record Ask(String message, String conversationId, String language) {}

    private final AssistantService assistant;
    private final MemberService members;

    public AssistantController(AssistantService assistant, MemberService members) {
        this.assistant = assistant;
        this.members = members;
    }

    @PostMapping
    public AssistantService.Answer ask(@RequestBody Ask ask) {
        if (ask.message() == null || ask.message().isBlank()) {
            throw new io.elasticore.springboot3.validation.BadRequestException("message is empty");
        }
        if (ask.message().length() > 2000) {
            throw new io.elasticore.springboot3.validation.BadRequestException("message is longer than 2000 characters");
        }
        String name = members.me().map(m -> m.getName()).orElse(null);
        return assistant.ask(ask.message().trim(), ask.conversationId(), ask.language(), name);
    }

    @ExceptionHandler(AssistantService.AssistantFailedException.class)
    ResponseEntity<ProblemDetail> failed(AssistantService.AssistantFailedException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
        p.setProperty("code", "assistant.failed");
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(p);
    }

    @ExceptionHandler(AssistantService.AssistantUnavailableException.class)
    ResponseEntity<ProblemDetail> unavailable(AssistantService.AssistantUnavailableException e) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        p.setProperty("code", "assistant.unavailable");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(p);
    }
}
