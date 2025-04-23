package vn.fptu.reasbe.model.enums.exchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import vn.fptu.reasbe.model.enums.core.BaseEnum;

@Getter
@RequiredArgsConstructor
public enum StatusExchangeHistory implements BaseEnum.CodeAccessible {
    NOT_YET_EXCHANGE("NYEX"),
    PENDING_EVIDENCE("PDEV"),
    SUCCESSFUL("SUCC"),
    FAILED("FAIL");

    private final String code;

    public static StatusExchangeHistory toEnumConstant(String code) {
        return BaseEnum.toEnumConstant(StatusExchangeHistory.class, code);
    }
}
