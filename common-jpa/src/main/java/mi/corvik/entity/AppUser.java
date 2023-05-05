package mi.corvik.entity;

import lombok.*;
import mi.corvik.entity.enums.UserState;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramUserId;

    @CreationTimestamp // аннотация навесит текущую дату в момент сохранения объекта в БД
    private LocalDateTime firstLoginDate;

    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private Boolean isActive; // флаг активного аккаунта
    @Enumerated(EnumType.STRING)
    private UserState state;
}