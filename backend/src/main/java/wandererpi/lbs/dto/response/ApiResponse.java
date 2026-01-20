package wandererpi.lbs.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T1, T2> {
    private Long timestamp;
    private Integer statusCode;
    private T1 message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T2 result;
}