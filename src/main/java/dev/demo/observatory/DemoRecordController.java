package dev.demo.observatory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestController
@RequestMapping("/api/demo/records")
class DemoRecordController {
    private final DemoRecordService service;

    DemoRecordController(DemoRecordService service) {
        this.service = service;
    }

    @GetMapping
    List<DemoRecord> listRecords() {
        return service.listRecords();
    }
}

@RestControllerAdvice
class DemoErrorHandler {
    @ExceptionHandler(DemoSourceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    ApiError sourceUnavailable() {
        return new ApiError("SOURCE_UNAVAILABLE", "데모 자료를 읽을 수 없습니다.");
    }

    record ApiError(String code, String message) {
    }
}

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class DemoSourceUnavailableException extends RuntimeException {
    DemoSourceUnavailableException() {
        super("데모 자료 연결을 확인할 수 없습니다.");
    }
}
