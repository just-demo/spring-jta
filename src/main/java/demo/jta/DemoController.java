package demo.jta;

import static demo.jta.DemoListener.DESTINATION;
import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class DemoController {

  private final DemoListener demoListener;
  private final JdbcTemplate jdbcTemplate;
  private final JmsTemplate jmsTemplate;

  @Transactional
  @PostMapping
  public String createMessage(@RequestParam(defaultValue = "false") boolean rollback) {
    String message = ISO_INSTANT.format(now());
    jdbcTemplate.update("insert into demo(message) values(?)", message);
    jmsTemplate.convertAndSend(DESTINATION, message);
    if (rollback) {
      throw new RuntimeException("Rolled back!");
    }
    return message;
  }

  @GetMapping
  public List<String> getMessages() {
    return Stream.concat(
            demoListener.getMessages().stream().map(message -> message + " jms"),
            jdbcTemplate.queryForList("select message from demo", String.class).stream().map(message -> message + " jdbc"))
        .sorted()
        .collect(toList());
  }
}
