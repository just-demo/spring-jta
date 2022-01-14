package demo.jta;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class DemoListener {

  public static final String DESTINATION = "demo";
  private final List<String> messages = new ArrayList<>();

  @JmsListener(destination = DESTINATION)
  public void onMessage(String message) {
    messages.add(message);
  }
}
