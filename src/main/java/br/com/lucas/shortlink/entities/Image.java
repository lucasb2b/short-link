package br.com.lucas.shortlink.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "TB_IMAGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID idImage;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String contentType; // Ex: image/png, image/jpeg

    @Column(nullable = false)
    private Long fileSize; // Tamanho do arquivo em bytes

    @Column(nullable = false)
    private String storageUrl; // URL de onde a imagem foi salva (ex: AWS S3)

    @Column(unique = true, nullable = false)
    private String shortCode; // Código curto para o link (ex: xyz123)

    // A data de expiração. Se for null, não expira nunca (usuário autenticado).
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // Relacionamento opcional. Se for nulo, foi upload anônimo.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    // ElementCollection cria uma tabela dependente para armazenar a lista de strings
    @ElementCollection
    @CollectionTable(name = "TB_IMAGE_TAGS", joinColumns = @JoinColumn(name = "image_id"))
    @Column(name = "tag")
    private List<String> tags;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}