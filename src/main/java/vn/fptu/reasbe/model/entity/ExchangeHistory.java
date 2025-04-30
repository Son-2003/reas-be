package vn.fptu.reasbe.model.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Length;
import vn.fptu.reasbe.model.entity.core.AbstractAuditableEntity;
import vn.fptu.reasbe.model.enums.exchange.StatusExchangeHistory;

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
@Table(name = "EXCHANGE_HISTORY")
@AttributeOverride(name = "id", column = @Column(name = "EXCHANGE_HISTORY_ID"))
public class ExchangeHistory extends AbstractAuditableEntity {
    @NotNull
    @Column(name = "BUYER_CONFIRMATION")
    private Boolean buyerConfirmation;

    @NotNull
    @Column(name = "SELLER_CONFIRMATION")
    private Boolean sellerConfirmation;

    @Column(name = "BUYER_IMAGE_URL", length = Length.LOB_DEFAULT)
    private String buyerImageUrl;

    @Column(name = "SELLER_IMAGE_URL", length = Length.LOB_DEFAULT)
    private String sellerImageUrl;

    @Column(name = "BUYER_ADDITIONAL_NOTES")
    private String buyerAdditionalNotes;

    @Column(name = "SELLER_ADDITIONAL_NOTES")
    private String sellerAdditionalNotes;

    @NotNull
    @Column(name = "STATUS_EXCHANGE_HISTORY", length = 4)
    private StatusExchangeHistory statusExchangeHistory;

    @OneToOne(mappedBy = "exchangeHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    @OneToOne(mappedBy = "exchangeHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExchangeRequest exchangeRequest;
}
