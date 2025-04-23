package vn.fptu.reasbe.model.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.Length;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.fptu.reasbe.model.entity.core.AbstractAuditableEntity;
import vn.fptu.reasbe.model.enums.criticalreport.StatusCriticalReport;
import vn.fptu.reasbe.model.enums.criticalreport.TypeCriticalReport;

import java.time.LocalDateTime;

/**
 *
 * @author ntig
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CRITICAL_REPORT")
@AttributeOverride(name = "id", column = @Column(name = "CRITICAL_REPORT_ID"))
public class CriticalReport extends AbstractAuditableEntity {

    @NotNull
    @Column(name = "TYPE_REPORT")
    @Enumerated(EnumType.STRING)
    private TypeCriticalReport typeReport;

    @NotNull
    @Column(name = "CONTENT_REPORT")
    private String contentReport;

    @Column(name = "CONTENT_RESPONSE")
    private String contentResponse;

    @Column(name = "IMAGE_URL", length = Length.LOB_DEFAULT)
    private String imageUrl;

    @Column(name = "RESOLVED_TIME")
    private LocalDateTime resolvedTime;

    @NotNull
    @Column(name = "STATUS_CRITICAL_REPORT")
    @Enumerated(EnumType.STRING)
    private StatusCriticalReport statusCriticalReport;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "FEEDBACK_ID")
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "EXCHANGE_REQUEST_ID")
    private ExchangeRequest exchangeRequest;

    @ManyToOne
    @JoinColumn(name = "REPORTER_ID")
    private User reporter;

    @ManyToOne
    @JoinColumn(name = "ANSWERER_ID")
    private User answerer;
}
