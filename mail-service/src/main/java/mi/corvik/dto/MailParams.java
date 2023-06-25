package mi.corvik.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class MailParams {
    private String id;  // user id
    private String emailTo;
}