package vn.fptu.reasbe.model.enums.item;

import vn.fptu.reasbe.model.enums.core.BaseEnum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author ntig
 */
@Getter
@RequiredArgsConstructor
public enum StatusItem implements BaseEnum.CodeAccessible {
    AVAILABLE("AVAI"),
    EXCHANGED("EXCH"),
    IN_EXCHANGE("INEX"),
    EXPIRED("EXPI"),
    NO_LONGER_FOR_EXCHANGE("NLFE"),
    PENDING("PEND"),
    REJECTED("REJE");

    private final String code;

    public static StatusItem toEnumConstant(String code) {
        return BaseEnum.toEnumConstant(StatusItem.class, code);
    }
}
