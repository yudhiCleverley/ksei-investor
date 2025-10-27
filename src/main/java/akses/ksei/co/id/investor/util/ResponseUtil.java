package akses.ksei.co.id.investor.util;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseUtil {

	public Map<String, Object> successBody(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", new Date());
		body.put("status", HttpStatus.OK.value());
		body.put("success", HttpStatus.OK.name());
		body.put("message", message);

		return body;
	}

	public ResponseEntity<Map<String, Object>> badRequest(String message) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
	}

	public ResponseEntity<Map<String, Object>> unauthorized(String message) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, message);
	}

	public ResponseEntity<Map<String, Object>> notFound(String message) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, message);
	}

	public ResponseEntity<Map<String, Object>> success(String message) {
		return ResponseEntity.ok(successBody(message));
	}

	public ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", new Date());
		body.put("status", status.value());
		body.put("error", status.name());
		body.put("message", message);

		return ResponseEntity.status(status).body(body);
	}

}
