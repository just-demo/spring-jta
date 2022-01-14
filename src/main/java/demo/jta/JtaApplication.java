package demo.jta;

import static java.time.Instant.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class JtaApplication {

  private final List<String> jmsMessages = new ArrayList<>();

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private JmsTemplate jmsTemplate;

  @Transactional
  @PostMapping
  public String createMessage(@RequestParam(defaultValue = "false") boolean rollback) {
    String message = ISO_INSTANT.format(now());
    jdbcTemplate.update("insert into demo(message) values(?)", message);
    jmsTemplate.convertAndSend("demo", message);
    if (rollback) {
      throw new RuntimeException("Rolled back!");
    }
    return message;
  }

  @GetMapping
  public List<String> getMessages() {
    return Stream.concat(
            jmsMessages.stream().map(message -> message + " jms"),
            jdbcTemplate.queryForList("select message from demo", String.class).stream().map(message -> message + " jdbc"))
        .sorted()
        .collect(toList());
  }

  @JmsListener(destination = "demo")
  public void onMessage(String message) {
    jmsMessages.add(message);
  }

  public static void main(String[] args) {
    SpringApplication.run(JtaApplication.class, args);
  }

}
