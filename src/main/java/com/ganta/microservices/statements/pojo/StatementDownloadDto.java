package com.ganta.microservices.statements.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatementDownloadDto {
    private UUID statementId;
    private String downloadUrl;
    private LocalDateTime expiresAt;
}
