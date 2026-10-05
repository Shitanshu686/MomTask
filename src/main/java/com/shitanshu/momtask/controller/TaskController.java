package com.shitanshu.momtask.controller;

import com.shitanshu.momtask.model.Task;
import com.shitanshu.momtask.repository.TaskRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    private final RestClient restClient =
            RestClient.create("http://localhost:11434");

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @PostMapping
    public Task createTask(@RequestBody String message) {

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Task message cannot be empty"
            );
        }

        String lowerMessage =
                message.toLowerCase().trim();

        String prompt = """
                You are MomTask AI.

                Understand this Hindi/Hinglish reminder.

                Return ONLY valid JSON with exactly:
                task, dateHint, time

                Rules:
                - task = short English description of the task.
                - dateHint must be exactly one of:
                  today, tomorrow, none
                - time must be exactly one of:
                  morning, afternoon, evening, night, none
                - "aaj" = today
                - "kal" = tomorrow
                - "subah" = morning
                - "dopahar" = afternoon
                - "shaam" = evening
                - "raat" = night
                - Never invent an exact clock time.
                - "doodh lana" = "bring milk".

                User message:
                %s
                """.formatted(message.trim());

        Map<String, Object> request = Map.of(
                "model", "qwen3:1.7b",
                "prompt", prompt,
                "stream", false,
                "think", false,
                "format", "json",
                "options", Map.of(
                        "num_predict", 60
                )
        );

        Map response = restClient.post()
                .uri("/api/generate")
                .body(request)
                .retrieve()
                .body(Map.class);

        String aiResponse =
                response.get("response").toString();

        String taskName =
                extractValue(aiResponse, "task");

        String dateHint =
                extractValue(aiResponse, "dateHint");

        String time =
                extractValue(aiResponse, "time");

        // DATE
        String finalDate;

        if ("tomorrow".equalsIgnoreCase(dateHint)
                || lowerMessage.contains("kal")) {

            finalDate =
                    LocalDate.now()
                            .plusDays(1)
                            .toString();

        } else if ("today".equalsIgnoreCase(dateHint)
                || lowerMessage.contains("aaj")) {

            finalDate =
                    LocalDate.now().toString();

        } else {

            finalDate = "";
        }

        // TIME
        if (lowerMessage.contains("subah")) {

            time = "morning";

        } else if (lowerMessage.contains("dopahar")) {

            time = "afternoon";

        } else if (lowerMessage.contains("shaam")
                || lowerMessage.contains("sham")) {

            time = "evening";

        } else if (lowerMessage.contains("raat")) {

            time = "night";

        } else if ("none".equalsIgnoreCase(time)) {

            time = "";
        }

        Task task = new Task(
                taskName,
                finalDate,
                time
        );

        // Default reminder time.
        // User can change this from the UI later.
        task.setReminderTime(
                getDefaultReminderTime(time)
        );

        return taskRepository.save(task);
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/test")
    public String test() {
        return "MomTask browser connection is working!";
    }

    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Task not found"
                        ));
    }

    @PutMapping("/{id}/complete")
    public Task completeTask(
            @PathVariable Long id) {

        Task task =
                taskRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        task.setCompleted(
                !task.isCompleted()
        );

        return taskRepository.save(task);
    }

    @PutMapping("/{id}/reminder")
    public Task toggleReminder(
            @PathVariable Long id) {

        Task task =
                taskRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        task.setReminderEnabled(
                !task.isReminderEnabled()
        );

        return taskRepository.save(task);
    }

    @PutMapping("/{id}/reminder-time")
    public Task updateReminderTime(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Task task =
                taskRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Task not found"
                                ));

        String reminderTime =
                body.get("reminderTime");

        if (reminderTime == null
                || reminderTime.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Reminder time cannot be empty"
            );
        }

        task.setReminderTime(
                reminderTime.trim()
        );

        return taskRepository.save(task);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(
            @PathVariable Long id) {

        if (!taskRepository.existsById(id)) {
            return "Task not found";
        }

        taskRepository.deleteById(id);

        return "Task deleted successfully";
    }

    private String getDefaultReminderTime(
            String time) {

        if ("morning".equalsIgnoreCase(time)) {
            return "08:00";
        }

        if ("afternoon".equalsIgnoreCase(time)) {
            return "13:00";
        }

        if ("evening".equalsIgnoreCase(time)) {
            return "18:00";
        }

        if ("night".equalsIgnoreCase(time)) {
            return "21:00";
        }

        return "09:00";
    }

    private String extractValue(
            String json,
            String key) {

        String search = "\"" + key + "\"";

        int keyIndex =
                json.indexOf(search);

        if (keyIndex == -1) {
            return "";
        }

        int colonIndex =
                json.indexOf(":", keyIndex);

        if (colonIndex == -1) {
            return "";
        }

        int firstQuote =
                json.indexOf("\"", colonIndex);

        if (firstQuote == -1) {
            return "";
        }

        int secondQuote =
                json.indexOf(
                        "\"",
                        firstQuote + 1
                );

        if (secondQuote == -1) {
            return "";
        }

        return json.substring(
                firstQuote + 1,
                secondQuote
        );
    }
}
